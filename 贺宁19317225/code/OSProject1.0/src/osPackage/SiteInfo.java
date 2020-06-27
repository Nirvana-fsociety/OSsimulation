package osPackage;

public class SiteInfo {
	int pcData;// 程序计数器内容
	int irData;// 指令寄存器内容
	int pswData;// 状态位寄存器内容
	int spData;// 栈指针内容
	int arData;// 地址寄存器内容

	public SiteInfo() {
		super();
		this.pcData = 0;
		this.irData = 0;
		this.pswData = 0;
		this.spData = 0;
		this.arData = 0;
	}

	public int getPcData() {
		return pcData;
	}

	public void setPcData(int pcData) {
		this.pcData = pcData;
	}

	public int getIrData() {
		return irData;
	}

	public void setIrData(int irData) {
		this.irData = irData;
	}

	public int getPswData() {
		return pswData;
	}

	public void setPswData(int pswData) {
		this.pswData = pswData;
	}

	public int getSpData() {
		return spData;
	}

	public void setSpData(int spData) {
		this.spData = spData;
	}

	public int getArData() {
		return arData;
	}

	public void setArData(int arData) {
		this.arData = arData;
	}

	public void updateData(int pcData, int irData, int pswData, int spData, int arData) {
		this.pcData = pcData;
		this.irData = irData;
		this.pswData = pswData;
		this.spData = spData;
		this.arData = arData;
	}
}
