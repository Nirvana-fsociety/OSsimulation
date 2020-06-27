package osPackage;

public class MemoryManagementUnit {
	private final static int LOGIC_PAGE_BITNUM = 7;// 逻辑页号位数
	private final static int PHYSIC_PAGE_BITNUM = 6;// 物理块号位数
	private final static int OFFSET_BITNUM = 9;// 偏移位数

	private Address logicAddress;// 逻辑地址（输入）
	private Address physicAddress;// 物理地址（输出）

	private Address pagetableBasicAddress;// 运行进程页表基址（物理）

	private TransLookBuffer TLB;// 快表

	public MemoryManagementUnit() {
		super();
		this.logicAddress = new Address(-1, LOGIC_PAGE_BITNUM, OFFSET_BITNUM);
		this.physicAddress = new Address(-1, PHYSIC_PAGE_BITNUM, OFFSET_BITNUM);
		this.pagetableBasicAddress = new Address(-1, PHYSIC_PAGE_BITNUM, OFFSET_BITNUM);
		this.TLB = new TransLookBuffer();
	}

	public Address getLogicAddress() {
		return logicAddress;
	}

	public void setLogicAddress(Address logicAddress) {
		this.logicAddress = logicAddress;
	}

	public Address getPhysicAddress() {
		return physicAddress;
	}

	public void setPhysicAddress(Address physicAddress) {
		this.physicAddress = physicAddress;
	}

	public Address getPagetableBasicAddress() {
		return pagetableBasicAddress;
	}

	public void setPagetableBasicAddress(Address pagetableBasicAddress) {
		this.pagetableBasicAddress = pagetableBasicAddress;
	}

	public TransLookBuffer getTLB() {
		return TLB;
	}

	public void setTLB(TransLookBuffer tLB) {
		TLB = tLB;
	}

	public static int getLogicPageBitnum() {
		return LOGIC_PAGE_BITNUM;
	}

	public static int getPhysicPageBitnum() {
		return PHYSIC_PAGE_BITNUM;
	}

	public static int getOffsetBitnum() {
		return OFFSET_BITNUM;
	}

	// 功能函数：输入逻辑地址
	public void inputLogicAddress(int address) {
		this.logicAddress.updateAddress(address);
	}

	// 功能函数：输出物理地址
	public Address outputPhysicAddress() {
		return this.physicAddress;
	}

	// 功能函数：设置页表基址
	public void setPageTableBasicAddress(int address) {
		this.pagetableBasicAddress.updateAddress(address);
	}

	// 转换函数：查找快表看是否命中
	public boolean translate1() {
		PageTableItem tmpItem = this.TLB.findTheItem(this.logicAddress.getPageID());
		if (tmpItem == null || !tmpItem.isStateBit()) {
			// 未命中快表，应当将false信息返回给上级——存储管理系统，存储管理系统会进一步处理
			return false;
		} else {
			// 命中快表
			this.physicAddress.updateAddress(tmpItem.getBlockID(), this.logicAddress.getOffset());
			return true;
		}
	}

	// 转换函数：存储管理系统查找页表后得到的页表项传入MMU后进行拼接物理地址
	public boolean translate2(PageTableItem pageItem) {
		if (pageItem == null || !pageItem.isStateBit()) {
			// 未命中页表，应当将false信息返回给存储管理系统，进行缺页中断
			return false;
		} else {
			// 命中页表：先拼接物理地址，再将该项加入TLB中。
			this.physicAddress.updateAddress(pageItem.getBlockID(), this.logicAddress.getOffset());
			PageTableItem tmpItem = new PageTableItem(pageItem.getData());
			this.TLB.enQueue(tmpItem);
			return true;
		}
	}
	
	public void clearTLB() {
		this.TLB.getTlbItemQueue().clear();
	}
}
