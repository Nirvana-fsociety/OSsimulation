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
				Thread.sleep(100);// ���һֱ��⣬����ֲ��������⣬������Ҳ��֪��Ϊɶ��
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.dmaPoint.wholeJob();
		}
	}
}
