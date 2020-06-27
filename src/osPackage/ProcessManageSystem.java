package osPackage;

import java.util.ArrayList;

public class ProcessManageSystem {
	/**
	 * 进程管理信息提供给记录文件的内容链表。
	 * 
	 * @implNote 为了记录进程管理全过程，不得不在各种细节位置中安插这种信息的提交过程。
	 */
	private String recordData = new String();
	/**
	 * 记录上一次加入的记录是哪一句
	 */
	private String lastRecordData = new String();
	/**
	 * 记录一个进程进入CPU时的秒
	 */
	private Integer intoCpuTime = new Integer(0);

	private final static int INTERRUPTION_NUM = 3;// 两种中断服务程序：DMA，时钟
	private final static int SYSTEM_INSTRUCTION_NUM = 128;// 一个特权程序所占的字数，为了让特权指令集凑足6页
	private final static int SPECIAL_SYSTEMCALL_NUM = 5;// 前5种系统调用是特殊指令专用。
	private final static int NORMAL_SYSTEMCALL_NUM = 4;// 总共有9种系统调用（前5种属于其他特殊指令，后4种属于系统调用指令）
	private final static int MAX_PRIORITY = 10;// 优先级最大为10
	private final static int MAX_INSTRUCTION_NUM = Block.getBlockSize() * 3 + 50;// 最大指令数目
	private final static int MIN_INSTRUCTION_NUM = Block.getBlockSize() * 3 + 30;// 最小指令数目
	private final static int MAX_DATA_NUM = Block.getBlockSize() + 50;// 最大数据量
	private final static int MIN_DATA_NUM = Block.getBlockSize() + 30;// 最小数据量

	private final static int KERNEL_STACK_SIZE = 1;// 核心栈默认占一页
	private final static int USERBUFFER_SIZE = 1;// 每个进程拥有的缓冲区页数
	private final static long NORMAL_MS = 800;// 普通指令假设耗时800毫秒
	private final static long CALCULATE_MS = 500;// 计算行为假设耗时500毫秒
	private final static long SYSTEMCALL_MS = 500;// 普通系统调用行为假设耗时500毫秒

	private Clock clock;// 时钟
	private MemoryManageSystem MMS;// 存储管理系统
	private CentralProcessingUnit CPU;// 需要操作的除了存储器还有CPU
	private DirectMemoryAccess DMA;// DMA硬件模拟，用于IO指令
	private Monitor monitor;// 资源管理器（用于进程的同步互斥）

	public ProcessManageSystem() {
		super();
		this.clock = new Clock();
		this.MMS = new MemoryManageSystem();
		this.CPU = new CentralProcessingUnit(0, 0, 0, 0);
		this.DMA = new DirectMemoryAccess(new Address(Address.getPhysicPageBitnum(), Address.getOffsetBitnum(),
				MemoryManageSystem.getBufferPagenum(), 0).getData(), this);
		this.monitor = new Monitor();
		this.setRecordData("引导程序启动……");
		this.setRecordData("开辟系统缓冲区……");
		this.MMS.getSystemBuddyAllocator().alloc_pages(1, 0);// 进程ID设为0表示系统引导程序申请缓冲区。
		this.setRecordData("开辟页表所占内存");
		this.MMS.getSystemBuddyAllocator().alloc_pages(1, 0);// 引导程序申请页表空间。
		this.setRecordData("开辟PCB池……");
		this.MMS.getSystemBuddyAllocator()
				.alloc_pages(PCBQueue.getSize() * ProcessControlBlock.getPcbSize() / Block.getBlockSize(), 0);// 申请PCB池
		this.setRecordData("初始化系统指令集……");
		ArrayList<Integer> blockIds = new ArrayList<Integer>();
		for (int i = 0; i < (INTERRUPTION_NUM + SPECIAL_SYSTEMCALL_NUM + NORMAL_SYSTEMCALL_NUM) * SYSTEM_INSTRUCTION_NUM
				/ (Block.getBlockSize() * 2); i++) {
			blockIds.addAll(this.MMS.getSystemBuddyAllocator().alloc_pages(2, 0));// 一次申请两页会快一点
		}
		this.writeSysProcessesIntoBlocks(blockIds);
		this.setRecordData("系统初始化完毕，一切就绪，可以启动！");
	}

