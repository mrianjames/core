package com.oaktree.core.gc;

import com.oaktree.core.utils.Text;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;

import java.lang.management.MemoryUsage;
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
    public GCSnapshot(GCSnapshot sn) {
        this.set(sn);
    }

    public void set(GCSnapshot sn) {
        if (sn == null) {
            return;
        }
        this.numGcEvents = sn.numGcEvents;
        this.totalRemoved = sn.totalRemoved;
        this.gcEventsByType.putAll(sn.gcEventsByType);
        this.gcDurationUsByType.putAll(sn.gcDurationUsByType);
        this.gcRemovedBytesByType.putAll(sn.gcRemovedBytesByType);
        this.start = sn.start;
        this.end = sn.end;
        this.gcDuration = sn.gcDuration;
    }

    public GCSnapshot() {}
    private long numGcEvents = 0;
    private Map<String,AtomicLong> gcEventsByType = new HashMap<>(10);
    private Map<String,AtomicLong> gcRemovedBytesByType = new HashMap<>(10);
    private Map<String,AtomicLong> gcDurationUsByType = new HashMap<String,AtomicLong>(10);
    private long gcDuration = 0;
    private long start = 0;
    private long end = 0;

    public long getGcDuration() {
        return gcDuration;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getTotalRemovedBytes() {
        return totalRemoved;
    }
    public long getTotalRemovedK() {
        return getTotalRemovedBytes()/1024;
    }
    public long getTotalRemovedM() {
        return getTotalRemovedK()/1024;
    }

    public long getNumGcEvents() {
        return numGcEvents;
    }

    private long totalRemoved = 0;
    private String[] types = new String[]{};
    public String[] getTypes() {
        return this.types;
    }
    public GCSnapshot(String[] types) {
        this.types = types;
        for (String type:types) {
            gcEventsByType.put(type,new AtomicLong(0));
            gcDurationUsByType.put(type,new AtomicLong(0));
            gcRemovedBytesByType.put(type,new AtomicLong(0));
        }
    }
    //handle and apply a gc event from jmx.
    public void onGcEvent(long time, GarbageCollectionNotificationInfo event) {
        GcInfo info = event.getGcInfo();
        long duration = info.getDuration();
        gcEventsByType.get(event.getGcName()).incrementAndGet();
        gcDurationUsByType.get(event.getGcName()).addAndGet(duration);

        long removed = 0;
        Map<String, MemoryUsage> membefore = info.getMemoryUsageBeforeGc();
        Map<String, MemoryUsage> memafter = info.getMemoryUsageAfterGc();
        for (String key:membefore.keySet()) {
            MemoryUsage bmu = membefore.get(key);
            MemoryUsage amu = memafter.get(key);
            removed += bmu.getUsed()-amu.getUsed();
            double usedPct = amu.getUsed()/amu.getMax();
            System.out.println(key+" Used now: " + amu.getUsed() + " was: "+bmu.getUsed() + " removed: "+removed + " Used/Max: " + usedPct+"% Used/Comm: " +);
        }
        gcRemovedBytesByType.get(event.getGcName()).addAndGet(removed);
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
            b.append("removed: ");
            b.append(gcRemovedBytesByType.get(type));
            b.append(" events: ");
            b.append(gcEventsByType.get(type));
            b.append(" duration: ");
            b.append(gcDurationUsByType.get(type));
            b.append("] ");
        }
        return "["+ Text.renderTime(start)+"-"+Text.renderTime(end) +"] TotalDuration: " + gcDuration + "us " + numGcEvents + " events. Collected: " + Text.to2Dp(totalRemoved) + " "+b.toString();
    }

    public void clear() {
        numGcEvents = 0;
        gcEventsByType.clear();
        gcDurationUsByType.clear();
        gcRemovedBytesByType.clear();
        types = new String[]{};
        gcDuration = 0;
        start = 0;
        end = 0;
    }


}
