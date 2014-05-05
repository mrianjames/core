package com.oaktree.core.syslog.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.utils.Text;

public class AbstractSyslogRecordSource extends AbstractComponent implements
		ISysLogRecordSource {
	private Collection<ISysLogRecordListener> srcListeners = new ArrayList<ISysLogRecordListener>();
	
	public void addListener(ISysLogRecordListener listener) {
		srcListeners.add(listener);
	}
	public void setSyslogRecordListener(ISysLogRecordListener listener) {
		this.srcListeners.add(listener);
	}
	private final static char LSB = '<';
	private final static char RSB = '>';
	
	private DateFormat tsf = new SimpleDateFormat("MMM dd HH:mm:ss");
	
	/**
	 * Parse and notify the string message.
	 * <93>Apr 29 07:38:58 192.168.56.1 myprogramname.testlogger This is from ftp
	 * @param msg
	 */
	public void onStringMessage(String msg) {
		int lb1 = msg.indexOf(LSB);
		int rb1 = msg.indexOf(RSB);
		String sp = msg.substring(lb1, rb1-1);
		int priority = Integer.valueOf(sp);
		int facility = getFacility(priority);
		int severity = getSeverity(priority);
		int endTimestampIndex = rb1+15;
		String strTimestamp = msg.substring(rb1, endTimestampIndex-1);
		long timestamp = 0;
		try {
			timestamp = tsf.parse(strTimestamp).getTime();
		} catch (ParseException e) {
			logger.warn("Cannot parse message timestamp",e);
		}
		int endIpIndex = msg.indexOf(Text.SPACE,endTimestampIndex);
		String ip = msg.substring(endTimestampIndex,endIpIndex-1);
		
		int endTagIndex = msg.indexOf(Text.SPACE,endIpIndex);
		String tag = msg.substring(endIpIndex,endTagIndex-1);
		String message = msg.substring(endTagIndex);
		this.onMessage(facility,severity,timestamp,ip,tag,message);
	}
	protected void onMessage(int facility, int severity, long timestamp,
			String ip, String tag, String message) {
		SysLogRecord record = new SysLogRecord(facility,severity,timestamp,ip,tag,message);
		for (ISysLogRecordListener listener:srcListeners) {
			listener.onSysLogRecord(record);
		}
	}
	
	private int getSeverity(int priority) {
		return priority & 0x07;
	}
	private int getFacility(int priority) {
		return priority >> 3;
	}
	
	
}
