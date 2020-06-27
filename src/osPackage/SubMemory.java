package osPackage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import osPackage.PCBQueue.Compare;
import osPackage.PCBQueue.FreePageRegion;

/**
 * @implNote ��������ļ����ռ������Ҫʵ��λͼ����
 *
 */
public class SubMemory {
	private final static String SUBMAINMEM_DIRECTRY_PATH = new String("SubMemory");// �����ļ���·��

	private final static int EXCHANGE_BLOCKNUM = 128;// ��潻������128��
	private final static int FILE_BLOCKNUM = 1920;// �ļ�������

	private int exchangeAreaStartAddress;// ����������ʼ��ַ
	private int usedExchBlockNum;// ��������ʹ�ÿ�������ͷ��ʼ�ã�������һ������Ҫ�������ʱ��Ӧ��ֱ����β���ģ�
	private BitMap exchangeBitmap;// �������е�λʾͼ
	private ArrayList<SubBlock> exchangeArea;// ������

	private int fileAreaStartAddress;// �ļ�������ʼ��ַ
	private int userdFileBlockNum;// �ļ�����ʹ�ÿ���
	private BitMap fileBitmap;// �������е�λʾͼ
	private ArrayList<SubBlock> fileArea;// �ļ���

	public SubMemory(int fileAreaStartSubAdd, int exchangeAreaStartSubAdd) {
		super();

		File file = new File(SUBMAINMEM_DIRECTRY_PATH);
		if (!file.exists()) {// ����ļ��в�����
			file.mkdir();// �����ļ���
		}

		this.exchangeAreaStartAddress = exchangeAreaStartSubAdd;
		this.usedExchBlockNum = 0;
		this.exchangeArea = new ArrayList<SubBlock>();

		for (int i = 0; i < EXCHANGE_BLOCKNUM; i++) {
			SubBlock block = new SubBlock(SubBlock.takeTackIDFromSubAddress(this.exchangeAreaStartAddress + i),
					SubBlock.takeSectorIDFromSubAddress(this.exchangeAreaStartAddress + i));
			this.exchangeArea.add(block);
		}

		// ���ý�����λʾͼ
		this.setExchangeBitmap(new BitMap(EXCHANGE_BLOCKNUM));

		this.fileAreaStartAddress = fileAreaStartSubAdd;
		this.userdFileBlockNum = 2;// ������ռ0�ţ�������ռ1��
		this.fileArea = new ArrayList<SubBlock>();

		for (int i = 0; i < FILE_BLOCKNUM; i++) {
			SubBlock block = new SubBlock(SubBlock.takeTackIDFromSubAddress(this.fileAreaStartAddress + i),
					SubBlock.takeSectorIDFromSubAddress(this.fileAreaStartAddress + i));
			this.fileArea.add(block);
		}

		// �����ļ���λʾͼ
		this.setFileBitmap(new BitMap(FILE_BLOCKNUM));
		this.fileBitmap.alterTheBit(MemoryManageSystem.getGuideAddress(), true);
		this.fileBitmap.alterTheBit(MemoryManageSystem.getSuperblockAddress(), true);
	}

	// ��ȡָ������ַ��һ������
	public SubBlock getTheSubBlock(int subAdd) {
		if (subAdd < this.fileAreaStartAddress + FILE_BLOCKNUM) {
			return this.fileArea.get(subAdd);
		} else if (subAdd < this.exchangeAreaStartAddress + EXCHANGE_BLOCKNUM) {
			return this.exchangeArea.get(subAdd - FILE_BLOCKNUM);
		} else {
			return null;
		}
	}

