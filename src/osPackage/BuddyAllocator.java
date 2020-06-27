package osPackage;

import java.util.ArrayList;

/*
 * 	64块物理块，伙伴算法最大组数应该是每位对应2的6次方。
 * 	所以free_area数组长为7（0~6）
 * 	0号位图拥有64位：1位=1块
 * 	1号位图拥有32位：1位=2块
 * 	2号位图拥有16位：1位=4块
 * 	3号位图拥有8位：1位=8块
 * 	4号位图拥有4位：1位=16块
 * 	5号位图拥有2位：1位=32块
 * 	6号位图拥有1位：1位=64块
 */
public class BuddyAllocator {
	private final static int FREEAREA_NUM = 6;
	private int pageNum;// 页框数(只能输入2的整数次幂)
	private int lessBorder;// 下边界（可以取到）
	private int moreBorder;// 上边界（不可取到）

	private ArrayList<FreeArea> freeAreas;

	private ArrayList<page> freeBlocks;// 空闲块链表。

	public class page {
		// 物理页框序号是数组下标，不必为一个成员变量
		// 有效位是bitmap标记的，所以也不需要
		// 是否被修改由页表记录，所以不需要
		private int blockNum;// 对应的物理块号
		private int belongID;// 记录该页面属于哪个进程

		public page(int blockNum) {
			super();
			this.blockNum = blockNum;
			this.belongID = -1;
		}

		public int getBlockNum() {
			return blockNum;
		}

		public void setBlockNum(int blockNum) {
			this.blockNum = blockNum;
		}

		public int getBelongID() {
			return belongID;
		}

		public void setBelongID(int belongID) {
			this.belongID = belongID;
		}

	}

	// 空闲链表结点类
	public class FreeArea {
		private int size;// 这个size指的是位图的位数
		private ArrayList<page> freeBlocks;// 空闲块链表。
		private BitMap bitMap;// 对应的位图。

		public FreeArea(int size, ArrayList<page> freeBlocks) {
			super();
			this.size = size;
			this.freeBlocks = freeBlocks;
			this.bitMap = new BitMap(size);// size位的位图
		}

		// 功能：设置指定组的所有页框的页框号
		public void setBlocksProID(int groupNum, int proID) {
			int startIndex = groupNum * (pageNum / this.size);
			for (int i = 0; i < pageNum / this.size; i++) {
				this.freeBlocks.get(startIndex + i).setBelongID(proID);
			}
		}

		// 功能：获取指定组的所有页框的页框号
		public ArrayList<Integer> getBlockNumList(int groupNum) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			int startIndex = groupNum * (pageNum / this.size);
			for (int i = 0; i < pageNum / this.size; i++) {
				list.add(this.freeBlocks.get(startIndex + i).getBlockNum());
			}
			return list;
		}

		// 功能：根据页框号和FreeArea的下标找到对应的组数
		public Integer findGroupIndexByPageNum(int num) {
			return (num - lessBorder) / (pageNum / this.size);
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public BitMap getBitMap() {
			return bitMap;
		}

		public void setBitMap(BitMap bitMap) {
			this.bitMap = bitMap;
		}

		public ArrayList<page> getFreeBlocks() {
			return freeBlocks;
		}

		public void setFreeBlocks(ArrayList<page> freeBlocks) {
			this.freeBlocks = freeBlocks;
		}

	}

	public BuddyAllocator(int less, int pageNum) {
		this.lessBorder = less;
		this.pageNum = pageNum;
		this.moreBorder = this.lessBorder + this.pageNum;
		int size = pageNum;
		this.freeBlocks = new ArrayList<page>();
		for (int i = 0; i < this.pageNum; i++) {
			page p = new page(i + lessBorder);
			this.freeBlocks.add(p);
		}
		// 每个FreeArea都对应64块，每一份都是一份拷贝，在分配和回收时不移动这个链表，只修改位图，在page中只修改所属进程即可。
		this.freeAreas = new ArrayList<BuddyAllocator.FreeArea>();// 数组的个数和位图的个数相同。
		for (int i = 0; i < FREEAREA_NUM; i++) {
			FreeArea freeArea = new FreeArea(size, this.freeBlocks);
			this.freeAreas.add(freeArea);
			size /= 2;// 将size更新。
		}
		this.freeAreas.get(FREEAREA_NUM - 1).getBitMap().alterTheBit(0, true);// 只有最后一组存在，这一组有32块。

	}

	// 申请number块物理页框，返回地是页框号数组（number必须是2的整数次幂）
	public synchronized ArrayList<Integer> alloc_pages(int number, int proID) {
		// 根据输入的数量找到合适的FreeArea
		int No = Integer.MAX_VALUE;// 标记应该申请FreeArea的下标(初始值设为最大，这样如果都没有找到就不会进入二号循环直接返回null)

		for (int i = 0; i < FREEAREA_NUM; i++) {
			if (number == this.calculateGroupSize(i)) {
				// 需要的页框数与当前FreeArea一组拥有的页数相等
				No = i;
				break;
			}
		}

		for (int i = No; i < FREEAREA_NUM; i++) {
			// 之后的每一个free Area组的大小都可以包住number，没必要再做判断。
			Integer integer = this.freeAreas.get(i).getBitMap().findFreeGroupIndex();
			if (integer != null) {
				// 说明当前的freeArea有空闲的组
				// 进行页框分配。
				return this.integrateGroup0(integer, i, number, proID);
			} // 无空闲组则继续寻找
		}

		return null;//bug
	}

