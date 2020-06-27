package osPackage;

public class PageTableItem {
	private final static int BITNUM = 32;
	private final static int PAGE_BITNUM = 7;
	private final static int BLOCK_BITNUM = 6;
	private final static int ACCESSTEXT_BITNUM = 6;
	private final static int SUBADDRESS_BITNUM = 11;

	private long data;// ԭʼ����

	private int pageID;// �߼�ҳ��
	private int blockID;// ������
	private boolean stateBit;// ��Чλ
	private int accessText;// �����ֶ�
	private boolean alterBit;// �޸�λ
	private int submemoryAddress;// �����ַ
	// ���캯��1

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

	// ���캯��2
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

	// �Ե��ù��ܺ���(�����װ)������data
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

	// ���ܺ����������ֶ��Լ�һ
	public void IncreaseAccessText() {
		this.accessText++;
		this.updateData();
	}

	// ���ܺ����������ֶ�����
	public void ClearAccessText() {
		this.accessText = 0;
		this.updateData();
	}
}
