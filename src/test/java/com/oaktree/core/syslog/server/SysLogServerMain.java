package com.oaktree.core.syslog.server;

import com.oaktree.core.data.sequence.DataSequence;
import com.oaktree.core.data.LoggingDataReceiver;
import com.oaktree.core.syslog.SysLogger;

public class SysLogServerMain {

	public static void main(String[] args) {
		UdpChannelSyslogRecordSource udpSource = new UdpChannelSyslogRecordSource("udp",12514);
		udpSource.initialise(); udpSource.start();
		TcpChannelSyslogRecordSource tcpSource = new TcpChannelSyslogRecordSource("tcp",12515);
		tcpSource.initialise(); tcpSource.start();
		
		LoggingDataReceiver<ISysLogRecord> logSink = new LoggingDataReceiver<ISysLogRecord>();
		logSink.setName("logsink");
		logSink.start();
		
		String url= "@192.168.0.6:514";
		SysLogger forwardSink = new SysLogger(null,"forward",url);
		forwardSink.initialise();forwardSink.start();
		
		DataSequence<ISysLogRecord,ISysLogRecord> server = new DataSequence<ISysLogRecord,ISysLogRecord>("server");
		udpSource.addDataReceiver(server);
		tcpSource.addDataReceiver(server);
		server.addDataReceiver(logSink);
		server.addDataReceiver(forwardSink);
		server.initialise();
		server.start();
	}

}
