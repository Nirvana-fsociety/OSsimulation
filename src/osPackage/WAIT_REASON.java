package osPackage;

public enum WAIT_REASON {
	UNKNOW, // 无等待原因，不阻塞
	INPUT_WAIT, // 由于发起了输入指令阻塞
	OUTPUT_WAIT, // 由于发起了输出指令阻塞
	NEED_REGISTER, // 需要寄存器
	NEED_REGISTER1, // 需要另一个寄存器
	RELEASE_REGISTER, // 释放寄存器
	RELEASE_REGISTER1, // 释放另一个寄存器
	NEED_BUFFER, // 需要缓冲区
	RELEASE_BUFFER, // 释放缓冲区
	REQUEST_PAGE_ACCESSMEM, // 访存指令请求页面
	REQUEST_PAGE_INSTRUCTION, // 读取下一条指令请求页面
	NEED_DEVICE, // 请求设备
	RELEASE_DEVICE, // 释放设备
	NEED_NORMAL_SYSTEM_EXE, // 需要系统核心执行一段时间普通系统指令集。
	TRANS_DATA_INTPUT, // 系统缓冲区到进程缓冲区搬运数据
	TRANS_DATA_OUTPUT// 进程缓冲区到系统缓冲区搬运数据
}