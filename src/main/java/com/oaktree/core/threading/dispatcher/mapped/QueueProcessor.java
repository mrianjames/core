package com.oaktree.core.threading.dispatcher.mapped;

import com.oaktree.core.logging.Log;
import com.oaktree.core.threading.dispatcher.IDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

/**
* An thread/queue combination that will process the tasks. This object will be shared by a number of
* keys.
*/
public class QueueProcessor extends Thread {

    private final static Logger logger = LoggerFactory.getLogger(QueueProcessor.class);

    private BlockingDeque<DispatchTask> queue;
		private volatile boolean run = true;
		private volatile long executedTasks;
        //private Map<String,Long> etasks = new HashMap<String,Long>();
		public QueueProcessor(BlockingDeque<DispatchTask> queue) {
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

		public void add(DispatchTask r) {
			if (r.getPriority() == IDispatcher.HIGH_PRIORITY) {
				queue.addFirst(r);
			} else {
				queue.add(r);
			}
		}

		int count = 0;
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

		public void stopProcessor() {
			run = false;
		}

    public BlockingDeque<DispatchTask> getQueue() {
        return queue;
    }
}
