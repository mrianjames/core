package com.oaktree.core.threading.dispatcher.mapped;

import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.threading.dispatcher.ITaskProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * A dispatcher that is designed for situations where the key universe is constrained, well known and small.
 * For example, if we know that 32 keys, 8 threads will be the extent of the universe then we can map keys to a thread
 * e.g. each thread processes 4 keys. Contention will exist for processing between the 4 keys sharing a thread and
 * it will be situation dependent on how high the ratio can be raised without causing starvation or adverse latency.
 *
 * The mapped dispatcher supports most common functionality of a dispatcher including draining & prioritisation. Statistics are supported but
 * will report the cumulative values for all threads on each queue.
 *
 * Tasks will be executed on a thread named to the key being dispatched.
 * In this dispatcher a key will always be processed on the same thread.
 *
 * Beware of domination - busy keys could potentially dominate a thread and starve others assigned to that queue.
 *
 * Current assignment of key->queue policy is based on round robin.
 * TODO plugin assignment policies - e.g. least busy.
 */
public class MappedDispatcher implements IDispatcher {

	private final static Logger logger = LoggerFactory.getLogger(MappedDispatcher.class);
	private String name;
	private int threadCount;
    private Map<String,ITaskProcessor> threadQueues = new ConcurrentHashMap<String,ITaskProcessor>();
	private List<String> keys = new ArrayList<String>();
    private ITaskProcessorAssignmentPolicy assignmentPolicy = new RoundRobinTaskProcessorAssignmentPolicy();

	private ITaskProcessor[] queues;
	private boolean canExpand = true;

	public MappedDispatcher(String name, int threads) {
		this.name = name;
		this.threadCount = threads;
	}
	
	public MappedDispatcher() {}
	
	public MappedDispatcher(String name, int threads, String[] keys) {
		this(name,threads);
		for (String key:keys) {
			this.keys.add(key);
		}
	}
	
	@Override
	public void setKeys(String[] keys) {
		for (String key:keys) {
			this.keys.add(key);
		}
	}

	@Override
	public void setThreads(int i) {
		this.threadCount = i;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void dispatch(String key, Runnable task) {
		this.dispatch(key, task, 5);
	}

    /**
     * Assign existing queue/thread to key.
     *
     * @param key
     * @param dispatchTask
     * @return
     */
	private ITaskProcessor assignQueue(String key,DispatchTask dispatchTask) {
		ITaskProcessor qp = assignmentPolicy.assign(key,dispatchTask);
        //if given a creational task ensures it is the first event to be processed so others cant skip the queue.
        if (dispatchTask != null) {
            qp.add(dispatchTask);
        }
		this.threadQueues.put(key, qp);
		return qp;
	}

	@Override
	public void dispatch(String key, Runnable runnable, int priority) {
        ITaskProcessor qp = this.threadQueues.get(key);
		if (qp == null) {
            if (!canExpand) {
                throw new IllegalArgumentException("Key cannot be assigned to a queue when canExpand=false");
            }
            //lock, check again and if still the case create.
            synchronized (key) {
                qp = this.threadQueues.get(key);
		        if (qp == null) {
			        this.assignQueue(key, new DispatchTask(key, runnable, priority));
                } else {
                    qp.add(new DispatchTask(key,runnable,priority));
                }
            }
		} else {
            DispatchTask task = new DispatchTask(key, runnable, priority);
            qp.add(task);
        }
	}

	@Override
	public void start() {
		logger.info("Starting mapped dispatcher " + this.name);
		if (!canExpand) {
			this.threadQueues = new HashMap<String,ITaskProcessor>();
		}

		this.queues = new ITaskProcessor[this.threadCount];
		//create threads
		for (int i = 0; i < threadCount;i++) {
            ITaskProcessor queue = new QueueTaskProcessor(new LinkedBlockingDeque<DispatchTask>());
			queue.setName(this.getName() + "[" + i + "]");
			queue.start();
			this.queues[i] = queue;
		}
        this.assignmentPolicy.setTaskProcessors(queues);
		//assign initial keys.
		for (String key:this.keys) {
			this.assignQueue(key, null);
		}
	}

	@Override
	public void stop() {
        logger.info("Stopping dispatcher");

		for (ITaskProcessor qp:this.queues) {
            qp.stopProcessor();

        }
        logger.info("Stopped all queue processors");
	}

	@Override
	public void drainQueue(List<Runnable> drainQueue, String key) {
//        ITaskProcessor qp = this.threadQueues.get(key);
//		if (qp != null) {
//            List<Runnable> removals = new ArrayList<Runnable>();
//			qp.drain(drainQueue);
//            for (Runnable t:drainQueue) {
//                DispatchTask task = (DispatchTask)t;
//                if  (!task.getKey().equals(key)) {
//                    removals.add(t);
//                }
//            }
//            drainQueue.removeAll(removals);
//		}
	}

	@Override
	public long getQueuedTaskCount(String key) {
        ITaskProcessor qp = this.threadQueues.get(key);
		if (qp == null) {
			return 0;
		}
		return qp.getQueuedTasks();
	}

	@Override
	public long getExecutedTaskCount(String key) {
        ITaskProcessor qp = this.threadQueues.get(key);
		if (qp == null) {
			return 0;
		}
		return qp.getExecutedTasks();
	}

	@Override
	public long getExecutedTaskCount() {
		long cnt = 0;
		for (ITaskProcessor qp:this.threadQueues.values()) {
			cnt += qp.getExecutedTasks();
		}
		return cnt; 
	}

	@Override
	public void setCanExpand(boolean canExpand) {
		this.canExpand = canExpand;
	}

	@Override
	public String getName() {
		return this.name;
	}

    @Override
    public void removeKey(String key) {
        if (canExpand) {
            this.threadQueues.remove(key);
        }
    }

    @Override
    public void removeKeys(String[] keys) {
        if (canExpand) {
            for (String key:keys) {
                this.threadQueues.remove(key);
            }
        }
    }

    @Override
    public String[] getKeys() {
        Set<String> keys = new HashSet<String>();
        for (ITaskProcessor qp:this.threadQueues.values()) {
            keys.addAll(qp.getKeys());
        }
        return keys.toArray(new String[keys.size()]);
    }

    @Override
	public long getQueuedTaskCount() {
		long cnt = 0;
		for (ITaskProcessor qp:this.threadQueues.values()) {
			cnt += qp.getQueuedTasks();
		}
		return cnt; 
	}

}
