package osPackage;

public enum PRO_STATE {
	UNKNOW,//未知状态，用于创建之前
	READY,//就绪
	RUN,//运行
	BLOCK,//阻塞
	PEND_READY,//挂起就绪
	PEND_BLOCK//挂起阻塞
}
