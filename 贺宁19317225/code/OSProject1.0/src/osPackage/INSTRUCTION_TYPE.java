package osPackage;

public enum INSTRUCTION_TYPE {
	UNKNOW, // 未知指令
	INPUT, // 输入指令
	OUTPUT, // 输出指令
	SYSTEM_CALL, // 系统调用
	CALCULATE, // 计算或赋值指令
	ACCESS_MEMORY, // 访存指令
	NORMAL, // 普通指令
	JUMP// 跳转指令
}
