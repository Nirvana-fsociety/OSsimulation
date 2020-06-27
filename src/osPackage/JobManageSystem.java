package osPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * @apiNote 随机生成一个未来请求
 * @apiNote 根据一个未来请求生成一个作业控制块<br>
 *          按照作业控制块的指令数生成每条指令<br>
 *          向外存申请到文件区的若干块存放作业的程序集和数据集<br>
 * @apiNote 随机生成一种指令：<br>
 *          1、随机生成访存指令；<br>
 *          2、随即生成输入指令；<br>
 *          3、随机生成输出指令；<br>
 *          4、随机生成系统调用指令；<br>
 *          5、随机生成计算指令；<br>
 *          6、随机生成普通指令。<br>
 */
public class JobManageSystem {
	private final static String INPUT_DIRECT = new String(System.getProperty("user.dir") + "\\input");
	/**
	 * 请求文件：请求到来的时间 优先级 程序集的指令数 作业数据区双字数据个数
	 */
	private final static String REQUEST_FILE_NAME = new String(System.getProperty("user.dir") + "\\request.txt");
	/**
	 * 一个完整程序块拥有的指令数
	 */
	private final static int BAR_SIZE = 5;
	/**
	 * 作业指令文本文件的文件夹，存储人能看懂的作业程序
	 */
	private final static String JOB_INSTRUCTION_FILENAME = System.getProperty("user.dir") + "\\JobsInstruction";
	/**
	 * 普通指令产生的概率是系统调用指令的几倍
	 */
	private final static int NORMALMORE = 4;
	/**
	 * 未来请求序列中请求数
	 */
	private final static int FUTUREREQUESTS_NUM = 5;
	/**
	 * 作业后备队列的最大尺寸
	 */
	private final static int JOBQUEUE_MAXSIZE = 8;
	/**
	 * 整型随机数生成器
	 */
	private Random random;
	/**
	 * 时钟指针
	 */
	private Clock clockPoint;
	/**
	 * 辅存指针
	 */
	private SubMemory subMemoryPoint;
	/**
	 * 未来请求序列
	 */
	private ArrayList<FutureRequest> futureRequestQueue;
	/**
	 * 作业后备队列
	 */
	private List<JobControlBlock> jobQueue;
	/**
	 * 已经被创建进程的作业，离开后备队列进入该运行作业队列
	 */
	private ArrayList<JobControlBlock> runningJobsQueue;
	/**
	 * 如果用户在运行前按下读文件按钮，我们将此设置为false，然后选择不再自动创建文件，而是读入已有文件的方式创建作业。
	 */
	private Boolean create;

	/**
	 * @param clock 将时钟传入，用来检测到来的请求使用。
	 * @apiNote 构造函数
	 * @implNote 生成一个未来请求队列
	 */
	public JobManageSystem(Clock clock, SubMemory submemoryPoint) {
		super();
		this.setClockPoint(clock);
		this.setSubMemoryPoint(submemoryPoint);
		this.random = new Random();
		this.futureRequestQueue = new ArrayList<FutureRequest>();
		this.jobQueue = Collections.synchronizedList(new ArrayList<JobControlBlock>());
		this.setRunningJobsQueue(new ArrayList<JobControlBlock>());
		// 随机生成一个未来请求序列
		this.futureRequestQueueRandly();
		// 创建文件夹
		File file = new File(JOB_INSTRUCTION_FILENAME);
		if (!file.exists()) {// 如果文件夹不存在
			file.mkdir();// 创建文件夹
		}
		this.setCreate(new Boolean(true));// 默认生成文件
		File input = new File(INPUT_DIRECT);
		if (!input.exists()) {// 如果文件夹不存在
			input.mkdir();// 创建文件夹
		}
	}

	/**
	 * 迭代删除文件夹
	 * 
	 * @param dirPath 文件夹路径
	 */
	public static void deleteDir(String dirPath) {
		File file = new File(dirPath);
		if (file.isFile()) {
			file.delete();
		} else {
			File[] files = file.listFiles();
			if (files == null) {
				file.delete();
			} else {
				for (int i = 0; i < files.length; i++) {
					deleteDir(files[i].getAbsolutePath());
				}
				file.delete();
			}
		}
	}

