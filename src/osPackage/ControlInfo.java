package osPackage;

import java.util.ArrayList;

public class ControlInfo {
	// ���̵��������Ϣ��
	private PRO_STATE processState;// ����״̬
	private WAIT_REASON waitReason;// ������ԭ��
	private int processPriority;// �������ȼ���1~10��Χ�ڵ�������
	// ���������Ϣ
	private Address pageTableAddress;// ����ҳ�����ڴ�ϵͳ���ĵ�ַ(����)
	private Address textPoint;// ���Ķ�ָ�루�����ָ�룩���߼���
	private Address dataPoint;// ���ݶ�ָ�루�߼���
	private Address kernelPoint;// ����ջջ�ף��߼���

	private Address bufferPoint;// �����û���ָ�루�߼���

	private int instructionNum;// ����ӵ�е�ָ����
	private int dataNum;// ����ӵ�е�������
	private int pageTableItemNum;// ӵ�е�ҳ��������ӵ�е�ҳ���С������ָ�����õ��ж�Ӧ�����ڴ��ҳ��������
	// ���̼�ͨ����Ϣ
	private ArrayList<MESSAGE> messages;// �ź�������
	// ���̵�����ַ

	private int processAddress;// ����ӳ�������ĵ�ַ
	// CPUռ�ú�ʹ����Ϣ
	private int accessTime;// ���ʴ���������ѡ�����Ľ��̣�
	private long remainTimePiece;// ʱ��Ƭʣ����
	private long usedCPUTime;// ��ռ��CPU��ʱ��
	private long usedSumTime;// ������ִ��ʱ���ܺ�

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

	// ���½��̵�����Ϣ
	public void updateManageData(PRO_STATE processState, WAIT_REASON waitReason, int processPriority) {
		this.processState = processState;
		this.waitReason = waitReason;
		this.processPriority = processPriority;
	}

	// ���½���ͨ����Ϣ
	public void updateCommunicateData(ArrayList<MESSAGE> messages) {
		this.messages = new ArrayList<MESSAGE>(messages);
	}

	// ����CPUռ�ú�ʹ����Ϣ
	public void updateCpuOccupyData(int accessTime, long remainTimePiece, long usedCPUTime, long usedSumTime) {
		this.accessTime = accessTime;
		this.remainTimePiece = remainTimePiece;
		this.usedCPUTime = usedCPUTime;
		this.usedSumTime = usedCPUTime;
	}

	// ���½��̵�����ַ
	public void updateSubmemoryAddress(int processAddress) {
		this.processAddress = processAddress;
	}

	// ���½��������Ϣ�ĵ�ַ
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
	 * @apiNote �����ֶ��Լ�1
	 */
	public void autoIncreaseAccessTime() {
		this.accessTime++;
	}

	/**
	 * @apiNote ��ո�PCB�ķ����ֶΡ�
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
