package com.oaktree.core.process;

import com.oaktree.core.utils.Text;

import java.lang.management.MemoryUsage;

public class MemoryPoolWrapper {
	private MemoryUsage pool;
	private long time;

	public MemoryPoolWrapper(MemoryUsage pool, long time) {
		this.time = time;
		this.pool = pool;
	}
	
	public long getInit() {
		return pool.getInit();
	}
	public long getMax() {
		return pool.getMax();
	}
	public long getCommitted() {
		return pool.getCommitted();
	}
	public long getUsed() {
		return pool.getUsed();
	}
	public String getTime() {
		return Text.renderTime(time);
	}
	
	public double getInitK() {
		return pool.getInit()/1024d;
	}
	public double getMaxK() {
		return pool.getMax()/1024d;
	}
	public double getCommittedK() {
		return pool.getCommitted()/1024d;
	}
	public double getUsedK() {
		return pool.getUsed()/1024d;
	}

	public double getInitM() {
		return pool.getInit()/1024d/1024d;
	}
	public double getMaxM() {
		return pool.getMax()/1024d/1024d;
	}
	public double getCommittedM() {
		return pool.getCommitted()/1024d/1024d;
	}
	public double getUsedM() {
		return pool.getUsed()/1024d/1024d;
	}
}
