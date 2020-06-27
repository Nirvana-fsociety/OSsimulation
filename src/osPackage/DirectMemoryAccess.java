package osPackage;

import java.util.ArrayList;

/**
 * @apiNote DMAӦ������CPU���е�Ӳ�������ڽ��̵���ϵͳ��
 * @implNote ����DMA��ô֪�����������ģ�<br>
 *           DMA�ǲ��ǲ���Ҫ֪�����ݽ��뻺��������һ�������Ǹ����̻������ͣ�<br>
 *           ϵͳ��ô֪���û��ѵ�����һ���ض�����<br>
 *           ����취��DMAӦ��֪��ϵͳ��������λ�ã�Ҳ֪������DMA������ָ��Ľ��̻�����λ�á�<br>
 *           ϵͳȥ��������������ԭ�򣬽��DMA�з����ͺŵ��������뻹��������Ӷ��׻��������������Ǹ�����<br>
 *           ��CPU��������DMA���ж��źţ��ͻ��DMA�еĽ��̻������ĵ�ַ������ϵͳ�����������ݰ��Ƶ�������̻�������<br>
 *           ���⣬���ֻ���ṩһ�������ݣ����ԣ����Խ�����һ�����ȶ���DMA��һ���ٴ洢����Ȼ��һ����һ�����������ݼĴ���<br>
 *           �Ĵ���ÿ�õ�һ���־���������ϵͳ��������<br>
 *           DMA��Ҫ��������棬������Ҫ��MMS��ָ�봫��DMA�ڲ�<br>
 */
public class DirectMemoryAccess {
	/**
	 * ϵͳ���������ڴ������ַ
	 * 
	 * @implNote ��DMA��ʼ����ʱ��͸�ֵ������ϵͳ��������ַ
	 */
	Address bufferAddress;
	/**
	 * �����ַ�Ĵ���
	 * 
	 * @apiNote ����ڴ��н��̻��������׵�ַ����ָ���ṩ�߼�ҳ�ţ�ͨ��MMUת���������ڴ��ַ����DMA��
	 */
	Address mainMemRegister;
	/**
	 * �����ַ�Ĵ���
	 * 
	 * @apiNote �������������еĵ�ַ
	 */
	Integer deviceAddRegister;
	/**
	 * �ּ�����
	 * 
	 * @apiNote ��¼Ҫ���˵�����
	 */
	Integer wordCounter;
	/**
	 * �����ж��ź���
	 * 
	 * @apiNote ����DMA��CPU�����ж��ź�
	 */
	boolean sendInterruption;
	/**
	 * �����ж��ź���
	 * 
	 * @apiNote ���ڽ�������CPU�������ź���
	 */
	boolean recieveInterruption;
	/**
	 * �����ź�
	 * 
	 * @apiNote ����DMA�Ĵ��䷽���ź�������ִ�е�I����O����<br>
	 *          Output---true��ʾ�򸨴����<br>
	 *          Input---false��ʾ����������<br>
	 */
	boolean directionMessage;
	/**
	 * ���ݻ���Ĵ���
	 * 
	 * @apiNote ���𽫸����е�һ�����ݰ��ְ��˵��ں˻�����<br>
	 *          ���ں˻������Ŀ鰴�ֽڰ��˵�����<br>
	 */
	Integer wordRegister;
	/**
	 * ���εĴ洢��
	 * 
	 * @apiNote ��Ҫ����������ִ�һ������<br>
	 *          �����һ����һ���ֹ�������洢����������������븨��<br>
	 *          ���룺�������������洢������һ����һ���ֵ�ȡ����<br>
	 *          ����Ӫ��һ�ּ��󣬾��Ǵ����һ����һ���ֶ���DMA
	 */
	ArrayList<Integer> shapelessBuffer;

	/**
	 * MMS��ַ
	 * 
	 * @apiNote Ϊ�˲���MMS������ָ��MMS
	 */
	MemoryManageSystem mmsPoint;

	/**
	 * PMSָ��
	 */
	ProcessManageSystem pmsPoint;

	/**
	 * ���캯�� ��ʼ�����еļĴ������ж��źŷ������ͽ�����<br>
	 * ����һ�����Ļ������������ڴ��ַ
	 */
	public DirectMemoryAccess(int KernelBufferAdd, ProcessManageSystem pms) {
		super();
		this.bufferAddress = new Address(KernelBufferAdd, Address.getPhysicPageBitnum(), Address.getOffsetBitnum());
		this.mainMemRegister = new Address(0, Address.getPhysicPageBitnum(), Address.getOffsetBitnum());
		this.deviceAddRegister = new Integer(0);
		this.wordCounter = 0;
		this.sendInterruption = false;
		this.recieveInterruption = false;
		this.directionMessage = false;
		this.wordRegister = new Integer(0);
		this.shapelessBuffer = new ArrayList<Integer>();
		this.mmsPoint = pms.getMMS();
		this.pmsPoint = pms;
	}

	/**
	 * @apiNote ���ڶ��̵߳�run()����������ѭ���У������⵽CPU�������źţ���ִ�и���Ϊ��
	 */
	public void wholeJob() {
		if (this.recieveInterruption) {
			this.pmsPoint.setRecordData("DMA���յ�CPU�����źţ���������ʼִ�д��乤������");
			// ���CPU�������ж��ź�
			this.recieveInterruption = false;
			// ��ʼ�����乤��
			this.work();
			// ���ù�CPU�����ж��ź�
			this.pmsPoint.setRecordData("����DMA���乤��ִ����ɣ���CPU�����ж��źš�");
			this.sendInterruption = true;
		}
	}

