package osPackage;

public class ProgramStatusWord {
	private int data;// 寄存器数据（最低位是CPU状态位）
	private boolean cpuState;// CPU的状态，false是用户态，true是内核态。

	//构造
	public ProgramStatusWord(boolean cpuState) {
		super();
		this.data = 0;
		this.cpuState = cpuState;
		if (this.cpuState) {
			this.data += 1;
		} else {
			this.data += 0;
		}
	}

	public ProgramStatusWord(int data) {
		super();
		this.data = data;
		// 检测数据的最低位，如果为1就是内核态，如果为0就是用户态
		if ((this.data & 1) == 1) {
			this.cpuState = true;
		} else {
			this.cpuState = false;
		}
	}
	
	//get & set
	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	public boolean isCpuState() {
		return cpuState;
	}

	public void setCpuState(boolean cpuState) {
		this.cpuState = cpuState;
	}
	
	//功能：更新数据
	public void updateData(int data) {
		this.data = data;
		// 检测数据的最低位，如果为1就是内核态，如果为0就是用户态
		if ((this.data & 1) == 1) {
			this.cpuState = true;
		} else {
			this.cpuState = false;
		}
	}

	// 功能：转为内核态
	public boolean changeToKernal() {
		if (this.cpuState) {// 如果CPU的状态本身就是内核态，就返回false，表示不需要更改。
			return false;
		} else {// 表示CPU当前状态本身是用户态，就返回true，表示已经完成更改。
			this.cpuState = true;
			this.data += 1;
			return true;
		}
	}

	// 功能：转回用户态
	public boolean backToUser() {
		if (!this.cpuState) {// 如果CPU的状态本身就是用户态，就返回false，表示不需要更改。
			return false;
		} else {// 表示CPU当前状态本身是内核态，就返回true，表示已经完成更改。
			this.cpuState = false;
			this.data -= 1;
			return true;
		}
	}
}
