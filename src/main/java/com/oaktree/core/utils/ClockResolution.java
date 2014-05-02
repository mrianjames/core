package com.oaktree.core.utils;

import com.oaktree.core.time.ITime;
import com.oaktree.core.time.JavaTime;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;


public class ClockResolution extends TimerTask {

    private ITime time = new JavaTime();

    public void setTime(ITime time) {
        this.time = time;
    }

    static long[] times = new long[]{};
	private static AtomicInteger counter = new AtomicInteger(0);
	private static int loops = 0;
	static Timer timer = new Timer("ResolutionTimer");
	private static CountDownLatch latch;

	/**
	 * Will default test to 1ms, 10 loops.
	 * @param args
	 */
	public static void main(String[] args) {
        ITime time = new JavaTime();

//		Thread t = new Thread(new Runnable(){
//
//			@Override
//			public void run() {
//				 try {
//					Thread.sleep(Integer.MAX_VALUE);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

//			}});
//		//t.setDaemon(true);
//		t.start();

		int res = (args.length > 0) ? Integer.valueOf(args[0]) : 1;
		loops = (args.length > 1) ? Integer.valueOf(args[1]) : 0;
		if (loops <= 1) {
			loops = 100;
		}

		System.out.println("Starting timer clock resolution test..scheduling tasks at " + res + " ms resolution. " + loops + " loops.");

		times = new long[loops];
		latch = new CountDownLatch(loops);
		TimerTask task = new ClockResolution();
		timer.schedule(task, res, res);
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
		
		timer.cancel();
		int intervals = loops-1;
		long dur[] = new long[intervals];

		/*
		 * analyse
		 */
		for (int i = 0; i < loops; i++) {
			long x = times[i];
			long y = i+1<loops ? times[i+1] : -1;
			if (y > 0) {
				long diff = y-x;
				dur[i] = diff;
			}
		}
		Arrays.sort(dur);
		long med = dur[(((intervals/100)*50)+1)];
		long ninety = dur[(((intervals/100)*90)+1)];
		long ninetynine = dur[(((intervals/100)*99)+1)];

		double m = ((double)(med))/1000000;
		double n = ((double)(ninety))/1000000;
		double nn = ((double)(ninetynine))/1000000;

		DecimalFormat format = new DecimalFormat("#,###.##");
		//System.out.println("Timer Duration: " + format.format(d) + "ms");
		System.out.println("Timer Median: " + format.format(m) + "ms");
		System.out.println("Timer 90%: " + format.format(n) + "ms");
		System.out.println("Timer 99%: " + format.format(nn) + "ms");
		System.out.println("Wobble90: " + format.format((-(res-n))) + " ms");
		System.out.println("Timer Clock resolution test complete. Starting wait test");

		Object o = new Object();
		long[] starts = new long[loops];
		long[] ends = new long[loops];
		synchronized (o) {
		for (int i = 0; i < loops; i++) {
			try {
				starts[i] = time.getNanoTime();
				o.wait(res);
				//Thread.sleep(res);
				ends[i] = time.getNanoTime();
			} catch (InterruptedException e) {
			}
		}
		}
		long[] durations = new long[loops];
		for (int i = 0; i < loops; i++) {
			durations[i] = (ends[i]-starts[i]);
		}
		Arrays.sort(durations);
		med = durations[(((intervals/100)*50)+1)];
		ninety = durations[(((intervals/100)*90)+1)];
		ninetynine = durations[(((intervals/100)*99)+1)];
		m = ((double)(med))/1000000;
		n = ((double)(ninety))/1000000;
		nn = ((double)(ninetynine))/1000000;

		System.out.println("Wait Median: " + format.format(m) + "ms");
		System.out.println("Wait 90%: " + format.format(n) + "ms");
		System.out.println("Wait 99%: " + format.format(nn) + "ms");

		System.out.println("Testing thread sleep...");
		for (int i = 0; i < loops; i++) {
			try {
				starts[i] = time.getNanoTime();
				Thread.sleep(res);
				ends[i] = time.getNanoTime();
			} catch (InterruptedException e) {
			}
		}
		durations = new long[loops];
		for (int i = 0; i < loops; i++) {
			durations[i] = (ends[i]-starts[i]);
		}
		Arrays.sort(durations);
		med = durations[(((intervals/100)*50)+1)];
		ninety = durations[(((intervals/100)*90)+1)];
		ninetynine = durations[(((intervals/100)*99)+1)];
		m = ((double)(med))/1000000;
		n = ((double)(ninety))/1000000;
		nn = ((double)(ninetynine))/1000000;

		System.out.println("Sleep Median: " + format.format(m) + "ms");
		System.out.println("Sleep 90%: " + format.format(n) + "ms");
		System.out.println("Sleep 99%: " + format.format(nn) + "ms");

		System.out.println("Testing Concurrent scheduler...");
		counter = new AtomicInteger(0);
		ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
		stpe.prestartAllCoreThreads();
		stpe.scheduleWithFixedDelay(task, res, res, TimeUnit.MILLISECONDS);
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
		
		stpe.shutdown();
		intervals = loops-1;
		dur = new long[intervals];
		/*
		 * analyse
		 */
		for (int i = 0; i < loops; i++) {
			long x = times[i];
			long y = i+1<loops ? times[i+1] : -1;
			if (y > 0) {
				long diff = y-x;
				dur[i] = diff;
			}
		}
		Arrays.sort(dur);
		med = dur[(((intervals/100)*50)+1)];
		ninety = dur[(((intervals/100)*90)+1)];
		ninetynine = dur[(((intervals/100)*99)+1)];

		m = ((double)(med))/1000000;
		n = ((double)(ninety))/1000000;
		nn = ((double)(ninetynine))/1000000;

		//System.out.println("CS Duration: " + format.format(d) + "ms");
		System.out.println("CS Median: " + format.format(m) + "ms");
		System.out.println("CS 90%: " + format.format(n) + "ms");
		System.out.println("CS 99%: " + format.format(nn) + "ms");
		
		
		System.out.println("Testing park sleep..."+res+"ms");
		for (int i = 0; i < loops; i++) {
			starts[i] = time.getNanoTime();
			LockSupport.parkNanos(res*100000);
			ends[i] = time.getNanoTime();
		}
		durations = new long[loops];
		for (int i = 0; i < loops; i++) {
			durations[i] = (ends[i]-starts[i]);
		}
		Arrays.sort(durations);
		med = durations[(((intervals/100)*50)+1)];
		ninety = durations[(((intervals/100)*90)+1)];
		ninetynine = durations[(((intervals/100)*99)+1)];
		m = ((double)(med))/1000000;
		n = ((double)(ninety))/1000000;
		nn = ((double)(ninetynine))/1000000;

		System.out.println("Park Median: " + format.format(m) + "ms");
		System.out.println("Park 90%: " + format.format(n) + "ms");
		System.out.println("Park 99%: " + format.format(nn) + "ms");
		
		System.out.println("Testing park nanos..."+(res)+"us");
		for (int i = 0; i < loops; i++) {
			starts[i] = time.getNanoTime();
			LockSupport.parkNanos(res*1000);
			ends[i] = time.getNanoTime();
		}
		durations = new long[loops];
		for (int i = 0; i < loops; i++) {
			durations[i] = (ends[i]-starts[i]);
		}
		Arrays.sort(durations);
		med = durations[(((intervals/100)*50)+1)];
		ninety = durations[(((intervals/100)*90)+1)];
		ninetynine = durations[(((intervals/100)*99)+1)];
		m = ((double)(med))/1000000;
		n = ((double)(ninety))/1000000;
		nn = ((double)(ninetynine))/1000000;

		System.out.println("Park Median: " + format.format(m) + "us");
		System.out.println("Park 90%: " + format.format(n) + "us");
		System.out.println("Park 99%: " + format.format(nn) + "us");
	}

	public void run() {
		times[counter.get()] = time.getNanoTime();
		counter.incrementAndGet();
		latch.countDown();
	}
}
