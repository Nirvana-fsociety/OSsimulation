package osPackage;

import java.util.ArrayList;

public class TransLookBuffer {
	private final static int TLB_SIZE = 32;// 快表大小
	private ArrayList<PageTableItem> tlbItemQueue;// 快表项队列

	public TransLookBuffer() {
		super();
		this.tlbItemQueue = new ArrayList<PageTableItem>();
	}

	// 按照逻辑页号寻找特定的页表项
	public PageTableItem findTheItem(int pageNum) {
		for (PageTableItem pageTableItem : this.tlbItemQueue) {
			if (pageTableItem.getPageID() == pageNum) {
				return pageTableItem;
			}
		}
		return null;
	}

	// 将新的一个页表项加入TLB中
	public synchronized void enQueue(PageTableItem newItem) {
		synchronized (this) {
			PageTableItem item = this.findTheItem(newItem.getPageID());
			if (this.tlbItemQueue.size() < TLB_SIZE) {
				if (item != null) {//说明快表中有这一逻辑页号的对应项。
					this.tlbItemQueue.remove(item);//删除后用来自页表的项覆盖。
				}
				this.tlbItemQueue.add(newItem);
			} else {
				if (item != null) {//说明快表中有这一逻辑页号的对应项。
					this.tlbItemQueue.remove(item);//删除后用来自页表的项覆盖。
				} else {//说明快表中没有这一逻辑页号的对应项。
					this.tlbItemQueue.remove(0);//删除第一项。新来的从队尾进入。
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
