package osPackage;

public class ProgramStatusWord {
	private int data;// �Ĵ������ݣ����λ��CPU״̬λ��
	private boolean cpuState;// CPU��״̬��false���û�̬��true���ں�̬��

	//����
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
		// ������ݵ����λ�����Ϊ1�����ں�̬�����Ϊ0�����û�̬
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
	
	//���ܣ���������
	public void updateData(int data) {
		this.data = data;
		// ������ݵ����λ�����Ϊ1�����ں�̬�����Ϊ0�����û�̬
		if ((this.data & 1) == 1) {
			this.cpuState = true;
		} else {
			this.cpuState = false;
		}
	}

	// ���ܣ�תΪ�ں�̬
	public boolean changeToKernal() {
		if (this.cpuState) {// ���CPU��״̬��������ں�̬���ͷ���false����ʾ����Ҫ���ġ�
			return false;
		} else {// ��ʾCPU��ǰ״̬�������û�̬���ͷ���true����ʾ�Ѿ���ɸ��ġ�
			this.cpuState = true;
			this.data += 1;
			return true;
		}
	}

	// ���ܣ�ת���û�̬
	public boolean backToUser() {
		if (!this.cpuState) {// ���CPU��״̬��������û�̬���ͷ���false����ʾ����Ҫ���ġ�
			return false;
		} else {// ��ʾCPU��ǰ״̬�������ں�̬���ͷ���true����ʾ�Ѿ���ɸ��ġ�
			this.cpuState = false;
			this.data -= 1;
			return true;
		}
	}
}
