package osPackage;

public class ThreadRequests implements Runnable {
	private JobManageSystem jmsPoint;
	private ProcessManageSystem pmsPoint;

	public ThreadRequests(JobManageSystem jms, ProcessManageSystem pms) {
		super();
		this.jmsPoint = jms;
		this.pmsPoint = pms;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(100);// 如果一直检测，会出现不检测的问题，具体我也不知道为啥。
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (this.jmsPoint) {
				if (this.jmsPoint.checkFutureRequest()) {
					this.pmsPoint
							.setRecordData("检测到请求到来，并创建为作业加入后被作业队列:" + this.pmsPoint.getClock().getSecondNum() + "s");
				}
			}
		}
	}

}
