package osPackage;

import java.util.Random;

public class FutureRequest {
	/**
	 * δ��������������������ʱ��������
	 */
	private final static int MAX_INTIME_DISTANCE = 10;
	/**
	 * δ��������������������ʱ�����С���
	 */
	private final static int MIN_INTIME_DISTANCE = 5;
	/**
	 * ��������ʱ��
	 */
	private int inTime;
	/**
	 * ���ȼ�
	 */
	private int priority;
	/**
	 * ���򼯵�ָ����
	 */
	private int instructionNum;
	/**
	 * ��ҵ������˫�����ݸ���
	 */
	private int dataNum;

	/**
	 * ���������������
	 */
	private Random random;

	/**
	 * ���캯��
	 * 
	 * @param intime ������ĵ���ʱ�䣺<br>
	 *               ��ť����-��ֵΪ��ǰʱ�䣻<br>
	 *               ϵͳ����������������-��ֵΪ��һ��������ʱ��֮���5~10���<br>
	 * @apiNote �����µ�δ����������ť���������е���
	 * @implNote ������ɸ�������
	 */
	public FutureRequest(int intime) {
		super();
		// ������ʱ��
		this.inTime = intime;
		this.random = new Random();
		this.priorityRandly();
		this.instructionNumRandly();
		this.dataNumRandly();
	}

	
	
	/**
	 * @apiNote �в����Ĺ��캯�������������ָ������ݡ�
	 * @param inTime
	 * @param priority
	 * @param instructionNum
	 * @param dataNum
	 */
	public FutureRequest(int inTime, int priority, int instructionNum, int dataNum) {
		super();
		this.random = new Random();
		this.inTime = inTime;
		this.priority = priority;
		this.instructionNum = instructionNum;
		this.dataNum = dataNum;
	}


	/**
	 * ����0~9�����ȼ���ֻ��IO�����Ľ��̲���ӵ��10�����ȼ���
	 */
	public void priorityRandly() {
		this.priority = this.random.nextInt(ProcessManageSystem.getMaxPriority());
	}

	/**
	 * ָ����������ɡ�
	 */
	public void instructionNumRandly() {
		this.instructionNum = this.random
				.nextInt(ProcessManageSystem.getMaxInstructionNum() - ProcessManageSystem.getMinInstructionNum())
				+ ProcessManageSystem.getMinInstructionNum();
	}

	/**
	 * ������������ɡ�
	 */
	public void dataNumRandly() {
		this.dataNum = this.random.nextInt(ProcessManageSystem.getMaxDataNum() - ProcessManageSystem.getMinDataNum())
				+ ProcessManageSystem.getMinDataNum();
	}

	public int getInTime() {
		return inTime;
	}

	public void setInTime(int inTime) {
		this.inTime = inTime;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getInstructionNum() {
		return instructionNum;
	}

	public void setInstructionNum(int instructionNum) {
		this.instructionNum = instructionNum;
	}

	public int getDataNum() {
		return dataNum;
	}

	public void setDataNum(int dataNum) {
		this.dataNum = dataNum;
	}

	public static int getMaxIntimeDistance() {
		return MAX_INTIME_DISTANCE;
	}

	public static int getMinIntimeDistance() {
		return MIN_INTIME_DISTANCE;
	}

}
