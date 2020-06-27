package osPackage;

public class CentralProcessingUnit {
	private final static int SITEINFO_SIZE = 5;// 现场信息的占内存单元数（关键寄存器个数）

	private final static int BITNUM = 16;// 地址总线，数据总线的位数

	private ProgramCounter PC;// 程序计数器
	private InstructionRegister IR;// 指令寄存器
	private ProgramStatusWord PSW;// 状态位寄存器
	private AddressRegister AR;// 地址寄存器
	private StackPointer SP;// 核心栈栈顶指针寄存器

	// 构造：如果什么都不需要的开始，参数可以调成0，0，0。运行起来之后再赋值。
	public CentralProcessingUnit(int pcData, int irData, int pswData, int spData) {
		this.PC = new ProgramCounter(pcData);
		this.IR = new InstructionRegister(this.PC.getData() - 1, irData);
		this.PSW = new ProgramStatusWord(pswData);
		this.AR = new AddressRegister(0);
		this.SP = new StackPointer(0);

	}

	// 功能函数：接收地址总线的地址数据（一般在上下文切换时用于定位程序开始的位置）
	public void addressBusToPC(int nextAddress) {
		this.PC.updateData(nextAddress);
	}

	// 功能函数：向外输出PC内的下一条指令地址（在正常运行一个进程时使用）
	public Address outputPC() {
		return this.PC.getAddress();
	}

	// 功能函数：向外输出IR内的数据
	public Instruction outputIR() {
		return this.IR.getInstruction();
	}

	// 功能函数：向外输出PSW内的状态数据
	public Integer outputPSW() {
		return this.PSW.getData();
	}

	// 功能函数：向外输出AR内的地址（访问数据）
	public Address outputAR() {
		return this.AR.getAddress();
	}

	// 功能函数：向外输出SP中的栈顶地址
	public Address outputSP() {
		return this.SP.getStackTopAddress();
	}

	// 功能函数：接收数据总线的指令内容（地址访问内存单元并将其通过数据总线返回）
	public void dataBusToIR(int runningInstruction) {
		// 把要运行的指令放入，而这条指令的地址一定是PC的上一个位置。
		// 此外，最开始运行前应当将PC置于指令集第一条指令的位置，而IR置于全0，然后开始寻址取指令。
		// 寻址：CPU将PC中的数据输出给总线，然后马上自加一，数据总线将指令传回，CPU将接收到的指令赋值给IR寄存器。
		this.IR = new InstructionRegister(this.PC.getData() - MemoryManageSystem.getUnitSize(), runningInstruction);
	}

	public void assignCPU(int pc, int ir, int psw, int ar, int sp) {
		this.PC.setData(pc);
		this.IR.updateData(pc - MemoryManageSystem.getUnitSize(), ir);// 默认顺序执行的前提下，指令应当是PC的上一个
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