	/**
	 * 将input文件夹中的用例全部复制到系统的对应位置上
	 */
	public boolean readOld() {
		for (int i = 0; i < 64; i++) {
			File dirFile = new File(INPUT_DIRECT + "\\" + 29 + SubBlock.getTrackPath() + "\\" + i + ".txt");
			if (!dirFile.exists()) {
				return false;
			}
		}
		File source = new File(INPUT_DIRECT + "\\request.txt");
		if (!source.exists()) {
			return false;
		}
		File dest = new File(REQUEST_FILE_NAME);
		if (dest.exists()) {
			dest.delete();
		}
		try {
			Files.copy(source.toPath(), dest.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 64; i++) {
			File dirFile = new File(INPUT_DIRECT + "\\" + 29 + SubBlock.getTrackPath() + "\\" + i + ".txt");
			File dirFile2 = new File(
					SubBlock.getSubmainmemDirectryPath() + "\\" + 29 + SubBlock.getTrackPath() + "\\" + i + ".txt");
			if (dirFile2.exists()) {
				dirFile2.delete();
			}
			try {
				Files.copy(dirFile.toPath(), dirFile2.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	/**
	 * @apiNote 随机生成一个未来请求序列
	 */
	public void futureRequestQueueRandly() {
		synchronized (futureRequestQueue) {
			int secondNum = 0;
			for (int i = 0; i < FUTUREREQUESTS_NUM; i++) {
				secondNum = this.random
						.nextInt(FutureRequest.getMaxIntimeDistance() - FutureRequest.getMinIntimeDistance())
						+ FutureRequest.getMinIntimeDistance();
				if (i > 0) {
					FutureRequest futureRequest = new FutureRequest(
							this.futureRequestQueue.get(i - 1).getInTime() + secondNum);
					this.futureRequestQueue.add(futureRequest);
				} else {
					FutureRequest futureRequest = new FutureRequest(this.clockPoint.getSecondNum() + secondNum);
					this.futureRequestQueue.add(futureRequest);
				}
			}
			this.sortRequestQueueByIntime();
			writeFutureRequest();
		}
	}

	public void writeFutureRequest() {
		ArrayList<String> arrayList = new ArrayList<String>();
		for (int i = 0; i < this.futureRequestQueue.size(); i++) {
			FutureRequest futureRequest = this.futureRequestQueue.get(i);
			String string = new String("I" + futureRequest.getInTime() + "I" + futureRequest.getPriority() + "I"
					+ futureRequest.getInstructionNum() + "I" + futureRequest.getDataNum() + "I\n");
			arrayList.add(string);
		}
		File file = new File(REQUEST_FILE_NAME);
		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (int i = 0; i < arrayList.size(); i++) {
				bufferedWriter.write(arrayList.get(i));
				bufferedWriter.flush();
			}
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @apiNote 读入请求文件
	 */
	public boolean readFutureRequest() {
		synchronized (futureRequestQueue) {
			this.futureRequestQueue.clear();
			ArrayList<String> arrayList = new ArrayList<String>();
			// 输入的起点是文件
			File file = new File(REQUEST_FILE_NAME);
			try {
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String string = null;
				while ((string = bufferedReader.readLine()) != null) {
					arrayList.add(string);
				}
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (arrayList.size() == 0) {
				return false;
			}
			// 字符串有用信息中间用I字符分割。
			for (int i = 0; i < arrayList.size(); i++) {
				int count = 0;
				int intime = -1;
				int priority = -1;
				int instNum = -1;
				int dataNum = -1;
				for (int j = 0; j < arrayList.get(i).length(); j++) {
					if (arrayList.get(i).charAt(j) == 'I') {
						for (int j2 = j + 1; j2 < arrayList.get(i).length(); j2++) {
							if (arrayList.get(i).charAt(j2) == 'I') {
								switch (count) {
								case 0:
									intime = Integer.valueOf(arrayList.get(i).substring(j + 1, j2), 10);
									count++;// 说明去了一个值了。
									break;
								case 1:
									priority = Integer.valueOf(arrayList.get(i).substring(j + 1, j2), 10);
									count++;// 说明优先级也取了
									break;
								case 2:
									instNum = Integer.valueOf(arrayList.get(i).substring(j + 1, j2), 10);
									count++;// 说明指令数也取了
									break;
								case 3:
									dataNum = Integer.valueOf(arrayList.get(i).substring(j + 1, j2), 10);
									count++;// 数据数也取了
									break;
								default:
									break;
								}
								break;
							}
						}
					}
				}
				this.futureRequestQueue.add(new FutureRequest(intime, priority, instNum, dataNum));
			}
			this.sortRequestQueueByIntime();
			return true;
		}
	}

	public static class RequestsIntimeCompare implements Comparator<FutureRequest> {
		@Override
		public int compare(FutureRequest o1, FutureRequest o2) {
			if (o1.getInTime() > o2.getInTime()) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	/**
	 * @apiNote 将未来请求序列按照到来时间从小到大排序
	 */
	public void sortRequestQueueByIntime() {
		RequestsIntimeCompare compare = new RequestsIntimeCompare();
		if (!this.futureRequestQueue.isEmpty()) {
			Collections.sort(this.futureRequestQueue, compare);
		}
	}

	/**
	 * @apiNote 创建一个未来请求，并加入未来请求序列。
	 * @param intime 创建时间，一定要用当前时钟时间。
	 */
	public void addOneFutureRequest(int intime) {
		synchronized (create) {
			FutureRequest futureRequest = new FutureRequest(intime);
			String string = new String("I" + futureRequest.getInTime() + "I" + futureRequest.getPriority() + "I"
					+ futureRequest.getInstructionNum() + "I" + futureRequest.getDataNum() + "I\n");
			File file = new File(REQUEST_FILE_NAME);
			try {
				FileWriter fileWriter = new FileWriter(file, true);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(string);
				bufferedWriter.flush();
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.setCreate(true);
			this.createJcb(futureRequest);
			this.setCreate(false);
		}
	}

	/**
	 * @return 找到的最大作业ID
	 */
	public Integer findMaxJobID() {
		int id = 0;
		for (JobControlBlock jobControlBlock : this.jobQueue) {
			if (jobControlBlock.getJobID() >= id) {
				id = jobControlBlock.getJobID();
			}
		}
		return id;
	}

	/**
	 * @apiNote 根据未来请求创建作业控制块，并且在外存申请空闲扇区
	 * @param request 一个未来请求
	 * @return true 成功创建作业并加入后备队列<br>
	 *         false 后备队列的尺寸不足或外存作业区不足导致无法创建作业<br>
	 */
	public boolean createJcb(FutureRequest request) {
		if (this.jobQueue.size() >= JOBQUEUE_MAXSIZE) {
			return false;
		}
		JobControlBlock block = new JobControlBlock();
		block.setJobID(this.findMaxJobID() + 1);
		block.setProcessPriority(request.getPriority());
		block.setInstructionNum(request.getInstructionNum());
		block.setDataNum(request.getDataNum());
		// 程序集申请外存地址
		int textSize = block.calculateJobTextPageNum();
		Integer textSubMemAddress = this.subMemoryPoint.applyJobBlocks(textSize);
		if (textSubMemAddress == null) {
			return false;
		} else {
			block.setTextSubMemAddress(textSubMemAddress);
		}
		// 数据集申请外存地址
		int dataSize = block.calculateJobTextPageNum();
		Integer dataSubMemAddress = this.subMemoryPoint.applyJobBlocks(dataSize);
		if (dataSubMemAddress == null) {
			return false;
		} else {
			block.setDataSubMemAddress(dataSubMemAddress);
		}
		if (this.create) {
			// 随机生成各类指令
			ArrayList<Integer> instructions = generateInstructions(block.getInstructionNum());
			// 将指令集写入辅存块。
			for (int i = 0; i < block.calculateJobTextPageNum(); i++) {
				int start = Block.getBlockSize() * i;
				int end = 0;
				if (instructions.size() >= Block.getBlockSize() * (i + 1)) {
					end = Block.getBlockSize();
				} else {
					end = instructions.size() - (Block.getBlockSize() * i);
				}
				ArrayList<Integer> subArray = new ArrayList<Integer>(instructions.subList(start, start + end));
				// 将指令写入文件
				this.initJobInsFile(block.getTextSubMemAddress() + i, block.getJobID(), start, start + end - 1);
				this.instructionsIntoFile(block.getTextSubMemAddress() + i, subArray);
				this.subMemoryPoint.getTheSubBlock(block.getTextSubMemAddress() + i).outputBlockData(subArray);
			}
			// 随机生成各种数据
			ArrayList<Integer> datas = new ArrayList<Integer>();
			for (int i = 0; i < block.getDataNum(); i++) {
				int data = this.random.nextInt();
				if (data < 0) {
					data = i;
				}
				datas.add(data);
			}
			for (int i = 0; i < block.calculateJobDataPageNum(); i++) {
				int start = Block.getBlockSize() * i;
				int end = 0;
				if (datas.size() >= Block.getBlockSize() * (i + 1)) {
					end = Block.getBlockSize();
				} else {
					end = datas.size() - (Block.getBlockSize() * i);
				}
				ArrayList<Integer> subArray = new ArrayList<Integer>(datas.subList(start, start + end));
				this.subMemoryPoint.getTheSubBlock(block.getDataSubMemAddress() + i).outputBlockData(subArray);
			}
		}
		// 两集载入辅存成功，加入后备队列。
		this.jobQueue.add(block);
		return true;
	}

	/**
	 * @apiNote 创建作业记录文本文件。
	 */
	public void initJobInsFile(Integer subaddress, int jobID, int start, int end) {
		File file = new File(JOB_INSTRUCTION_FILENAME + "\\JobInstructions" + subaddress + ".txt");
		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write("Job<<" + jobID + ">>---[No." + start + " , No." + end + "]\n");
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @apiNote 将作业指令内容记录到对应文本文件中。
	 */
	public void instructionsIntoFile(Integer subaddress, ArrayList<Integer> instructions) {
		File file = new File(JOB_INSTRUCTION_FILENAME + "\\JobInstructions" + subaddress + ".txt");
		InstructionRegister translator = new InstructionRegister(0, 0);
		try {
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (int i = 0; i < instructions.size(); i++) {
				bufferedWriter.write(
						"Instruction<" + i + ">" + this.translateInstruction(translator, instructions.get(i)) + "\n");
				bufferedWriter.flush();
			}
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param translator  IR寄存器类，调来用的，并不是真的，因为IR寄存器能解析指令，为了重用代码。
	 * @param instruction 要解析的指令元数据
	 * @return 解析好的指令中文。
	 */
	public String translateInstruction(InstructionRegister translator, Integer instruction) {
		translator.updateData(0, instruction);
		String string = new String();
		switch (translator.getInstructionID()) {
		case ACCESS_MEMORY:
			string += "--|访存|--" + "[Register-" + translator.getRegisterIDList().get(0) + "][Offset-"
					+ translator.getAccessOffset() + "]";
			return string;
		case CALCULATE:
			string += "--|计算|--" + "[Register-" + translator.getRegisterIDList().get(0) + "][Register-"
					+ translator.getRegisterIDList().get(1) + "]";
			return string;
		case INPUT:
			string += "--|输入|--" + "[DeviceAddress-" + translator.getDeviceAddress() + "]";
			return string;
		case OUTPUT:
			string += "--|输出|--" + "[DeviceAddress-" + translator.getDeviceAddress() + "]";
			return string;
		case SYSTEM_CALL:
			string += "--|系统|--" + "[SystemCallID-" + translator.getCallID() + "]";
			return string;
		case JUMP:
			string += "--|跳转|--" + "[Offset" + translator.getJumpOffset() + "]";
			return string;
		case NORMAL:
			string += "--|普通|--";
			return string;
		default:
			string += "--|未知|--";
			return string;
		}
	}

	/**
	 * @apiNote 生成指定数量的指令数
	 * @implNote 跳转指令定死跳转一块页框的大小，如果跳转指令的跳转数不再定死，请修改。
	 * @param insNum 要生成的指令数。
	 * @return 生成的指令集链表。
	 */
	public ArrayList<Integer> generateInstructions(int insNum) {
		ArrayList<Integer> instructions = new ArrayList<Integer>();
		int baseBlockNum = insNum / Block.getBlockSize();// 指令能装满的页数。
		// 假设总共256 × 3 + 32条指令，先生成29条真正执行的非跳转指令，3条跳转指令。
		int realInsNum = insNum - Block.getBlockSize() * baseBlockNum - baseBlockNum;
		int barNum = this.random.nextInt(realInsNum / BAR_SIZE) + 1;// 如果算出是3个bar，我们将只能生成0~2个，所以+1
		for (int i = 0; i < barNum; i++) {
			instructions.addAll(this.createOneBar());
		}
		for (int i = 0; i < realInsNum - (barNum * BAR_SIZE); i++) {
			// 随机生成0~24范围内的数（1~5是特殊指令）-2是为了排除0和6得到特殊指令的数量
			int isNormal = this.random.nextInt(NORMALMORE + 1);
			int index = this.random.nextInt(instructions.size());
			if (isNormal > 0) {
				// 生成0以上的数据全都创建为普通指令。
				instructions.add(index, this.createNormal());
			} else {
				instructions.add(index, this.createSystemCall());
			}
		}
		// 在29条非跳转特殊指令中找到一个距离均匀距离。
		int deltaJumpDist = realInsNum / (baseBlockNum + 1);
		// 插入跳转指令的下标。
		int jumpIndex = 0;
		for (int i = 0; i < baseBlockNum; i++) {
			jumpIndex += deltaJumpDist;
			// 随机生成下标位于实际执行指令之前之中之后。
			instructions.add(jumpIndex, this.createJump());
			// 跳转指令后生成的普通指令数256条
			int normalInsNum = Block.getBlockSize();
			for (int j = 0; j < normalInsNum; j++) {
				instructions.add(jumpIndex + 1 + j, this.createNormal());
			}
			jumpIndex += normalInsNum;
		}
		return instructions;
	}

	/**
	 * @apiNote 无限循环检测未来请求队列，如果有到期的未来请求就创建为作业，加入作业后备队列。
	 * @return true 创建了作业<br>
	 *         false 创建作业失败<br>
	 */
	public boolean checkFutureRequest() {
		synchronized (futureRequestQueue) {
			if (this.futureRequestQueue.isEmpty()) {
				return false;
			} else {
				for (FutureRequest futureRequest : futureRequestQueue) {
					if (this.clockPoint.getSecondNum() >= futureRequest.getInTime()) {
						synchronized (this.jobQueue) {
							// 如果有请求到了时间，就创建作业。
							if (this.createJcb(futureRequest)) {
								this.futureRequestQueue.remove(futureRequest);
								return true;
							}
						}
					}
				}
			}
			return false;
		}
	}

	public Integer createIntruction(int instructionID) {
		switch (instructionID) {
		case 1:
			return this.createInput();
		case 2:
			return this.createOutput();
		case 3:
			return this.createSystemCall();
		case 4:
			return this.createCalculate();
		case 5:
			return this.createAccessMemory();
		case 6:
			return this.createNormal();
		case 7:
			return this.createJump();
		default:
			return null;
		}
	}

	/**
	 * @return 创建成功的一条跳转指令
	 */
	public Integer createJump() {
		int ID = 7;
		// 创建的Δoffset并不是真的offset而是一个增量，提取后要和基址相加而非与基址页号拼接。
		int deltaOffset = Block.getBlockSize() - 1;
		// 定死要跨256条指令，但是每次取跳转指令后PC还会自动+1，所以抛去一个。如果需要改就后期修改。
		int data = 0;
		data += ID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum());
		data += deltaOffset * MemoryManageSystem.getUnitSize();
		return new Integer(data);
	}

	/**
	 * @return 创建成功的一条系统调用指令
	 */
	public Integer createSystemCall() {
		int ID = 3;
		int systemID = this.random.nextInt(ProcessManageSystem.getNormalSystemcallNum())
				+ ProcessManageSystem.getSpecialSystemcallNum();
		int data = 0;
		data += ID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum());
		data += systemID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum()
				- InstructionRegister.getCallidBitnum());
		return new Integer(data);
	}

	/**
	 * @return 创建成功的一条访存指令。
	 */
	public Integer createAccessMemory() {
		int ID = 5;
		int registerID = this.random.nextInt(Monitor.getRegisterNum());
		// 创建的Δoffset并不是真的offset而是一个增量，提取后要和基址相加而非与基址页号拼接。
		// 这里设置是最小数据量，这样就不会超范围，但是有20个数据永远无法访问，所以期待以后优化。
		int deltaOffset = this.random.nextInt(ProcessManageSystem.getMinDataNum());
		int data = 0;
		data += ID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum());
		data += registerID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum()
				- InstructionRegister.getRegisteridBitnum());
		data += deltaOffset * MemoryManageSystem.getUnitSize();
		return new Integer(data);
	}

	/**
	 * @implNote 随机在设备区找了一页
	 * @return 创建成功的一条输入指令
	 */
	public Integer createInput() {
		int ID = 1;
		// 随机生成外设文件区地址
		int deviceAdd = this.random.nextInt(MemoryManageSystem.getDeviceBlocknum())
				+ MemoryManageSystem.getDeviceStartAddress();
		int data = 0;
		data += ID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum());
		data += deviceAdd;
		return new Integer(data);
	}

	/**
	 * @implNote 随机在设备区找了一页
	 * @return 创建成功的一条输出指令
	 */
	public Integer createOutput() {
		int ID = 2;
		int deviceAdd = this.random.nextInt(MemoryManageSystem.getDeviceBlocknum())
				+ MemoryManageSystem.getDeviceStartAddress();
		int data = 0;
		data += ID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum());
		data += deviceAdd;
		return new Integer(data);
	}

	public Integer createCalculate() {
		int ID = 4;
		int registerID1 = this.random.nextInt(Monitor.getRegisterNum());
		int registerID2 = this.random.nextInt(Monitor.getRegisterNum());
		while (registerID1 == registerID2) {// 只要两个寄存器ID相同，就再次生成，直到不相等。
			registerID2 = this.random.nextInt(Monitor.getRegisterNum());
		}
		int data = 0;
		data += ID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum());
		data += registerID1 << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum()
				- InstructionRegister.getRegisteridBitnum());
		data += registerID2 << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum()
				- InstructionRegister.getRegisteridBitnum() - InstructionRegister.getRegisteridBitnum());
		return new Integer(data);
	}

	/**
	 * @param registerIDA 源寄存器编号
	 * @param registerIDB 目的寄存器编号
	 * @return 生成的计算指令
	 */
	public Integer createCalculate(int registerIDA, int registerIDB) {
		int ID = 4;
		if (registerIDA >= Monitor.getRegisterNum() || registerIDB >= Monitor.getRegisterNum()
				|| registerIDA == registerIDB) {
			return null;
		}
		int registerID1 = registerIDB;// 目的寄存器
		int registerID2 = registerIDA;// 源寄存器
		int data = 0;
		data += ID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum());
		data += registerID1 << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum()
				- InstructionRegister.getRegisteridBitnum());
		data += registerID2 << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum()
				- InstructionRegister.getRegisteridBitnum() - InstructionRegister.getRegisteridBitnum());
		return new Integer(data);
	}

