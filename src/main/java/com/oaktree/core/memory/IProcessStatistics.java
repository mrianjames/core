package com.oaktree.core.memory;

import java.util.Map;

/**
 * Get details of this process.
 * 
 * @author ij
 *
 */
public interface IProcessStatistics {
	
	public final static String THREADS = "THREADS";
	public final static String PID = "PID";
	public final static String USER = "USER";
	public final static String GROUP = "GROUP";
	public final static String USER_CPU = "USER_CPU";
	public final static String START_TIME = "START_TIME";
	public final static String SYS_CPU = "SYS_CPU";
	public final static String TOTAL_CPU = "TOTAL_CPU";
	public final static String MEM_RES = "MEM_RES";
	public final static String MEM_SIZE = "MEM_SIZE";
	public final static String VM = "VM";
	public final static String DISP_QUEUED = "DISP_QUEUED";
	public final static String DISP_EXEC = "DISP_EXEC";
	
	public Map<String,String> getProcessDetails();
	public void flush();	
}
