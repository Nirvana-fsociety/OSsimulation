package osPackage;

public class CentralProcessingUnit {
	private final static int SITEINFO_SIZE = 5;// �ֳ���Ϣ��ռ�ڴ浥Ԫ�����ؼ��Ĵ���������

	private final static int BITNUM = 16;// ��ַ���ߣ��������ߵ�λ��

	private ProgramCounter PC;// ���������
	private InstructionRegister IR;// ָ��Ĵ���
	private ProgramStatusWord PSW;// ״̬λ�Ĵ���
	private AddressRegister AR;// ��ַ�Ĵ���
	private StackPointer SP;// ����ջջ��ָ��Ĵ���

	// ���죺���ʲô������Ҫ�Ŀ�ʼ���������Ե���0��0��0����������֮���ٸ�ֵ��
	public CentralProcessingUnit(int pcData, int irData, int pswData, int spData) {
		this.PC = new ProgramCounter(pcData);
		this.IR = new InstructionRegister(this.PC.getData() - 1, irData);
		this.PSW = new ProgramStatusWord(pswData);
		this.AR = new AddressRegister(0);
		this.SP = new StackPointer(0);

	}

	// ���ܺ��������յ�ַ���ߵĵ�ַ���ݣ�һ�����������л�ʱ���ڶ�λ����ʼ��λ�ã�
	public void addressBusToPC(int nextAddress) {
		this.PC.updateData(nextAddress);
	}

	// ���ܺ������������PC�ڵ���һ��ָ���ַ������������һ������ʱʹ�ã�
	public Address outputPC() {
		return this.PC.getAddress();
	}

	// ���ܺ������������IR�ڵ�����
	public Instruction outputIR() {
		return this.IR.getInstruction();
	}

	// ���ܺ������������PSW�ڵ�״̬����
	public Integer outputPSW() {
		return this.PSW.getData();
	}

	// ���ܺ������������AR�ڵĵ�ַ���������ݣ�
	public Address outputAR() {
		return this.AR.getAddress();
	}

	// ���ܺ������������SP�е�ջ����ַ
	public Address outputSP() {
		return this.SP.getStackTopAddress();
	}

	// ���ܺ����������������ߵ�ָ�����ݣ���ַ�����ڴ浥Ԫ������ͨ���������߷��أ�
	public void dataBusToIR(int runningInstruction) {
		// ��Ҫ���е�ָ����룬������ָ��ĵ�ַһ����PC����һ��λ�á�
		// ���⣬�ʼ����ǰӦ����PC����ָ���һ��ָ���λ�ã���IR����ȫ0��Ȼ��ʼѰַȡָ�
		// Ѱַ��CPU��PC�е�������������ߣ�Ȼ�������Լ�һ���������߽�ָ��أ�CPU�����յ���ָ�ֵ��IR�Ĵ�����
		this.IR = new InstructionRegister(this.PC.getData() - MemoryManageSystem.getUnitSize(), runningInstruction);
	}

	public void assignCPU(int pc, int ir, int psw, int ar, int sp) {
		this.PC.setData(pc);
		this.IR.updateData(pc - MemoryManageSystem.getUnitSize(), ir);// Ĭ��˳��ִ�е�ǰ���£�ָ��Ӧ����PC����һ��
		this.PSW.updateData(psw);
		this.AR.updateData(ar);
		this.SP.updateData(sp);
	}

	public ProgramCounter getPC() {
		return PC;
	}

	public void setPC(ProgramCounter pC) {
		PC = pC;
	}

	public InstructionRegister getIR() {
		return IR;
	}

	public void setIR(InstructionRegister iR) {
		IR = iR;
	}

	public ProgramStatusWord getPSW() {
		return PSW;
	}

	public void setPSW(ProgramStatusWord pSW) {
		PSW = pSW;
	}

	public static int getBitnum() {
		return BITNUM;
	}

	public AddressRegister getAR() {
		return AR;
	}

	public void setAR(AddressRegister aR) {
		AR = aR;
	}

	public StackPointer getSP() {
		return SP;
	}

	public void setSP(StackPointer sP) {
		SP = sP;
	}

	public static int getSiteinfoSize() {
		return SITEINFO_SIZE;
	}
}
