package com.oaktree.core.syslog;


/**
	 * Well known facilities.
	 * @author ij
	 *
	 */
	public class Facilities {
		public Facility getFacility(int id) {
			switch (id) {
			case 0:
				return kernelMessages;
			}
			return null;
		}
		public static Facility kernelMessages = new Facility(0,"kernel messages");
		public static Facility userLevelMessages = new Facility(1,"user-level messages");
		public static Facility mailSystemMessages = new Facility(2,"mail system messages");
		public static Facility systemDaemonMessages = new Facility(3,"system daemon messages");
		public static Facility securityMessages = new Facility(4,"security/authorization messages");
		public static Facility syslogdMessages = new Facility(5,"messages generated internally by syslogd");
		public static Facility linePrinterMessages = new Facility(6,"line printer subsystem");
		public static Facility networkMessages = new Facility(7,"network news subsystem");
		public static Facility uucpMessages = new Facility(8,"UUCP subsystem");
		public static Facility clockDaemonMessages = new Facility(9,"clock daemon");
		public static Facility securityAuthMessages = new Facility(10,"security/authorization messages");

		public static Facility ftpMessages = new Facility(11,"FTP daemon");
		public static Facility ntpMessages = new Facility(12,"NTP subsystem");
		public static Facility logAuditMessages = new Facility(13,"log audit");
		public static Facility logAlertMessages = new Facility(14,"log alert");
		public static Facility clockDaemon2Messages = new Facility(15,"clock daemon");

		public static Facility local0Messages = new Facility(16,"local0");
		public static Facility local1Messages = new Facility(17,"local1");
		public static Facility local2Messages = new Facility(18,"local2");
		public static Facility local3Messages = new Facility(19,"local3");
		public static Facility local4Messages = new Facility(20,"local4");
		public static Facility local5Messages = new Facility(21,"local5");
		public static Facility local6Messages = new Facility(22,"local6");
		public static Facility local7Messages = new Facility(23,"local7");
	}