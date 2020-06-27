package osPackage;

import java.util.ArrayList;

public class JobControlBlock {
	private int jobID;// ��ҵ��
	private int processPriority;// ��ҵ���ȼ�
	private int textSubMemAddress;// ָ��������д�ŵĵ�ַ
	private int instructionNum;// �ý���ӵ�е�ָ����
	private int dataSubMemAddress;// ����������еĴ�ŵ�ַ
	private int dataNum;// 16λ������
	private ArrayList<MESSAGE> messageQueue;// ��Ϣ����ָ�루��Ϣ������ͨ��ͳ�����е�ָ������Ҫ����Դ��

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

	// ���ܣ����¹���
	public void updateJCB(int jobID, int priority, int textSubAdd, int instructionNum, int dataSubMemAdd, int dataNum) {
		this.jobID = jobID;
		this.processPriority = priority;
		this.textSubMemAddress = textSubAdd;
		this.instructionNum = instructionNum;
		this.dataSubMemAddress = dataSubMemAdd;
		this.dataNum = dataNum;
	}

	// ���ܣ�������ҵ��ָ�ռ����ҳ
	public Integer calculateJobTextPageNum() {
		int textSize = (this.instructionNum / Block.getBlockSize());
		if ((this.instructionNum % Block.getBlockSize()) > 0) {
			textSize++;
		}
		return textSize;
	}

	// ���ܣ�������ҵ�ı���ռ����ҳ
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
