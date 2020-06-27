package osPackage;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ThreadRefresh implements Runnable {
	private final static String RECORD_FILENAME = new String("Process.txt");
	private final static int REQUEST_LISTS_NUM = 4;
	private final static int JOB_LISTSA_NUM = 4;
	private final static int JOB_LISTSB_NUM = 2;
	private final static int READY_LISTSA_NUM = 6;
	private final static int READY_LISTSB_NUM = 4;
	private final static int BLOCK_LISTSA_NUM = 6;
	private final static int BLOCK_LISTSB_NUM = 4;
	private final static int PEND_READY_LISTSA_NUM = 4;
	private final static int PEND_READY_LISTSB_NUM = 2;
	private final static int PEND_BLOCK_LISTSA_NUM = 4;
	private final static int PEND_BLOCK_LISTSB_NUM = 2;
	/**
	 * 进程管理系统指针
	 */
	private ProcessManageSystem pmsPoint;// 进程调度系统的时钟指针
	/**
	 * 作业管理系统指针
	 */
	private JobManageSystem jmsPoint;
	/**
	 * 系统运行总时间
	 */
	private JTextField txtSysruntime;
	/**
	 * 时钟中断信号
	 */
	private JTextField txtClockMessage;
	/**
	 * DMA信息表
	 */
	private ArrayList<JTextField> dmaTextFields;
	/**
	 * MMU信息表
	 */
	private ArrayList<JTextField> mmuTextFields;
	/**
	 * TLB表格指针
	 */
	private JTable tlbTablePoint;
	/**
	 * 页表表格指针
	 */
	private JTable pgTablePoint;
	/**
	 * 未来请求序列列表
	 */
	private ArrayList<JList<Integer>> requestLists;
	/**
	 * 作业后备队列列表前半部分
	 */
	private ArrayList<JList<Integer>> jobListsA;
	/**
	 * 作业后备队列列表后半部分
	 */
	private ArrayList<JList<String>> jobListsB;
	/**
	 * 就绪队列前半部分
	 */
	private ArrayList<JList<Integer>> readyListsA;
	/**
	 * 就绪队列后半部分
	 */
	private ArrayList<JList<String>> readyListsB;
	/**
	 * 阻塞队列前半部分
	 */
	private ArrayList<JList<Integer>> blockListsA;
	/**
	 * 阻塞队列前半部分
	 */
	private ArrayList<JList<String>> blockListsB;
	/**
	 * 挂起就绪队列前半部分
	 */
	private ArrayList<JList<Integer>> pendRListsA;
	/**
	 * 挂起就绪队列后半部分
	 */
	private ArrayList<JList<String>> pendRListsB;
	/**
	 * 挂起阻塞队列前半部分
	 */
	private ArrayList<JList<Integer>> pendBListsA;
	/**
	 * 挂起阻塞队列后半部分
	 */
	private ArrayList<JList<String>> pendBListsB;
	/**
	 * 运行进程PCB信息内容
	 */
	private ArrayList<JTextField> runningTxtFields;
	/**
	 * CPU当前内容列表
	 */
	private ArrayList<JTextField> cpuTextFields;
	/**
	 * 未来请求序列内容
	 */
	private ArrayList<ArrayList<Integer>> requestDataLists;
	/**
	 * 作业后备队列专用列表内容器前半部分
	 */
	private ArrayList<ArrayList<Integer>> jobDataListsA;
	/**
	 * 作业后备队列专用列表内容器后半部分
	 */
	private ArrayList<ArrayList<String>> jobDataListsB;
	/**
	 * 就绪队列专用列表内容器前半部分
	 */
	private ArrayList<ArrayList<Integer>> readyDataListsA;
	/**
	 * 就绪队列专用列表内容器后半部分
	 */
	private ArrayList<ArrayList<String>> readyDataListsB;
	/**
	 * 阻塞队列专用列表内容器前半部分
	 */
	private ArrayList<ArrayList<Integer>> blockDataListsA;
	/**
	 * 阻塞队列专用列表内容器后半部分
	 */
	private ArrayList<ArrayList<String>> blockDataListsB;
	/**
	 * 挂起就绪队列专用列表内容器前半部分
	 */
	private ArrayList<ArrayList<Integer>> pendReadyDataListsA;
	/**
	 * 挂起就绪队列专用列表内容器后半部分
	 */
	private ArrayList<ArrayList<String>> pendReadyDataListsB;
	/**
	 * 挂起阻塞队列专用列表内容器前半部分
	 */
	private ArrayList<ArrayList<Integer>> pendBlockDataListsA;
	/**
	 * 挂起阻塞队列专用列表内容器前后半部分
	 */
	private ArrayList<ArrayList<String>> pendBlockDataListsB;
	/**
	 * 日志显示
	 */
	private JTextArea textAreaPoint;
	private ArrayList<ArrayList<JTextField>> buddyTextFields;
	/**
	 * 上一条记录，是为了和之前的记录比较，如果相同就不要输出了。
	 */
	private String lastRecordString;

	public ThreadRefresh(ProcessManageSystem pms, JobManageSystem jms, JTextField txtSysruntime,
			JTextField txtClockMessage, JTextField txtCpumessage, JTextField txtDmamessage, JTextField txtUserbufferadd,
			JTextField txtSystembufferadd, JTextField txtDataregister, JTextField txtDeviceadd,
			JList<Integer> futureList0, JList<Integer> futureList1, JList<Integer> futureList2,
			JList<Integer> futureList3, JList<Integer> jobList0, JList<Integer> jobList1, JList<Integer> jobList2,
			JList<Integer> jobList3, JList<String> jobList4, JList<String> jobList5, JList<Integer> readyList0,
			JList<Integer> readyList1, JList<Integer> readyList2, JList<Integer> readyList3, JList<Integer> readyList4,
			JList<Integer> readyList5, JList<String> readyList6, JList<String> readyList7, JList<String> readyList8,
			JList<String> readyList9, JList<Integer> blockList0, JList<Integer> blockList1, JList<Integer> blockList2,
			JList<Integer> blockList3, JList<Integer> blockList4, JList<Integer> blockList5, JList<String> blockList6,
			JList<String> blockList7, JList<String> blockList8, JList<String> blockList9, JList<Integer> pendRQlist0,
			JList<Integer> pendRQlist1, JList<Integer> pendRQlist2, JList<Integer> pendRQlist3,
			JList<String> pendRQlist4, JList<String> pendRQlist5, JList<Integer> pendBQList0,
			JList<Integer> pendBQList1, JList<Integer> pendBQList2, JList<Integer> pendBQList3,
			JList<String> pendBQList4, JList<String> pendBQList5, JTextField txtProid, JTextField txtPriority,
			JTextField txtAccesstext, JTextField txtWaitreason, JTextField txtInstructionnum, JTextField txtDatanum,
			JTextField txtTextadd, JTextField txtDataadd, JTextField txtStackadd, JTextField txtBufferadd,
			JTextField txtProTableadd, JTextField txtCputime, JTextField txtSumtime, JTextField txtPc, JTextField txtIr,
			JTextField txtPsw, JTextField txtAr, JTextField txtSp, JTextField txtLogicadd, JTextField txtTableadd,
			JTextField txtPhysicadd, JTable tlbTable, JTable pgTable, JTextArea textArea,
			ArrayList<ArrayList<JTextField>> buddyFields) {
		super();
		this.pmsPoint = pms;
		this.jmsPoint = jms;
		this.txtSysruntime = txtSysruntime;
		this.txtClockMessage = txtClockMessage;

		this.dmaTextFields = new ArrayList<JTextField>();
		this.dmaTextFields.add(txtCpumessage);
		this.dmaTextFields.add(txtDmamessage);
		this.dmaTextFields.add(txtUserbufferadd);
		this.dmaTextFields.add(txtSystembufferadd);
		this.dmaTextFields.add(txtDataregister);
		this.dmaTextFields.add(txtDeviceadd);

		this.requestLists = new ArrayList<JList<Integer>>();
		this.requestLists.add(futureList0);
		this.requestLists.add(futureList1);
		this.requestLists.add(futureList2);
		this.requestLists.add(futureList3);

		this.jobListsA = new ArrayList<JList<Integer>>();
		this.jobListsA.add(jobList0);
		this.jobListsA.add(jobList1);
		this.jobListsA.add(jobList2);
		this.jobListsA.add(jobList3);
		this.jobListsB = new ArrayList<JList<String>>();
		this.jobListsB.add(jobList4);
		this.jobListsB.add(jobList5);

		this.requestDataLists = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < REQUEST_LISTS_NUM; i++) {
			this.requestDataLists.add(new ArrayList<Integer>());
		}
		this.jobDataListsA = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < JOB_LISTSA_NUM; i++) {
			this.jobDataListsA.add(new ArrayList<Integer>());
		}
		this.jobDataListsB = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < JOB_LISTSB_NUM; i++) {
			this.jobDataListsB.add(new ArrayList<String>());
		}
		this.readyDataListsA = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < READY_LISTSA_NUM; i++) {
			this.readyDataListsA.add(new ArrayList<Integer>());
		}
		this.readyDataListsB = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < READY_LISTSB_NUM; i++) {
			this.readyDataListsB.add(new ArrayList<String>());
		}
		this.blockDataListsA = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < BLOCK_LISTSA_NUM; i++) {
			this.blockDataListsA.add(new ArrayList<Integer>());
		}
		this.blockDataListsB = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < BLOCK_LISTSB_NUM; i++) {
			this.blockDataListsB.add(new ArrayList<String>());
		}
		this.pendReadyDataListsA = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < PEND_READY_LISTSA_NUM; i++) {
			this.pendReadyDataListsA.add(new ArrayList<Integer>());
		}
		this.pendReadyDataListsB = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < PEND_READY_LISTSB_NUM; i++) {
			this.pendReadyDataListsB.add(new ArrayList<String>());
		}
		this.pendBlockDataListsA = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < PEND_BLOCK_LISTSA_NUM; i++) {
			this.pendBlockDataListsA.add(new ArrayList<Integer>());
		}
		this.pendBlockDataListsB = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < PEND_BLOCK_LISTSB_NUM; i++) {
			this.pendBlockDataListsB.add(new ArrayList<String>());
		}

		this.readyListsA = new ArrayList<JList<Integer>>();
		this.readyListsA.add(readyList0);
		this.readyListsA.add(readyList1);
		this.readyListsA.add(readyList2);
		this.readyListsA.add(readyList3);
		this.readyListsA.add(readyList4);
		this.readyListsA.add(readyList5);
		this.readyListsB = new ArrayList<JList<String>>();
		this.readyListsB.add(readyList6);
		this.readyListsB.add(readyList7);
		this.readyListsB.add(readyList8);
		this.readyListsB.add(readyList9);
		this.blockListsA = new ArrayList<JList<Integer>>();
		this.blockListsA.add(blockList0);
		this.blockListsA.add(blockList1);
		this.blockListsA.add(blockList2);
		this.blockListsA.add(blockList3);
		this.blockListsA.add(blockList4);
		this.blockListsA.add(blockList5);
		this.blockListsB = new ArrayList<JList<String>>();
		this.blockListsB.add(blockList6);
		this.blockListsB.add(blockList7);
		this.blockListsB.add(blockList8);
		this.blockListsB.add(blockList9);
		this.pendRListsA = new ArrayList<JList<Integer>>();
		this.pendRListsA.add(pendRQlist0);
		this.pendRListsA.add(pendRQlist1);
		this.pendRListsA.add(pendRQlist2);
		this.pendRListsA.add(pendRQlist3);
		this.pendRListsB = new ArrayList<JList<String>>();
		this.pendRListsB.add(pendRQlist4);
		this.pendRListsB.add(pendRQlist5);
		this.pendBListsA = new ArrayList<JList<Integer>>();
		this.pendBListsA.add(pendBQList0);
		this.pendBListsA.add(pendBQList1);
		this.pendBListsA.add(pendBQList2);
		this.pendBListsA.add(pendBQList3);
		this.pendBListsB = new ArrayList<JList<String>>();
		this.pendBListsB.add(pendBQList4);
		this.pendBListsB.add(pendBQList5);

		this.runningTxtFields = new ArrayList<JTextField>();
		this.runningTxtFields.add(txtProid);
		this.runningTxtFields.add(txtPriority);
		this.runningTxtFields.add(txtAccesstext);
		this.runningTxtFields.add(txtWaitreason);
		this.runningTxtFields.add(txtInstructionnum);
		this.runningTxtFields.add(txtDatanum);
		this.runningTxtFields.add(txtTextadd);
		this.runningTxtFields.add(txtDataadd);
		this.runningTxtFields.add(txtStackadd);
		this.runningTxtFields.add(txtBufferadd);
		this.runningTxtFields.add(txtProTableadd);
		this.runningTxtFields.add(txtCputime);
		this.runningTxtFields.add(txtSumtime);

		this.cpuTextFields = new ArrayList<JTextField>();
		this.cpuTextFields.add(txtPc);
		this.cpuTextFields.add(txtIr);
		this.cpuTextFields.add(txtPsw);
		this.cpuTextFields.add(txtAr);
		this.cpuTextFields.add(txtSp);

		this.mmuTextFields = new ArrayList<JTextField>();
		this.mmuTextFields.add(txtLogicadd);
		this.mmuTextFields.add(txtTableadd);
		this.mmuTextFields.add(txtPhysicadd);

		this.tlbTablePoint = tlbTable;
		this.pgTablePoint = pgTable;
		this.textAreaPoint = textArea;

		this.buddyTextFields = buddyFields;

		this.initRecordFile();
		this.lastRecordString = new String("！");
	}

	/**
	 * @apiNote 更新日志信息
	 */
	public void refreshRecords() {
		synchronized (this.pmsPoint) {
			if (!this.pmsPoint.getRecordData().equals("")) {
				if (!this.lastRecordString.equals(this.pmsPoint.getRecordData())) {
					this.textAreaPoint.append(this.pmsPoint.getRecordData() + "\n");
					// 将滚轮设置到底部。
					this.textAreaPoint.setCaretPosition(this.textAreaPoint.getText().length());
					this.recordIntoFile();
					this.lastRecordString = new String(this.pmsPoint.getRecordData());
					this.pmsPoint.setRecordData(null);
				}
			}
		}
	}

	/**
	 * @apiNote 创建日志文本文件。
	 */
	public void initRecordFile() {
		File file = new File(System.getProperty("user.dir") + "\\" + RECORD_FILENAME);
		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write("");
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @apiNote 将日志内容记录到文本文件中。
	 */
	public void recordIntoFile() {
		File file = new File(System.getProperty("user.dir") + "\\" + RECORD_FILENAME);
		try {
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(this.pmsPoint.getRecordData() + "\n");
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void refreshMainMemFrame() {
		synchronized (this.pmsPoint.getMMS()) {
			MemoryManageSystem system = this.pmsPoint.getMMS();
			BuddyAllocator sysAllocator = system.getSystemBuddyAllocator();
			// 先刷新系统区
			for (int i = 0; i < BuddyAllocator.getFreeareaNum(); i++) {
				int blockNumPerBit = (int) Math.pow(2, i);// 本层的一位表示几个物理块。
				for (int j = 0; j < sysAllocator.getFreeAreas().get(i).getSize(); j++) {
					if (sysAllocator.getFreeAreas().get(i).getBitMap().getBitList()[j]) {
						for (int k = 0; k < blockNumPerBit; k++) {
							this.buddyTextFields.get(i).get(j * blockNumPerBit + k).setBackground(Color.GREEN);
						}
					} else {
						for (int k = 0; k < blockNumPerBit; k++) {
							this.buddyTextFields.get(i).get(j * blockNumPerBit + k).setBackground(Color.GRAY);
						}
					}
				}
			}
			// 再刷新用户区
			BuddyAllocator userAllocator = system.getUserBuddyAllocator();
			for (int i = 0; i < BuddyAllocator.getFreeareaNum(); i++) {
				int blockNumPerBit = (int) Math.pow(2, i);// 本层的一位表示几个物理块。
				for (int j = 0; j < userAllocator.getFreeAreas().get(i).getSize(); j++) {
					int startsize = sysAllocator.getFreeAreas().get(i).getSize();// 用户区在系统区后面，所以必须知道系统区多少块。
					if (userAllocator.getFreeAreas().get(i).getBitMap().getBitList()[j]) {
						for (int k = 0; k < blockNumPerBit; k++) {
							this.buddyTextFields.get(i).get((j + startsize) * blockNumPerBit + k)
									.setBackground(Color.GREEN);
						}
					} else {
						for (int k = 0; k < blockNumPerBit; k++) {
							this.buddyTextFields.get(i).get((j + startsize) * blockNumPerBit + k)
									.setBackground(Color.GRAY);
						}
					}
				}
			}
		}
	}

	/**
	 * @apiNote 更新时钟界面
	 */
	public void refreshClockFrame() {
		this.txtSysruntime.setText(Integer.toString(this.pmsPoint.getClock().getSecondNum()));
		if (this.pmsPoint.getClock().timePiecePassed()) {
			this.txtClockMessage.setBackground(Color.RED);
			this.txtClockMessage.setText("TRUE");
		} else {
			this.txtClockMessage.setBackground(Color.GREEN);
			this.txtClockMessage.setText("FALSE");
		}
	}

	/**
	 * @apiNote 更新DMA界面
	 */
	public void refreshDmaFrame() {
		DirectMemoryAccess dmaPoint = this.pmsPoint.getDMA();

		this.dmaTextFields.get(2).setText("PageID:" + dmaPoint.getMainMemRegister().getPageID() + " Offset:"
				+ dmaPoint.getMainMemRegister().getOffset());
		this.dmaTextFields.get(3).setText("PageID:" + dmaPoint.getBufferAddress().getPageID() + " Offset:"
				+ dmaPoint.getBufferAddress().getOffset());
		this.dmaTextFields.get(4).setText(Integer.toString(dmaPoint.getWordRegister()));
		int trackID = SubBlock.takeTackIDFromSubAddress(dmaPoint.getDeviceAddRegister());
		int sectorID = SubBlock.takeSectorIDFromSubAddress(dmaPoint.getDeviceAddRegister());
		this.dmaTextFields.get(5).setText("Track:" + trackID + "  Sector:" + sectorID);

		if (dmaPoint.isRecieveInterruption()) {
			this.dmaTextFields.get(0).setBackground(Color.RED);
			this.dmaTextFields.get(0).setText("TRUE");
		} else {
			this.dmaTextFields.get(0).setBackground(Color.GREEN);
			this.dmaTextFields.get(0).setText("FALSE");
		}
		if (dmaPoint.isSendInterruption()) {
			this.dmaTextFields.get(1).setBackground(Color.RED);
			this.dmaTextFields.get(1).setText("TRUE");
		} else {
			this.dmaTextFields.get(1).setBackground(Color.GREEN);
			this.dmaTextFields.get(1).setText("FALSE");
		}
	}

	/**
	 * @apiNote 更新未来请求序列界面
	 */
	public void refreshRequestFrame() {
		for (int i = 0; i < this.requestDataLists.size(); i++) {
			this.requestDataLists.get(i).clear();
		}
		for (FutureRequest request : this.jmsPoint.getFutureRequestQueue()) {
			this.requestDataLists.get(0).add(request.getPriority());
			this.requestDataLists.get(1).add(request.getInstructionNum());
			this.requestDataLists.get(2).add(request.getDataNum());
			this.requestDataLists.get(3).add(request.getInTime());
		}
		for (int i = 0; i < this.requestLists.size(); i++) {
			Integer[] integers = new Integer[this.requestDataLists.get(i).size()];
			this.requestLists.get(i).setListData(this.requestDataLists.get(i).toArray(integers));
		}
	}

	/**
	 * @apiNote 更新作业后备队列界面
	 */
	public void refreshJobFrame() {
		for (int i = 0; i < this.jobDataListsA.size(); i++) {
			this.jobDataListsA.get(i).clear();
		}
		for (int i = 0; i < this.jobDataListsB.size(); i++) {
			this.jobDataListsB.get(i).clear();
		}
		for (JobControlBlock block : this.jmsPoint.getJobQueue()) {
			this.jobDataListsA.get(0).add(block.getJobID());
			this.jobDataListsA.get(1).add(block.getProcessPriority());
			this.jobDataListsA.get(2).add(block.getInstructionNum());
			this.jobDataListsA.get(3).add(block.getDataNum());

			int trackID = SubBlock.takeTackIDFromSubAddress(block.getTextSubMemAddress());
			int sectorID = SubBlock.takeSectorIDFromSubAddress(block.getTextSubMemAddress());
			this.jobDataListsB.get(0).add("Track:" + trackID + "  Sector:" + sectorID);
			trackID = SubBlock.takeTackIDFromSubAddress(block.getDataSubMemAddress());
			sectorID = SubBlock.takeSectorIDFromSubAddress(block.getDataSubMemAddress());
			this.jobDataListsB.get(1).add("Track:" + trackID + "  Sector:" + sectorID);
		}
		for (int i = 0; i < this.jobDataListsA.size(); i++) {
			Integer[] integers = new Integer[this.jobDataListsA.get(i).size()];
			this.jobListsA.get(i).setListData(this.jobDataListsA.get(i).toArray(integers));
		}
		for (int i = 0; i < this.jobDataListsB.size(); i++) {
			String[] strings = new String[this.jobDataListsB.get(i).size()];
			this.jobListsB.get(i).setListData(this.jobDataListsB.get(i).toArray(strings));
		}
	}

	/**
	 * @apiNote 更新CPU界面
	 */
	public void refreshCpuFrame() {
		this.cpuTextFields.get(0).setText("PageID:" + this.pmsPoint.getCPU().getPC().getAddress().getPageID()
				+ " Offset:" + this.pmsPoint.getCPU().getPC().getAddress().getOffset());
		this.cpuTextFields.get(1).setText(String.valueOf(this.pmsPoint.getCPU().getIR().getInstruction().getData()));
		this.cpuTextFields.get(2).setText(String.valueOf(this.pmsPoint.getCPU().getPSW().getData()));
		this.cpuTextFields.get(3).setText("PageID:" + this.pmsPoint.getCPU().getAR().getAddress().getPageID()
				+ " Offset:" + this.pmsPoint.getCPU().getAR().getAddress().getOffset());
		this.cpuTextFields.get(4).setText("PageID:" + this.pmsPoint.getCPU().getSP().getStackTopAddress().getPageID()
				+ " Offset:" + this.pmsPoint.getCPU().getSP().getStackTopAddress().getOffset());
	}

	public void refreshRunningPro() {
		synchronized (this.pmsPoint.getMMS().getPcbQueue().getRunningPCB()) {
			if (!this.pmsPoint.getMMS().getPcbQueue().getRunningPCB().isEmpty()) {
				ProcessControlBlock block = this.pmsPoint.getMMS().getPcbQueue().getRunningPCB().get(0);
				this.runningTxtFields.get(0).setText(String.valueOf(block.getProcessID()));
				this.runningTxtFields.get(1).setText(String.valueOf(block.getControlInfo().getProcessPriority()));
				this.runningTxtFields.get(2).setText(String.valueOf(block.getControlInfo().getAccessTime()));
				this.runningTxtFields.get(3)
						.setText(this.transWaitReasonToString((block.getControlInfo().getWaitReason())));
				this.runningTxtFields.get(4).setText(String.valueOf(block.getControlInfo().getInstructionNum()));
				this.runningTxtFields.get(5).setText(String.valueOf(block.getControlInfo().getDataNum()));
				this.runningTxtFields.get(6).setText("PageID:" + block.getControlInfo().getTextPoint().getPageID()
						+ " Offset:" + block.getControlInfo().getTextPoint().getOffset());
				this.runningTxtFields.get(7).setText("PageID:" + block.getControlInfo().getDataPoint().getPageID()
						+ " Offset:" + block.getControlInfo().getDataPoint().getOffset());
				this.runningTxtFields.get(8).setText("PageID:" + block.getControlInfo().getKernelPoint().getPageID()
						+ " Offset:" + block.getControlInfo().getKernelPoint().getOffset());
				this.runningTxtFields.get(9).setText("PageID:" + block.getControlInfo().getBufferPoint().getPageID()
						+ " Offset:" + block.getControlInfo().getBufferPoint().getOffset());
				this.runningTxtFields.get(10)
						.setText("PageID:" + block.getControlInfo().getPageTableAddress().getPageID() + " Offset:"
								+ block.getControlInfo().getPageTableAddress().getOffset());
				this.runningTxtFields.get(11).setText(String.valueOf(block.getControlInfo().getUsedCPUTime()));
				this.runningTxtFields.get(12).setText(String.valueOf(block.getControlInfo().getUsedSumTime()));
			} else {
				this.runningTxtFields.get(0).setText("");
				this.runningTxtFields.get(1).setText("");
				this.runningTxtFields.get(2).setText("");
				this.runningTxtFields.get(3).setText(this.transWaitReasonToString(WAIT_REASON.UNKNOW));
				this.runningTxtFields.get(4).setText("");
				this.runningTxtFields.get(5).setText("");
				this.runningTxtFields.get(6).setText("PageID:" + "-" + " Offset:" + "-");
				this.runningTxtFields.get(7).setText("PageID:" + "-" + " Offset:" + "-");
				this.runningTxtFields.get(8).setText("PageID:" + "-" + " Offset:" + "-");
				this.runningTxtFields.get(9).setText("PageID:" + "-" + " Offset:" + "-");
				this.runningTxtFields.get(10).setText("PageID:" + "-" + " Offset:" + "-");
				this.runningTxtFields.get(11).setText("");
				this.runningTxtFields.get(12).setText("");
			}
		}

	}

	/**
	 * @apiNote 更新就绪队列界面
	 */
	public void refreshReadyQueueFrame() {
		synchronized (pmsPoint.getMMS().getPcbQueue().getReadyPCBList()) {
			for (int i = 0; i < this.readyDataListsA.size(); i++) {
				this.readyDataListsA.get(i).clear();
			}
			for (int i = 0; i < this.readyDataListsB.size(); i++) {
				this.readyDataListsB.get(i).clear();
			}
			for (ProcessControlBlock block : this.pmsPoint.getMMS().getPcbQueue().getReadyPCBList()) {
				this.readyDataListsA.get(0).add(block.getProcessID());
				this.readyDataListsA.get(1).add(block.getControlInfo().getProcessPriority());
				this.readyDataListsA.get(2).add(block.getControlInfo().getAccessTime());
				this.readyDataListsA.get(3).add((int) block.getControlInfo().getUsedSumTime());
				this.readyDataListsA.get(4).add(block.getControlInfo().getInstructionNum());
				this.readyDataListsA.get(5).add(block.getControlInfo().getDataNum());

				this.readyDataListsB.get(0).add("PageID:" + block.getControlInfo().getTextPoint().getPageID()
						+ " Offset:" + block.getControlInfo().getTextPoint().getOffset());
				this.readyDataListsB.get(1).add("PageID:" + block.getControlInfo().getDataPoint().getPageID()
						+ " Offset:" + block.getControlInfo().getDataPoint().getOffset());
				this.readyDataListsB.get(2).add("PageID:" + block.getControlInfo().getKernelPoint().getPageID()
						+ " Offset:" + block.getControlInfo().getKernelPoint().getOffset());
				this.readyDataListsB.get(3).add(this.transWaitReasonToString(block.getControlInfo().getWaitReason()));
			}
		}
		for (int i = 0; i < this.readyDataListsA.size(); i++) {
			Integer[] integers = new Integer[this.readyDataListsA.get(i).size()];
			this.readyListsA.get(i).setListData(this.readyDataListsA.get(i).toArray(integers));
		}
		for (int i = 0; i < this.readyDataListsB.size(); i++) {
			String[] strings = new String[this.readyDataListsB.get(i).size()];
			this.readyListsB.get(i).setListData(this.readyDataListsB.get(i).toArray(strings));
		}
	}

	/**
	 * @apiNote 更新阻塞队列界面
	 */
	public void refreshBlockQueueFrame() {
		synchronized (pmsPoint.getMMS().getPcbQueue().getBlockPCBList()) {
			for (int i = 0; i < this.blockDataListsA.size(); i++) {
				this.blockDataListsA.get(i).clear();
			}
			for (int i = 0; i < this.blockDataListsB.size(); i++) {
				this.blockDataListsB.get(i).clear();
			}
			for (ProcessControlBlock block : this.pmsPoint.getMMS().getPcbQueue().getBlockPCBList()) {
				this.blockDataListsA.get(0).add(block.getProcessID());
				this.blockDataListsA.get(1).add(block.getControlInfo().getProcessPriority());
				this.blockDataListsA.get(2).add(block.getControlInfo().getAccessTime());
				this.blockDataListsA.get(3).add((int) block.getControlInfo().getUsedSumTime());
				this.blockDataListsA.get(4).add(block.getControlInfo().getInstructionNum());
				this.blockDataListsA.get(5).add(block.getControlInfo().getDataNum());

				this.blockDataListsB.get(0).add("PageID:" + block.getControlInfo().getTextPoint().getPageID()
						+ " Offset:" + block.getControlInfo().getTextPoint().getOffset());
				this.blockDataListsB.get(1).add("PageID:" + block.getControlInfo().getDataPoint().getPageID()
						+ " Offset:" + block.getControlInfo().getDataPoint().getOffset());
				this.blockDataListsB.get(2).add("PageID:" + block.getControlInfo().getKernelPoint().getPageID()
						+ " Offset:" + block.getControlInfo().getKernelPoint().getOffset());
				this.blockDataListsB.get(3).add(this.transWaitReasonToString(block.getControlInfo().getWaitReason()));
			}
		}
		for (int i = 0; i < this.blockDataListsA.size(); i++) {
			Integer[] integers = new Integer[this.blockDataListsA.get(i).size()];
			this.blockListsA.get(i).setListData(this.blockDataListsA.get(i).toArray(integers));
		}
		for (int i = 0; i < this.blockDataListsB.size(); i++) {
			String[] strings = new String[this.blockDataListsB.get(i).size()];
			this.blockListsB.get(i).setListData(this.blockDataListsB.get(i).toArray(strings));
		}
	}

	/**
	 * @apiNote 更新挂起就绪队列
	 */
	public void refreshPendReadyQueueFrame() {
		synchronized (pmsPoint.getMMS().getPcbQueue().getPendReadyList()) {
			for (int i = 0; i < this.pendReadyDataListsA.size(); i++) {
				this.pendReadyDataListsA.get(i).clear();
			}
			for (int i = 0; i < this.pendReadyDataListsB.size(); i++) {
				this.pendReadyDataListsB.get(i).clear();
			}
			for (ProcessControlBlock block : this.pmsPoint.getMMS().getPcbQueue().getPendReadyList()) {
				this.pendReadyDataListsA.get(0).add(block.getProcessID());
				this.pendReadyDataListsA.get(1).add(block.getControlInfo().getProcessPriority());
				this.pendReadyDataListsA.get(2).add(block.getControlInfo().getInstructionNum());
				this.pendReadyDataListsA.get(3).add(block.getControlInfo().getDataNum());
				this.pendReadyDataListsB.get(0)
						.add(this.transWaitReasonToString(block.getControlInfo().getWaitReason()));
				int trackID = SubBlock.takeTackIDFromSubAddress(block.getControlInfo().getProcessAddress());
				int sectorID = SubBlock.takeSectorIDFromSubAddress(block.getControlInfo().getProcessAddress());
				this.pendReadyDataListsB.get(1).add("Track:" + trackID + "  Sector:" + sectorID);
			}
			for (int i = 0; i < this.pendReadyDataListsA.size(); i++) {
				Integer[] integers = new Integer[this.pendReadyDataListsA.get(i).size()];
				this.pendRListsA.get(i).setListData(this.pendReadyDataListsA.get(i).toArray(integers));
			}
			for (int i = 0; i < this.pendReadyDataListsB.size(); i++) {
				String[] strings = new String[this.pendReadyDataListsB.get(i).size()];
				this.pendRListsB.get(i).setListData(this.pendReadyDataListsB.get(i).toArray(strings));
			}
		}
	}

	/**
	 * @apiNote 更新挂起阻塞队列
	 */
	public void refreshPendBlockQueueFrame() {
		synchronized (pmsPoint.getMMS().getPcbQueue().getPendBlockList()) {
			for (int i = 0; i < this.pendBlockDataListsA.size(); i++) {
				this.pendBlockDataListsA.get(i).clear();
			}
			for (int i = 0; i < this.pendBlockDataListsB.size(); i++) {
				this.pendBlockDataListsB.get(i).clear();
			}
			for (ProcessControlBlock block : this.pmsPoint.getMMS().getPcbQueue().getPendBlockList()) {
				this.pendBlockDataListsA.get(0).add(block.getProcessID());
				this.pendBlockDataListsA.get(1).add(block.getControlInfo().getProcessPriority());
				this.pendBlockDataListsA.get(2).add(block.getControlInfo().getInstructionNum());
				this.pendBlockDataListsA.get(3).add(block.getControlInfo().getDataNum());
				this.pendBlockDataListsB.get(0)
						.add(this.transWaitReasonToString(block.getControlInfo().getWaitReason()));
				int trackID = SubBlock.takeTackIDFromSubAddress(block.getControlInfo().getProcessAddress());
				int sectorID = SubBlock.takeSectorIDFromSubAddress(block.getControlInfo().getProcessAddress());
				this.pendBlockDataListsB.get(1).add("Track:" + trackID + "  Sector:" + sectorID);
			}
			for (int i = 0; i < this.pendBlockDataListsA.size(); i++) {
				Integer[] integers = new Integer[this.pendBlockDataListsA.get(i).size()];
				this.pendBListsA.get(i).setListData(this.pendBlockDataListsA.get(i).toArray(integers));
			}
			for (int i = 0; i < this.pendBlockDataListsB.size(); i++) {
				String[] strings = new String[this.pendBlockDataListsB.get(i).size()];
				this.pendBListsB.get(i).setListData(this.pendBlockDataListsB.get(i).toArray(strings));
			}
		}
	}

	/**
	 * @apiNote 更新MMU&TLB
	 */
	public void refreshMmuFrame() {
		synchronized (this.pmsPoint.getMMS().getMmu()) {
			MemoryManagementUnit mmuPoint = this.pmsPoint.getMMS().getMmu();
			this.mmuTextFields.get(0).setText("PageID:" + mmuPoint.getLogicAddress().getPageID() + " Offset:"
					+ mmuPoint.getLogicAddress().getOffset());
			this.mmuTextFields.get(1).setText("PageID:" + mmuPoint.getPagetableBasicAddress().getPageID() + " Offset:"
					+ mmuPoint.getPagetableBasicAddress().getOffset());
			this.mmuTextFields.get(2).setText("PageID:" + mmuPoint.getPhysicAddress().getPageID() + " Offset:"
					+ mmuPoint.getPhysicAddress().getOffset());
			TransLookBuffer buffer = mmuPoint.getTLB();
			for (int i = 0; i < TransLookBuffer.getTlbSize(); i++) {
				if (i < buffer.getTlbItemQueue().size()) {
					PageTableItem item = buffer.getTlbItemQueue().get(i);
					this.tlbTablePoint.getModel().setValueAt(String.valueOf(item.getPageID()), i, 0);
					this.tlbTablePoint.getModel().setValueAt(String.valueOf(item.getBlockID()), i, 1);
					this.tlbTablePoint.getModel().setValueAt(String.valueOf(item.isStateBit()), i, 2);
					this.tlbTablePoint.getModel().setValueAt(String.valueOf(item.getAccessText()), i, 3);
					this.tlbTablePoint.getModel().setValueAt(String.valueOf(item.isAlterBit()), i, 4);
					this.tlbTablePoint.getModel().setValueAt(String.valueOf(item.getSubmemoryAddress()), i, 5);
				} else {
					for (int j = 0; j < 6; j++) {
						this.tlbTablePoint.getModel().setValueAt("", i, j);
					}
				}
			}
		}
	}

	public void refreshPgTableFrame() {
		synchronized (this.pmsPoint.getMMS().getPageTable()) {
			PageTable table = this.pmsPoint.getMMS().getPageTable();
			for (int i = 0; i < PageTable.getTableLength(); i++) {
				PageTableItem item = table.getTableItems().get(i);
				this.pgTablePoint.getModel().setValueAt(String.valueOf(item.getPageID()), i, 0);
				this.pgTablePoint.getModel().setValueAt(String.valueOf(item.getBlockID()), i, 1);
				this.pgTablePoint.getModel().setValueAt(String.valueOf(item.isStateBit()), i, 2);
				this.pgTablePoint.getModel().setValueAt(String.valueOf(item.getAccessText()), i, 3);
				this.pgTablePoint.getModel().setValueAt(String.valueOf(item.isAlterBit()), i, 4);
				this.pgTablePoint.getModel().setValueAt(String.valueOf(item.getSubmemoryAddress()), i, 5);
			}
		}
	}

	/**
	 * @param reason 等待原因
	 * @return 翻译好的等待原因中文版
	 */
	public String transWaitReasonToString(WAIT_REASON reason) {
		switch (reason) {
		case UNKNOW:
			return "无阻塞原因";
		case INPUT_WAIT:
			return "等待DMA输入";
		case OUTPUT_WAIT:
			return "等待DMA传出";
		case NEED_REGISTER:
			return "需要源寄存器";
		case NEED_REGISTER1:
			return "需要目的寄存器";
		case RELEASE_REGISTER:
			return "释放源寄存器";
		case RELEASE_REGISTER1:
			return "释放目的寄存器";
		case NEED_BUFFER:
			return "需要缓冲区";
		case RELEASE_BUFFER:
			return "释放缓冲区";
		case REQUEST_PAGE_ACCESSMEM:
			return "取数缺页中断";
		case REQUEST_PAGE_INSTRUCTION:
			return "取指缺页中断";
		case NEED_DEVICE:
			return "请求外部设备";
		case RELEASE_DEVICE:
			return "释放外部设备";
		case NEED_NORMAL_SYSTEM_EXE:
			return "系统调用处理";
		case TRANS_DATA_INTPUT:
			return "读系统缓冲区";
		default:
			return "无阻塞原因";
		}
	}

	public void refreshFrame() {
		this.refreshClockFrame();
		this.refreshDmaFrame();
		this.refreshRequestFrame();
		this.refreshJobFrame();
		this.refreshCpuFrame();
		this.refreshReadyQueueFrame();
		this.refreshBlockQueueFrame();
		this.refreshRunningPro();
		this.refreshMmuFrame();
		this.refreshPgTableFrame();
		this.refreshMainMemFrame();
		this.refreshPendReadyQueueFrame();
		this.refreshPendBlockQueueFrame();
		this.refreshRecords();
	}

	@Override
	public void run() {
		while (true) {
			this.refreshFrame();
		}
	}

}
