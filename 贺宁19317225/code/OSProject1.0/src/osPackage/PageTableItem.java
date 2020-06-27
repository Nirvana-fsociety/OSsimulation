package osPackage;

public class PageTableItem {
	private final static int BITNUM = 32;
	private final static int PAGE_BITNUM = 7;
	private final static int BLOCK_BITNUM = 6;
	private final static int ACCESSTEXT_BITNUM = 6;
	private final static int SUBADDRESS_BITNUM = 11;

	private long data;// 原始数据

	private int pageID;// 逻辑页号
	private int blockID;// 物理块号
	private boolean stateBit;// 有效位
	private int accessText;// 访问字段
	private boolean alterBit;// 修改位
	private int submemoryAddress;// 外存块地址
	// 构造函数1

	public PageTableItem(long data) {
		super();
		this.data = data;
		this.pageID = (int) ((this.data >> (BITNUM - PAGE_BITNUM)) & ((1 << PAGE_BITNUM) - 1));
		this.blockID = (int) ((this.data >> (BITNUM - PAGE_BITNUM - BLOCK_BITNUM)) & ((1 << BLOCK_BITNUM) - 1));
		if ((this.data & (1 << (ACCESSTEXT_BITNUM + 1 + SUBADDRESS_BITNUM))) == 0) {
			this.stateBit = false;
		} else {
			this.stateBit = true;
		}
		this.accessText = (int) ((this.data >> (SUBADDRESS_BITNUM + 1)) & ((1 << ACCESSTEXT_BITNUM) - 1));
		if ((this.data & (1 << SUBADDRESS_BITNUM)) == 0) {
			this.alterBit = false;
		} else {
			this.alterBit = true;
		}
		this.submemoryAddress = (int) (this.data & ((1 << SUBADDRESS_BITNUM) - 1));
	}

	// 构造函数2
	public PageTableItem(int pageNum, int blockNum, boolean stateBit, int accessText, boolean alterBit,
			int submemoryAddress) {
		super();
		this.pageID = pageNum & ((1 << PAGE_BITNUM) - 1);
		this.blockID = blockNum & ((1 << BLOCK_BITNUM) - 1);
		this.stateBit = stateBit;
		this.accessText = accessText & ((1 << ACCESSTEXT_BITNUM) - 1);
		this.alterBit = alterBit;
		this.submemoryAddress = submemoryAddress & ((1 << SUBADDRESS_BITNUM) - 1);
		this.updateData();
	}

	// get & set
	public long getData() {
		return data;
	}

	public void setData(long data) {
		this.data = data;
	}

	public int getPageID() {
		return pageID;
	}

	public void setPageID(int pageID) {
		this.pageID = pageID;
	}

	public int getBlockID() {
		return blockID;
	}

	public void setBlockID(int blockId) {
		this.blockID = blockId;
	}

	public boolean isStateBit() {
		return stateBit;
	}

	public void setStateBit(boolean stateBit) {
		this.stateBit = stateBit;
	}

	public int getAccessText() {
		return accessText;
	}

	public void setAccessText(int accessText) {
		this.accessText = accessText;
	}

	public boolean isAlterBit() {
		return alterBit;
	}

	public void setAlterBit(boolean alterBit) {
		this.alterBit = alterBit;
	}

	public int getSubmemoryAddress() {
		return submemoryAddress;
	}

	public void setSubmemoryAddress(int submemoryAddress) {
		this.submemoryAddress = submemoryAddress;
	}

	public static int getBitnum() {
		return BITNUM;
	}

	public static int getPageBitnum() {
		return PAGE_BITNUM;
	}

	public static int getBlockBitnum() {
		return BLOCK_BITNUM;
	}

	public static int getAccesstextBitnum() {
		return ACCESSTEXT_BITNUM;
	}

	public static int getSubaddressBitnum() {
		return SUBADDRESS_BITNUM;
	}

	// 自调用功能函数(冗余封装)：更新data
	public void updateData() {
		this.data = 0l;
		this.data += (long) this.pageID << (BITNUM - PAGE_BITNUM);
		this.data += (long) this.blockID << (BITNUM - PAGE_BITNUM - BLOCK_BITNUM);
		if (this.stateBit) {
			this.data += 1l << (ACCESSTEXT_BITNUM + 1 + SUBADDRESS_BITNUM);
		}
		this.data += (long) this.accessText << (SUBADDRESS_BITNUM + 1);
		if (this.alterBit) {
			this.data += 1l << SUBADDRESS_BITNUM;
		}
		this.data += (long) this.submemoryAddress;
	}

	// 功能函数：访问字段自加一
	public void IncreaseAccessText() {
		this.accessText++;
		this.updateData();
	}

	// 功能函数：访问字段清零
	public void ClearAccessText() {
		this.accessText = 0;
		this.updateData();
	}
}
