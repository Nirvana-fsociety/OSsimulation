package osPackage;

public class ThreadJobs implements Runnable {
	private ProcessManageSystem pmsPoint;// ���̹���ϵͳָ��
	private JobManageSystem jmsPoint;

	public ThreadJobs(ProcessManageSystem pms, JobManageSystem jms) {
		super();
		this.pmsPoint = pms;
		this.jmsPoint = jms;
	}

	/**
	 * @apiNote �����ҵ�󱸶��У��߳���ѭ���塣
	 * @return ��ҵ�ɹ���������
	 */
	public boolean checkJobQueue() {
		if (this.pmsPoint.getClock().checkJobTimePassed()) {
			this.pmsPoint.setRecordData("���󱸶��С�");
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
				Thread.sleep(100);// ���һֱ��⣬����ֲ��������⣬������Ҳ��֪��Ϊɶ��
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.checkJobQueue();
		}
	}
}
