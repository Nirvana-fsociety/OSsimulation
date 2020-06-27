package osPackage;

import java.util.ArrayList;

public class JobControlBlock {
	private int jobID;// 作业号
	private int processPriority;// 作业优先级
	private int textSubMemAddress;// 指令在外存中存放的地址
	private int instructionNum;// 该进程拥有的指令数
	private int dataSubMemAddress;// 数据在外存中的存放地址
	private int dataNum;// 16位变量数
	private ArrayList<MESSAGE> messageQueue;// 消息队列指针（消息队列是通过统计所有的指令中需要的资源）

	public JobControlBlock() {
		super();
		this.jobID = -1;
		this.processPriority = -1;
		this.textSubMemAddress = -1;
		this.instructionNum = 0;
		this.dataSubMemAddress = -1;
		this.dataNum = 0;
		this.messageQueue = new ArrayList<MESSAGE>();
	}

	// 功能：更新功能
	public void updateJCB(int jobID, int priority, int textSubAdd, int instructionNum, int dataSubMemAdd, int dataNum) {
		this.jobID = jobID;
		this.processPriority = priority;
		this.textSubMemAddress = textSubAdd;
		this.instructionNum = instructionNum;
		this.dataSubMemAddress = dataSubMemAdd;
		this.dataNum = dataNum;
	}

	// 功能：计算作业的指令集占多少页
	public Integer calculateJobTextPageNum() {
		int textSize = (this.instructionNum / Block.getBlockSize());
		if ((this.instructionNum % Block.getBlockSize()) > 0) {
			textSize++;
		}
		return textSize;
	}

	// 功能：计算作业的变量占多少页
	public Integer calculateJobDataPageNum() {
		int dataSize = (this.dataNum / Block.getBlockSize());
		if ((this.dataNum % Block.getBlockSize()) > 0) {
			dataSize++;
		}
		return dataSize;
	}

	public int getJobID() {
		return jobID;
	}

	public void setJobID(int jobID) {
		this.jobID = jobID;
	}

	public int getProcessPriority() {
		return processPriority;
	}

	public void setProcessPriority(int processPriority) {
		this.processPriority = processPriority;
	}

	public int getTextSubMemAddress() {
		return textSubMemAddress;
	}

	public void setTextSubMemAddress(int textSubMemAddress) {
		this.textSubMemAddress = textSubMemAddress;
	}

	public int getInstructionNum() {
		return instructionNum;
	}

	public void setInstructionNum(int instructionNum) {
		this.instructionNum = instructionNum;
	}

	public ArrayList<MESSAGE> getMessageQueue() {
		return messageQueue;
	}

	public void setMessageQueue(ArrayList<MESSAGE> messageQueue) {
		this.messageQueue = messageQueue;
	}

	public int getDataSubMemAddress() {
		return dataSubMemAddress;
	}

	public void setDataSubMemAddress(int dataSubMemAddress) {
		this.dataSubMemAddress = dataSubMemAddress;
	}

	public int getDataNum() {
		return dataNum;
	}

	public void setDataNum(int dataNum) {
		this.dataNum = dataNum;
	}

}
