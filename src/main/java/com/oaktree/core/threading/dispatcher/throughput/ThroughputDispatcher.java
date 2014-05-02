package com.oaktree.core.threading.dispatcher.throughput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.threading.dispatcher.PrioritisedDispatchTask;
import com.oaktree.core.threading.PrioritisedNamedThreadFactory;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.JavaTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.INameable;
import com.oaktree.core.logging.Log;

/**
 * ThroughputDispatcher class implements IDispatcher using individual queues for tasks in time/priority orders and a 
 * threadpool servicing a main queue. When a task is added to a keys queue a notification is placed on the 
 * main threadpool queue that this queue should be inspected for work. When a thread becomes free it runs 
 * this inspection task and if any work is found it is then run on that thread. In order to minimise unnecesary 
 * context switching on completion of this task further tasks may be run before returning the thread back to the 
 * thread pool ready to process other task queues work. 
 * <img src="../../../../../../../images/dispatcher.png"/>
 */

public class ThroughputDispatcher extends AbstractComponent implements IDispatcher {

    //collection of keys/queues - thread safety??
    private List<String> keys = new CopyOnWriteArrayList<String>();
    private int dominationCount = 1;
    private ITime time = new JavaTime();

    public void setTime(ITime time) {
        this.time = time;
    }

    @Override
    public long getExecutedTaskCount() {
        long total = 0;
        for (String key:this.keys) {
            total += this.getExecutedTaskCount(key);
        }
        return total;
    }
    
    @Override
    public long getQueuedTaskCount() {
        long total = 0;
        for (String key:this.keys) {
            total += this.getQueuedTaskCount(key);
        }
        return total;
    }

	/**
	 * A prioritisable task that can invoke the next push to the main queue from the 
	 * keys queue when it completes.
	 */
	private class FollowOnPrioritisedDispatchTask extends PrioritisedDispatchTask {
		
		private final MyQueue<FollowOnPrioritisedDispatchTask> queue;
		
		public FollowOnPrioritisedDispatchTask(IDispatcher dispatcher, Runnable r,int priority, boolean recordTaskStats,MyQueue<FollowOnPrioritisedDispatchTask> queue) {
			super(dispatcher,r,priority,recordTaskStats,time);
			this.queue = queue;
		}
		
		public FollowOnPrioritisedDispatchTask(IDispatcher dispatcher,Runnable task, MyQueue<FollowOnPrioritisedDispatchTask> queue) {
			super(dispatcher,task,0,time);
			this.queue = queue;
		}
		
		@Override
		public boolean equals(Object o) {
			return super.equals(o);
		}
		
		@Override
		public int hashCode() {
			return super.hashCode();
		}
		
		@Override
		public void run() {
			try {	
				Thread.currentThread().setName(this.queue.getName());
				super.run();
				queue.incrementExecutedTasks();
			} catch (Throwable t) {
				Log.exception(logger, t);
			} finally {
                try {
                    /*
                     * Get next task and put in main queue for servicing.
                     */
                    FollowOnPrioritisedDispatchTask rbl = queue.poll();
                    if (rbl!=null) {
                        //logger.info("Found task on queue " + queue.getName() + " size: " + queue.size());
                        //executorPool.submit(rbl);
                        int c = queue.incrementAndGetDominationCount();
                        if (c % ThroughputDispatcher.this.dominationCount == 0) {
                            executorPool.submit(rbl);
                        } else {
                            rbl.run();
                        }

                    } else {
                        //logger.info("Queue was empty");
                        queue.releaseFlag();
                        processNextQueueTask(queue);
                    }
                } catch (RejectedExecutionException e) {
                    Log.exception(logger,e);
                } catch  (Throwable e) {
                    Log.exception(logger,e);
                }
			}
		}

	}

	/**
	 * Definition of our queue.
	 * @author jameian
	 *
	 * @param <E>
	 */
	private interface MyQueue<E> extends BlockingDeque<E> {
		 public boolean aquireFlag();		    
		public String getName();
		public long getExecutedTasks();
		public void incrementExecutedTasks();
		public void releaseFlag();
        public int incrementAndGetDominationCount();
        public void clearDominationCount();
	} 


	/**
	 * A queue with a flag on it to say if its processable.
	 * @author oaktree
	 *
	 * @param <V>
	 */
	@SuppressWarnings("serial")
	private static class QueueWrapper<V> extends LinkedBlockingDeque<V>  implements MyQueue<V> {
	    
		public QueueWrapper(String name) {
			this.name = name;
		}
		
		private String name;
        private int count;
		private long executedTasks = 0;
	    
	    public Semaphore flag = new Semaphore(1);
	    
	    @Override
		public boolean aquireFlag() {
			return flag.tryAcquire();
		}
	    
