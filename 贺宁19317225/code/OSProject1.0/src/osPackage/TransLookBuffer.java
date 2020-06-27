package osPackage;

import java.util.ArrayList;

public class TransLookBuffer {
	private final static int TLB_SIZE = 32;// ����С
	private ArrayList<PageTableItem> tlbItemQueue;// ��������

	public TransLookBuffer() {
		super();
		this.tlbItemQueue = new ArrayList<PageTableItem>();
	}

	// �����߼�ҳ��Ѱ���ض���ҳ����
	public PageTableItem findTheItem(int pageNum) {
		for (PageTableItem pageTableItem : this.tlbItemQueue) {
			if (pageTableItem.getPageID() == pageNum) {
				return pageTableItem;
			}
		}
		return null;
	}

	// ���µ�һ��ҳ�������TLB��
	public synchronized void enQueue(PageTableItem newItem) {
		synchronized (this) {
			PageTableItem item = this.findTheItem(newItem.getPageID());
			if (this.tlbItemQueue.size() < TLB_SIZE) {
				if (item != null) {//˵�����������һ�߼�ҳ�ŵĶ�Ӧ�
					this.tlbItemQueue.remove(item);//ɾ����������ҳ�����ǡ�
				}
				this.tlbItemQueue.add(newItem);
			} else {
				if (item != null) {//˵�����������һ�߼�ҳ�ŵĶ�Ӧ�
					this.tlbItemQueue.remove(item);//ɾ����������ҳ�����ǡ�
				} else {//˵�������û����һ�߼�ҳ�ŵĶ�Ӧ�
					this.tlbItemQueue.remove(0);//ɾ����һ������ĴӶ�β���롣
				}
				this.tlbItemQueue.add(newItem);
			}
		}
	}

	public ArrayList<PageTableItem> getTlbItemQueue() {
		return tlbItemQueue;
	}

	public void setTlbItemQueue(ArrayList<PageTableItem> tlbItemQueue) {
		this.tlbItemQueue = tlbItemQueue;
	}

	public static int getTlbSize() {
		return TLB_SIZE;
	}

}
