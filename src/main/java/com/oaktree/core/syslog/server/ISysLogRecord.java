package com.oaktree.core.syslog.server;

public interface ISysLogRecord {
	public long getTimestamp();
	public String getMessage();
	public int getFacility();
	public int getSeverity();
	public String getHost();
	public String getTag();
}
