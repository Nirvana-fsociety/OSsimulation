package osPackage;

public class BitMap {
	/**
	 * λͼ�ߴ�
	 */
	private int mapSize;
	/**
	 * λͼ��������
	 */
	private boolean[] bitList;

	public BitMap() {
		super();
		this.mapSize = 0;
		this.bitList = null;
	}

	public BitMap(int mapSize) {
		this.mapSize = mapSize;
		this.bitList = new boolean[this.mapSize];
		// �ڻ���㷨�У����ֵ�λͼÿһλ���ǲ����õģ�ֻ�����һ��λͼÿһλ�ǿ��õġ�
		for (int i = 0; i < bitList.length; i++) {
			this.bitList[i] = false;// false��ʾ����㷨�иÿ鲻���ڡ�������ļ������ʾ�տ顣
		}
	}

	// ���ܣ�����λͼ��һλ����ֵ
	public boolean alterTheBit(int loca, boolean value) {
		if (loca < this.mapSize) {
			this.bitList[loca] = value;
			return true;
		} else {
			return false;
		}
	}

	// ���ܣ����ָ��λ����ֵ
	public boolean checkTheBit(int loca) {
		return this.bitList[loca];
	}

	// ����㷨ר�ù��ܣ���ø�λͼ�ĵ�һ����������±�
	public Integer findFreeGroupIndex() {
		for (int i = 0; i < bitList.length; i++) {
			if (this.bitList[i]) {
				return Integer.valueOf(i);
			}
		}
		return null;
	}

	/**
	 * @apiNote ��λͼ�еļ�λռ��(�������ڻ���㷨����Ϊ��ֵ����������岻ͬ)
	 * @param headIndex ��Ҫռ�õ����Ŀ�ͷ
	 * @param num       ��Ҫռ�õ�λ��
	 */
	public void occupyBitmap(int headIndex, int num) {
		for (int i = headIndex; i < headIndex + num; i++) {
			this.bitList[i] = true;
		}
	}

	/**
	 * @apiNote ��λͼ�еļ�λ�ͷ�(�������ڻ���㷨����Ϊ��ֵ����������岻ͬ)
	 * @param headIndex ��Ҫ�ͷŵ����Ŀ�ͷ
	 * @param num       ��Ҫ�ͷŵ�λ��
	 */
	public void releaseBitmap(int headIndex, int num) {
		for (int i = headIndex; i < headIndex + num; i++) {
			this.bitList[i] = false;
		}
	}

	public int getMapSize() {
		return mapSize;
	}

	public void setMapSize(int mapSize) {
		this.mapSize = mapSize;
	}

	public boolean[] getBitList() {
		return bitList;
	}

	public void setBitList(boolean[] bitList) {
		this.bitList = bitList;
	}

}