		public long getExecutedTasks() {
			return this.executedTasks;
		}	    
		
		public void incrementExecutedTasks() {
	    	this.executedTasks++;	    	
	    }
		@Override
		public void releaseFlag() {
			flag.release();
		}

        @Override
        public int incrementAndGetDominationCount() {
            count += 1;
            return count;
        }

        @Override
        public void clearDominationCount() {
            count = 0;
        }

        @Override
		public String getName() {
			return this.name;
		}	
	}	

	/**
	 * Constant for the initial allocation of default hashmap. Can also create queues upfront
	 * with setKeys
	 */
	private static final int INITIAL_KEY_SIZE = 10000;

	static final Logger logger = LoggerFactory.getLogger(ThroughputDispatcher.class.getName());


	/**
	 * Can this dispatcher expand its list of keys.
	 */
	private boolean canExpand = true;
	
	/**
	 * The thread pool that will sevice the work in the main queue when put on.
	 */
	private ThreadPoolExecutor executorPool ;

	/**
	 * The main queue that will give the thread pool work. 
	 */
	private LinkedBlockingQueue<Runnable> mainQueue;

	/**
	 * A map of queues of tasks to a named key.
	 */
	private Map<String, MyQueue<FollowOnPrioritisedDispatchTask>> queues = new HashMap<String, MyQueue<FollowOnPrioritisedDispatchTask>>(INITIAL_KEY_SIZE);
	
	/**
	 * A factory for tuning the specifics of the threads in this dispatcher.
	 */
	private ThreadFactory threadFactory = new PrioritisedNamedThreadFactory("dispatcher",Thread.NORM_PRIORITY);
	
	/**
	 * The thread priority we shall make threads with.
	 */
	private int threadPriority = Thread.NORM_PRIORITY;
	

	/**
	 * Number of threads the pool shall use.
	 */
	private int threads = 4;
	
	/**
	 * Make a named dispatcher with a specified number of threads.
	 * @param name
	 * @param threads
	 */
	public ThroughputDispatcher(String name, int threads) {
	    this.setName(name);
	    this.threads = threads;
	}
	
	public ThroughputDispatcher() {}
	
	public ThroughputDispatcher(String name, String[] keys, int threads) {
		this(name,threads);
		this.setKeys(keys);
	}

	/**
	 * Make a new queue for a named key.
	 * @param key
	 * @return
	 */
	private MyQueue<FollowOnPrioritisedDispatchTask> createQueue(String key) {
            this.keys.add(key);
		MyQueue<FollowOnPrioritisedDispatchTask> queue;
		synchronized (key.intern()) {
			queue = this.queues.get(key);
			if (queue == null) {
				if (logger.isTraceEnabled()) {
					logger.trace(this.getName() + " making new queue for " + key);
				}
				queue = new QueueWrapper<FollowOnPrioritisedDispatchTask>(key);				
				MyQueue<FollowOnPrioritisedDispatchTask> anotherQueue = this.queues.put(key, queue);
				if (anotherQueue!=null) {
				    queue = anotherQueue;
				}
			}
		}
		return queue;
	}
	
	@Override
	public void dispatch(String key, Runnable task) {
		this.dispatch(key, task,5);
	}
	
	@Override
	public void drainQueue(List<Runnable> drainQueue,String key) {
		MyQueue<FollowOnPrioritisedDispatchTask> queue = this.queues.get(key);
		queue.aquireFlag();
		queue.drainTo(drainQueue);
		queue.releaseFlag();
	}

	/**
	 * An id generator for making task ids, mainly for latency tracking.
	 */
	private final AtomicLong idgen = new AtomicLong();
	
	@Override
	public void dispatch(String key, Runnable task, int priority) {
		if (key != null) {
			String theName = key;
			MyQueue<FollowOnPrioritisedDispatchTask> queue = this.queues.get(theName);
			if (queue == null) {
				queue = createQueue(key);				
			}
			FollowOnPrioritisedDispatchTask newTask = new FollowOnPrioritisedDispatchTask(this,task,priority,false, queue);
			
			if (priority == IDispatcher.HIGH_PRIORITY) {
				queue.addFirst(newTask);
			} else {
				queue.add(newTask);
			}
			processNextQueueTask(queue);
			
		} else {			
			/*
			 * default dispatch.
			 */
			try {
				executorPool.submit(task, null);
			} catch (Exception e) {
				logger.warn("Error dispatching on key " + key + ": " + e.getMessage());
			}
		}
	}

	@Override
	public long getExecutedTaskCount(String key) {
		MyQueue<FollowOnPrioritisedDispatchTask> wrapper = this.queues.get(key);
		 if (wrapper != null) {
			 return wrapper.getExecutedTasks();
		 }
		 return 0;
	}


