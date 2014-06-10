package com.oaktree.core.threading.dispatcher.burner;

import com.oaktree.core.logging.Log;
import com.oaktree.core.threading.dispatcher.IDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * TODO review for removal...
 *
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 30/06/11
 * Time: 16:34
 */
public class BurnerDispatcher implements IDispatcher, Runnable {

    private Thread thread = new Thread();
    private String name;
    private final Deque<Runnable> queue = new ArrayDeque<Runnable>(1000000);
    private boolean run = true;
    private final static Logger logger = LoggerFactory.getLogger(BurnerDispatcher.class);

    @Override
    public int getCurrentThreads() {
        return 1;
    }

    @Override
    public int getMaxThreads() {
        return 1;
    }

    public BurnerDispatcher() {
    }
    public BurnerDispatcher(String name) {
        this();
        this.name = name;
    }

    @Override
    public void setKeys(String[] keys) {
    }

    @Override
    public void setThreads(int i) {
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void dispatch(String key, Runnable task) {
        queue.add(task);
    }
    
    private Set<String> keys = new CopyOnWriteArraySet<String>();

    @Override
    public void dispatch(String key, Runnable runnable, int priority) {
        keys.add(key);
        if (priority == IDispatcher.HIGH_PRIORITY) {
            synchronized (queue) {
                queue.addFirst(runnable);
            }
        } else {
            synchronized (queue) {
                queue.add(runnable);
            }
        }

    }

    @Override
    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (run) {
                //rlock.lock();
                Runnable r;
                synchronized (queue) {
                    r = queue.poll();
                }
                //rlock.unlock();
                if (r != null) {
                   r.run();
                }
            }
        } catch (Throwable t) {
            Log.exception(logger,t);
        }
    }

    @Override
    public void stop() {
        run = false;
        thread.interrupt();
    }

    @Override
    public void drainQueue(List<Runnable> drainQueue, String key) {
    }

    @Override
    public long getQueuedTaskCount(String string) {
        return queue.size();
    }

    @Override
    public long getQueuedTaskCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getExecutedTaskCount(String string) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getExecutedTaskCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setCanExpand(boolean canExpand) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void removeKey(String key) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeKeys(String[] keys) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String[] getKeys() {
        return keys.toArray(new String[keys.size()]);
    }
}
