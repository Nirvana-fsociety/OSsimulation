package osPackage;

/**
 * @apiNote ʱ���߳�
 *
 */
public class ThreadTime implements Runnable {
	private Clock clockPoint;
	public ThreadTime(Clock clock) {
		super();
		this.clockPoint = clock;
	}

	
	@Override
	public void run() {
		while (true) {
			this.clockPoint.oneSecondPassed();
		}
	}

}
