package osPackage;

public class ThreadProcess implements Runnable {
	private ProcessManageSystem pmsPoint;// 进程管理系统指针
	

	public ThreadProcess(ProcessManageSystem pms) {
		super();
		this.pmsPoint = pms;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.pmsPoint.executeOnce();
		}
	}
}
