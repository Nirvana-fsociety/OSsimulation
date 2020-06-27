package osPackage;

public class Clock {
	private final static int TIME_PEICE = 4;// ʱ��Ƭ
	private final static int JOBS_TIME = 5;// ÿ������ͼ���Ƿ��к���ҵ
	private final static int DEADLOCK_TIME = 9;// ÿ��9��ͼ���Ƿ�������
	private long lastTimePoint;
	private int secondNum;// ϵͳ�������������
	/**
	 * true ʱ��Ƭ�ѹ�<br>
	 * false ʱ��Ƭδ��<br>
	 */
	private Boolean timepeicePassed;
	private int timepeiceCountSecondNum;// Ϊ�˼���ʱ��Ƭ����¼���˼���1��
	private Boolean jobtimePassed;
	private int jobtimeCountSecondNum;// Ϊ�˼������ҵ���ʱ����˼���1��
	private Boolean deadlocktimePassed;
	private int deadlocktimeCountSecondNum;// Ϊ�˼����������ʱ����˼���1��

	public Clock() {
		super();
		timepeicePassed = new Boolean(false);
		jobtimePassed = new Boolean(false);
		setDeadlocktimePassed(new Boolean(false));
		this.recordCurrentMs();
		this.setSecondNum(0);
		this.timepeiceCountSecondNum = 0;
		this.setJobtimeCountSecondNum(0);
		this.setDeadlocktimeCountSecondNum(0);
	}

	public void recordCurrentMs() {
		this.lastTimePoint = System.currentTimeMillis();
	}

	/**
	 * һ���ѹ���
	 * 
	 * @apiNote ʱ�Ӽ�ʱ�̺߳��ĺ�������ѭ����<br>
	 * @implNote �ж��Ƿ��ѹ�һ�룬<br>
	 *           һ�������������ʱ��Ϊ��ǰ��������<br>
	 *           ����ͳ�ƾ�����һ��ʱ��Ƭ�ж��ѹ�����<br>
	 *           ����ͳ�ƾ�����һ����ҵ���ʱ���ѹ�����<br>
	 * @return true һ���ѹ�<br>
	 *         false һ��δ��<br>
	 */
	public boolean oneSecondPassed() {
		if (System.currentTimeMillis() >= (this.lastTimePoint + 1000L)) {
			this.recordCurrentMs();
			this.secondNum++;// �Կ������ò����
			this.timepeiceCountSecondNum++;// ���ϵͳһֱδ���ʱ���жϣ����ֵ�Ͳ������
			this.jobtimeCountSecondNum++;// ���δ���Ͳ����
			this.deadlocktimeCountSecondNum++;
			if (this.timepeiceCountSecondNum >= TIME_PEICE) {
				// �ж��Ƿ����һ��ʱ��Ƭ�����˾�����������ı���
				this.timepeiceCountSecondNum = 0;
				this.timepeicePassed = true;
			} else {
				this.timepeicePassed = false;
			}
			if (this.jobtimeCountSecondNum >= JOBS_TIME) {
				this.jobtimeCountSecondNum = 0;
				this.jobtimePassed = true;
			} else {
				this.jobtimePassed = false;
			}
			if (this.deadlocktimeCountSecondNum >= DEADLOCK_TIME) {
				this.deadlocktimeCountSecondNum = 0;
				this.deadlocktimePassed = true;
			} else {
				this.deadlocktimePassed = false;
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ʱ��Ƭ�Ƿ��ѹ���
	 * 
	 * @return true �ѹ���һ��ʱ��Ƭ�����ڵ��׹��˼���ʱ��Ƭ����ν<br>
	 *         false ��δ��һ��ʱ��Ƭ<br>
	 */
	public boolean timePiecePassed() {
		return this.timepeicePassed;
	}

	/**
	 * ����ҵ���ʱ���Ƿ��ѵ���
	 * 
	 * @implNote �ж��Ƿ�����ҵ���ʱ�䣬���˾�����������ı���
	 * @return true �ѵ�ʱ�䣬���ڵ��׹��˼���ʱ������ν<br>
	 *         false ��δ��һ����ҵ���ʱ��<br>
	 */
	public boolean checkJobTimePassed() {
		return this.jobtimePassed;
	}

	/**
	 * @return true �ѵ�ʱ�䣬���ڵ��׹��˼���ʱ������ν<br>
	 *         false ��δ��һ���������ʱ��<br>
	 */
	public boolean checkDeadLockTimePassed() {
		return this.deadlocktimePassed;
	}

	public long getLastTimePoint() {
		return lastTimePoint;
	}

	public void setLastTimePoint(long lastTimePoint) {
		this.lastTimePoint = lastTimePoint;
	}

	public int getCountSecondNum() {
		return timepeiceCountSecondNum;
	}

	public void setCountSecondNum(int countSecondNum) {
		this.timepeiceCountSecondNum = countSecondNum;
	}

	public static int getTimePeice() {
		return TIME_PEICE;
	}

	public int getJobtimeCountSecondNum() {
		return jobtimeCountSecondNum;
	}

	public void setJobtimeCountSecondNum(int jobtimeCountSecondNum) {
		this.jobtimeCountSecondNum = jobtimeCountSecondNum;
	}

	public int getSecondNum() {
		return secondNum;
	}

	public void setSecondNum(int secondNum) {
		this.secondNum = secondNum;
	}

	public Boolean getTimepeicePassed() {
		return timepeicePassed;
	}

	public void setTimepeicePassed(Boolean timepeicePassed) {
		this.timepeicePassed = timepeicePassed;
	}

	public int getTimepeiceCountSecondNum() {
		return timepeiceCountSecondNum;
	}

	public void setTimepeiceCountSecondNum(int timepeiceCountSecondNum) {
		this.timepeiceCountSecondNum = timepeiceCountSecondNum;
	}

	public static int getJobsTime() {
		return JOBS_TIME;
	}

	public Boolean getDeadlocktimePassed() {
		return deadlocktimePassed;
	}

	public void setDeadlocktimePassed(Boolean deadlocktimePassed) {
		this.deadlocktimePassed = deadlocktimePassed;
	}

	public int getDeadlocktimeCountSecondNum() {
		return deadlocktimeCountSecondNum;
	}

	public void setDeadlocktimeCountSecondNum(int deadlocktimeCountSecondNum) {
		this.deadlocktimeCountSecondNum = deadlocktimeCountSecondNum;
	}

}
