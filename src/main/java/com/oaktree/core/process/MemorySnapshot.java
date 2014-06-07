package com.oaktree.core.process;

import com.oaktree.core.utils.Text;

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MemorySnapshot {
	private long time;
	private MemoryUsage heap;
	private List<MemoryPoolMXBean> pools;
	private MemoryUsage nonheap;

	public long getTime() {
		return time;
	}

	public MemoryUsage getHeap() {
		return heap;
	}

	public List<MemoryPoolMXBean> getPools() {
		return pools;
	}
	public MemoryPoolMXBean getPool(String name) {
		for (MemoryPoolMXBean p:pools) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(time+" Heap: comm:"+Text.bytesToMB(heap.getCommitted())+ " max:"+Text.bytesToMB(heap.getMax())+" used:"+Text.bytesToMB(heap.getUsed()));
		b.append(", NonHeap: comm:"+Text.bytesToMB(nonheap.getCommitted())+ " max:"+Text.bytesToMB(nonheap.getMax())+" used:"+Text.bytesToMB(nonheap.getUsed()));
		for (MemoryPoolMXBean m:pools) {
			MemoryUsage u = m.getUsage();
			b.append(" [Pool: "+m.getName() + " comm:"+Text.bytesToMB(u.getCommitted())+ " max:"+Text.bytesToMB(u.getMax())+" used:"+Text.bytesToMB(u.getUsed())+"]");
		}
		return b.toString();
	}
	public MemoryUsage getNonheap() {
		return nonheap;
	}

	public MemorySnapshot(long time, MemoryUsage heap, MemoryUsage nonheap,List<MemoryPoolMXBean> pools) {
		this.time = time;
		this.heap = heap;
		this.nonheap = nonheap;
		this.pools = pools;
	}

	public Collection<? extends String> getPoolNames() {
		Set<String> pnames = new HashSet<String>();
		for (MemoryPoolMXBean m:pools) {
			pnames.add(m.getName());
		}
		return pnames;
	}
}