	/**
	 * @apiNote ����ָ��Ҫ����Ŀ�������ȡ�����Ľ�����
	 * @param size Ҫ����������Ľ���������
	 * @return ��ȡ�Ľ��������׵�ַ��
	 */
	public Integer occupyNewExchangeArea(int size) {
		// ��ΪFreePageRegion��PCB�������е�һ�������࣬����ֻ�ܴ�PCB����������ȡ��
		ArrayList<FreePageRegion> list = new ArrayList<PCBQueue.FreePageRegion>();
		// ͳ�ƿ���������Щ��������PCB�б��е������Ӧ�㷨��
		int start = MemoryManageSystem.getExchangeStartAddress();
		int end = MemoryManageSystem.getExchangeStartAddress() + MemoryManageSystem.getExchangeBlocknum();
		for (int i = MemoryManageSystem.getExchangeStartAddress(); i < MemoryManageSystem.getExchangeStartAddress()
				+ MemoryManageSystem.getExchangeBlocknum();) {
			if (!this.exchangeBitmap.checkTheBit(i - this.exchangeAreaStartAddress)) {
				// �ҵ���һ����Ч�����ĵ�ַ
				start = i;
				end = MemoryManageSystem.getExchangeStartAddress() + MemoryManageSystem.getExchangeBlocknum();
				for (int j = start; j < MemoryManageSystem.getExchangeStartAddress()
						+ MemoryManageSystem.getExchangeBlocknum(); j++) {
					if (this.exchangeBitmap.checkTheBit(j - this.exchangeAreaStartAddress)) {
						// �ҵ���start��ʼ�ĵ�һ���ѱ�ռ��ҳ
						end = j;
						break;
					}
				}
				if (end - start >= size) {
					list.add(new FreePageRegion(start, end - start));
				}
				i = end + 1;
			} else {
				i++;
			}
		}
		if (list.size() == 0) {// ˵��û�п�����
			return null;
		} else {// ˵���п�����
			Compare compare = new Compare();
			Collections.sort(list, compare);// ���շ����ֶδ�С��������
			for (FreePageRegion freePageRegion : list) {
				if (freePageRegion.num >= size) {
					// �������㹻����
					this.exchangeBitmap.occupyBitmap(freePageRegion.pageID - this.exchangeAreaStartAddress, size);
					this.usedExchBlockNum += size;
					return freePageRegion.pageID;
				}
			}
		}
		return null;// û���㹻��Ŀ�����
	}

	/**
	 * @apiNote ������ʼ����ַ����������С���һ�ν�������ռ��
	 * @param startSubAdd Ҫ�ͷ����������ʼ��ַ��
	 * @param size        �ͷ����򳤶�
	 * @return true �ɹ��ͷ�
	 * @return false ��Ϊ�������ʼ��ַ���ļ������ڽ����������߳ߴ糬����ʹ�õĽ������������ͷ�ʧ�ܡ�
	 */
	public boolean releaseOldExchangeArea(int startSubAdd, int size) {
		if (startSubAdd < this.exchangeAreaStartAddress) {
			return false;// ��ַ�ڽ�������
		} else if (size > this.usedExchBlockNum) {
			return false;// Ҫ�ͷŵĿ���̬��
		} else {
			// ���Ҫ�ͷŵ������Ǵ�λͼ����һ��λ�ÿ�ʼ
			int startIndex = startSubAdd - this.exchangeAreaStartAddress;
			this.exchangeBitmap.releaseBitmap(startIndex, size);
			return true;// �ɹ��ͷ�
		}
	}

