package com.oaktree.core.gc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import com.oaktree.core.pool.IPool;
import com.oaktree.core.pool.SimplePool;

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
 * @author ij
 *
 */
public class GCService extends AbstractComponent implements IGCService {
	
	private final static Logger logger = LoggerFactory.getLogger(GCService.class);
	protected static final String YOUNG = "Young Gen GC";
	protected static final String OLD =  "Old Gen GC";
	protected static final String G1 =  "G1";
	
	public GCService(String name) {
		this.setName(name);
		this.setComponentType(ComponentType.SERVICE);
		this.setComponentSubType("GCService");
	}

    private List<GCEvent> allEvents = new CopyOnWriteArrayList<GCEvent>();
	private long startTime;
	//total gc duration in us.
	private AtomicLong cumulativeGCTime = new AtomicLong(0);
	@Override
	public void start() {
		super.start();
		this.registerForJmxUpdates();
		this.setState(ComponentState.AVAILABLE);
	}
	
	private void registerForJmxUpdates() {
		checkJavaVersionSupportsJmxUpdates();
		this.startTime = System.currentTimeMillis()*1000;
        super.start();
        //get all the GarbageCollectorMXBeans - there's one for each heap generation
        //so probably two - the old generation and young generation
        List<GarbageCollectorMXBean> gcbeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
        //Install a notifcation handler for each bean
        for (GarbageCollectorMXBean gcbean : gcbeans) {
            //System.out.println(gcbean.getName());
        	this.addGcType(gcbean.getName());
            NotificationEmitter emitter = (NotificationEmitter) gcbean;
            //use an anonymously generated listener for this example
            // - proper code should really use a named class
            NotificationListener listener = new NotificationListener() {
                //keep a count of the total time spent in GCs
                long totalGcDuration = 0;

                //implement the notifier callback handler
                @Override
                public void handleNotification(Notification notification, Object handback) {
                	try {
	                    //we only handle GARBAGE_COLLECTION_NOTIFICATION notifications here
	                    if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
	                        //get the information associated with this notification
	                        GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
	                        //get all the info and pretty print it
	                        long duration = info.getGcInfo().getDuration();
	                        String gctype = info.getGcAction();
	                        if ("end of minor GC".equals(gctype)) {
	                            gctype = YOUNG;
	                        } else if ("end of major GC".equals(gctype)) {
	                            gctype =OLD;
	                        }
	                        //System.out.println();
	                        //System.out.println(gctype + ": - " + info.getGcInfo().getId()+ " " + info.getGcName() + " (from " + info.getGcCause()+") "+duration + " microseconds; start-end times " + info.getGcInfo().getStartTime()+ "-" + info.getGcInfo().getEndTime());
	                        //System.out.println("GcInfo CompositeType: " + info.getGcInfo().getCompositeType());
	                        //System.out.println("GcInfo MemoryUsageAfterGc: " + info.getGcInfo().getMemoryUsageAfterGc());
	                        //System.out.println("GcInfo MemoryUsageBeforeGc: " + info.getGcInfo().getMemoryUsageBeforeGc());
	
	                        //Get the information about each memory space, and pretty print it
	                        Map<String, MemoryUsage> membefore = info.getGcInfo().getMemoryUsageBeforeGc();
	                        Map<String, MemoryUsage> mem = info.getGcInfo().getMemoryUsageAfterGc();
	                        double totCollection;
	                        GCEvent gc = new GCEvent( startTime + info.getGcInfo().getStartTime(), startTime + info.getGcInfo().getEndTime(),gctype, info.getGcName(), info.getGcAction(), info.getGcCause());
                            allEvents.add(gc);

                            //System.out.print(name + (memCommitted==memMax?"(fully expanded)":"(still expandable)") +"used: "+(beforepercent/10)+"."+(beforepercent%10)+"%->"+(percent/10)+"."+(percent%10)+"%("+((memUsed/1048576)+1)+"MB) / ");
                            
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
	                                gc.addMemoryArea(name, gc,before.getCommitted(), memdetail.getCommitted(), before.getUsed(), memdetail.getUsed());    
	                            }	                            
	                        }
	                        addGcEvent(gc);
	                        
                            
	                        //System.out.println();
	                        totalGcDuration += info.getGcInfo().getDuration();
	                        if (info.getGcInfo().getEndTime() > 0) {
		                        long percent = totalGcDuration*1000L/info.getGcInfo().getEndTime();
		                        //System.out.println("GC cumulated overhead "+(percent/10)+"."+(percent%10)+"%");
	                        }
	                    }
                	} catch (Throwable t) {
                		t.printStackTrace();
                	}
                }
            };

            //Add the listener
            emitter.addNotificationListener(listener, null, null);
        }
	}
	private List<String> gctypes = new ArrayList<String>();
	private void addGcType(String name) {
		gctypes.add(name);
	}
	public String[] getGCNames() {
		return gctypes.toArray(new String[gctypes.size()]);
	}
	private void addGcEvent(GCEvent event) {
		this.allEvents.add(event);
		this.cumulativeGCTime.addAndGet(event.getDuration());
		if (logger.isInfoEnabled()) {
			logger.info(event.toString());
		}
	}
	private void checkJavaVersionSupportsJmxUpdates() {
		String specv = ManagementFactory.getRuntimeMXBean().getSpecVersion();
		int jv = Integer.valueOf(specv.substring(specv.indexOf(".")+1));
		if (jv < 7) {
			logger.warn("NO GC INFORMATION WILL BE AVAILABLE DUE TO LOW JAVA VERSION. UPGRADE to Java 1.7");
		}
	}
	
	public static void main(String[] args) {
		GCService gcs = new GCService("test");
		gcs.initialise();gcs.start();
		int i = 0;
        while (true) {
            logger.info("Making some stuff for gc " + i);
            i++;
            LockSupport.parkNanos(1000);
        }
	}

	@Override
	public GCEvent[] getAllGCEvents() {
		return this.allEvents.toArray(new GCEvent[allEvents.size()]);
	}

	@Override
	public GCEvent[] getAllGCEvents(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getGCEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GCEvent[] getAllGCEventsBetween(long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GCEvent[] getAllGCEventsBetween(String type, long start,
			long end) {
		// TODO Auto-generated method stub
		return null;
	}

}
