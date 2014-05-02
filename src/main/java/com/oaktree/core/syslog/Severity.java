package com.oaktree.core.syslog;

/**
 * Severity of message.
 * Forms part of the priority field.
 * 
 * 0       Emergency: system is unusable
 * 1       Alert: action must be taken immediately
 * 2       Critical: critical conditions
 * 3       Error: error conditions
 * 4       Warning: warning conditions
 * 5       Notice: normal but significant condition
 * 6       Informational: informational messages
 * 7       Debug: debug-level messages
 * 
 * @author ij
 *
 */
public enum Severity {
	EMERGENCY(0),		
	ALERT(1),
	CRITICAL(2),
	ERROR(3),
	WARNING(4),
	NOTICE(5),
	INFO(6),
	DEBUG(7);
	
	int value;
	Severity(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	
	
}