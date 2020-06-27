package osPackage;

import java.util.ArrayList;
import java.util.Vector;

public class Monitor {
	private final static int BUFFER_NUM = 1;// ��������
	private final static int REGISTER_NUM = 8;// �Ĵ�����
	private final static int DEVICE_NUM = 4;// ��������
	private final static int MEMORY_SOURCE_NUM = 2;// �ڴ���Դ������
	private final static int MEMORY_SOURCE_EMPTY_START = 0;// ������̹���ʱ�������ɳ������̳���ǰ��Ҫ����������ʱ���ѡ�
	private final static int MEMORY_SOURCE_FULL_START = 0;// �ɳ����Ľ����ڳ���ʱ�������ɹ�����̼���ʱ���ѡ�

	private ArrayList<MessagePair> messages;// ���̶���

	// �������ר������
	private Vector<Integer> Available;
	private Vector<Vector<Integer>> Allocation;
	private Vector<Vector<Integer>> Request;
	private Vector<Integer> Work;
	private Vector<Boolean> finish;

	public class MessagePair {
		private MESSAGE message;// �ź���
		private int messNum;// �ź���������
		private ArrayList<Integer> proIDQueue;// ���̺ŵȴ�����

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
			this.proIDQueue.clear();// ���������ź�����������ն���
		}

		// ���ܣ�������̲��ͷŹܳ�
		public void wait(Integer proID) {
			this.proIDQueue.add(proID);
		}

