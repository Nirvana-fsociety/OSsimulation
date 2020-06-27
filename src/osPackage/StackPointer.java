package osPackage;

public class StackPointer {
	private final static int LOGIC_PAGE_BITNUM = 7;// 逻辑页号位数
	private final static int OFFSET_BITNUM = 9;// 偏移位数
	private final static int UNIT_SIZE = 4;// 存放一个现场的双字节字数，目前一次存4个寄存器所以是4
	/**
	 * 永远指向要填入的空位
	 */
	private Address stackTopAddress;
	private int data;

	public StackPointer(int data) {
		super();
		this.data = data;
		this.stackTopAddress = new Address(data, LOGIC_PAGE_BITNUM, OFFSET_BITNUM);
	}

	public StackPointer(int pageNum, int offset) {
		super();
		this.stackTopAddress = new Address(LOGIC_PAGE_BITNUM, OFFSET_BITNUM, pageNum, offset);
		this.data = this.stackTopAddress.getData();
	}

	public Address getStackTopAddress() {
		return stackTopAddress;
	}

	public void setStackTopAddress(Address siteStartAddress) {
		this.stackTopAddress = siteStartAddress;
	}

	public static int getLogicPageBitnum() {
		return LOGIC_PAGE_BITNUM;
	}

	public static int getOffsetBitnum() {
		return OFFSET_BITNUM;
	}

	public static int getUnitSize() {
		return UNIT_SIZE;
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	// 功能函数：更新数据
	public void updateData(int data) {
		this.data = data;
		this.stackTopAddress = new Address(data, LOGIC_PAGE_BITNUM, OFFSET_BITNUM);
	}

	// 功能函数：自减
	public void decrease() {
		this.data -= MemoryManageSystem.getUnitSize();
		this.stackTopAddress.updateAddress(this.data);
	}

	// 功能函数：自增
	public void increase() {
		this.data += MemoryManageSystem.getUnitSize();
		this.stackTopAddress.updateAddress(this.data);
	}
}
