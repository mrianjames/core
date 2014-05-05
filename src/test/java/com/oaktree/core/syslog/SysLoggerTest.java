package com.oaktree.core.syslog;

public class SysLoggerTest {

	
	/**
	 * TEST
	 * @param args
	 */
	public static void main(String[] args) {
		//String url = "@192.168.0.6:514";
<<<<<<< HEAD
		String url = "@127.0.0.1:12514";
		//String url = "@@127.0.0.1:12515";
=======
		//String url = "@127.0.0.1:12514";
		String url = "@@127.0.0.1:12515";
>>>>>>> 426dd0eb3474e0dd4b23ed9d039174b68fb9fbba
		SysLogger sl = new SysLogger("myprogramname","testlogger",url);
		sl.initialise();
		sl.start();
		
		//sl.log(Facilities.kernelMessages.getId(),Severity.INFO.value,"This is from ian");
		sl.log(Facilities.ftpMessages.getId(),Severity.NOTICE.value,"This is from ftp");
<<<<<<< HEAD
		//sl.log(Facilities.linePrinterMessages.getId(),Severity.INFO.value,"This is from ftp");
		//sl.log(Facilities.clockDaemonMessages.getId(),Severity.ALERT.value,"This is from ftp");
=======
		sl.log(Facilities.linePrinterMessages.getId(),Severity.INFO.value,"This is from ftp");
		sl.log(Facilities.clockDaemonMessages.getId(),Severity.ALERT.value,"This is from ftp");
>>>>>>> 426dd0eb3474e0dd4b23ed9d039174b68fb9fbba
	}

}
