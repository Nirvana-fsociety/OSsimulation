package osPackage;

import java.util.ArrayList;

/*
 * 	64������飬����㷨�������Ӧ����ÿλ��Ӧ2��6�η���
 * 	����free_area���鳤Ϊ7��0~6��
 * 	0��λͼӵ��64λ��1λ=1��
 * 	1��λͼӵ��32λ��1λ=2��
 * 	2��λͼӵ��16λ��1λ=4��
 * 	3��λͼӵ��8λ��1λ=8��
 * 	4��λͼӵ��4λ��1λ=16��
 * 	5��λͼӵ��2λ��1λ=32��
 * 	6��λͼӵ��1λ��1λ=64��
 */
public class BuddyAllocator {
	private final static int FREEAREA_NUM = 6;
	private int pageNum;// ҳ����(ֻ������2����������)
	private int lessBorder;// �±߽磨����ȡ����
	private int moreBorder;// �ϱ߽磨����ȡ����

	private ArrayList<FreeArea> freeAreas;

	private ArrayList<page> freeBlocks;// ���п�����

	public class page {
		// ����ҳ������������±꣬����Ϊһ����Ա����
		// ��Чλ��bitmap��ǵģ�����Ҳ����Ҫ
		// �Ƿ��޸���ҳ���¼�����Բ���Ҫ
		private int blockNum;// ��Ӧ��������
		private int belongID;// ��¼��ҳ�������ĸ�����

		public page(int blockNum) {
			super();
			this.blockNum = blockNum;
			this.belongID = -1;
		}

		public int getBlockNum() {
			return blockNum;
		}

		public void setBlockNum(int blockNum) {
			this.blockNum = blockNum;
		}

		public int getBelongID() {
			return belongID;
		}

		public void setBelongID(int belongID) {
			this.belongID = belongID;
		}

	}

	// ������������
	public class FreeArea {
		private int size;// ���sizeָ����λͼ��λ��
		private ArrayList<page> freeBlocks;// ���п�����
		private BitMap bitMap;// ��Ӧ��λͼ��

		public FreeArea(int size, ArrayList<page> freeBlocks) {
			super();
			this.size = size;
			this.freeBlocks = freeBlocks;
			this.bitMap = new BitMap(size);// sizeλ��λͼ
		}

		// ���ܣ�����ָ���������ҳ���ҳ���
		public void setBlocksProID(int groupNum, int proID) {
			int startIndex = groupNum * (pageNum / this.size);
			for (int i = 0; i < pageNum / this.size; i++) {
				this.freeBlocks.get(startIndex + i).setBelongID(proID);
			}
		}

		// ���ܣ���ȡָ���������ҳ���ҳ���
		public ArrayList<Integer> getBlockNumList(int groupNum) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			int startIndex = groupNum * (pageNum / this.size);
			for (int i = 0; i < pageNum / this.size; i++) {
				list.add(this.freeBlocks.get(startIndex + i).getBlockNum());
			}
			return list;
		}

		// ���ܣ�����ҳ��ź�FreeArea���±��ҵ���Ӧ������
		public Integer findGroupIndexByPageNum(int num) {
			return (num - lessBorder) / (pageNum / this.size);
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public BitMap getBitMap() {
			return bitMap;
		}

		public void setBitMap(BitMap bitMap) {
			this.bitMap = bitMap;
		}

		public ArrayList<page> getFreeBlocks() {
			return freeBlocks;
		}

		public void setFreeBlocks(ArrayList<page> freeBlocks) {
			this.freeBlocks = freeBlocks;
		}

	}

