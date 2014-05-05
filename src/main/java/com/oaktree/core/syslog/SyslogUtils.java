package com.oaktree.core.syslog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SyslogUtils {
	public static int getSeverity(int priority) {
		return priority & 0x07;
	}
	public static int getFacility(int priority) {
		return priority >> 3;
	}
	
	public static int resolvePriority(int facility, int severity) {
		return (facility * 8)+severity;
	}
	
	public static ThreadLocal<DateFormat> syslogTS = new ThreadLocal<DateFormat>(){
		protected DateFormat initialValue() {
			return new SimpleDateFormat("MMM dd HH:mm:ss");
		}
	};
}
