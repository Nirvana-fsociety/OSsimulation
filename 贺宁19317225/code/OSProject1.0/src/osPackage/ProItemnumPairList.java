package osPackage;

import java.util.ArrayList;

public class ProItemnumPairList {
	private ArrayList<ProcessItemNumPair> arrayList;

	public ProItemnumPairList() {
		super();
		this.arrayList = new ArrayList<ProcessItemNumPair>();
	}

	public ArrayList<ProcessItemNumPair> getArrayList() {
		return arrayList;
	}

	public void setArrayList(ArrayList<ProcessItemNumPair> arrayList) {
		this.arrayList = arrayList;
	}

	// ���ܺ���������һ����
	public boolean insertOnePair(int proID, int itemNum, int itemSize) {
		if (this.arrayList.size() < PCBQueue.getSize()) {
			ProcessItemNumPair pair = new ProcessItemNumPair(proID, itemNum, itemSize);
			this.arrayList.add(pair);
			return true;
		} else {
			return false;
		}
	}

	// ���ܺ�������ȡָ������ID�Ķ�
	public ProcessItemNumPair getPairOfThePro(int proID) {
		for (ProcessItemNumPair processItemNumPair : this.arrayList) {
			if (processItemNumPair.getProID() == proID) {
				return processItemNumPair;
			}
		}
		return null;
	}

	// ���ܺ�����ɾ���ض�����ID�Ķ�
	public void deleteThePair(int proID) {
		this.arrayList.remove(this.getPairOfThePro(proID));
	}
	
}
