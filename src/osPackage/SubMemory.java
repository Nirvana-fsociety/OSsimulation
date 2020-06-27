package osPackage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import osPackage.PCBQueue.Compare;
import osPackage.PCBQueue.FreePageRegion;

/**
 * @implNote 关于外存文件区空间管理：需要实现位图管理法
 *
 */
public class SubMemory {
	private final static String SUBMAINMEM_DIRECTRY_PATH = new String("SubMemory");// 辅存文件夹路径

	private final static int EXCHANGE_BLOCKNUM = 128;// 外存交换区有128块
	private final static int FILE_BLOCKNUM = 1920;// 文件区块数

	private int exchangeAreaStartAddress;// 交换区的起始地址
	private int usedExchBlockNum;// 交换区已使用块数（从头开始用，所以下一个进程要来申请的时候应该直接用尾部的）
	private BitMap exchangeBitmap;// 交换区中的位示图
	private ArrayList<SubBlock> exchangeArea;// 交换区

	private int fileAreaStartAddress;// 文件区的起始地址
	private int userdFileBlockNum;// 文件区已使用块数
	private BitMap fileBitmap;// 超级块中的位示图
	private ArrayList<SubBlock> fileArea;// 文件区

	public SubMemory(int fileAreaStartSubAdd, int exchangeAreaStartSubAdd) {
		super();

		File file = new File(SUBMAINMEM_DIRECTRY_PATH);
		if (!file.exists()) {// 如果文件夹不存在
			file.mkdir();// 创建文件夹
		}

		this.exchangeAreaStartAddress = exchangeAreaStartSubAdd;
		this.usedExchBlockNum = 0;
		this.exchangeArea = new ArrayList<SubBlock>();

		for (int i = 0; i < EXCHANGE_BLOCKNUM; i++) {
			SubBlock block = new SubBlock(SubBlock.takeTackIDFromSubAddress(this.exchangeAreaStartAddress + i),
					SubBlock.takeSectorIDFromSubAddress(this.exchangeAreaStartAddress + i));
			this.exchangeArea.add(block);
		}

		// 设置交换区位示图
		this.setExchangeBitmap(new BitMap(EXCHANGE_BLOCKNUM));

		this.fileAreaStartAddress = fileAreaStartSubAdd;
		this.userdFileBlockNum = 2;// 引导块占0号，超级块占1号
		this.fileArea = new ArrayList<SubBlock>();

		for (int i = 0; i < FILE_BLOCKNUM; i++) {
			SubBlock block = new SubBlock(SubBlock.takeTackIDFromSubAddress(this.fileAreaStartAddress + i),
					SubBlock.takeSectorIDFromSubAddress(this.fileAreaStartAddress + i));
			this.fileArea.add(block);
		}

		// 设置文件区位示图
		this.setFileBitmap(new BitMap(FILE_BLOCKNUM));
		this.fileBitmap.alterTheBit(MemoryManageSystem.getGuideAddress(), true);
		this.fileBitmap.alterTheBit(MemoryManageSystem.getSuperblockAddress(), true);
	}

	// 获取指定外存地址的一块外存块
	public SubBlock getTheSubBlock(int subAdd) {
		if (subAdd < this.fileAreaStartAddress + FILE_BLOCKNUM) {
			return this.fileArea.get(subAdd);
		} else if (subAdd < this.exchangeAreaStartAddress + EXCHANGE_BLOCKNUM) {
			return this.exchangeArea.get(subAdd - FILE_BLOCKNUM);
		} else {
			return null;
		}
	}

	/**
	 * @apiNote 根据指定要申请的块数，获取连续的交换区
	 * @param size 要申请的连续的交换区块数
	 * @return 获取的交换区的首地址。
	 */
	public Integer occupyNewExchangeArea(int size) {
		// 因为FreePageRegion是PCB队列类中的一个公有类，所以只能从PCB队列类中提取。
		ArrayList<FreePageRegion> list = new ArrayList<PCBQueue.FreePageRegion>();
		// 统计空闲区有哪些（采用了PCB列表中的最佳适应算法）
		int start = MemoryManageSystem.getExchangeStartAddress();
		int end = MemoryManageSystem.getExchangeStartAddress() + MemoryManageSystem.getExchangeBlocknum();
		for (int i = MemoryManageSystem.getExchangeStartAddress(); i < MemoryManageSystem.getExchangeStartAddress()
				+ MemoryManageSystem.getExchangeBlocknum();) {
			if (!this.exchangeBitmap.checkTheBit(i - this.exchangeAreaStartAddress)) {
				// 找到第一个无效扇区的地址
				start = i;
				end = MemoryManageSystem.getExchangeStartAddress() + MemoryManageSystem.getExchangeBlocknum();
				for (int j = start; j < MemoryManageSystem.getExchangeStartAddress()
						+ MemoryManageSystem.getExchangeBlocknum(); j++) {
					if (this.exchangeBitmap.checkTheBit(j - this.exchangeAreaStartAddress)) {
						// 找到自start开始的第一个已被占用页
						end = j;
						break;
					}
				}
				if (end - start >= size) {
					list.add(new FreePageRegion(start, end - start));
				}
				i = end + 1;
			} else {
				i++;
			}
		}
		if (list.size() == 0) {// 说明没有空闲区
			return null;
		} else {// 说明有空闲区
			Compare compare = new Compare();
			Collections.sort(list, compare);// 按照访问字段从小到大排序
			for (FreePageRegion freePageRegion : list) {
				if (freePageRegion.num >= size) {
					// 空闲区足够大了
					this.exchangeBitmap.occupyBitmap(freePageRegion.pageID - this.exchangeAreaStartAddress, size);
					this.usedExchBlockNum += size;
					return freePageRegion.pageID;
				}
			}
		}
		return null;// 没有足够大的空闲区
	}

