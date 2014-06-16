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
	public long getTotalGCTimeMs();
    public long getTotalGCTimeUs();
	public double getTotalRemovedB();
	public double getTotalRemovedK();
	public double getTotalRemovedM();
    public GCSnapshot[] getAllSnapshots();
    public GCMemoryAreaSnapshot[] getAreaSnapshots(String type);
    public GCSnapshot[] getSnapshotsBetween(long start, long end);
    public long getNumEvents();


}
