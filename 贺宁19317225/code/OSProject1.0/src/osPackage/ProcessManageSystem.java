package osPackage;

import java.util.ArrayList;

public class ProcessManageSystem {
	/**
	 * ���̹�����Ϣ�ṩ����¼�ļ�����������
	 * 
	 * @implNote Ϊ�˼�¼���̹���ȫ���̣����ò��ڸ���ϸ��λ���а���������Ϣ���ύ���̡�
	 */
	private String recordData = new String();
	/**
	 * ��¼��һ�μ���ļ�¼����һ��
	 */
	private String lastRecordData = new String();
	/**
	 * ��¼һ�����̽���CPUʱ����
	 */
	private Integer intoCpuTime = new Integer(0);

	private final static int INTERRUPTION_NUM = 3;// �����жϷ������DMA��ʱ��
	private final static int SYSTEM_INSTRUCTION_NUM = 128;// һ����Ȩ������ռ��������Ϊ������Ȩָ�����6ҳ
	private final static int SPECIAL_SYSTEMCALL_NUM = 5;// ǰ5��ϵͳ����������ָ��ר�á�
	private final static int NORMAL_SYSTEMCALL_NUM = 4;// �ܹ���9��ϵͳ���ã�ǰ5��������������ָ���4������ϵͳ����ָ�
	private final static int MAX_PRIORITY = 10;// ���ȼ����Ϊ10
	private final static int MAX_INSTRUCTION_NUM = Block.getBlockSize() * 3 + 50;// ���ָ����Ŀ
	private final static int MIN_INSTRUCTION_NUM = Block.getBlockSize() * 3 + 30;// ��Сָ����Ŀ
	private final static int MAX_DATA_NUM = Block.getBlockSize() + 50;// ���������
	private final static int MIN_DATA_NUM = Block.getBlockSize() + 30;// ��С������

	private final static int KERNEL_STACK_SIZE = 1;// ����ջĬ��ռһҳ
	private final static int USERBUFFER_SIZE = 1;// ÿ������ӵ�еĻ�����ҳ��
	private final static long NORMAL_MS = 800;// ��ָͨ������ʱ800����
	private final static long CALCULATE_MS = 500;// ������Ϊ�����ʱ500����
	private final static long SYSTEMCALL_MS = 500;// ��ͨϵͳ������Ϊ�����ʱ500����

	private Clock clock;// ʱ��
	private MemoryManageSystem MMS;// �洢����ϵͳ
	private CentralProcessingUnit CPU;// ��Ҫ�����ĳ��˴洢������CPU
	private DirectMemoryAccess DMA;// DMAӲ��ģ�⣬����IOָ��
	private Monitor monitor;// ��Դ�����������ڽ��̵�ͬ�����⣩

	public ProcessManageSystem() {
		super();
		this.clock = new Clock();
		this.MMS = new MemoryManageSystem();
		this.CPU = new CentralProcessingUnit(0, 0, 0, 0);
		this.DMA = new DirectMemoryAccess(new Address(Address.getPhysicPageBitnum(), Address.getOffsetBitnum(),
				MemoryManageSystem.getBufferPagenum(), 0).getData(), this);
		this.monitor = new Monitor();
		this.setRecordData("����������������");
		this.setRecordData("����ϵͳ����������");
		this.MMS.getSystemBuddyAllocator().alloc_pages(1, 0);// ����ID��Ϊ0��ʾϵͳ�����������뻺������
		this.setRecordData("����ҳ����ռ�ڴ�");
		this.MMS.getSystemBuddyAllocator().alloc_pages(1, 0);// ������������ҳ��ռ䡣
		this.setRecordData("����PCB�ء���");
		this.MMS.getSystemBuddyAllocator()
				.alloc_pages(PCBQueue.getSize() * ProcessControlBlock.getPcbSize() / Block.getBlockSize(), 0);// ����PCB��
		this.setRecordData("��ʼ��ϵͳָ�����");
		ArrayList<Integer> blockIds = new ArrayList<Integer>();
		for (int i = 0; i < (INTERRUPTION_NUM + SPECIAL_SYSTEMCALL_NUM + NORMAL_SYSTEMCALL_NUM) * SYSTEM_INSTRUCTION_NUM
				/ (Block.getBlockSize() * 2); i++) {
			blockIds.addAll(this.MMS.getSystemBuddyAllocator().alloc_pages(2, 0));// һ��������ҳ���һ��
		}
		this.writeSysProcessesIntoBlocks(blockIds);
		this.setRecordData("ϵͳ��ʼ����ϣ�һ�о���������������");
	}

	/**
	 * @param sysInsId ��Ȩ������<br>
	 *                 0.P����<br>
	 *                 1.V����<br>
	 *                 2.ȱҳ�쳣�������<br>
	 *                 3.DMA��ֵ����<br>
	 *                 4.Input�ƺ����<br>
	 *                 5~8����ϵͳ���ó���<br>
	 *                 9.DMA�жϴ������<br>
	 *                 10.ʱ���жϴ������<br>
	 *                 11.��������жϴ������<br>
	 * @return ������16λָ�
	 */
	public ArrayList<Integer> createOneSysInstr(int sysInsId) {
		if (sysInsId > 11 || sysInsId < 0) {
			return null;
		} else {
			ArrayList<Integer> arrayList = new ArrayList<Integer>();
			for (int i = 0; i < SYSTEM_INSTRUCTION_NUM; i++) {
				int id = sysInsId & 0xf;
				int operateCode = i & 0xfff;
				Integer integer = new Integer(0);
				integer += (id << 12);
				integer += operateCode;
				arrayList.add(integer);
			}
			return arrayList;
		}
	}

	/**
	 * @param blockIds ���ٵ�ָ�ʹ�õĿ�š�
	 */
	public void writeSysProcessesIntoBlocks(ArrayList<Integer> blockIds) {
		ArrayList<Integer> insPageData = new ArrayList<Integer>();
		for (int i = 0; i < INTERRUPTION_NUM + SPECIAL_SYSTEMCALL_NUM + NORMAL_SYSTEMCALL_NUM; i++) {
			ArrayList<Integer> instructions = createOneSysInstr(i);
			insPageData.addAll(instructions);
		}
		for (int i = 0; i < blockIds.size(); i++) {
			ArrayList<Integer> pageData = new ArrayList<Integer>(
					insPageData.subList(i * Block.getBlockSize(), (i + 1) * Block.getBlockSize()));
			this.MMS.getMainMemory().getTheMainBlock(blockIds.get(i)).outputBlockData(pageData);
		}
	}

