package com.oaktree.core.gc;

import java.util.Collection;

import com.oaktree.core.container.IComponent;

/**
 * Service that exposes GC information.
 * 
 * @author ij
 *
 */
public interface IGCService extends IComponent {
//	public GCEvent[] getAllGCEvents();
//	public GCEvent[] getAllGCEvents(String type);
//	public String[] getGCEventTypes();
//	public GCEvent[] getAllGCEventsBetween(long start, long end);
//	public GCEvent[] getAllGCEventsBetween(String type,long start, long end);
	public long getTotalGCTimeMs();
	public double getTotalRemovedB();
	public double getTotalRemovedK();
	public double getTotalRemovedM();
    public GCSnapshot[] getAllSnapshots();
    public GCSnapshot[] getAllSnapshots(String type);
    public GCSnapshot[] getSnapshotsBetween(long start, long end);
    public long getNumEvents();


}
