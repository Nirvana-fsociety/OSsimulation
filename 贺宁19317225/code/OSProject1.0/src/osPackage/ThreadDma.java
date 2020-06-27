package osPackage;

public class ThreadDma implements Runnable {
	DirectMemoryAccess dmaPoint;

	public ThreadDma(DirectMemoryAccess dma) {
		super();
		this.dmaPoint = dma;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(100);// 如果一直检测，会出现不检测的问题，具体我也不知道为啥。
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.dmaPoint.wholeJob();
		}
	}
}
