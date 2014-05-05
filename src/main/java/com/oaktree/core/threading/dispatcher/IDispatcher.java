package com.oaktree.core.threading.dispatcher;

import java.util.List;

/**
 * A dispatcher is a component that controls the allocation of tasks to threads. It provides the following guarantee - 
 * for any given key (queue) tasks shall be run in the order that events are given to it if the task "priority" is equal. 
 * Tasks that have a high priority will be placed at the front of the queue and processed before those with lesser priority.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public interface IDispatcher {
	
	public final static int NORMAL_PRIORITY = 5;
	public final static int HIGH_PRIORITY = 10;

	/**
	 * Set the range of keys. (optional)
	 * @param keys
	 */
	public void setKeys(String[] keys);
	/**
	 * Set the number of threads in the dispatchers thread pool.
	 * @param i
	 */
	public void setThreads(int i);
	/**
	 * Set the dispatcher name.
	 * @param name
	 */
	public void setName(String name);
	/**
	 * Dispatch a runnable task based upon a key. All tasks for a key will be processed
	 * sequentially in the order they are given to the dispatcher.
	 * @param key
	 * @param task
	 */
	public void dispatch(String key, Runnable task);
	
	/**
	 * Dispatch a task with a given priority.
	 * @param key
	 * @param runnable
	 * @param priority
	 */
	public void dispatch(String key, Runnable runnable, int priority);
	
	/**
	 * Start the dispatcher.
	 */
	public void start();
	
	/**
	 * Stop the dispatcher.
	 */
	public void stop();
	
	/**
	 * Drain all existing events from a queue. 
	 * @param drainQueue
	 * @param key
	 */
	public void drainQueue(List<Runnable> drainQueue,String key);
	
	/**
	 * Basic metrics. Get the number of tasks currently on a named queue in the dispatcher.
	 * @param string
	 * @return
	 */
	public long getQueuedTaskCount(String string);
	
	/**
	 * Get a rough sum of all the queued tasks.
	 * @return rough sum of all currently queued tasks.
	 */
	public long getQueuedTaskCount();
	
	/**
	 * Basic metrics. Get the number of tasks executed for a named queue in a dispatcher.
	 * @param string
	 * @return executed task count for a particular queue key.
	 */
	public long getExecutedTaskCount(String string);

    /**
     * Get executed number of tasks on all keys.
     * @return number of executed tasks so far.
     */
    public long getExecutedTaskCount();
	
	/**
	 * Set if we can expand this dispatchers key set.
	 * @param canExpand
	 */
	public void setCanExpand(boolean canExpand);
	
	/**
	 * Get the name of the dispatcher.
	 * @return name of dispatcher
	 */
	public String getName();

    /**
     * Remove a particular key from the dispatcher.
     * @param key
     */
    public void removeKey(String key);

    /**
     * Remove a number of keys from the dispatcher.
     * @param keys
     */
    public void removeKeys(String[] keys);

    /**
     * Get all our keys.
     * @return keys registered in this dispatcher currently
     */
    public String[] getKeys();
}
