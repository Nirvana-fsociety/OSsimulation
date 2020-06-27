package osPackage;

import java.util.ArrayList;

public class MemoryManageSystem {
	private final static int UNIT_SIZE = 2;// 一个单元2字节

	private final static int BUFFER_PAGENUM = 0;// 缓冲区页框号
	private final static int SYSTEM_START_PAGENUM = 0;// 系统区起始页框号
	private final static int PAGETABLE_PAGEID = 1;// 页表物理块号
	private final static int PCBQUEUE_PAGEID = 2;// PCB队列的起始物理块号
	private final static int SUPERBLOCK_COPY_PAGEID = 31;// 外存文件卷的超级块复制映像
	private final static int USER_START_PAGENUM = 32;// 用户区起始页框号

	private final static int FILE_START_ADDRESS = 0;// 文件区的起始地址
	private final static int GUIDE_ADDRESS = 0;// 引导块外存地址
	private final static int SUPERBLOCK_ADDRESS = 1;// 超级块外存地址
	private final static int INODE_START_ADDRESS = 2;// inode区起始外存地址
	private final static int INODE_BLOCKNUM = 5;// inode区的所占外存块数
	private final static int DEVICE_START_ADDRESS = 1800;// 设备文件区的起始外存地址
	private final static int DEVICE_BLOCKNUM = 4;// 设备区所占扇区数（目前认为和争夺的设备数相等）
	private final static int JOB_START_ADDRESS = 1920 - PCBQueue.getSize() * 8;// 作业的程序文件和数据文件存放区的起始外存地址
	private final static int JOB_BLOCKNUM = PCBQueue.getSize() * 8;// 作业区所占扇区数
	private final static int EXCHANGE_START_ADDRESS = 1920;// 外存交换区的起始地址
	private final static int EXCHANGE_BLOCKNUM = 128;// 外存交换区所占块数

	private PageTable pageTable;// 页表
	private PCBQueue pcbQueue;// PCB队列

	private BuddyAllocator userBuddyAllocator;// 用户区伙伴算法分配器
	private BuddyAllocator systemBuddyAllocator;// 系统区伙伴算法分配器

	private MainMemory mainMemory;// 主存
	private SubMemory subMemory;// 辅存

	private MemoryManagementUnit mmu;// MMU

	private DataBus dataBus;// 数据总线
	private AddressBus addressBus;// 地址总线

	public MemoryManageSystem() {
		super();
		this.mainMemory = new MainMemory(SYSTEM_START_PAGENUM, BUFFER_PAGENUM, USER_START_PAGENUM);
		this.subMemory = new SubMemory(FILE_START_ADDRESS, EXCHANGE_START_ADDRESS);

		this.userBuddyAllocator = new BuddyAllocator(USER_START_PAGENUM, MainMemory.getUserBlocknum());
		this.systemBuddyAllocator = new BuddyAllocator(SYSTEM_START_PAGENUM, MainMemory.getSystemBlocknum());

		this.pageTable = new PageTable(PAGETABLE_PAGEID);
		this.pageTable.writePageTableIntoBlock(this.mainMemory);
		this.pcbQueue = new PCBQueue(PCBQUEUE_PAGEID);

		this.mmu = new MemoryManagementUnit();

		this.dataBus = new DataBus(0);
		this.addressBus = new AddressBus(new Address(0, Address.getLogicPageBitnum(), Address.getOffsetBitnum()));
	}

