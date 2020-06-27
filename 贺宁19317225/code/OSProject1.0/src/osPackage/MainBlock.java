package osPackage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainBlock extends Block {
	private final static String MAINMEM_DIRECTRY = new String("MainMemory");// �����ļ���·��

	public MainBlock(int blockNum) {
		super(blockNum);

		this.composeFilePath();

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

	public static String getMainmemDirectry() {
		return MAINMEM_DIRECTRY;
	}

	// ���ܺ����������ļ�����·��
	public void composeFilePath() {
		// �����ļ�·��
		this.fileName += System.getProperty("user.dir");
		this.fileName += "\\" + MAINMEM_DIRECTRY + "\\" + this.blockID + ".txt";
	}
}
