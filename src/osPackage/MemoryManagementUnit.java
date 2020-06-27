package osPackage;

public class MemoryManagementUnit {
	private final static int LOGIC_PAGE_BITNUM = 7;// �߼�ҳ��λ��
	private final static int PHYSIC_PAGE_BITNUM = 6;// ������λ��
	private final static int OFFSET_BITNUM = 9;// ƫ��λ��

	private Address logicAddress;// �߼���ַ�����룩
	private Address physicAddress;// �����ַ�������

	private Address pagetableBasicAddress;// ���н���ҳ���ַ������

	private TransLookBuffer TLB;// ���

	public MemoryManagementUnit() {
		super();
		this.logicAddress = new Address(-1, LOGIC_PAGE_BITNUM, OFFSET_BITNUM);
		this.physicAddress = new Address(-1, PHYSIC_PAGE_BITNUM, OFFSET_BITNUM);
		this.pagetableBasicAddress = new Address(-1, PHYSIC_PAGE_BITNUM, OFFSET_BITNUM);
		this.TLB = new TransLookBuffer();
	}

	public Address getLogicAddress() {
		return logicAddress;
	}

	public void setLogicAddress(Address logicAddress) {
		this.logicAddress = logicAddress;
	}

	public Address getPhysicAddress() {
		return physicAddress;
	}

	public void setPhysicAddress(Address physicAddress) {
		this.physicAddress = physicAddress;
	}

	public Address getPagetableBasicAddress() {
		return pagetableBasicAddress;
	}

	public void setPagetableBasicAddress(Address pagetableBasicAddress) {
		this.pagetableBasicAddress = pagetableBasicAddress;
	}

	public TransLookBuffer getTLB() {
		return TLB;
	}

	public void setTLB(TransLookBuffer tLB) {
		TLB = tLB;
	}

	public static int getLogicPageBitnum() {
		return LOGIC_PAGE_BITNUM;
	}

	public static int getPhysicPageBitnum() {
		return PHYSIC_PAGE_BITNUM;
	}

	public static int getOffsetBitnum() {
		return OFFSET_BITNUM;
	}

	// ���ܺ����������߼���ַ
	public void inputLogicAddress(int address) {
		this.logicAddress.updateAddress(address);
	}

	// ���ܺ�������������ַ
	public Address outputPhysicAddress() {
		return this.physicAddress;
	}

	// ���ܺ���������ҳ���ַ
	public void setPageTableBasicAddress(int address) {
		this.pagetableBasicAddress.updateAddress(address);
	}

	// ת�����������ҿ���Ƿ�����
	public boolean translate1() {
		PageTableItem tmpItem = this.TLB.findTheItem(this.logicAddress.getPageID());
		if (tmpItem == null || !tmpItem.isStateBit()) {
			// δ���п��Ӧ����false��Ϣ���ظ��ϼ������洢����ϵͳ���洢����ϵͳ���һ������
			return false;
		} else {
			// ���п��
			this.physicAddress.updateAddress(tmpItem.getBlockID(), this.logicAddress.getOffset());
			return true;
		}
	}

	// ת���������洢����ϵͳ����ҳ���õ���ҳ�����MMU�����ƴ�������ַ
	public boolean translate2(PageTableItem pageItem) {
		if (pageItem == null || !pageItem.isStateBit()) {
			// δ����ҳ��Ӧ����false��Ϣ���ظ��洢����ϵͳ������ȱҳ�ж�
			return false;
		} else {
			// ����ҳ����ƴ�������ַ���ٽ��������TLB�С�
			this.physicAddress.updateAddress(pageItem.getBlockID(), this.logicAddress.getOffset());
			PageTableItem tmpItem = new PageTableItem(pageItem.getData());
			this.TLB.enQueue(tmpItem);
			return true;
		}
	}
	
	public void clearTLB() {
		this.TLB.getTlbItemQueue().clear();
	}
}
