/*
 * Created on Aug 16, 2005
 */

package com.oaktree.core.memory;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Ian James A class that provides simple memory analysis methods.
 * @deprecated - use Sigar instead
 */

public class Memory {

	/**
	 * Num bytes in k and k in Mb.
	 */
	private static final double	BYTES	= 1024;

	/**
	 * Number of decimal places to format in.
	 */
	private static final int	DP		= 2;

	/**
	 * Get the number of bytes in K
	 * 
	 * @param bytes
	 * @return double
	 */
	public static double bytesToK(double bytes) {
		return bytes / BYTES;
	}

	/**
	 * Get the number of bytes in Mb
	 * 
	 * @param bytes
	 * @return double
	 */
	public static double bytesToMb(double bytes) {
		return bytesToK(bytes) / BYTES;
	}

	/**
	 * Format a double to x dp
	 * 
	 * @param d
	 * @param dp
	 * @return String
	 */

	public static String format(double d, int dp) {
		NumberFormat format = DecimalFormat.getNumberInstance();
		format.setMaximumFractionDigits(dp);
		return format.format(d);
	}

	/**
	 * Get the Free memory in the JVM
	 * 
	 * @return long
	 */
	public long getFreeMemory() {
		return Runtime.getRuntime().freeMemory();
	}

	/**
	 * Get the max memory of JVM
	 * 
	 * @return long
	 */
	public long getMaxMemory() {
		return Runtime.getRuntime().maxMemory();
	}

	/**
	 * Get the total memory in the JVM
	 * 
	 * @return long
	 */
	public long getTotalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	/**
	 * Return memory as a string.
	 * 
	 * @see java.lang.Object#toString()
	 */

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Total: ");
		buffer.append(format(bytesToMb(this.getTotalMemory()), DP));
		buffer.append("Mb");
		buffer.append(", Max: ");
		buffer.append(format(bytesToMb(this.getMaxMemory()), DP));
		buffer.append("Mb");
		buffer.append(", Free: ");
		buffer.append(format(bytesToMb(this.getFreeMemory()), DP));
		buffer.append("Mb");

		return buffer.toString();
	}

}
