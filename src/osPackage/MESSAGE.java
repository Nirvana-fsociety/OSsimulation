package osPackage;

public enum MESSAGE {
	NONE, // 无资源
	REGISTER0, REGISTER1, REGISTER2, REGISTER3, REGISTER4, REGISTER5, REGISTER6, REGISTER7, // 寄存器
	BUFFER, // 缓冲区
	DEVICE0, DEVICE1, DEVICE2, DEVICE3, // 外部设备
	MEMORY_SOURCE_FULL, // 内存资源：挂起进程如果想拥有被激活的许可就必须提出对该资源提出申请，这个资源由撤销的进程在临死前提供。
	MEMORY_SOURCE_EMPTY // 内存资源：撤销进程如果想拥有激活挂起进程的资格就必须提出对此资源的申请，这个资源由挂起进程提供。（并不意味着如果申请不到就不能撤销，还是可以撤销的，但是申请不到就不能激活进程）
}
