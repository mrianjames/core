package com.oaktree.core.syslog;

public class SysLoggerTest {

	
	/**
	 * TEST
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		//String url = "@192.168.0.6:514";

		String url = "@127.0.0.1:12514";
		//String url = "@@127.0.0.1:12515";

		SysLogger sl = new SysLogger("myprogramname","testlogger",url);
		sl.initialise();
		sl.start();
		
		sl.log(Facilities.ftpMessages.getId(),Severity.NOTICE.value,"This is from udp");
		sl.stop();
		Thread.sleep(5000);
		url = "@@127.0.0.1:12515";
		SysLogger s2 = new SysLogger("myprogramname","testlogger",url);
		s2.initialise();
		s2.start();
		s2.log(Facilities.ftpMessages.getId(),Severity.INFO.value,"This is from tcp");
		s2.stop();
	}

}
