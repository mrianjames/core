/**
 * 
 */
package com.oaktree.core.threading.dispatcher.selfish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.JavaTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.threading.PrioritisedNamedThreadFactory;
import com.oaktree.core.threading.dispatcher.IDispatchTask;
import com.oaktree.core.threading.dispatcher.PrioritisedDispatchTask;

/**
 * A very basic "built for speed, not for comfort" dispatcher that provides less features
 * (no prioritisation).
 * 
 * This dispatcher guarantees that events given to the dispatcher are sequentially executed for the
 * "key" and for that "key" whilst that task is running we will not execute another task (i.e. 
 * sequentially threadsafe).
 * 
 * A reader of this class will soon discover that queues/executors are tied to threads in 
 * a 1-1 fashion and that a key/queue can therefore never be serviced by other
 * threads. This results in the situation where one queue/thread is running long dominating
 * tasks and despite other threads being free, work for another key cannot be processed if
 * assigned to the same queue. However, in many scenarios this dispatcher offers massive 
 * performance against theoretically proper solutions, that may also provide better metrics and/or
 * functionality.
 * 
 * You should only use this dispatcher when the following applies:
 * 1) Your universe is small in ratio to threads/cores available/setup.
 * 2) Your tasks are either uniform, or are small in duration. This avoids odd long running tasks
 * 		dominating a thread.
 * 
 * DisruptorMappedDispatcher offers similar performance but also offers prioritisation.
 */
public class SelfishDispatcher implements IDispatcher {

    private ITime time = new JavaTime();

    public void setTime(ITime time) {
        this.time = time;
    }

    /**
     * The LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(SelfishDispatcher.class.getName());
    /**
     * The assignment of keys to executors.
     */
    private Map<String, ThreadPoolExecutor> executorsByKey = new ConcurrentHashMap<String, ThreadPoolExecutor>();
    /**
     * The executors.
     */
    private ThreadPoolExecutor[] executors;
    /**
     * The name of this dispatcher.
     */
    private String name;
    /**
     * Statistics. Max queue size for a "thread"
     */
    private Map<ThreadPoolExecutor, Long> maxQueueLength = new HashMap<ThreadPoolExecutor, Long>();
    /**
     * Number of threads to use.
     */
    private int threads = 4;


    @Override
    public int getCurrentThreads() {
        return threads;
    }

    @Override
    public int getMaxThreads() {
        return threads;
    }

    /**
     * Set of initial keys/queues to define.
     */
    private List<String> keys = new ArrayList<String>();
    
    private Set<String> allKeys = new CopyOnWriteArraySet<String>();

    /**
     * Can this dispatcher expand key sets (may change implementation of key/queue map)
     */
    private boolean canExpand;
    /**
     * Priority threads should be assigned. Oddly, lowering priority gives you better throughput overall.
     */
    private int threadPriority = Thread.MIN_PRIORITY;

    /**
     * Create a new mapped dispatcher with the given name and number of executors.
     * @param name
     * @param number
     */
    public SelfishDispatcher(String name, int number) {
        this.name = name;
        this.threads = number;
    }

    @Override
    public void dispatch(String key, Runnable task) {
        this.dispatchTask(key, task);
    }

    @Override
    public long getExecutedTaskCount() {
        long total = 0;
        for (String key : this.allKeys) {
            total += this.getExecutedTaskCount(key);
        }
        return total;
    }

