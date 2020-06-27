package osPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PCBQueue {
	private final static int SIZE = 8;// PCB最大个数

	private List<ProcessControlBlock> blankPCBList;// PCB空闲队列
	private List<ProcessControlBlock> readyPCBList;// PCB就绪队列
	private List<ProcessControlBlock> blockPCBList;// PCB阻塞队列
	private List<ProcessControlBlock> runningPCB;// 运行PCB(只能有一个)

	private List<ProcessControlBlock> pendBlockList;// 挂起等待队列
	private List<ProcessControlBlock> pendReadyList;// 挂起就绪队列

	public PCBQueue(int headPageID) {
		this.blankPCBList = Collections.synchronizedList(new ArrayList<ProcessControlBlock>());

		Address beginAdd = new Address(Address.getPhysicPageBitnum(), Address.getOffsetBitnum(), headPageID, 0);
		for (int i = 0; i < SIZE; i++) {
			ProcessControlBlock block = new ProcessControlBlock(-1,
					beginAdd.getData() + (i * ProcessControlBlock.getPcbSize() * MemoryManageSystem.getUnitSize()));
			this.blankPCBList.add(block);
		}
		this.readyPCBList = Collections.synchronizedList(new ArrayList<ProcessControlBlock>());
		this.blockPCBList = Collections.synchronizedList(new ArrayList<ProcessControlBlock>());
		this.runningPCB = Collections.synchronizedList(new ArrayList<ProcessControlBlock>());

		this.pendBlockList = Collections.synchronizedList(new ArrayList<ProcessControlBlock>());
		this.pendReadyList = Collections.synchronizedList(new ArrayList<ProcessControlBlock>());
	}

	public static class PriorityLessThanCompare implements Comparator<ProcessControlBlock> {
		@Override
		public int compare(ProcessControlBlock o1, ProcessControlBlock o2) {
			if (o1.getControlInfo().getProcessPriority() <= o2.getControlInfo().getProcessPriority()) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	/**
	 * @apiNote 将就绪队列按照优先级从大到小排序
	 */
	public void sortReadyQueueByPriority() {
		PriorityLessThanCompare compare = new PriorityLessThanCompare();
		if (!this.readyPCBList.isEmpty()) {
			Collections.sort(this.readyPCBList, compare);
		}
	}

	/**
	 * @apiNote 将挂起就绪队列按照优先级从大到小排序
	 */
	public void sortPendReadyQueueByPriority() {
		PriorityLessThanCompare compare = new PriorityLessThanCompare();
		if (!this.pendReadyList.isEmpty()) {
			Collections.sort(this.pendReadyList, compare);
		}
	}

	public ProcessControlBlock findThePCB(int proID) {
		for (ProcessControlBlock processControlBlock : this.blockPCBList) {
			if (processControlBlock.getProcessID() == proID) {
				return processControlBlock;
			}
		}
		for (ProcessControlBlock processControlBlock : this.readyPCBList) {
			if (processControlBlock.getProcessID() == proID) {
				return processControlBlock;
			}
		}
		for (ProcessControlBlock processControlBlock : this.pendBlockList) {
			if (processControlBlock.getProcessID() == proID) {
				return processControlBlock;
			}
		}
		for (ProcessControlBlock processControlBlock : this.pendReadyList) {
			if (processControlBlock.getProcessID() == proID) {
				return processControlBlock;
			}
		}
		if (!this.runningPCB.isEmpty() && this.runningPCB.get(0).getProcessID() == proID) {
			return this.runningPCB.get(0);
		}
		return null;
	}

	public ProcessControlBlock findBlockPCB(int proID) {
		for (ProcessControlBlock processControlBlock : this.blockPCBList) {
			if (processControlBlock.getProcessID() == proID) {
				return processControlBlock;
			}
		}
		return null;
	}

	public ProcessControlBlock findReadyPCB(int proID) {
		for (ProcessControlBlock processControlBlock : this.readyPCBList) {
			if (processControlBlock.getProcessID() == proID) {
				return processControlBlock;
			}
		}
		return null;
	}

	public ProcessControlBlock findPendReadyPCB(int proID) {
		for (ProcessControlBlock processControlBlock : this.pendReadyList) {
			if (processControlBlock.getProcessID() == proID) {
				return processControlBlock;
			}
		}
		return null;
	}

	public ProcessControlBlock findPendBlockPCB(int proID) {
		for (ProcessControlBlock processControlBlock : this.pendBlockList) {
			if (processControlBlock.getProcessID() == proID) {
				return processControlBlock;
			}
		}
		return null;
	}

	// 小功能：判断是否有空闲
	public boolean haveBlank() {
		if (this.calculatePCBNum() < SIZE) {
			return true;
		} else {
			return false;
		}
	}

	// 小功能：返回最大的进程ID号
	public Integer findMaxProID() {

		if (this.calculatePCBNum() == 0) {// 如果没有运行的进程就返回0作为第一个进程号
			return 0;
		}

		int tmp = 1;
		for (ProcessControlBlock processControlBlock : this.readyPCBList) {
			if (processControlBlock.getProcessID() > tmp) {
				tmp = processControlBlock.getProcessID();
			}
		}

		for (ProcessControlBlock processControlBlock : this.blockPCBList) {
			if (processControlBlock.getProcessID() > tmp) {
				tmp = processControlBlock.getProcessID();
			}
		}

		for (ProcessControlBlock processControlBlock : this.pendReadyList) {
			if (processControlBlock.getProcessID() > tmp) {
				tmp = processControlBlock.getProcessID();
			}
		}

		for (ProcessControlBlock processControlBlock : this.pendBlockList) {
			if (processControlBlock.getProcessID() > tmp) {
				tmp = processControlBlock.getProcessID();
			}
		}

		if (!this.runningPCB.isEmpty() && this.runningPCB.get(0).getProcessID() > tmp) {
			tmp = this.runningPCB.get(0).getProcessID();
		}

		return tmp;
	}

	public boolean insertOnePCB(ProcessControlBlock block) {
		synchronized (readyPCBList) {
			// 向PCB队列插入一个新的PCB
			if (this.calculatePCBNum() < SIZE) {
				if (this.blankPCBList.isEmpty()) {// 空闲块为空
					return false;
				} else {
					// 将空闲队列队首的PCB块的主存物理块地址分配给新的PCB上。
					block.getPhysicAdd().updateAddress(this.blankPCBList.get(0).getPhysicAdd().getData());
					this.blankPCBList.remove(0);// 从PCB空闲块队首取出PCB块
					this.readyPCBList.add(block);
					this.sortReadyQueueByPriority();// 对就绪队列进行排序
					this.calculatePCBNum();
					return true;
				}
			} else {// PCB队列已满
				return false;
			}
		}
	}

	/**
	 * @param proID 要删除PCB的进程ID
	 * @return true 成功删除PCB，并且给空白PCB池补一个PCB。
	 * @return false 没有已占用的PCB，所以没办法删除
	 */
	public boolean deleteThePCB(int proID) {
		synchronized (blockPCBList) {
			synchronized (readyPCBList) {
				synchronized (runningPCB) {
					if (this.calculatePCBNum() == 0) {
						return false;
					} else {
						if (this.blankPCBList.size() == SIZE) {
							return false;
						} else {
							for (ProcessControlBlock processControlBlock : this.blockPCBList) {
								if (processControlBlock.getProcessID() == proID) {
									this.blankPCBList.add(processControlBlock);
									this.blockPCBList.remove(processControlBlock);
								}
							}
							for (ProcessControlBlock processControlBlock : this.readyPCBList) {
								if (processControlBlock.getProcessID() == proID) {
									this.blankPCBList.add(processControlBlock);
									this.blockPCBList.remove(processControlBlock);
								}
							}
							if (!this.runningPCB.isEmpty() && this.runningPCB.get(0).getProcessID() == proID) {
								this.blankPCBList.add(this.runningPCB.get(0));
								this.runningPCB.remove(0);
							}
							return true;
						}
					}
				}
			}
		}
	}

	public int calculatePCBNum() {
		return this.readyPCBList.size() + this.blockPCBList.size() + this.runningPCB.size() + this.pendBlockList.size()
				+ this.pendReadyList.size();
	}

	public boolean readyToRun() {
		synchronized (readyPCBList) {
			synchronized (runningPCB) {
				if (this.readyPCBList.isEmpty()) {// 判断就绪队列是否为空
					return false;
				} else if (!this.runningPCB.isEmpty()) {// 判断运行态是否已经有进程，如果有就拒绝运行
					return false;
				} else {
					// 就绪队列队首移到运行PCB
					this.runningPCB.add(this.readyPCBList.get(0));
					this.readyPCBList.remove(0);
					this.sortReadyQueueByPriority();

					return true;
				}
			}
		}
	}

	public boolean runToReady() {
		synchronized (readyPCBList) {
			synchronized (runningPCB) {
				if (this.runningPCB.isEmpty()) {
					// 判断运行进程是否存在
					return false;
				} else {
					// 运行PCB移到就绪队列队尾
					this.readyPCBList.add(this.runningPCB.get(0));
					this.runningPCB.remove(0);
					this.sortReadyQueueByPriority();

					return true;
				}
			}
		}
	}

	public boolean runToBlock() {
		synchronized (blockPCBList) {
			synchronized (runningPCB) {
				if (this.runningPCB.isEmpty()) {
					// 判断运行进程是否存在
					return false;
				} else {
					// 运行PCB移到阻塞队列队尾
					this.blockPCBList.add(this.runningPCB.get(0));
					this.runningPCB.remove(0);
					this.sortReadyQueueByPriority();

					return true;
				}
			}
		}
	}

	public boolean blockToReady(int proID) {
		synchronized (blockPCBList) {
			synchronized (readyPCBList) {
				if (this.findBlockPCB(proID) == null) {
					// 找不到PCB返回false。
					return false;
				} else {
					// 指定的阻塞PCB转移到就绪队尾。
					this.readyPCBList.add(this.findBlockPCB(proID));
					this.blockPCBList.remove(this.findBlockPCB(proID));
					this.sortReadyQueueByPriority();

					return true;
				}
			}
		}
	}

	public boolean blockToPendBlock(int proID) {
		synchronized (blockPCBList) {
			synchronized (pendBlockList) {
				if (this.findBlockPCB(proID) == null) {
					return false;
				} else {
					this.pendBlockList.add(this.findBlockPCB(proID));
					this.blockPCBList.remove(this.findBlockPCB(proID));

					return true;
				}
			}
		}
	}

	public boolean readyToPendReady(int proID) {
		synchronized (readyPCBList) {
			synchronized (pendReadyList) {
				if (this.findReadyPCB(proID) == null) {
					return false;
				} else {
					this.pendReadyList.add(this.findReadyPCB(proID));
					this.readyPCBList.remove(this.findReadyPCB(proID));
					this.sortPendReadyQueueByPriority();

					return true;
				}
			}
		}
	}

	public boolean pendBlockToBlock(int proID) {
		synchronized (pendBlockList) {
			synchronized (blockPCBList) {
				if (this.findPendBlockPCB(proID) == null) {
					return false;
				} else {
					this.blockPCBList.add(this.findPendBlockPCB(proID));
					this.pendBlockList.remove(this.findPendBlockPCB(proID));

					return true;
				}
			}
		}
	}

	public boolean pendReadyToReady(int proID) {
		synchronized (pendReadyList) {
			synchronized (readyPCBList) {
				if (this.findPendReadyPCB(proID) == null) {
					return false;
				} else {
					this.readyPCBList.add(this.findPendReadyPCB(proID));
					this.pendReadyList.remove(this.findPendReadyPCB(proID));
					this.sortReadyQueueByPriority();

					return true;
				}
			}
		}
	}

	public boolean pendBlockToPendReady(int proID) {
		synchronized (pendBlockList) {
			synchronized (pendReadyList) {
				if (this.findPendBlockPCB(proID) == null) {
					return false;
				} else {
					this.pendReadyList.add(this.findPendBlockPCB(proID));
					this.pendBlockList.remove(this.findPendBlockPCB(proID));
					this.sortPendReadyQueueByPriority();

					return true;
				}
			}
		}
	}

	/**
	 * @apiNote 最佳适应算法专用工具类，为了同时供外存申请空闲区使用，申明为静态类
	 */
	public static class FreePageRegion {
		public int pageID;// 首页号
		public int num;// 页数

		public FreePageRegion() {
			super();
			this.pageID = -1;
			this.num = 0;
		}

		public FreePageRegion(int pageID, int num) {
			super();
			this.pageID = pageID;
			this.num = num;
		}

	}

	/**
	 * @apiNote 最佳适应算法专用工具类
	 */
	public static class Compare implements Comparator<FreePageRegion> {
		@Override
		public int compare(FreePageRegion o1, FreePageRegion o2) {
			if (o1.num > o2.num) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	/**
	 * 寻找一个特定页面数要求的连续地址空间的第一页逻辑页号
	 * 
	 * @param size
	 * @return
	 */
	public Integer findFreeHeadPageNumBySize(int size) {
		// 统计每一个现有进程的逻辑地址区间
		/*
		 * 如果没有进程存在，则返回逻辑页号0 如果有进程存在： 获取页表在内存的首地址（对应逻辑页号0）
		 */
		int maxSize = PageTable.getTableLength();
		// 判断PCB池为空
		if (this.calculatePCBNum() == 0) {
			if (size <= maxSize) {
				return 0;
			} else {
				return null;
			}
		} else {
			// 用数组记录哪些页被占有。
			boolean[] pageIDs = new boolean[maxSize];
			for (int i = 0; i < maxSize; i++) {
				pageIDs[i] = false;
			}
			synchronized (pendBlockList) {
				synchronized (pendReadyList) {
					synchronized (blockPCBList) {
						synchronized (readyPCBList) {
							synchronized (runningPCB) {
								for (ProcessControlBlock processControlBlock : this.pendBlockList) {
									int index = processControlBlock.findPagetableItemIndex();
									for (int i = index; i < processControlBlock.getControlInfo().getPageTableItemNum()
											+ index; i++) {
										pageIDs[i] = true;
									}
								}
								for (ProcessControlBlock processControlBlock : this.pendReadyList) {
									int index = processControlBlock.findPagetableItemIndex();
									for (int i = index; i < processControlBlock.getControlInfo().getPageTableItemNum()
											+ index; i++) {
										pageIDs[i] = true;
									}
								}
								for (ProcessControlBlock processControlBlock : this.blockPCBList) {
									int index = processControlBlock.findPagetableItemIndex();
									for (int i = index; i < processControlBlock.getControlInfo().getPageTableItemNum()
											+ index; i++) {
										pageIDs[i] = true;
									}
								}
								for (ProcessControlBlock processControlBlock : this.readyPCBList) {
									int index = processControlBlock.findPagetableItemIndex();
									for (int i = index; i < processControlBlock.getControlInfo().getPageTableItemNum()
											+ index; i++) {
										pageIDs[i] = true;
									}
								}
								if (!this.runningPCB.isEmpty()) {
									int index = this.runningPCB.get(0).findPagetableItemIndex();
									for (int i = index; i < this.runningPCB.get(0).getControlInfo()
											.getPageTableItemNum() + index; i++) {
										pageIDs[i] = true;
									}
								}
								// 根据数组提取所有空闲区数组（每个单元由起始页号，和尺寸组成）
								ArrayList<FreePageRegion> list = new ArrayList<FreePageRegion>();
								for (int i = 0; i < maxSize;) {
									if (!pageIDs[i]) {
										int start = i;// 记录第一个false
										int end = maxSize - 1;// 记录最后一个false
										for (int j = start; j < maxSize; j++) {
											if (pageIDs[j]) {
												end = j - 1;
												break;
											}
										}
										FreePageRegion region = new FreePageRegion(start, end - start + 1);
										list.add(region);
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
										if (freePageRegion.num > size) {
											// 空闲区足够大了
											return freePageRegion.pageID;
										}
									}
								}
								return null;// 没有足够大的空闲区
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @apiNote 找到第一个阻塞队列和挂起阻塞队列中因为特定原因的PCB
	 * @param reason
	 * @return 被找到的PCB
	 */
	public ProcessControlBlock findFirstWaitBlockByReason(WAIT_REASON reason) {
		for (ProcessControlBlock processControlBlock : this.blockPCBList) {
			if (processControlBlock.getControlInfo().getWaitReason() == reason) {
				return processControlBlock;
			}
		}
		for (ProcessControlBlock processControlBlock : this.pendBlockList) {
			if (processControlBlock.getControlInfo().getWaitReason() == reason) {
				return processControlBlock;
			}
		}
		return null;
	}

	/**
	 * @apiNote 自增就绪队列和阻塞队列的PCB的访问字段。
	 * @return true 成功将就绪队列和阻塞队列的每个PCB的访问字段自增1<br>
	 *         false 根本每有运行或者阻塞的PCB，所以没有做访问字段的自加。<br>
	 */
	public boolean autoIncreaseReadyBlockPCB() {
		synchronized (this.readyPCBList) {
			synchronized (this.blockPCBList) {
				if (this.readyPCBList.isEmpty() && this.blockPCBList.isEmpty()) {
					return false;
				} else {
					for (int i = 0; i < this.readyPCBList.size(); i++) {
						this.readyPCBList.get(i).getControlInfo().autoIncreaseAccessTime();
					}
					for (int i = 0; i < this.blockPCBList.size(); i++) {
						this.blockPCBList.get(i).getControlInfo().autoIncreaseAccessTime();
					}
					return true;
				}
			}

		}
	}

	/**
	 * @return 访问字段最大的进程ID
	 */
	public Integer findMaxAccessTextProID() {
		synchronized (readyPCBList) {
			synchronized (blockPCBList) {
				ProcessControlBlock block = this.readyPCBList.get(0);
				for (int i = 0; i < this.readyPCBList.size(); i++) {
					if (block.getControlInfo().getAccessTime() < this.readyPCBList.get(i).getControlInfo()
							.getAccessTime()) {
						block = this.readyPCBList.get(i);
					}
				}
				for (int i = 0; i < this.blockPCBList.size(); i++) {
					if (block.getControlInfo().getAccessTime() < this.blockPCBList.get(i).getControlInfo()
							.getAccessTime()) {
						block = this.blockPCBList.get(i);
					}
				}
				return new Integer(block.getProcessID());
			}
		}
	}

	public List<ProcessControlBlock> getBlankPCBList() {
		return blankPCBList;
	}

	public void setBlankPCBList(ArrayList<ProcessControlBlock> blankPCBList) {
		this.blankPCBList = blankPCBList;
	}

	public List<ProcessControlBlock> getReadyPCBList() {
		return readyPCBList;
	}

	public void setReadyPCBList(ArrayList<ProcessControlBlock> readyPCBList) {
		this.readyPCBList = readyPCBList;
	}

	public List<ProcessControlBlock> getBlockPCBList() {
		return blockPCBList;
	}

	public void setBlockPCBList(ArrayList<ProcessControlBlock> blockPCBList) {
		this.blockPCBList = blockPCBList;
	}

	public List<ProcessControlBlock> getRunningPCB() {
		return runningPCB;
	}

	public void setRunningPCB(ArrayList<ProcessControlBlock> runningPCB) {
		this.runningPCB = runningPCB;
	}

	public static int getSize() {
		return SIZE;
	}

	public List<ProcessControlBlock> getPendBlockList() {
		return pendBlockList;
	}

	public void setPendBlockList(ArrayList<ProcessControlBlock> pendBlockList) {
		this.pendBlockList = pendBlockList;
	}

	public List<ProcessControlBlock> getPendReadyList() {
		return pendReadyList;
	}

	public void setPendReadyList(ArrayList<ProcessControlBlock> pendReadyList) {
		this.pendReadyList = pendReadyList;
	}
}
