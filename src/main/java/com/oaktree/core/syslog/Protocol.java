package com.oaktree.core.syslog;

public enum Protocol {
	UDP,TCP;
	public boolean isUdp() {
		return this.equals(UDP);
	}
	public boolean isTcp() {
		return this.equals(TCP);
	}
}