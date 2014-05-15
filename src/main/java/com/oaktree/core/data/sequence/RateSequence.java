package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;
import com.oaktree.core.data.IDataReceiver;
import com.oaktree.core.time.ITimeScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
public class RateSequence<I extends IData<T>,O extends DataRate,T> extends DataSequence<I,DataRate> implements Runnable {
    private final ITimeScheduler scheduler;
    private final long bucket;
    private final long duration;
    private final boolean logRate;
    private final boolean notify;
    private Future<?> apptmt;


    /**
     * Where we keep the counters, keyed on the key of the data object, whatever type that is.
     */
    private ConcurrentMap<T,AtomicLong> countMap = new ConcurrentHashMap<T,AtomicLong>(100);

    /**
     *
     * @param name
     * @param scheduler
     * @param bucketDuration - number of ms between counting periods. normally per second (1000) or per minute (60000)
     * @param log - log the rate when counting?
     * @param notify - notify listener(s) when counting?
     */
    public RateSequence(String name, ITimeScheduler scheduler, long bucketDuration, boolean log, boolean notify) {
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


    /**
     * Here we override the std processing (logging) and
     * keep counts per key.
     *
     * @param data
     * @param from
     * @param receivedTime
     * @return
     */
    protected O process(I data, IComponent from, long receivedTime) {
        super.process(data,from,receivedTime);
        AtomicLong ai = countMap.get(data.getDataKey());
        if (ai == null) {
            synchronized (data.getDataKey()) {
                AtomicLong al = new AtomicLong(0);
                AtomicLong existing = countMap.putIfAbsent(data.getDataKey(), al);
                if (existing != null) {
                    ai = existing;
                } else {
                    ai = al;
                }
            }
        }
        ai.getAndIncrement();
        //return (O)data;
        return null;
    }

    /**
     * Get the count for a key.
     * @param key
     * @return
     */
    public long getCount(T key) {
        AtomicLong l = countMap.get(key);
        if (l == null) {
            return 0;
        }
        return l.get();
    }

    /**
     * Get a copy of the keys.
     * @return
     */
    public Collection<T> getKeys() {
        ArrayList<T> list = new ArrayList<T>(countMap.keySet());
        return list;
    }

    @Override
    public void run() {
        long now = scheduler.getNow();
        DataRate<T> rates = new DataRate<T>();
        for (T key:getKeys()) {
            double rate = calculateRateAndClear(key);

            //process the map....
            if (logRate) {
                logger.info(getName() + " key:"+key+" dataRate: " + rate);
            }
            if (notify) {
                DataRate rt = new DataRate(key, rate);
                rates.addChild(rt);
            }
        }
        for (IDataReceiver<DataRate> sink : getReceivers()) {
            sink.onData(rates, this, now);
        }
    }

    private double calculateRateAndClear(T key) {
        AtomicLong counter = countMap.get(key);
        double rate =  ((1000d/(double)bucket)*(double)counter.get());
        counter.set(0);
        return rate;
    }



}