		// ���ܣ��������
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
			this.Available.add(new Integer(this.messages.get(i).getMessNum()));// ����13����Դ�ĸ���ȫΪ0������иı���Ҫ������
		}
		this.Allocation = new Vector<Vector<Integer>>();
		for (int i = 0; i < PCBQueue.getSize(); i++) {
			this.Allocation.add(new Vector<Integer>());
			for (int j = 0; j < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; j++) {
				this.Allocation.get(i).add(0);// Allocation����ȫΪ0����Ϊ����û�н���ռ����Դ��
			}
		}
		this.Request = new Vector<Vector<Integer>>();
		for (int i = 0; i < PCBQueue.getSize(); i++) {
			this.Request.add(new Vector<Integer>());
			for (int j = 0; j < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; j++) {
				this.Request.get(i).add(0);// ��ʼʱ˵�н��̶�û������
			}
		}
		this.Work = new Vector<Integer>();
		for (int i = 0; i < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; i++) {
			this.Work.add(1);// ��ʼʱWork����������Available����һ�¡�
		}
		this.finish = new Vector<Boolean>();
		for (int i = 0; i < PCBQueue.getSize(); i++) {
			this.finish.add(true);// ��ʼ�����н��̶��������ˡ���Ϊ������û�н��̡�
		}
	}

	/**
	 * @param message Ҫ���ŵ���Դ
	 */
	public void autoDecreaseAvailable(MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Available.get(index) - 1;
		this.Available.set(index, data);
	}

	/**
	 * @param message Ҫ�Żص���Դ
	 */
	public void autoIncreaseAvailable(MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Available.get(index) + 1;
		this.Available.set(index, data);
	}

	/**
	 * @param proID   Ҫ������Դ�Ľ���ID
	 * @param message Ҫ�������Դ
	 */
	public void autoDecreaseRequest(int proID, MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Request.get(proID - 1).get(index) - 1;// ����ID�Ǵ�1��ʼ�ģ�����Ҫ��һ��
		this.Request.get(proID - 1).set(index, data);
	}

	/**
	 * @param proID   Ҫ������Դ�Ľ���ID
	 * @param message Ҫ�������Դ
	 */
	public void autoIncreaseRequest(int proID, MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Request.get(proID - 1).get(index) + 1;// ����ID�Ǵ�1��ʼ�ģ�����Ҫ��һ��
		this.Request.get(proID - 1).set(index, data);
	}

	/**
	 * @param proID   Ҫ�ͷ���Դ�Ľ���ID
	 * @param message Ҫ�������Դ
	 */
	public void autoDecreaseAlloction(int proID, MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Allocation.get(proID - 1).get(index) - 1;// ����ID�Ǵ�1��ʼ�ģ�����Ҫ��һ��
		this.Allocation.get(proID - 1).set(index, data);
	}

	/**
	 * @param proID   ��ռ����Դ�Ľ���ID
	 * @param message Ҫ�������Դ
	 */
	public void autoIncreaseAlloction(int proID, MESSAGE message) {
		int index = this.analyseMessage(message);
		int data = this.Allocation.get(proID - 1).get(index) + 1;// ����ID�Ǵ�1��ʼ�ģ�����Ҫ��һ��
		this.Allocation.get(proID - 1).set(index, data);
	}

	/**
	 * @param message
	 * @return ��Ϣ����Ӧ����Դ��š�
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
	 * @apiNote ��������㷨ִ�к���
	 * @return �����Ľ���
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
	 * @apiNote ��������
	 * @param deadProIdArray �����Ľ���ID����
	 */
	public void updateDeadProIDArray(ArrayList<Integer> deadProIdArray) {
		for (int i = 0; i < PCBQueue.getSize(); i++) {
			if (!this.finish.get(i)) {
				deadProIdArray.add(i + 1);
			}
		}
	}

	/**
	 * @apiNote ����Work��������ݣ�ʹ֮��Available����һ�¡�
	 */
	public void updateWorkArray() {
		for (int i = 0; i < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; i++) {
			this.Work.set(i, this.Available.get(i));
		}
	}

	/**
	 * @apiNote ��������������Ľ����ͷų���Դ�����Ѷ�Ӧ��finish��Ϊtrue��
	 * @param proID ������������Ľ���ID
	 */
	public void addAlloctionToWork(int proID) {
		for (int i = 0; i < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; i++) {
			int data = this.Work.get(i) + this.Allocation.get(proID - 1).get(i);
			this.Work.set(i, data);
		}
		this.finish.set(proID - 1, true);
	}

	/**
	 * @apiNote ����Allocation�������finish����
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
	 * @return �ҵĵĵ�ǰ����������Դ����Ľ��̵�ID��
	 */
	public Integer findSatisfiedProID() {
		boolean isFound = false;
		for (int i = 0; i < PCBQueue.getSize(); i++) {
			if (!this.finish.get(i)) {
				int j = 0;
				for (j = 0; j < BUFFER_NUM + REGISTER_NUM + DEVICE_NUM + MEMORY_SOURCE_NUM; j++) {
					if (this.Request.get(i).get(j) > this.Work.get(j)) {
						// ����һ����Դ��������ý��̡�
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

	// С���ܣ������ź�����Ѱ���ź�����
	public MessagePair findByMessage(MESSAGE message) {
		for (MessagePair messagePair : this.messages) {
			if (messagePair.getMessage() == message) {
				return messagePair;
			}
		}
		return null;
	}

	// ���ܣ�P����
	public boolean P(MESSAGE message, Integer proID) {
		MessagePair pair = this.findByMessage(message);
		if (pair != null) {
			pair.messNum--;
			if (pair.messNum < 0) {
				pair.wait(proID);
				this.autoIncreaseRequest(proID, message);// ����������Դ������û�����㣬�����������鶼��+1��
				return false;// ˵����������
			} else {
				this.autoDecreaseAvailable(message);// ����������Դ�ɹ�����ǰʣ����Դ�����-1
				if (message != MESSAGE.MEMORY_SOURCE_EMPTY && message != MESSAGE.MEMORY_SOURCE_FULL) {
					this.autoIncreaseAlloction(proID, message);// ���̳ɹ�ռ����Դ��ռ����+1
				}
				return true;// ˵��û�б�����
			}
		} else {
			return false;// �����ò�Ҫ�ã���Ϊû�����壬�Ҳ���pair���µ�
		}
	}

	// ���ܣ�V����
	public Integer V(MESSAGE message, Integer proID) {
		MessagePair pair = this.findByMessage(message);
		if (pair != null) {
			pair.messNum++;
			if (message != MESSAGE.MEMORY_SOURCE_EMPTY) {
				this.autoDecreaseAlloction(proID, message);// ���̽����ͷŵ���Դ
			}
			if (pair.messNum <= 0) {
				Integer wakeproId = pair.signal();// ˵����������
				this.autoDecreaseRequest(wakeproId, message);// �����ѽ��̵����뱻���㡣
				if (message != MESSAGE.MEMORY_SOURCE_FULL) {
					this.autoIncreaseAlloction(wakeproId, message);// �����ѽ���ռ���˽�������Դ
				}
				return wakeproId;
			} else {
				this.autoIncreaseAvailable(message);// װ��������Դ����
				return null;// ˵��û����Ҫ���ѵĽ���
			}
		} else {
			return null;// �����ò�Ҫ�ã���Ϊû�����壬�Ҳ���pair���µ�
		}
	}

	/**
	 * @param proID ����ID
	 * @return <code>true</code> �ɹ���������������ź����ĵȴ������еļ�¼�����ҽ�messNum++��
	 */
	public boolean clearProIDWaitingMessages(int proID) {
		for (int i = 0; i < this.messages.size(); i++) {
			if (this.messages.get(i).getMessage() != MESSAGE.MEMORY_SOURCE_FULL) {// ���˵������ź�
				for (int j = 0; j < this.messages.get(i).getProIDQueue().size(); j++) {
					if (this.messages.get(i).getProIDQueue().get(j).equals(proID)) {
						this.messages.get(i).setMessNum(this.messages.get(i).getMessNum() + 1);
						this.messages.get(i).getProIDQueue().remove(new Integer(proID));
						this.Request.get(j).set(i, 0);// ͬʱ��������Դ����0
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
