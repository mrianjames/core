package com.oaktree.core.memory;

import java.util.List;
import java.util.Map;

/**
 * Details about this box.
 * 
 * @author ij
 *
 */
public interface IBoxStatistics {
	
	public final static String CPU_COUNT = "CPU_COUNT";
	public final static String CORE_COUNT = "CORE_COUNT";
	public final static String CPU_VENDOR = "CPU_VENDOR";
	public final static String CPU_MODEL = "CPU_MODEL";
	public final static String CPU_RATING = "CPU_RATING";
	public final static String CPU_USER = "CPU_USER";
	public final static String CPU_SYS = "CPU_SYS";
	public final static String CPU_WAIT = "CPU_WAIT";
	public final static String CPU_NICE = "CPU_NICE";
	public final static String CPU_TOTAL = "CPU_TOTAL";
	public final static String UPTIME = "UPTIME";
	public final static String MEM_FREE = "MEM_FREE";
	public final static String MEM_TOTAL = "MEM_TOTAL";
	public final static String MEM_USED = "MEM_USED";
	public final static String LOAD_1 = "LOAD_1";
	public final static String LOAD_2 = "LOAD_2";
	public final static String LOAD_3 = "LOAD_3";
	public final static String HOSTNAME = "HOST";
	public final static String OS_NAME = "OS_NAME";
	public final static String OS_TYPE = "OS_TYPE";
	public final static String OS_VERSION = "OS_VERSION";
	public final static String VM = "VM";
	public final static String PNIC = "PNIC";
	public final static String PIP = "PIP";
	public final static String PMAC = "PMAC";
	
	public Map<String,String> getCPUSummary();
	public Map<String,String> getNetworkDetails();
	public Map<String,String> getMemoryDetails();
	public List<Map<String,String>> getCPUDetails();
	
	public void flush();
}
