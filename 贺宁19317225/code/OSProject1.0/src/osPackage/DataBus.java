package osPackage;

public class DataBus {
	private Integer data;// ����

	public DataBus(Integer another) {
		super();
		this.data = new Integer(another.intValue());
	}

	// ���ܣ����������뵽����
	public void inputDataIntoBus(Integer data) {
		this.data = new Integer(data.intValue());
	}

	// ���ܣ��������ϵ�����ȡ����
	public Integer outputDataFromBus() {
		return this.data;
	}

	public Integer getData() {
		return data;
	}

	public void setData(Integer data) {
		this.data = data;
	}
}
