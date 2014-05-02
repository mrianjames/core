package com.oaktree.core.threading.dispatcher;

/**
 * A task that is runnable in our dispatcher.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public interface IDispatchTask extends Runnable,Comparable<IDispatchTask>{
	public void setId(long id);
	int getPriority();
	public long getId();
	/**
	 * Get the time taken for the task to queue before getting dispatch. 0 if not yet dispatched.
	 * @return time taken for task to execute since enque.
	 */
	long getQueuedDurationNanos();
	/**
	 * Is this task completed i..e run to completion?
	 * @return completness of task
	 */
	boolean isComplete();
	/**
	 * Has this task been dispatched?
	 * @return if has been dispatched yet
	 */
	boolean isDispatched();
	/**
	 * Get the time taken to actually run this task once dispatched.
	 * @return time taken to run once dispatched in nanos.
	 */
	long getRunDurationNanos();
}
