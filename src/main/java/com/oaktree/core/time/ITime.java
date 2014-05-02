package com.oaktree.core.time;

import java.util.concurrent.TimeUnit;

/**
 * Abstraction for time snapping allowing for native implementations to be
 * used to access different clocks.
 *
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 28/07/11
 * Time: 08:33
 */
public interface ITime {

    /**
     * Get a number that represents getting the time of day since epoch 1970 in ms (System.currentMilliseconds)
     * @return time of day (ms)
     */
    public long getTimeOfDay();

    /**
     * Get the resolution the time of day method returns.
     * @return time of day resolution
     */
    public TimeUnit getTimeOfDayResolution();

    /**
     * Get the fine grained "time" for measurement
     * @return time in nanos.
     */
    public long getNanoTime();

    /**
     * Get the time in micros. Will depend on implementation as to if this value is comparable
     * over processes/machines (e.g. java nanotime /1000 will not be comparable)
     * @return  time in micros
     */
    public long getMicroTime();

    /**
     * Get what resolution the nano is returned in
     * @return timeunit nano is returned in.
     */
    public TimeUnit getNanoResolution();
}
