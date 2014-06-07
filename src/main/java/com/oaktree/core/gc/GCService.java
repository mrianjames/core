package com.oaktree.core.gc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import com.oaktree.core.collection.multimap.IMultiMap;
import com.oaktree.core.collection.multimap.MultiMap;
import com.oaktree.core.pool.IObjectFactory;
import com.oaktree.core.pool.IPool;
import com.oaktree.core.pool.SimplePool;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.ITimeScheduler;
import com.oaktree.core.time.JavaTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.container.ComponentType;
import com.sun.management.GarbageCollectionNotificationInfo;

/**
 * This class receives GC update events (JDK 7 onwards) and stores them
 * for later use by other components. It will also provide summary analysis of what
 * gc has occured
 *
 * GC Throughput
 *
 * -verbose:gc -Xloggc:gc.log -XX:+PrintGCDetails
 *
 * -XX:+UseParNewGC
 * -XX:+UseConcMarkSweepGC
 *
 * -XX:+UseG1GC
 *
 * @author ij
 *
 */
public class GCService extends AbstractComponent implements IGCService,Runnable {
	
	private final static Logger logger = LoggerFactory.getLogger(GCService.class);
	protected static final String YOUNG = "Young Gen GC";
	protected static final String OLD =  "Old Gen GC";
	protected static final String G1 =  "G1";
    private final ITimeScheduler scheduler;
    private AtomicLong totalRemovedB = new AtomicLong(0);
    private IObjectFactory<GCSnapshot> snapshotFactory = new IObjectFactory<GCSnapshot>(){
        @Override
        public GCSnapshot make() {
            return new GCSnapshot();
        }
    };
    private IPool<GCSnapshot> pool = new SimplePool<>(10*60*60,snapshotFactory);
    private List<GCSnapshot> snapshots = new CopyOnWriteArrayList<>();
    private List<String> gctypes = new ArrayList<String>();
    //private List<GCEvent> allEvents = new CopyOnWriteArrayList<GCEvent>();
    //private IMultiMap<String,GCEvent> eventsByType = new MultiMap<>(true);
    //private static long startTime = System.currentTimeMillis()*1000;
    //total gc duration in us.
    private AtomicLong cumulativeGCTime = new AtomicLong(0);
    private AtomicLong numGcEvents = new AtomicLong(0);
	
	public GCService(String name,ITimeScheduler scheduler) {
		this.setName(name);
		this.setComponentType(ComponentType.SERVICE);
		this.setComponentSubType("GCService");
        if (scheduler == null) {
            throw new IllegalArgumentException("Invalid scheduler");
        }
        this.scheduler = scheduler;
	}
    public GCService(String name,ITimeScheduler scheduler, long duration) {
        this(name,scheduler);
        this.duration = duration;
    }



    private long duration = 30000;
	@Override
	public void start() {
		super.start();
		this.registerForJmxUpdates();
        scheduler.schedule(getName(),duration,duration,this);
		this.setState(ComponentState.AVAILABLE);
	}