	/**
	 * @param sysInsId 特权程序编号<br>
	 *                 0.P操作<br>
	 *                 1.V操作<br>
	 *                 2.缺页异常处理程序<br>
	 *                 3.DMA赋值程序<br>
	 *                 4.Input善后程序<br>
	 *                 5~8其他系统调用程序<br>
	 *                 9.DMA中断处理程序<br>
	 *                 10.时钟中断处理程序<br>
	 *                 11.死锁检测中断处理程序<br>
	 * @return 产生的16位指令集
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
	 * @param blockIds 开辟的指令集使用的块号。
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
	 * @param block 因转态而被阻塞的进程
	 * @return 从指令中提取的信号量
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
	 * @param registerID 来自指令的寄存器ID。
	 * @return 返回寄存器信号量。
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
	 * @param deviceID 外设ID。
	 * @return 返回外设信号量。
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
	 * 根据系统调用号，执行对应的系统调用指令
	 * 
	 * @apiNote 0:P操作<br>
	 *          1:V操作<br>
	 *          2:缺页异常处理程序<br>
	 *          3:DMA激发程序<br>
	 * @param callID  系统调用号
	 * @param message P操作所申请的信号或V操作所释放的信号
	 * @param block   因为系统调用而被阻塞的进程PCB
	 */
	public void systemCall(int callID, ProcessControlBlock block) {
		// 根据阻塞原因设置PV信号
		MESSAGE message = this.analyseMessage(block);
		switch (callID) {
		case 0:
			// P操作（阻塞原因：需要**）
			this.setRecordData("系统调用::::" + "系统为进程" + block.getProcessID() + "申请资源<" + message.toString() + ">");
			if (this.monitor.P(message, block.getProcessID())) {
				// P操作成功，唤醒阻塞进程
				this.setRecordData("系统调用::::" + "为进程" + block.getProcessID() + "申请资源<" + message.toString() + ">"
						+ "成功！！！(唤醒进程" + block.getProcessID() + ")");
				this.wakePro(block.getProcessID());
			}
			break;
		case 1:
			// V操作（阻塞原因：释放**）
			this.setRecordData("系统调用::::" + "系统为进程" + block.getProcessID() + "释放资源<" + message.toString() + ">");
			Integer proID = this.monitor.V(message, block.getProcessID());
			this.setRecordData("系统调用::::" + "为进程" + block.getProcessID() + "释放资源<" + message.toString() + ">"
					+ "成功！！！(唤醒进程" + block.getProcessID() + ")");
			if (proID != null) {
				// V操作成功，唤醒相应进程。
				this.setRecordData("系统调用::::" + "系统为进程" + block.getProcessID() + "释放资源,导致等待资源进程" + proID + "被唤醒！");
				this.wakePro(proID);
			}
			this.wakePro(block.getProcessID());
			break;
		case 2:
			// 缺页异常处理程序（阻塞原因：请求页面）
			// 在执行缺页异常前先检查一下是否需要挂起
			// 中级调度：检测是否需要挂起进程
			if (this.MMS.getPageTable().needPending()) {
				this.pendPro(this.MMS.getPcbQueue().findMaxProID());
			}
			this.setRecordData("系统调用::::" + "为进程" + block.getProcessID() + "执行缺页异常处理程序。");
			this.MMS.doPageFault(block.getProcessID());
			// 执行后将进程唤醒
			this.setRecordData(
					"系统调用::::" + "为进程" + block.getProcessID() + "执行缺页异常处理程序成功！！！(唤醒进程" + block.getProcessID() + ")");
			this.wakePro(block.getProcessID());
			break;
		case 3:
			// DMA触发程序（阻塞原因：等待输入/出完成）
			// 1、给DMA赋值，DMA做好准备(默认取一页数据)
			if (block.getControlInfo().getWaitReason() == WAIT_REASON.INPUT_WAIT) {
				this.setRecordData("系统调用::::" + "系统为进程" + block.getProcessID() + "启动DMA用于输入。");
				this.DMA.inputParam(false, this.CPU.getIR().getDeviceAddress(),
						block.getControlInfo().getBufferPoint().getData(), Block.getBlockSize());
			}
			if (block.getControlInfo().getWaitReason() == WAIT_REASON.OUTPUT_WAIT) {
				this.setRecordData("系统调用::::" + "系统为进程" + block.getProcessID() + "将数据搬移到系统缓冲区并启动DMA用于输出。");
				this.DMA.inputParam(true, this.CPU.getIR().getDeviceAddress(),
						block.getControlInfo().getBufferPoint().getData(), Block.getBlockSize());
				this.MMS.getAddressBus().inputAddressIntoBus(this.DMA.getMainMemRegister());
				// 需要通过MMU找到进程缓冲区的物理地址
				for (int i = 0; i < 2; i++) {
					// 根据地址线上的地址进行访存
					if (this.mmuWork(block.getProcessID())) {
						Address physicAdd = this.MMS.getMmu().getPhysicAddress();
						// 将系统缓冲区内的数据搬运到用户缓冲区。
						this.MMS.coverBlockFromMainAToMainB(physicAdd.getPageID(),
								this.DMA.getBufferAddress().getPageID());
						// 什么时候成功压入现场就结束，不一定非要执行两次
						break;
					} else {
						// 直接缺页中断，不考虑当前状态
						this.MMS.doPageFault(block.getProcessID());
					}
				}
			}
			// 2、向DMA发送激活信号，激活DMA
			this.DMA.setRecieveInterruption(true);
			// 启动DMA会导致该进程一直阻塞，直至DMA完成工作，所以不需要唤醒。
			break;
		case 4:
			// Input善后程序：将系统缓冲区的数据搬运到用户缓冲区
			if (block.getControlInfo().getWaitReason() == WAIT_REASON.TRANS_DATA_INTPUT) {
				this.setRecordData("系统调用::::" + "系统为进程" + block.getProcessID() + "将系统缓冲区数据搬移到进程缓冲区。");
				// 需要通过MMU找到进程缓冲区的物理地址
				this.MMS.getAddressBus().inputAddressIntoBus(this.DMA.getMainMemRegister());
				for (int i = 0; i < 2; i++) {
					// 根据地址线上的地址进行访存
					if (this.mmuWork(block.getProcessID())) {
						Address physicAdd = this.MMS.getMmu().getPhysicAddress();
						// 将系统缓冲区内的数据搬运到用户缓冲区。
						this.MMS.coverBlockFromMainAToMainB(this.DMA.getBufferAddress().getPageID(),
								physicAdd.getPageID());
						// 什么时候成功压入现场就结束，不一定非要执行两次
						break;
					} else {
						// 直接缺页中断，不考虑当前状态
						this.MMS.doPageFault(block.getProcessID());
					}
				}
			}
			this.setRecordData("系统调用::::" + "系统为进程" + block.getProcessID() + "将系统缓冲区数据搬移到进程缓冲区成功！！！(唤醒进程"
					+ block.getProcessID() + ")");
			this.wakePro(block.getProcessID());
			break;
		default:// 系统调用指令设计具体操作。
			try {
				this.setRecordData("系统调用::::" + "系统为进程" + block.getProcessID() + "执行其他系统程序。");
				Thread.sleep(SYSTEMCALL_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.wakePro(block.getProcessID());
			break;
		}
	}

	/**
	 * 执行一次函数
	 * 
	 * @apiNote 核心函数，进程调度线程的死循环体
	 * @implNote 1、检查当前是否有运行的PCB：<br>
	 *           (1)如果没有去检查就绪队列并运行新进程<br>
	 *           (2)运行成功判断属于哪种就绪进程<br>
	 *           (3)阻塞进程需要用核心栈再恢复一次现场，否则直接切换为用户态。<br>
	 *           2、如果当前有运行的进程:<br>
	 *           (1)取新指令<br>
	 *           (2)执行当前指令<br>
	 *           (3)检查中断信号<br>
	 *           为了每次执行指令所使用的机会都是相等的，而不会因为是第一条指令就多花时间，<br>
	 *           所以如果没有进程执行就专门单独执行一次运行进程操作。<br>
	 */
	public void executeOnce() {
		if (this.MMS.getPcbQueue().getRunningPCB().isEmpty()) {// 说明当前无进程运行
			// CPU的工作：就算当前没有进程运行，也要检测发来的中断信号。
			this.interruption(null);
			// 中级调度：检测是否需要挂起进程
			if (this.MMS.getPageTable().needPending()) {
				this.pendPro(this.MMS.getPcbQueue().findMaxProID());
			}
			// 开始检测就绪队列以运行新的进程。
			this.CPU.getPSW().changeToKernal();
			if (this.runPro()) {
				// 说明就绪队列有进程，且成功运行
				ProcessControlBlock runningblock = this.MMS.getPcbQueue().getRunningPCB().get(0);
				if (runningblock.getControlInfo().getWaitReason() != WAIT_REASON.UNKNOW) {
					// 如果不是新创建的进程或因为时间片耗尽就绪的进程，说明来自阻塞队列，应该用核心栈恢复
					this.recoverSiteinfoFromKernelStack(runningblock);
				} else {
					// 如果是新建进程或超时进程，创建后恢复用户态
					this.CPU.getPSW().backToUser();
				}
			}
		} else {// 当前有进程运行
			ProcessControlBlock block = this.MMS.getPcbQueue().getRunningPCB().get(0);
			if (this.isExeFinnished(block)) {// 指令集执行完成，
				this.CPU.getPSW().changeToKernal();// 切换为内核态
				this.deletePro(block.getProcessID());// 撤销执行结束的进程

				this.interruption(null);
			} else {// 指令集没有执行完成
				if (block.getControlInfo().getWaitReason() == WAIT_REASON.UNKNOW
						|| block.getControlInfo().getWaitReason() == WAIT_REASON.REQUEST_PAGE_INSTRUCTION) {
					// 上一条指令已经执行完了，或者是取指令过程中的缺页中断。
					// 取指令，如果缺页没取成，就阻塞，执行缺页中断。当下一次轮到该进程再次执行该进程就可以取得指令了
					this.readInstruction(block);
				}
				if (!this.MMS.getPcbQueue().getRunningPCB().isEmpty()) {// 如果运行的进程因为取指失败而阻塞就不能继续执行。
					// 执行该指令如果取指令失败，就会运行下一个进程，如果所有进程都缺页，就会运行最开始阻塞现在早已唤醒的那一个。
					this.exeOneInstruction(block);
					// 检查DMA的中断信号，如果有中断信号，暂时冻结当前进程，处理中断，处理完成后继续执行原来的进程，唤醒的进程优先级调至最高，下一个就执行它。
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
	 * 程序段是否执行完成？
	 * 
	 * @param block
	 * @return true
	 *         PC当前存储的是最后一条指令地址的后1个位置，等价于IR取得的指令是最后一条有效指令，并且指令已经执行完了（阻塞原因为未知）。<br>
	 *         false PC当前指向的地址还有指令<br>
	 */
	public boolean isExeFinnished(ProcessControlBlock block) {
		return (block.getControlInfo().getTextPoint().distanceTo(this.CPU.getPC().getAddress()) == block
				.getControlInfo().getInstructionNum())
				&& (block.getControlInfo().getWaitReason() == WAIT_REASON.UNKNOW);
	}

	/**
	 * 功能：执行一条指令（调动所有的硬件达到指令的执行）
	 * 
	 * @param block 要运行这条指令的进程的PCB
	 */
	public void exeOneInstruction(ProcessControlBlock block) {
		switch (this.CPU.getIR().getInstructionID()) {
		case ACCESS_MEMORY:
			this.setRecordData("进程" + block.getProcessID() + "——执行访存指令。" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeAccessMemory(block);
			break;
		case INPUT:
			this.setRecordData("进程" + block.getProcessID() + "——执行输入指令。" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeInput(block);
			break;
		case OUTPUT:
			this.setRecordData("进程" + block.getProcessID() + "——执行输出指令。" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeOutput(block);
			break;
		case SYSTEM_CALL:
			this.setRecordData("进程" + block.getProcessID() + "——执行系统调用指令。" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeSystemCall(block);
			break;
		case CALCULATE:
			this.setRecordData("进程" + block.getProcessID() + "——执行计算指令。" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeCalculate(block);
			break;
		case NORMAL:
			this.setRecordData("进程" + block.getProcessID() + "——执行普通指令。" + "<PageID:"
					+ this.CPU.getIR().getInstruction().getAddress().getPageID() + " Offset:"
					+ this.CPU.getIR().getInstruction().getAddress().getOffset() + ">");
			this.exeNormal(block);
			break;
		case JUMP:
			this.setRecordData("进程" + block.getProcessID() + "——执行跳转指令。" + "<PageID:"
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
	 * @apiNote 跳转指令执行
	 * @param block 要运行这条指令的进程的PCB
	 * @implNote 1、给PC加IR中的偏移值，执行完成执行下一条。<br>
	 */
	public void exeJump(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			if (this.CPU.getIR().getJumpOffset() != (Block.getBlockSize() - 1) * MemoryManageSystem.getUnitSize()) {
				break;// 调试bug，如果指令乱写，跳转跳得就会不对。
			}
			this.CPU.getPC().updateData(this.CPU.getPC().getData() + this.CPU.getIR().getJumpOffset());
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("进程" + block.getProcessID() + "——执行跳转指令结束！！");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote 普通系统调用函数执行
	 * @param block 要运行这条指令的进程的PCB
	 * @implNote 1、保存现场根据CPU的IR寄存器提供的系统调用号进行系统调用<br>
	 *           2、执行完成，阻塞原因清空，执行下一条<br>
	 */
	public void exeSystemCall(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			// 说明没有阻塞过，且当前是访存指令，应该从头开始执行
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_NORMAL_SYSTEM_EXE);
			// 系统调用号为0，做P操作
			this.systemCall(this.CPU.getIR().getCallID(), block);
			break;
		case NEED_NORMAL_SYSTEM_EXE:
			// 说明系统指令集执行完成，指令执行结束，将阻塞原因设为unknown，应该取下一条指令了。
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("进程" + block.getProcessID() + "——执行系统调用指令结束！！");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote 访存指令函数执行
	 * @param block 要运行这条指令的进程的PCB
	 * @implNote 1、争夺寄存器<br>
	 *           2、访存<br>
	 *           3、释放寄存器<br>
	 *           4、查中断信号<br>
	 *           5、找下一条指令<br>
	 */
	public void exeAccessMemory(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			// 说明没有阻塞过，且当前是访存指令，应该从头开始执行
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_REGISTER);
			// 系统调用号为0，做P操作
			this.systemCall(0, block);
			break;
		case NEED_REGISTER:
			// 说明该进程已经获得寄存器，且是访存指令，应该开始访存
			// 1.访问主存（将来自PCB的数据段页号和来自IR的偏移写入AR寄存器，将AR的数据读入地址总线，通过MMU访存，可能引发缺页中断）
			this.CPU.getAR().updateData((block.getControlInfo().getDataPoint().getPageID() << Address.getOffsetBitnum())
					+ this.CPU.getIR().getAccessOffset());// AR寄存器赋值
			this.MMS.getAddressBus().inputAddressIntoBus(this.CPU.outputAR());// 送入地址总线
			// 2.根据地址线上的地址进行访存
			if (this.mmuWork(block.getProcessID())) {// 如果MMU成功转换出物理地址，就应该立即取数并释放寄存器
				Address physicAdd = this.MMS.getMmu().getPhysicAddress();
				// 根据物理地址取出数据送入数据线。
				this.MMS.getDataBus().inputDataIntoBus(this.MMS.readOneWordFromMainMem(physicAdd));
				// 释放寄存器
				this.saveSiteinfoIntoKernelStack(block);
				this.blockPro(WAIT_REASON.RELEASE_REGISTER);
				// 系统调用号为1，做V操作
				this.systemCall(1, block);
			} else {// 如果MMU工作失败，说明缺页，应该执行缺页中断
				this.saveSiteinfoIntoKernelStack(block);
				this.blockPro(WAIT_REASON.REQUEST_PAGE_ACCESSMEM);
				// 系统调用号为2， 做缺页异常处理程序。
				this.systemCall(2, block);
			}
			break;
		case REQUEST_PAGE_ACCESSMEM:// 这一步实际上是上一步的重复，有待优化
			// 说明该进程缺页中断已经处理完成，再次让MMU转换地址
			this.MMS.getAddressBus().inputAddressIntoBus(this.CPU.outputAR());// 送入地址总线
			this.mmuWork(block.getProcessID());
			Address physicAdd = this.MMS.getMmu().getPhysicAddress();
			// 根据物理地址取出数据送入数据线。
			this.MMS.getDataBus().inputDataIntoBus(this.MMS.readOneWordFromMainMem(physicAdd));
			// 释放寄存器
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_REGISTER);
			// 系统调用号为1，做V操作
			this.systemCall(1, block);
			break;
		case RELEASE_REGISTER:
			// 说明释放寄存器成功，该指令执行完成。将阻塞原因设为unknown，应该取下一条指令了。
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("进程" + block.getProcessID() + "——执行访存指令结束！！");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote 输入函数执行
	 * @param block 要运行这条指令的进程的PCB
	 * @implNote 1、申请外设<br>
	 *           2、申请系统缓冲区<br>
	 *           3、阻塞进程，触发DMA<br>
	 *           4、释放缓冲区<br>
	 *           5、释放外设<br>
	 */
	public void exeInput(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			// 说明没有阻塞过，且当前是访存指令，应该从头开始执行
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_DEVICE);
			// 系统调用号为0，做P操作
			this.systemCall(0, block);
			break;
		case NEED_DEVICE:
			// 说明该进程已获得设备
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_BUFFER);
			// 系统调用号为0，做P操作
			this.systemCall(0, block);
			break;
		case NEED_BUFFER:
			// 说明该进程已获得缓冲区
			// 阻塞当前进程
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.INPUT_WAIT);
			// 系统调用号为3，让DMA工作
			this.systemCall(3, block);
			break;
		case INPUT_WAIT:
			// 说明DMA输入成功，自己也被唤醒。
			// 阻塞当前进程
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.TRANS_DATA_INTPUT);
			// 系统调用搬运数据到进程缓冲区
			this.systemCall(4, block);
			break;
		case TRANS_DATA_INTPUT:
			// 释放缓冲区
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_BUFFER);
			// 系统调用号为1，做V操作
			this.systemCall(1, block);
			break;
		case RELEASE_BUFFER:
			// 说明释放缓冲区完成，释放设备
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_DEVICE);
			this.systemCall(1, block);
			break;
		case RELEASE_DEVICE:
			// 说明设备释放完成，指令执行结束，将阻塞原因设为unknown，应该取下一条指令了。
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("进程" + block.getProcessID() + "——执行输入指令结束！！");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote 输出函数执行
	 * @param block 要运行这条指令的进程的PCB
	 * @implNote 1、申请外设<br>
	 *           2、申请系统缓冲区<br>
	 *           3、阻塞进程，触发DMA<br>
	 *           4、释放缓冲区<br>
	 *           5、释放外设<br>
	 */
	public void exeOutput(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			// 说明没有阻塞过，且当前是访存指令，应该从头开始执行
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_DEVICE);
			// 系统调用号为0，做P操作
			this.systemCall(0, block);
			break;
		case NEED_DEVICE:
			// 说明该进程已获得设备
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_BUFFER);
			// 系统调用号为0，做P操作
			this.systemCall(0, block);
			break;
		case NEED_BUFFER:
			// 说明该进程已获得缓冲区
			// 阻塞当前进程
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.OUTPUT_WAIT);
			// 系统调用号为3，让DMA工作
			this.systemCall(3, block);
			break;
		case OUTPUT_WAIT:
			// 说明DMA输出成功，自己也被唤醒。
			// 释放缓冲区
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_BUFFER);
			// 系统调用号为1，做V操作
			this.systemCall(1, block);
			break;
		case RELEASE_BUFFER:
			// 说明释放缓冲区完成，释放设备
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_DEVICE);
			this.systemCall(1, block);
			break;
		case RELEASE_DEVICE:
			// 说明设备释放完成，指令执行结束，将阻塞原因设为unknown，应该取下一条指令了。
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("进程" + block.getProcessID() + "——执行输出指令结束！！");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote 计算指令执行
	 * @param block 要运行这条指令的进程的PCB
	 */
	public void exeCalculate(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			// 说明没有阻塞过，且当前是访存指令，应该从头开始执行
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_REGISTER);
			// 系统调用号为0，做P操作
			this.systemCall(0, block);
			break;
		case NEED_REGISTER:
			// 说明没有阻塞过，且当前是访存指令，应该从头开始执行
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.NEED_REGISTER1);
			// 系统调用号为0，做P操作
			this.systemCall(0, block);
			break;
		case NEED_REGISTER1:
			// 说明当前的进程拥有两个寄存器，执行一条假的计算操作
			try {
				Thread.sleep(CALCULATE_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 释放寄存器
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_REGISTER1);
			// 系统调用号为1，做V操作
			this.systemCall(1, block);
			break;
		case RELEASE_REGISTER1:
			// 说明释放第一个寄存器完成
			// 释放另一个寄存器
			this.saveSiteinfoIntoKernelStack(block);
			this.blockPro(WAIT_REASON.RELEASE_REGISTER);
			// 系统调用号为1，做V操作
			this.systemCall(1, block);
			break;
		case RELEASE_REGISTER:
			// 说明释放令该寄存器都完成，指令执行完成。将阻塞原因设为unknown，应该取下一条指令了。
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("进程" + block.getProcessID() + "——执行计算指令结束！！");
			break;
		default:
			break;
		}
	}

	/**
	 * @apiNote 执行普通指令
	 * @param block 要运行这条指令的进程的PCB
	 */
	public void exeNormal(ProcessControlBlock block) {
		switch (block.getControlInfo().getWaitReason()) {
		case UNKNOW:
			try {
				Thread.sleep(NORMAL_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 将阻塞原因设为unknown，应该取下一条指令了。
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			this.setRecordData("进程" + block.getProcessID() + "——执行普通指令结束！！");
			break;
		default:
			break;
		}
	}

	/**
	 * 按照PC的指向，读出一条指令送入IR寄存器，并将PC自加一
	 * 
	 * @apiNote 前半段取指函数
	 * @param block 正在运行的进程PCB
	 */
	public void readInstruction(ProcessControlBlock block) {
		this.MMS.getAddressBus().inputAddressIntoBus(this.CPU.outputPC());
		// 根据地址线上的地址进行访存
		if (this.mmuWork(block.getProcessID())) {
			this.CPU.getPC().autoIncrease();// 只有成功取出PC所指向的指令才能自加1，缺页中断不允许自加一。
			Address physicAdd = this.MMS.getMmu().getPhysicAddress();
			// 根据物理地址取出指令送入数据线。
			this.MMS.getDataBus().inputDataIntoBus(this.MMS.readOneWordFromMainMem(physicAdd));
			// 将数据线的数据取出输入IR寄存器
			this.CPU.dataBusToIR(this.MMS.getDataBus().outputDataFromBus());
			// 将阻塞原因改为未知，使得下一条指令可以从头运行
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
		} else {
			this.saveSiteinfoIntoKernelStack(block);
			// 单独设立等待原因，这个阻塞原因就不会被以为是访存指令访存行为成功
			this.blockPro(WAIT_REASON.REQUEST_PAGE_INSTRUCTION);
			// 系统调用号为2， 做缺页异常处理程序。
			this.systemCall(2, block);
		}
	}

	/**
	 * 中断处理
	 * 
	 * @apiNote 目前只负责IO中断的处理（DMA发来信号的处理）和时钟中断（时钟发来的时间片到信息）
	 * @param block 当前正在运行的进程PCB
	 * @implNote 1、检查DMA是否发来信息<br>
	 *           （1）核心栈保存现场<br>
	 *           （2）根据输入还是输出执行中断处理程序<br>
	 *           （3）处理完成，核心栈恢复现场<br>
	 *           2、检查时钟是否发来信息<br>
	 *           （1）转为核心态<br>
	 *           （2）当前PCB移到就绪队尾<br>
	 *           （3）转回用户态<br>
	 */
	public void interruption(ProcessControlBlock block) {
		// DMA中断处理程序
		if (block != null) {
			if (this.DMA.isSendInterruption()) {
				this.DMA.setSendInterruption(false);// 先将传来的中断信号清空
				// 保存现场
				this.saveSiteinfoIntoKernelStack(block);
				if (this.DMA.isDirectionMessage()) {
					this.interruptHandlerForOutput();
				} else {
					this.interruptHandlerForInput();
				}
				this.recoverSiteinfoFromKernelStack(block);
			}
		} else {// 当前无进程直接转核心态处理，处理完返回用户态。
			if (this.DMA.isSendInterruption()) {
				this.DMA.setSendInterruption(false);// 先将传来的中断信号清空
				// 保存现场
				this.CPU.getPSW().changeToKernal();
				if (this.DMA.isDirectionMessage()) {
					this.interruptHandlerForOutput();
				} else {
					this.interruptHandlerForInput();
				}
				this.CPU.getPSW().backToUser();
			}
		}
		// 时钟中断处理程序
		if (this.clock.timePiecePassed()) {
			this.setRecordData("时钟中断发生：时间片已过！" + this.clock.getSecondNum() + "s");
			// 以取时钟信号，所以清空。
			this.clock.setTimepeicePassed(false);
			this.CPU.getPSW().changeToKernal();
			this.readyPro();
			this.CPU.getPSW().backToUser();
		}
		if (this.clock.checkDeadLockTimePassed()) {
			this.setRecordData("死锁检测时间到！" + this.clock.getSecondNum() + "s");
			this.clock.setDeadlocktimePassed(false);
			this.CPU.getPSW().changeToKernal();
			ArrayList<Integer> arrayList = this.monitor.checkDeadLock();
			if (arrayList.size() == 0) {
				this.setRecordData("并未出现死锁。");
			} else {
				String deadRecord = new String("死锁！！！进程:");
				for (int i = 0; i < arrayList.size(); i++) {
					deadRecord += "(" + arrayList.get(i) + ")";
				}
				deadRecord += new String("处于死锁状态，启动死锁处理程序……");
				this.setRecordData(deadRecord);
				this.recoverDeadLine(arrayList);
				this.setRecordData("……死锁处理结束，剥夺了死锁进程的资源并回滚一条指令。");
			}
			this.CPU.getPSW().backToUser();
		}
	}

	/**
	 * Input中断处理程序
	 * 
	 * @apiNote 用于DMA的输入完成后发出的中断信号处理
	 * @implNote 首先将系统缓冲区数据搬运到用户缓冲区<br>
	 *           唤醒因为Input指令阻塞的最靠近队首的进程<br>
	 * @return 处理程序处理结果
	 */
	public boolean interruptHandlerForInput() {
		ProcessControlBlock block = this.MMS.getPcbQueue().findFirstWaitBlockByReason(WAIT_REASON.INPUT_WAIT);
		if (block != null) {
			// 不要将优先级调到最大，只需要等进程返回CPU去继续执行就可以了。
//			// 将要唤醒的进程优先级调为最大。
//			block.getControlInfo().setProcessPriority(MAX_PRIORITY);
			this.wakePro(block.getProcessID());// 唤醒因为等待缓冲区的进程
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Output中断处理程序
	 * 
	 * @apiNote 用于DMA的输出完成后发出的中断信号处理
	 * @implNote 唤醒因为Output指令阻塞的最靠近队首的进程<br>
	 * @return 处理程序处理结果
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
	 * @apiNote 让MMU将逻辑地址转换为物理地址
	 * @param proID
	 * @return false:页表未命中，触发缺页中断；<br>
	 *         true:成功转换为物理地址，存在MMU中。<br>
	 */
	public boolean mmuWork(int proID) {
		synchronized (this.MMS.getMmu()) {
			// 将地址总线的数据取下来送入MMU
			this.MMS.getMmu().inputLogicAddress(this.MMS.getAddressBus().outputAddressFromBus().getData());
		}
		// MMU开始做本职工作，将逻辑地址转为物理地址
		return this.MMS.transLogicAddressToPhysicAddress(proID);
	}

	/**
	 * 用核心栈保存现场并转为内核态
	 * 
	 * @param block 要保存现场信息的进程的PCB
	 */
	public void saveSiteinfoIntoKernelStack(ProcessControlBlock block) {
		int oldAdd = this.MMS.getAddressBus().getAddress().getData();// 记录之前的地址线数据
		// 栈底：SP、PC、IR、PSW、AR、栈顶指针(为了保证压入的SP数据是栈底，便于恢复时SP在其他位置，先压入SP)
		this.pushIntoKernelStack(block, this.CPU.outputSP().getData());
		this.pushIntoKernelStack(block, this.CPU.outputPC().getData());
		this.pushIntoKernelStack(block, this.CPU.outputIR().getData());
		this.pushIntoKernelStack(block, this.CPU.outputPSW());
		this.pushIntoKernelStack(block, this.CPU.outputAR().getData());
		// 转换系统状态
		this.CPU.getPSW().changeToKernal();
		// 因为保存到核心栈的过程中，地址总线上的地址换成了SP内的地址，所以不管之前地址总线上是什么，都恢复
		this.MMS.getAddressBus()
				.inputAddressIntoBus(new Address(oldAdd, Address.getLogicPageBitnum(), Address.getOffsetBitnum()));
	}

	/**
	 * 用核心栈恢复现场并转为用户态
	 * 
	 * @param block 保存现场信息的进程的PCB
	 */
	public void recoverSiteinfoFromKernelStack(ProcessControlBlock block) {
		int oldAdd = this.MMS.getAddressBus().getAddress().getData();// 记录之前的地址线数据
		// 取出核心栈数据
		int ar = this.popFromKernelStack(block);
		int psw = this.popFromKernelStack(block);
		int ir = this.popFromKernelStack(block);
		int pc = this.popFromKernelStack(block);
		int sp = this.popFromKernelStack(block);
		// 恢复CPU现场
		this.CPU.assignCPU(pc, ir, psw, ar, sp);
		this.CPU.getPSW().backToUser();// 回到用户态（必须在恢复现场后，这样可以覆盖当时现场PSW的核心态）
		// 因为保存到核心栈的过程中，地址总线上的地址换成了SP内的地址，所以不管之前地址总线上是什么，都恢复
		this.MMS.getAddressBus()
				.inputAddressIntoBus(new Address(oldAdd, Address.getLogicPageBitnum(), Address.getOffsetBitnum()));
	}

	/**
	 * 核心栈的压栈功能
	 * 
	 * @param block 当前要停在运行态的进程PCB
	 * @param data  要保存的数据
	 */
	public void pushIntoKernelStack(ProcessControlBlock block, Integer data) {
		Address logicAdd = this.CPU.outputSP();
		// 将逻辑地址挂到总线上
		this.MMS.getAddressBus().inputAddressIntoBus(logicAdd);
		for (int i = 0; i < 2; i++) {
			// 根据地址线上的地址进行访存
			if (this.mmuWork(block.getProcessID())) {
				// 只有数据成功压入SP所指向的位置才能自加1，缺页中断不允许自加一。
				this.CPU.getSP().increase();
				Address physicAdd = this.MMS.getMmu().getPhysicAddress();
				// 要保存的数据送入数据线。
				this.MMS.getDataBus().inputDataIntoBus(data);
				// 将数据线的数据取出送入内存物理地址指定位置
				this.MMS.writeOneWordIntoMainMem(physicAdd, this.MMS.getDataBus().outputDataFromBus());
				// 什么时候成功压入现场就结束，不一定非要执行两次
				break;
			} else {
				// 直接缺页中断，不考虑当前状态
				this.MMS.doPageFault(block.getProcessID());
			}
		}
	}

	/**
	 * 核心栈的弹栈功能
	 * 
	 * @param block 结束运行暂停状态的进程
	 * @return 弹栈弹出的现场信息数据
	 */
	public Integer popFromKernelStack(ProcessControlBlock block) {
		Address logicAdd = this.CPU.outputSP();
		if (block.getControlInfo().getKernelPoint().getData() == logicAdd.getData()) {
			return null;// 栈指针==栈底，返回null
		} else {
			// 将逻辑地址减一个单元的地址挂到总线上
			Address dataLogicAdd = new Address(logicAdd.getData() - MemoryManageSystem.getUnitSize(),
					Address.getLogicPageBitnum(), Address.getOffsetBitnum());
			this.MMS.getAddressBus().inputAddressIntoBus(dataLogicAdd);
			// 访存
			for (int i = 0; i < 2; i++) {
				// 根据地址线上的地址进行访存
				if (this.mmuWork(block.getProcessID())) {
					// 只有成功弹出SP所指向的数据才能自减1，缺页中断不允许自减一。
					this.CPU.getSP().decrease();
					Address physicAdd = this.MMS.getMmu().getPhysicAddress();
					// 根据物理地址取出数据送入数据线。
					this.MMS.getDataBus().inputDataIntoBus(this.MMS.readOneWordFromMainMem(physicAdd));
					// 取出数据线的数据
					return this.MMS.getDataBus().outputDataFromBus();
				} else {
					// 直接缺页中断，不考虑当前状态
					this.MMS.doPageFault(block.getProcessID());
				}
			}
		}
		return null;
	}

	/**
	 * 检查就绪队列是否有进程
	 * 
	 * @return true 就绪队列有进程<br>
	 *         false 就绪队列为空<br>
	 */
	public boolean checkReadyQueue() {
		if (this.MMS.getPcbQueue().getReadyPCBList().size() == 0) {
			return false;
		} else {
			return true;
		}
	}

	// 功能：创建进程
	public boolean createPro(JobControlBlock JCB) {
		this.setRecordData("尝试将作业" + JCB.getJobID() + "创建为新进程");
		synchronized (this.MMS) {
			if (this.MMS.getPcbQueue().haveBlank()) {
				// 为进程分配唯一进程标识符
				int proID = this.MMS.getPcbQueue().findMaxProID() + 1;
				ProcessControlBlock block = new ProcessControlBlock(proID, -1);// 物理地址空间不需要现在分配。
				// 为新的进程映像分配地址空间(需要计算一个Job占用的空间多大)(注意这里时分配逻辑地址空间)
				int textPageNum = JCB.calculateJobTextPageNum();// 正文段页数
				int dataPageNum = JCB.calculateJobDataPageNum();// 数据段页数
				// 寻找一块连续的逻辑空间
				Integer pageID = this.MMS.getPcbQueue()
						.findFreeHeadPageNumBySize(textPageNum + dataPageNum + KERNEL_STACK_SIZE + USERBUFFER_SIZE);// 数据段默认占一页
				if (pageID == null) {// 说明找不到合适的地址空间，拒绝加入新的进程
					this.setRecordData("作业" + JCB.getJobID() + "创建进程失败：逻辑地址空间不足！");
					return false;
				}
				/*
				 * question 系统怎么判断哪一段逻辑地址没有分配出去？ 系统在开始时，怎么讲外存的页表加载进内存？
				 * 解决办法是PCB保存页表的内存地址而非外存地址，其次存储器管理系统应该默认页表在系统区有个宏定义基址
				 */
				// 开辟空交换区（尺寸=进程映像的大小：正文段）
				Integer exchangeStartAdd = this.MMS.getSubMemory()
						.occupyNewExchangeArea(textPageNum + dataPageNum + KERNEL_STACK_SIZE + USERBUFFER_SIZE);
				if (exchangeStartAdd == null) {
					return false;// 缓冲区不足，拒绝创建
				}
				block.getControlInfo().updateSubmemoryAddress(exchangeStartAdd);
				// 进程映像虽然目前还不在交换区，但是需要记录新开辟的交换区的地址，所以就先记录在该字段中

				// 开辟逻辑空间（初始化外存地址与逻辑页号的对应关系）
				this.MMS.getPageTable().updateSubMemoryFirst(pageID, JCB.getTextSubMemAddress(), textPageNum,
						JCB.getDataSubMemAddress(), dataPageNum, exchangeStartAdd,
						textPageNum + dataPageNum + KERNEL_STACK_SIZE + USERBUFFER_SIZE);
				this.MMS.getPageTable().insertOneProItemNumPair(proID,
						textPageNum + dataPageNum + KERNEL_STACK_SIZE + USERBUFFER_SIZE);
				// 设置PCB正文段起始地址
				Address textAdd = new Address(Address.getLogicPageBitnum(), Address.getOffsetBitnum(), pageID, 0);
				block.getControlInfo().updateTextAddress(textAdd.getData());
				// 设置PCB数据段起始地址
				textAdd.updateAddress(pageID + textPageNum, 0);
				block.getControlInfo().updateDataAddress(textAdd.getData());
				// 设置PCB核心栈地址
				textAdd.updateAddress(pageID + textPageNum + dataPageNum, 0);
				block.getControlInfo().updateKernelAddress(textAdd.getData());
				// 设置PCB用户缓冲区地址
				textAdd.updateAddress(pageID + textPageNum + dataPageNum + KERNEL_STACK_SIZE, 0);
				block.getControlInfo().updateBufferAddress(textAdd.getData());
				// 初始化PCB（进程标识符、进程优先级、进程状态=就绪态）
				block.setProcessID(proID);
				block.getControlInfo().updateManageData(PRO_STATE.READY, WAIT_REASON.UNKNOW, JCB.getProcessPriority());

				// 页表第一项地址
				Address pagetableAdd = new Address(Address.getPhysicPageBitnum(), Address.getOffsetBitnum(), 0, 0);
				int pageTableAddOffset = pageID * PageTable.getTableItemsize();
				pagetableAdd.updateAddress(this.MMS.getPageTable().getPageTableAddress().getPageID(),
						pageTableAddOffset);
				block.getControlInfo().setPageTableAddress(pagetableAdd);
				// 页表项数(页表项数=进程的正文段+数据段+核心栈+用户缓冲区)
				block.getControlInfo()
						.setPageTableItemNum(textPageNum + dataPageNum + KERNEL_STACK_SIZE + USERBUFFER_SIZE);

				// 传说中的预调页(如果要修改核心栈和用户缓冲区的页数，这里也要修改)
				ArrayList<Integer> preBlockIDs = this.MMS.getUserBuddyAllocator()
						.alloc_pages(KERNEL_STACK_SIZE + USERBUFFER_SIZE, block.getProcessID());
				if (preBlockIDs == null) {
					return false;
				} else if (preBlockIDs.size() != KERNEL_STACK_SIZE + USERBUFFER_SIZE) {
					this.setRecordData("作业" + JCB.getJobID() + "创建进程失败：预调页失败！");
					return false;// 预调页失败，创建进程失败。
				} else {
					// 更新页表
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
				// 指令数
				block.getControlInfo().setInstructionNum(JCB.getInstructionNum());
				// 数据量
				block.getControlInfo().setDataNum(JCB.getDataNum());
				// 信号量
				block.getControlInfo().updateCommunicateData(JCB.getMessageQueue());
				// CPU占用情况
				block.getControlInfo().updateCpuOccupyData(0, Clock.getTimePeice(), 0, 0);

				// 现场信息设置
				Instruction instruction = new Instruction(block.getControlInfo().getTextPoint().getData(), 0);
				// 这个地址和真实的指令不匹配，执行的时候先根据PC找指令
				int sitePc = block.getControlInfo().getTextPoint().getData();// 这是PC的初始地址
				int siteSp = block.getControlInfo().getKernelPoint().getData();// 核心栈栈底
				block.getSiteInfo().updateData(sitePc, instruction.getData(), 0, siteSp, 0);// CPU默认用户态

				// 申请空闲PCB并移入就绪队列
				if (this.MMS.getPcbQueue().insertOnePCB(block)) {
					block.writePCBintoBlock(this.MMS.getMainMemory());
					this.setRecordData("进程" + block.getProcessID() + "创建成功！");
					return true;
				} else {
					this.setRecordData("作业" + JCB.getJobID() + "创建进程失败：无空闲PCB！");
					return false;
				}
			} else {
				this.setRecordData("作业" + JCB.getJobID() + "创建进程失败：PCB池已满！");
				return false;
			}
		}
	}

	/**
	 * @implNote 1.释放交换区；<br>
	 *           2.放弃占用的页表；<br>
	 *           3.删除PCB；<br>
	 * @param proID 要撤销的进程的ID
	 * @return false 撤销进程失败
	 * @return true 撤销成功
	 */
	public boolean deletePro(int proID) {
		ProcessControlBlock block = this.MMS.getPcbQueue().findThePCB(proID);
		if (block == null) {
			this.setRecordData("进程" + proID + "撤销失败：查无此进程！");
			return false;// 找不到指定的进程，撤销失败
		} else {
			// 释放交换区
			if (this.MMS.getSubMemory().releaseOldExchangeArea(block.getControlInfo().getProcessAddress(),
					block.getControlInfo().getPageTableItemNum())) {
				ArrayList<Integer> blockNumList = new ArrayList<Integer>();
				// 默认一个进程所有逻辑页是相连的，如果不相邻请修改下面的遍历过程。
				for (int i = 0; i < block.getControlInfo().getPageTableItemNum(); i++) {
					PageTableItem item = this.MMS.getPageTable()
							.findItemByPageNum(block.getControlInfo().getTextPoint().getPageID() + i);
					if (item.isStateBit()) {
						blockNumList.add(item.getBlockID());
					}
				}
				this.MMS.getUserBuddyAllocator().free_pages(blockNumList);// 伙伴算法释放空闲块。
				this.MMS.getPageTable().leavePageTable(block.getControlInfo().getPageTableAddress(),
						block.getControlInfo().getPageTableItemNum());// 放弃页表
				if (this.MMS.getPageTable().deleteTheProItemNumPair(proID)) {
					if (this.monitor.P(MESSAGE.MEMORY_SOURCE_EMPTY, proID)) {// 查看是否能激活一个挂起的进程。
						if (!this.MMS.getPcbQueue().getPendBlockList().isEmpty()
								|| !this.MMS.getPcbQueue().getPendReadyList().isEmpty()) {
							// 只有在挂起队列都不为空的情况下才会生产激活许可，如果没有挂起进程，就不生产，从而达到每次挂起都需要等下一个撤销才能激活的效果。
							Integer activateID = this.monitor.V(MESSAGE.MEMORY_SOURCE_FULL, proID);// 如果可以申请到激活挂起进程的资格就尝试生产一个允许激活许可证。
							if (activateID != null) {
								this.activatePro(activateID);
							}
						}
					}
					//如果没有P成功，请求数会增加，EMPTY信号量也会减为负数，并且进程会在后面排队，所以要清除请求。
					//如果P成功，Available会减少到0，Allocation会增加到1，信号量回到0个。
					//所以对于Allocation我们不应该让Allocation+1
					this.monitor.clearProIDWaitingMessages(proID);// 清除所有的资源需求。
					this.setRecordData("进程" + block.getProcessID() + "被撤销！");
					return this.MMS.getPcbQueue().deleteThePCB(proID);// 从相应PCB队列中删除PCB
				} else {
					return false;
				}
			} else {
				this.setRecordData("进程" + block.getProcessID() + "撤销失败：外存交换区释放错误！");
				return false;
			}
		}
	}

	/**
	 * 阻塞进程(必须在内核状态下才能调用此函数)
	 * 
	 * @implNote 1、判断是否有进程在运行<br>
	 *           2、如果有进程，先将现场保存到PCB<br>
	 *           3、向PCB写入阻塞原因<br>
	 *           4、PCB状态更改为阻塞态<br>
	 *           5、将更新后的PCB信息写入主存<br>
	 *           6、PCB搬移至阻塞队列<br>
	 * @param reason 阻塞原因
	 * @return true 成功阻塞当前进程<br>
	 *         false 当前无进程运行，无法阻塞<br>
	 */
	public boolean blockPro(WAIT_REASON reason) {
		if (this.MMS.getPcbQueue().getRunningPCB().isEmpty()) {
			return false;// 当前无运行进程，不予阻塞
		} else {
			ProcessControlBlock block = this.MMS.getPcbQueue().getRunningPCB().get(0);
			// 保存现场
			this.saveSiteInfoByPcb(block);
			// 写原因
			block.getControlInfo().setWaitReason(reason);
			// 更改为阻塞态
			block.getControlInfo().setProcessState(PRO_STATE.BLOCK);
			block.writePCBintoBlock(this.MMS.getMainMemory());
			if (this.MMS.getPcbQueue().runToBlock()) {
				this.recordRunTimeOnceB(block);
				this.setRecordData("进程" + block.getProcessID() + "被阻塞！");
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * 唤醒进程(必须在内核状态下才能调用此函数)
	 * 
	 * @implNote 1、将阻塞队列的PCB移动到就绪队列并按优先级排队<br>
	 *           2、状态改为就绪态<br>
	 *           3、将更新的PCB数据写入内存<br>
	 *           4、同样该函数可以用于挂起阻塞到挂起就绪<br>
	 * @param proID 需要唤醒的进程ID
	 * @return true 成功唤醒指定进程<br>
	 *         false 并未在阻塞队列找到指定ID的进程<br>
	 */
	public boolean wakePro(int proID) {
		if (this.MMS.getPcbQueue().blockToReady(proID)) {
			ProcessControlBlock block = this.MMS.getPcbQueue().findReadyPCB(proID);// 移入就绪队列
			block.getControlInfo().setProcessState(PRO_STATE.READY);// 改状态
			block.writePCBintoBlock(this.MMS.getMainMemory());
			this.setRecordData("进程" + block.getProcessID() + "被唤醒！");
			return true;
		} else if (this.MMS.getPcbQueue().pendBlockToPendReady(proID)) {
			ProcessControlBlock block = this.MMS.getPcbQueue().findPendReadyPCB(proID);// 移入挂起就绪队列
			block.getControlInfo().setProcessState(PRO_STATE.PEND_READY);
			block.writePCBintoBlock(this.MMS.getMainMemory());
			this.setRecordData("进程" + block.getProcessID() + "从挂起阻塞队列被挂起唤醒！");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 挂起进程
	 * 
	 * @param proID
	 * @return
	 */
	public boolean pendPro(int proID) {
		ProcessControlBlock block = this.MMS.getPcbQueue().findThePCB(proID);
		if (block.getControlInfo().getProcessState() == PRO_STATE.BLOCK) {// 如果是阻塞态
			block.getControlInfo().setProcessState(PRO_STATE.PEND_BLOCK);// 修改进程状态
			block.writePCBintoBlock(this.MMS.getMainMemory());// 更新内存内容
			// 将进程的驻留在内存中的块全都挂回交换区。
			this.moveProToSwap(block);
			if (this.MMS.getPcbQueue().blockToPendBlock(proID)) {
				this.monitor.V(MESSAGE.MEMORY_SOURCE_EMPTY, proID);// 为撤销进程生产一个激活挂起进程权力证。
				this.monitor.P(MESSAGE.MEMORY_SOURCE_FULL, proID);// 同时，挂起的进程申请激活，这个P操作绝不能被马上满足。
				this.setRecordData("进程" + block.getProcessID() + "从挂起到挂起阻塞队列！");
				return true;
			} else {
				return false;
			}
		}
		if (block.getControlInfo().getProcessState() == PRO_STATE.READY) {// 如果是就绪态
			block.getControlInfo().setProcessState(PRO_STATE.PEND_READY);// 修改进程状态
			block.writePCBintoBlock(this.MMS.getMainMemory());// 更新内存内容
			// 将进程的驻留在内存中的块全都挂回交换区。
			this.moveProToSwap(block);
			if (this.MMS.getPcbQueue().readyToPendReady(proID)) {
				this.monitor.V(MESSAGE.MEMORY_SOURCE_EMPTY, proID);// 为撤销进程生产一个激活挂起进程权力证。
				this.monitor.P(MESSAGE.MEMORY_SOURCE_FULL, proID);// 同时，挂起的进程申请激活，这个P操作绝不能被马上满足。
				this.setRecordData("进程" + block.getProcessID() + "从挂起到挂起就绪队列！");
				return true;
			} else {
				return false;
			}
		}
		return false;// 不属于就绪态或是阻塞态，就不能挂起
	}

	/**
	 * 激活进程
	 * 
	 * @param proID
	 * @return
	 */
	public boolean activatePro(int proID) {
		ProcessControlBlock block2 = this.MMS.getPcbQueue().findPendReadyPCB(proID);// 去挂起就绪队列找
		if (block2 != null) {
			block2.getControlInfo().setProcessState(PRO_STATE.READY);
			block2.writePCBintoBlock(this.MMS.getMainMemory());// 更新内存内容
			if (this.MMS.getPcbQueue().pendReadyToReady(proID)) {
				this.setRecordData("进程" + block2.getProcessID() + "从挂起就绪队列被激活！");
				return true;
			} else {
				return false;
			}
		}
		ProcessControlBlock block = this.MMS.getPcbQueue().findPendBlockPCB(proID);// 去挂起阻塞队列找
		if (block != null) {// 找到了
			block.getControlInfo().setProcessState(PRO_STATE.BLOCK);// 修改进程状态
			block.writePCBintoBlock(this.MMS.getMainMemory());// 更新内存内容
			if (this.MMS.getPcbQueue().pendBlockToBlock(proID)) {
				this.setRecordData("进程" + block.getProcessID() + "从挂起阻塞队列被激活！");
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * 运行进程(必须在内核状态下才能调用此函数)
	 * 
	 * @apiNote 1、如果是阻塞进程唤醒的，还需要在调用此函数后用核心栈再恢复一次现场<br>
	 *          2、如果是新创建的或时间片耗尽就绪的，还需要调用此函数后回用户态才能开始执行指令<br>
	 * @implNote 1、检查就绪队列有进程<br>
	 *           2、移动就绪队首进程到运行<br>
	 *           3、状态改为运行态<br>
	 *           4、将更新的数据写入主存<br>
	 *           5、将页表基址装入MMU<br>
	 *           6、用PCB恢复现场<br>
	 * @return true 成功运行一个进程<br>
	 *         false 就绪队列为空，无法运行进程<br>
	 */
	public boolean runPro() {
		if (this.checkReadyQueue()) {
			this.MMS.getPcbQueue().readyToRun();// 就绪-运行
			ProcessControlBlock block = this.MMS.getPcbQueue().getRunningPCB().get(0);
			block.getControlInfo().setProcessState(PRO_STATE.RUN);
			block.writePCBintoBlock(this.MMS.getMainMemory());
			synchronized (this.MMS.getMmu()) {
				// 将进程的页表基址装入
				this.MMS.getMmu().setPagetableBasicAddress(block.getControlInfo().getPageTableAddress());
				// 清空块表等待重新填充。
				this.MMS.getMmu().clearTLB();
			}
			// CPU现场恢复
			this.recoverSiteInfoByPcb(block);
			// 清空访问字段并给其他的自增1
			block.getControlInfo().clearAccessTime();
			this.MMS.getPcbQueue().autoIncreaseReadyBlockPCB();
			this.recordRunTimeOnceA();
			this.setRecordData("进程" + block.getProcessID() + "进入CPU运行！");
			return true;
		} else {// 就绪队列无进程
			return false;
		}
	}

	/**
	 * 功能： 将运行进程返回到就绪队列队尾
	 * 
	 * @implNote 1、检查运行态有无进程<br>
	 *           2、用PCB保护现场<br>
	 *           3、状态改为就绪态<br>
	 *           4、将等待原因设为unknown<br>
	 *           5、更新的PCB数据写入主存<br>
	 *           6、将运行的PCB移动到就绪队列<br>
	 * @return true 运行进程成功移入就绪队列队尾<br>
	 *         false 无运行进程，无法回就绪态<br>
	 */
	public boolean readyPro() {
		if (this.MMS.getPcbQueue().getRunningPCB().isEmpty()) {
			return false;// 当前无运行进程，不予就绪
		} else {
			ProcessControlBlock block = this.MMS.getPcbQueue().getRunningPCB().get(0);
			// 保存现场
			this.saveSiteInfoByPcb(block);
			// 更改为就绪态
			block.getControlInfo().setProcessState(PRO_STATE.READY);
			// 设置等待原因为无，用于运行该进程时检测是否需要核心栈恢复现场
			block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
			block.writePCBintoBlock(this.MMS.getMainMemory());
			if (this.MMS.getPcbQueue().runToReady()) {
				this.setRecordData("进程" + block.getProcessID() + "耗完时间片，回到就绪队列队尾！");
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * @apiNote 将某个进程驻留在内存中的块全都挂回交换区.(仅限于用户区的交换)
	 * @param block 要被挂起的进程
	 * @return 是否成功挂起
	 */
	public boolean moveProToSwap(ProcessControlBlock block) {
		ArrayList<PageTableItem> items = this.MMS.getPageTable().findTrueItems(
				block.getControlInfo().getPageTableAddress(), block.getControlInfo().getPageTableItemNum());
		if (items.size() == 0) {
			return false;
		} else {
			for (PageTableItem pageTableItem : items) {
				// 更新辅存地址
				if (this.MMS.getPageTable().updateMainToSubItemSubAdd(pageTableItem.getBlockID(), block)) {
					// 将数据放回对应的辅存地址
					this.MMS.coverBlockFromMainToSub(pageTableItem.getBlockID(), pageTableItem.getSubmemoryAddress());
					ArrayList<Integer> blockIdList = new ArrayList<Integer>();
					blockIdList.add(pageTableItem.getBlockID());
					this.MMS.getUserBuddyAllocator().free_pages(blockIdList);
					pageTableItem.setStateBit(false);// 设为无效项
					// 进程拥有实际页表项数减一
					this.MMS.getPageTable().getProItemnumPairList().getPairOfThePro(block.getProcessID())
							.decreaseItemNum();
				} else {
					return false;// 更新辅存失败，意味着无法挂回交换区，中断交换过程。
				}
			}
			return true;
		}
	}

	/**
	 * 保存现场
	 * 
	 * @param target:想要将现场保存到的目标PCB
	 */
	public void saveSiteInfoByPcb(ProcessControlBlock target) {
		target.getSiteInfo().updateData(this.CPU.getPC().getData(), this.CPU.getIR().getInstruction().getData(),
				this.CPU.getPSW().getData(), this.CPU.getSP().getData(), this.CPU.getAR().getData());
	}

	/**
	 * 恢复现场
	 * 
	 * @param source:提供现场信息的来源PCB
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
				if (recordData != null) {// 如果传入的不是null，表示有消息。
					if (!this.lastRecordData.equals(recordData)) {// 如果这条消息不是上一条消息的重复
						if (!this.recordData.equals("")) {
							// 如果之前的消息不因为外层消息提取而清空过，就可以直接将新消息插入到旧消息的前面。
							this.recordData = new String(this.recordData + "\n" + recordData);
						} else {
							// 如果已经被外部刷新进程提取了信息，并且清空过，就应该作为第一条信息，无需插在前面加回车。
							this.recordData = new String(recordData);
						}
						// 将新消息记录为旧消息记录。
						this.lastRecordData = new String(recordData);
					}
				} else {
					// 如果传入的null表示清空，但是不清空上一条消息的记录，可以检测新到的消息是不是还是上一条消息。
					this.recordData = new String();
				}
			}
		}
	}

	/**
	 * @apiNote 死锁恢复函数。
	 * @param proIdList 死锁进程的ID
	 */
	public boolean recoverDeadLine(ArrayList<Integer> proIdList) {
		if (proIdList == null) {
			return false;
		} else if (proIdList.size() == 0) {
			return false;
		} else {
			for (int i = 0; i < proIdList.size(); i++) {
				// 找到每一个死锁进程所占有资源并将其一一通过V释放，同时唤醒等待这些资源的第一个进程。
				int proid = proIdList.get(i);
				ArrayList<MESSAGE> arrayList = this.findProIDHavingMessages(proid);// 资源数组
				for (int j = 0; j < arrayList.size(); j++) {
					this.setRecordData("……进程" + proid + "释放资源<" + arrayList.get(j).toString() + ">……");
					Integer integer = this.monitor.V(arrayList.get(j), proid);
					if (integer != null) {
						this.wakePro(integer);
						proIdList.remove(integer);
					}
				}
				this.setRecordData("……已经释放进程" + proid + "的全部占用资源……");
				if (proIdList.size() == 0) {
					return true;// 受困的进程已经全部唤醒，无需再做恢复操作。
				}
				this.monitor.clearProIDWaitingMessages(proid);
				ProcessControlBlock block = this.MMS.getPcbQueue().findThePCB(proid);

				if (block != null) {
					if (block.getSiteInfo().getPcData() > block.getControlInfo().getTextPoint().getData()) {
						// 不论是挂起阻塞还是挂起就绪一律变为挂起就绪，并且回滚一条指令。
						block.getSiteInfo()
								.setPcData(block.getSiteInfo().getPcData() - MemoryManageSystem.getUnitSize());
						this.setRecordData("……进程" + proid + "回滚一条指令，重新执行……");
					}
					block.getControlInfo().setWaitReason(WAIT_REASON.UNKNOW);
					// 这里如果直接唤醒，应该会导致该指令被跳过。
					this.wakePro(proid);
					proIdList.remove(new Integer(proid));
				}
				i = -1;
			}
			return true;
		}
	}

	/**
	 * @param proID 占用资源的进程编号
	 * @return 进程占用的信号量链表。
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
				// 拥有外部设备。
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			break;
		case NEED_BUFFER:
			if (state == PRO_STATE.READY || state == PRO_STATE.PEND_READY) {
				// 拥有缓冲区和外部设备。
				arrayList.add(MESSAGE.BUFFER);
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// 拥有外部设备。
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			break;
		case TRANS_DATA_INTPUT:
			// 拥有缓冲区和外部设备。
			arrayList.add(MESSAGE.BUFFER);
			arrayList.add(this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			break;
		case RELEASE_BUFFER:
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// 拥有缓冲区和外部设备。
				arrayList.add(MESSAGE.BUFFER);
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			if (state == PRO_STATE.READY || state == PRO_STATE.PEND_READY) {
				// 拥有外部设备。
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			break;
		case RELEASE_DEVICE:
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// 拥有外部设备。
				arrayList.add(
						this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			}
			break;
		case NEED_REGISTER:
			if (state == PRO_STATE.READY || state == PRO_STATE.PEND_READY) {
				// 如果已在就绪，说明申请到了源寄存器，拥有源寄存器。
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
			}
			break;
		case NEED_REGISTER1:
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// 如果还在阻塞说明没有申请到目的寄存器，但拥有源寄存器。
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
			}
			if (state == PRO_STATE.READY || state == PRO_STATE.PEND_READY) {
				// 如果已在就绪，说明申请到了目的寄存器，拥有两个寄存器
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(1)));
			}
			break;
		case RELEASE_REGISTER1:
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// 如果还在阻塞说明没有成功释放目的寄存器，拥有源寄存器和目的寄存器。
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(1)));
			}
			if (state == PRO_STATE.READY || state == PRO_STATE.PEND_READY) {
				// 如果已在就绪，说明释放了目的寄存器，只有源寄存器
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
			}
			break;
		case RELEASE_REGISTER:
			if (state == PRO_STATE.BLOCK || state == PRO_STATE.PEND_BLOCK) {
				// 如果还在阻塞说明没有成功释放源寄存器，拥有源寄存器。
				arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
			}
			break;
		case INPUT_WAIT:
			// 拥有缓冲区和外部设备。
			arrayList.add(MESSAGE.BUFFER);
			arrayList.add(this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			break;
		case OUTPUT_WAIT:
			// 拥有缓冲区和外部设备。
			arrayList.add(MESSAGE.BUFFER);
			arrayList.add(this.analyseDevice(register.getDeviceAddress() - MemoryManageSystem.getDeviceStartAddress()));
			break;
		case REQUEST_PAGE_ACCESSMEM:
			// 拥有源寄存器。
			arrayList.add(this.analyseRegister(register.getRegisterIDList().get(0)));
			break;
		default:
			// 无设备。
			break;
		}
		return arrayList;
	}
}
