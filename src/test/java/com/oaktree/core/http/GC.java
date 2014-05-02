package com.oaktree.core.http;

public class GC {
	private long time;
	private String type;
	private long before;
	private long after;

	public GC(long time,String type, long before, long after) {
		this.time = time;
		this.type = type;
		this.before = before;
		this.after = after;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getBefore() {
		return before;
	}

	public void setBefore(long before) {
		this.before = before;
	}

	public long getAfter() {
		return after;
	}

	public void setAfter(long after) {
		this.after = after;
	}
}
