package osPackage;

public enum WAIT_REASON {
	UNKNOW, // �޵ȴ�ԭ�򣬲�����
	INPUT_WAIT, // ���ڷ���������ָ������
	OUTPUT_WAIT, // ���ڷ��������ָ������
	NEED_REGISTER, // ��Ҫ�Ĵ���
	NEED_REGISTER1, // ��Ҫ��һ���Ĵ���
	RELEASE_REGISTER, // �ͷżĴ���
	RELEASE_REGISTER1, // �ͷ���һ���Ĵ���
	NEED_BUFFER, // ��Ҫ������
	RELEASE_BUFFER, // �ͷŻ�����
	REQUEST_PAGE_ACCESSMEM, // �ô�ָ������ҳ��
	REQUEST_PAGE_INSTRUCTION, // ��ȡ��һ��ָ������ҳ��
	NEED_DEVICE, // �����豸
	RELEASE_DEVICE, // �ͷ��豸
	NEED_NORMAL_SYSTEM_EXE, // ��Ҫϵͳ����ִ��һ��ʱ����ͨϵͳָ���
	TRANS_DATA_INTPUT, // ϵͳ�����������̻�������������
	TRANS_DATA_OUTPUT// ���̻�������ϵͳ��������������
}