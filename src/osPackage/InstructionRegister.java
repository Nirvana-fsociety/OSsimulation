package osPackage;

import java.util.ArrayList;

/*
 * 	目前指令只有类型
 * 	如果有别的要往里加，现在访存指令有地址，但是不好往里加。
 */

public class InstructionRegister {
	private final static int BITNUM = 16;
	private final static int ID_BITNUM = 3;
	private final static int REGISTERID_BITNUM = 3;
	private final static int CALLID_BITNUM = 6;
	private final static int PAGENUM_BITNUM = 7;
	private final static int DELTA_OFFSET_BITNUM = 10;// 访存指令需要，但是访存只能给10位，所以最多就是两页。
	private final static int JUMP_OFFSET_BITNUM = 13;// 跳转指令的相对位移量，给13位，足够用，目前用4×9位偏移。
	private final static int SUBMEM_ADD_BITNUM = 11;
	private final static int INSTRUCTIONID_NUM = 8;// 指令种类识别号有8个，0号是未知，1~7都有实际的指令。

	private Instruction instruction;

	private INSTRUCTION_TYPE instructionID;// 指令标识

	private int accessPageNum;// 页号（位于访存地址的低7位）（系统调用的映射表所在页）
	private int accessOffset;// 访存偏移，是一个增量要与数据段指针相加得到地址。
	private int jumpOffset;// 跳转偏移，是一个增量要与正文段指针相加得到地址。
	private ArrayList<Integer> registerIDList;// 申请的寄存器标识数组号（计算、赋值、访存）
	private int callID;// 系统调用号（系统调用）
	private int deviceAddress;// 外设在辅存的地址

	// 构造函数
	public InstructionRegister(Instruction instruction) {
		super();
		this.accessPageNum = -1;
		this.setAccessOffset(-1);
		this.registerIDList = new ArrayList<Integer>();
		this.callID = -1;

		this.instruction = instruction;

		this.takeInstruction();
	}

	public InstructionRegister(int addressData, int instructionData) {
		this.instruction = new Instruction(addressData, instructionData);
		this.setAccessOffset(-1);
		this.accessPageNum = -1;
		this.registerIDList = new ArrayList<Integer>();
		this.callID = -1;

		this.takeInstruction();
	}

	// 功能函数：更新数据
	public void updateData(int addressData, int instructionData) {
		this.instruction = new Instruction(addressData, instructionData);

		this.accessPageNum = -1;
		this.registerIDList = new ArrayList<Integer>();
		this.callID = -1;

		this.takeInstruction();
	}

	// 功能函数：提取译码（标识）（寄存器组）（内存页号）
	/**
	 * 0.提取标识
	 */
	public void takeInstructionID() {
		int type = this.instruction.getData() >> (BITNUM - ID_BITNUM);

		switch (type) {
		case 0:
			this.instructionID = INSTRUCTION_TYPE.UNKNOW;
			break;
		case 1:
			this.instructionID = INSTRUCTION_TYPE.INPUT;
			break;
		case 2:
			this.instructionID = INSTRUCTION_TYPE.OUTPUT;
			break;
		case 3:
			this.instructionID = INSTRUCTION_TYPE.SYSTEM_CALL;
			break;
		case 4:
			this.instructionID = INSTRUCTION_TYPE.CALCULATE;
			break;
		case 5:
			this.instructionID = INSTRUCTION_TYPE.ACCESS_MEMORY;
			break;
		case 6:
			this.instructionID = INSTRUCTION_TYPE.NORMAL;
			break;
		case 7:
			this.instructionID = INSTRUCTION_TYPE.JUMP;
			break;
		default:
			this.instructionID = INSTRUCTION_TYPE.UNKNOW;
			break;
		}
	}

	/**
	 * 1.提取寄存器组（从指令标识开始，根据类型判断有几个寄存器） 未知：0<br>
	 * 输入输出：0<br>
	 * 系统调用：0<br>
	 * 计算赋值：2<br>
	 * 访问内存：1<br>
	 * 通常：0<br>
	 */
	public void takeRegisterIDList() {
		switch (this.instructionID) {
		case ACCESS_MEMORY:
			int tmpInt = this.instruction.getData() >> (DELTA_OFFSET_BITNUM);
			tmpInt = tmpInt & ((1 << REGISTERID_BITNUM) - 1);
			this.registerIDList.add(new Integer(tmpInt));
			break;
		case CALCULATE:
			for (int i = 0; i < 2; i++) {
				int tmpInt2 = this.instruction.getData() >> (PAGENUM_BITNUM + (REGISTERID_BITNUM * i));
				tmpInt2 = tmpInt2 & ((1 << REGISTERID_BITNUM) - 1);
				this.registerIDList.add(new Integer(tmpInt2));
			}
			break;
		default:
			this.registerIDList = null;
			break;
		}
	}

