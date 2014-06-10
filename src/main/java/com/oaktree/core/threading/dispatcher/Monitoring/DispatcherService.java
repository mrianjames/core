package com.oaktree.core.threading.dispatcher.Monitoring;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentType;
import com.oaktree.core.threading.dispatcher.IDispatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple service that hold dispatcher watchdog output for presentation.
 */
public class DispatcherService extends AbstractComponent implements IDispatcherListener {

    private  String dispatcherType;
    private  String dispatcherName;
    private List<DispatchSnapshot> snapshots = new ArrayList<DispatchSnapshot>();
    private int maxThreads;
    private int currentThreads;

    public DispatcherService(String name) {
		setName(name);
        setComponentType(ComponentType.SERVICE);
		setComponentSubType("DispatchService");
	}
	@Override
	public synchronized void onDispatchStatistics(IDispatcher dispatcher,DispatchSnapshot snapshot) {
        if (dispatcherName == null) {
            dispatcherName = dispatcher.getName();
        }
        if (dispatcherType == null) {
            dispatcherType = dispatcher.getClass().getSimpleName();
        }
        currentThreads = dispatcher.getCurrentThreads();
        maxThreads = dispatcher.getMaxThreads();
		snapshots .add(snapshot);
	}

	public synchronized DispatchSnapshot[] getSnapshots() {
		return snapshots.toArray(new DispatchSnapshot[snapshots.size()]);
	}
	public synchronized String getDispatcherName() {
        return dispatcherName;
    }
    public synchronized String getDispatcherType() {
        return dispatcherType;
    }
    public synchronized int getDispatcherCurrentThreads() { return currentThreads; }
    public synchronized int getDispatcherMaxThreads() { return maxThreads; }
}