	/**
	 * @apiNote ���ݴ���ĳߴ�����ռ��������һ�οռ�
	 * @implNote Ϊ��ʹ��PCB������������Ӧ�㷨������������PCB���е��������Ϊ��̬��
	 * @param size
	 * @return ��ÿռ������׵�ַ
	 */
	public Integer applyJobBlocks(int size) {
		ArrayList<FreePageRegion> list = new ArrayList<PCBQueue.FreePageRegion>();
		// ͳ�ƿ���������Щ��������PCB�б��е������Ӧ�㷨��
		int start = MemoryManageSystem.getJobStartAddress();
		int end = MemoryManageSystem.getJobStartAddress() + MemoryManageSystem.getJobBlocknum();
		for (int i = MemoryManageSystem.getJobStartAddress(); i < MemoryManageSystem.getJobStartAddress()
				+ MemoryManageSystem.getJobBlocknum();) {
			if (!this.fileBitmap.checkTheBit(i - this.fileAreaStartAddress)) {
				// �ҵ���һ����Ч�����ĵ�ַ
				start = i;
				end = MemoryManageSystem.getJobStartAddress() + MemoryManageSystem.getJobBlocknum();
				for (int j = start; j < MemoryManageSystem.getJobStartAddress()
						+ MemoryManageSystem.getJobBlocknum(); j++) {
					if (this.fileBitmap.checkTheBit(j - this.fileAreaStartAddress)) {
						// �ҵ���start��ʼ�ĵ�һ����Чҳ
						end = j;
						break;
					}
				}
				if (end - start >= size) {
					list.add(new FreePageRegion(start, end - start));
				}
				i = end + 1;// ����¼��¼����¼�����ж�Ҫ�ݽ���
			} else {
				i++;
			}
		}
		if (list.size() == 0) {// ˵��û�п�����
			return null;
		} else {// ˵���п�����
			Compare compare = new Compare();
			Collections.sort(list, compare);// ���շ����ֶδ�С��������
			for (FreePageRegion freePageRegion : list) {
				if (freePageRegion.num >= size) {
					// �������㹻����
					this.fileBitmap.occupyBitmap(freePageRegion.pageID - this.fileAreaStartAddress, size);
					this.userdFileBlockNum += size;
					return freePageRegion.pageID;
				}
			}
		}
		return null;// û���㹻��Ŀ�����
	}

	public int getExchangeAreaStartPageNum() {
		return exchangeAreaStartAddress;
	}

	public void setExchangeAreaStartPageNum(int exchangeAreaStartPageNum) {
		this.exchangeAreaStartAddress = exchangeAreaStartPageNum;
	}

	public ArrayList<SubBlock> getExchangeArea() {
		return exchangeArea;
	}

	public void setExchangeArea(ArrayList<SubBlock> exchangeArea) {
		this.exchangeArea = exchangeArea;
	}

	public static String getSubmainmemDirectryPath() {
		return SUBMAINMEM_DIRECTRY_PATH;
	}

	public int getUsedBlockNum() {
		return usedExchBlockNum;
	}

	public void setUsedBlockNum(int usedBlockNum) {
		this.usedExchBlockNum = usedBlockNum;
	}

	public int getExchangeAreaStartAddress() {
		return exchangeAreaStartAddress;
	}

	public void setExchangeAreaStartAddress(int exchangeAreaStartAddress) {
		this.exchangeAreaStartAddress = exchangeAreaStartAddress;
	}

	public int getUsedExchBlockNum() {
		return usedExchBlockNum;
	}

	public void setUsedExchBlockNum(int usedExchBlockNum) {
		this.usedExchBlockNum = usedExchBlockNum;
	}

	public static int getExchangeBlocknum() {
		return EXCHANGE_BLOCKNUM;
	}

	public int getFileAreaStartAddress() {
		return fileAreaStartAddress;
	}

	public void setFileAreaStartAddress(int fileAreaStartAddress) {
		this.fileAreaStartAddress = fileAreaStartAddress;
	}

	public int getUserdFileBlockNum() {
		return userdFileBlockNum;
	}

	public void setUserdFileBlockNum(int userdFileBlockNum) {
		this.userdFileBlockNum = userdFileBlockNum;
	}

	public ArrayList<SubBlock> getFileArea() {
		return fileArea;
	}

	public void setFileArea(ArrayList<SubBlock> fileArea) {
		this.fileArea = fileArea;
	}

	public static int getFileBlocknum() {
		return FILE_BLOCKNUM;
	}

	public BitMap getFileBitmap() {
		return fileBitmap;
	}

	public void setFileBitmap(BitMap fileBitmap) {
		this.fileBitmap = fileBitmap;
	}

	public BitMap getExchangeBitmap() {
		return exchangeBitmap;
	}

	public void setExchangeBitmap(BitMap exchangeBitmap) {
		this.exchangeBitmap = exchangeBitmap;
	}

}
