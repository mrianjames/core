package com.oaktree.core.syslog.server;

public class SysLogServerMain {

	public static void main(String[] args) {
		UdpChannelSyslogRecordSource udpSource = new UdpChannelSyslogRecordSource("udp",12514);
		TcpChannelSyslogRecordSource tcpSource = new TcpChannelSyslogRecordSource("tcp",12515);
		
		SyslogServer sls = new SyslogServer();
		//sls.addSysLogRecordSource(udpSource);
		sls.addSysLogRecordSource(tcpSource);
		sls.initialise();
		sls.start();
	}

}
