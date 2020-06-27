package osPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PCBQueue {
	private final static int SIZE = 8;// PCB������

	private List<ProcessControlBlock> blankPCBList;// PCB���ж���
	private List<ProcessControlBlock> readyPCBList;// PCB��������
	private List<ProcessControlBlock> blockPCBList;// PCB��������
	private List<ProcessControlBlock> runningPCB;// ����PCB(ֻ����һ��)

	private List<ProcessControlBlock> pendBlockList;// ����ȴ�����
	private List<ProcessControlBlock> pendReadyList;// �����������

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
	 * @apiNote ���������а������ȼ��Ӵ�С����
	 */
	public void sortReadyQueueByPriority() {
		PriorityLessThanCompare compare = new PriorityLessThanCompare();
		if (!this.readyPCBList.isEmpty()) {
			Collections.sort(this.readyPCBList, compare);
		}
	}

	/**
	 * @apiNote ������������а������ȼ��Ӵ�С����
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

	// С���ܣ��ж��Ƿ��п���
	public boolean haveBlank() {
		if (this.calculatePCBNum() < SIZE) {
			return true;
		} else {
			return false;
		}
	}

	// С���ܣ��������Ľ���ID��
	public Integer findMaxProID() {

		if (this.calculatePCBNum() == 0) {// ���û�����еĽ��̾ͷ���0��Ϊ��һ�����̺�
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
			// ��PCB���в���һ���µ�PCB
			if (this.calculatePCBNum() < SIZE) {
				if (this.blankPCBList.isEmpty()) {// ���п�Ϊ��
					return false;
				} else {
					// �����ж��ж��׵�PCB�������������ַ������µ�PCB�ϡ�
					block.getPhysicAdd().updateAddress(this.blankPCBList.get(0).getPhysicAdd().getData());
					this.blankPCBList.remove(0);// ��PCB���п����ȡ��PCB��
					this.readyPCBList.add(block);
					this.sortReadyQueueByPriority();// �Ծ������н�������
					this.calculatePCBNum();
					return true;
				}
			} else {// PCB��������
				return false;
			}
		}
	}

	/**
	 * @param proID Ҫɾ��PCB�Ľ���ID
	 * @return true �ɹ�ɾ��PCB�����Ҹ��հ�PCB�ز�һ��PCB��
	 * @return false û����ռ�õ�PCB������û�취ɾ��
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
				if (this.readyPCBList.isEmpty()) {// �жϾ��������Ƿ�Ϊ��
					return false;
				} else if (!this.runningPCB.isEmpty()) {// �ж�����̬�Ƿ��Ѿ��н��̣�����о;ܾ�����
					return false;
				} else {
					// �������ж����Ƶ�����PCB
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
					// �ж����н����Ƿ����
					return false;
				} else {
					// ����PCB�Ƶ��������ж�β
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
					// �ж����н����Ƿ����
					return false;
				} else {
					// ����PCB�Ƶ��������ж�β
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
					// �Ҳ���PCB����false��
					return false;
				} else {
					// ָ��������PCBת�Ƶ�������β��
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
	 * @apiNote �����Ӧ�㷨ר�ù����࣬Ϊ��ͬʱ��������������ʹ�ã�����Ϊ��̬��
	 */
	public static class FreePageRegion {
		public int pageID;// ��ҳ��
		public int num;// ҳ��

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
	 * @apiNote �����Ӧ�㷨ר�ù�����
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
	 * Ѱ��һ���ض�ҳ����Ҫ���������ַ�ռ�ĵ�һҳ�߼�ҳ��
	 * 
	 * @param size
	 * @return
	 */
	public Integer findFreeHeadPageNumBySize(int size) {
		// ͳ��ÿһ�����н��̵��߼���ַ����
		/*
		 * ���û�н��̴��ڣ��򷵻��߼�ҳ��0 ����н��̴��ڣ� ��ȡҳ�����ڴ���׵�ַ����Ӧ�߼�ҳ��0��
		 */
		int maxSize = PageTable.getTableLength();
		// �ж�PCB��Ϊ��
		if (this.calculatePCBNum() == 0) {
			if (size <= maxSize) {
				return 0;
			} else {
				return null;
			}
		} else {
			// �������¼��Щҳ��ռ�С�
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
								// ����������ȡ���п��������飨ÿ����Ԫ����ʼҳ�ţ��ͳߴ���ɣ�
								ArrayList<FreePageRegion> list = new ArrayList<FreePageRegion>();
								for (int i = 0; i < maxSize;) {
									if (!pageIDs[i]) {
										int start = i;// ��¼��һ��false
										int end = maxSize - 1;// ��¼���һ��false
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
								if (list.size() == 0) {// ˵��û�п�����
									return null;
								} else {// ˵���п�����
									Compare compare = new Compare();
									Collections.sort(list, compare);// ���շ����ֶδ�С��������
									for (FreePageRegion freePageRegion : list) {
										if (freePageRegion.num > size) {
											// �������㹻����
											return freePageRegion.pageID;
										}
									}
								}
								return null;// û���㹻��Ŀ�����
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @apiNote �ҵ���һ���������к͹���������������Ϊ�ض�ԭ���PCB
	 * @param reason
	 * @return ���ҵ���PCB
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
	 * @apiNote �����������к��������е�PCB�ķ����ֶΡ�
	 * @return true �ɹ����������к��������е�ÿ��PCB�ķ����ֶ�����1<br>
	 *         false ����ÿ�����л���������PCB������û���������ֶε��Լӡ�<br>
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
	 * @return �����ֶ����Ľ���ID
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
