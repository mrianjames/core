package com.oaktree.core.syslog.server;

public class SysLogRecord implements ISysLogRecord {

	private long timestamp;
	private String tag;
	private String message;
	private String ip;
	private int facility;
	private int severity;
	public SysLogRecord(int facility, int severity, long timestamp, String ip, String tag, String message) {
		this.facility = facility;
		this.severity = severity;
		this.timestamp = timestamp;
		this.ip = ip;
		this.tag = tag;
		this.message = message;
	}
	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public int getFacility() {
		return facility;
	}

	@Override
	public int getSeverity() {
		return severity;
	}

	
	@Override
	public String getHost() {
		return ip;
	}

	@Override
	public String getTag() {
		return tag;
	}

}
