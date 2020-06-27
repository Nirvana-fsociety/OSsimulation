package osPackage;

public class DataBus {
	private Integer data;// 数据

	public DataBus(Integer another) {
		super();
		this.data = new Integer(another.intValue());
	}

	// 功能：将数据输入到总线
	public void inputDataIntoBus(Integer data) {
		this.data = new Integer(data.intValue());
	}

	// 功能：将总线上的数据取下来
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
