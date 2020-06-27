package osPackage;

public class Address {
	private final static int PHYSIC_PAGE_BITNUM = 6;
	private final static int LOGIC_PAGE_BITNUM = 7;
	private final static int OFFSET_BITNUM = 9;

	private int data;// 地址
	private int pageBitNum;// 页号位数
	private int offsetBitNum;// 偏移位数

	private int pageID;// 页号
	private int offset;// 偏移

	public Address(int data, int pageBitNum, int offsetBitNum) {
		super();
		this.pageBitNum = pageBitNum;
		this.offsetBitNum = offsetBitNum;
		this.updateAddress(data);
	}

	public Address(int pageBitNum, int offsetBitNum, int pageNum, int offset) {
		super();
		this.pageBitNum = pageBitNum;
		this.offsetBitNum = offsetBitNum;
		this.updateAddress(pageNum, offset);
	}

	// 功能：求两个地址之间有多少个存储单元
	public int distanceTo(Address that) {
		return (that.data - this.data) / MemoryManageSystem.getUnitSize();
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	public int getPageBitNum() {
		return pageBitNum;
	}

	public void setPageBitNum(int pageBitNum) {
		this.pageBitNum = pageBitNum;
	}

	public int getOffsetBitNum() {
		return offsetBitNum;
	}

	public void setOffsetBitNum(int offsetBitNum) {
		this.offsetBitNum = offsetBitNum;
	}

	public int getPageID() {
		return pageID;
	}

	public void setPageID(int pageID) {
		this.pageID = pageID;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void updateAddress(int data) {
		this.data = data;
		this.pageID = (this.data >> offsetBitNum) & ((1 << this.pageBitNum) - 1);
		this.offset = this.data & ((1 << this.offsetBitNum) - 1);
	}

	public void updateAddress(int pageNum, int offset) {
		this.pageID = pageNum;
		this.offset = offset;
		this.data = 0;
		this.data += this.pageID << this.offsetBitNum;
		this.data += this.offset;
	}

	public static int getPhysicPageBitnum() {
		return PHYSIC_PAGE_BITNUM;
	}

	public static int getLogicPageBitnum() {
		return LOGIC_PAGE_BITNUM;
	}

	public static int getOffsetBitnum() {
		return OFFSET_BITNUM;
	}

}
