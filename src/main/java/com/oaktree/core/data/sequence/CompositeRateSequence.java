package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;
import com.oaktree.core.data.IDataReceiver;
import com.oaktree.core.time.ITimeScheduler;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * Sequence that tracks overall (all key) incoming rates.
 * The output type is a DataRate object.
 * This sequence will not passthrough what came in.
 *
 * Created by ianjames on 14/05/2014.
 */
public class CompositeRateSequence<I extends IData<T>,O extends DataRate,T> extends DataSequence<I,DataRate> implements Runnable {
    private final ITimeScheduler scheduler;
    private final long bucket;
    private final long duration;
    private final boolean logRate;
    private final boolean notify;
    private Future<?> apptmt;

    private AtomicLong counter = new AtomicLong(0);


    /**
     *
     * @param name
     * @param scheduler
     * @param bucketDuration - number of ms between counting periods. normally per second (1000) or per minute (60000)
     * @param log - log the rate when counting?
     * @param notify - notify listener(s) when counting?
     */
    public CompositeRateSequence(String name, ITimeScheduler scheduler, long bucketDuration, boolean log, boolean notify) {
        super(name);
        this.scheduler = scheduler;
        this.bucket = bucketDuration;
        this.duration = bucket;
        this.logRate = log;
        this.notify = notify;
    }

    @Override
    public String toString() {
        return "CompositeRateSequence["+getName()+"] " + bucket +"ms";
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.apptmt = scheduler.schedule(getName(),duration,duration,this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (this.apptmt != null) {
            this.apptmt.cancel(true);
        }
    }

    @Override
    public void run() {
        double rate = calculateRate();
        long now = scheduler.getNow();
        //process the map....
        if (logRate) {
            logger.info(getName() + " dataRate: "+rate);
        }
        if (notify) {
            DataRate rt = new DataRate(DataRate.ALL,rate);
            for (IDataReceiver<DataRate> sink : getReceivers()) {
                sink.onData(rt, this, now);
            }
        }

        counter.set(0);
    }

    private double calculateRate() {
        return ((1000d/(double)bucket)*(double)counter.get());
    }

    protected O process(I data, IComponent from, long receivedTime) {
        super.process(data,from,receivedTime);
        counter.incrementAndGet();
        return null; //dont pass anything on....our scheduled task will do this if needs be.
    }

}
