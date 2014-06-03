package com.oaktree.core.gc;

import com.oaktree.core.utils.Text;

public class GCMemoryArea {

	private String name;
	private long wasCommitted;
	private long isCommitted;
	private GCEvent gcEvent;
	private long wasUsed;
	private long isUsed;
	
	public GCMemoryArea() {}
	public GCMemoryArea(String areaName, GCEvent event, long wasCommitted, long isCommitted, long wasUsed, long isUsed) {
		this.name = areaName;
		this.gcEvent = event;
		this.wasCommitted = wasCommitted;
		this.isCommitted = isCommitted;
		this.wasUsed = wasUsed;
		this.isUsed = isUsed;
	}
	public void setMemoryAreaName(String name)  {this.name = name; }
	public void setGCEvent(GCEvent event) {
		this.gcEvent =event;
	}
	
	public void setWasCommitted(long wasCommitted) { this.wasCommitted = wasCommitted; }
	public void setIsCommitted(long isCommitted) { this.isCommitted = isCommitted; }
	
	public double getCommittedChangeB() {
		return isCommitted - wasCommitted;
	}
	public double getCommittedChangeK() {
		return getCommittedChangeB()/1024d;
	}
	public double getCommittedChangeM() {
		return getCommittedChangeK()/1024d;
	}
	public double getWasCommittedB() {
		return wasCommitted;
	}
	public double getWasCommittedK() {
		return getWasCommittedB()/1024;
	}
	public double getWasCommittedM() {
		return getWasCommittedK()/1024;
	}
	public double getIsCommittedB() {
		return isCommitted;
	}
	public double getIsCommittedK() {
		return getIsCommittedB()/1024;
	}
	public double getIsCommittedM() {
		return getIsCommittedK()/1024;
	}
	public void setWasUsed(long wasUsed) { this.wasUsed = wasUsed; }
	public void setIsUsed(long isUsed) { this.isUsed = isUsed; }
	
	public double getUsedChangeB() {
		return isUsed - wasUsed;
	}
	public double getUsedChangeK() {
		return getUsedChangeB()/1024d;
	}
	public double getUsedChangeM() {
		return getUsedChangeK()/1024d;
	}
	
	public double getWasUsedB() {
		return wasUsed;
	}
	public double getWasUsedK() {
		return getWasUsedB()/1024;
	}
	public double getWasUsedM() {
		return getWasUsedK()/1024;
	}
	public double getIsUsedB() {
		return isUsed;
	}
	public double getIsUsedK() {
		return getIsUsedB()/1024;
	}
	public double getIsUsedM() {
		return getIsUsedK()/1024;
	}
	
	public String getMemoryAreaName() {
		return name;
	}
	public GCEvent getGCEvent() {
		return gcEvent;
	}
	@Override
	public String toString() {
		return name + "[comm:{ "+ Text.to2Dp(getWasCommittedM()) +"->"+Text.to2Dp(getIsCommittedM())+"} used:{"+Text.to2Dp(getWasUsedM())+"->"+Text.to2Dp(getIsUsedM())+"}]";
	}
}
