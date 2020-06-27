package osPackage;

public class ProcessItemNumPair {
	private int proID;
	private int itemNum;//该进程实际拥有的有效项个数
	private int itemSize;//该进程拥有的页表项数（连续的全部所属页表项）
	
	public ProcessItemNumPair() {
		super();
		this.proID = -1;
		this.itemNum = 0;
		this.itemSize = 0;
	}

	public ProcessItemNumPair(int proID, int itemNum, int itemSize) {
		super();
		this.proID = proID;
		this.itemNum = itemNum;
		this.itemSize = itemSize;
	}

	public int getProID() {
		return proID;
	}

	public void setProID(int proID) {
		this.proID = proID;
	}

	public int getItemNum() {
		return itemNum;
	}

	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
	}
	
	public int getItemSize() {
		return itemSize;
	}

	public void setItemSize(int itemSize) {
		this.itemSize = itemSize;
	}

	//功能函数：页表项数自增一
	public void IncreaseItemNum() {
		this.itemNum++;
	}
	//功能函数：页表项数自减一
	public void decreaseItemNum() {
		this.itemNum--;
	}
}
