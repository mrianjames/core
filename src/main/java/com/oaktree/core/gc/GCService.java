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

import com.oaktree.core.utils.Text;
import com.sun.management.GcInfo;
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
    private long lastGcTime = 0;
    private GarbageCollectionNotificationInfo lastGC;
    public String getLastGcTime() {
        return Text.renderTime(lastGcTime);
    }
    public String getLastGCEvent() {
        if (lastGC == null) {
            return Text.EMPTY_STRING;
        }
        StringBuilder b = new StringBuilder(256);
        //b.setLength(0);
        b.append("There was a ");
        b.append(lastGC.getGcCause());
        b.append(" on ");
        b.append(lastGC.getGcName());
        b.append(" caused by ");
        b.append(lastGC.getGcAction());

        return b.toString();
    }

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
                            lastGC = info;
                            lastGcTime = now;

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

	private void checkJavaVersionSupportsJmxUpdates() {
		String specv = ManagementFactory.getRuntimeMXBean().getSpecVersion();
		int jv = Integer.valueOf(specv.substring(specv.indexOf(".")+1));
		if (jv < 7) {
			logger.warn("NO GC INFORMATION WILL BE AVAILABLE DUE TO LOW JAVA VERSION. UPGRADE to Java 1.7");
		}
	}


	@Override
	public long getTotalGCTimeMs() {
		return cumulativeGCTime.get()/1000;
	}

    @Override
    public long getTotalGCTimeUs() {
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
    public GCSnapshot[] getAllSnapshots(String type) {
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
        long removed = currentSnapshot.getTotalRemovedB();
        totalRemovedB.addAndGet(removed);
        if (currentSnapshot.getNumGcEvents() > 0) {
            logger.info("GCService: " + currentSnapshot);
            snapshots.add(getSnapshotObject(currentSnapshot));
        }
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
