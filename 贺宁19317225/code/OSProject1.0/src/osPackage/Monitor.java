package osPackage;

import java.util.ArrayList;
import java.util.Vector;

public class Monitor {
	private final static int BUFFER_NUM = 1;// 缓冲区数
	private final static int REGISTER_NUM = 8;// 寄存器数
	private final static int DEVICE_NUM = 4;// 外设数量
	private final static int MEMORY_SOURCE_NUM = 2;// 内存资源种类数
	private final static int MEMORY_SOURCE_EMPTY_START = 0;// 挂起进程挂起时生产，由撤销进程撤销前想要激活挂起进程时消费。
	private final static int MEMORY_SOURCE_FULL_START = 0;// 由撤销的进程在撤销时生产，由挂起进程激活时消费。

	private ArrayList<MessagePair> messages;// 进程队列

	// 死锁检测专用数组
	private Vector<Integer> Available;
	private Vector<Vector<Integer>> Allocation;
	private Vector<Vector<Integer>> Request;
	private Vector<Integer> Work;
	private Vector<Boolean> finish;

	public class MessagePair {
		private MESSAGE message;// 信号量
		private int messNum;// 信号量的数量
		private ArrayList<Integer> proIDQueue;// 进程号等待队列

		public ArrayList<Integer> getProIDQueue() {
			return proIDQueue;
		}

		public void setProIDQueue(ArrayList<Integer> proIDQueue) {
			this.proIDQueue = proIDQueue;
		}

		public MessagePair() {
			super();
			this.message = MESSAGE.NONE;
			this.messNum = 0;
			this.proIDQueue = new ArrayList<Integer>();
		}

		public void updateMessPair(MESSAGE message, int messNum) {
			this.message = message;
			this.messNum = messNum;
			this.proIDQueue.clear();// 重新设置信号量，并且清空队列
		}

		// 功能：挂起进程并释放管程
		public void wait(Integer proID) {
			this.proIDQueue.add(proID);
		}

		// 功能：激活进程
		public Integer signal() {
			Integer tmp = new Integer(this.proIDQueue.get(0).intValue());
			this.proIDQueue.remove(0);
			return tmp;
		}

		public MESSAGE getMessage() {
			return message;
		}

		public void setMessage(MESSAGE message) {
			this.message = message;
		}

		public int getMessNum() {
			return messNum;
		}

		public void setMessNum(int messNum) {
			this.messNum = messNum;
		}

	}

	public Monitor() {
		super();
		this.messages = new ArrayList<Monitor.MessagePair>();

		MessagePair pair = new MessagePair();
		pair.updateMessPair(MESSAGE.BUFFER, BUFFER_NUM);
		this.messages.add(pair);

		MessagePair pair2 = new MessagePair();
		pair2.updateMessPair(MESSAGE.REGISTER0, 1);
		this.messages.add(pair2);

		MessagePair pair3 = new MessagePair();
		pair3.updateMessPair(MESSAGE.REGISTER1, 1);
		this.messages.add(pair3);

		MessagePair pair4 = new MessagePair();
		pair4.updateMessPair(MESSAGE.REGISTER2, 1);
		this.messages.add(pair4);

		MessagePair pair5 = new MessagePair();
		pair5.updateMessPair(MESSAGE.REGISTER3, 1);
		this.messages.add(pair5);

		MessagePair pair6 = new MessagePair();
		pair6.updateMessPair(MESSAGE.REGISTER4, 1);
		this.messages.add(pair6);

		MessagePair pair7 = new MessagePair();
		pair7.updateMessPair(MESSAGE.REGISTER5, 1);
		this.messages.add(pair7);

		MessagePair pair8 = new MessagePair();
		pair8.updateMessPair(MESSAGE.REGISTER6, 1);
		this.messages.add(pair8);

		MessagePair pair9 = new MessagePair();
		pair9.updateMessPair(MESSAGE.REGISTER7, 1);
		this.messages.add(pair9);

		MessagePair pair10 = new MessagePair();
		pair10.updateMessPair(MESSAGE.DEVICE0, 1);
		this.messages.add(pair10);

		MessagePair pair11 = new MessagePair();
		pair11.updateMessPair(MESSAGE.DEVICE1, 1);
		this.messages.add(pair11);

		MessagePair pair12 = new MessagePair();
		pair12.updateMessPair(MESSAGE.DEVICE2, 1);
		this.messages.add(pair12);

		MessagePair pair13 = new MessagePair();
		pair13.updateMessPair(MESSAGE.DEVICE3, 1);
		this.messages.add(pair13);

		MessagePair pair14 = new MessagePair();
		pair14.updateMessPair(MESSAGE.MEMORY_SOURCE_EMPTY, MEMORY_SOURCE_EMPTY_START);
		this.messages.add(pair14);

		MessagePair pair15 = new MessagePair();
		pair15.updateMessPair(MESSAGE.MEMORY_SOURCE_FULL, MEMORY_SOURCE_FULL_START);
		this.messages.add(pair15);

		this.Available = new Vector<Integer>();
		for (int i = 0; i < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; i++) {
			this.Available.add(new Integer(this.messages.get(i).getMessNum()));// 定死13个资源的个数全为0，如果有改变需要调整。
		}
		this.Allocation = new Vector<Vector<Integer>>();
		for (int i = 0; i < PCBQueue.getSize(); i++) {
			this.Allocation.add(new Vector<Integer>());
			for (int j = 0; j < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; j++) {
				this.Allocation.get(i).add(0);// Allocation数组全为0，因为根本没有进程占有资源。
			}
		}
		this.Request = new Vector<Vector<Integer>>();
		for (int i = 0; i < PCBQueue.getSize(); i++) {
			this.Request.add(new Vector<Integer>());
			for (int j = 0; j < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; j++) {
				this.Request.get(i).add(0);// 初始时说有进程都没有需求。
			}
		}
		this.Work = new Vector<Integer>();
		for (int i = 0; i < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; i++) {
			this.Work.add(1);// 初始时Work数组内容与Available数组一致。
		}
		this.finish = new Vector<Boolean>();
		for (int i = 0; i < PCBQueue.getSize(); i++) {
			this.finish.add(true);// 初始是所有进程都都满足了。因为根本就没有进程。
		}
	}

