package osPackage;

import java.util.ArrayList;

public class ProcessControlBlock {
	private final static int PCB_SIZE = 64;// PCB的尺寸（字数）实际上目前只实际使用了20个字
	/**
	 * 该PCB信息在内存中的首地址
	 */
	private Address physicAdd;

	private int processID;// 进程标识
	private SiteInfo siteInfo;// 现场信息
	private ControlInfo controlInfo;// 控制信息

	public ProcessControlBlock(int proID, int physicAdd) {
		super();
		if (physicAdd == -1) {
			this.physicAdd = new Address(0, Address.getPhysicPageBitnum(), Address.getOffsetBitnum());
		} else {
			this.physicAdd = new Address(physicAdd, Address.getPhysicPageBitnum(), Address.getOffsetBitnum());
		}
		this.processID = proID;
		this.siteInfo = new SiteInfo();
		this.controlInfo = new ControlInfo();
	}

	// 功能：进程更新数据
	public void updateProcessControlBlock(int processID, Address physicAddress, int pcData, int irData, int pswData,
			int spData, int arData, PRO_STATE processState, WAIT_REASON waitReason, int processPriority,
			ArrayList<MESSAGE> messages, int accessTime, long remainTimePiece, long usedCPUTime, long usedSumTime,
			int processAddress, int pageTableAddress, int textAddress, int dataAddress, int kernelAddress,
			int bufferAddress, int instructionNum, int dataNum, int pageTableItemNum) {
		this.processID = processID;

		this.physicAdd.updateAddress(physicAddress.getData());

		this.siteInfo.updateData(pcData, irData, pswData, spData, arData);

		this.controlInfo.updateManageData(processState, waitReason, processPriority);
		this.controlInfo.updateCommunicateData(messages);
		this.controlInfo.updateCpuOccupyData(accessTime, remainTimePiece, usedCPUTime, usedSumTime);
		this.controlInfo.updatePageTableAddress(pageTableAddress);
		this.controlInfo.updateTextDataKernelAddress(textAddress, dataAddress, kernelAddress, bufferAddress);
		this.controlInfo.updateSubmemoryAddress(processAddress);
		this.controlInfo.setInstructionNum(instructionNum);
		this.controlInfo.setPageTableItemNum(pageTableItemNum);
	}

	// 功能：该PCB拷贝一个传来的PCB的所有参数
	public void copyThePCB(ProcessControlBlock that) {
		this.updateProcessControlBlock(that.getProcessID(), that.getPhysicAdd(), that.getSiteInfo().getPcData(),
				that.getSiteInfo().getIrData(), that.getSiteInfo().getPswData(), that.getSiteInfo().getSpData(),
				that.getSiteInfo().getArData(), that.getControlInfo().getProcessState(),
				that.getControlInfo().getWaitReason(), that.getControlInfo().getProcessPriority(),
				that.getControlInfo().getMessages(), that.getControlInfo().getAccessTime(),
				that.getControlInfo().getRemainTimePiece(), that.getControlInfo().getUsedCPUTime(),
				that.getControlInfo().getUsedSumTime(), that.getControlInfo().getProcessAddress(),
				that.getControlInfo().getPageTableAddress().getData(), that.getControlInfo().getTextPoint().getData(),
				that.getControlInfo().getDataPoint().getData(), that.getControlInfo().getKernelPoint().getData(),
				that.getControlInfo().getBufferPoint().getData(), that.getControlInfo().getInstructionNum(),
				that.getControlInfo().getDataNum(), that.getControlInfo().getPageTableItemNum());
	}

	// 功能：求该进程的第一个页表项的下标
	public Integer findPagetableItemIndex() {
		int i = this.controlInfo.getPageTableAddress().getOffset() / PageTable.getTableItemsize();
		return i;
	}

	/**
	 * 将PCB中的全部数据写入主存
	 * 
	 * @param mainMemory
	 */
	public void writePCBintoBlock(MainMemory mainMemory) {
		ArrayList<Integer> datas = new ArrayList<Integer>();

		// 将进程号送入
		Integer tmp1 = new Integer(this.processID);
		datas.add(tmp1);
		// 将控制信息送入
		int tmp2;
		switch (this.controlInfo.getProcessState()) {
		case UNKNOW:
			tmp2 = 0;
			break;
		case READY:
			tmp2 = 1;
			break;
		case RUN:
			tmp2 = 2;
			break;
		case BLOCK:
			tmp2 = 3;
			break;
		case PEND_READY:
			tmp2 = 4;
			break;
		case PEND_BLOCK:
			tmp2 = 5;
			break;
		default:
			tmp2 = 0;
			break;
		}
		datas.add(new Integer(tmp2));
		int tmp3;
		switch (this.controlInfo.getWaitReason()) {
		case UNKNOW:
			tmp3 = 0;
			break;
		case INPUT_WAIT:
			tmp3 = 1;
			break;
		case OUTPUT_WAIT:
			tmp3 = 2;
			break;
		case NEED_REGISTER:
			tmp3 = 3;
			break;
		case NEED_BUFFER:
			tmp3 = 4;
			break;
		default:
			tmp3 = 0;
			break;
		}
		datas.add(new Integer(tmp3));
		datas.add(new Integer(this.controlInfo.getProcessPriority()));
		datas.add(new Integer(this.controlInfo.getPageTableAddress().getData()));
		datas.add(new Integer(this.controlInfo.getTextPoint().getData()));
		datas.add(new Integer(this.controlInfo.getDataPoint().getData()));
		datas.add(new Integer(this.controlInfo.getKernelPoint().getData()));
		datas.add(new Integer(this.controlInfo.getBufferPoint().getData()));
		datas.add(new Integer(this.controlInfo.getInstructionNum()));
		datas.add(new Integer(this.controlInfo.getPageTableItemNum()));
		datas.add(new Integer(this.controlInfo.getProcessAddress()));
		datas.add(new Integer(this.controlInfo.getAccessTime()));
		datas.add(new Integer((int) this.controlInfo.getRemainTimePiece()));
		datas.add(new Integer((int) this.controlInfo.getUsedCPUTime()));

		// 将控制信息送入
		datas.add(new Integer(this.siteInfo.getPcData()));
		datas.add(new Integer(this.siteInfo.getIrData()));
		datas.add(new Integer(this.siteInfo.getPswData()));
		datas.add(new Integer(this.siteInfo.getSpData()));
		datas.add(new Integer(this.siteInfo.getArData()));

		// 写入主存
		Address address = new Address(this.physicAdd.getData(), Address.getPhysicPageBitnum(),
				Address.getOffsetBitnum());
		for (Integer integer : datas) {
			mainMemory.writeWordIntoMemory(address, integer);
			address.updateAddress(address.getData() + MemoryManageSystem.getUnitSize());
		}
	}

	public int getProcessID() {
		return processID;
	}

	public void setProcessID(int processID) {
		this.processID = processID;
	}

	public SiteInfo getSiteInfo() {
		return siteInfo;
	}

	public void setSiteInfo(SiteInfo siteInfo) {
		this.siteInfo = siteInfo;
	}

	public ControlInfo getControlInfo() {
		return controlInfo;
	}

	public void setControlInfo(ControlInfo controlInfo) {
		this.controlInfo = controlInfo;
	}

	public Address getPhysicAdd() {
		return physicAdd;
	}

	public void setPhysicAdd(Address physicAdd) {
		this.physicAdd = physicAdd;
	}

	public static int getPcbSize() {
		return PCB_SIZE;
	}

}
