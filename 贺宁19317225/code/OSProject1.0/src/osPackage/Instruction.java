package osPackage;

public class Instruction {
	Address logicAddress;
	int data;

	public Instruction(int logicAddress, int data) {
		super();
		this.logicAddress = new Address(logicAddress, Address.getLogicPageBitnum(), Address.getOffsetBitnum());
		this.data = data;
	}

	// 将一条32位的01串传入，转换成两个成员变量
	public Instruction(long txtData) {
		super();
		int addressInt = (int) (txtData >> 16);
		this.logicAddress = new Address(addressInt, Address.getLogicPageBitnum(), Address.getOffsetBitnum());
		this.data = (int) (txtData & ((1 << 16) - 1));
	}

	public Address getAddress() {
		return logicAddress;
	}

	public void setAddress(Address address) {
		this.logicAddress = address;
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

}
