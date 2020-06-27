package osPackage;

public class ProgramCounter {
	private int data;// 原始数据

	private Address address;// 地址

	// 构造
	public ProgramCounter(int data) {
		super();
		this.data = data;
		this.address = new Address(this.data, Address.getLogicPageBitnum(), Address.getOffsetBitnum());
	}

	public ProgramCounter(int logicPage, int offset) {
		super();
		this.data = 0;
		this.address = new Address(Address.getLogicPageBitnum(), Address.getOffsetBitnum(), logicPage, offset);
		this.data += logicPage << Address.getOffsetBitnum();
		this.data += offset;
	}

	// get & set
	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	// 功能函数：更新数据
	public void updateData(int data) {
		this.data = data;
		this.address.updateAddress(this.data);
	}

	// 功能：自加一
	public void autoIncrease() {
		this.data += MemoryManageSystem.getUnitSize();
		this.address.updateAddress(this.data);
	}
}
