package osPackage;

public class SiteInfo {
	int pcData;// �������������
	int irData;// ָ��Ĵ�������
	int pswData;// ״̬λ�Ĵ�������
	int spData;// ջָ������
	int arData;// ��ַ�Ĵ�������

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