	private ITime time = new JavaTime();
	public void setTime(ITime time) {
		this.time = time;
	}
    private GCSnapshot currentSnapshot;
	private void registerForJmxUpdates() {
		checkJavaVersionSupportsJmxUpdates();
	    super.start();
        //get all the GarbageCollectorMXBeans - there's one for each heap generation
        //so probably two - the old generation and young generation
        List<GarbageCollectorMXBean> gcbeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
        //Install a notifcation handler for each bean
        for (GarbageCollectorMXBean gcbean : gcbeans) {
            //System.out.println(gcbean.getName());
            //G1 Young Generation
            //G1 Old Generation
            //copy (normal gc)
            //MarkSweepCompact
            //ParNew
            //ConcurrentMarkSweep
        	this.addGcType(gcbean.getName());

            NotificationEmitter emitter = (NotificationEmitter) gcbean;
            //use an anonymously generated listener for this example
            // - proper code should really use a named class
            NotificationListener listener = new NotificationListener() {
                //keep a count of the total time spent in GCs
               // long totalGcDuration = 0;

                //implement the notifier callback handler
                @Override
                public void handleNotification(Notification notification, Object handback) {
                	long now = time.getTimeOfDay();
                	try {
	                    //we only handle GARBAGE_COLLECTION_NOTIFICATION notifications here
	                    if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
	                        //get the information associated with this notification
	                        GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());

                            currentSnapshot.onGcEvent(now,info);
                            cumulativeGCTime.addAndGet(info.getGcInfo().getDuration());
                            numGcEvents.incrementAndGet();
	                        //get all the info and pretty print it
//	                        long duration = info.getGcInfo().getDuration();
//	                        String gctype = info.getGcAction();
//	                        if ("end of minor GC".equals(gctype)) {
//	                            gctype = YOUNG;
//	                        } else if ("end of major GC".equals(gctype)) {
//	                            gctype =OLD;
//	                        }
//	                        //System.out.println();
//	                        //System.out.println(gctype + ": - " + info.getGcInfo().getId()+ " " + info.getGcName() + " (from " + info.getGcCause()+") "+duration + " microseconds; start-end times " + info.getGcInfo().getStartTime()+ "-" + info.getGcInfo().getEndTime());
//	                        //System.out.println("GcInfo CompositeType: " + info.getGcInfo().getCompositeType());
//	                        //System.out.println("GcInfo MemoryUsageAfterGc: " + info.getGcInfo().getMemoryUsageAfterGc());
//	                        //System.out.println("GcInfo MemoryUsageBeforeGc: " + info.getGcInfo().getMemoryUsageBeforeGc());
//
//	                        //Get the information about each memory space, and pretty print it
//	                        Map<String, MemoryUsage> membefore = info.getGcInfo().getMemoryUsageBeforeGc();
//	                        Map<String, MemoryUsage> mem = info.getGcInfo().getMemoryUsageAfterGc();
//	                        double totCollection;
//	                        GCEvent gc = new GCEvent( now,startTime + info.getGcInfo().getStartTime(), startTime + info.getGcInfo().getEndTime(),gctype, info.getGcName(), info.getGcAction(), info.getGcCause());
//
//
//                            //System.out.print(name + (memCommitted==memMax?"(fully expanded)":"(still expandable)") +"used: "+(beforepercent/10)+"."+(beforepercent%10)+"%->"+(percent/10)+"."+(percent%10)+"%("+((memUsed/1048576)+1)+"MB) / ");
//
//	                        for (Map.Entry<String, MemoryUsage> entry : mem.entrySet()) {
//	                            String name = entry.getKey();
//	                            MemoryUsage memdetail = entry.getValue();
//	                            long memInit = memdetail.getInit();
//	                            long memCommitted = memdetail.getCommitted();
//	                            long memMax = memdetail.getMax();
//	                            long memUsed = memdetail.getUsed();
//	                            MemoryUsage before = membefore.get(name);
//	                            if (memUsed-before.getUsed() != 0) {
//	                                long beforepercent = ((before.getUsed() * 1000L) / before.getCommitted());
//	                                long percent = ((memUsed * 1000L) / before.getCommitted()); //>100% when it gets expanded
//	                                gc.addMemoryArea(name, gc,before.getCommitted(), memdetail.getCommitted(), before.getUsed(), memdetail.getUsed());
//	                            }
//	                        }
//	                        addGcEvent(gc);
//
//
//	                        //System.out.println();
//	                        totalGcDuration += info.getGcInfo().getDuration();
//	                        if (info.getGcInfo().getEndTime() > 0) {
//		                        long percent = totalGcDuration*1000L/info.getGcInfo().getEndTime();
//		                        //System.out.println("GC cumulated overhead "+(percent/10)+"."+(percent%10)+"%");
//	                        }
	                    }
                	} catch (Throwable t) {
                		t.printStackTrace();
                	}
                }
            };

            //Add the listener
            emitter.addNotificationListener(listener, null, null);
        }
        currentSnapshot = new GCSnapshot(getGCNames());
	}
	private void addGcType(String name) {
		gctypes.add(name);
	}
	public String[] getGCNames() {
		return gctypes.toArray(new String[gctypes.size()]);
	}
