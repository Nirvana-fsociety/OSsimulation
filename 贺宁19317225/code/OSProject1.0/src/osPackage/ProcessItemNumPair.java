package osPackage;

public class ProcessItemNumPair {
	private int proID;
	private int itemNum;//�ý���ʵ��ӵ�е���Ч�����
	private int itemSize;//�ý���ӵ�е�ҳ��������������ȫ������ҳ���
	
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

	//���ܺ�����ҳ����������һ
	public void IncreaseItemNum() {
		this.itemNum++;
	}
	//���ܺ�����ҳ�������Լ�һ
	public void decreaseItemNum() {
		this.itemNum--;
	}
}
