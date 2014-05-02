package com.oaktree.core.utils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: IJ
 * Date: 22/02/12
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public class MinimumSleepTest implements  Runnable{
    public final static long duration = 1000;
    static ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(1);
    public static void main(String[] args) throws Exception {


        //ses.schedule(new MinimumSleepTest(),duration,TimeUnit.MICROSECONDS);
        //ses.scheduleWithFixedDelay(new MinimumSleepTest(),duration, duration,TimeUnit.MICROSECONDS);
        ses.scheduleAtFixedRate(new MinimumSleepTest(),duration, duration,TimeUnit.MICROSECONDS);

        
        
//        Unsafe u = Unsafe.getUnsafe();
//        long start,end = 0;
//        System.out.println("Starting...");
//        start = System.nanoTime();
//        u.park(true,1000);
//        end = System.nanoTime();
//        System.out.println("parked 1000: " + (end-start) + "nanos");
        
        
    }
    private long previous = 0;

    @Override
    public void run() {
        long l = System.nanoTime()/1000;
        long diff = (l-previous);
        long wobble = diff-duration;
        System.out.println("Time: " + l + " diff: " +  diff +  " us " + " wobblefactor: " + wobble + " us");
        previous = l;

    }
}
