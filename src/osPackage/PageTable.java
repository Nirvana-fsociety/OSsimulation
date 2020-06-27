package osPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PageTable {
	private final static int TABLE_LENGTH = 128;// 算上空的页表项共有给128个页表项。
	private final static int TABLE_ITEMSIZE = 4;// 页表项大小
	private final static int TABLE_MAX_SIZE = 32;// 能够给驻留的的页表项个数：32个，内存中32个物理块。
	private final static int TABLE_LIMMIT = 24;// 达到这个限制还要有新进程运行而未分配空间，则要挂起进程。
	private final static int PRO_MAX_SIZE = 8;// 驻留集大小：进程最多可拥有的内存块数/也就是最多可拥有的有效页表项。
	private final static int PAGETABLE_OFFSET = 0;// 页表偏移默认为0

	private Address pageTableAddress;// 页表的内存物理地址
	private int tableItemNum;// 页表项实际个数。
	private ProItemnumPairList proItemnumPairList;// 进程-页表项数列表。
	private ArrayList<PageTableItem> tableItems;

	public PageTable(int pageID) {
		super();
		this.tableItems = new ArrayList<PageTableItem>();
		for (int i = 0; i < TABLE_LENGTH; i++) {
			// 交换区在外存一定是连续的，所以每一个逻辑页号与外存地址一一对应，同步连续
			PageTableItem item = new PageTableItem(i, Integer.MAX_VALUE, false, 0, false, Integer.MAX_VALUE);
			// 整型最大值表示地址不存在或者块号不存在。（在创建页表项时会被转化为63，目前来看不影响）
			this.tableItems.add(item);
		}
		this.tableItemNum = 0;
		this.proItemnumPairList = new ProItemnumPairList();
		this.pageTableAddress = new Address(Address.getPhysicPageBitnum(), Address.getOffsetBitnum(), pageID,
				PAGETABLE_OFFSET);
	}

	// 功能：统计有效的页表项数
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
					pageTableItem = null;//用于测试bug.
				}
			}
		}
	}

	// 功能：在插入页表项前先插入进程-页表项数对，若已有对，就不需要插入
	public boolean insertOneProItemNumPair(int proID, int pageItemSize) {
		ProcessItemNumPair pair = this.proItemnumPairList.getPairOfThePro(proID);
		if (pair == null) {// 查找的对不存在，要插入新的对
			if (!this.proItemnumPairList.insertOnePair(proID, 0, pageItemSize)) {
				// 这里插入0，不是1，因为后期还要自增。
				// 插入新的对失败，说明进程已经够5个了，拒绝了插入，本操作也不行。
				return false;
			}
			// 插入新的对成功，可以进行后续的操作。
		}
		return true;
	}

	/**
	 * @param proID
	 * @return false 并未找到对应进程的对
	 * @return true 成功删除对应的进程的对
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

	// 功能：判断是否应该挂起
	public boolean needPending() {
		synchronized (this) {
			this.updateTureItemNum();
			if (this.tableItemNum > TABLE_LIMMIT) {
				// 如果总的页表项数达到限制临界值，应该考虑挂起而不是替换页表项，所以拒绝插入。
				return true;
			} else {
				return false;
			}
		}
	}

	// 插入前预处理，页表其他项自增一，该项访问字段清零，有效位设为真
	public void preprocessingBeforeInsert(PageTableItem item) {
		synchronized (this) {
			// 页表项每一项自增一
			for (PageTableItem pageTableItem : this.tableItems) {
				pageTableItem.IncreaseAccessText();
			}
			item.ClearAccessText();// 清空新页表项的访问字段
			item.setStateBit(true);// 把页表项的有效位一定要置为true
		}
	}

	// 判断是否有空余页表项，如果是就返回true
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

	// 在页表项有空余时向特定进程所拥有的页表段加入一项
	public void insertOneItemOfPro(int pageNum, int blockNum, boolean stateBit, int accessText, boolean alterBit,
			int submemoryAddress, int proID) {
		synchronized (this) {
			// 前两个条件都过了，就可以准备插入项了，先创建新的页表项，再更新项数，最后更新页表内容。
			PageTableItem item = new PageTableItem(pageNum, blockNum, stateBit, accessText, alterBit, submemoryAddress);
			this.preprocessingBeforeInsert(item);
			// 无需替换，直接插入即可(正确的情况下，逻辑页号就是页表项的序号，因为页号也是从零开始的整型值)
			this.tableItems.remove(item.getPageID());
			this.tableItems.add(item.getPageID(), item);
			// 因为是增加新的，所以要将统计数据更新
			this.tableItemNum++;
			this.proItemnumPairList.getPairOfThePro(proID).IncreaseItemNum();// 这个要测试，因为pair的改变能否带动list的改变。
		}
	}

	// 在该进程的页表项用完后加入一项
	public void replaceOneItemOfPro(int pageNum, int blockNum, boolean stateBit, int accessText, boolean alterBit,
			int submemoryAddress, int proID) {
		synchronized (this) {
			this.findItemByBlockNum(blockNum).setStateBit(false);// 删除该被替换的页表项（将有效位改为false）
			// 前两个条件都过了，就可以准备插入项了，先创建新的页表项，再更新项数，最后更新页表内容。
			PageTableItem item = new PageTableItem(pageNum, blockNum, stateBit, accessText, alterBit, submemoryAddress);
			this.preprocessingBeforeInsert(item);
			// 说明给进程分配的页表项已满（这里的页表项只要求同一进程拥有相连的一片页表项，但是中间可有无效的页表项）
			this.tableItems.remove(item.getPageID());// 将要加入项的位置的原有项删除
			this.tableItems.add(item.getPageID(), item);// 把新页表项加入页表的对应位置
		}
	}

	// 页表项比较访问字段的方法类
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

	// 页表项比较页号的方法类
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

	// 根据页表项地址找到是第几个页表项
	public Integer findItemIndexByPhysicAdd(Address address) {
		return this.pageTableAddress.distanceTo(address)
				/ (PageTable.TABLE_ITEMSIZE / MemoryManageSystem.getUnitSize());
	}

	// 按照LRU算法寻找到应该被替换的那一页
	public PageTableItem findLRUPageTableItem(Address proBasicAddress, int proID) {
		int startIndex = this.findItemIndexByPhysicAdd(proBasicAddress);
		// 因为一个页表项长度为存储单元的两倍，故/2
		ArrayList<PageTableItem> list = new ArrayList<PageTableItem>();
		for (int i = 0; i < this.proItemnumPairList.getPairOfThePro(proID).getItemSize(); i++) {
			PageTableItem item = new PageTableItem(this.tableItems.get(startIndex + i).getData());
			if (item.isStateBit()) {
				list.add(item);
			}
		}
		PageTableItemAccessTextLessThanCompare compare1 = new PageTableItemAccessTextLessThanCompare();
		Collections.sort(list, compare1);// 按照访问字段从大到小排序
		// 为了保证返回的是成员列表的一项的引用，而非中间产物列表list的引用，所以根据页号再去找这一项。
		PageTableItem tmpPoint = this.findItemByPageNum(list.get(0).getPageID());// 页号是唯一的，可以通过页号找。

		return tmpPoint;
	}

	// 按照逻辑页号寻找特定的页表项
	public PageTableItem findItemByPageNum(int pageNum) {
		for (PageTableItem pageTableItem : this.tableItems) {
			if (pageTableItem.getPageID() == pageNum) {
				return pageTableItem;
			}
		}
		return null;
	}

	// 按照物理页号寻找特定的页表项
	public PageTableItem findItemByBlockNum(int blockID) {
		if (blockID >= MemoryManageSystem.getUserStartPagenum()) {
			for (PageTableItem pageTableItem : this.tableItems) {
				//这个有效判断不能少，因为根据物理块搜索如果不是有效的不能保证物理块号不是乱码。
				if (pageTableItem.isStateBit() && pageTableItem.getBlockID() == blockID) {
					return pageTableItem;
				}
			}
		}
		return null;
	}

	// 按照基址和逻辑页号寻找特定页表项(在快表不命中的情况下存储管理系统将调用该函数)
	public PageTableItem findItemOfTheProcess(Address proBasicAddress, int pageID, int proID) {
		int itemStartNum = this.findItemIndexByPhysicAdd(proBasicAddress);
		ProcessItemNumPair pair = this.proItemnumPairList.getPairOfThePro(proID);
		if (pair != null) {
			// 因为一个页表项长度为存储单元的两倍，故/2
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

	// 按照基址和逻辑页号寻找该页的外存地址
	public Integer findSubMemAddress(Address proBasicAddress, int pageNum, int proID) {
		int itemStartNum = this.findItemIndexByPhysicAdd(proBasicAddress);
		// 因为一个页表项长度为存储单元的两倍，故/2
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

	// 根据起始页号、交换区外存地址、进程映像的块数，更新页表的对应项的外存地址（用于创建进程时，此时程序段、数据段还未调入内存）
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
					.setSubmemoryAddress(exchangeSubAdd + textBlockNum + dataBlockNum + i);// 核心栈映像和用户缓冲区就在交换区的最后几个扇区
		}
	}

	// (参数是物理块地址、PCB)更新要换出的内存块所对应页表项外存地址（在运行过程中，只要有内存块调回外存，之前就应该更新外存地址）
	public boolean updateMainToSubItemSubAdd(int blockID, ProcessControlBlock pcb) {
		// 检查该块是不是有效的
		PageTableItem item = this.findItemByBlockNum(blockID);
		if (!item.isStateBit()) {
			return false;// 如果页面无效更新就失败（因为无效页只有两种可能：还未调入，在文件区；早已替换到交换区）
		}
		// 根据逻辑页表项找到对应的交换区外存地址
		int startSubAdd = pcb.getControlInfo().getProcessAddress();// 交换区起点
		int pageID = item.getPageID();// 逻辑页号
		int index = this.findItemIndexByPhysicAdd(pcb.getControlInfo().getPageTableAddress());
		int offset = pageID - index;// 要调换的辅存地址到交换区起点地址距离多少页
		if (offset < pcb.getControlInfo().getPageTableItemNum()) {// 未偏移该进程的交换区
			int resultSub = startSubAdd + offset;// 结果就是交换区起点+距离
			if (item.getSubmemoryAddress() != resultSub) {// 如果与当前外存地址不同，就应该替换
				item.setSubmemoryAddress(resultSub);
			}
			return true;
		} else {
			return false;
		}

	}

	/**
	 * @apiNote 将有效位该位false。(用于进程的撤销和挂起)
	 * @param startAdd 进程拥有的页表地址
	 * @param itemNum  页表项数
	 */
	public void leavePageTable(Address startAdd, int itemNum) {
		int index = this.findItemIndexByPhysicAdd(startAdd);
		for (int i = 0; i < itemNum; i++) {
			PageTableItem item = this.tableItems.get(index + i);
			item.setStateBit(false);
		}
	}

	// 根据进程拥有的页表地址、页表项数，获得所有有效的页表项
	public ArrayList<PageTableItem> findTrueItems(Address startAdd, int itemNum) {
		int index = this.findItemIndexByPhysicAdd(startAdd);
		ArrayList<PageTableItem> items = new ArrayList<PageTableItem>();
		for (int i = 0; i < itemNum; i++) {
			PageTableItem item = this.tableItems.get(index + i);
			if (item.isStateBit()) {
				items.add(item);// 这样返回的是引用值，修改返回的链表会修改原始链表
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

	// 功能：设置页表物理基址
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
