package com.oaktree.core.syslog.server;

public class SysLogServerMain {

	public static void main(String[] args) {
		UdpChannelSyslogRecordSource udpSource = new UdpChannelSyslogRecordSource("udp",12514);
		TcpChannelSyslogRecordSource tcpSource = new TcpChannelSyslogRecordSource("tcp",12515);
		
		SyslogServer sls = new SyslogServer();
<<<<<<< HEAD
		sls.addSysLogRecordSource(udpSource);
=======
		//sls.addSysLogRecordSource(udpSource);
>>>>>>> 426dd0eb3474e0dd4b23ed9d039174b68fb9fbba
		sls.addSysLogRecordSource(tcpSource);
		sls.initialise();
		sls.start();
	}

}
