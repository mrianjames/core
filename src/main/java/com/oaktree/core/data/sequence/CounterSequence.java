package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ianjames on 13/05/2014.
 */
public class CounterSequence<I extends IData<T>,O extends IData<T>,T> extends DataSequence<I,O> {

    public CounterSequence(String name) {
        super(name);
    }

    /**
     * Where we keep the counters, keyed on the key of the data object, whatever type that is.
     */
    private ConcurrentMap<T,AtomicLong> countMap = new ConcurrentHashMap<T,AtomicLong>(100);

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
        return (O)data;
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
    public String toString() {
        return "CounterSequence["+getName()+"]";
    }

//    public static void main(String[] args)
//    {
//
//        //rules
//        // if a string allocated with new from "" then the value of that is pointed at, but its a new object.
//        // if you allocate with "" you are defining a new String implicitly and you point at that.
//        //
//        //
//        //
//
//        //test intern
//        char[] ca = new char[]{'T','E','S','T',' ','S','T','R','I','N','G'};
//        String x1 = new String("TEST STRING"); //string behind this is not this object...
//        String x2 = "TEST STRING"; //string behind this is the object that all get.
//        String ptr1 = x1; //as youd expect this object ref is x1 ref. however its not the intern...
//        String x3 = new String(ca);
//
//        System.out.println("x1: "+System.identityHashCode(x1)+" int: "+System.identityHashCode(x1.intern()));
//        System.out.println("x2: "+System.identityHashCode(x2) + " eqx1: "+(x1.equals(x2)) + " int: " + System.identityHashCode(x2.intern()));
//        System.out.println("ptr: "+System.identityHashCode(ptr1) + " int: " + System.identityHashCode(ptr1.intern()));
//        System.out.println("ca: "+System.identityHashCode(x3) + " int: " + System.identityHashCode(x3.intern()));
//    }
}
