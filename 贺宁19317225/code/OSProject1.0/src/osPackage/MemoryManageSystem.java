package osPackage;

import java.util.ArrayList;

public class MemoryManageSystem {
	private final static int UNIT_SIZE = 2;// һ����Ԫ2�ֽ�

	private final static int BUFFER_PAGENUM = 0;// ������ҳ���
	private final static int SYSTEM_START_PAGENUM = 0;// ϵͳ����ʼҳ���
	private final static int PAGETABLE_PAGEID = 1;// ҳ��������
	private final static int PCBQUEUE_PAGEID = 2;// PCB���е���ʼ������
	private final static int SUPERBLOCK_COPY_PAGEID = 31;// ����ļ���ĳ����鸴��ӳ��
	private final static int USER_START_PAGENUM = 32;// �û�����ʼҳ���

	private final static int FILE_START_ADDRESS = 0;// �ļ�������ʼ��ַ
	private final static int GUIDE_ADDRESS = 0;// ����������ַ
	private final static int SUPERBLOCK_ADDRESS = 1;// ����������ַ
	private final static int INODE_START_ADDRESS = 2;// inode����ʼ����ַ
	private final static int INODE_BLOCKNUM = 5;// inode������ռ������
	private final static int DEVICE_START_ADDRESS = 1800;// �豸�ļ�������ʼ����ַ
	private final static int DEVICE_BLOCKNUM = 4;// �豸����ռ��������Ŀǰ��Ϊ��������豸����ȣ�
	private final static int JOB_START_ADDRESS = 1920 - PCBQueue.getSize() * 8;// ��ҵ�ĳ����ļ��������ļ����������ʼ����ַ
	private final static int JOB_BLOCKNUM = PCBQueue.getSize() * 8;// ��ҵ����ռ������
	private final static int EXCHANGE_START_ADDRESS = 1920;// ��潻��������ʼ��ַ
	private final static int EXCHANGE_BLOCKNUM = 128;// ��潻������ռ����

	private PageTable pageTable;// ҳ��
	private PCBQueue pcbQueue;// PCB����

	private BuddyAllocator userBuddyAllocator;// �û�������㷨������
	private BuddyAllocator systemBuddyAllocator;// ϵͳ������㷨������

	private MainMemory mainMemory;// ����
	private SubMemory subMemory;// ����

	private MemoryManagementUnit mmu;// MMU

	private DataBus dataBus;// ��������
	private AddressBus addressBus;// ��ַ����

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

	// ȱҳ�쳣�������(�����ͣ��ǰ���̣������ú���ջ�����ֳ���ִ�иò���)
	public void doPageFault(int proID) {
		synchronized (this) {
			this.mmu.inputLogicAddress(this.addressBus.outputAddressFromBus().getData());// ���´������϶���ַ����ֹ�������ǰ�ı����ֳ����޸Ĺ��߼���ַ��
			// �鵽��ҳ������ַ��ͨ��ҳ��飬��Ϊ�ڽ��̴���ʱ���ͻὫ����ӵ�е������ַ���߼���ַһһ��Ӧ����
			int logicPageNum = this.mmu.getLogicAddress().getPageID();
			Address proBasicAddress = mmu.getPagetableBasicAddress();
			Integer subMemAddress = this.pageTable.findSubMemAddress(proBasicAddress, logicPageNum, proID);
			// �ж��Ƿ��п���(��Ϊ��ȱҳ�жϴ�����򣬾���������µĽ���-ҳ�������Ե�ҳ���У�
			// Ҳ�����ж��ǲ��Ǹù����жϹ���Ӧ���ڵ����쳣�������ǰ���жϡ�
			if (this.pageTable.hasFreeItemOfPro(proID)) {
				// �ÿ��У��͵��ò������
				// ͨ������㷨��ù������һ��ҳ��ȱҳ�ж϶���һ�η�һҳ�������޸Ļ���㷨�������е�ϵͳҳ���λͼ��
				ArrayList<Integer> blockNumList = this.userBuddyAllocator.alloc_pages(1, proID);
				if (blockNumList != null && subMemAddress != null) {
					int freeBlockNum = blockNumList.get(0);
					// �������ҳ��
					this.coverBlockFromSubToMain(subMemAddress, freeBlockNum);
					// �޸Ľ���ҳ����
					this.pageTable.insertOneItemOfPro(logicPageNum, freeBlockNum, true, 0, false, subMemAddress, proID);
				}
			} else {
				// ����LRU�滻�㷨�ҵ�����̭ҳ��
				int freeBlockNum = this.pageTable.findLRUPageTableItem(proBasicAddress, proID).getBlockID();
				// �޿���ҳ��Ҫ��ҳ���滻(��Ҫʱд��)
				this.swapBlockfromSubToMain(subMemAddress, freeBlockNum);
				// �����޸�ҳ�����Ϊҳ��û�䣬ֻ��ҳ���е����ݱ���
				// �޸Ľ���ҳ����
				this.pageTable.replaceOneItemOfPro(logicPageNum, freeBlockNum, true, 0, false, subMemAddress, proID);
			}
			this.mmu.getTLB().enQueue(this.pageTable.findItemByPageNum(logicPageNum));
			this.pageTable.writePageTableIntoBlock(this.mainMemory);
		}
	}

