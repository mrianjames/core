package com.oaktree.core.threading.dispatcher;

import java.util.List;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 20/05/11
 * Time: 08:48
 */
public abstract class AbstractDispatcher implements IDispatcher{

    private boolean canExpand;
    private String name;

    public boolean canExpand() {
        return canExpand;
    }

    @Override
    public void setKeys(String[] keys) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void dispatch(String key, Runnable task) {
        dispatch(key,task,IDispatcher.NORMAL_PRIORITY);
    }

    @Override
    public void start() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void drainQueue(List<Runnable> drainQueue, String key) {
    }

    @Override
    public long getQueuedTaskCount(String string) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
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
        this.canExpand = canExpand;
    }

    @Override
    public String getName() {
        return name;  //To change body of implemented methods use File | Settings | File Templates.
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
    public int getCurrentThreads() {
        return 0;
    }

    @Override
    public int getMaxThreads() {
        return 0;
    }

}
