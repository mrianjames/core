package com.oaktree.core.threading.dispatcher.Monitoring;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.container.ComponentType;
import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.ITimeScheduler;
import com.oaktree.core.time.JavaTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: IJ
 * Date: 21/02/12
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
public class DispatcherMonitor extends AbstractComponent implements Runnable{

    private final static Logger logger= LoggerFactory.getLogger(DispatcherMonitor.class.getName());
    private IDispatcher dispatcher;
    private ITimeScheduler scheduler;
    private long interval = 10000; //10s default
    private int warningThreshold = 5;
    private Future<?> apptmt;

    public DispatcherMonitor(String name, IDispatcher dispatcher, ITimeScheduler scheduler, long interval) {
        this.setComponentType(ComponentType.SERVICE);
        this.scheduler = scheduler;
        this.setComponentSubType("DispatcherMonitor");
        this.setName(name);
        this.dispatcher = dispatcher;
        this.interval = interval;
    }
    
    @Override
    public void start() {
        super.start();
        if (logger.isInfoEnabled()) {
        	logger.info("Staritng " + getName() + " with interval " + interval + " on dispatcher " + dispatcher.getName());
        }
        this.apptmt = this.scheduler.schedule(getName(), interval, interval, this);
        this.setState(ComponentState.AVAILABLE);
    }

    @Override
    public void run() {
        //gather dispatcher statistics and present in a fashionable way...
        long totalExecCount = dispatcher.getExecutedTaskCount();
        long totalQueuedCount = dispatcher.getQueuedTaskCount();
        long totalQueuedIncrease = totalQueuedCount - prevQueuedCount;
        long totalExecIncrease = totalExecCount - prevExecCount;
        String[] keys = dispatcher.getKeys();
        int keyIncrease = keys.length - prevKeyCount;
        double keyIncreaseRate = (double)keyIncrease / ((double)interval/1000d);
        double queuedIncreaseRate = (double)totalQueuedIncrease / (interval/1000d);
        double execIncreaseRate = (double)totalExecIncrease / (interval/1000d);
        long maxExec = 0;
        long maxQueued = 0;
        String topQueued = null;
        String topExec = null;
        int warn = 0;
        for (String key:keys) {
            long exec = dispatcher.getExecutedTaskCount(key);
            long queued = dispatcher.getQueuedTaskCount(key);
            if (exec > maxExec) {
                topExec = key;
                maxExec = exec;
            }
            if (queued > maxQueued) {
                topQueued = key;
                maxQueued = queued;
            }
            if (queued > warningThreshold) {
                warn+=1;
            }
        }
        if (logger.isInfoEnabled()) {
	        StringBuilder builder = new StringBuilder(500);
	        builder.append("DispatcherStats[");
	        builder.append(dispatcher.getName());
	        builder.append("] Keys:");
	        builder.append(keys.length);
	        builder.append(", ");
	        builder.append(keyIncrease > 0 ? "+" : "");
	        builder.append(keyIncrease);
	        builder.append(" rt: ");
	        builder.append(keyIncreaseRate);
	        builder.append("p/s qd:");
	        builder.append(totalQueuedCount);
	        builder.append(totalQueuedIncrease > 0 ? " +" : "");
	        builder.append(totalQueuedIncrease);
	        builder.append(" rt: ");
	        builder.append(queuedIncreaseRate);
	        builder.append("p/s exec:");
	        builder.append(totalExecCount);
	        builder.append(totalExecIncrease > 0 ? " +" : "");
	        builder.append(totalExecIncrease);
	        builder.append(" rt: ");
	        builder.append(execIncreaseRate);
	        builder.append("p/s topQ: ");
	        builder.append(topQueued);
	        builder.append("[");
	        builder.append(maxQueued);
	        builder.append("]");
	        builder.append(" topEx: ");
	        builder.append(topExec);
	        builder.append("[");
	        builder.append(maxExec);
	        builder.append("]");
	        builder.append(" #>warn: ");
	        builder.append(warn);
	        
	        logger.info(builder.toString());
        }
        prevKeyCount = keys.length;
        prevExecCount = totalExecCount;
        prevQueuedCount = totalQueuedCount;

        if (dispatchListener != null) {
            DispatchSnapshot snapshot = new DispatchSnapshot(dispatcher.getName(),dispatcher.getClass().getName(),timeSource.getTimeOfDay(),totalExecCount,
                    totalQueuedCount,totalQueuedIncrease,totalExecIncrease,keyIncrease,keyIncreaseRate,
                    queuedIncreaseRate,execIncreaseRate,
                    topExec,topQueued,keys.length);
            dispatchListener.onDispatchStatistics(this.dispatcher,snapshot);

        }
    }
    private IDispatcherListener dispatchListener;
    public void setDispatchListener(IDispatcherListener listener) {
    	this.dispatchListener = listener;
    }
    private int prevKeyCount = 0;
    private long prevQueuedCount = 0;
    private long prevExecCount = 0;
    private ITime timeSource = new JavaTime();

    public void stop() {
        this.setState(ComponentState.STOPPING);
        if (apptmt != null) {
            logger.info("Canceling scheduled task");
            apptmt.cancel(true);
        }
        this.setState(ComponentState.STOPPED);
    }
}
