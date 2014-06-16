package com.oaktree.core.gc;

import com.oaktree.core.utils.Text;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A periodic statement of GC activity. Expected you generate one of these every minute
 * and this can be stored indefinitely or in some buffer to provide in memory stats.
 *
 * Created by ianjames on 04/06/2014.
 */
public class GCSnapshot {

    private final static Logger logger = LoggerFactory.getLogger(GCSnapshot.class);


    private long numGcEvents = 0;
    private Map<String,GCMemoryAreaSnapshot> memAreaSnapshots = new HashMap<String,GCMemoryAreaSnapshot>(2);
    private long gcDuration = 0;
    private long start = 0;
    private long end = 0;
    private long totalRemoved = 0;
    private String[] types = new String[]{};

    public GCSnapshot(GCSnapshot sn) {
        this.set(sn);
    }

    public void set(GCSnapshot sn) {
        if (sn == null) {
            return;
        }
        this.numGcEvents = sn.numGcEvents;
        this.totalRemoved = sn.totalRemoved;
        for (GCMemoryAreaSnapshot s:sn.getAreaSnapshots().values()) {
            this.memAreaSnapshots.put(s.getName(),new GCMemoryAreaSnapshot(s)); //TODO
        }

        this.start = sn.start;
        this.end = sn.end;
        this.gcDuration = sn.gcDuration;
        this.types = sn.getTypes();
    }

    public GCSnapshot() {}
    public GCMemoryAreaSnapshot getMemoryAreaSnapshot(String type) {
        return memAreaSnapshots.get(type);
    }
    public Map<String,GCMemoryAreaSnapshot> getAreaSnapshots() {
        return memAreaSnapshots;
    }

    public long getGcDuration() {
        return gcDuration;
    }

    public long getStart() {
        return start;
    }
    public String getStartTimeAsString() {
        return Text.renderTime(start);
    }

    public long getEnd() {
        return end;
    }

    public long getTotalRemovedB() {
        return totalRemoved;
    }
    public long getTotalRemovedK() {
        return getTotalRemovedB()/1024;
    }
    public long getTotalRemovedM() {
        return getTotalRemovedK()/1024;
    }

    public long getNumGcEvents() {
        return numGcEvents;
    }

    public String[] getTypes() {
        return this.types;
    }
    public GCSnapshot(String[] types) {
        this.types = types;
        for (String type:types) {
            memAreaSnapshots.put(type,new GCMemoryAreaSnapshot(0,type,0,0,0,0d,0d ));
        }
    }


    //handle and apply a gc event from jmx.
    public synchronized void onGcEvent(long time, GarbageCollectionNotificationInfo event) {
        logger.info("OnGCEvent: "+event.getGcName() + " (types: " + Arrays.toString(this.getTypes())+")");
        GcInfo info = event.getGcInfo();
        long duration = info.getDuration();
        GCMemoryAreaSnapshot mas = memAreaSnapshots.get(event.getGcName());

        long removed = 0;
        Map<String, MemoryUsage> membefore = info.getMemoryUsageBeforeGc();
        Map<String, MemoryUsage> memafter = info.getMemoryUsageAfterGc();
        for (String key:membefore.keySet()) {
            MemoryUsage bmu = membefore.get(key);
            MemoryUsage amu = memafter.get(key);
            long aremoved = bmu.getUsed()-amu.getUsed();
            removed += aremoved;
            double usedMaxPct = ((double)aremoved/(double)amu.getMax())*100d;
            double usedComPct = ((double)aremoved/(double)amu.getCommitted())*100d;
            double wasCap = ((double)bmu.getUsed()/(double)bmu.getMax())*100d;
            double isCap = ((double)amu.getUsed()/(double)amu.getMax())*100d;
            System.out.println(key+" max: "+amu.getMax()+" comm: "+amu.getCommitted()+"Used now: " + amu.getUsed() + " was: "+bmu.getUsed() + " removed: "+(bmu.getUsed()-amu.getUsed()) + " Used/Max: " + Text.to2Dp(usedMaxPct)+"% Used/Comm: " + Text.to2Dp(usedComPct) +"% CapacityBefore: " + Text.to2Dp(wasCap) + "% CapacityNow: " + Text.to2Dp(isCap) + "%.");
            mas.setUsedMaxPct(usedMaxPct);
            mas.setUsedComPct(usedComPct);
            mas.setWasCapacity(wasCap);
            mas.setIsCapacity(isCap);
            mas.setStartTime(time);
            mas.add(1,removed,duration);
        }
        System.out.println("TotalRemoved: "+removed +", " + Text.to2Dp(((double)removed/1024d/1024d)) + "MB");
        mas.add(1, duration, removed);

        totalRemoved += removed;
        gcDuration += duration;
        if (start == 0) {
            start = time;
        }
        end = time;
        numGcEvents++;

    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (String type: this.types) {
            b.append(type);
            b.append("[");
            GCMemoryAreaSnapshot s = memAreaSnapshots.get(type);
            b.append(s.toString());
            b.append("] ");
        }
        return "["+ Text.renderTime(start)+"-"+Text.renderTime(end) +"] TotalDuration: " + gcDuration + "us " + numGcEvents + " events. Collected: " + Text.to2Dp(totalRemoved) + " "+b.toString();
    }

    public synchronized void clear() {
        numGcEvents = 0;
        totalRemoved = 0;
        for (GCMemoryAreaSnapshot a:this.memAreaSnapshots.values()) {
            a.reset();
        }
        types = new String[]{};
        gcDuration = 0;
        start = 0;
        end = 0;
    }


    public void setTypes(String[] types) {
        this.types = types;
    }
}
