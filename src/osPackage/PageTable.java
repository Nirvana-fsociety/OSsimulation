package osPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PageTable {
	private final static int TABLE_LENGTH = 128;// ���Ͽյ�ҳ����и�128��ҳ���
	private final static int TABLE_ITEMSIZE = 4;// ҳ�����С
	private final static int TABLE_MAX_SIZE = 32;// �ܹ���פ���ĵ�ҳ���������32�����ڴ���32������顣
	private final static int TABLE_LIMMIT = 24;// �ﵽ������ƻ�Ҫ���½������ж�δ����ռ䣬��Ҫ������̡�
	private final static int PRO_MAX_SIZE = 8;// פ������С����������ӵ�е��ڴ����/Ҳ��������ӵ�е���Чҳ���
	private final static int PAGETABLE_OFFSET = 0;// ҳ��ƫ��Ĭ��Ϊ0

	private Address pageTableAddress;// ҳ����ڴ������ַ
	private int tableItemNum;// ҳ����ʵ�ʸ�����
	private ProItemnumPairList proItemnumPairList;// ����-ҳ�������б�
	private ArrayList<PageTableItem> tableItems;

	public PageTable(int pageID) {
		super();
		this.tableItems = new ArrayList<PageTableItem>();
		for (int i = 0; i < TABLE_LENGTH; i++) {
			// �����������һ���������ģ�����ÿһ���߼�ҳ��������ַһһ��Ӧ��ͬ������
			PageTableItem item = new PageTableItem(i, Integer.MAX_VALUE, false, 0, false, Integer.MAX_VALUE);
			// �������ֵ��ʾ��ַ�����ڻ��߿�Ų����ڡ����ڴ���ҳ����ʱ�ᱻת��Ϊ63��Ŀǰ������Ӱ�죩
			this.tableItems.add(item);
		}
		this.tableItemNum = 0;
		this.proItemnumPairList = new ProItemnumPairList();
		this.pageTableAddress = new Address(Address.getPhysicPageBitnum(), Address.getOffsetBitnum(), pageID,
				PAGETABLE_OFFSET);
	}

	// ���ܣ�ͳ����Ч��ҳ������
	public void updateTureItemNum() {
		synchronized (this) {
			this.tableItemNum = 0;
			for (int i = 0; i < this.tableItems.size(); i++) {
				PageTableItem pageTableItem = this.tableItems.get(i);
				if (pageTableItem != null) {
					if (pageTableItem.isStateBit()) {
						this.tableItemNum++;
					}
				} else {
					pageTableItem = null;//���ڲ���bug.
				}
			}
		}
	}

	// ���ܣ��ڲ���ҳ����ǰ�Ȳ������-ҳ�������ԣ������жԣ��Ͳ���Ҫ����
	public boolean insertOneProItemNumPair(int proID, int pageItemSize) {
		ProcessItemNumPair pair = this.proItemnumPairList.getPairOfThePro(proID);
		if (pair == null) {// ���ҵĶԲ����ڣ�Ҫ�����µĶ�
			if (!this.proItemnumPairList.insertOnePair(proID, 0, pageItemSize)) {
				// �������0������1����Ϊ���ڻ�Ҫ������
				// �����µĶ�ʧ�ܣ�˵�������Ѿ���5���ˣ��ܾ��˲��룬������Ҳ���С�
				return false;
			}
			// �����µĶԳɹ������Խ��к����Ĳ�����
		}
		return true;
	}

	/**
	 * @param proID
	 * @return false ��δ�ҵ���Ӧ���̵Ķ�
	 * @return true �ɹ�ɾ����Ӧ�Ľ��̵Ķ�
	 */
	public boolean deleteTheProItemNumPair(int proID) {
		ProcessItemNumPair pair = this.proItemnumPairList.getPairOfThePro(proID);
		if (pair == null) {
			return false;
		} else {
			this.proItemnumPairList.deleteThePair(proID);
			return true;
		}
	}

	// ���ܣ��ж��Ƿ�Ӧ�ù���
	public boolean needPending() {
		synchronized (this) {
			this.updateTureItemNum();
			if (this.tableItemNum > TABLE_LIMMIT) {
				// ����ܵ�ҳ�������ﵽ�����ٽ�ֵ��Ӧ�ÿ��ǹ���������滻ҳ������Ծܾ����롣
				return true;
			} else {
				return false;
			}
		}
	}

	// ����ǰԤ����ҳ������������һ����������ֶ����㣬��Чλ��Ϊ��
	public void preprocessingBeforeInsert(PageTableItem item) {
		synchronized (this) {
			// ҳ����ÿһ������һ
			for (PageTableItem pageTableItem : this.tableItems) {
				pageTableItem.IncreaseAccessText();
			}
			item.ClearAccessText();// �����ҳ����ķ����ֶ�
			item.setStateBit(true);// ��ҳ�������Чλһ��Ҫ��Ϊtrue
		}
	}

	// �ж��Ƿ��п���ҳ�������Ǿͷ���true
	public boolean hasFreeItemOfPro(int proID) {
		synchronized (this) {
			this.updateTureItemNum();
			if (this.proItemnumPairList.getPairOfThePro(proID).getItemNum() < PRO_MAX_SIZE) {
				return true;
			} else {
				return false;
			}
		}
	}

	// ��ҳ�����п���ʱ���ض�������ӵ�е�ҳ��μ���һ��
	public void insertOneItemOfPro(int pageNum, int blockNum, boolean stateBit, int accessText, boolean alterBit,
			int submemoryAddress, int proID) {
		synchronized (this) {
			// ǰ�������������ˣ��Ϳ���׼���������ˣ��ȴ����µ�ҳ����ٸ���������������ҳ�����ݡ�
			PageTableItem item = new PageTableItem(pageNum, blockNum, stateBit, accessText, alterBit, submemoryAddress);
			this.preprocessingBeforeInsert(item);
			// �����滻��ֱ�Ӳ��뼴��(��ȷ������£��߼�ҳ�ž���ҳ�������ţ���Ϊҳ��Ҳ�Ǵ��㿪ʼ������ֵ)
			this.tableItems.remove(item.getPageID());
			this.tableItems.add(item.getPageID(), item);
			// ��Ϊ�������µģ�����Ҫ��ͳ�����ݸ���
			this.tableItemNum++;
			this.proItemnumPairList.getPairOfThePro(proID).IncreaseItemNum();// ���Ҫ���ԣ���Ϊpair�ĸı��ܷ����list�ĸı䡣
		}
	}

	// �ڸý��̵�ҳ������������һ��
	public void replaceOneItemOfPro(int pageNum, int blockNum, boolean stateBit, int accessText, boolean alterBit,
			int submemoryAddress, int proID) {
		synchronized (this) {
			this.findItemByBlockNum(blockNum).setStateBit(false);// ɾ���ñ��滻��ҳ�������Чλ��Ϊfalse��
			// ǰ�������������ˣ��Ϳ���׼���������ˣ��ȴ����µ�ҳ����ٸ���������������ҳ�����ݡ�
			PageTableItem item = new PageTableItem(pageNum, blockNum, stateBit, accessText, alterBit, submemoryAddress);
			this.preprocessingBeforeInsert(item);
			// ˵�������̷����ҳ���������������ҳ����ֻҪ��ͬһ����ӵ��������һƬҳ��������м������Ч��ҳ���
			this.tableItems.remove(item.getPageID());// ��Ҫ�������λ�õ�ԭ����ɾ��
			this.tableItems.add(item.getPageID(), item);// ����ҳ�������ҳ��Ķ�Ӧλ��
		}
	}

	// ҳ����ȽϷ����ֶεķ�����
	public static class PageTableItemAccessTextLessThanCompare implements Comparator<PageTableItem> {
		@Override
		public int compare(PageTableItem o1, PageTableItem o2) {
			if (o1.getAccessText() < o2.getAccessText()) {
				return 1;
			} else {
				return -1;
			}
		}

	}

	// ҳ����Ƚ�ҳ�ŵķ�����
	public static class PageTableItemPageNumMoreThanCompare implements Comparator<PageTableItem> {
		@Override
		public int compare(PageTableItem o1, PageTableItem o2) {
			if (o1.getPageID() > o2.getPageID()) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	// ����ҳ�����ַ�ҵ��ǵڼ���ҳ����
	public Integer findItemIndexByPhysicAdd(Address address) {
		return this.pageTableAddress.distanceTo(address)
				/ (PageTable.TABLE_ITEMSIZE / MemoryManageSystem.getUnitSize());
	}

	// ����LRU�㷨Ѱ�ҵ�Ӧ�ñ��滻����һҳ
	public PageTableItem findLRUPageTableItem(Address proBasicAddress, int proID) {
		int startIndex = this.findItemIndexByPhysicAdd(proBasicAddress);
		// ��Ϊһ��ҳ�����Ϊ�洢��Ԫ����������/2
		ArrayList<PageTableItem> list = new ArrayList<PageTableItem>();
		for (int i = 0; i < this.proItemnumPairList.getPairOfThePro(proID).getItemSize(); i++) {
			PageTableItem item = new PageTableItem(this.tableItems.get(startIndex + i).getData());
			if (item.isStateBit()) {
				list.add(item);
			}
		}
		PageTableItemAccessTextLessThanCompare compare1 = new PageTableItemAccessTextLessThanCompare();
		Collections.sort(list, compare1);// ���շ����ֶδӴ�С����
		// Ϊ�˱�֤���ص��ǳ�Ա�б��һ������ã������м�����б�list�����ã����Ը���ҳ����ȥ����һ�
		PageTableItem tmpPoint = this.findItemByPageNum(list.get(0).getPageID());// ҳ����Ψһ�ģ�����ͨ��ҳ���ҡ�

		return tmpPoint;
	}

	// �����߼�ҳ��Ѱ���ض���ҳ����
	public PageTableItem findItemByPageNum(int pageNum) {
		for (PageTableItem pageTableItem : this.tableItems) {
			if (pageTableItem.getPageID() == pageNum) {
				return pageTableItem;
			}
		}
		return null;
	}

	// ��������ҳ��Ѱ���ض���ҳ����
	public PageTableItem findItemByBlockNum(int blockID) {
		if (blockID >= MemoryManageSystem.getUserStartPagenum()) {
			for (PageTableItem pageTableItem : this.tableItems) {
				//�����Ч�жϲ����٣���Ϊ����������������������Ч�Ĳ��ܱ�֤�����Ų������롣
				if (pageTableItem.isStateBit() && pageTableItem.getBlockID() == blockID) {
					return pageTableItem;
				}
			}
		}
		return null;
	}

	// ���ջ�ַ���߼�ҳ��Ѱ���ض�ҳ����(�ڿ�����е�����´洢����ϵͳ�����øú���)
	public PageTableItem findItemOfTheProcess(Address proBasicAddress, int pageID, int proID) {
		int itemStartNum = this.findItemIndexByPhysicAdd(proBasicAddress);
		ProcessItemNumPair pair = this.proItemnumPairList.getPairOfThePro(proID);
		if (pair != null) {
			// ��Ϊһ��ҳ�����Ϊ�洢��Ԫ����������/2
			int itemNum = pair.getItemSize();
			for (int i = 0; i < itemNum; i++) {
				PageTableItem tmpPoint = this.tableItems.get(itemStartNum + i);
				if ((tmpPoint.getPageID() == pageID)) {
					return this.tableItems.get(itemStartNum + i);
				}
			}
			return null;
		} else {
			return null;
		}
	}

	// ���ջ�ַ���߼�ҳ��Ѱ�Ҹ�ҳ������ַ
	public Integer findSubMemAddress(Address proBasicAddress, int pageNum, int proID) {
		int itemStartNum = this.findItemIndexByPhysicAdd(proBasicAddress);
		// ��Ϊһ��ҳ�����Ϊ�洢��Ԫ����������/2
		if (this.proItemnumPairList.getPairOfThePro(proID) == null) {
			return null;
		} else {
			int itemNum = this.proItemnumPairList.getPairOfThePro(proID).getItemSize();
			for (int i = 0; i < itemNum; i++) {
				PageTableItem tmpPoint = this.tableItems.get(itemStartNum + i);
				if ((tmpPoint.getPageID() == pageNum)) {
					return Integer.valueOf(this.tableItems.get(itemStartNum + i).getSubmemoryAddress());
				}
			}
			return null;
		}
	}

	// ������ʼҳ�š�����������ַ������ӳ��Ŀ���������ҳ��Ķ�Ӧ�������ַ�����ڴ�������ʱ����ʱ����Ρ����ݶλ�δ�����ڴ棩
	public void updateSubMemoryFirst(int startPageID, int textSubAdd, int textBlockNum, int dataSubAdd,
			int dataBlockNum, int exchangeSubAdd, int exchangeBlockNum) {
		for (int i = 0; i < textBlockNum; i++) {
			this.tableItems.get(startPageID + i).setSubmemoryAddress(textSubAdd + i);
		}
		for (int i = 0; i < dataBlockNum; i++) {
			this.tableItems.get(startPageID + textBlockNum + i).setSubmemoryAddress(dataSubAdd + i);
		}
		for (int i = 0; i < exchangeBlockNum - textBlockNum - dataBlockNum; i++) {
			this.tableItems.get(startPageID + textBlockNum + dataBlockNum + i)
					.setSubmemoryAddress(exchangeSubAdd + textBlockNum + dataBlockNum + i);// ����ջӳ����û����������ڽ���������󼸸�����
		}
	}

	// (������������ַ��PCB)����Ҫ�������ڴ������Ӧҳ��������ַ�������й����У�ֻҪ���ڴ�������棬֮ǰ��Ӧ�ø�������ַ��
	public boolean updateMainToSubItemSubAdd(int blockID, ProcessControlBlock pcb) {
		// ���ÿ��ǲ�����Ч��
		PageTableItem item = this.findItemByBlockNum(blockID);
		if (!item.isStateBit()) {
			return false;// ���ҳ����Ч���¾�ʧ�ܣ���Ϊ��Чҳֻ�����ֿ��ܣ���δ���룬���ļ����������滻����������
		}
		// �����߼�ҳ�����ҵ���Ӧ�Ľ���������ַ
		int startSubAdd = pcb.getControlInfo().getProcessAddress();// ���������
		int pageID = item.getPageID();// �߼�ҳ��
		int index = this.findItemIndexByPhysicAdd(pcb.getControlInfo().getPageTableAddress());
		int offset = pageID - index;// Ҫ�����ĸ����ַ������������ַ�������ҳ
		if (offset < pcb.getControlInfo().getPageTableItemNum()) {// δƫ�Ƹý��̵Ľ�����
			int resultSub = startSubAdd + offset;// ������ǽ��������+����
			if (item.getSubmemoryAddress() != resultSub) {// ����뵱ǰ����ַ��ͬ����Ӧ���滻
				item.setSubmemoryAddress(resultSub);
			}
			return true;
		} else {
			return false;
		}

	}

	/**
	 * @apiNote ����Чλ��λfalse��(���ڽ��̵ĳ����͹���)
	 * @param startAdd ����ӵ�е�ҳ���ַ
	 * @param itemNum  ҳ������
	 */
	public void leavePageTable(Address startAdd, int itemNum) {
		int index = this.findItemIndexByPhysicAdd(startAdd);
		for (int i = 0; i < itemNum; i++) {
			PageTableItem item = this.tableItems.get(index + i);
			item.setStateBit(false);
		}
	}

	// ���ݽ���ӵ�е�ҳ���ַ��ҳ�����������������Ч��ҳ����
	public ArrayList<PageTableItem> findTrueItems(Address startAdd, int itemNum) {
		int index = this.findItemIndexByPhysicAdd(startAdd);
		ArrayList<PageTableItem> items = new ArrayList<PageTableItem>();
		for (int i = 0; i < itemNum; i++) {
			PageTableItem item = this.tableItems.get(index + i);
			if (item.isStateBit()) {
				items.add(item);// �������ص�������ֵ���޸ķ��ص�������޸�ԭʼ����
			}
		}
		return items;
	}

	public void writePageTableIntoBlock(MainMemory mainMemory) {
		Address address = new Address(this.pageTableAddress.getData(), Address.getPhysicPageBitnum(),
				Address.getOffsetBitnum());
		for (PageTableItem pageTableItem : tableItems) {
			Integer a = new Integer((int) ((pageTableItem.getData() >> 16) & 0xffff));
			Integer b = new Integer((int) (pageTableItem.getData() & 0xffff));
			mainMemory.writeWordIntoMemory(address, a);
			address.updateAddress(address.getData() + MemoryManageSystem.getUnitSize());
			mainMemory.writeWordIntoMemory(address, b);
			address.updateAddress(address.getData() + MemoryManageSystem.getUnitSize());
		}
	}

	public ArrayList<PageTableItem> getTableItems() {
		return tableItems;
	}

	public void setTableItems(ArrayList<PageTableItem> tableItems) {
		this.tableItems = tableItems;
	}

	public int getTableItemNum() {
		return tableItemNum;
	}

	public void setTableItemNum(int tableItemNum) {
		this.tableItemNum = tableItemNum;
	}

	public static int getTableMaxSize() {
		return TABLE_LENGTH;
	}

	public static int getTableSize() {
		return TABLE_MAX_SIZE;
	}

	public static int getTableLength() {
		return TABLE_LENGTH;
	}

	public static int getTableLimmit() {
		return TABLE_LIMMIT;
	}

	public static int getProMaxSize() {
		return PRO_MAX_SIZE;
	}

	public ProItemnumPairList getProItemnumPairList() {
		return proItemnumPairList;
	}

	public void setProItemnumPairList(ProItemnumPairList proItemnumPairList) {
		this.proItemnumPairList = proItemnumPairList;
	}

	// ���ܣ�����ҳ�������ַ
	public void setPageTableAddress(int address) {
		this.pageTableAddress.updateAddress(address);
	}

	public Address getPageTableAddress() {
		return pageTableAddress;
	}

	public void setPageTableAddress(Address pageTableAddress) {
		this.pageTableAddress = pageTableAddress;
	}

	public static int getTableItemsize() {
		return TABLE_ITEMSIZE;
	}

	public static int getPagetableOffset() {
		return PAGETABLE_OFFSET;
	}
}
