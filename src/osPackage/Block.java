package osPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author ����
 * @implNote �������һ�����ݵķ��������Ҽ���synchronized�ؼ���
 */
public class Block {
	private final static int BLOCK_SIZE = 256;// һ��ӵ�е�˫�ֽڵ�λ��

	protected int blockID;// �ÿ�������Ż�������ַ
	protected String fileName;// �ļ�·����

	public Block(int blockNum) {
		super();
		this.blockID = blockNum;
		this.fileName = new String();
	}

	public int getBlockID() {
		return blockID;
	}

	public void setBlockNum(int blockNum) {
		this.blockID = blockNum;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public static int getBlockSize() {
		return BLOCK_SIZE;
	}

	/*
	 * �ļ���ʽ���£� 01234567890123456789 1111 1111 1111 1111 0000 1111 0000 1111 ����
	 */
	// ���ܺ�����ÿ����Ԫֻ��16λ��������ʽ���ظÿ��Ӧ������
	public synchronized ArrayList<Integer> inputBlockData() {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		// �����������ļ�
		File file = new File(this.fileName);
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String string = null;
			while ((string = bufferedReader.readLine()) != null) {
				if (string.charAt(0) != '0' && string.charAt(0) != '1') {
					string = string.trim();
				}
				arrayList.add(binStringToInteger(string));
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (arrayList.size() < BLOCK_SIZE) {
			int i = arrayList.size();
			for (; i < BLOCK_SIZE; i++) {
				arrayList.add(0);
			}
		}
		return arrayList;
	}

	// ���ܺ�������һ������д���ļ�
	public synchronized void outputBlockData(ArrayList<Integer> arrayList) {
		ArrayList<Integer> list = this.inputBlockData();
		if (list.size() == BLOCK_SIZE) {
			arrayList.addAll(list.subList(arrayList.size() - 1, BLOCK_SIZE - 1));
		}
		if (arrayList.size() != BLOCK_SIZE) {
			int i = arrayList.size();
			for (; i < BLOCK_SIZE; i++) {
				arrayList.add(0);
			}
		}
		// ������ж����ļ�
		File file = new File(this.fileName);
		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (int i = 0; i < arrayList.size(); i++) {
				bufferedWriter.write(this.integerToBinString(arrayList.get(i)));
				bufferedWriter.flush();
			}
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// СС���ܣ����������ַ���תΪ���������
	public Integer binStringToInteger(String string) {
		Integer integer = new Integer(0);
		String tmpString;

		tmpString = string.substring(0, 4);// ��ȡ0~3λ
		integer = Integer.valueOf(tmpString, 2);
		integer *= 16;

		tmpString = string.substring(5, 9);
		integer += Integer.valueOf(tmpString, 2);
		integer *= 16;

		tmpString = string.substring(10, 14);
		integer += Integer.valueOf(tmpString, 2);
		integer *= 16;

		tmpString = string.substring(15, 19);
		integer += Integer.valueOf(tmpString, 2);

		return integer;
	}

	// СС���ܣ������������תΪ�������ַ���
	public String integerToBinString(Integer integer) {
		String string = new String(Integer.toString(integer.intValue(), 2));
		int len = 16 - string.length();
		String str = new String();
		for (int i = 0; i < len; i++) {
			str += "0";
		}
		str += string;
		String result = new String();
		result += str.substring(0, 4);
		result += " ";
		result += str.substring(4, 8);
		result += " ";
		result += str.substring(8, 12);
		result += " ";
		result += str.substring(12, 16);
		result += " \n";
		return result;
	}
}
