package osPackage;

public class BitMap {
	/**
	 * 位图尺寸
	 */
	private int mapSize;
	/**
	 * 位图数据容器
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
		// 在伙伴算法中，开局的位图每一位都是不能用的，只有最后一个位图每一位是可用的。
		for (int i = 0; i < bitList.length; i++) {
			this.bitList[i] = false;// false表示伙伴算法中该块不存在。在外存文件中则表示空块。
		}
	}

	// 功能：设置位图的一位的真值
	public boolean alterTheBit(int loca, boolean value) {
		if (loca < this.mapSize) {
			this.bitList[loca] = value;
			return true;
		} else {
			return false;
		}
	}

	// 功能：检查指定位的真值
	public boolean checkTheBit(int loca) {
		return this.bitList[loca];
	}

	// 伙伴算法专用功能：获得该位图的第一个空闲组的下标
	public Integer findFreeGroupIndex() {
		for (int i = 0; i < bitList.length; i++) {
			if (this.bitList[i]) {
				return Integer.valueOf(i);
			}
		}
		return null;
	}

	/**
	 * @apiNote 将位图中的几位占用(不能用于伙伴算法，因为真值所代表的意义不同)
	 * @param headIndex 想要占用的区的开头
	 * @param num       想要占用的位数
	 */
	public void occupyBitmap(int headIndex, int num) {
		for (int i = headIndex; i < headIndex + num; i++) {
			this.bitList[i] = true;
		}
	}

	/**
	 * @apiNote 将位图中的几位释放(不能用于伙伴算法，因为真值所代表的意义不同)
	 * @param headIndex 想要释放的区的开头
	 * @param num       想要释放的位数
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