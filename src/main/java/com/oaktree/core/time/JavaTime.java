package com.oaktree.core.time;

import java.util.concurrent.TimeUnit;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 28/07/11
 * Time: 08:41
 */
public class JavaTime implements ITime {
    @Override
    public long getTimeOfDay() {
        return System.currentTimeMillis();
    }

    @Override
    public TimeUnit getTimeOfDayResolution() {
        return TimeUnit.MILLISECONDS;
    }

    @Override
    public long getNanoTime() {
        //return System.nanoTime();
        return System.currentTimeMillis()*1000000;
    }

    @Override
    public long getMicroTime() {
        return getNanoTime()/1000;
    }

    @Override
    public TimeUnit getNanoResolution() {
        return TimeUnit.NANOSECONDS;
    }
}
