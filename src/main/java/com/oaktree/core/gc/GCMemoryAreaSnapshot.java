package com.oaktree.core.gc;

import com.oaktree.core.utils.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ianjames on 12/06/2014.
 */
public class GCMemoryAreaSnapshot {
    private double usedComPct;
    private double usedMaxPct;
    private long start;
    private double wasCapacity;
    private double isCapacity;

    public long getStart() {
        return start;
    }
    public String getStartTimeAsString() {
        return Text.renderTime(start);
    }


    public GCMemoryAreaSnapshot(){}
    public GCMemoryAreaSnapshot(GCMemoryAreaSnapshot s){
        this(s.getStart(),s.getName(),s.getEventCount(),s.getRemovedB(),s.getDurationUs(),s.getWasCapacity(),s.getIsCapacity());
    }
    public GCMemoryAreaSnapshot(long startTime,String name, long gcEventsByType, long gcRemovedBytesByType, long gcDurationUsByType,double wasCapacity,double isCapacity) {
        set(startTime,name,gcEventsByType,gcRemovedBytesByType,gcDurationUsByType,wasCapacity,isCapacity);
    }
    public synchronized void set(long startTime, String name, long gcEventsByType, long gcRemovedBytesByType, long gcDurationUsByType,double wasCapacity, double isCapacity) {
        this.name = name;
        this.start = startTime;
        this.gcEventsByType = gcEventsByType;
        this.gcRemovedBytesByType = gcRemovedBytesByType;
        this.gcDurationUsByType = gcDurationUsByType;
        this.wasCapacity = wasCapacity;
        this.isCapacity = isCapacity;
    }
    public synchronized void add(long gcEventsByType,long gcRemovedBytesByType, long gcDurationUsByType) {
        this.gcEventsByType += gcEventsByType;
        this.gcRemovedBytesByType += gcRemovedBytesByType;
        this.gcDurationUsByType += gcDurationUsByType;
    }
    private String name;
    public String getName() {
        return this.name;
    }
    private long gcEventsByType = 0;
    private long gcRemovedBytesByType = 0;
    private long gcDurationUsByType = 0;

    public synchronized long getEventCount() {
        return gcEventsByType;
    }
    public synchronized long getDurationUs() {
        return gcDurationUsByType;
    }

    public synchronized long getRemovedB() {
        return gcRemovedBytesByType;
    }
    public synchronized long getRemovedK() {
        return getRemovedB()/1024;
    }
    public synchronized long getRemovedM() {
        return getRemovedK()/1024;
    }
    @Override
    public String toString() {
        return name + " Events: " + gcEventsByType + " Duration: " + gcDurationUsByType + "us Removed: "+gcRemovedBytesByType+"bytes usedMaxPct: "+usedMaxPct+"% usedComPct: "+usedComPct+"%.";
    }

    public synchronized void setUsedComPct(double usedComPct) {
        this.usedComPct = usedComPct;
    }

    public synchronized void setUsedMaxPct(double usedMaxPct) {
        this.usedMaxPct = usedMaxPct;
    }

    public synchronized void reset() {
        gcEventsByType = 0;
        gcRemovedBytesByType = 0;
        gcDurationUsByType = 0;
        usedComPct = 0;
        usedMaxPct =0;
        start = 0;
        wasCapacity = 0d;
        isCapacity = 0d;
    }

    public void setStartTime(long startTime) {
        this.start = startTime;
    }

    public void setWasCapacity(double wasCap) {
        this.wasCapacity = wasCap;
    }
    public void setIsCapacity(double isCap) {
        this.isCapacity = isCap;
    }
    public double getWasCapacity() {
        return this.wasCapacity;
    }
    public double getIsCapacity() {
        return this.isCapacity;
    }
}