	/**
	 * @apiNote ���ڴ�ᵽ���������ʹ�ã������һ��
	 * @implNote ������һ�������������δ洢��
	 */
	public void readOneBlockFromSub() {
		this.shapelessBuffer = new ArrayList<Integer>(
				this.mmsPoint.getSubMemory().getTheSubBlock(this.deviceAddRegister).inputBlockData());
	}

	/**
	 * @apiNote �Ӹ���ᵽ���������ʹ�ã��������һ��
	 * @implNote �������������ݹ������ָ���豸��
	 */
	public void writeOneBlockToSub() {
		this.mmsPoint.getSubMemory().getTheSubBlock(this.deviceAddRegister).outputBlockData(this.shapelessBuffer);
	}

	/**
	 * @apiNote ���ڴ�ᵽ���������ʹ�ã�������������
	 * @implNote ������ϵͳ����������������ȡ�������������<br>
	 *           ϵͳ������ָ������һ<br>
	 */
	public void fillShapelessBuffer() {
		this.shapelessBuffer.clear();
		this.bufferAddress.updateAddress(MemoryManageSystem.getBufferPagenum(), 0);
		for (int i = 0; i < this.wordCounter; i++) {
			this.wordRegister = this.mmsPoint.readOneWordFromMainMem(this.bufferAddress);
			this.shapelessBuffer.add(this.wordRegister);// ����Ӧ�ò�����ӻ���ʾ
			this.bufferAddress.updateAddress(this.bufferAddress.getData() + MemoryManageSystem.getUnitSize());
		}
		if (this.shapelessBuffer.size() > 256) {
			this.shapelessBuffer.size();//bug
		}
	}

	/**
	 * @apiNote �Ӹ���ᵽ��������У����������
	 * @implNote ��������������ȡ����д������<br>
	 *           ϵͳ������ָ������һ<br>
	 */
	public void emptifyShaplessBuffer() {
		for (int i = 0; i < this.wordCounter; i++) {
			this.wordRegister = this.shapelessBuffer.get(i);// ����Ӧ�ò�����ӻ���ʾ
			this.mmsPoint.getMainMemory().writeWordIntoMemory(this.bufferAddress, this.wordRegister);
			this.bufferAddress.updateAddress(this.bufferAddress.getData() + MemoryManageSystem.getUnitSize());
		}
		this.bufferAddress.updateAddress(MemoryManageSystem.getBufferPagenum(), 0);
		this.shapelessBuffer.clear();
	}

	/**
	 * @apiNote ��DMA����
	 */
	public void work() {
		if (this.directionMessage) {// ��ʾ���������
			this.fillShapelessBuffer();
			this.writeOneBlockToSub();
		} else {// ��ʾ���뵽�ڴ�
			this.readOneBlockFromSub();
			this.emptifyShaplessBuffer();
		}
	}

	/**
	 * �������������DMA����ǰ����DMA��׼��
	 * 
	 * @param direction
	 * @param subAdd
	 * @param userBufferAdd
	 * @param wordNum
	 */
	public void inputParam(boolean direction, int subAdd, int userBufferAdd, int wordNum) {
		this.deviceAddRegister = subAdd;
		this.directionMessage = direction;
		this.mainMemRegister.updateAddress(userBufferAdd);
		this.wordCounter = wordNum;
	}

	public Address getBufferAddress() {
		return bufferAddress;
	}

	public void setBufferAddress(Address bufferAddress) {
		this.bufferAddress = bufferAddress;
	}

	public Address getMainMemRegister() {
		return mainMemRegister;
	}

	public void setMainMemRegister(Address mainMemRegister) {
		this.mainMemRegister = mainMemRegister;
	}

	public Integer getDeviceAddRegister() {
		return deviceAddRegister;
	}

	public void setDeviceAddRegister(Integer deviceAddRegister) {
		this.deviceAddRegister = deviceAddRegister;
	}

	public Integer getWordCounter() {
		return wordCounter;
	}

	public void setWordCounter(Integer wordCounter) {
		this.wordCounter = wordCounter;
	}

	public boolean isSendInterruption() {
		return sendInterruption;
	}

	public void setSendInterruption(boolean sendInterruption) {
		this.sendInterruption = sendInterruption;
	}

	public boolean isRecieveInterruption() {
		return recieveInterruption;
	}

	/**
	 * @apiNote ����һ���̵߳Ķ������е���ѭ���е�ѭ������
	 * @param recieveInterruption
	 */
	public void setRecieveInterruption(boolean recieveInterruption) {
		this.recieveInterruption = recieveInterruption;
	}

	public boolean isDirectionMessage() {
		return directionMessage;
	}

	public void setDirectionMessage(boolean directionMessage) {
		this.directionMessage = directionMessage;
	}

	public Integer getWordRegister() {
		return wordRegister;
	}

	public void setWordRegister(Integer wordRegister) {
		this.wordRegister = wordRegister;
	}

	public ArrayList<Integer> getShapelessBuffer() {
		return shapelessBuffer;
	}

	public void setShapelessBuffer(ArrayList<Integer> shapelessBuffer) {
		this.shapelessBuffer = shapelessBuffer;
	}

	public MemoryManageSystem getMMS() {
		return mmsPoint;
	}

	public void setMMS(MemoryManageSystem mMS) {
		mmsPoint = mMS;
	}

}
