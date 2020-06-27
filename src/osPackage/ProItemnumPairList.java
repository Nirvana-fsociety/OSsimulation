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

	// 功能函数：插入一个对
	public boolean insertOnePair(int proID, int itemNum, int itemSize) {
		if (this.arrayList.size() < PCBQueue.getSize()) {
			ProcessItemNumPair pair = new ProcessItemNumPair(proID, itemNum, itemSize);
			this.arrayList.add(pair);
			return true;
		} else {
			return false;
		}
	}

	// 功能函数：获取指定进程ID的对
	public ProcessItemNumPair getPairOfThePro(int proID) {
		for (ProcessItemNumPair processItemNumPair : this.arrayList) {
			if (processItemNumPair.getProID() == proID) {
				return processItemNumPair;
			}
		}
		return null;
	}

	// 功能函数：删除特定进程ID的对
	public void deleteThePair(int proID) {
		this.arrayList.remove(this.getPairOfThePro(proID));
	}
	
}
