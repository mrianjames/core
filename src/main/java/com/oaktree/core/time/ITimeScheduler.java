package com.oaktree.core.time;

import com.oaktree.core.container.IComponent;

import java.util.Date;
import java.util.concurrent.Future;

/**
 * Interface for time abstract time and the various implementations.
 * Time as they say is relative. In a scheduler sense this is true - all tasks whether specified for a future
 * specified time or a duration interval from now will be treated equally as duration intervals from "now".
 *
 * Scheduling defintions:
 * 1) hypertime - all time intervals are reduced by a supplied factor so they are speeded up or slowed down.
 * 2) realtime - a hypertime of "1" meaning intervals will be scheduled as supplied.
 * 3) click - a concept of ignoring time and allow an interactive walk through all tasks in time order.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public interface ITimeScheduler extends IComponent {
	/**
	 * Schedule a task to run in n configured TimeUnit optionally dispatched on a key if this scheduler
     * supports a dispatcher.
     *
	 * @param interval
	 * @param task
	 * @return
	 */
	public Future<?> schedule(String key,long interval,Runnable task);
	public Future<?> schedule(String key,Date date, Runnable task);
	public Future<?> schedule(String key,Date date, long thenevery, Runnable task);
	public Future<?> schedule(String key,long initial, long every, Runnable task);

    /**
     * Set the hyper-time multiplier that time in non-click mode runs.
     * @param multiplier
     */
	public void setHypertime(double multiplier);

    /**
     * Get the currently configured hyper time setting.
     * @return
     */
	public double getHypertime();

    /**
     * Get the time now according to the warped view of time.
     * @return
     */
	public long getNow();
        public long getTime();
	public void setStartTime(long now);
	public void cancel(Future<?> appointment);
	/**
	 * Get the next event, optionally blocking until it is exists. If you block you may get an InterruptedException.
	 * @return
	 */
	public void next(boolean playMissedEvents, boolean block) throws InterruptedException;

    /**
     * Resume this schedulers execution of tasks, playing any missed tasks.
     *
     * @param playmissedtasks
     */
	public void resume(boolean playmissedtasks);

    /**
     * Set "click" mode where tasks are not run until next() is called.
     * @param on
     */
	public void setClickMode(boolean on);
}
