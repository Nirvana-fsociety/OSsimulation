package osPackage;

import java.util.Random;

public class FutureRequest {
	/**
	 * 未来请求序列两个请求到来时间的最大间隔
	 */
	private final static int MAX_INTIME_DISTANCE = 10;
	/**
	 * 未来请求序列两个请求到来时间的最小间隔
	 */
	private final static int MIN_INTIME_DISTANCE = 5;
	/**
	 * 请求到来的时间
	 */
	private int inTime;
	/**
	 * 优先级
	 */
	private int priority;
	/**
	 * 程序集的指令数
	 */
	private int instructionNum;
	/**
	 * 作业数据区双字数据个数
	 */
	private int dataNum;

	/**
	 * 整型随机数生成器
	 */
	private Random random;

	/**
	 * 构造函数
	 * 
	 * @param intime 该请求的到来时间：<br>
	 *               按钮创建-赋值为当前时间；<br>
	 *               系统启动生成请求序列-赋值为上一个请求到来时间之后的5~10秒后<br>
	 * @apiNote 创建新的未来请求，请求按钮和请求序列调用
	 * @implNote 随机生成各种属性
	 */
	public FutureRequest(int intime) {
		super();
		// 请求到来时间
		this.inTime = intime;
		this.random = new Random();
		this.priorityRandly();
		this.instructionNumRandly();
		this.dataNumRandly();
	}

	
	
	/**
	 * @apiNote 有参数的构造函数，不随机生成指令和数据。
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
	 * 生成0~9的优先级，只有IO阻塞的进程才能拥有10级优先级。
	 */
	public void priorityRandly() {
		this.priority = this.random.nextInt(ProcessManageSystem.getMaxPriority());
	}

	/**
	 * 指令数随机生成。
	 */
	public void instructionNumRandly() {
		this.instructionNum = this.random
				.nextInt(ProcessManageSystem.getMaxInstructionNum() - ProcessManageSystem.getMinInstructionNum())
				+ ProcessManageSystem.getMinInstructionNum();
	}

	/**
	 * 数据数随机生成。
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