    /**
     * Dispatch a task on a string key.
     * @param nameable
     * @param task
     * @return
     */
    public Future<?> dispatchTask(String nameable, Runnable task) {
        if (nameable == null) {
            throw new IllegalArgumentException("Nameable must exist; null is not tolerated.");
        }
        allKeys.add(nameable);
        final IDispatchTask safeTask = new PrioritisedDispatchTask(this, task, 0, Thread.NORM_PRIORITY, false,time);

        ThreadPoolExecutor executor = this.executorsByKey.get(nameable);
        if (executor == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(this.name + " assigning executor for " + nameable);
            }
            synchronized (nameable.intern()) {
                executor = this.executorsByKey.get(nameable);
                if (executor == null) {
                    executor = this.assign(this);
                    if (executor != null) {
                        this.executorsByKey.put(nameable, executor);
                    }
                }
            }
        }
        if (executor != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Dispatching for " + nameable);
            }
            return executor.submit(safeTask);
        } else {
            logger.error(this.name + " failed to assign an executor for " + nameable);
            return null;
        }
    }
    private int nextAssignedIndex = 0;

    public ThreadPoolExecutor assign(SelfishDispatcher despatcher) {
        if (this.nextAssignedIndex >= executors.length) {
            this.nextAssignedIndex = 0;
        }

        return executors[this.nextAssignedIndex++];
    }

    /**
     * Return the available executors.
     * @return ThreadPoolExecutor[]
     */
    private ThreadPoolExecutor[] getExecutors() {
        return this.executors;
    }

    /**
     * Get which executor an order is on.
     */
    public String getExecutorForKey(String key) {
        ThreadPoolExecutor executor = this.executorsByKey.get(key);
        if (executor != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(executor.getQueue().size());
            builder.append(" tasks assigned, ");
            builder.append(executor.getCompletedTaskCount());
            builder.append(" tasks completed: ");
            builder.append(executor.toString());
            builder.append(" max queue: ");
            builder.append(this.maxQueueLength.get(executor));
            return builder.toString();
        }
        return "NONE";
    }

    /**
     * Get all our thread queue sizes.
     * @return
     */
    public String getQueueSizes() {
        final StringBuilder builder = new StringBuilder();
        int i = 0;
        for (ThreadPoolExecutor executor : this.executors) {

            builder.append("Executor ");
            builder.append(i);
            builder.append(" has ");
            builder.append(executor.getQueue().size());
            builder.append(" tasks assigned, ");
            builder.append(executor.getCompletedTaskCount());
            builder.append(" tasks completed: ");
            builder.append(executor.toString());
            builder.append(" max queue: ");
            builder.append(this.maxQueueLength.get(executor));
            builder.append("\n");
            i++;
        }
        return builder.toString();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void removeKey(String key) {
        if (canExpand) {
            this.executorsByKey.remove(key);
        }

    }

    @Override
    public void removeKeys(String[] keys) {
        if (canExpand) {
            for (String key:keys) {
                this.executorsByKey.remove(key);
            }
        }
    }

    @Override
    public String[] getKeys() {
        return allKeys.toArray(new String[allKeys.size()]);
    }

    /**
     * Shutdown the dispatcher
     */
    public void shutdown() {
        for (ThreadPoolExecutor executor : this.executors) {
            executor.shutdown();
        }
    }

    /**
     * Create a debug string for mapped dispatcher.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("Executors: ");
        ThreadPoolExecutor[] executors = this.getExecutors();
        buffer.append(executors.length);
        int i = 0;
        for (ThreadPoolExecutor executor : executors) {
            StringBuffer tpd = new StringBuffer();
            tpd.append("\nExecutor ");
            tpd.append(i);
            tpd.append(" PoolSize: ");
            tpd.append(executor.getPoolSize());
            tpd.append(" Tasks: ");
            tpd.append(executor.getQueue().size());
            tpd.append(" max queue: ");
            tpd.append(this.maxQueueLength.get(executor));
            buffer.append(tpd);
            i++;
        }
        return buffer.toString();
    }

    @Override
    public void dispatch(String key, Runnable runnable, int priority) {
        this.dispatch(key, runnable);
    }

    @Override
    public void setKeys(String[] keys) {
    	for (String key:keys) {
    		this.keys.add(key);
    	}
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setThreads(int threads) {
        this.threads = threads;
    }

    @Override
    public void start() {
        /*
         * Create the executors.
         */
        this.executors = new ThreadPoolExecutor[this.threads];
        for (int i = 0; i < this.executors.length; i++) {
            /*
             * Create a thread pool of one. The reason for this is to allow the explicit
             * use of a ThreadPoolExecutor, which can reveal queue length.
             */
            this.executors[i] =
                    new ThreadPoolExecutor(
                    1,
                    1,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new PrioritisedNamedThreadFactory("dispatcher", this.threadPriority));
            if (logger.isInfoEnabled()) {
                logger.info("Prestarting all threads in " + this.name);
            }
            this.executors[i].prestartAllCoreThreads();
            if (logger.isInfoEnabled()) {
                logger.info("All threads started in " + this.name);
            }
        }
        /*
         * Assignments.
         */
        if (!canExpand) {
            this.executorsByKey = new HashMap<String, ThreadPoolExecutor>();
        }

        /*
         * Create the initial key to executors.
         */
        if (this.keys != null && this.keys.size() > 0) {
            for (String key : keys) {
                ThreadPoolExecutor executor = this.assign(this);
                if (executor != null) {
                    this.executorsByKey.put(key, executor);
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info(name + " configured with " + this.threads + " executors");
        }
    }

    @Override
    public void stop() {
        if (logger.isInfoEnabled()) {
            logger.info("Stopping dispatcher " + this.name);
        }
        for (int i = 0; i < this.executors.length; i++) {
            this.executors[i].shutdown();
        }
        if (logger.isInfoEnabled()) {
            logger.info("Stopped dispatcher " + this.name);
        }
    }

    public boolean isCanExpand() {
        return canExpand;
    }

    public void setCanExpand(boolean canExpand) {
        this.canExpand = canExpand;
    }

    public int getThreadPriority() {
        return threadPriority;
    }

    public void setThreadPriority(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    @Override
    public long getExecutedTaskCount(String key) {
        ThreadPoolExecutor e = this.executorsByKey.get(key);
        if (e  != null) {
            return e.getCompletedTaskCount();
        }
        return 0;
    }

    @Override
    public long getQueuedTaskCount(String key) {
        ThreadPoolExecutor e = this.executorsByKey.get(key);
        if (e  != null) {
            return e.getQueue().size();
        }
        return 0;
    }

	@Override
	public void drainQueue(List<Runnable> drainQueue, String key) {
		ThreadPoolExecutor e = this.executorsByKey.get(key);
        if (e  != null) {
            e.getQueue().drainTo(drainQueue);
        }
	}

	@Override
	public long getQueuedTaskCount() {
        long total = 0;
        for (String key : this.allKeys) {
            total += this.getQueuedTaskCount(key);
        }
        return total;
	}

}
