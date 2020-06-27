package osPackage;

public class AddressBus {
	private Address address;

	public AddressBus(Address another) {
		super();
		this.address = new Address(another.getData(), another.getPageBitNum(), another.getOffsetBitNum());
	}

	// ���ܣ����������뵽������
	public void inputAddressIntoBus(Address address) {
		this.address.updateAddress(address.getData());
	}

	// ���ܣ��������ϵ�����ȡ����
	public Address outputAddressFromBus() {
		return this.address;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

}