	public BuddyAllocator(int less, int pageNum) {
		this.lessBorder = less;
		this.pageNum = pageNum;
		this.moreBorder = this.lessBorder + this.pageNum;
		int size = pageNum;
		this.freeBlocks = new ArrayList<page>();
		for (int i = 0; i < this.pageNum; i++) {
			page p = new page(i + lessBorder);
			this.freeBlocks.add(p);
		}
		// ÿ��FreeArea����Ӧ64�飬ÿһ�ݶ���һ�ݿ������ڷ���ͻ���ʱ���ƶ��������ֻ�޸�λͼ����page��ֻ�޸��������̼��ɡ�
		this.freeAreas = new ArrayList<BuddyAllocator.FreeArea>();// ����ĸ�����λͼ�ĸ�����ͬ��
		for (int i = 0; i < FREEAREA_NUM; i++) {
			FreeArea freeArea = new FreeArea(size, this.freeBlocks);
			this.freeAreas.add(freeArea);
			size /= 2;// ��size���¡�
		}
		this.freeAreas.get(FREEAREA_NUM - 1).getBitMap().alterTheBit(0, true);// ֻ�����һ����ڣ���һ����32�顣

	}

	// ����number������ҳ�򣬷��ص���ҳ������飨number������2���������ݣ�
	public synchronized ArrayList<Integer> alloc_pages(int number, int proID) {
		// ��������������ҵ����ʵ�FreeArea
		int No = Integer.MAX_VALUE;// ���Ӧ������FreeArea���±�(��ʼֵ��Ϊ������������û���ҵ��Ͳ���������ѭ��ֱ�ӷ���null)

		for (int i = 0; i < FREEAREA_NUM; i++) {
			if (number == this.calculateGroupSize(i)) {
				// ��Ҫ��ҳ�����뵱ǰFreeAreaһ��ӵ�е�ҳ�����
				No = i;
				break;
			}
		}

		for (int i = No; i < FREEAREA_NUM; i++) {
			// ֮���ÿһ��free Area��Ĵ�С�����԰�סnumber��û��Ҫ�����жϡ�
			Integer integer = this.freeAreas.get(i).getBitMap().findFreeGroupIndex();
			if (integer != null) {
				// ˵����ǰ��freeArea�п��е���
				// ����ҳ����䡣
				return this.integrateGroup0(integer, i, number, proID);
			} // �޿����������Ѱ��
		}

		return null;//bug
	}

	// ���ܣ�����ָ��freeArea����ĳ���
	public int calculateGroupSize(int freeAreaIndex) {
		return pageNum / this.freeAreas.get(freeAreaIndex).getSize();
	}

	// ���ܣ������飨�ݹ飩���룺��ǰ�飬��ǰ�㣬Ŀ���鳤�ȣ���������ʱ����
	public ArrayList<Integer> integrateGroup0(int groupIndex, int level, int targetSize, int proID) {
		int size = this.calculateGroupSize(level);
		if (size == targetSize) {
			// �ж������� ���������ǰ����Ŀ����ȳ��� �򡪡����ɶ�Ӧ��������š�
			this.freeAreas.get(level).getBitMap().alterTheBit(groupIndex, false);
			this.freeAreas.get(level).setBlocksProID(groupIndex, proID);
			return this.freeAreas.get(level).getBlockNumList(groupIndex);
		} else {
			// ÿ�����������
			// ����ǰһ����Ϊ�ȳ�������
			this.freeAreas.get(level).getBitMap().alterTheBit(groupIndex, false);
			// ��һ��ҵ���һ��
			this.freeAreas.get(level - 1).getBitMap().alterTheBit((groupIndex * 2) + 1, true);
			// ǰһ��������һ������
			return this.integrateGroup0(groupIndex * 2, level - 1, targetSize, proID);
		}
	}

