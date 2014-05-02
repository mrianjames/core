package com.oaktree.core.threading.dispatcher.Monitoring;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentType;

import java.util.ArrayList;
import java.util.List;

public class DispatcherService extends AbstractComponent implements IDispatcherListener {

	private List<DispatchSnapshot> snapshots = new ArrayList<DispatchSnapshot>();
	public DispatcherService(String name) {
		setName(name);
		setComponentType(ComponentType.SERVICE);
		setComponentSubType("DispatchService");
	}
	@Override
	public synchronized void onDispatchStatistics(DispatchSnapshot snapshot) {
		snapshots .add(snapshot);
	}

	public synchronized DispatchSnapshot[] getSnapshots() {
		return snapshots.toArray(new DispatchSnapshot[snapshots.size()]);
	}
	
}
