package osPackage;

import java.util.ArrayList;

/**
 * @apiNote DMA应该是与CPU并列的硬件，属于进程调度系统，
 * @implNote 还差DMA怎么知道缓冲区在哪？<br>
 *           DMA是不是不需要知道数据进入缓冲区后下一步该往那个进程缓冲区送？<br>
 *           系统怎么知道该唤醒的是哪一个特定进程<br>
 *           解决办法：DMA应该知道系统缓冲区的位置，也知道激活DMA的那条指令的进程缓冲区位置。<br>
 *           系统去阻塞队列中阻塞原因，结合DMA中方向型号到底是输入还是输出，从队首唤醒最早阻塞的那个进程<br>
 *           当CPU接受来自DMA的中断信号，就会查DMA中的进程缓冲区的地址，并将系统核心区的数据搬移到这个进程缓冲区。<br>
 *           另外，外存只能提供一整块数据，所以，可以将外存的一整块先读入DMA的一个假存储器，然后一个字一个字送入数据寄存器<br>
 *           寄存器每拿到一个字就送入主存系统缓冲区。<br>
 *           DMA需要操作内外存，所以需要将MMS的指针传入DMA内部<br>
 */
public class DirectMemoryAccess {
	/**
	 * 系统缓冲区的内存物理地址
	 * 
	 * @implNote 在DMA初始化的时候就赋值，传入系统缓冲区地址
	 */
	Address bufferAddress;
	/**
	 * 主存地址寄存器
	 * 
	 * @apiNote 存放内存中进程缓冲区的首地址（由指令提供逻辑页号，通过MMU转换出物理内存地址送入DMA）
	 */
	Address mainMemRegister;
	/**
	 * 外设地址寄存器
	 * 
	 * @apiNote 存放外设在外存中的地址
	 */
	Integer deviceAddRegister;
	/**
	 * 字计数器
	 * 
	 * @apiNote 记录要搬运的字数
	 */
	Integer wordCounter;
	/**
	 * 发送中断信号量
	 * 
	 * @apiNote 用于DMA向CPU发出中断信号
	 */
	boolean sendInterruption;
	/**
	 * 接收中断信号量
	 * 
	 * @apiNote 用于接收来自CPU的命令信号量
	 */
	boolean recieveInterruption;
	/**
	 * 方向信号
	 * 
	 * @apiNote 控制DMA的传输方向，信号由正在执行的I还是O决定<br>
	 *          Output---true表示向辅存输出<br>
	 *          Input---false表示向主存输入<br>
	 */
	boolean directionMessage;
	/**
	 * 数据缓冲寄存器
	 * 
	 * @apiNote 负责将辅存中的一块数据按字搬运到内核缓冲区<br>
	 *          或将内核缓冲区的块按字节搬运到辅存<br>
	 */
	Integer wordRegister;
	/**
	 * 无形的存储区
	 * 
	 * @apiNote 主要用来替外存现存一块数据<br>
	 *          输出：一个字一个字灌入这个存储区，存满后整体灌入辅存<br>
	 *          输入：辅存整体灌入这存储区，再一个字一个字的取出来<br>
	 *          总体营造一种假象，就是从外存一个字一个字读入DMA
	 */
	ArrayList<Integer> shapelessBuffer;

	/**
	 * MMS地址
	 * 
	 * @apiNote 为了操作MMS，所以指向MMS
	 */
	MemoryManageSystem mmsPoint;

	/**
	 * PMS指针
	 */
	ProcessManageSystem pmsPoint;

	/**
	 * 构造函数 初始化所有的寄存器和中断信号发送器和接收器<br>
	 * 接受一个核心缓冲区的物理内存地址
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
	 * @apiNote 用于多线程的run()函数，在死循环中，如果检测到CPU发来的信号，就执行该行为。
	 */
	public void wholeJob() {
		if (this.recieveInterruption) {
			this.pmsPoint.setRecordData("DMA接收到CPU发来信号，启动并开始执行传输工作……");
			// 清空CPU发来的中断信号
			this.recieveInterruption = false;
			// 开始做传输工作
			this.work();
			// 设置供CPU检测的中断信号
			this.pmsPoint.setRecordData("……DMA传输工作执行完成，向CPU发出中断信号。");
			this.sendInterruption = true;
		}
	}

	/**
	 * @apiNote 从内存搬到辅存过程中使用，传输第一步
	 * @implNote 将外存的一块数据送入无形存储区
	 */
	public void readOneBlockFromSub() {
		this.shapelessBuffer = new ArrayList<Integer>(
				this.mmsPoint.getSubMemory().getTheSubBlock(this.deviceAddRegister).inputBlockData());
	}

	/**
	 * @apiNote 从辅存搬到主存过程中使用，传输最后一步
	 * @implNote 将无形区的数据灌入外存指定设备区
	 */
	public void writeOneBlockToSub() {
		this.mmsPoint.getSubMemory().getTheSubBlock(this.deviceAddRegister).outputBlockData(this.shapelessBuffer);
	}

	/**
	 * @apiNote 从内存搬到辅存过程中使用，将无形区充满
	 * @implNote 用来将系统缓冲区的数据逐字取出，填充无形区<br>
	 *           系统缓冲区指针自增一<br>
	 */
	public void fillShapelessBuffer() {
		this.shapelessBuffer.clear();
		this.bufferAddress.updateAddress(MemoryManageSystem.getBufferPagenum(), 0);
		for (int i = 0; i < this.wordCounter; i++) {
			this.wordRegister = this.mmsPoint.readOneWordFromMainMem(this.bufferAddress);
			this.shapelessBuffer.add(this.wordRegister);// 这里应该插入可视化显示
			this.bufferAddress.updateAddress(this.bufferAddress.getData() + MemoryManageSystem.getUnitSize());
		}
		if (this.shapelessBuffer.size() > 256) {
			this.shapelessBuffer.size();//bug
		}
	}

	/**
	 * @apiNote 从辅存搬到主存过程中，清空无形区
	 * @implNote 将无形区中逐字取出，写入主存<br>
	 *           系统缓冲区指针自增一<br>
	 */
	public void emptifyShaplessBuffer() {
		for (int i = 0; i < this.wordCounter; i++) {
			this.wordRegister = this.shapelessBuffer.get(i);// 这里应该插入可视化显示
			this.mmsPoint.getMainMemory().writeWordIntoMemory(this.bufferAddress, this.wordRegister);
			this.bufferAddress.updateAddress(this.bufferAddress.getData() + MemoryManageSystem.getUnitSize());
		}
		this.bufferAddress.updateAddress(MemoryManageSystem.getBufferPagenum(), 0);
		this.shapelessBuffer.clear();
	}

	/**
	 * @apiNote 让DMA工作
	 */
	public void work() {
		if (this.directionMessage) {// 表示输出到辅存
			this.fillShapelessBuffer();
			this.writeOneBlockToSub();
		} else {// 表示输入到内存
			this.readOneBlockFromSub();
			this.emptifyShaplessBuffer();
		}
	}

	/**
	 * 输入参数，用在DMA激活前，让DMA做准备
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
	 * @apiNote 用于一个线程的独立运行的死循环中的循环操作
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
