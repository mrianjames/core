package com.oaktree.core.latency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.logging.Log;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.ITimeScheduler;
import com.oaktree.core.time.JavaTime;
import com.oaktree.core.time.MultiTimeScheduler;
import com.oaktree.core.utils.DoubleStatistics;
import com.oaktree.core.utils.StatisticalSummary;
import com.oaktree.core.utils.Text;

/**
 * A latency recorder that records latencies into memory rather than an external writer
 * User: ij
 * Date: 08/06/11
 * Time: 11:15
 */
public class InMemoryLatencyRecorder extends AbstractComponent implements ILatencyRecorder, Runnable {

	/**
	 * Exception message for erroneous input "type" field.
	 */
	private final static String NO_TYPE_EXCEPTION = "No type specified for latency collection point. You must specify a type";
    /**
     * Map of the statistics objects. There is one statistic object per key. 
     */
    private Map<String,DoubleStatistics> statsMap = new ConcurrentHashMap<String,DoubleStatistics>();
    /**
     * Map of the summary statistical objects. There is one statistic object per key. 
     * Will only be populated if you want accumulated statistics. If you dont then set accumulateSummaryStatistics = false 
     */
    private Map<String,StatisticalSummary> summaryStatsMap = new ConcurrentHashMap<String,StatisticalSummary>();
    /**
     * The time stamps for "begin" elements, stored per key. This collection may grow over time if begins are inserted
     * that don't have an end - such occurances are client programming errors rather than latency recorder implementation
     * bugs. 
     */
    private Map<String,Long> beginStamps = new ConcurrentHashMap<String,Long>(100);
    
    /**
     * Should we, as well as percentile information maintain basic statistics such as count, min, max, mean, stdev?
     * This is useful when flushing every time period (as is the norm) and you want overall values.
     */
    private boolean accumulateSummaryStatistics = true;
    /**
     * Abstract concept of time allowing time-stamping to occur using different time sources.
     * This is useful for high precision native timers to be used rather than variable Java implementations.
     */
    private ITime time = new JavaTime();
    /**
     * Scheduler for scheduling the printing task.
     */
    private ITimeScheduler scheduler;
    /**
     * When to print the summary statistics. In milliseconds.
     */
    private long printDuration = 5000;
    /**
     * Its possible to collect stats indefinitely and report on-the-run statistics rather than period by period.
     * When this is false then you must ensure that maxElements is sized for the whole run, and you can support this
     * size of array (there are 2 arrays to consider of maxElements size).
     * Generally not advised.
     */
    private boolean clearOnFlush = false;    
    
    /**
     * Number of elements per category per flush bucket.
     */
    private int maxElements = 10000; 
    /**
     * For printing summary stats.
     */
    private final static Logger logger = LoggerFactory.getLogger(InMemoryLatencyRecorder.class);
    /**
     * flag to turn recording off.
     */
    private volatile boolean active = true;
    /**
     * A summary statistic object we suck the latency information out into for presentation. Its 
     * just a convenience for getting information out in one hit (i.e. one sort of array).
     */
    private StatisticalSummary summary ;
    
    /**
     * counter for printing de-activation every x buckets so we dont spam logs.
     */
    private int deactivationPrintCount = 0;

	/**
	 * print de-activation line every x buckets.
	 */
    private int deactivationPrintEvery = 10; 
    
    public InMemoryLatencyRecorder() {
        scheduler = new MultiTimeScheduler();
        scheduler.initialise();
        scheduler.start();
        summary= new StatisticalSummary();
    }
    public InMemoryLatencyRecorder(int printDuration) {
        this();
        this.printDuration = printDuration;
    }
    public InMemoryLatencyRecorder(ITimeScheduler scheduler) {
        this.scheduler = scheduler;
    }
    public InMemoryLatencyRecorder(ITimeScheduler scheduler, int printDuration) {
        this.printDuration = printDuration;
    }

    public void setScheduler(ITimeScheduler scheduler) {
        this.scheduler = scheduler;
    }

    private String makeKey(String id,long subId) {
        //return id + Text.PERIOD + subId;
        return id;
    }
    
    /**
     * Make a key we store latency information against. Note we support concatenating keys. However
     * this is not ideal - much preferred to pass in single well defined "type" as a key.
     * @param type
     * @param subtype
     * @return
     */
    private String makeType(String type, String subtype) {
    	if (type == null) {
			throw new IllegalArgumentException(NO_TYPE_EXCEPTION);
		}
		
    	if (subtype == null) {
    		return type;
    	}
        return type + Text.PERIOD + subtype;
    }

    @Override
    public void begin(String type, String subtype, String id, long subid) {
        if (active) {
            beginStamps.put(makeKey(id,subid),time.getNanoTime());
        }
    }

    @Override
    public void beginAt(String type, String subtype, String id, long subid, long time) {
        if (active) {
            beginStamps.put(makeKey(id,subid),time);
        }
    }

    @Override
    public void end(String type, String subtype, String id, long subId) {
        if (active) {
            endAt(type,subtype,id,subId,time.getNanoTime());
        }
    }

