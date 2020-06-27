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
 * @apiNote �������һ��δ������
 * @apiNote ����һ��δ����������һ����ҵ���ƿ�<br>
 *          ������ҵ���ƿ��ָ��������ÿ��ָ��<br>
 *          ��������뵽�ļ��������ɿ�����ҵ�ĳ��򼯺����ݼ�<br>
 * @apiNote �������һ��ָ�<br>
 *          1��������ɷô�ָ�<br>
 *          2���漴��������ָ�<br>
 *          3������������ָ�<br>
 *          4���������ϵͳ����ָ�<br>
 *          5��������ɼ���ָ�<br>
 *          6�����������ָͨ�<br>
 */
public class JobManageSystem {
	private final static String INPUT_DIRECT = new String(System.getProperty("user.dir") + "\\input");
	/**
	 * �����ļ�����������ʱ�� ���ȼ� ���򼯵�ָ���� ��ҵ������˫�����ݸ���
	 */
	private final static String REQUEST_FILE_NAME = new String(System.getProperty("user.dir") + "\\request.txt");
	/**
	 * һ�����������ӵ�е�ָ����
	 */
	private final static int BAR_SIZE = 5;
	/**
	 * ��ҵָ���ı��ļ����ļ��У��洢���ܿ�������ҵ����
	 */
	private final static String JOB_INSTRUCTION_FILENAME = System.getProperty("user.dir") + "\\JobsInstruction";
	/**
	 * ��ָͨ������ĸ�����ϵͳ����ָ��ļ���
	 */
	private final static int NORMALMORE = 4;
	/**
	 * δ������������������
	 */
	private final static int FUTUREREQUESTS_NUM = 5;
	/**
	 * ��ҵ�󱸶��е����ߴ�
	 */
	private final static int JOBQUEUE_MAXSIZE = 8;
	/**
	 * ���������������
	 */
	private Random random;
	/**
	 * ʱ��ָ��
	 */
	private Clock clockPoint;
	/**
	 * ����ָ��
	 */
	private SubMemory subMemoryPoint;
	/**
	 * δ����������
	 */
	private ArrayList<FutureRequest> futureRequestQueue;
	/**
	 * ��ҵ�󱸶���
	 */
	private List<JobControlBlock> jobQueue;
	/**
	 * �Ѿ����������̵���ҵ���뿪�󱸶��н����������ҵ����
	 */
	private ArrayList<JobControlBlock> runningJobsQueue;
	/**
	 * ����û�������ǰ���¶��ļ���ť�����ǽ�������Ϊfalse��Ȼ��ѡ�����Զ������ļ������Ƕ��������ļ��ķ�ʽ������ҵ��
	 */
	private Boolean create;

	/**
	 * @param clock ��ʱ�Ӵ��룬������⵽��������ʹ�á�
	 * @apiNote ���캯��
	 * @implNote ����һ��δ���������
	 */
	public JobManageSystem(Clock clock, SubMemory submemoryPoint) {
		super();
		this.setClockPoint(clock);
		this.setSubMemoryPoint(submemoryPoint);
		this.random = new Random();
		this.futureRequestQueue = new ArrayList<FutureRequest>();
		this.jobQueue = Collections.synchronizedList(new ArrayList<JobControlBlock>());
		this.setRunningJobsQueue(new ArrayList<JobControlBlock>());
		// �������һ��δ����������
		this.futureRequestQueueRandly();
		// �����ļ���
		File file = new File(JOB_INSTRUCTION_FILENAME);
		if (!file.exists()) {// ����ļ��в�����
			file.mkdir();// �����ļ���
		}
		this.setCreate(new Boolean(true));// Ĭ�������ļ�
		File input = new File(INPUT_DIRECT);
		if (!input.exists()) {// ����ļ��в�����
			input.mkdir();// �����ļ���
		}
	}