	/**
	 * @param message 要发放的资源
	 */
	public void autoDecreaseAvailable(MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Available.get(index) - 1;
		this.Available.set(index, data);
	}

	/**
	 * @param message 要放回的资源
	 */
	public void autoIncreaseAvailable(MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Available.get(index) + 1;
		this.Available.set(index, data);
	}

	/**
	 * @param proID   要申请资源的进程ID
	 * @param message 要申请的资源
	 */
	public void autoDecreaseRequest(int proID, MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Request.get(proID - 1).get(index) - 1;// 进程ID是从1开始的，所以要减一。
		this.Request.get(proID - 1).set(index, data);
	}

	/**
	 * @param proID   要申请资源的进程ID
	 * @param message 要申请的资源
	 */
	public void autoIncreaseRequest(int proID, MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Request.get(proID - 1).get(index) + 1;// 进程ID是从1开始的，所以要减一。
		this.Request.get(proID - 1).set(index, data);
	}

	/**
	 * @param proID   要释放资源的进程ID
	 * @param message 要申请的资源
	 */
	public void autoDecreaseAlloction(int proID, MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Allocation.get(proID - 1).get(index) - 1;// 进程ID是从1开始的，所以要减一。
		this.Allocation.get(proID - 1).set(index, data);
	}

	/**
	 * @param proID   已占用资源的进程ID
	 * @param message 要申请的资源
	 */
	public void autoIncreaseAlloction(int proID, MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Allocation.get(proID - 1).get(index) + 1;// 进程ID是从1开始的，所以要减一。
		this.Allocation.get(proID - 1).set(index, data);
	}

	/**
	 * @param message
	 * @return 信息量对应的资源编号。
	 */
	public int analyseMessage(MESSAGE message) {
		switch (message) {
		case BUFFER:
			return 0;
		case REGISTER0:
			return 1;
		case REGISTER1:
			return 2;
		case REGISTER2:
			return 3;
		case REGISTER3:
			return 4;
		case REGISTER4:
			return 5;
		case REGISTER5:
			return 6;
		case REGISTER6:
			return 7;
		case REGISTER7:
			return 8;
		case DEVICE0:
			return 9;
		case DEVICE1:
			return 10;
		case DEVICE2:
			return 11;
		case DEVICE3:
			return 12;
		case MEMORY_SOURCE_EMPTY:
			return 13;
		case MEMORY_SOURCE_FULL:
			return 14;
		case NONE:
			return 15;
		default:
			return 15;
		}
	}

	/**
	 * @apiNote 死锁检测算法执行函数
	 * @return 死锁的进程
	 */
	public ArrayList<Integer> checkDeadLock() {
		ArrayList<Integer> deadProIDs = new ArrayList<Integer>();
		this.updateWorkArray();
		this.updateFinish();
		Integer proId = this.findSatisfiedProID();
		while (proId != null) {
			this.addAlloctionToWork(proId);
			proId = this.findSatisfiedProID();
		}
		this.updateDeadProIDArray(deadProIDs);
		return deadProIDs;
	}

	/**
	 * @apiNote 更新死锁
	 * @param deadProIdArray 死锁的进程ID链表
	 */
	public void updateDeadProIDArray(ArrayList<Integer> deadProIdArray) {
		for (int i = 0; i < PCBQueue.getSize(); i++) {
			if (!this.finish.get(i)) {
				deadProIdArray.add(i + 1);
			}
		}
	}

	/**
	 * @apiNote 更新Work数组的内容，使之与Available数组一致。
	 */
	public void updateWorkArray() {
		for (int i = 0; i < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; i++) {
			this.Work.set(i, this.Available.get(i));
		}
	}

