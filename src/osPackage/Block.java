package osPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author 贺宁
 * @implNote 输入输出一块数据的方法都被我加了synchronized关键词
 */
public class Block {
	private final static int BLOCK_SIZE = 256;// 一块拥有的双字节单位数

	protected int blockID;// 该块的物理块号或者外存地址
	protected String fileName;// 文件路径名

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
	 * 文件格式如下： 01234567890123456789 1111 1111 1111 1111 0000 1111 0000 1111 ……
	 */
	// 功能函数：每个单元只放16位的数组形式返回该块对应的数据
	public synchronized ArrayList<Integer> inputBlockData() {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		// 输入的起点是文件
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

	// 功能函数：将一块数据写入文件
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
		// 输出的中断是文件
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

	// 小小功能：将二进制字符串转为整型类对象
	public Integer binStringToInteger(String string) {
		Integer integer = new Integer(0);
		String tmpString;

		tmpString = string.substring(0, 4);// 截取0~3位
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

	// 小小功能：将整形类对象转为二进制字符串
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