	@Override
	public long getQueuedTaskCount(String key) {
		MyQueue<FollowOnPrioritisedDispatchTask> wrapper = this.queues.get(key);
		 if (wrapper != null) {
			 return wrapper.size();
		 }
		 return 0;
	}
	
	/**
	 * Get the number of queues we have.
	 * @return
	 */
	public int getNumQueues() {
		return this.queues.size();
	}

	/**
	 * Get the priority we have setup the threads with.
	 * @return
	 */
	public int getThreadPriority() {
		return threadPriority;
	}

	/**
	 * Get the number of threads in the thread pool.
	 * @return
	 */
	public int getThreads() {
		return threads;
	}

	/**
	 * Get if we can expand our universe of keys.
	 * @return
	 */
	public boolean canExpand() {
		return canExpand;
	}

	/**
	 * For a given queue try and process the next task (by submitting to the main queue). 
	 */
	private void processNextQueueTask(MyQueue<FollowOnPrioritisedDispatchTask> queue) {
            if (executorPool == null) {
                throw new IllegalStateException("pool not started or invalid. have you called start");
            }
	    FollowOnPrioritisedDispatchTask rbl = queue.peek();
	    if (rbl!=null) {
	    	if (queue.aquireFlag()) {
	        	FollowOnPrioritisedDispatchTask qHead = queue.poll();
	        	if (qHead==rbl) {
	        	    executorPool.submit(rbl);
	        	} else { 
	        		if (qHead!=null) {	        			
		        	    if (queue instanceof QueueWrapper<?>) {
		        	    	((QueueWrapper<FollowOnPrioritisedDispatchTask>)(queue)).addFirst(qHead); //put it back
		        	    	logger.warn("Had to put task back on front.");
		        	    } else {
		        	    	logger.error("Task may have got lost....");	
		        	    }
	        	    }
	        	    queue.releaseFlag();
	        	    /*
	        	     * Needed?
	        	     */
	        	    processNextQueueTask(queue);
	        	}
        	}
	    }
	}

	@Override
	public void setCanExpand(boolean canExpand) {
		this.canExpand = canExpand;
	}

    @Override
    public void removeKey(String key) {
        if (canExpand) {
            this.queues.remove(key);
        }
    }

    @Override
    public void removeKeys(String[] keys) {
        if (canExpand) {
            for (String key:keys) {
                this.queues.remove(key);
            }
        }
    }

    @Override
    public String[] getKeys() {
        return this.keys.toArray(new String[keys.size()]);
    }

    @Override
	public void setKeys(String[] keys) {
		for (String key:keys) {
			this.createQueue(key);
		}
	}

	public void setKeysFromNameables(INameable[] keys) {
		for (INameable key:keys) {
			this.createQueue(key.getName());
		}
	}

        public void setKeysFromList(List<String> keys) {
            for (String key:keys) {
                this.createQueue(key);
            }
        }
	
	/**
	 * When starting threads we will use this thread priority.
	 * @param threadPriority
	 */
	public void setThreadPriority(int threadPriority) {
		this.threadPriority = threadPriority;
	}

	@Override
	public void setThreads(int i) {
		this.threads = i;
	}

	/**
	 * shutdown the dispatcher
	 */
	public void shutdown() {
		this.executorPool.shutdown();
	}

	@Override
	public void start() {
		/*
		 * Make a thread pool and start all threads.
		 */
		if (canExpand) {
			this.queues = new ConcurrentHashMap<String, MyQueue<FollowOnPrioritisedDispatchTask>>();
		}
		this.threadFactory = new PrioritisedNamedThreadFactory(this.getName(),this.threadPriority );
		this.mainQueue = new LinkedBlockingQueue<Runnable>();
		this.executorPool = new ThreadPoolExecutor(this.threads,this.threads,0,TimeUnit.MILLISECONDS,this.mainQueue,this.threadFactory);
        this.executorPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    FutureTask task = (FutureTask)r;
                    String reason = "";
                    if (executor.isShutdown() || executor.isTerminated() || executor.isTerminating()) {
                        reason = "Pool shutdown in progress.";
                    }
                    Object o = task.get();
                    logger.warn("Cannot execute task: " + o.getClass().getName() + " Reason: " + reason);
                } catch (Throwable e) {
                    Log.exception(logger,e);
                }
            }
        });
		this.executorPool.prestartAllCoreThreads();
	}

	@Override
	public void stop() {
		executorPool.shutdown();
	}

	public void setThreadFactory(ThreadFactory factory) {
		this.threadFactory = factory;
	}

    /**
     * Set the number of times we will run the next task off the same queue. A consideration in this number is that if its
     * too high (1000's) then stack overflow may occur. Defaults to 1.
     * @param number
     */
    public void setDominiationCount(int number) {
        this.dominationCount = number;
    }
}
