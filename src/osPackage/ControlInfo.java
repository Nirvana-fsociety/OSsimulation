package osPackage;

import java.util.ArrayList;

public class ControlInfo {
	// 进程调度相关信息：
	private PRO_STATE processState;// 进程状态
	private WAIT_REASON waitReason;// 阻塞的原因
	private int processPriority;// 进程优先级（1~10范围内的整数）
	// 进程组成信息
	private Address pageTableAddress;// 进程页表在内存系统区的地址(物理)
	private Address textPoint;// 正文段指针（程序段指针）（逻辑）
	private Address dataPoint;// 数据段指针（逻辑）
	private Address kernelPoint;// 核心栈栈底（逻辑）

	private Address bufferPoint;// 进程用户区指针（逻辑）

	private int instructionNum;// 进程拥有的指令数
	private int dataNum;// 进程拥有的数据量
	private int pageTableItemNum;// 拥有的页表项数（拥有的页表大小，并不指真正用的有对应物理内存的页表项数）
	// 进程间通信信息
	private ArrayList<MESSAGE> messages;// 信号量队列
	// 进程的外存地址

	private int processAddress;// 进程映像在外存的地址
	// CPU占用和使用信息
	private int accessTime;// 访问次数（用于选择挂起的进程）
	private long remainTimePiece;// 时间片剩余量
	private long usedCPUTime;// 已占用CPU的时间
	private long usedSumTime;// 进程已执行时间总和

	public ControlInfo() {
		super();
		this.processState = PRO_STATE.UNKNOW;
		this.waitReason = WAIT_REASON.UNKNOW;
		this.processPriority = 0;

		this.pageTableAddress = new Address(0, Address.getPhysicPageBitnum(), Address.getOffsetBitnum());
		this.textPoint = new Address(0, Address.getLogicPageBitnum(), Address.getOffsetBitnum());
		this.dataPoint = new Address(0, Address.getLogicPageBitnum(), Address.getOffsetBitnum());
		this.kernelPoint = new Address(0, Address.getLogicPageBitnum(), Address.getOffsetBitnum());
		this.bufferPoint = new Address(0, Address.getLogicPageBitnum(), Address.getOffsetBitnum());

		this.messages = new ArrayList<MESSAGE>();
	}

	// 更新进程调度信息
	public void updateManageData(PRO_STATE processState, WAIT_REASON waitReason, int processPriority) {
		this.processState = processState;
		this.waitReason = waitReason;
		this.processPriority = processPriority;
	}

	// 更新进程通信信息
	public void updateCommunicateData(ArrayList<MESSAGE> messages) {
		this.messages = new ArrayList<MESSAGE>(messages);
	}

	// 更新CPU占用和使用信息
	public void updateCpuOccupyData(int accessTime, long remainTimePiece, long usedCPUTime, long usedSumTime) {
		this.accessTime = accessTime;
		this.remainTimePiece = remainTimePiece;
		this.usedCPUTime = usedCPUTime;
		this.usedSumTime = usedCPUTime;
	}

	// 更新进程的外存地址
	public void updateSubmemoryAddress(int processAddress) {
		this.processAddress = processAddress;
	}

	// 更新进程组成信息的地址
	public void updateTextDataKernelAddress(int textAddress, int dataAddress, int kernelAddress, int bufferAddress) {
		this.textPoint.updateAddress(textAddress);
		this.dataPoint.updateAddress(dataAddress);
		this.kernelPoint.updateAddress(kernelAddress);
		this.bufferPoint.updateAddress(bufferAddress);
	}

	public PRO_STATE getProcessState() {
		return processState;
	}

	public void setProcessState(PRO_STATE processState) {
		this.processState = processState;
	}

	public WAIT_REASON getWaitReason() {
		return waitReason;
	}

	public void setWaitReason(WAIT_REASON waitReason) {
		this.waitReason = waitReason;
	}

	public int getProcessPriority() {
		return processPriority;
	}

	public void setProcessPriority(int processPriority) {
		this.processPriority = processPriority;
	}

	public Address getTextPoint() {
		return textPoint;
	}

	public void setTextPoint(Address textPoint) {
		this.textPoint = textPoint;
	}

	public Address getDataPoint() {
		return dataPoint;
	}

	public void setDataPoint(Address dataPoint) {
		this.dataPoint = dataPoint;
	}

	public Address getBufferPoint() {
		return bufferPoint;
	}

	public void setBufferPoint(Address bufferPoint) {
		this.bufferPoint = bufferPoint;
	}

	public ArrayList<MESSAGE> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<MESSAGE> messages) {
		this.messages = messages;
	}

	public Address getPageTableAddress() {
		return pageTableAddress;
	}

	public void setPageTableAddress(Address pageTableAddress) {
		this.pageTableAddress = pageTableAddress;
	}

	public int getProcessAddress() {
		return processAddress;
	}

	public void setProcessAddress(int processAddress) {
		this.processAddress = processAddress;
	}

	public int getAccessTime() {
		return accessTime;
	}

	public void setAccessTime(int accessTime) {
		this.accessTime = accessTime;
	}

	/**
	 * @apiNote 访问字段自加1
	 */
	public void autoIncreaseAccessTime() {
		this.accessTime++;
	}

	/**
	 * @apiNote 清空该PCB的访问字段。
	 */
	public void clearAccessTime() {
		this.accessTime = 0;
	}

	public long getRemainTimePiece() {
		return remainTimePiece;
	}

	public void setRemainTimePiece(long remainTimePiece) {
		this.remainTimePiece = remainTimePiece;
	}

	public long getUsedCPUTime() {
		return usedCPUTime;
	}

	public void setUsedCPUTime(long usedCPUTime) {
		this.usedCPUTime = usedCPUTime;
	}

	public long getUsedSumTime() {
		return usedSumTime;
	}

	public void setUsedSumTime(long usedSumTime) {
		this.usedSumTime = usedSumTime;
	}

	public void updateTextAddress(int addressData) {
		this.textPoint.updateAddress(addressData);
	}

	public void updateDataAddress(int addressData) {
		this.dataPoint.updateAddress(addressData);
	}

	public void updateKernelAddress(int addressData) {
		this.kernelPoint.updateAddress(addressData);
	}

	public void updateBufferAddress(int addressData) {
		this.bufferPoint.updateAddress(addressData);
	}

	public void updatePageTableAddress(int addressData) {
		this.pageTableAddress.updateAddress(addressData);
	}

	public void updateProcessAddress(int addressData) {
		this.processAddress = addressData;
	}

	public Address getKernelPoint() {
		return kernelPoint;
	}

	public void setKernelPoint(Address kernelPoint) {
		this.kernelPoint = kernelPoint;
	}

	public int getInstructionNum() {
		return instructionNum;
	}

	public void setInstructionNum(int instructionNum) {
		this.instructionNum = instructionNum;
	}

	public int getPageTableItemNum() {
		return pageTableItemNum;
	}

	public void setPageTableItemNum(int pageTableItemNum) {
		this.pageTableItemNum = pageTableItemNum;
	}

	public int getDataNum() {
		return dataNum;
	}

	public void setDataNum(int dataNum) {
		this.dataNum = dataNum;
	}

}