	/**
	 * 2.提取页号(从低位开始)
	 */
	public void takeAccessPageNum() {
		if (this.instructionID.equals(INSTRUCTION_TYPE.SYSTEM_CALL)) {
			this.accessPageNum = this.instruction.getData() & ((1 << PAGENUM_BITNUM) - 1);
		} else {
			this.accessPageNum = -1;
		}
	}

	public void takeJumpOffset() {
		if (this.instructionID.equals(INSTRUCTION_TYPE.JUMP)) {
			this.setJumpOffset(this.instruction.getData() & ((1 << JUMP_OFFSET_BITNUM) - 1));
		} else {
			this.setJumpOffset(-1);
		}
	}

	/**
	 * 5.提取访存指令的偏移量，页号默认为进程的数据段。
	 */
	public void takeAccessOffset() {
		if (this.instructionID.equals(INSTRUCTION_TYPE.ACCESS_MEMORY)) {
			this.accessOffset = this.instruction.getData() & ((1 << DELTA_OFFSET_BITNUM) - 1);
		} else {
			this.accessOffset = -1;
		}
	}

	/**
	 * 3.提取系统调用号
	 */
	public void takeCallID() {
		if (this.instructionID.equals(INSTRUCTION_TYPE.SYSTEM_CALL)) {
			this.callID = (this.instruction.getData() >> PAGENUM_BITNUM) & ((1 << CALLID_BITNUM) - 1);
		} else {
			this.callID = -1;
		}
	}

	/**
	 * 4.提取外设地址
	 */
	public void takeDeviceAdd() {
		if (this.instructionID.equals(INSTRUCTION_TYPE.INPUT) || this.instructionID.equals(INSTRUCTION_TYPE.OUTPUT)) {
			this.deviceAddress = this.instruction.getData() & ((1 << SUBMEM_ADD_BITNUM) - 1);
		} else {
			this.deviceAddress = -1;
		}
	}

	/**
	 * 分析一条指令
	 */
	public void takeInstruction() {
		this.takeInstructionID();
		this.takeRegisterIDList();
		this.takeCallID();
		this.takeAccessPageNum();
		this.takeDeviceAdd();
		this.takeAccessOffset();
		this.takeJumpOffset();
	}

	// get & set
	public INSTRUCTION_TYPE getInstructionID() {
		return instructionID;
	}

	public void setInstructionID(INSTRUCTION_TYPE instructionID) {
		this.instructionID = instructionID;
	}

	public static int getBitnum() {
		return BITNUM;
	}

	public static int getIdBitnum() {
		return ID_BITNUM;
	}

	public int getPageNum() {
		return accessPageNum;
	}

	public void setPageNum(int pageNum) {
		this.accessPageNum = pageNum;
	}

	public static int getPagenumBitnum() {
		return PAGENUM_BITNUM;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public void setInstruction(Instruction instruction) {
		this.instruction = instruction;
	}

	public int getAccessPageNum() {
		return accessPageNum;
	}

	public void setAccessPageNum(int accessPageNum) {
		this.accessPageNum = accessPageNum;
	}

	public ArrayList<Integer> getRegisterIDList() {
		return registerIDList;
	}

	public void setRegisterIDList(ArrayList<Integer> registerIDList) {
		this.registerIDList = registerIDList;
	}

	public int getCallID() {
		return callID;
	}

	public void setCallID(int callID) {
		this.callID = callID;
	}

	public static int getRegisteridBitnum() {
		return REGISTERID_BITNUM;
	}

	public int getDeviceAddress() {
		return deviceAddress;
	}

	public void setDeviceAddress(int deviceAddress) {
		this.deviceAddress = deviceAddress;
	}

	public static int getCallidBitnum() {
		return CALLID_BITNUM;
	}

	public static int getSubmemAddBitnum() {
		return SUBMEM_ADD_BITNUM;
	}

	public static int getInstructionidNum() {
		return INSTRUCTIONID_NUM;
	}

	public static int getOffsetBitnum() {
		return DELTA_OFFSET_BITNUM;
	}

	public int getAccessOffset() {
		return accessOffset;
	}

	public void setAccessOffset(int accessOffset) {
		this.accessOffset = accessOffset;
	}

	public int getJumpOffset() {
		return jumpOffset;
	}

	public void setJumpOffset(int jumpOffset) {
		this.jumpOffset = jumpOffset;
	}

}