	/**
	 * ����ɾ���ļ���
	 * 
	 * @param dirPath �ļ���·��
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
	 * ��input�ļ����е�����ȫ�����Ƶ�ϵͳ�Ķ�Ӧλ����
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
	 * @apiNote �������һ��δ����������
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
	 * @apiNote ���������ļ�
	 */
	public boolean readFutureRequest() {
		synchronized (futureRequestQueue) {
			this.futureRequestQueue.clear();
			ArrayList<String> arrayList = new ArrayList<String>();
			// �����������ļ�
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
			// �ַ���������Ϣ�м���I�ַ��ָ
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
									count++;// ˵��ȥ��һ��ֵ�ˡ�
									break;
								case 1:
									priority = Integer.valueOf(arrayList.get(i).substring(j + 1, j2), 10);
									count++;// ˵�����ȼ�Ҳȡ��
									break;
								case 2:
									instNum = Integer.valueOf(arrayList.get(i).substring(j + 1, j2), 10);
									count++;// ˵��ָ����Ҳȡ��
									break;
								case 3:
									dataNum = Integer.valueOf(arrayList.get(i).substring(j + 1, j2), 10);
									count++;// ������Ҳȡ��
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
	 * @apiNote ��δ���������а��յ���ʱ���С��������
	 */
	public void sortRequestQueueByIntime() {
		RequestsIntimeCompare compare = new RequestsIntimeCompare();
		if (!this.futureRequestQueue.isEmpty()) {
			Collections.sort(this.futureRequestQueue, compare);
		}
	}

	/**
	 * @apiNote ����һ��δ�����󣬲�����δ���������С�
	 * @param intime ����ʱ�䣬һ��Ҫ�õ�ǰʱ��ʱ�䡣
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
	 * @return �ҵ��������ҵID
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
	 * @apiNote ����δ�����󴴽���ҵ���ƿ飬��������������������
	 * @param request һ��δ������
	 * @return true �ɹ�������ҵ������󱸶���<br>
	 *         false �󱸶��еĳߴ粻��������ҵ�����㵼���޷�������ҵ<br>
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
		// ������������ַ
		int textSize = block.calculateJobTextPageNum();
		Integer textSubMemAddress = this.subMemoryPoint.applyJobBlocks(textSize);
		if (textSubMemAddress == null) {
			return false;
		} else {
			block.setTextSubMemAddress(textSubMemAddress);
		}
		// ���ݼ���������ַ
		int dataSize = block.calculateJobTextPageNum();
		Integer dataSubMemAddress = this.subMemoryPoint.applyJobBlocks(dataSize);
		if (dataSubMemAddress == null) {
			return false;
		} else {
			block.setDataSubMemAddress(dataSubMemAddress);
		}
		if (this.create) {
			// ������ɸ���ָ��
			ArrayList<Integer> instructions = generateInstructions(block.getInstructionNum());
			// ��ָ�д�븨��顣
			for (int i = 0; i < block.calculateJobTextPageNum(); i++) {
				int start = Block.getBlockSize() * i;
				int end = 0;
				if (instructions.size() >= Block.getBlockSize() * (i + 1)) {
					end = Block.getBlockSize();
				} else {
					end = instructions.size() - (Block.getBlockSize() * i);
				}
				ArrayList<Integer> subArray = new ArrayList<Integer>(instructions.subList(start, start + end));
				// ��ָ��д���ļ�
				this.initJobInsFile(block.getTextSubMemAddress() + i, block.getJobID(), start, start + end - 1);
				this.instructionsIntoFile(block.getTextSubMemAddress() + i, subArray);
				this.subMemoryPoint.getTheSubBlock(block.getTextSubMemAddress() + i).outputBlockData(subArray);
			}
			// ������ɸ�������
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
		// �������븨��ɹ�������󱸶��С�
		this.jobQueue.add(block);
		return true;
	}

	/**
	 * @apiNote ������ҵ��¼�ı��ļ���
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
	 * @apiNote ����ҵָ�����ݼ�¼����Ӧ�ı��ļ��С�
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
	 * @param translator  IR�Ĵ����࣬�����õģ���������ģ���ΪIR�Ĵ����ܽ���ָ�Ϊ�����ô��롣
	 * @param instruction Ҫ������ָ��Ԫ����
	 * @return �����õ�ָ�����ġ�
	 */
	public String translateInstruction(InstructionRegister translator, Integer instruction) {
		translator.updateData(0, instruction);
		String string = new String();
		switch (translator.getInstructionID()) {
		case ACCESS_MEMORY:
			string += "--|�ô�|--" + "[Register-" + translator.getRegisterIDList().get(0) + "][Offset-"
					+ translator.getAccessOffset() + "]";
			return string;
		case CALCULATE:
			string += "--|����|--" + "[Register-" + translator.getRegisterIDList().get(0) + "][Register-"
					+ translator.getRegisterIDList().get(1) + "]";
			return string;
		case INPUT:
			string += "--|����|--" + "[DeviceAddress-" + translator.getDeviceAddress() + "]";
			return string;
		case OUTPUT:
			string += "--|���|--" + "[DeviceAddress-" + translator.getDeviceAddress() + "]";
			return string;
		case SYSTEM_CALL:
			string += "--|ϵͳ|--" + "[SystemCallID-" + translator.getCallID() + "]";
			return string;
		case JUMP:
			string += "--|��ת|--" + "[Offset" + translator.getJumpOffset() + "]";
			return string;
		case NORMAL:
			string += "--|��ͨ|--";
			return string;
		default:
			string += "--|δ֪|--";
			return string;
		}
	}

	/**
	 * @apiNote ����ָ��������ָ����
	 * @implNote ��תָ�����תһ��ҳ��Ĵ�С�������תָ�����ת�����ٶ��������޸ġ�
	 * @param insNum Ҫ���ɵ�ָ������
	 * @return ���ɵ�ָ�����
	 */
	public ArrayList<Integer> generateInstructions(int insNum) {
		ArrayList<Integer> instructions = new ArrayList<Integer>();
		int baseBlockNum = insNum / Block.getBlockSize();// ָ����װ����ҳ����
		// �����ܹ�256 �� 3 + 32��ָ�������29������ִ�еķ���תָ�3����תָ�
		int realInsNum = insNum - Block.getBlockSize() * baseBlockNum - baseBlockNum;
		int barNum = this.random.nextInt(realInsNum / BAR_SIZE) + 1;// ��������3��bar�����ǽ�ֻ������0~2��������+1
		for (int i = 0; i < barNum; i++) {
			instructions.addAll(this.createOneBar());
		}
		for (int i = 0; i < realInsNum - (barNum * BAR_SIZE); i++) {
			// �������0~24��Χ�ڵ�����1~5������ָ�-2��Ϊ���ų�0��6�õ�����ָ�������
			int isNormal = this.random.nextInt(NORMALMORE + 1);
			int index = this.random.nextInt(instructions.size());
			if (isNormal > 0) {
				// ����0���ϵ�����ȫ������Ϊ��ָͨ�
				instructions.add(index, this.createNormal());
			} else {
				instructions.add(index, this.createSystemCall());
			}
		}
		// ��29������ת����ָ�����ҵ�һ��������Ⱦ��롣
		int deltaJumpDist = realInsNum / (baseBlockNum + 1);
		// ������תָ����±ꡣ
		int jumpIndex = 0;
		for (int i = 0; i < baseBlockNum; i++) {
			jumpIndex += deltaJumpDist;
			// ��������±�λ��ʵ��ִ��ָ��֮ǰ֮��֮��
			instructions.add(jumpIndex, this.createJump());
			// ��תָ������ɵ���ָͨ����256��
			int normalInsNum = Block.getBlockSize();
			for (int j = 0; j < normalInsNum; j++) {
				instructions.add(jumpIndex + 1 + j, this.createNormal());
			}
			jumpIndex += normalInsNum;
		}
		return instructions;
	}

	/**
	 * @apiNote ����ѭ�����δ��������У�����е��ڵ�δ������ʹ���Ϊ��ҵ��������ҵ�󱸶��С�
	 * @return true ��������ҵ<br>
	 *         false ������ҵʧ��<br>
	 */
	public boolean checkFutureRequest() {
		synchronized (futureRequestQueue) {
			if (this.futureRequestQueue.isEmpty()) {
				return false;
			} else {
				for (FutureRequest futureRequest : futureRequestQueue) {
					if (this.clockPoint.getSecondNum() >= futureRequest.getInTime()) {
						synchronized (this.jobQueue) {
							// �����������ʱ�䣬�ʹ�����ҵ��
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
	 * @return �����ɹ���һ����תָ��
	 */
	public Integer createJump() {
		int ID = 7;
		// �����Ħ�offset���������offset����һ����������ȡ��Ҫ�ͻ�ַ��Ӷ������ַҳ��ƴ�ӡ�
		int deltaOffset = Block.getBlockSize() - 1;
		// ����Ҫ��256��ָ�����ÿ��ȡ��תָ���PC�����Զ�+1��������ȥһ���������Ҫ�ľͺ����޸ġ�
		int data = 0;
		data += ID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum());
		data += deltaOffset * MemoryManageSystem.getUnitSize();
		return new Integer(data);
	}

	/**
	 * @return �����ɹ���һ��ϵͳ����ָ��
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
	 * @return �����ɹ���һ���ô�ָ�
	 */
	public Integer createAccessMemory() {
		int ID = 5;
		int registerID = this.random.nextInt(Monitor.getRegisterNum());
		// �����Ħ�offset���������offset����һ����������ȡ��Ҫ�ͻ�ַ��Ӷ������ַҳ��ƴ�ӡ�
		// ������������С�������������Ͳ��ᳬ��Χ��������20��������Զ�޷����ʣ������ڴ��Ժ��Ż���
		int deltaOffset = this.random.nextInt(ProcessManageSystem.getMinDataNum());
		int data = 0;
		data += ID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum());
		data += registerID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum()
				- InstructionRegister.getRegisteridBitnum());
		data += deltaOffset * MemoryManageSystem.getUnitSize();
		return new Integer(data);
	}

	/**
	 * @implNote ������豸������һҳ
	 * @return �����ɹ���һ������ָ��
	 */
	public Integer createInput() {
		int ID = 1;
		// ������������ļ�����ַ
		int deviceAdd = this.random.nextInt(MemoryManageSystem.getDeviceBlocknum())
				+ MemoryManageSystem.getDeviceStartAddress();
		int data = 0;
		data += ID << (InstructionRegister.getBitnum() - InstructionRegister.getIdBitnum());
		data += deviceAdd;
		return new Integer(data);
	}

	/**
	 * @implNote ������豸������һҳ
	 * @return �����ɹ���һ�����ָ��
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
		while (registerID1 == registerID2) {// ֻҪ�����Ĵ���ID��ͬ�����ٴ����ɣ�ֱ������ȡ�
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
	 * @param registerIDA Դ�Ĵ������
	 * @param registerIDB Ŀ�ļĴ������
	 * @return ���ɵļ���ָ��
	 */
	public Integer createCalculate(int registerIDA, int registerIDB) {
		int ID = 4;
		if (registerIDA >= Monitor.getRegisterNum() || registerIDB >= Monitor.getRegisterNum()
				|| registerIDA == registerIDB) {
			return null;
		}
		int registerID1 = registerIDB;// Ŀ�ļĴ���
		int registerID2 = registerIDA;// Դ�Ĵ���
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
	 * @apiNote ����һ���������������߼��ĳ����
	 * @return
	 */
	public ArrayList<Integer> createOneBar() {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		// �ȷô棬����һ������
		arrayList.add(this.createAccessMemory());
		// �����û�������������
		arrayList.add(this.createInput());
		// �����ݽ��м���
		arrayList.add(this.createCalculate());
		// ��������д���ڴ��еı���
		arrayList.add(this.createAccessMemory());
		// ���ڴ��еļ�����������û�
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
			// ������������ַ
			int textSize = block.calculateJobTextPageNum();
			Integer textSubMemAddress = this.subMemoryPoint.applyJobBlocks(textSize);
			if (textSubMemAddress == null) {
				return false;
			} else {
				block.setTextSubMemAddress(textSubMemAddress);
			}
			// ���ݼ���������ַ
			int dataSize = block.calculateJobTextPageNum();
			Integer dataSubMemAddress = this.subMemoryPoint.applyJobBlocks(dataSize);
			if (dataSubMemAddress == null) {
				return false;
			} else {
				block.setDataSubMemAddress(dataSubMemAddress);
			}
			// ������ɸ���ָ��
			ArrayList<Integer> instructions = generateInstructions(block.getInstructionNum());
			instructions.remove(0);// ȥ����2��ָ��
			// ����һ���ض�����ָ��
			if (no == num - 1) {
				instructions.add(0, this.createCalculate(no, 0));
			} else {
				instructions.add(0, this.createCalculate(no, no + 1));
			}
			// ��ָ�д�븨��顣
			for (int i = 0; i < block.calculateJobTextPageNum(); i++) {
				int start = Block.getBlockSize() * i;
				int end = 0;
				if (instructions.size() >= Block.getBlockSize() * (i + 1)) {
					end = Block.getBlockSize();
				} else {
					end = instructions.size() - (Block.getBlockSize() * i);
				}
				ArrayList<Integer> subArray = new ArrayList<Integer>(instructions.subList(start, start + end));
				// ��ָ��д���ļ�
				this.initJobInsFile(block.getTextSubMemAddress() + i, block.getJobID(), start, start + end - 1);
				this.instructionsIntoFile(block.getTextSubMemAddress() + i, subArray);
				this.subMemoryPoint.getTheSubBlock(block.getTextSubMemAddress() + i).outputBlockData(subArray);
			}
			// ������ɸ�������
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
			// �������븨��ɹ�������󱸶��С�
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
