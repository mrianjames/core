package com.oaktree.core.process;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.container.ComponentType;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.ITimeScheduler;
import com.oaktree.core.time.JavaTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The memory service is a wrapper around JMX information, collecting a window of
 * information for providing to clients.
 * 
 * Memory details will depend on what GC options are used.
 * 
 * TODO only keep a selection of records i.e. last 200.
 * 
 * @author ianjames
 *
 */
public class MemoryService extends AbstractComponent implements Runnable{
	public MemoryService(long captureInterval) {
		setName("MemoryService");
		setComponentType(ComponentType.SERVICE);
		setComponentSubType("MemoryService");
		this.captureInterval = captureInterval;
	}
	private List<MemorySnapshot> snapshots = new ArrayList<MemorySnapshot>();
	
	private Logger logger = LoggerFactory.getLogger(MemoryService.class);
	private MemoryMXBean memBean = ManagementFactory.getMemoryMXBean() ;
	private ITime time = new JavaTime();
	private ITimeScheduler scheduler;
	private long captureInterval;
	public void setScheduler(ITimeScheduler scheduler) {
		this.scheduler = scheduler;
	}
	public void setTime(ITime time) {
		this.time= time;
	}
	
	public MemorySnapshot getSnapshot() {
		MemoryUsage heap = memBean.getHeapMemoryUsage();
		MemoryUsage nonheap = memBean.getNonHeapMemoryUsage();
		List<MemoryPoolMXBean> l = ManagementFactory.getMemoryPoolMXBeans();
		MemorySnapshot snapshot = new MemorySnapshot(time.getTimeOfDay(),heap,nonheap,l);
		return snapshot;
	}
	public void initialise() {
		super.initialise();
		if (scheduler == null) {
			throw new IllegalStateException("No scheduler specified.");
		}
		List<MemoryPoolMXBean> l = ManagementFactory.getMemoryPoolMXBeans();
		for (MemoryPoolMXBean m:l) {
			poolNames.add(m.getName());
		}
	}
	public void start() {
		super.start();
		scheduler.schedule(getName(), 0, captureInterval,this);
		logger.info("Started " + getName()+ " with interval "+captureInterval);
		setState(ComponentState.AVAILABLE);
	}
	private Set<String> poolNames = new HashSet<String>();
	@Override
	public void run() {
		MemorySnapshot s = getSnapshot();
		synchronized (this) {
			snapshots.add(s);
		}
		logger.info(s.toString());
	}
	
	public String[] getPoolNames() {
		return poolNames.toArray(new String[poolNames.size()]);
	}
	public synchronized MemoryPoolWrapper[] getSnapshots(String poolName) {
		List<MemoryPoolWrapper> x = new ArrayList<MemoryPoolWrapper>();
		int i = snapshots.size()-1;
		while (i >= 0) {
			MemorySnapshot s = snapshots.get(i);
			MemoryPoolMXBean poo = s.getPool(poolName);
			
			if (poo != null) {
				x.add(new MemoryPoolWrapper(poo.getUsage(),s.getTime()));
			} else if (poolName.equals("Heap")) {
				MemoryUsage u = s.getHeap();
				if (u != null) {
					x.add(new MemoryPoolWrapper(u,s.getTime()));
				}
			} else if (poolName.equals("NonHeap")) {
				MemoryUsage u = s.getNonheap();
				if (u != null) {
					x.add(new MemoryPoolWrapper(u,s.getTime()));
				}
			}
			i--;
		}
		return x.toArray(new MemoryPoolWrapper[x.size()]);
	}
}