	// 缺页异常处理程序(务必暂停当前进程，并且用核心栈保存现场后执行该操作)
	public void doPageFault(int proID) {
		synchronized (this) {
			this.mmu.inputLogicAddress(this.addressBus.outputAddressFromBus().getData());// 重新从总线上读地址（防止处理程序前的保护现场有修改过逻辑地址）
			// 查到此页的外存地址（通过页表查，因为在进程创建时，就会将进程拥有的外存块地址和逻辑地址一一对应。）
			int logicPageNum = this.mmu.getLogicAddress().getPageID();
			Address proBasicAddress = mmu.getPagetableBasicAddress();
			Integer subMemAddress = this.pageTable.findSubMemAddress(proBasicAddress, logicPageNum, proID);
			// 判断是否有空闲(因为是缺页中断处理程序，就无需插入新的进程-页表项数对到页表中）
			// 也无需判断是不是该挂起，判断挂起应当在调用异常处理程序前就判断。
			if (this.pageTable.hasFreeItemOfPro(proID)) {
				// 用空闲，就调用插入操作
				// 通过伙伴算法获得供分配的一个页框（缺页中断都是一次分一页）并且修改伙伴算法分配器中的系统页框表（位图）
				ArrayList<Integer> blockNumList = this.userBuddyAllocator.alloc_pages(1, proID);
				if (blockNumList != null && subMemAddress != null) {
					int freeBlockNum = blockNumList.get(0);
					// 分配空闲页框
					this.coverBlockFromSubToMain(subMemAddress, freeBlockNum);
					// 修改进程页表项
					this.pageTable.insertOneItemOfPro(logicPageNum, freeBlockNum, true, 0, false, subMemAddress, proID);
				}
			} else {
				// 按照LRU替换算法找到被淘汰页面
				int freeBlockNum = this.pageTable.findLRUPageTableItem(proBasicAddress, proID).getBlockID();
				// 无空闲页框，要做页面替换(必要时写回)
				this.swapBlockfromSubToMain(subMemAddress, freeBlockNum);
				// 无需修改页框表，因为页框没变，只是页框中的数据变了
				// 修改进程页表项
				this.pageTable.replaceOneItemOfPro(logicPageNum, freeBlockNum, true, 0, false, subMemAddress, proID);
			}
			this.mmu.getTLB().enQueue(this.pageTable.findItemByPageNum(logicPageNum));
			this.pageTable.writePageTableIntoBlock(this.mainMemory);
		}
	}

	// 获取空闲的块号
	public Integer findFreeBlockNum() {
		// 如何检查内存中是否有空闲页框呢——Linux使用伙伴算法管理空闲页框
		return null;
	}

	/**
	 * 将主存中的一块覆盖主存中的另一块。
	 * 
	 * @param blockNumA
	 * @param blockNumB
	 */
	public void coverBlockFromMainAToMainB(int blockNumA, int blockNumB) {
		this.mainMemory.getTheMainBlock(blockNumB)
				.outputBlockData(this.mainMemory.getTheMainBlock(blockNumA).inputBlockData());
	}

	// 将外存中的一块通过内存缓冲区覆盖外存中的另一块
	public void coverBlockFromSubAToSubB(int subMemAddA, int subMemAddB) {
		this.subMemory.getTheSubBlock(subMemAddB)
				.outputBlockData(this.subMemory.getTheSubBlock(subMemAddA).inputBlockData());
	}

	// 将外存中的一块直接覆盖内存中的一块
	public void coverBlockFromSubToMain(int subMemAddress, int blockNum) {
		this.mainMemory.getTheMainBlock(blockNum)
				.outputBlockData(this.subMemory.getTheSubBlock(subMemAddress).inputBlockData());
	}

	// 将内存中被修改的一块直接覆盖外存中的一块
	public void coverBlockFromMainToSub(int blockNum, int subMemAddress) {
		this.subMemory.getTheSubBlock(subMemAddress)
				.outputBlockData(this.mainMemory.getTheMainBlock(blockNum).inputBlockData());
	}

	// 将外存的一块与内存的一块交换
	public void swapBlockfromSubToMain(int subMemAddress, int blockNum) {
		PageTableItem item = this.pageTable.findItemByBlockNum(blockNum);
		if (item == null) {
			return ;
		}
		if (item.isStateBit() && item.isAlterBit()) {
			// 说明要被替换的内存物理块是被修改的，要写回再覆盖
			this.coverBlockFromMainToSub(blockNum, item.getSubmemoryAddress());
			this.coverBlockFromSubToMain(subMemAddress, blockNum);
		} else {
			// 说明被替换的内存物理块是未被修改的，直接覆盖
			this.coverBlockFromSubToMain(subMemAddress, blockNum);
		}
	}

	// 功能：将逻辑地址转为物理地址
	public boolean transLogicAddressToPhysicAddress(int proID) {
		this.mmu.inputLogicAddress(this.addressBus.outputAddressFromBus().getData());// 重新从总线上读地址（防止处理程序前的恢复现场有修改过逻辑地址）
		if (!this.mmu.translate1()) {// 快表未命中
			PageTableItem tmp = this.pageTable.findItemOfTheProcess(this.mmu.getPagetableBasicAddress(),
					this.mmu.getLogicAddress().getPageID(), proID);
			if (!this.mmu.translate2(tmp)) {// 页表未命中
				return false;
			} else {// 页表命中
				return true;
			}
		} else {
			return true;// 快表命中，所以转换成功
		}
	}

