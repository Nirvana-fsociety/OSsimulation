package osPackage;

public class AddressBus {
	private Address address;

	public AddressBus(Address another) {
		super();
		this.address = new Address(another.getData(), another.getPageBitNum(), another.getOffsetBitNum());
	}

	// 功能：将数据输入到总线上
	public void inputAddressIntoBus(Address address) {
		this.address.updateAddress(address.getData());
	}

	// 功能：将总线上的数据取出来
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
