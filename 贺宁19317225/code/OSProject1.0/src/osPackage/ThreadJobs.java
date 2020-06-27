package osPackage;

public class ThreadJobs implements Runnable {
	private ProcessManageSystem pmsPoint;// 进程管理系统指针
	private JobManageSystem jmsPoint;

	public ThreadJobs(ProcessManageSystem pms, JobManageSystem jms) {
		super();
		this.pmsPoint = pms;
		this.jmsPoint = jms;
	}

	/**
	 * @apiNote 检测作业后备队列，线程死循环体。
	 * @return 作业成功创建进程
	 */
	public boolean checkJobQueue() {
		if (this.pmsPoint.getClock().checkJobTimePassed()) {
			this.pmsPoint.setRecordData("检测后备队列。");
			synchronized (this.jmsPoint.getJobQueue()) {
				if (this.jmsPoint.getJobQueue().isEmpty()) {
					return false;
				} else {
					if (this.pmsPoint.createPro(this.jmsPoint.getJobQueue().get(0))) {
						this.jmsPoint.getRunningJobsQueue().add(this.jmsPoint.getJobQueue().get(0));
						this.jmsPoint.getJobQueue().remove(0);
						return true;
					} else {
						return false;
					}
				}
			}
		} else {
			return false;
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(100);// 如果一直检测，会出现不检测的问题，具体我也不知道为啥。
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.checkJobQueue();
		}
	}
}