	/**
	 * @param block ��ת̬���������Ľ���
	 * @return ��ָ������ȡ���ź���
	 */
	public MESSAGE analyseMessage(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case NEED_REGISTER:
			return this.analyseRegister(this.CPU.getIR().getRegisterIDList().get(0));
		case RELEASE_REGISTER:
			return this.analyseRegister(this.CPU.getIR().getRegisterIDList().get(0));
		case NEED_REGISTER1:
			return this.analyseRegister(this.CPU.getIR().getRegisterIDList().get(1));
		case RELEASE_REGISTER1:
			return this.analyseRegister(this.CPU.getIR().getRegisterIDList().get(1));
		case NEED_BUFFER:
			return MESSAGE.BUFFER;
		case RELEASE_BUFFER:
			return MESSAGE.BUFFER;
		case NEED_DEVICE:
			return this.analyseDevice(this.CPU.getIR().getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress());
		case RELEASE_DEVICE:
			return this.analyseDevice(this.CPU.getIR().getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress());
		default:
			return MESSAGE.NONE;
		}
	}

	/**
	 * @param registerID ����ָ��ļĴ���ID��
	 * @return ���ؼĴ����ź�����
	 */
	public MESSAGE analyseRegister(int registerID) {
		switch (registerID) {
		case 0:
			return MESSAGE.REGISTER0;
		case 1:
			return MESSAGE.REGISTER1;
		case 2:
			return MESSAGE.REGISTER2;
		case 3:
			return MESSAGE.REGISTER3;
		case 4:
			return MESSAGE.REGISTER4;
		case 5:
			return MESSAGE.REGISTER5;
		case 6:
			return MESSAGE.REGISTER6;
		case 7:
			return MESSAGE.REGISTER7;
		default:
			return MESSAGE.NONE;
		}
	}

	/**
	 * @param deviceID ����ID��
	 * @return ���������ź�����
	 */
	public MESSAGE analyseDevice(int deviceID) {
		switch (deviceID) {
		case 0:
			return MESSAGE.DEVICE0;
		case 1:
			return MESSAGE.DEVICE1;
		case 2:
			return MESSAGE.DEVICE2;
		case 3:
			return MESSAGE.DEVICE3;
		default:
			return MESSAGE.NONE;
		}
	}

	/**
	 * ����ϵͳ���úţ�ִ�ж�Ӧ��ϵͳ����ָ��
	 * 
	 * @apiNote 0:P����<br>
	 *          1:V����<br>
	 *          2:ȱҳ�쳣�������<br>
	 *          3:DMA��������<br>
	 * @param callID  ϵͳ���ú�
	 * @param message P������������źŻ�V�������ͷŵ��ź�
	 * @param block   ��Ϊϵͳ���ö��������Ľ���PCB
	 */
	public void systemCall(int callID, ProcessControlBlock block) {
		// ��������ԭ������PV�ź�
		MESSAGE message = this.analyseMessage(block);
		switch (callID) {
		case 0:
			// P����������ԭ����Ҫ**��
			this.setRecordData("ϵͳ����::::" + "ϵͳΪ����" + block.getProcessID() + "������Դ<" + message.toString() + ">");
			if (this.monitor.P(message, block.getProcessID())) {
				// P�����ɹ���������������
				this.setRecordData("ϵͳ����::::" + "Ϊ����" + block.getProcessID() + "������Դ<" + message.toString() + ">"
						+ "�ɹ�������(���ѽ���" + block.getProcessID() + ")");
				this.wakePro(block.getProcessID());
			}
			break;
		case 1:
			// V����������ԭ���ͷ�**��
			this.setRecordData("ϵͳ����::::" + "ϵͳΪ����" + block.getProcessID() + "�ͷ���Դ<" + message.toString() + ">");
			Integer proID = this.monitor.V(message, block.getProcessID());
			this.setRecordData("ϵͳ����::::" + "Ϊ����" + block.getProcessID() + "�ͷ���Դ<" + message.toString() + ">"
					+ "�ɹ�������(���ѽ���" + block.getProcessID() + ")");
			if (proID != null) {
				// V�����ɹ���������Ӧ���̡�
				this.setRecordData("ϵͳ����::::" + "ϵͳΪ����" + block.getProcessID() + "�ͷ���Դ,���µȴ���Դ����" + proID + "�����ѣ�");
				this.wakePro(proID);
			}
			this.wakePro(block.getProcessID());
			break;
		case 2:
			// ȱҳ�쳣�����������ԭ������ҳ�棩
			// ��ִ��ȱҳ�쳣ǰ�ȼ��һ���Ƿ���Ҫ����
			// �м����ȣ�����Ƿ���Ҫ�������
			if (this.MMS.getPageTable().needPending()) {
				this.pendPro(this.MMS.getPcbQueue().findMaxProID());
			}
			this.setRecordData("ϵͳ����::::" + "Ϊ����" + block.getProcessID() + "ִ��ȱҳ�쳣�������");
			this.MMS.doPageFault(block.getProcessID());
			// ִ�к󽫽��̻���
			this.setRecordData(
					"ϵͳ����::::" + "Ϊ����" + block.getProcessID() + "ִ��ȱҳ�쳣�������ɹ�������(���ѽ���" + block.getProcessID() + ")");
			this.wakePro(block.getProcessID());
			break;
		case 3:
			// DMA������������ԭ�򣺵ȴ�����/����ɣ�
			// 1����DMA��ֵ��DMA����׼��(Ĭ��ȡһҳ����)
			if (block.getControlInfo().getWaitReason() == WAIT_REASON.INPUT_WAIT) {
				this.setRecordData("ϵͳ����::::" + "ϵͳΪ����" + block.getProcessID() + "����DMA�������롣");
				this.DMA.inputParam(false, this.CPU.getIR().getDeviceAddress(),
						block.getControlInfo().getBufferPoint().getData(), Block.getBlockSize());
			}
			if (block.getControlInfo().getWaitReason() == WAIT_REASON.OUTPUT_WAIT) {
				this.setRecordData("ϵͳ����::::" + "ϵͳΪ����" + block.getProcessID() + "�����ݰ��Ƶ�ϵͳ������������DMA���������");
				this.DMA.inputParam(true, this.CPU.getIR().getDeviceAddress(),
						block.getControlInfo().getBufferPoint().getData(), Block.getBlockSize());
				this.MMS.getAddressBus().inputAddressIntoBus(this.DMA.getMainMemRegister());
				// ��Ҫͨ��MMU�ҵ����̻������������ַ
				for (int i = 0; i < 2; i++) {
					// ���ݵ�ַ���ϵĵ�ַ���зô�
					if (this.mmuWork(block.getProcessID())) {
						Address physicAdd = this.MMS.getMmu().getPhysicAddress();
						// ��ϵͳ�������ڵ����ݰ��˵��û���������
						this.MMS.coverBlockFromMainAToMainB(physicAdd.getPageID(),
								this.DMA.getBufferAddress().getPageID());
						// ʲôʱ��ɹ�ѹ���ֳ��ͽ�������һ����Ҫִ������
						break;
					} else {
						// ֱ��ȱҳ�жϣ������ǵ�ǰ״̬
						this.MMS.doPageFault(block.getProcessID());
					}
				}
			}
			// 2����DMA���ͼ����źţ�����DMA
			this.DMA.setRecieveInterruption(true);
			// ����DMA�ᵼ�¸ý���һֱ������ֱ��DMA��ɹ��������Բ���Ҫ���ѡ�
			break;
		case 4:
			// Input�ƺ���򣺽�ϵͳ�����������ݰ��˵��û�������
			if (block.getControlInfo().getWaitReason() == WAIT_REASON.TRANS_DATA_INTPUT) {
				this.setRecordData("ϵͳ����::::" + "ϵͳΪ����" + block.getProcessID() + "��ϵͳ���������ݰ��Ƶ����̻�������");
				// ��Ҫͨ��MMU�ҵ����̻������������ַ
				this.MMS.getAddressBus().inputAddressIntoBus(this.DMA.getMainMemRegister());
				for (int i = 0; i < 2; i++) {
					// ���ݵ�ַ���ϵĵ�ַ���зô�
					if (this.mmuWork(block.getProcessID())) {
						Address physicAdd = this.MMS.getMmu().getPhysicAddress();
						// ��ϵͳ�������ڵ����ݰ��˵��û���������
						this.MMS.coverBlockFromMainAToMainB(this.DMA.getBufferAddress().getPageID(),
								physicAdd.getPageID());
						// ʲôʱ��ɹ�ѹ���ֳ��ͽ�������һ����Ҫִ������
						break;
					} else {
						// ֱ��ȱҳ�жϣ������ǵ�ǰ״̬
						this.MMS.doPageFault(block.getProcessID());
					}
				}
			}
			this.setRecordData("ϵͳ����::::" + "ϵͳΪ����" + block.getProcessID() + "��ϵͳ���������ݰ��Ƶ����̻������ɹ�������(���ѽ���"
					+ block.getProcessID() + ")");
			this.wakePro(block.getProcessID());
			break;
		default:// ϵͳ����ָ����ƾ��������
			try {
				this.setRecordData("ϵͳ����::::" + "ϵͳΪ����" + block.getProcessID() + "ִ������ϵͳ����");
				Thread.sleep(SYSTEMCALL_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.wakePro(block.getProcessID());
			break;
		}
	}

	/**
	 * ִ��һ�κ���
	 * 
	 * @apiNote ���ĺ��������̵����̵߳���ѭ����
	 * @implNote 1����鵱ǰ�Ƿ������е�PCB��<br>
	 *           (1)���û��ȥ���������в������½���<br>
	 *           (2)���гɹ��ж��������־�������<br>
	 *           (3)����������Ҫ�ú���ջ�ٻָ�һ���ֳ�������ֱ���л�Ϊ�û�̬��<br>
	 *           2�������ǰ�����еĽ���:<br>
	 *           (1)ȡ��ָ��<br>
	 *           (2)ִ�е�ǰָ��<br>
	 *           (3)����ж��ź�<br>
	 *           Ϊ��ÿ��ִ��ָ����ʹ�õĻ��ᶼ����ȵģ���������Ϊ�ǵ�һ��ָ��Ͷ໨ʱ�䣬<br>
	 *           �������û�н���ִ�о�ר�ŵ���ִ��һ�����н��̲�����<br>
	 */
	public void executeOnce() {
		if (this.MMS.getPcbQueue().getRunningPCB().isEmpty()) {// ˵����ǰ�޽�������
			// CPU�Ĺ��������㵱ǰû�н������У�ҲҪ��ⷢ�����ж��źš�
			this.interruption(null);
			// �м����ȣ�����Ƿ���Ҫ�������
			if (this.MMS.getPageTable().needPending()) {
				this.pendPro(this.MMS.getPcbQueue().findMaxProID());
			}
			// ��ʼ�����������������µĽ��̡�
			this.CPU.getPSW().changeToKernal();
			if (this.runPro()) {
				// ˵�����������н��̣��ҳɹ�����
				ProcessControlBlock runningblock = this.MMS.getPcbQueue().getRunningPCB().get(0);
				if (runningblock.getControlInfo().getWaitReason() != WAIT_REASON.UNKNOW) {
					// ��������´����Ľ��̻���Ϊʱ��Ƭ�ľ������Ľ��̣�˵�������������У�Ӧ���ú���ջ�ָ�
					this.recoverSiteinfoFromKernelStack(runningblock);
				} else {
					// ������½����̻�ʱ���̣�������ָ��û�̬
					this.CPU.getPSW().backToUser();
				}
			}
		} else {// ��ǰ�н�������
			ProcessControlBlock block = this.MMS.getPcbQueue().getRunningPCB().get(0);
			if (this.isExeFinnished(block)) {// ָ�ִ����ɣ�
				this.CPU.getPSW().changeToKernal();// �л�Ϊ�ں�̬
				this.deletePro(block.getProcessID());// ����ִ�н����Ľ���

				this.interruption(null);
			} else {// ָ�û��ִ�����
				if (block.getControlInfo().getWaitReason() == WAIT_REASON.UNKNOW
						|| block.getControlInfo().getWaitReason() == WAIT_REASON.REQUEST_PAGE_INSTRUCTION) {
					// ��һ��ָ���Ѿ�ִ�����ˣ�������ȡָ������е�ȱҳ�жϡ�
					// ȡָ����ȱҳûȡ�ɣ���������ִ��ȱҳ�жϡ�����һ���ֵ��ý����ٴ�ִ�иý��̾Ϳ���ȡ��ָ����
					this.readInstruction(block);
				}
				if (!this.MMS.getPcbQueue().getRunningPCB().isEmpty()) {// ������еĽ�����Ϊȡָʧ�ܶ������Ͳ��ܼ���ִ�С�
					// ִ�и�ָ�����ȡָ��ʧ�ܣ��ͻ�������һ�����̣�������н��̶�ȱҳ���ͻ������ʼ�����������ѻ��ѵ���һ����
					this.exeOneInstruction(block);
					// ���DMA���ж��źţ�������ж��źţ���ʱ���ᵱǰ���̣������жϣ�������ɺ����ִ��ԭ���Ľ��̣����ѵĽ������ȼ�������ߣ���һ����ִ������
					this.interruption(block);
				}
			}
		}
	}

	public void recordRunTimeOnceA() {
		synchronized (clock) {
			intoCpuTime = clock.getSecondNum();
		}
	}

	public void recordRunTimeOnceB(ProcessControlBlock block) {
		synchronized (clock) {
			block.getControlInfo().setUsedCPUTime(clock.getSecondNum() - intoCpuTime);
			block.getControlInfo()
					.setUsedSumTime(block.getControlInfo().getUsedSumTime() + block.getControlInfo().getUsedCPUTime());
			intoCpuTime = clock.getSecondNum();
		}
	}

	/**
	 * ������Ƿ�ִ����ɣ�
	 * 
	 * @param block
	 * @return true
	 *         PC��ǰ�洢�������һ��ָ���ַ�ĺ�1��λ�ã��ȼ���IRȡ�õ�ָ�������һ����Чָ�����ָ���Ѿ�ִ�����ˣ�����ԭ��Ϊδ֪����<br>
	 *         false PC��ǰָ��ĵ�ַ����ָ��<br>
	 */
	public boolean isExeFinnished(ProcessControlBlock block) {
		return (block.getControlInfo().getTextPoint().distanceTo(this.CPU.getPC().getAddress()) == block
				.getControlInfo().getInstructionNum())
				&& (block.getControlInfo().getWaitReason() == WAIT_REASON.UNKNOW);
	}

	/**
	 * ���ܣ�ִ��һ��ָ��������е�Ӳ���ﵽָ���ִ�У�
	 * 
	 * @param block Ҫ��������ָ��Ľ��̵�PCB
	 */
	public void exeOneInstruction(ProcessControlBlock block) {
		switch (this.CPU.getIR().getInstructionID()) {
		case ACCESS_MEMORY:
			this.setRecordData("����" + block.getProcessID() + "����ִ�зô�ָ�" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeAccessMemory(block);
			break;
		case INPUT:
			this.setRecordData("����" + block.getProcessID() + "����ִ������ָ�" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeInput(block);
			break;
		case OUTPUT:
			this.setRecordData("����" + block.getProcessID() + "����ִ�����ָ�" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeOutput(block);
			break;
		case SYSTEM_CALL:
			this.setRecordData("����" + block.getProcessID() + "����ִ��ϵͳ����ָ�" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeSystemCall(block);
			break;
		case CALCULATE:
			this.setRecordData("����" + block.getProcessID() + "����ִ�м���ָ�" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeCalculate(block);
			break;
		case NORMAL:
			this.setRecordData("����" + block.getProcessID() + "����ִ����ָͨ�" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeNormal(block);
			break;
		case JUMP:
			this.setRecordData("����" + block.getProcessID() + "����ִ����תָ�" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeJump(block);
			break;
		default:
			break;
		}
		this.recordRunTimeOnceB(block);
	}

	/**
	 * @apiNote ��תָ��ִ��
	 * @param block Ҫ��������ָ��Ľ��̵�PCB
	 * @implNote 1����PC��IR�е�ƫ��ֵ��ִ�����ִ����һ����<br>
	 */
	public void exeJump(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			if (this.CPU.getIR().getJumpOffset() != (Block.getBlockSize() - 1) * MemoryManageSystem.getUnitSize()) {
				break;// ����bug�����ָ����д����ת���þͻ᲻�ԡ�
			}
			this.CPU.getPC().updateData(this.CPU.getPC().getData() + this.CPU.getIR().getJumpOffset());
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("����" + block.getProcessID() + "����ִ����תָ���������");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote ��ͨϵͳ���ú���ִ��
	 * @param block Ҫ��������ָ��Ľ��̵�PCB
	 * @implNote 1�������ֳ�����CPU��IR�Ĵ����ṩ��ϵͳ���úŽ���ϵͳ����<br>
	 *           2��ִ����ɣ�����ԭ����գ�ִ����һ��<br>
	 */
	public void exeSystemCall(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			// ˵��û�����������ҵ�ǰ�Ƿô�ָ�Ӧ�ô�ͷ��ʼִ��
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_NORMAL_SYSTEM_EXE);
			// ϵͳ���ú�Ϊ0����P����
			this.systemCall(this.CPU.getIR().getCallID(), block);
			break;
		case NEED_NORMAL_SYSTEM_EXE:
			// ˵��ϵͳָ�ִ����ɣ�ָ��ִ�н�����������ԭ����Ϊunknown��Ӧ��ȡ��һ��ָ���ˡ�
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("����" + block.getProcessID() + "����ִ��ϵͳ����ָ���������");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote �ô�ָ���ִ��
	 * @param block Ҫ��������ָ��Ľ��̵�PCB
	 * @implNote 1������Ĵ���<br>
	 *           2���ô�<br>
	 *           3���ͷżĴ���<br>
	 *           4�����ж��ź�<br>
	 *           5������һ��ָ��<br>
	 */
	public void exeAccessMemory(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			// ˵��û�����������ҵ�ǰ�Ƿô�ָ�Ӧ�ô�ͷ��ʼִ��
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_REGISTER);
			// ϵͳ���ú�Ϊ0����P����
			this.systemCall(0, block);
			break;
		case NEED_REGISTER:
			// ˵���ý����Ѿ���üĴ��������Ƿô�ָ�Ӧ�ÿ�ʼ�ô�
			// 1.�������棨������PCB�����ݶ�ҳ�ź�����IR��ƫ��д��AR�Ĵ�������AR�����ݶ����ַ���ߣ�ͨ��MMU�ô棬��������ȱҳ�жϣ�
			this.CPU.getAR().updateData((block.getControlInfo().getDataPoint().getPageID() << Address.getOffsetBitnum())
					+ this.CPU.getIR().getAccessOffset());// AR�Ĵ�����ֵ
			this.MMS.getAddressBus().inputAddressIntoBus(this.CPU.outputAR());// �����ַ����
			// 2.���ݵ�ַ���ϵĵ�ַ���зô�
			if (this.mmuWork(block.getProcessID())) {// ���MMU�ɹ�ת���������ַ����Ӧ������ȡ�����ͷżĴ���
				Address physicAdd = this.MMS.getMmu().getPhysicAddress();
				// ���������ַȡ���������������ߡ�
				this.MMS.getDataBus().inputDataIntoBus(this.MMS.readOneWordFromMainMem(physicAdd));
				// �ͷżĴ���
				this.saveSiteinfoIntoKernelStack(block);
				this.blockPro(WAIT_REASON.RELEASE_REGISTER);
				// ϵͳ���ú�Ϊ1����V����
				this.systemCall(1, block);
			} else {// ���MMU����ʧ�ܣ�˵��ȱҳ��Ӧ��ִ��ȱҳ�ж�
				this.saveSiteinfoIntoKernelStack(block);
				this.blockPro(WAIT_REASON.REQUEST_PAGE_ACCESSMEM);
				// ϵͳ���ú�Ϊ2�� ��ȱҳ�쳣�������
				this.systemCall(2, block);
			}
			break;
		case REQUEST_PAGE_ACCESSMEM:// ��һ��ʵ��������һ�����ظ����д��Ż�
			// ˵���ý���ȱҳ�ж��Ѿ�������ɣ��ٴ���MMUת����ַ
			this.MMS.getAddressBus().inputAddressIntoBus(this.CPU.outputAR());// �����ַ����
			this.mmuWork(block.getProcessID());
			Address physicAdd = this.MMS.getMmu().getPhysicAddress();
			// ���������ַȡ���������������ߡ�
			this.MMS.getDataBus().inputDataIntoBus(this.MMS.readOneWordFromMainMem(physicAdd));
			// �ͷżĴ���
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_REGISTER);
			// ϵͳ���ú�Ϊ1����V����
			this.systemCall(1, block);
			break;
		case RELEASE_REGISTER:
			// ˵���ͷżĴ����ɹ�����ָ��ִ����ɡ�������ԭ����Ϊunknown��Ӧ��ȡ��һ��ָ���ˡ�
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("����" + block.getProcessID() + "����ִ�зô�ָ���������");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote ���뺯��ִ��
	 * @param block Ҫ��������ָ��Ľ��̵�PCB
	 * @implNote 1����������<br>
	 *           2������ϵͳ������<br>
	 *           3���������̣�����DMA<br>
	 *           4���ͷŻ�����<br>
	 *           5���ͷ�����<br>
	 */
	public void exeInput(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			// ˵��û�����������ҵ�ǰ�Ƿô�ָ�Ӧ�ô�ͷ��ʼִ��
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_DEVICE);
			// ϵͳ���ú�Ϊ0����P����
			this.systemCall(0, block);
			break;
		case NEED_DEVICE:
			// ˵���ý����ѻ���豸
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_BUFFER);
			// ϵͳ���ú�Ϊ0����P����
			this.systemCall(0, block);
			break;
		case NEED_BUFFER:
			// ˵���ý����ѻ�û�����
			// ������ǰ����
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.INPUT_WAIT);
			// ϵͳ���ú�Ϊ3����DMA����
			this.systemCall(3, block);
			break;
		case INPUT_WAIT:
			// ˵��DMA����ɹ����Լ�Ҳ�����ѡ�
			// ������ǰ����
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.TRANS_DATA_INTPUT);
			// ϵͳ���ð������ݵ����̻�����
			this.systemCall(4, block);
			break;
		case TRANS_DATA_INTPUT:
			// �ͷŻ�����
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_BUFFER);
			// ϵͳ���ú�Ϊ1����V����
			this.systemCall(1, block);
			break;
		case RELEASE_BUFFER:
			// ˵���ͷŻ�������ɣ��ͷ��豸
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_DEVICE);
			this.systemCall(1, block);
			break;
		case RELEASE_DEVICE:
			// ˵���豸�ͷ���ɣ�ָ��ִ�н�����������ԭ����Ϊunknown��Ӧ��ȡ��һ��ָ���ˡ�
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("����" + block.getProcessID() + "����ִ������ָ���������");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote �������ִ��
	 * @param block Ҫ��������ָ��Ľ��̵�PCB
	 * @implNote 1����������<br>
	 *           2������ϵͳ������<br>
	 *           3���������̣�����DMA<br>
	 *           4���ͷŻ�����<br>
	 *           5���ͷ�����<br>
	 */
	public void exeOutput(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			// ˵��û�����������ҵ�ǰ�Ƿô�ָ�Ӧ�ô�ͷ��ʼִ��
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_DEVICE);
			// ϵͳ���ú�Ϊ0����P����
			this.systemCall(0, block);
			break;
		case NEED_DEVICE:
			// ˵���ý����ѻ���豸
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_BUFFER);
			// ϵͳ���ú�Ϊ0����P����
			this.systemCall(0, block);
			break;
		case NEED_BUFFER:
			// ˵���ý����ѻ�û�����
			// ������ǰ����
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.OUTPUT_WAIT);
			// ϵͳ���ú�Ϊ3����DMA����
			this.systemCall(3, block);
			break;
		case OUTPUT_WAIT:
			// ˵��DMA����ɹ����Լ�Ҳ�����ѡ�
			// �ͷŻ�����
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_BUFFER);
			// ϵͳ���ú�Ϊ1����V����
			this.systemCall(1, block);
			break;
		case RELEASE_BUFFER:
			// ˵���ͷŻ�������ɣ��ͷ��豸
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_DEVICE);
			this.systemCall(1, block);
			break;
		case RELEASE_DEVICE:
			// ˵���豸�ͷ���ɣ�ָ��ִ�н�����������ԭ����Ϊunknown��Ӧ��ȡ��һ��ָ���ˡ�
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("����" + block.getProcessID() + "����ִ�����ָ���������");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote ����ָ��ִ��
	 * @param block Ҫ��������ָ��Ľ��̵�PCB
	 */
	public void exeCalculate(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			// ˵��û�����������ҵ�ǰ�Ƿô�ָ�Ӧ�ô�ͷ��ʼִ��
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_REGISTER);
			// ϵͳ���ú�Ϊ0����P����
			this.systemCall(0, block);
			break;
		case NEED_REGISTER:
			// ˵��û�����������ҵ�ǰ�Ƿô�ָ�Ӧ�ô�ͷ��ʼִ��
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_REGISTER1);
			// ϵͳ���ú�Ϊ0����P����
			this.systemCall(0, block);
			break;
		case NEED_REGISTER1:
			// ˵����ǰ�Ľ���ӵ�������Ĵ�����ִ��һ���ٵļ������
			try {
				Thread.sleep(CALCULATE_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// �ͷżĴ���
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_REGISTER1);
			// ϵͳ���ú�Ϊ1����V����
			this.systemCall(1, block);
			break;
		case RELEASE_REGISTER1:
			// ˵���ͷŵ�һ���Ĵ������
			// �ͷ���һ���Ĵ���
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_REGISTER);
			// ϵͳ���ú�Ϊ1����V����
			this.systemCall(1, block);
			break;
		case RELEASE_REGISTER:
			// ˵���ͷ���üĴ�������ɣ�ָ��ִ����ɡ�������ԭ����Ϊunknown��Ӧ��ȡ��һ��ָ���ˡ�
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("����" + block.getProcessID() + "����ִ�м���ָ���������");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote ִ����ָͨ��
	 * @param block Ҫ��������ָ��Ľ��̵�PCB
	 */
	public void exeNormal(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			try {
				Thread.sleep(NORMAL_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// ������ԭ����Ϊunknown��Ӧ��ȡ��һ��ָ���ˡ�
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("����" + block.getProcessID() + "����ִ����ָͨ���������");
			break;
		default:
			break;
		}
	}

	/**
	 * ����PC��ָ�򣬶���һ��ָ������IR�Ĵ���������PC�Լ�һ
	 * 
	 * @apiNote ǰ���ȡָ����
	 * @param block �������еĽ���PCB
	 */
	public void readInstruction(ProcessControlBlock block) {
		this.MMS.getAddressBus().inputAddressIntoBus(this.CPU.outputPC());
		// ���ݵ�ַ���ϵĵ�ַ���зô�
		if (this.mmuWork(block.getProcessID())) {
			this.CPU.getPC().autoIncrease();// ֻ�гɹ�ȡ��PC��ָ���ָ������Լ�1��ȱҳ�жϲ������Լ�һ��
			Address physicAdd = this.MMS.getMmu().getPhysicAddress();
			// ���������ַȡ��ָ�����������ߡ�
			this.MMS.getDataBus().inputDataIntoBus(this.MMS.readOneWordFromMainMem(physicAdd));
			// �������ߵ�����ȡ������IR�Ĵ���
			this.CPU.dataBusToIR(this.MMS.getDataBus().outputDataFromBus());
			// ������ԭ���Ϊδ֪��ʹ����һ��ָ����Դ�ͷ����
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
		} else {
			this.saveSiteinfoIntoKernelStack(block);
			// ���������ȴ�ԭ���������ԭ��Ͳ��ᱻ��Ϊ�Ƿô�ָ��ô���Ϊ�ɹ�
			this.blockPro(WAIT_REASON.REQUEST_PAGE_INSTRUCTION);
			// ϵͳ���ú�Ϊ2�� ��ȱҳ�쳣�������
			this.systemCall(2, block);
		}
	}

	/**
	 * �жϴ���
	 * 
	 * @apiNote Ŀǰֻ����IO�жϵĴ���DMA�����źŵĴ�����ʱ���жϣ�ʱ�ӷ�����ʱ��Ƭ����Ϣ��
	 * @param block ��ǰ�������еĽ���PCB
	 * @implNote 1�����DMA�Ƿ�����Ϣ<br>
	 *           ��1������ջ�����ֳ�<br>
	 *           ��2���������뻹�����ִ���жϴ������<br>
	 *           ��3��������ɣ�����ջ�ָ��ֳ�<br>
	 *           2�����ʱ���Ƿ�����Ϣ<br>
	 *           ��1��תΪ����̬<br>
	 *           ��2����ǰPCB�Ƶ�������β<br>
	 *           ��3��ת���û�̬<br>
	 */
	public void interruption(ProcessControlBlock block) {
		// DMA�жϴ������
		if (block != null) {
			if (this.DMA.isSendInterruption()) {
				this.DMA.setSendInterruption(false);// �Ƚ��������ж��ź����
				// �����ֳ�
				this.saveSiteinfoIntoKernelStack(block);
				if (this.DMA.isDirectionMessage()) {
					this.interruptHandlerForOutput();
				} else {
					this.interruptHandlerForInput();
				}
				this.recoverSiteinfoFromKernelStack(block);
			}
		} else {// ��ǰ�޽���ֱ��ת����̬���������귵���û�̬��
			if (this.DMA.isSendInterruption()) {
				this.DMA.setSendInterruption(false);// �Ƚ��������ж��ź����
				// �����ֳ�
				this.CPU.getPSW().changeToKernal();
				if (this.DMA.isDirectionMessage()) {
					this.interruptHandlerForOutput();
				} else {
					this.interruptHandlerForInput();
				}
				this.CPU.getPSW().backToUser();
			}
		}
		// ʱ���жϴ������
		if (this.clock.timePiecePassed()) {
			this.setRecordData("ʱ���жϷ�����ʱ��Ƭ�ѹ���" + this.clock.getSecondNum() + "s");
			// ��ȡʱ���źţ�������ա�
			this.clock.setTimepeicePassed(false);
			this.CPU.getPSW().changeToKernal();
			this.readyPro();
			this.CPU.getPSW().backToUser();
		}
		if (this.clock.checkDeadLockTimePassed()) {
			this.setRecordData("�������ʱ�䵽��" + this.clock.getSecondNum() + "s");
			this.clock.setDeadlocktimePassed(false);
			this.CPU.getPSW().changeToKernal();
			ArrayList<Integer> arrayList = this.monitor.checkDeadLock();
			if (arrayList.size() == 0) {
				this.setRecordData("��δ����������");
			} else {
				String deadRecord = new String("��������������:");
				for (int i = 0; i < arrayList.size(); i++) {
					deadRecord += "(" + arrayList.get(i) + ")";
				}
				deadRecord += new String("��������״̬����������������򡭡�");
				this.setRecordData(deadRecord);
				this.recoverDeadLine(arrayList);
				this.setRecordData("������������������������������̵���Դ���ع�һ��ָ�");
			}
			this.CPU.getPSW().backToUser();
		}
	}

	/**
	 * Input�жϴ������
	 * 
	 * @apiNote ����DMA��������ɺ󷢳����ж��źŴ���
	 * @implNote ���Ƚ�ϵͳ���������ݰ��˵��û�������<br>
	 *           ������ΪInputָ��������������׵Ľ���<br>
	 * @return �����������
	 */
	public boolean interruptHandlerForInput() {
		ProcessControlBlock block = this.MMS.getPcbQueue().findFirstWaitBlockByReason(WAIT_REASON.INPUT_WAIT);
		if (block != null) {
			// ��Ҫ�����ȼ��������ֻ��Ҫ�Ƚ��̷���CPUȥ����ִ�оͿ����ˡ�
//			// ��Ҫ���ѵĽ������ȼ���Ϊ���
//			block.getControlInfo().setProcessPriority(MAX_PRIORITY);
			this.wakePro(block.getProcessID());// ������Ϊ�ȴ��������Ľ���
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Output�жϴ������
	 * 
	 * @apiNote ����DMA�������ɺ󷢳����ж��źŴ���
	 * @implNote ������ΪOutputָ��������������׵Ľ���<br>
	 * @return �����������
	 */
	public boolean interruptHandlerForOutput() {
		ProcessControlBlock block = this.MMS.getPcbQueue().findFirstWaitBlockByReason(WAIT_REASON.OUTPUT_WAIT);
		if (block == null) {
			return false;
		} else {
			return this.wakePro(block.getProcessID());
		}
	}

	/**
	 * @apiNote ��MMU���߼���ַת��Ϊ�����ַ
	 * @param proID
	 * @return false:ҳ��δ���У�����ȱҳ�жϣ�<br>
	 *         true:�ɹ�ת��Ϊ�����ַ������MMU�С�<br>
	 */
	public boolean mmuWork(int proID) {
		synchronized (this.MMS.getMmu()) {
			// ����ַ���ߵ�����ȡ��������MMU
			this.MMS.getMmu().inputLogicAddress(this.MMS.getAddressBus().outputAddressFromBus().getData());
		}
		// MMU��ʼ����ְ���������߼���ַתΪ�����ַ
		return this.MMS.transLogicAddressToPhysicAddress(proID);
	}

	/**
	 * �ú���ջ�����ֳ���תΪ�ں�̬
	 * 
	 * @param block Ҫ�����ֳ���Ϣ�Ľ��̵�PCB
	 */
	public void saveSiteinfoIntoKernelStack(ProcessControlBlock block) {
		int oldAdd = this.MMS.getAddressBus().getAddress().getData();// ��¼֮ǰ�ĵ�ַ������
		// ջ�ף�SP��PC��IR��PSW��AR��ջ��ָ��(Ϊ�˱�֤ѹ���SP������ջ�ף����ڻָ�ʱSP������λ�ã���ѹ��SP)
		this.pushIntoKernelStack(block, this.CPU.outputSP().getData());
		this.pushIntoKernelStack(block, this.CPU.outputPC().getData());
		this.pushIntoKernelStack(block, this.CPU.outputIR().getData());
		this.pushIntoKernelStack(block, this.CPU.outputPSW());
		this.pushIntoKernelStack(block, this.CPU.outputAR().getData());
		// ת��ϵͳ״̬
		this.CPU.getPSW().changeToKernal();
		// ��Ϊ���浽����ջ�Ĺ����У���ַ�����ϵĵ�ַ������SP�ڵĵ�ַ�����Բ���֮ǰ��ַ��������ʲô�����ָ�
		this.MMS.getAddressBus()
				.inputAddressIntoBus(new Address(oldAdd, Address.getLogicPageBitnum(), Address.getOffsetBitnum()));
	}

	/**
	 * �ú���ջ�ָ��ֳ���תΪ�û�̬
	 * 
	 * @param block �����ֳ���Ϣ�Ľ��̵�PCB
	 */
	public void recoverSiteinfoFromKernelStack(ProcessControlBlock block) {
		int oldAdd = this.MMS.getAddressBus().getAddress().getData();// ��¼֮ǰ�ĵ�ַ������
		// ȡ������ջ����
		int ar = this.popFromKernelStack(block);
		int psw = this.popFromKernelStack(block);
		int ir = this.popFromKernelStack(block);
		int pc = this.popFromKernelStack(block);
		int sp = this.popFromKernelStack(block);
		// �ָ�CPU�ֳ�
		this.CPU.assignCPU(pc, ir, psw, ar, sp);
		this.CPU.getPSW().backToUser();// �ص��û�̬�������ڻָ��ֳ����������Ը��ǵ�ʱ�ֳ�PSW�ĺ���̬��
		// ��Ϊ���浽����ջ�Ĺ����У���ַ�����ϵĵ�ַ������SP�ڵĵ�ַ�����Բ���֮ǰ��ַ��������ʲô�����ָ�
		this.MMS.getAddressBus()
				.inputAddressIntoBus(new Address(oldAdd, Address.getLogicPageBitnum(), Address.getOffsetBitnum()));
	}

	/**
	 * ����ջ��ѹջ����
	 * 
	 * @param block ��ǰҪͣ������̬�Ľ���PCB
	 * @param data  Ҫ���������
	 */
	public void pushIntoKernelStack(ProcessControlBlock block, Integer data) {
		Address logicAdd = this.CPU.outputSP();
		// ���߼���ַ�ҵ�������
		this.MMS.getAddressBus().inputAddressIntoBus(logicAdd);
		for (int i = 0; i < 2; i++) {
			// ���ݵ�ַ���ϵĵ�ַ���зô�
			if (this.mmuWork(block.getProcessID())) {
				// ֻ�����ݳɹ�ѹ��SP��ָ���λ�ò����Լ�1��ȱҳ�жϲ������Լ�һ��
				this.CPU.getSP().increase();
				Address physicAdd = this.MMS.getMmu().getPhysicAddress();
				// Ҫ������������������ߡ�
				this.MMS.getDataBus().inputDataIntoBus(data);
				// �������ߵ�����ȡ�������ڴ������ַָ��λ��
				this.MMS.writeOneWordIntoMainMem(physicAdd, this.MMS.getDataBus().outputDataFromBus());
				// ʲôʱ��ɹ�ѹ���ֳ��ͽ�������һ����Ҫִ������
				break;
			} else {
				// ֱ��ȱҳ�жϣ������ǵ�ǰ״̬
				this.MMS.doPageFault(block.getProcessID());
			}
		}
	}

	/**
	 * ����ջ�ĵ�ջ����
	 * 
	 * @param block ����������ͣ״̬�Ľ���
	 * @return ��ջ�������ֳ���Ϣ����
	 */
	public Integer popFromKernelStack(ProcessControlBlock block) {
		Address logicAdd = this.CPU.outputSP();
		if (block.getControlInfo().getKernelPoint().getData() == logicAdd.getData()) {
			return null;// ջָ��==ջ�ף�����null
		} else {
			// ���߼���ַ��һ����Ԫ�ĵ�ַ�ҵ�������
			Address dataLogicAdd = new Address(logicAdd.getData() - MemoryManageSystem.getUnitSize(),
					Address.getLogicPageBitnum(), Address.getOffsetBitnum());
			this.MMS.getAddressBus().inputAddressIntoBus(dataLogicAdd);
			// �ô�
			for (int i = 0; i < 2; i++) {
				// ���ݵ�ַ���ϵĵ�ַ���зô�
				if (this.mmuWork(block.getProcessID())) {
					// ֻ�гɹ�����SP��ָ������ݲ����Լ�1��ȱҳ�жϲ������Լ�һ��
					this.CPU.getSP().decrease();
					Address physicAdd = this.MMS.getMmu().getPhysicAddress();
					// ���������ַȡ���������������ߡ�
					this.MMS.getDataBus().inputDataIntoBus(this.MMS.readOneWordFromMainMem(physicAdd));
					// ȡ�������ߵ�����
					return this.MMS.getDataBus().outputDataFromBus();
				} else {
					// ֱ��ȱҳ�жϣ������ǵ�ǰ״̬
					this.MMS.doPageFault(block.getProcessID());
				}
			}
		}
		return null;
	}

	/**
	 * �����������Ƿ��н���
	 * 
	 * @return true ���������н���<br>
	 *         false ��������Ϊ��<br>
	 */
	public boolean checkReadyQueue() {
		if (this.MMS.getPcbQueue().getReadyPCBList().size() == 0) {
			return false;
		} else {
			return true;
		}
	}

	// ���ܣ���������
	public boolean createPro(JobControlBlock JCB) {
		this.setRecordData("���Խ���ҵ" + JCB.getJobID() + "����Ϊ�½���");
		synchronized (this.MMS) {
			if (this.MMS.getPcbQueue().haveBlank()) {
				// Ϊ���̷���Ψһ���̱�ʶ��
				int proID = this.MMS.getPcbQueue().findMaxProID() + 1;
				ProcessControlBlock block = new ProcessControlBlock(proID, -1);// �����ַ�ռ䲻��Ҫ���ڷ��䡣
				// Ϊ�µĽ���ӳ������ַ�ռ�(��Ҫ����һ��Jobռ�õĿռ���)(ע������ʱ�����߼���ַ�ռ�)
				int textPageNum = JCB.calculateJobTextPageNum();// ���Ķ�ҳ��
				int dataPageNum = JCB.calculateJobDataPageNum();// ���ݶ�ҳ��
				// Ѱ��һ���������߼��ռ�
				Integer pageID = this.MMS.getPcbQueue()
						.findFreeHeadPageNumBySize(textPageNum + dataPageNum + KERNEL_STACK_SIZE + USERBUFFER_SIZE);// ���ݶ�Ĭ��ռһҳ
				if (pageID == null) {// ˵���Ҳ������ʵĵ�ַ�ռ䣬�ܾ������µĽ���
					this.setRecordData("��ҵ" + JCB.getJobID() + "��������ʧ�ܣ��߼���ַ�ռ䲻�㣡");
					return false;
				}
				/*
				 * question ϵͳ��ô�ж���һ���߼���ַû�з����ȥ�� ϵͳ�ڿ�ʼʱ����ô������ҳ����ؽ��ڴ棿
				 * ����취��PCB����ҳ����ڴ��ַ��������ַ����δ洢������ϵͳӦ��Ĭ��ҳ����ϵͳ���и��궨���ַ
				 */
				// ���ٿս��������ߴ�=����ӳ��Ĵ�С�����ĶΣ�
				Integer exchangeStartAdd = this.MMS.getSubMemory()
						.occupyNewExchangeArea(textPageNum + dataPageNum + KERNEL_STACK_SIZE + USERBUFFER_SIZE);
				if (exchangeStartAdd == null) {
					return false;// ���������㣬�ܾ�����
				}
				block.getControlInfo().updateSubmemoryAddress(exchangeStartAdd);
				// ����ӳ����ȻĿǰ�����ڽ�������������Ҫ��¼�¿��ٵĽ������ĵ�ַ�����Ծ��ȼ�¼�ڸ��ֶ���

				// �����߼��ռ䣨��ʼ������ַ���߼�ҳ�ŵĶ�Ӧ��ϵ��
				this.MMS.getPageTable().updateSubMemoryFirst(pageID, JCB.getTextSubMemAddress(), textPageNum,
						JCB.getDataSubMemAddress(), dataPageNum, exchangeStartAdd,
						textPageNum + dataPageNum + KERNEL_STACK_SIZE + USERBUFFER_SIZE);
				this.MMS.getPageTable().insertOneProItemNumPair(proID,
						textPageNum + dataPageNum + KERNEL_STACK_SIZE + USERBUFFER_SIZE);
				// ����PCB���Ķ���ʼ��ַ
				Address textAdd = new Address(Address.getLogicPageBitnum(), Address.getOffsetBitnum(), pageID, 0);
				block.getControlInfo().updateTextAddress(textAdd.getData());
				// ����PCB���ݶ���ʼ��ַ
				textAdd.updateAddress(pageID + textPageNum, 0);
				block.getControlInfo().updateDataAddress(textAdd.getData());
				// ����PCB����ջ��ַ
				textAdd.updateAddress(pageID + textPageNum + dataPageNum, 0);
				block.getControlInfo().updateKernelAddress(textAdd.getData());
				// ����PCB�û���������ַ
				textAdd.updateAddress(pageID + textPageNum + dataPageNum + KERNEL_STACK_SIZE, 0);
				block.getControlInfo().updateBufferAddress(textAdd.getData());
				// ��ʼ��PCB�����̱�ʶ�����������ȼ�������״̬=����̬��
				block.setProcessID(proID);
				block.getControlInfo().updateManageData(PRO_STATE.READY, WAIT_REASON.UNKNOW, JCB.getProcessPriority());

				// ҳ���һ���ַ
				Address pagetableAdd = new Address(Address.getPhysicPageBitnum(), Address.getOffsetBitnum(), 0, 0);
				int pageTableAddOffset = pageID * PageTable.getTableItemsize();
				pagetableAdd.updateAddress(this.MMS.getPageTable().getPageTableAddress().getPageID(),
						pageTableAddOffset);
				block.getControlInfo().setPageTableAddress(pagetableAdd);
				// ҳ������(ҳ������=���̵����Ķ�+���ݶ�+����ջ+�û�������)
				block.getControlInfo()
						.setPageTableItemNum(textPageNum + dataPageNum + KERNEL_STACK_SIZE + USERBUFFER_SIZE);

				// ��˵�е�Ԥ��ҳ(���Ҫ�޸ĺ���ջ���û���������ҳ��������ҲҪ�޸�)
				ArrayList<Integer> preBlockIDs = this.MMS.getUserBuddyAllocator()
						.alloc_pages(KERNEL_STACK_SIZE + USERBUFFER_SIZE, block.getProcessID());
				if (preBlockIDs == null) {
					return false;
				} else if (preBlockIDs.size() != KERNEL_STACK_SIZE + USERBUFFER_SIZE) {
					this.setRecordData("��ҵ" + JCB.getJobID() + "��������ʧ�ܣ�Ԥ��ҳʧ�ܣ�");
					return false;// Ԥ��ҳʧ�ܣ���������ʧ�ܡ�
				} else {
					// ����ҳ��
					for (int i = 0; i < KERNEL_STACK_SIZE; i++) {
						this.getMMS().getPageTable().insertOneItemOfPro(
								block.getControlInfo().getKernelPoint().getPageID() + i, preBlockIDs.get(i), true, 0,
								false, exchangeStartAdd + textPageNum + dataPageNum + i, proID);
					}
					for (int i = 0; i < USERBUFFER_SIZE; i++) {
						this.getMMS().getPageTable().insertOneItemOfPro(
								block.getControlInfo().getBufferPoint().getPageID() + i,
								preBlockIDs.get(i + KERNEL_STACK_SIZE), true, 0, false,
								exchangeStartAdd + textPageNum + dataPageNum + KERNEL_STACK_SIZE + i, proID);
					}
				}
				// ָ����
				block.getControlInfo().setInstructionNum(JCB.getInstructionNum());
				// ������
				block.getControlInfo().setDataNum(JCB.getDataNum());
				// �ź���
				block.getControlInfo().updateCommunicateData(JCB.getMessageQueue());
				// CPUռ�����
				block.getControlInfo().updateCpuOccupyData(0, Clock.getTimePeice(), 0, 0);

				// �ֳ���Ϣ����
				Instruction instruction = new Instruction(block.getControlInfo().getTextPoint().getData(), 0);
				// �����ַ����ʵ��ָ�ƥ�䣬ִ�е�ʱ���ȸ���PC��ָ��
				int sitePc = block.getControlInfo().getTextPoint().getData();// ����PC�ĳ�ʼ��ַ
				int siteSp = block.getControlInfo().getKernelPoint().getData();// ����ջջ��
				block.getSiteInfo().updateData(sitePc, instruction.getData(), 0, siteSp, 0);// CPUĬ���û�̬

				// �������PCB�������������
				if (this.MMS.getPcbQueue().insertOnePCB(block)) {
					block.writePCBintoBlock(this.MMS.getMainMemory());
					this.setRecordData("����" + block.getProcessID() + "�����ɹ���");
					return true;
				} else {
					this.setRecordData("��ҵ" + JCB.getJobID() + "��������ʧ�ܣ��޿���PCB��");
					return false;
				}
			} else {
				this.setRecordData("��ҵ" + JCB.getJobID() + "��������ʧ�ܣ�PCB��������");
				return false;
			}
		}
	}

	/**
	 * @implNote 1.�ͷŽ�������<br>
	 *           2.����ռ�õ�ҳ��<br>
	 *           3.ɾ��PCB��<br>
	 * @param proID Ҫ�����Ľ��̵�ID
	 * @return false ��������ʧ��
	 * @return true �����ɹ�
	 */
	public boolean deletePro(int proID) {
		ProcessControlBlock block = this.MMS.getPcbQueue().findThePCB(proID);
		if (block == null) {
			this.setRecordData("����" + proID + "����ʧ�ܣ����޴˽��̣�");
			return false;// �Ҳ���ָ���Ľ��̣�����ʧ��
		} else {
			// �ͷŽ�����
			if (this.MMS.getSubMemory().releaseOldExchangeArea(block.getControlInfo().getProcessAddress(),
					block.getControlInfo().getPageTableItemNum())) {
				ArrayList<Integer> blockNumList = new ArrayList<Integer>();
				// Ĭ��һ�����������߼�ҳ�������ģ�������������޸�����ı������̡�
				for (int i = 0; i < block.getControlInfo().getPageTableItemNum(); i++) {
					PageTableItem item = this.MMS.getPageTable()
							.findItemByPageNum(block.getControlInfo().getTextPoint().getPageID() + i);
					if (item.isStateBit()) {
						blockNumList.add(item.getBlockID());
					}
				}
				this.MMS.getUserBuddyAllocator().free_pages(blockNumList);// ����㷨�ͷſ��п顣
				this.MMS.getPageTable().leavePageTable(block.getControlInfo().getPageTableAddress(),
						block.getControlInfo().getPageTableItemNum());// ����ҳ��
				if (this.MMS.getPageTable().deleteTheProItemNumPair(proID)) {
					if (this.monitor.P(MESSAGE.MEMORY_SOURCE_EMPTY, proID)) {// �鿴�Ƿ��ܼ���һ������Ľ��̡�
						if (!this.MMS.getPcbQueue().getPendBlockList().isEmpty()
								|| !this.MMS.getPcbQueue().getPendReadyList().isEmpty()) {
							// ֻ���ڹ�����ж���Ϊ�յ�����²Ż�����������ɣ����û�й�����̣��Ͳ��������Ӷ��ﵽÿ�ι�����Ҫ����һ���������ܼ����Ч����
							Integer activateID = this.monitor.V(MESSAGE.MEMORY_SOURCE_FULL, proID);// ����������뵽���������̵��ʸ�ͳ�������һ�����������֤��
							if (activateID != null) {
								this.activatePro(activateID);
							}
						}
					}
					//���û��P�ɹ��������������ӣ�EMPTY�ź���Ҳ���Ϊ���������ҽ��̻��ں����Ŷӣ�����Ҫ�������
					//���P�ɹ���Available����ٵ�0��Allocation�����ӵ�1���ź����ص�0����
					//���Զ���Allocation���ǲ�Ӧ����Allocation+1
					this.monitor.clearProIDWaitingMessages(proID);// ������е���Դ����
					this.setRecordData("����" + block.getProcessID() + "��������");
					return this.MMS.getPcbQueue().deleteThePCB(proID);// ����ӦPCB������ɾ��PCB
				} else {
					return false;
				}
			} else {
				this.setRecordData("����" + block.getProcessID() + "����ʧ�ܣ���潻�����ͷŴ���");
				return false;
			}
		}
	}

	/**
	 * ��������(�������ں�״̬�²��ܵ��ô˺���)
	 * 
	 * @implNote 1���ж��Ƿ��н���������<br>
	 *           2������н��̣��Ƚ��ֳ����浽PCB<br>
	 *           3����PCBд������ԭ��<br>
	 *           4��PCB״̬����Ϊ����̬<br>
	 *           5�������º��PCB��Ϣд������<br>
	 *           6��PCB��������������<br>
	 * @param reason ����ԭ��
	 * @return true �ɹ�������ǰ����<br>
	 *         false ��ǰ�޽������У��޷�����<br>
	 */
	public boolean blockPro(WAIT_REASON reason) {
		if (this.MMS.getPcbQueue().getRunningPCB().isEmpty()) {
			return false;// ��ǰ�����н��̣���������
		} else {
			ProcessControlBlock block = this.MMS.getPcbQueue().getRunningPCB().get(0);
			// �����ֳ�
			this.saveSiteInfoByPcb(block);
			// дԭ��
			block.getControlInfo().setWaitReason(reason);
			// ����Ϊ����̬
			block.getControlInfo().setProcessState(PRO_STATE.BLOCK);
			block.writePCBintoBlock(this.MMS.getMainMemory());
			if (this.MMS.getPcbQueue().runToBlock()) {
				this.recordRunTimeOnceB(block);
				this.setRecordData("����" + block.getProcessID() + "��������");
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * ���ѽ���(�������ں�״̬�²��ܵ��ô˺���)
	 * 
	 * @implNote 1�����������е�PCB�ƶ����������в������ȼ��Ŷ�<br>
	 *           2��״̬��Ϊ����̬<br>
	 *           3�������µ�PCB����д���ڴ�<br>
	 *           4��ͬ���ú����������ڹ����������������<br>
	 * @param proID ��Ҫ���ѵĽ���ID
	 * @return true �ɹ�����ָ������<br>
	 *         false ��δ�����������ҵ�ָ��ID�Ľ���<br>
	 */
	public boolean wakePro(int proID) {
		if (this.MMS.getPcbQueue().blockToReady(proID)) {
			ProcessControlBlock block = this.MMS.getPcbQueue().findReadyPCB(proID);// �����������
			block.getControlInfo().setProcessState(PRO_STATE.READY);// ��״̬
			block.writePCBintoBlock(this.MMS.getMainMemory());
			this.setRecordData("����" + block.getProcessID() + "�����ѣ�");
			return true;
		} else if (this.MMS.getPcbQueue().pendBlockToPendReady(proID)) {
			ProcessControlBlock block = this.MMS.getPcbQueue().findPendReadyPCB(proID);// ��������������
			block.getControlInfo().setProcessState(PRO_STATE.PEND_READY);
			block.writePCBintoBlock(this.MMS.getMainMemory());
			this.setRecordData("����" + block.getProcessID() + "�ӹ����������б������ѣ�");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * �������
	 * 
	 * @param proID
	 * @return
	 */
	public boolean pendPro(int proID) {
		ProcessControlBlock block = this.MMS.getPcbQueue().findThePCB(proID);
		if (block.getControlInfo().getProcessState() == PRO_STATE.BLOCK) {// ���������̬
			block.getControlInfo().setProcessState(PRO_STATE.PEND_BLOCK);// �޸Ľ���״̬
			block.writePCBintoBlock(this.MMS.getMainMemory());// �����ڴ�����
			// �����̵�פ�����ڴ��еĿ�ȫ���һؽ�������
			this.moveProToSwap(block);
			if (this.MMS.getPcbQueue().blockToPendBlock(proID)) {
				this.monitor.V(MESSAGE.MEMORY_SOURCE_EMPTY, proID);// Ϊ������������һ������������Ȩ��֤��
				this.monitor.P(MESSAGE.MEMORY_SOURCE_FULL, proID);// ͬʱ������Ľ������뼤����P���������ܱ��������㡣
				this.setRecordData("����" + block.getProcessID() + "�ӹ��𵽹����������У�");
				return true;
			} else {
				return false;
			}
		}
		if (block.getControlInfo().getProcessState() == PRO_STATE.READY) {// ����Ǿ���̬
			block.getControlInfo().setProcessState(PRO_STATE.PEND_READY);// �޸Ľ���״̬
			block.writePCBintoBlock(this.MMS.getMainMemory());// �����ڴ�����
			// �����̵�פ�����ڴ��еĿ�ȫ���һؽ�������
			this.moveProToSwap(block);
			if (this.MMS.getPcbQueue().readyToPendReady(proID)) {
				this.monitor.V(MESSAGE.MEMORY_SOURCE_EMPTY, proID);// Ϊ������������һ������������Ȩ��֤��
				this.monitor.P(MESSAGE.MEMORY_SOURCE_FULL, proID);// ͬʱ������Ľ������뼤����P���������ܱ��������㡣
				this.setRecordData("����" + block.getProcessID() + "�ӹ��𵽹���������У�");
				return true;
			} else {
				return false;
			}
		}
		return false;// �����ھ���̬��������̬���Ͳ��ܹ���
	}

	/**
	 * �������
	 * 
	 * @param proID
	 * @return
	 */
	public boolean activatePro(int proID) {
		ProcessControlBlock block2 = this.MMS.getPcbQueue().findPendReadyPCB(proID);// ȥ�������������
		if (block2 != null) {
			block2.getControlInfo().setProcessState(PRO_STATE.READY);
			block2.writePCBintoBlock(this.MMS.getMainMemory());// �����ڴ�����
			if (this.MMS.getPcbQueue().pendReadyToReady(proID)) {
				this.setRecordData("����" + block2.getProcessID() + "�ӹ���������б����");
				return true;
			} else {
				return false;
			}
		}
		ProcessControlBlock block = this.MMS.getPcbQueue().findPendBlockPCB(proID);// ȥ��������������
		if (block != null) {// �ҵ���
			block.getControlInfo().setProcessState(PRO_STATE.BLOCK);// �޸Ľ���״̬
			block.writePCBintoBlock(this.MMS.getMainMemory());// �����ڴ�����
			if (this.MMS.getPcbQueue().pendBlockToBlock(proID)) {
				this.setRecordData("����" + block.getProcessID() + "�ӹ����������б����");
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * ���н���(�������ں�״̬�²��ܵ��ô˺���)
	 * 
	 * @apiNote 1��������������̻��ѵģ�����Ҫ�ڵ��ô˺������ú���ջ�ٻָ�һ���ֳ�<br>
	 *          2��������´����Ļ�ʱ��Ƭ�ľ������ģ�����Ҫ���ô˺�������û�̬���ܿ�ʼִ��ָ��<br>
	 * @implNote 1�������������н���<br>
	 *           2���ƶ��������׽��̵�����<br>
	 *           3��״̬��Ϊ����̬<br>
	 *           4�������µ�����д������<br>
	 *           5����ҳ���ַװ��MMU<br>
	 *           6����PCB�ָ��ֳ�<br>
	 * @return true �ɹ�����һ������<br>
	 *         false ��������Ϊ�գ��޷����н���<br>
	 */
	public boolean runPro() {
		if (this.checkReadyQueue()) {
			this.MMS.getPcbQueue().readyToRun();// ����-����
			ProcessControlBlock block = this.MMS.getPcbQueue().getRunningPCB().get(0);
			block.getControlInfo().setProcessState(PRO_STATE.RUN);
			block.writePCBintoBlock(this.MMS.getMainMemory());
			synchronized (this.MMS.getMmu()) {
				// �����̵�ҳ���ַװ��
				this.MMS.getMmu().setPagetableBasicAddress(block.getControlInfo().getPageTableAddress());
				// ��տ��ȴ�������䡣
				this.MMS.getMmu().clearTLB();
			}
			// CPU�ֳ��ָ�
			this.recoverSiteInfoByPcb(block);
			// ��շ����ֶβ�������������1
			block.getControlInfo().clearAccessTime();
			this.MMS.getPcbQueue().autoIncreaseReadyBlockPCB();
			this.recordRunTimeOnceA();
			this.setRecordData("����" + block.getProcessID() + "����CPU���У�");
			return true;
		} else {// ���������޽���
			return false;
		}
	}

	/**
	 * ���ܣ� �����н��̷��ص��������ж�β
	 * 
	 * @implNote 1���������̬���޽���<br>
	 *           2����PCB�����ֳ�<br>
	 *           3��״̬��Ϊ����̬<br>
	 *           4�����ȴ�ԭ����Ϊunknown<br>
	 *           5�����µ�PCB����д������<br>
	 *           6�������е�PCB�ƶ�����������<br>
	 * @return true ���н��̳ɹ�����������ж�β<br>
	 *         false �����н��̣��޷��ؾ���̬<br>
	 */
	public boolean readyPro() {
		if (this.MMS.getPcbQueue().getRunningPCB().isEmpty()) {
			return false;// ��ǰ�����н��̣��������
		} else {
			ProcessControlBlock block = this.MMS.getPcbQueue().getRunningPCB().get(0);
			// �����ֳ�
			this.saveSiteInfoByPcb(block);
			// ����Ϊ����̬
			block.getControlInfo().setProcessState(PRO_STATE.READY);
			// ���õȴ�ԭ��Ϊ�ޣ��������иý���ʱ����Ƿ���Ҫ����ջ�ָ��ֳ�
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			block.writePCBintoBlock(this.MMS.getMainMemory());
			if (this.MMS.getPcbQueue().runToReady()) {
				this.setRecordData("����" + block.getProcessID() + "����ʱ��Ƭ���ص��������ж�β��");
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * @apiNote ��ĳ������פ�����ڴ��еĿ�ȫ���һؽ�����.(�������û����Ľ���)
	 * @param block Ҫ������Ľ���
	 * @return �Ƿ�ɹ�����
	 */
	public boolean moveProToSwap(ProcessControlBlock block) {
		ArrayList<PageTableItem> items = this.MMS.getPageTable().findTrueItems(
				block.getControlInfo().getPageTableAddress(), block.getControlInfo().getPageTableItemNum());
		if (items.size() == 0) {
			return false;
		} else {
			for (PageTableItem pageTableItem : items) {
				// ���¸����ַ
				if (this.MMS.getPageTable().updateMainToSubItemSubAdd(pageTableItem.getBlockID(), block)) {
					// �����ݷŻض�Ӧ�ĸ����ַ
					this.MMS.coverBlockFromMainToSub(pageTableItem.getBlockID(), pageTableItem.getSubmemoryAddress());
					ArrayList<Integer> blockIdList = new ArrayList<Integer>();
					blockIdList.add(pageTableItem.getBlockID());
					this.MMS.getUserBuddyAllocator().free_pages(blockIdList);
					pageTableItem.setStateBit(false);// ��Ϊ��Ч��
					// ����ӵ��ʵ��ҳ��������һ
					this.MMS.getPageTable().getProItemnumPairList().getPairOfThePro(block.getProcessID())
							.decreaseItemNum();
				} else {
					return false;// ���¸���ʧ�ܣ���ζ���޷��һؽ��������жϽ������̡�
				}
			}
			return true;
		}
	}

	/**
	 * �����ֳ�
	 * 
	 * @param target:��Ҫ���ֳ����浽��Ŀ��PCB
	 */
	public void saveSiteInfoByPcb(ProcessControlBlock target) {
		target.getSiteInfo().updateData(this.CPU.getPC().getData(), this.CPU.getIR().getInstruction().getData(),
				this.CPU.getPSW().getData(), this.CPU.getSP().getData(), this.CPU.getAR().getData());
	}

	/**
	 * �ָ��ֳ�
	 * 
	 * @param source:�ṩ�ֳ���Ϣ����ԴPCB
	 */
	public void recoverSiteInfoByPcb(ProcessControlBlock source) {
		this.CPU.getPC().updateData(source.getSiteInfo().getPcData());
		this.CPU.getIR().updateData(source.getSiteInfo().getPcData() - MemoryManageSystem.getUnitSize(),
				source.getSiteInfo().getIrData());
		this.CPU.getPSW().updateData(source.getSiteInfo().getPswData());
		this.CPU.getSP().updateData(source.getSiteInfo().getSpData());
	}

	public MemoryManageSystem getMMS() {
		return MMS;
	}

	public void setMMS(MemoryManageSystem mMS) {
		MMS = mMS;
	}

	public CentralProcessingUnit getCPU() {
		return CPU;
	}

	public void setCPU(CentralProcessingUnit cPU) {
		CPU = cPU;
	}

	public Monitor getMonitor() {
		return monitor;
	}

	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}

	public static int getKernelStack() {
		return KERNEL_STACK_SIZE;
	}

	public static int getUserbufferSize() {
		return USERBUFFER_SIZE;
	}

	public DirectMemoryAccess getDMA() {
		return DMA;
	}

	public void setDMA(DirectMemoryAccess dMA) {
		DMA = dMA;
	}

	public static int getMaxPriority() {
		return MAX_PRIORITY;
	}

	public Clock getClock() {
		return clock;
	}

	public void setClock(Clock clock) {
		this.clock = clock;
	}

	public static long getCalculateMs() {
		return CALCULATE_MS;
	}

	public static int getMaxInstructionNum() {
		return MAX_INSTRUCTION_NUM;
	}

	public static int getMaxDataNum() {
		return MAX_DATA_NUM;
	}

	public static int getMinInstructionNum() {
		return MIN_INSTRUCTION_NUM;
	}

	public static int getMinDataNum() {
		return MIN_DATA_NUM;
	}

	public static int getSpecialSystemcallNum() {
		return SPECIAL_SYSTEMCALL_NUM;
	}

	public static int getNormalSystemcallNum() {
		return NORMAL_SYSTEMCALL_NUM;
	}

	public static long getSystemcallMs() {
		return SYSTEMCALL_MS;
	}

	public static long getNormalMs() {
		return NORMAL_MS;
	}

	public String getRecordData() {
		return recordData;
	}

	public synchronized void setRecordData(String recordData) {
		synchronized (this.lastRecordData) {
			synchronized (this.recordData) {
				if (recordData != null) {// �������Ĳ���null����ʾ����Ϣ��
					if (!this.lastRecordData.equals(recordData)) {// ���������Ϣ������һ����Ϣ���ظ�
						if (!this.recordData.equals("")) {
							// ���֮ǰ����Ϣ����Ϊ�����Ϣ��ȡ����չ����Ϳ���ֱ�ӽ�����Ϣ���뵽����Ϣ��ǰ�档
							this.recordData = new String(this.recordData + "\n" + recordData);
						} else {
							// ����Ѿ����ⲿˢ�½�����ȡ����Ϣ��������չ�����Ӧ����Ϊ��һ����Ϣ���������ǰ��ӻس���
							this.recordData = new String(recordData);
						}
						// ������Ϣ��¼Ϊ����Ϣ��¼��
						this.lastRecordData = new String(recordData);
					}
				} else {
					// ��������null��ʾ��գ����ǲ������һ����Ϣ�ļ�¼�����Լ���µ�����Ϣ�ǲ��ǻ�����һ����Ϣ��
					this.recordData = new String();
				}
			}
		}
	}

	/**
	 * @apiNote �����ָ�������
	 * @param proIdList �������̵�ID
	 */
	public boolean recoverDeadLine(ArrayList<Integer> proIdList) {
		if (proIdList == null) {
			return false;
		} else if (proIdList.size() == 0) {
			return false;
		} else {
			for (int i = 0; i < proIdList.size(); i++) {
				// �ҵ�ÿһ������������ռ����Դ������һһͨ��V�ͷţ�ͬʱ���ѵȴ���Щ��Դ�ĵ�һ�����̡�
				int proid = proIdList.get(i);
				ArrayList<MESSAGE> arrayList = this.findProIDHavingMessages(proid);// ��Դ����
				for (int j = 0; j < arrayList.size(); j++) {
					this.setRecordData("��������" + proid + "�ͷ���Դ<" + arrayList.get(j).toString() + ">����");
					Integer integer = this.monitor.V(arrayList.get(j), proid);
					if (integer != null) {
						this.wakePro(integer);
						proIdList.remove(integer);
					}
				}
				this.setRecordData("�����Ѿ��ͷŽ���" + proid + "��ȫ��ռ����Դ����");
				if (proIdList.size() == 0) {
					return true;// �����Ľ����Ѿ�ȫ�����ѣ����������ָ�������
				}
				this.monitor.clearProIDWaitingMessages(proid);
				ProcessControlBlock block = this.MMS.getPcbQueue().findThePCB(proid);

				if (block != null) {
					if (block.getSiteInfo().getPcData() > block.getControlInfo().getTextPoint().getData()) {
						// �����ǹ����������ǹ������һ�ɱ�Ϊ������������һع�һ��ָ�
						block.getSiteInfo()
								.setPcData(block.getSiteInfo().getPcData() - MemoryManageSystem.getUnitSize());
						this.setRecordData("��������" + proid + "�ع�һ��ָ�����ִ�С���");
					}
					block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
					// �������ֱ�ӻ��ѣ�Ӧ�ûᵼ�¸�ָ�������
					this.wakePro(proid);
					proIdList.remove(new Integer(proid));
				}
				i = -1;
			}
			return true;
		}
	}

	/**
	 * @param proID ռ����Դ�Ľ��̱��
	 * @return ����ռ�õ��ź�������
	 */
	public ArrayList<MESSAGE> findProIDHavingMessages(int proID) {
		ArrayList<MESSAGE> arrayList = new ArrayList<MESSAGE>();
		ProcessControlBlock block = this.MMS.getPcbQueue().findThePCB(proID);
		if (block == null) {
			return arrayList;
		}
		InstructionRegister register = new InstructionRegister(
				block.getSiteInfo().getPcData() - MemoryManageSystem.getUnitSize(), block.getSiteInfo().getIrData());
		PRO_STATE state = block.getControlInfo().getProcessState();
		switch (block.getControlInfo().getWaitReason()) {
		case NEED_DEVICE:
			if (state == PRO_STATE.READY || state == PRO_STATE.PEND_READY) {
				// ӵ���ⲿ�豸��
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			break;
		case NEED_BUFFER:
			if (state == PRO_STATE.READY || state == PRO_STATE.PEND_READY) {
				// ӵ�л��������ⲿ�豸��
				arrayList.add(MESSAGE.BUFFER);
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// ӵ���ⲿ�豸��
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			break;
		case TRANS_DATA_INTPUT:
			// ӵ�л��������ⲿ�豸��
			arrayList.add(MESSAGE.BUFFER);
			arrayList.add(this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			break;
		case RELEASE_BUFFER:
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// ӵ�л��������ⲿ�豸��
				arrayList.add(MESSAGE.BUFFER);
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			if (state == PRO_STATE.READY || state == PRO_STATE.PEND_READY) {
				// ӵ���ⲿ�豸��
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			break;
		case RELEASE_DEVICE:
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// ӵ���ⲿ�豸��
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			break;
		case NEED_REGISTER:
			if (state == PRO_STATE.READY || state == PRO_STATE.PEND_READY) {
				// ������ھ�����˵�����뵽��Դ�Ĵ�����ӵ��Դ�Ĵ�����
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
			}
			break;
		case NEED_REGISTER1:
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// �����������˵��û�����뵽Ŀ�ļĴ�������ӵ��Դ�Ĵ�����
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
			}
			if (state == PRO_STATE.READY || state == PRO_STATE.PEND_READY) {
				// ������ھ�����˵�����뵽��Ŀ�ļĴ�����ӵ�������Ĵ���
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(1)));
			}
			break;
		case RELEASE_REGISTER1:
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// �����������˵��û�гɹ��ͷ�Ŀ�ļĴ�����ӵ��Դ�Ĵ�����Ŀ�ļĴ�����
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(1)));
			}
			if (state == PRO_STATE.READY || state == PRO_STATE.PEND_READY) {
				// ������ھ�����˵���ͷ���Ŀ�ļĴ�����ֻ��Դ�Ĵ���
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
			}
			break;
		case RELEASE_REGISTER:
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// �����������˵��û�гɹ��ͷ�Դ�Ĵ�����ӵ��Դ�Ĵ�����
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
			}
			break;
		case INPUT_WAIT:
			// ӵ�л��������ⲿ�豸��
			arrayList.add(MESSAGE.BUFFER);
			arrayList.add(this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			break;
		case OUTPUT_WAIT:
			// ӵ�л��������ⲿ�豸��
			arrayList.add(MESSAGE.BUFFER);
			arrayList.add(this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			break;
		case REQUEST_PAGE_ACCESSMEM:
			// ӵ��Դ�Ĵ�����
			arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
			break;
		default:
			// ���豸��
			break;
		}
		return arrayList;
	}
}
