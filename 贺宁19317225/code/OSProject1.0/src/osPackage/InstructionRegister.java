package osPackage;

import java.util.ArrayList;

/*
 * 	Ŀǰָ��ֻ������
 * 	����б��Ҫ����ӣ����ڷô�ָ���е�ַ�����ǲ�������ӡ�
 */

public class InstructionRegister {
	private final static int BITNUM = 16;
	private final static int ID_BITNUM = 3;
	private final static int REGISTERID_BITNUM = 3;
	private final static int CALLID_BITNUM = 6;
	private final static int PAGENUM_BITNUM = 7;
	private final static int DELTA_OFFSET_BITNUM = 10;// �ô�ָ����Ҫ�����Ƿô�ֻ�ܸ�10λ��������������ҳ��
	private final static int JUMP_OFFSET_BITNUM = 13;// ��תָ������λ��������13λ���㹻�ã�Ŀǰ��4��9λƫ�ơ�
	private final static int SUBMEM_ADD_BITNUM = 11;
	private final static int INSTRUCTIONID_NUM = 8;// ָ������ʶ�����8����0����δ֪��1~7����ʵ�ʵ�ָ�

	private Instruction instruction;

	private INSTRUCTION_TYPE instructionID;// ָ���ʶ

	private int accessPageNum;// ҳ�ţ�λ�ڷô��ַ�ĵ�7λ����ϵͳ���õ�ӳ�������ҳ��
	private int accessOffset;// �ô�ƫ�ƣ���һ������Ҫ�����ݶ�ָ����ӵõ���ַ��
	private int jumpOffset;// ��תƫ�ƣ���һ������Ҫ�����Ķ�ָ����ӵõ���ַ��
	private ArrayList<Integer> registerIDList;// ����ļĴ�����ʶ����ţ����㡢��ֵ���ô棩
	private int callID;// ϵͳ���úţ�ϵͳ���ã�
	private int deviceAddress;// �����ڸ���ĵ�ַ

	// ���캯��
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

	// ���ܺ�������������
	public void updateData(int addressData, int instructionData) {
		this.instruction = new Instruction(addressData, instructionData);

		this.accessPageNum = -1;
		this.registerIDList = new ArrayList<Integer>();
		this.callID = -1;

		this.takeInstruction();
	}

	// ���ܺ�������ȡ���루��ʶ�����Ĵ����飩���ڴ�ҳ�ţ�
	/**
	 * 0.��ȡ��ʶ
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
	 * 1.��ȡ�Ĵ����飨��ָ���ʶ��ʼ�����������ж��м����Ĵ����� δ֪��0<br>
	 * ���������0<br>
	 * ϵͳ���ã�0<br>
	 * ���㸳ֵ��2<br>
	 * �����ڴ棺1<br>
	 * ͨ����0<br>
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
	 * 2.��ȡҳ��(�ӵ�λ��ʼ)
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
	 * 5.��ȡ�ô�ָ���ƫ������ҳ��Ĭ��Ϊ���̵����ݶΡ�
	 */
	public void takeAccessOffset() {
		if (this.instructionID.equals(INSTRUCTION_TYPE.ACCESS_MEMORY)) {
			this.accessOffset = this.instruction.getData() & ((1 << DELTA_OFFSET_BITNUM) - 1);
		} else {
			this.accessOffset = -1;
		}
	}

	/**
	 * 3.��ȡϵͳ���ú�
	 */
	public void takeCallID() {
		if (this.instructionID.equals(INSTRUCTION_TYPE.SYSTEM_CALL)) {
			this.callID = (this.instruction.getData() >> PAGENUM_BITNUM) & ((1 << CALLID_BITNUM) - 1);
		} else {
			this.callID = -1;
		}
	}

	/**
	 * 4.��ȡ�����ַ
	 */
	public void takeDeviceAdd() {
		if (this.instructionID.equals(INSTRUCTION_TYPE.INPUT) || this.instructionID.equals(INSTRUCTION_TYPE.OUTPUT)) {
			this.deviceAddress = this.instruction.getData() & ((1 << SUBMEM_ADD_BITNUM) - 1);
		} else {
			this.deviceAddress = -1;
		}
	}

	/**
	 * ����һ��ָ��
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