	/**
	 * @apiNote 根据起始外存地址，交换区大小解除一段交换区的占用
	 * @param startSubAdd 要释放区域外存起始地址。
	 * @param size        释放区域长度
	 * @return true 成功释放
	 * @return false 因为传入的起始地址在文件区不在交换区，或者尺寸超过已使用的交换区块数而释放失败。
	 */
	public boolean releaseOldExchangeArea(int startSubAdd, int size) {
		if (startSubAdd < this.exchangeAreaStartAddress) {
			return false;// 地址在交换区下
		} else if (size > this.usedExchBlockNum) {
			return false;// 要释放的块数态多
		} else {
			// 求得要释放的区域是从位图的哪一个位置开始
			int startIndex = startSubAdd - this.exchangeAreaStartAddress;
			this.exchangeBitmap.releaseBitmap(startIndex, size);
			return true;// 成功释放
		}
	}

	/**
	 * @apiNote 根据传入的尺寸申请占用连续的一段空间
	 * @implNote 为了使用PCB队列类的最佳适应算法，将本来属于PCB队列的类内类改为静态。
	 * @param size
	 * @return 获得空间的外存首地址
	 */
	public Integer applyJobBlocks(int size) {
		ArrayList<FreePageRegion> list = new ArrayList<PCBQueue.FreePageRegion>();
		// 统计空闲区有哪些（采用了PCB列表中的最佳适应算法）
		int start = MemoryManageSystem.getJobStartAddress();
		int end = MemoryManageSystem.getJobStartAddress() + MemoryManageSystem.getJobBlocknum();
		for (int i = MemoryManageSystem.getJobStartAddress(); i < MemoryManageSystem.getJobStartAddress()
				+ MemoryManageSystem.getJobBlocknum();) {
			if (!this.fileBitmap.checkTheBit(i - this.fileAreaStartAddress)) {
				// 找到第一个无效扇区的地址
				start = i;
				end = MemoryManageSystem.getJobStartAddress() + MemoryManageSystem.getJobBlocknum();
				for (int j = start; j < MemoryManageSystem.getJobStartAddress()
						+ MemoryManageSystem.getJobBlocknum(); j++) {
					if (this.fileBitmap.checkTheBit(j - this.fileAreaStartAddress)) {
						// 找到自start开始的第一个有效页
						end = j;
						break;
					}
				}
				if (end - start >= size) {
					list.add(new FreePageRegion(start, end - start));
				}
				i = end + 1;// 无论录不录进记录数组中都要递进。
			} else {
				i++;
			}
		}
		if (list.size() == 0) {// 说明没有空闲区
			return null;
		} else {// 说明有空闲区
			Compare compare = new Compare();
			Collections.sort(list, compare);// 按照访问字段从小到大排序
			for (FreePageRegion freePageRegion : list) {
				if (freePageRegion.num >= size) {
					// 空闲区足够大了
					this.fileBitmap.occupyBitmap(freePageRegion.pageID - this.fileAreaStartAddress, size);
					this.userdFileBlockNum += size;
					return freePageRegion.pageID;
				}
			}
		}
		return null;// 没有足够大的空闲区
	}

	public int getExchangeAreaStartPageNum() {
		return exchangeAreaStartAddress;
	}

	public void setExchangeAreaStartPageNum(int exchangeAreaStartPageNum) {
		this.exchangeAreaStartAddress = exchangeAreaStartPageNum;
	}

	public ArrayList<SubBlock> getExchangeArea() {
		return exchangeArea;
	}

	public void setExchangeArea(ArrayList<SubBlock> exchangeArea) {
		this.exchangeArea = exchangeArea;
	}

	public static String getSubmainmemDirectryPath() {
		return SUBMAINMEM_DIRECTRY_PATH;
	}

	public int getUsedBlockNum() {
		return usedExchBlockNum;
	}

	public void setUsedBlockNum(int usedBlockNum) {
		this.usedExchBlockNum = usedBlockNum;
	}

	public int getExchangeAreaStartAddress() {
		return exchangeAreaStartAddress;
	}

	public void setExchangeAreaStartAddress(int exchangeAreaStartAddress) {
		this.exchangeAreaStartAddress = exchangeAreaStartAddress;
	}

	public int getUsedExchBlockNum() {
		return usedExchBlockNum;
	}

	public void setUsedExchBlockNum(int usedExchBlockNum) {
		this.usedExchBlockNum = usedExchBlockNum;
	}

	public static int getExchangeBlocknum() {
		return EXCHANGE_BLOCKNUM;
	}

	public int getFileAreaStartAddress() {
		return fileAreaStartAddress;
	}

	public void setFileAreaStartAddress(int fileAreaStartAddress) {
		this.fileAreaStartAddress = fileAreaStartAddress;
	}

	public int getUserdFileBlockNum() {
		return userdFileBlockNum;
	}

	public void setUserdFileBlockNum(int userdFileBlockNum) {
		this.userdFileBlockNum = userdFileBlockNum;
	}

	public ArrayList<SubBlock> getFileArea() {
		return fileArea;
	}

	public void setFileArea(ArrayList<SubBlock> fileArea) {
		this.fileArea = fileArea;
	}

	public static int getFileBlocknum() {
		return FILE_BLOCKNUM;
	}

	public BitMap getFileBitmap() {
		return fileBitmap;
	}

	public void setFileBitmap(BitMap fileBitmap) {
		this.fileBitmap = fileBitmap;
	}

	public BitMap getExchangeBitmap() {
		return exchangeBitmap;
	}

	public void setExchangeBitmap(BitMap exchangeBitmap) {
		this.exchangeBitmap = exchangeBitmap;
	}

}
