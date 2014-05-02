package com.oaktree.core.threading.dispatcher;

import com.oaktree.core.logging.Log;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.JavaTime;
import com.oaktree.core.utils.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Our task that we want to execute with a priority. Latency stats are available if configured.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class PrioritisedDispatchTask implements IDispatchTask, Comparable<IDispatchTask> {

    private final static Logger logger = LoggerFactory.getLogger(PrioritisedDispatchTask.class.getName());

    private ITime time = new JavaTime();

    public void setTime(ITime time) {
        this.time = time;
    }

    /**
	 * Optional id to identify this task.
	 */
	private long id;
	/**
	 * The actual task we will run.
	 */
	private Runnable runnable;
	/**
	 * Priority of this task.
	 */
	private int priority = 5;
	/**
	 * Creation time of task. In nanoseconds.
	 */
	private long created;
	/**
	 * Time we started to execute this task.In nanoseconds.
	 */
	private long run;
	/**
	 * The time we completed this task. In nanoseconds.
	 */
	private long completed = 0;
	/**
	 * Should we record task stats.
	 */
	private boolean recordTaskStats;
	@Override
	public long getRunDurationNanos() {
		return completed > 0 ? this.completed - this.run : 0; 
	}
	@Override
	public boolean isComplete() {
		return completed > 0;
	}
	@Override
	public boolean isDispatched() {
		return run > 0;
	}
	
	public PrioritisedDispatchTask(IDispatcher dispatcher, Runnable r,long id,ITime time) {
		this.id = id;
        this.time = time;
		this.runnable = r;			
	}
	
	public PrioritisedDispatchTask(IDispatcher dispatcher, Runnable r,long id, int priority, boolean recordTaskStats, ITime time) {
		this.id = id;
		this.runnable = r;
        this.time = time;
		this.priority = priority;
		this.recordTaskStats = recordTaskStats;
		if (this.recordTaskStats) {
			this.created = time.getNanoTime();
		}
	}

	public PrioritisedDispatchTask(IDispatcher dispatcher, Runnable r,int priority, boolean recordTaskStats,ITime time) {
		this.runnable = r;
		this.priority = priority;
        this.time = time;
		this.recordTaskStats = recordTaskStats;
		if (this.recordTaskStats) {
			this.created = time.getNanoTime();
		}
	}

	
	@Override
	public long getQueuedDurationNanos() {
		return this.run > 0 ? (run-created) : 0; 
	}
	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}
	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public void run() {
		try {
			if (this.recordTaskStats) {
				this.run = time.getNanoTime();
			}
			runnable.run();
			if (this.recordTaskStats) {
				this.completed = time.getNanoTime();
			}
			
		} catch (Throwable t) {
			Log.exception(logger,t);
		}
	}
	
	
	@Override
	public int compareTo(IDispatchTask o) {
		int p =  o.getPriority()-this.priority;
		if (p == 0) {
			return (int)(this.id-o.getId());
		} 
		return p;
	}
	
	public int hashCode() {
		return this.runnable.hashCode() + (17*(int)this.id);
	}
	
	@Override
	public boolean equals(Object task) {
		if (task instanceof IDispatchTask) {
			return this.compareTo((IDispatchTask)task) == 0;
		} else {
			return false;
		}
	}
	public String toString() {
		return this.id + ", " + this.priority;
	}
}