	// 功能：计算指定freeArea的组的长度
	public int calculateGroupSize(int freeAreaIndex) {
		return pageNum / this.freeAreas.get(freeAreaIndex).getSize();
	}

	// 功能：整合组（递归）传入：当前组，当前层，目标组长度，用来申请时整理。
	public ArrayList<Integer> integrateGroup0(int groupIndex, int level, int targetSize, int proID) {
		int size = this.calculateGroupSize(level);
		if (size == targetSize) {
			// 判断条件： 如果——当前组与目标组等长； 则——生成对应的物理块编号。
			this.freeAreas.get(level).getBitMap().alterTheBit(groupIndex, false);
			this.freeAreas.get(level).setBlocksProID(groupIndex, proID);
			return this.freeAreas.get(level).getBlockNumList(groupIndex);
		} else {
			// 每层操作：三步
			// 将当前一组拆分为等长的两半
			this.freeAreas.get(level).getBitMap().alterTheBit(groupIndex, false);
			// 后一半挂到上一级
			this.freeAreas.get(level - 1).getBitMap().alterTheBit((groupIndex * 2) + 1, true);
			// 前一半用来进一步整合
			return this.integrateGroup0(groupIndex * 2, level - 1, targetSize, proID);
		}
	}

	// 功能：将传来的页框号的链表回收(链表不一定是2的整数次幂，也不必须是连号的)，返回最后整合到第几层了
	public void free_pages(ArrayList<Integer> blockNumList) {
//		for (int i = 0; i < blockNumList.size(); i++) {
//			if (blockNumList.get(i) == 63) {
//				blockNumList.set(i, 63);//调试bug
//			}
//		}
		// 一个页框一个页框的加入，每加入一个页框就用整合组函数进行整合一次
		for (int i = 0; i < blockNumList.size(); i++) {
			int freeAreaIndex = this.calculateFreeAreaIndex(1);
			// 老组号依据当前要放入的新组号是奇是偶确定
			Integer groupNum0;
			// 再找新组号，设为true
			Integer groupNum1 = this.freeAreas.get(freeAreaIndex).findGroupIndexByPageNum(blockNumList.get(i));
			if (groupNum1 % 2 == 1) {
				// 如果当前组号是奇数，就找左边的组号
				groupNum0 = groupNum1 - 1;
			} else {
				// 如果当前组号是偶数，就找右边的组号
				groupNum0 = groupNum1 + 1;
			}
			if (!this.freeAreas.get(freeAreaIndex).getBitMap().checkTheBit(groupNum0)) {
				// 如果发现这个组号为false，就改为null。
				groupNum0 = null;
			}
			this.freeAreas.get(freeAreaIndex).getBitMap().alterTheBit(groupNum1, true);
			this.integrateGroup1(groupNum1, groupNum0, freeAreaIndex, blockNumList.get(i));
		}
	}

	// 功能：根据size的大小求freeArea的下角标
	public int calculateFreeAreaIndex(int groupSize) {
		int size = groupSize;
		int exp = 0;
		while (size != 1) {
			size /= 2;
			exp++;
		}
		return exp;
	}

	// 功能：整合组（递归），用来释放时整理
	public Integer integrateGroup1(Integer newGroupNum, Integer oldGroupNum, int level, int pageNum) {
		// 判断条件：如果老组号不存在或者两个组号不相邻或者较小者为奇数，就返回，表示整合到底了
		if (oldGroupNum == null) {
			return level;// 返回最后整合到了哪一层
		} else if (Math.abs(newGroupNum - oldGroupNum) > 1) {
			return level;
		} else if (Math.min(newGroupNum, oldGroupNum) % 2 == 1) {
			return level;
		} else {
			// 每层操作：
			// 将两个相邻块合并
			this.freeAreas.get(level).getBitMap().alterTheBit(newGroupNum, false);
			this.freeAreas.get(level).getBitMap().alterTheBit(oldGroupNum, false);
			// 找到下一层应该放入的新组号
			Integer groupNum1 = this.freeAreas.get(level + 1).findGroupIndexByPageNum(pageNum);
			// 寻找下一层的老组号
			Integer groupNum0;
			if (groupNum1 % 2 == 1) {
				// 如果当前组号是奇数，就找左边的组号
				groupNum0 = groupNum1 - 1;
			} else {
				// 如果当前组号是偶数，就找右边的组号
				groupNum0 = groupNum1 + 1;
			}
			if (groupNum0 < this.freeAreas.get(level + 1).getBitMap().getMapSize() && groupNum0 >= 0) {
				if (!this.freeAreas.get(level + 1).getBitMap().checkTheBit(groupNum0)) {
					// 如果发现这个组号为false，就改为null。
					groupNum0 = null;
				}
			} else {
				groupNum0 = null;
			}
			// 将新的位置设为true
			this.freeAreas.get(level + 1).getBitMap().alterTheBit(groupNum1, true);
			// 进行下一层操作
			return this.integrateGroup1(groupNum1, groupNum0, level + 1, pageNum);
		}
	}

	public int getLessBorder() {
		return lessBorder;
	}

	public void setLessBorder(int lessBorder) {
		this.lessBorder = lessBorder;
	}

	public int getMoreBorder() {
		return moreBorder;
	}

	public void setMoreBorder(int moreBorder) {
		this.moreBorder = moreBorder;
	}

	public ArrayList<FreeArea> getFreeAreas() {
		return freeAreas;
	}

	public void setFreeAreas(ArrayList<FreeArea> freeAreas) {
		this.freeAreas = freeAreas;
	}

	public static int getFreeareaNum() {
		return FREEAREA_NUM;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

}
