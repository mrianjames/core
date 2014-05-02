package com.oaktree.core.threading.dispatcher.SingleDispatcher;

import com.oaktree.core.threading.dispatcher.AbstractDispatcher;
import com.oaktree.core.threading.dispatcher.mapped.DispatchTask;
import com.oaktree.core.threading.dispatcher.mapped.QueueTaskProcessor;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 20/05/11
 * Time: 08:37
 */
public class SingleDispatcher extends AbstractDispatcher{

    private QueueTaskProcessor queue;
    private Set<String> allKeys = new CopyOnWriteArraySet<String>();

    @Override
    public void dispatch(String key, Runnable task) {
        allKeys.add(key);
        queue.add(new DispatchTask(key,task));
    }
    @Override
    public String[] getKeys() {
        return allKeys.toArray(new String[allKeys.size()]);
    }


    @Override
    public void dispatch(String key, Runnable runnable, int priority) {
        queue.add(new DispatchTask(key,runnable,priority));
    }

    @Override
    public void start() {
        BlockingDeque<DispatchTask> q = new LinkedBlockingDeque<DispatchTask>();
        queue = new QueueTaskProcessor(q);
        queue.start();

    }

    @Override
    public void stop() {
        queue.stopProcessor();
        queue.interrupt();
    }

    @Override
    public void drainQueue(List<Runnable> drainQueue, String key) {
        drainQueue.addAll(queue.getQueue());
    }

    @Override
    public long getQueuedTaskCount(String string) {
        return queue.getQueuedTasks();
    }

    @Override
    public long getQueuedTaskCount() {
        return queue.getQueuedTasks();
    }

    @Override
    public long getExecutedTaskCount(String string) {
        return queue.getExecutedTasks();
    }

    @Override
    public long getExecutedTaskCount() {
        return queue.getExecutedTasks();
    }
	@Override
	public void setThreads(int i) {		
	}

}