	public Integer createNormal() {
		int ID = 6;
		int data = 0;
		data += ID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum());
		return new Integer(data);
	}

	/**
	 * @apiNote 创建一个符合正常程序逻辑的程序块
	 * @return
	 */
	public ArrayList<Integer> createOneBar() {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		// 先访存，声明一个变量
		arrayList.add(this.createAccessMemory());
		// 再让用户进行数据输入
		arrayList.add(this.createInput());
		// 对数据进行计算
		arrayList.add(this.createCalculate());
		// 将计算结果写入内存中的变量
		arrayList.add(this.createAccessMemory());
		// 将内存中的计算结果输出给用户
		arrayList.add(this.createOutput());
		return arrayList;
	}

	public boolean createDeath() {
		int num = 2;
		if (this.jobQueue.size() + num >= JOBQUEUE_MAXSIZE) {
			return false;
		}
		ArrayList<String> arrayList = new ArrayList<String>();
		for (int i = 0; i < num; i++) {
			FutureRequest futureRequest = new FutureRequest(this.clockPoint.getSecondNum(),
					ProcessManageSystem.getMaxPriority(), ProcessManageSystem.getMinInstructionNum(),
					ProcessManageSystem.getMinDataNum());
			String string = new String("I" + futureRequest.getInTime() + "I" + futureRequest.getPriority() + "I"
					+ futureRequest.getInstructionNum() + "I" + futureRequest.getDataNum() + "I\n");
			arrayList.add(string);
		}
		File file = new File(REQUEST_FILE_NAME);
		try {
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (int i = 0; i < arrayList.size(); i++) {
				bufferedWriter.write(arrayList.get(i));
				bufferedWriter.flush();
			}
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int no = 0; no < num; no++) {
			JobControlBlock block = new JobControlBlock();
			block.setJobID(this.findMaxJobID() + 1);
			block.setProcessPriority(ProcessManageSystem.getMaxPriority());
			block.setInstructionNum(ProcessManageSystem.getMinInstructionNum());
			block.setDataNum(ProcessManageSystem.getMinDataNum());
			// 程序集申请外存地址
			int textSize = block.calculateJobTextPageNum();
			Integer textSubMemAddress = this.subMemoryPoint.applyJobBlocks(textSize);
			if (textSubMemAddress == null) {
				return false;
			} else {
				block.setTextSubMemAddress(textSubMemAddress);
			}
			// 数据集申请外存地址
			int dataSize = block.calculateJobTextPageNum();
			Integer dataSubMemAddress = this.subMemoryPoint.applyJobBlocks(dataSize);
			if (dataSubMemAddress == null) {
				return false;
			} else {
				block.setDataSubMemAddress(dataSubMemAddress);
			}
			// 随机生成各类指令
			ArrayList<Integer> instructions = generateInstructions(block.getInstructionNum());
			instructions.remove(0);// 去掉第2条指令
			// 生成一个特定计算指令
			if (no == num - 1) {
				instructions.add(0, this.createCalculate(no, 0));
			} else {
				instructions.add(0, this.createCalculate(no, no + 1));
			}
			// 将指令集写入辅存块。
			for (int i = 0; i < block.calculateJobTextPageNum(); i++) {
				int start = Block.getBlockSize() * i;
				int end = 0;
				if (instructions.size() >= Block.getBlockSize() * (i + 1)) {
					end = Block.getBlockSize();
				} else {
					end = instructions.size() - (Block.getBlockSize() * i);
				}
				ArrayList<Integer> subArray = new ArrayList<Integer>(instructions.subList(start, start + end));
				// 将指令写入文件
				this.initJobInsFile(block.getTextSubMemAddress() + i, block.getJobID(), start, start + end - 1);
				this.instructionsIntoFile(block.getTextSubMemAddress() + i, subArray);
				this.subMemoryPoint.getTheSubBlock(block.getTextSubMemAddress() + i).outputBlockData(subArray);
			}
			// 随机生成各种数据
			ArrayList<Integer> datas = new ArrayList<Integer>();
			for (int i = 0; i < block.getDataNum(); i++) {
				int data = this.random.nextInt();
				if (data < 0) {
					data = i;
				}
				datas.add(data);
			}
			for (int i = 0; i < block.calculateJobDataPageNum(); i++) {
				int start = Block.getBlockSize() * i;
				int end = 0;
				if (datas.size() >= Block.getBlockSize() * (i + 1)) {
					end = Block.getBlockSize();
				} else {
					end = datas.size() - (Block.getBlockSize() * i);
				}
				ArrayList<Integer> subArray = new ArrayList<Integer>(datas.subList(start, start + end));
				this.subMemoryPoint.getTheSubBlock(block.getDataSubMemAddress() + i).outputBlockData(subArray);
			}
			// 两集载入辅存成功，加入后备队列。
			this.jobQueue.add(block);
		}
		return true;
	}

	public Clock getClockPoint() {
		return clockPoint;
	}

	public void setClockPoint(Clock clockPoint) {
		this.clockPoint = clockPoint;
	}

	public SubMemory getSubMemoryPoint() {
		return subMemoryPoint;
	}

	public void setSubMemoryPoint(SubMemory subMemoryPoint) {
		this.subMemoryPoint = subMemoryPoint;
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public ArrayList<FutureRequest> getFutureRequestQueue() {
		return futureRequestQueue;
	}

	public void setFutureRequestQueue(ArrayList<FutureRequest> futureRequestQueue) {
		this.futureRequestQueue = futureRequestQueue;
	}

	public List<JobControlBlock> getJobQueue() {
		return jobQueue;
	}

	public void setJobQueue(ArrayList<JobControlBlock> jobQueue) {
		this.jobQueue = jobQueue;
	}

	public static int getFuturerequestsNum() {
		return FUTUREREQUESTS_NUM;
	}

	public static int getJobqueueMaxsize() {
		return JOBQUEUE_MAXSIZE;
	}

	public static int getNormalmore() {
		return NORMALMORE;
	}

	public ArrayList<JobControlBlock> getRunningJobsQueue() {
		return runningJobsQueue;
	}

	public void setRunningJobsQueue(ArrayList<JobControlBlock> runningJobsQueue) {
		this.runningJobsQueue = runningJobsQueue;
	}

	public Boolean getCreate() {
		return create;
	}

	public void setCreate(Boolean create) {
		this.create = new Boolean(create);
	}

}
