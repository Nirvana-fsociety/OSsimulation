package osPackage;

import java.io.File;
import java.util.ArrayList;

public class MainMemory {
	private final static String MAINMEM_DIRECTRY = new String("MainMemory");// 主存文件夹路径

	private final static int SYSTEM_BLOCKNUM = 32;// 系统区块数
	private final static int USER_BLOCKNUM = 32;// 用户区块数

	private final static int BUFFER_BLOCKNUM = 1;// 缓冲区的个数

	private int systemAreaStartPageNum;// 系统区起始页号
	private int bufferAreaPageNum;// 缓冲区页号
	private int pageTablePageNum;// 页表页号（正好放一页）
	private int pcbQueueStartPageNum;// PCB队列起始页号
	private int userAreaStartPageNum;// 用户区起始页号

	private ArrayList<MainBlock> systemArea;// 系统区物理块组
	private ArrayList<MainBlock> userArea;// 用户区物理块组

	public MainMemory(int systemAreaStartPageNum, int bufferAreaPageNum, int userAreaStartPageNum) {
		super();
		this.systemAreaStartPageNum = systemAreaStartPageNum;
		this.bufferAreaPageNum = bufferAreaPageNum;
		this.pageTablePageNum += this.bufferAreaPageNum + BUFFER_BLOCKNUM + 1;// 页表紧跟缓冲区后
		this.pcbQueueStartPageNum += this.pageTablePageNum + 1;// PCB队列紧跟页表后
		this.userAreaStartPageNum = userAreaStartPageNum;

		File file = new File(MAINMEM_DIRECTRY);
		if (!file.exists()) {// 如果文件夹不存在
			file.mkdir();// 创建文件夹
		}

		this.systemArea = new ArrayList<MainBlock>();
		this.userArea = new ArrayList<MainBlock>();

		for (int i = 0; i < SYSTEM_BLOCKNUM; i++) {
			MainBlock block1 = new MainBlock(i);
			this.systemArea.add(block1);
		}

		for (int i = SYSTEM_BLOCKNUM; i < SYSTEM_BLOCKNUM + USER_BLOCKNUM; i++) {
			MainBlock block2 = new MainBlock(i);
			this.userArea.add(block2);
		}

	}

	// 功能:获取指定物理块号的一块内存块
	public MainBlock getTheMainBlock(int blockNum) {
		if (blockNum < SYSTEM_BLOCKNUM + this.systemAreaStartPageNum) {
			return this.systemArea.get(blockNum);
		} else if (blockNum < USER_BLOCKNUM + this.userAreaStartPageNum) {
			return this.userArea.get(blockNum - SYSTEM_BLOCKNUM);
		} else {
			return null;
		}
	}

	// 功能：将一个双字节字写入内存
	public boolean writeWordIntoMemory(Address physicAddress, Integer word) {
		MainBlock block = this.getTheMainBlock(physicAddress.getPageID());
		if (block == null) {
			return false;
		} else {
			ArrayList<Integer> arrayList = new ArrayList<Integer>(block.inputBlockData());
			if (physicAddress.getOffset() / MemoryManageSystem.getUnitSize() < arrayList.size()) {
				arrayList.remove(physicAddress.getOffset() / MemoryManageSystem.getUnitSize());
			}
			arrayList.add(physicAddress.getOffset() / MemoryManageSystem.getUnitSize(), word);
			block.outputBlockData(arrayList);
			return true;
		}
	}

	// 功能：从内存指定位置读一个字
	public Integer readWordFromMemory(Address physicAddress) {
		MainBlock block = this.getTheMainBlock(physicAddress.getPageID());
		if (block == null) {
			return null;
		} else {
			ArrayList<Integer> arrayList = new ArrayList<Integer>(block.inputBlockData());
			if (physicAddress.getOffset() / MemoryManageSystem.getUnitSize() < arrayList.size()) {
				return arrayList.get(physicAddress.getOffset() / MemoryManageSystem.getUnitSize());
			} else {
				return null;
			}
		}
	}

	public int getSystemAreaStartPageNum() {
		return systemAreaStartPageNum;
	}

	public void setSystemAreaStartPageNum(int systemAreaStartPageNum) {
		this.systemAreaStartPageNum = systemAreaStartPageNum;
	}

	public int getBufferAreaPageNum() {
		return bufferAreaPageNum;
	}

	public void setBufferAreaPageNum(int bufferAreaPageNum) {
		this.bufferAreaPageNum = bufferAreaPageNum;
	}

	public int getUserAreaStartPageNum() {
		return userAreaStartPageNum;
	}

	public void setUserAreaStartPageNum(int userAreaStartPageNum) {
		this.userAreaStartPageNum = userAreaStartPageNum;
	}

	public ArrayList<MainBlock> getSystemArea() {
		return systemArea;
	}

	public void setSystemArea(ArrayList<MainBlock> systemArea) {
		this.systemArea = systemArea;
	}

	public ArrayList<MainBlock> getUserArea() {
		return userArea;
	}

	public void setUserArea(ArrayList<MainBlock> userArea) {
		this.userArea = userArea;
	}

	public static int getSystemBlocknum() {
		return SYSTEM_BLOCKNUM;
	}

	public static int getUserBlocknum() {
		return USER_BLOCKNUM;
	}

	public static String getMainmemDirectry() {
		return MAINMEM_DIRECTRY;
	}

	public int getPageTablePageNum() {
		return pageTablePageNum;
	}

	public void setPageTablePageNum(int pageTablePageNum) {
		this.pageTablePageNum = pageTablePageNum;
	}

	public int getPcbQueueStartPageNum() {
		return pcbQueueStartPageNum;
	}

	public void setPcbQueueStartPageNum(int pcbQueueStartPageNum) {
		this.pcbQueueStartPageNum = pcbQueueStartPageNum;
	}

	public static int getBufferBlocknum() {
		return BUFFER_BLOCKNUM;
	}

}