	/**
	 * @apiNote 将可以满足需求的进程释放出资源，并把对应的finish改为true。
	 * @param proID 可以满足需求的进程ID
	 */
	public void addAlloctionToWork(int proID) {
		for (int i = 0; i < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; i++) {
			int data = this.Work.get(i) + this.Allocation.get(proID - 1).get(i);
			this.Work.set(i, data);
		}
		this.finish.set(proID - 1, true);
	}

	/**
	 * @apiNote 根据Allocation数组更新finish数组
	 */
	public void updateFinish() {
		boolean isFound = false;
		for (int i = 0; i < PCBQueue.getSize(); i++) {
			for (int j = 0; j < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; j++) {
				if (this.Allocation.get(i).get(j) > 0) {
					isFound = true;
					break;
				} else {
					isFound = false;
				}
			}
			if (isFound) {
				this.finish.set(i, false);
				isFound = false;
			} else {
				this.finish.set(i, true);
			}
		}
	}

	/**
	 * @return 找的的当前可以满足资源分配的进程的ID。
	 */
	public Integer findSatisfiedProID() {
		boolean isFound = false;
		for (int i = 0; i < PCBQueue.getSize(); i++) {
			if (!this.finish.get(i)) {
				int j = 0;
				for (j = 0; j < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; j++) {
					if (this.Request.get(i).get(j) > this.Work.get(j)) {
						// 发现一个资源不能满足该进程。
						isFound = false;
						break;
					} else {
						isFound = true;
					}
				}
				if (isFound) {
					return new Integer(i + 1);
				}
			}
		}
		return null;
	}

	// 小功能：根据信号类型寻找信号量对
	public MessagePair findByMessage(MESSAGE message) {
		for (MessagePair messagePair : this.messages) {
			if (messagePair.getMessage() == message) {
				return messagePair;
			}
		}
		return null;
	}

	// 功能：P操作
	public boolean P(MESSAGE message, Integer proID) {
		MessagePair pair = this.findByMessage(message);
		if (pair != null) {
			pair.messNum--;
			if (pair.messNum < 0) {
				pair.wait(proID);
				this.autoIncreaseRequest(proID, message);// 进程申请资源，但是没有满足，它的需求数组都会+1。
				return false;// 说明被阻塞了
			} else {
				this.autoDecreaseAvailable(message);// 进程申请资源成功，当前剩余资源数组就-1
				if (message != MESSAGE.MEMORY_SOURCE_EMPTY && message != MESSAGE.MEMORY_SOURCE_FULL) {
					this.autoIncreaseAlloction(proID, message);// 进程成功占用资源，占有数+1
				}
				return true;// 说明没有被阻塞
			}
		} else {
			return false;// 这个最好不要用，因为没有意义，找不到pair导致的
		}
	}

	// 功能：V操作
	public Integer V(MESSAGE message, Integer proID) {
		MessagePair pair = this.findByMessage(message);
		if (pair != null) {
			pair.messNum++;
			if (message != MESSAGE.MEMORY_SOURCE_EMPTY) {
				this.autoDecreaseAlloction(proID, message);// 进程交出释放的资源
			}
			if (pair.messNum <= 0) {
				Integer wakeproId = pair.signal();// 说明被唤醒了
				this.autoDecreaseRequest(wakeproId, message);// 被唤醒进程的申请被满足。
				if (message != MESSAGE.MEMORY_SOURCE_FULL) {
					this.autoIncreaseAlloction(wakeproId, message);// 被唤醒进程占用了交出的资源
				}
				return wakeproId;
			} else {
				this.autoIncreaseAvailable(message);// 装入已有资源库中
				return null;// 说明没有需要唤醒的进程
			}
		} else {
			return null;// 这个最好不要用，因为没有意义，找不到pair导致的
		}
	}

	/**
	 * @param proID 进程ID
	 * @return <code>true</code> 成功情况进程在所有信号量的等待队列中的记录，并且将messNum++。
	 */
	public boolean clearProIDWaitingMessages(int proID) {
		for (int i = 0; i < this.messages.size(); i++) {
			if (this.messages.get(i).getMessage() != MESSAGE.MEMORY_SOURCE_FULL) {// 过滤掉挂起信号
				for (int j = 0; j < this.messages.get(i).getProIDQueue().size(); j++) {
					if (this.messages.get(i).getProIDQueue().get(j).equals(proID)) {
						this.messages.get(i).setMessNum(this.messages.get(i).getMessNum() + 1);
						this.messages.get(i).getProIDQueue().remove(new Integer(proID));
						this.Request.get(j).set(i, 0);// 同时将请求资源数清0
					}
				}
			}
		}
		return true;
	}

	public ArrayList<MessagePair> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<MessagePair> messages) {
		this.messages = messages;
	}

	public static int getBufferNum() {
		return BUFFER_NUM;
	}

	public static int getRegisterNum() {
		return REGISTER_NUM;
	}

	public static int getDeviceNum() {
		return DEVICE_NUM;
	}

}
