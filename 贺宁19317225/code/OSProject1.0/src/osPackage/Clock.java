package osPackage;

public class Clock {
	private final static int TIME_PEICE = 4;// 时间片
	private final static int JOBS_TIME = 5;// 每过五秒就检测是否有后备作业
	private final static int DEADLOCK_TIME = 9;// 每过9秒就检测是否有死锁
	private long lastTimePoint;
	private int secondNum;// 系统自运行起的秒数
	/**
	 * true 时间片已过<br>
	 * false 时间片未过<br>
	 */
	private Boolean timepeicePassed;
	private int timepeiceCountSecondNum;// 为了计算时间片而记录过了几个1秒
	private Boolean jobtimePassed;
	private int jobtimeCountSecondNum;// 为了计算后备作业检测时间过了几个1秒
	private Boolean deadlocktimePassed;
	private int deadlocktimeCountSecondNum;// 为了计算死锁检测时间过了几个1秒

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
	 * 一秒已过？
	 * 
	 * @apiNote 时钟计时线程核心函数：死循环体<br>
	 * @implNote 判断是否已过一秒，<br>
	 *           一秒过则更新最近计时点为当前毫秒数，<br>
	 *           并且统计距离上一个时间片中断已过几秒<br>
	 *           并且统计距离上一个作业检测时间已过几秒<br>
	 * @return true 一秒已过<br>
	 *         false 一秒未果<br>
	 */
	public boolean oneSecondPassed() {
		if (System.currentTimeMillis() >= (this.lastTimePoint + 1000L)) {
			this.recordCurrentMs();
			this.secondNum++;// 自开启后用不清空
			this.timepeiceCountSecondNum++;// 如果系统一直未检测时钟中断，这个值就不能清空
			this.jobtimeCountSecondNum++;// 如果未检测就不清空
			this.deadlocktimeCountSecondNum++;
			if (this.timepeiceCountSecondNum >= TIME_PEICE) {
				// 判断是否过了一个时间片，过了就清零计秒数的变量
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
	 * 时间片是否已过？
	 * 
	 * @return true 已过了一个时间片，至于到底过了几个时间片无所谓<br>
	 *         false 还未过一个时间片<br>
	 */
	public boolean timePiecePassed() {
		return this.timepeicePassed;
	}

	/**
	 * 后备作业检测时间是否已到？
	 * 
	 * @implNote 判断是否到了作业检测时间，到了就清零计秒数的变量
	 * @return true 已到时间，至于到底过了几个时间无所谓<br>
	 *         false 还未到一个作业检测时间<br>
	 */
	public boolean checkJobTimePassed() {
		return this.jobtimePassed;
	}

	/**
	 * @return true 已到时间，至于到底过了几个时间无所谓<br>
	 *         false 还未到一个死锁检测时间<br>
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
