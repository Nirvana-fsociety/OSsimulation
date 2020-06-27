package osPackage;

public class StackPointer {
	private final static int LOGIC_PAGE_BITNUM = 7;// �߼�ҳ��λ��
	private final static int OFFSET_BITNUM = 9;// ƫ��λ��
	private final static int UNIT_SIZE = 4;// ���һ���ֳ���˫�ֽ�������Ŀǰһ�δ�4���Ĵ���������4
	/**
	 * ��Զָ��Ҫ����Ŀ�λ
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

	// ���ܺ�������������
	public void updateData(int data) {
		this.data = data;
		this.stackTopAddress = new Address(data, LOGIC_PAGE_BITNUM, OFFSET_BITNUM);
	}

	// ���ܺ������Լ�
	public void decrease() {
		this.data -= MemoryManageSystem.getUnitSize();
		this.stackTopAddress.updateAddress(this.data);
	}

	// ���ܺ���������
	public void increase() {
		this.data += MemoryManageSystem.getUnitSize();
		this.stackTopAddress.updateAddress(this.data);
	}
}
