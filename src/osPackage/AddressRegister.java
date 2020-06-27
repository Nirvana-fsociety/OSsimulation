package osPackage;

public class AddressRegister {
	private final static int LOGIC_PAGE_BITNUM = 7;// �߼�ҳ��λ��
	private final static int OFFSET_BITNUM = 9;// ƫ��λ��

	/**
	 * ԭʼ����
	 */
	private int data;
	private Address address;

	// ����
	public AddressRegister(int pageID, int offset) {
		super();
		this.address = new Address(LOGIC_PAGE_BITNUM, OFFSET_BITNUM, pageID, offset);
		this.data = this.address.getData();
	}

	public AddressRegister(int data) {
		super();
		this.data = data;
		this.address = new Address(this.data, LOGIC_PAGE_BITNUM, OFFSET_BITNUM);
	}

	// get & set
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public static int getLogicPageBitnum() {
		return LOGIC_PAGE_BITNUM;
	}

	public static int getOffsetBitnum() {
		return OFFSET_BITNUM;
	}

	// ���ܣ���������
	public void updateData(int address) {
		this.address.updateAddress(address);
		this.data = this.address.getData();
	}
	
	public void updateData(int pageID, int offset) {
		this.address.updateAddress(pageID, offset);
		this.data = this.address.getData();
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}
}