	// ���ܣ���������ҳ��ŵ��������(����һ����2���������ݣ�Ҳ�����������ŵ�)������������ϵ��ڼ�����
	public void free_pages(ArrayList<Integer> blockNumList) {
//		for (int i = 0; i < blockNumList.size(); i++) {
//			if (blockNumList.get(i) == 63) {
//				blockNumList.set(i, 63);//����bug
//			}
//		}
		// һ��ҳ��һ��ҳ��ļ��룬ÿ����һ��ҳ����������麯����������һ��
		for (int i = 0; i < blockNumList.size(); i++) {
			int freeAreaIndex = this.calculateFreeAreaIndex(1);
			// ��������ݵ�ǰҪ����������������żȷ��
			Integer groupNum0;
			// ��������ţ���Ϊtrue
			Integer groupNum1 = this.freeAreas.get(freeAreaIndex).findGroupIndexByPageNum(blockNumList.get(i));
			if (groupNum1 % 2 == 1) {
				// �����ǰ�����������������ߵ����
				groupNum0 = groupNum1 - 1;
			} else {
				// �����ǰ�����ż���������ұߵ����
				groupNum0 = groupNum1 + 1;
			}
			if (!this.freeAreas.get(freeAreaIndex).getBitMap().checkTheBit(groupNum0)) {
				// �������������Ϊfalse���͸�Ϊnull��
				groupNum0 = null;
			}
			this.freeAreas.get(freeAreaIndex).getBitMap().alterTheBit(groupNum1, true);
			this.integrateGroup1(groupNum1, groupNum0, freeAreaIndex, blockNumList.get(i));
		}
	}

	// ���ܣ�����size�Ĵ�С��freeArea���½Ǳ�
	public int calculateFreeAreaIndex(int groupSize) {
		int size = groupSize;
		int exp = 0;
		while (size != 1) {
			size /= 2;
			exp++;
		}
		return exp;
	}

	// ���ܣ������飨�ݹ飩�������ͷ�ʱ����
	public Integer integrateGroup1(Integer newGroupNum, Integer oldGroupNum, int level, int pageNum) {
		// �ж��������������Ų����ڻ���������Ų����ڻ��߽�С��Ϊ�������ͷ��أ���ʾ���ϵ�����
		if (oldGroupNum == null) {
			return level;// ����������ϵ�����һ��
		} else if (Math.abs(newGroupNum - oldGroupNum) > 1) {
			return level;
		} else if (Math.min(newGroupNum, oldGroupNum) % 2 == 1) {
			return level;
		} else {
			// ÿ�������
			// ���������ڿ�ϲ�
			this.freeAreas.get(level).getBitMap().alterTheBit(newGroupNum, false);
			this.freeAreas.get(level).getBitMap().alterTheBit(oldGroupNum, false);
			// �ҵ���һ��Ӧ�÷���������
			Integer groupNum1 = this.freeAreas.get(level + 1).findGroupIndexByPageNum(pageNum);
			// Ѱ����һ��������
			Integer groupNum0;
			if (groupNum1 % 2 == 1) {
				// �����ǰ�����������������ߵ����
				groupNum0 = groupNum1 - 1;
			} else {
				// �����ǰ�����ż���������ұߵ����
				groupNum0 = groupNum1 + 1;
			}
			if (groupNum0 < this.freeAreas.get(level + 1).getBitMap().getMapSize() && groupNum0 >= 0) {
				if (!this.freeAreas.get(level + 1).getBitMap().checkTheBit(groupNum0)) {
					// �������������Ϊfalse���͸�Ϊnull��
					groupNum0 = null;
				}
			} else {
				groupNum0 = null;
			}
			// ���µ�λ����Ϊtrue
			this.freeAreas.get(level + 1).getBitMap().alterTheBit(groupNum1, true);
			// ������һ�����
			return this.integrateGroup1(groupNum1, groupNum0, level + 1, pageNum);
		}
	}

	public int getLessBorder() {
		return lessBorder;
	}

	public void setLessBorder(int lessBorder) {
		this.lessBorder = lessBorder;
	}

	public int getMoreBorder() {
		return moreBorder;
	}

	public void setMoreBorder(int moreBorder) {
		this.moreBorder = moreBorder;
	}

	public ArrayList<FreeArea> getFreeAreas() {
		return freeAreas;
	}

	public void setFreeAreas(ArrayList<FreeArea> freeAreas) {
		this.freeAreas = freeAreas;
	}

	public static int getFreeareaNum() {
		return FREEAREA_NUM;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

}
