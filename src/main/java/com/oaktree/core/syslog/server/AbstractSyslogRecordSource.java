package com.oaktree.core.syslog.server;

import java.text.ParseException;

import com.oaktree.core.patterns.sequence.DataSequence;
import com.oaktree.core.syslog.SyslogUtils;
import com.oaktree.core.utils.Text;

public class AbstractSyslogRecordSource extends DataSequence<ISysLogRecord,ISysLogRecord> {

	public AbstractSyslogRecordSource(String name) {
		super(name);
	}
	//	private Collection<ISysLogRecordListener> srcListeners = new ArrayList<ISysLogRecordListener>();
//	
//	public void addListener(ISysLogRecordListener listener) {
//		srcListeners.add(listener);
//	}
//	public void setSyslogRecordListener(ISysLogRecordListener listener) {
//		this.srcListeners.add(listener);
//	}
	private final static char LSB = '<';
	private final static char RSB = '>';
	
	//private DateFormat tsf = new SimpleDateFormat("MMM dd HH:mm:ss");
	
	/**
	 * Parse and notify the string message.
	 * <93>Apr 29 07:38:58 192.168.56.1 myprogramname.testlogger This is from ftp
	 * @param msg
	 */
	public void onStringMessage(String msg) {
		if (msg == null || msg.length() == 0) {
			return;
		}
		int lb1 = msg.indexOf(LSB);
		int rb1 = msg.indexOf(RSB);
		String sp = msg.substring(lb1+1, rb1);
		int priority = Integer.valueOf(sp);
		int facility = SyslogUtils.getFacility(priority);
		int severity = SyslogUtils.getSeverity(priority);
		int endTimestampIndex = rb1+16;
		String strTimestamp = msg.substring(rb1+1, endTimestampIndex);
		long timestamp = 0;
		try {
			timestamp = SyslogUtils.syslogTS.get().parse(strTimestamp).getTime();
		} catch (ParseException e) {
			logger.warn("Cannot parse message timestamp",e);
		}
		int endIpIndex = msg.indexOf(Text.SPACE,endTimestampIndex+1);
		String ip = msg.substring(endTimestampIndex+1,endIpIndex);
		
		int endTagIndex = msg.indexOf(Text.SPACE,endIpIndex+1);
		String tag = msg.substring(endIpIndex+1,endTagIndex);
		String message = msg.substring(endTagIndex+1);
		this.onMessage(facility,severity,timestamp,ip,tag,message);
	}
	protected void onMessage(int facility, int severity, long timestamp,
			String ip, String tag, String message) {
		SysLogRecord record = new SysLogRecord(facility,severity,timestamp,ip,tag,message);
		this.onData(record, this, getTime());
	}
	
	private long getTime() {
		return System.currentTimeMillis();
	}

	
	
}
