package com.oaktree.core.utils;

import java.util.Date;

/**
 * Abstraction of time, for scheduling, reporting etc. Different implementations could be used to 
 * squash or expand real time for backtesting purposes.
 * Note that any scheduling is limited by the clock resolution of the OS e.g. 10ms min scheduling time on
 * solaris 10 x86.
 * @author ij
 *
 */
public interface ITimer {
	/**
	 * Schedule a task to run at a "time" (sys millis)
	 * @param task
	 * @param date
	 */
	public void schedule(Runnable task, long date);
	/**
	 * Schedule a task to run a date/time.
	 * @param task
	 * @param date
	 */
	public void schedule(Runnable task, Date date);
	/**
	 * Get now in millis
	 * @return time now in millis since epoch
	 */
	public long getNow();
	/**
	 * Get midnight in millis.
	 * @return midnight in millis
	 */
	public long getMidnight();
	/**
	 * get num millis since midnight.
	 * @return millis since midnight.
	 */
	public long getTime();
}
