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
				Thread.sleep(100);// ���һֱ��⣬����ֲ��������⣬������Ҳ��֪��Ϊɶ��
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (this.jmsPoint) {
				if (this.jmsPoint.checkFutureRequest()) {
					this.pmsPoint
							.setRecordData("��⵽��������������Ϊ��ҵ�������ҵ����:" + this.pmsPoint.getClock().getSecondNum() + "s");
				}
			}
		}
	}

}
