package osPackage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SubBlock extends Block {
	private final static String SUBMAINMEM_DIRECTRY_PATH = new String("SubMemory");// �����ļ���·��
	private final static String TRACK_PATH = new String("Track");// �ŵ��ļ�������β׺

	private final static int TRACK_BITNUM = 5;// �ŵ���λ��
	private final static int SECTOR_BITNUM = 6;// ������

	private int trackNum;// �ŵ���
	private int sectorNum;// ������

	public SubBlock(int trackNum, int sectorNum) {
		super(sectorNum);// �������һ�������൱��һ�飬����������൱�������š�
		this.trackNum = trackNum;
		this.sectorNum = sectorNum;
		this.composeFilePath();

		File dir = new File(SUBMAINMEM_DIRECTRY_PATH + "\\" + this.trackNum + TRACK_PATH);
		if (!dir.exists()) {
			dir.mkdir();
		}

		File file = new File(this.fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();

				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int i = 0; i < Block.getBlockSize(); i++) {
					tmp.add(0);
				}
				this.outputBlockData(tmp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public int getTrackNum() {
		return trackNum;
	}

	public void setTrackNum(int trackNum) {
		this.trackNum = trackNum;
	}

	public int getSectorNum() {
		return sectorNum;
	}

	public void setSectorNum(int sectorNum) {
		this.sectorNum = sectorNum;
	}

	public static String getSubmainmemDirectryPath() {
		return SUBMAINMEM_DIRECTRY_PATH;
	}

	public static String getTrackPath() {
		return TRACK_PATH;
	}

	public static int getTrackBitnum() {
		return TRACK_BITNUM;
	}

	public static int getSectorBitnum() {
		return SECTOR_BITNUM;
	}

	// ���ܺ����������ļ�����·��
	public void composeFilePath() {
		// �����ļ�·��
		this.fileName += System.getProperty("user.dir");
		this.fileName += "\\" + SUBMAINMEM_DIRECTRY_PATH + "\\" + this.trackNum + TRACK_PATH + "\\" + this.sectorNum
				+ ".txt";
	}

	// ��̬С���ܣ�������ַ����Ϊ�ŵ��ź�������
	public static int takeTackIDFromSubAddress(int subAddress) {
		return subAddress >> SECTOR_BITNUM;
	}

	public static int takeSectorIDFromSubAddress(int subAddress) {
		return subAddress & ((1 << SECTOR_BITNUM) - 1);
	}
}
