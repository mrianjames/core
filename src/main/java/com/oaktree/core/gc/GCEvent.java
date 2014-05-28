package com.oaktree.core.gc;

import java.util.ArrayList;
import java.util.Collection;

import com.oaktree.core.utils.Text;

//name is ParNew
public class GCEvent {
    private final String cause;
    private final String action;
    private long stime;
    private long etime;
	private String name;
	private String type;
	
	public GCEvent(long stime, long etime, String type,String name,String action, String cause) {
    	this.stime = stime;
        this.etime = etime;
		this.name = name;
		this.cause = cause;
        this.action = action;
        this.type = type;
	}

	public long getStartTime() {
		return stime/1000;
	}
	public String getStartTimeAsString() {
		return Text.renderTime(stime/1000);
	}
    public long getEndTime() { return etime; }

	public String getName() {
		return name;
	}


	

    public String getCause() { return this.cause; }

    public String getAction() { return this.action; }

    
    public long getDuration() { return this.etime - this.stime; }

    private Collection<GCMemoryArea> gcMemoryAreas = new ArrayList<GCMemoryArea>();
    public void addMemoryArea(String areaName, GCEvent event, long wasCommitted, long isCommitted, long wasUsed, long isUsed) {
    	gcMemoryAreas.add(new GCMemoryArea(areaName, event, wasCommitted, isCommitted, wasUsed, isUsed));
    }
    
    public boolean isG1Action() {
    	return GCService.G1.equals(this.type); 
    }
    public boolean isYoungAction() {
    	return GCService.YOUNG.equals(this.type);  
    }
    public boolean isOldAction() {
    	return GCService.OLD.equals(this.type);  
    }
    @Override
    public String toString() {
    	
        return Text.renderTime(stime/1000) + " " +name + " ("+type+")" + action + " "+ cause + " duration:" + (etime-stime) + "us. " + getDescription().toString();
    }
    public String getDescription() {
    	StringBuilder memareas = new StringBuilder();
    	for (GCMemoryArea area:gcMemoryAreas) {
    		memareas.append(Text.LEFT_SQUARE_BRACKET);
    		memareas.append(area.toString());
    		memareas.append(Text.RIGHT_SQUARE_BRACKET);
    	}
    	return memareas.toString();
    }
    
    public long getRemovedB() {
    	long cleared = 0;
    	for (GCMemoryArea area:gcMemoryAreas) {
    		cleared += area.getUsedChangeB();
    	}
    	return -cleared;
    }
    
    public long getRemovedK() {
    	return getRemovedB()/1024;
    }
    public long getRemovedM() {
    	return getRemovedK()/1024;
    }
}
