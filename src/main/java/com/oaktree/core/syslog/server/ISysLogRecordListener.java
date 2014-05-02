package com.oaktree.core.syslog.server;

public interface ISysLogRecordListener {
	public void onSysLogRecord(ISysLogRecord record);
}
