package com.oaktree.core.gc;

import com.oaktree.core.container.AbstractComponent;
import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryUsage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.LockSupport;

/**
 * -XX:+UseParNewGC -XX:+UseConcMarkSweepGC
 *
 * Created by ianjames on 26/05/2014.
 */
public class MockGCService extends AbstractComponent {

    public static void main(String[] args) throws Exception {
        MockGCService mgc = new MockGCService();
        mgc.start();
        int i = 0;
        while (true) {
            logger.info("Making some stuff for gc " + i);
            i++;
            LockSupport.parkNanos(1000);
        }

    }
    private long startTime;
    private Collection<GCEvent> gcEvents = new CopyOnWriteArrayList<GCEvent>();

    public void start(){
        this.startTime = System.currentTimeMillis()*1000;
        super.start();
        //get all the GarbageCollectorMXBeans - there's one for each heap generation
        //so probably two - the old generation and young generation
        List<GarbageCollectorMXBean> gcbeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
        //Install a notifcation handler for each bean
        for (GarbageCollectorMXBean gcbean : gcbeans) {
            System.out.println(gcbean.getName());
            NotificationEmitter emitter = (NotificationEmitter) gcbean;
            //use an anonymously generated listener for this example
            // - proper code should really use a named class
            NotificationListener listener = new NotificationListener() {
                //keep a count of the total time spent in GCs
                long totalGcDuration = 0;

                //implement the notifier callback handler
                @Override
                public void handleNotification(Notification notification, Object handback) {
                    //we only handle GARBAGE_COLLECTION_NOTIFICATION notifications here
                    if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                        //get the information associated with this notification
                        GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                        //get all the info and pretty print it
                        long duration = info.getGcInfo().getDuration();
                        String gctype = info.getGcAction();
                        if ("end of minor GC".equals(gctype)) {
                            gctype = "Young Gen GC";
                        } else if ("end of major GC".equals(gctype)) {
                            gctype = "Old Gen GC";
                        }
                        System.out.println();
                        System.out.println(gctype + ": - " + info.getGcInfo().getId()+ " " + info.getGcName() + " (from " + info.getGcCause()+") "+duration + " microseconds; start-end times " + info.getGcInfo().getStartTime()+ "-" + info.getGcInfo().getEndTime());
                        //System.out.println("GcInfo CompositeType: " + info.getGcInfo().getCompositeType());
                        //System.out.println("GcInfo MemoryUsageAfterGc: " + info.getGcInfo().getMemoryUsageAfterGc());
                        //System.out.println("GcInfo MemoryUsageBeforeGc: " + info.getGcInfo().getMemoryUsageBeforeGc());

                        //Get the information about each memory space, and pretty print it
                        Map<String, MemoryUsage> membefore = info.getGcInfo().getMemoryUsageBeforeGc();
                        Map<String, MemoryUsage> mem = info.getGcInfo().getMemoryUsageAfterGc();
                        double totCollection;
                        for (Map.Entry<String, MemoryUsage> entry : mem.entrySet()) {
                            String name = entry.getKey();
                            MemoryUsage memdetail = entry.getValue();
                            long memInit = memdetail.getInit();
                            long memCommitted = memdetail.getCommitted();
                            long memMax = memdetail.getMax();
                            long memUsed = memdetail.getUsed();
                            MemoryUsage before = membefore.get(name);
                            if (memUsed-before.getUsed() != 0) {
                                long beforepercent = ((before.getUsed() * 1000L) / before.getCommitted());
                                long percent = ((memUsed * 1000L) / before.getCommitted()); //>100% when it gets expanded
                                GCEvent gc = new GCEvent(name, startTime + info.getGcInfo().getStartTime(), startTime + info.getGcInfo().getEndTime(), info.getGcName(), info.getGcAction(), info.getGcCause(), before.getUsed(), memdetail.getUsed());
                                gcEvents.add(gc);

                                //System.out.print(name + (memCommitted==memMax?"(fully expanded)":"(still expandable)") +"used: "+(beforepercent/10)+"."+(beforepercent%10)+"%->"+(percent/10)+"."+(percent%10)+"%("+((memUsed/1048576)+1)+"MB) / ");
                                System.out.println(gc);
                            }

                        }
                        System.out.println();
                        totalGcDuration += info.getGcInfo().getDuration();
                        long percent = totalGcDuration*1000L/info.getGcInfo().getEndTime();
                        System.out.println("GC cumulated overhead "+(percent/10)+"."+(percent%10)+"%");
                    }
                }
            };

            //Add the listener
            emitter.addNotificationListener(listener, null, null);
        }
    }
}
