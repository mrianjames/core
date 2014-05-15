package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sequence that keeps a total count of the incoming events before passing on to next sequence.
 *
 * Created by ianjames on 13/05/2014.
 */
public class CompositeCounterSequence<I extends IData<T>,O extends IData<T>,T> extends DataSequence<I,O> {

    public CompositeCounterSequence(String name) {
        super(name);
    }

    /**
     * Where we keep the counters, keyed on the key of the data object, whatever type that is.
     */
    private AtomicLong counter = new AtomicLong(0);

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
        counter.getAndIncrement();
        return (O)data;
    }

    /**
     * Get the count for a key.
     * @return
     */
    public long getCount() {
        return counter.get();
    }

    @Override
    public String toString() {
        return "CompositeCounterSequence["+getName()+"]";
    }

}