    @Override
    public void endAt(String type, String subtype, String id, long subId, long time) {
        if (!active) {
             return;
        }
        String t = makeType(type,subtype);
        DoubleStatistics stats = statsMap.get(t);
        if (stats == null) {
             synchronized (t.intern()) {
                stats = statsMap.get(t);
                if (stats == null) {
                    stats = new DoubleStatistics(maxElements);
                    statsMap.put(t,stats);
                    if (accumulateSummaryStatistics) {
                    	summaryStatsMap.put(t,new StatisticalSummary());
                    }
                    if (logger.isInfoEnabled()) {
                    	logger.info("Created type " + type + " of size " + maxElements);
                    }
                }
             }
        }
        //get begin stamp...if not exist then its odd;ignore.
        String key = makeKey(id,subId);
        Long start = beginStamps.remove(key);
        if (start != null) {
            double duration =  (time - start)/1000d;
            try {
                stats.addValue(duration);
            } catch (Exception e) {
                logger.warn("Capacity limit hit on type " + type + " (" + stats.getCount() + "). Deactivating");
                this.setActive(false);
            }
        } else {
            logger.warn("End without begin on key " + key + " on type " + type);
        }
    }
    
    public int getNumPendingEvents() {
        return this.beginStamps.size();
    }

    @Override
    public void setWriter(ILatencyWriter sw) {
    }

    public void setMaxElementsPerFlush(int maxElements) {
        this.maxElements = maxElements;
    }

    public void setClearOnFlush(boolean clearOnFlush) {
        this.clearOnFlush = clearOnFlush;
    }

    public void setPrintDuration(long printDuration) {
        this.printDuration = printDuration;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * Change if we are active.
     * @param active
     */
    public void setActive(boolean active) {
    	boolean previous = this.active;
        this.active = active;
        if (active) {
        	deactivationPrintCount = 0;
        } else {
        	if (previous) {
        		logger.info("Deactivation...clearing statistics and latency points");
	        	//deactivation. Clear all current statistics.
	        	for (Map.Entry<String,DoubleStatistics> e:this.statsMap.entrySet()) {
	        		e.getValue().clear();
	        	}
	        	this.beginStamps.clear();
        	}
        }
    }

    @Override
    public void start() {
        this.setState(ComponentState.AVAILABLE);
        scheduler.schedule(getName(),printDuration,printDuration,this);
    }

    
    private final static String COUNT = "[us] B[#:";
    private final static String MIN = " Mn:";
    private final static String MAX = " Mx:";
    private final static String MED = " 50:";
    private final static String NINETY = " 90:";
    private final static String NINETY_FIVE = " 95:";
    private final static String NINETY_NINE = " 99:";
    private final static String STD = " StdD: ";
    private final static String BUCKET_END = "]";
    private final static String SUMMARY_HEADER = " S[";
    private final static String SUMM_COUNT = "] T#:";
    private final static String SUMM_AVG = " Av:";
    private final static String SUMM_MIN_A = " TMn[";
    private final static String SUMM_MIN_B = "]:";
    private final static String SUMM_MAX_A = " TMx[";
    private final static String SUMM_MAX_B = "]:";
    private final static String SUMM_STD = " TStdD:";
    private final static String SUMM_END = "]";
    @Override
    public void run() {
    	if (active) {
    		for (Map.Entry<String,DoubleStatistics> e:this.statsMap.entrySet()) {
	            DoubleStatistics d = e.getValue();
	            if (d.getCount() > 0) {
	                try {
	                	StringBuilder msg = new StringBuilder(1024);	                	
	                	d.getAllStatistics(summary);
	                	msg.append(e.getKey());
	                	msg.append(COUNT);
	                	msg.append(summary.getCount());
	                	msg.append(MED);
	                	msg.append(summary.getMedian());
	                	msg.append(NINETY);
	                	msg.append(summary.getNinety());
	                	msg.append(NINETY_FIVE);
	                	msg.append(summary.getNinetyFive());
	                	msg.append(NINETY_NINE);
	                	msg.append(summary.getNinetyNine());
	                	msg.append(MIN);
	                	msg.append(summary.getMin());
	                	msg.append(MAX);
	                	msg.append(summary.getMax());
	                	msg.append(STD);
	                	msg.append(Text.to4Dp(summary.getStddev()));
	                	msg.append(BUCKET_END);
	                	if (accumulateSummaryStatistics) {
	            			StatisticalSummary ss = summaryStatsMap.get(e.getKey());
	            			if (ss != null) {
	            				ss.add(summary);
	            				msg.append(SUMMARY_HEADER);
	            				msg.append(ss.getGeneration());
	            				msg.append(SUMM_COUNT);
	            				msg.append(ss.getCount());
	            				msg.append(SUMM_AVG);
	            				msg.append(Text.to4Dp(ss.getMean()));
	            				msg.append(SUMM_MIN_A);
	            				msg.append(ss.getMinGeneration());
	            				msg.append(SUMM_MIN_B);
	            				msg.append(ss.getMin());
	            				msg.append(SUMM_MAX_A);
	            				msg.append(ss.getMaxGeneration());
	            				msg.append(SUMM_MAX_B);
	            				msg.append(ss.getMax());
	            				msg.append(SUMM_END);
	            			}
	        	        }
	        	        
	                    logger.info(msg.toString());
	                } catch (Exception t) {
	                    Log.exception(logger,t);
	                }
	                if (clearOnFlush) {
	                    d.clear();
	                }
	            }
	        }
	        
    	} else {
    		deactivationPrintCount++;
    		if (deactivationPrintCount % deactivationPrintEvery == 0) {
    			logger.info(getName() + " is DEACTIVATED.");
    		}
    	}
    	
    }

    /**
     * Set the time implementation.
     *
     * @param time
     */
    public void setTime(ITime time) {
        this.time = time;
    }

    /**
     * On de-activation print this fact every x number of bucket prints. So we dont spam logs.
     * @param deactivationPrintEvery
     */
  	public void setDeactivationPrintEvery(int deactivationPrintEvery) {
  		this.deactivationPrintEvery = deactivationPrintEvery;
  	}

}