	// 小小功能：将一个字写入主存(用在逻辑物理地址转换之后)
	public boolean writeOneWordIntoMainMem(Address physicAddress, Integer word) {
		if (this.mainMemory.writeWordIntoMemory(physicAddress, word)) {
			PageTableItem tmpPoint = this.pageTable.findItemByBlockNum(physicAddress.getPageID());
			if (tmpPoint == null) {
				return false;//调试bug使用。
			} else {
				tmpPoint.setAlterBit(true);
				return true;
			}
		} else {
			return false;
		}
	}

	// 小小功能：将一个字从主存读出(用在逻辑物理地址转换之后)
	public Integer readOneWordFromMainMem(Address physicAddress) {
		return this.mainMemory.readWordFromMemory(physicAddress);
	}

	public PageTable getPageTable() {
		return pageTable;
	}

	public void setPageTable(PageTable pageTable) {
		this.pageTable = pageTable;
	}

	public PCBQueue getPcbQueue() {
		return pcbQueue;
	}

	public void setPcbQueue(PCBQueue pcbQueue) {
		this.pcbQueue = pcbQueue;
	}

	public MainMemory getMainMemory() {
		return mainMemory;
	}

	public void setMainMemory(MainMemory mainMemory) {
		this.mainMemory = mainMemory;
	}

	public SubMemory getSubMemory() {
		return subMemory;
	}

	public void setSubMemory(SubMemory subMemory) {
		this.subMemory = subMemory;
	}

	public MemoryManagementUnit getMmu() {
		return mmu;
	}

	public void setMmu(MemoryManagementUnit mmu) {
		this.mmu = mmu;
	}

	public BuddyAllocator getUserBuddyAllocator() {
		return userBuddyAllocator;
	}

	public void setUserBuddyAllocator(BuddyAllocator userBuddyAllocator) {
		this.userBuddyAllocator = userBuddyAllocator;
	}

	public BuddyAllocator getSystemBuddyAllocator() {
		return systemBuddyAllocator;
	}

	public void setSystemBuddyAllocator(BuddyAllocator systemBuddyAllocator) {
		this.systemBuddyAllocator = systemBuddyAllocator;
	}

	public static int getBufferPagenum() {
		return BUFFER_PAGENUM;
	}

	public static int getSystemStartPagenum() {
		return SYSTEM_START_PAGENUM;
	}

	public static int getUserStartPagenum() {
		return USER_START_PAGENUM;
	}

	public static int getUnitSize() {
		return UNIT_SIZE;
	}

	public DataBus getDataBus() {
		return dataBus;
	}

	public void setDataBus(DataBus dataBus) {
		this.dataBus = dataBus;
	}

	public AddressBus getAddressBus() {
		return addressBus;
	}

	public void setAddressBus(AddressBus addressBus) {
		this.addressBus = addressBus;
	}

	public static int getPagetablePageid() {
		return PAGETABLE_PAGEID;
	}

	public static int getPcbqueuePageid() {
		return PCBQUEUE_PAGEID;
	}

	public static int getFileStartAddress() {
		return FILE_START_ADDRESS;
	}

	public static int getExchangeStartAddress() {
		return EXCHANGE_START_ADDRESS;
	}

	public static int getSuperblockCopyPageid() {
		return SUPERBLOCK_COPY_PAGEID;
	}

	public static int getSuperblockAddress() {
		return SUPERBLOCK_ADDRESS;
	}

	public static int getGuideAddress() {
		return GUIDE_ADDRESS;
	}

	public static int getJobStartAddress() {
		return JOB_START_ADDRESS;
	}

	public static int getInodeBlocknum() {
		return INODE_BLOCKNUM;
	}

	public static int getDeviceStartAddress() {
		return DEVICE_START_ADDRESS;
	}

	public static int getInodeStartAddress() {
		return INODE_START_ADDRESS;
	}

	public static int getDeviceBlocknum() {
		return DEVICE_BLOCKNUM;
	}

	public static int getJobBlocknum() {
		return JOB_BLOCKNUM;
	}

	public static int getExchangeBlocknum() {
		return EXCHANGE_BLOCKNUM;
	}

}
