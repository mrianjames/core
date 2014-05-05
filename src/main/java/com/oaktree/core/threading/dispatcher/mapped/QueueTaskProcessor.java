package com.oaktree.core.threading.dispatcher.mapped;

import com.oaktree.core.logging.Log;
import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.threading.dispatcher.ITaskProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArraySet;

/**
* A task processor that uses a thread/queue combination that will process the tasks. This object will be shared by a number of
* keys.
 *
*/
public class QueueTaskProcessor extends Thread implements ITaskProcessor {

    private final static Logger logger = LoggerFactory.getLogger(QueueTaskProcessor.class);
    /**
     * For statistics.
     */
    private Set<String> allKeys = new CopyOnWriteArraySet<String>();

    private BlockingDeque<DispatchTask> queue;
		private volatile boolean run = true;
		private volatile long executedTasks;
        //private Map<String,Long> etasks = new HashMap<String,Long>();
		public QueueTaskProcessor(BlockingDeque<DispatchTask> queue) {
			this.queue = queue;
		}
		public long getQueuedTasks() {
			return this.queue.size();
		}
		public long getExecutedTasks() {
			return this.executedTasks;
		}
//        public long getExecutedTasks(String key) {
//            Long o =  etasks.get(key);
//            if (o != null) {
//                return o;
//            }
//            return 0;
//        }

    @Override
		public void add(DispatchTask r) {
            allKeys.add(r.getKey());
			if (r.getPriority() == IDispatcher.HIGH_PRIORITY) {
				queue.addFirst(r);
			} else {
				queue.add(r);
			}
		}

    @Override
    public void drain(List<DispatchTask> drainer) {
        queue.drainTo(drainer);
    }

    int count = 0;
        @Override
		public void run() {
			try {
				while (run) {
					DispatchTask r = queue.take(); //5/6us with cpu 40-60%.
			        //DispatchTask r = queue.poll(10,TimeUnit.MICROSECONDS); //14us cpu 400%
                    //DispatchTask r = queue.poll(); //5us but cpu 700%
					if (r != null) {
                        //long et = getExecutedTasks(r.getKey());
                        executedTasks++;
						//etasks.put(r.getKey(),++et);
                        this.setName(r.getKey());
						r.run();
					}
				}
			} catch (Exception e) {
                if (run) {
				    Log.exception(logger, e);
                }
			}
		}

    @Override
		public void stopProcessor() {
			run = false;
            //jump out of take if stuck there.
            this.interrupt();
		}

    public BlockingDeque<DispatchTask> getQueue() {
        return queue;
    }

    @Override
    public Collection<String> getKeys() {
        return allKeys;
    }
}
