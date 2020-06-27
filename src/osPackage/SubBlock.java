package osPackage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SubBlock extends Block {
	private final static String SUBMAINMEM_DIRECTRY_PATH = new String("SubMemory");// 辅存文件夹路径
	private final static String TRACK_PATH = new String("Track");// 磁道文件夹名称尾缀

	private final static int TRACK_BITNUM = 5;// 磁道号位数
	private final static int SECTOR_BITNUM = 6;// 扇区号

	private int trackNum;// 磁道号
	private int sectorNum;// 扇区号

	public SubBlock(int trackNum, int sectorNum) {
		super(sectorNum);// 由于外存一个扇区相当于一块，所以外存块号相当于扇区号。
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

	// 功能函数：生成文件绝对路径
	public void composeFilePath() {
		// 生成文件路径
		this.fileName += System.getProperty("user.dir");
		this.fileName += "\\" + SUBMAINMEM_DIRECTRY_PATH + "\\" + this.trackNum + TRACK_PATH + "\\" + this.sectorNum
				+ ".txt";
	}

	// 静态小功能：将外村地址解析为磁道号和扇区号
	public static int takeTackIDFromSubAddress(int subAddress) {
		return subAddress >> SECTOR_BITNUM;
	}

	public static int takeSectorIDFromSubAddress(int subAddress) {
		return subAddress & ((1 << SECTOR_BITNUM) - 1);
	}
}