	// ��ȡ���еĿ��
	public Integer findFreeBlockNum() {
		// ��μ���ڴ����Ƿ��п���ҳ���ء���Linuxʹ�û���㷨�������ҳ��
		return null;
	}

	/**
	 * �������е�һ�鸲�������е���һ�顣
	 * 
	 * @param blockNumA
	 * @param blockNumB
	 */
	public void coverBlockFromMainAToMainB(int blockNumA, int blockNumB) {
		this.mainMemory.getTheMainBlock(blockNumB)
				.outputBlockData(this.mainMemory.getTheMainBlock(blockNumA).inputBlockData());
	}

	// ������е�һ��ͨ���ڴ滺������������е���һ��
	public void coverBlockFromSubAToSubB(int subMemAddA, int subMemAddB) {
		this.subMemory.getTheSubBlock(subMemAddB)
				.outputBlockData(this.subMemory.getTheSubBlock(subMemAddA).inputBlockData());
	}

	// ������е�һ��ֱ�Ӹ����ڴ��е�һ��
	public void coverBlockFromSubToMain(int subMemAddress, int blockNum) {
		this.mainMemory.getTheMainBlock(blockNum)
				.outputBlockData(this.subMemory.getTheSubBlock(subMemAddress).inputBlockData());
	}

	// ���ڴ��б��޸ĵ�һ��ֱ�Ӹ�������е�һ��
	public void coverBlockFromMainToSub(int blockNum, int subMemAddress) {
		this.subMemory.getTheSubBlock(subMemAddress)
				.outputBlockData(this.mainMemory.getTheMainBlock(blockNum).inputBlockData());
	}

	// ������һ�����ڴ��һ�齻��
	public void swapBlockfromSubToMain(int subMemAddress, int blockNum) {
		PageTableItem item = this.pageTable.findItemByBlockNum(blockNum);
		if (item == null) {
			return ;
		}
		if (item.isStateBit() && item.isAlterBit()) {
			// ˵��Ҫ���滻���ڴ�������Ǳ��޸ĵģ�Ҫд���ٸ���
			this.coverBlockFromMainToSub(blockNum, item.getSubmemoryAddress());
			this.coverBlockFromSubToMain(subMemAddress, blockNum);
		} else {
			// ˵�����滻���ڴ��������δ���޸ĵģ�ֱ�Ӹ���
			this.coverBlockFromSubToMain(subMemAddress, blockNum);
		}
	}

	// ���ܣ����߼���ַתΪ�����ַ
	public boolean transLogicAddressToPhysicAddress(int proID) {
		this.mmu.inputLogicAddress(this.addressBus.outputAddressFromBus().getData());// ���´������϶���ַ����ֹ�������ǰ�Ļָ��ֳ����޸Ĺ��߼���ַ��
		if (!this.mmu.translate1()) {// ���δ����
			PageTableItem tmp = this.pageTable.findItemOfTheProcess(this.mmu.getPagetableBasicAddress(),
					this.mmu.getLogicAddress().getPageID(), proID);
			if (!this.mmu.translate2(tmp)) {// ҳ��δ����
				return false;
			} else {// ҳ������
				return true;
			}
		} else {
			return true;// ������У�����ת���ɹ�
		}
	}

	// СС���ܣ���һ����д������(�����߼������ַת��֮��)
	public boolean writeOneWordIntoMainMem(Address physicAddress, Integer word) {
		if (this.mainMemory.writeWordIntoMemory(physicAddress, word)) {
			PageTableItem tmpPoint = this.pageTable.findItemByBlockNum(physicAddress.getPageID());
			if (tmpPoint == null) {
				return false;//����bugʹ�á�
			} else {
				tmpPoint.setAlterBit(true);
				return true;
			}
		} else {
			return false;
		}
	}

	// СС���ܣ���һ���ִ��������(�����߼������ַת��֮��)
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
