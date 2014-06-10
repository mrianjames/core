package com.oaktree.core.threading.dispatcher.SingleDispatcher;

import com.oaktree.core.threading.dispatcher.AbstractDispatcher;
import com.oaktree.core.threading.dispatcher.IDispatcher;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 20/05/11
 * Time: 08:47
 */
public class NullDispatcher extends AbstractDispatcher {
    private Set<String> allKeys = new CopyOnWriteArraySet<String>();

    @Override
    public void dispatch(String key, Runnable runnable, int priority) {
        allKeys.add(key);
        runnable.run();
    }

    @Override
    public String[] getKeys() {
        return allKeys.toArray(new String[allKeys.size()]);
    }

    @Override
    public int getCurrentThreads() {
        return 0;
    }

    @Override
    public int getMaxThreads() {
        return 0;
    }

    @Override
	public void setThreads(int i) {		
	}
}