//	private void addGcEvent(GCEvent event) {
//		if (event.getRemovedB() == 0) {
//			return;
//		}
//		this.allEvents.add(event);
//        eventsByType.put(event.getName(),event);
//		this.cumulativeGCTime.addAndGet(event.getDuration());
//		long removed = event.getRemovedB();
//		if (removed > 0) {
//			this.totalRemovedB.addAndGet(removed);
//		}
//
//		if (logger.isInfoEnabled()) {
//			logger.info("GCEvent: "+event.toString());
//		}
//
//	}
	private void checkJavaVersionSupportsJmxUpdates() {
		String specv = ManagementFactory.getRuntimeMXBean().getSpecVersion();
		int jv = Integer.valueOf(specv.substring(specv.indexOf(".")+1));
		if (jv < 7) {
			logger.warn("NO GC INFORMATION WILL BE AVAILABLE DUE TO LOW JAVA VERSION. UPGRADE to Java 1.7");
		}
	}
	
//	public static void main(String[] args) {
//		GCService gcs = new GCService("test");
//		gcs.initialise();gcs.start();
//		int i = 0;
//        while (true) {
//            logger.info("Making some stuff for gc " + i);
//            i++;
//            LockSupport.parkNanos(1000);
//        }
//	}

//	@Override
//	public GCEvent[] getAllGCEvents() {
//		return this.allEvents.toArray(new GCEvent[allEvents.size()]);
//	}
//
//	@Override
//	public GCEvent[] getAllGCEvents(String type) {
//		Collection<GCEvent> x = new ArrayList<GCEvent>();
//        x = this.eventsByType.get(type);
//        if (x == null) {
//            return new GCEvent[]{};
//        }
//        return x.toArray(new GCEvent[x.size()]);
//	}
//
//	@Override
//	public String[] getGCEventTypes() {
//		return this.gctypes.toArray(new String[gctypes.size()]);
//	}
//
//	@Override
//	public GCEvent[] getAllGCEventsBetween(long start, long end) {
//        return getGCEventsBetween(getAllGCEvents(),start,end);
//	}
//
//    private GCEvent[] getGCEventsBetween(GCEvent[] allEvents, long start, long end) {
//        if (end < start) {
//            return new GCEvent[]{};
//        }
//        List<GCEvent> events =  new ArrayList<>(100);
//        for (GCEvent e:allEvents) {
//            if (e.getStartTime() > start && e.getEndTime() < end) {
//                events.add(e);
//            }
//        }
//        return events.toArray(new GCEvent[events.size()]);
//    }
//
//    @Override
//	public GCEvent[] getAllGCEventsBetween(String type, long start,
//			long end) {
//		GCEvent[] x = getAllGCEvents(type);
//        return getGCEventsBetween(x,start,end);
//	}
	@Override
	public long getTotalGCTimeMs() {
		return cumulativeGCTime.get();
	}
	@Override
	public double getTotalRemovedB() {
		return totalRemovedB.get();
	}
	@Override
	public double getTotalRemovedK() {
		return (double)getTotalRemovedB()/1024d;
	}
	@Override
	public double getTotalRemovedM() {
		return (double)getTotalRemovedK()/1024d;
	}

    @Override
    public GCSnapshot[] getAllSnapshots() {
        return snapshots.toArray(new GCSnapshot[snapshots.size()]);
    }

    @Override
    public GCSnapshot[] getSnapshotsBetween(long start, long end) {
        return new GCSnapshot[0];
    }

    @Override
    public long getNumEvents() {
        return numGcEvents.get();
    }

    @Override
    public void run() {
        if (currentSnapshot.getNumGcEvents() > 0) {
            logger.info("GCService: " + currentSnapshot);
        }
        long removed = currentSnapshot.getTotalRemovedBytes();
        totalRemovedB.addAndGet(removed);
        snapshots.add(getSnapshotObject(currentSnapshot));
        currentSnapshot.clear();
    }

    private GCSnapshot getSnapshotObject(GCSnapshot existing) {
        GCSnapshot snap;
        if (pool == null) {
            snap = new GCSnapshot();
        } else {
            snap = pool.get();
        }
        snap.set(existing);
        return snap;
    }


}
