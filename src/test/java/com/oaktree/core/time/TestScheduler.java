package com.oaktree.core.time;

import com.oaktree.core.time.MultiTimeScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;

import com.oaktree.core.utils.Text;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.format.annotation.DateTimeFormat;

public class TestScheduler {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetNow() {

        MultiTimeScheduler scheduler = new MultiTimeScheduler();
        //scheduler.setStartTime(l);
        scheduler.initialise();
        scheduler.start();
        scheduler.setHypertime(1);
        long midnight =  Text.getToday();
        long now = scheduler.getNow();
        long time = scheduler.getTime();
        Assert.assertEquals(now-midnight,time,10); //rough time
//        System.out.println("Now:" + Text.renderTime(now).substring(0,10) + ", time: " + Text.toTime(time).substring(0,10));
//        Assert.assertEquals(Text.renderTime(now).substring(0,10),Text.toTime(time).substring(0,10));
    }

    @Test
    public void testScheduleUntilReps() {
        MultiTimeScheduler scheduler = new MultiTimeScheduler();
        scheduler.initialise();
        scheduler.start();
        scheduler.setHypertime(1);
        final CountDownLatch latch = new CountDownLatch(3);
        final List<Long> run = new ArrayList<Long>();
        Runnable task = new Runnable() {

            @Override
            public void run() {
                long t = System.currentTimeMillis();
                System.out.println("Executed: " + Text.renderTime(t));
                run.add(t);
                //latch.countDown();
            }
        };
        scheduler.scheduleUntilReps("A",1000,100,3,task);
        try {Thread.sleep(3000);} catch (Exception e) {}
        Assert.assertEquals(3,run.size());
    }

    @Test
    public void testScheduleUntilTime() {
        MultiTimeScheduler scheduler = new MultiTimeScheduler();
        scheduler.initialise();
        scheduler.start();
        scheduler.setHypertime(1);
        final CountDownLatch latch = new CountDownLatch(3);
        final List<Long> run = new ArrayList<Long>();
        Runnable task = new Runnable() {

            @Override
            public void run() {
                long t = System.currentTimeMillis();
                System.out.println("Executed: " + Text.renderTime(t));
                run.add(t);
                //latch.countDown();
            }
        };
        long now = System.currentTimeMillis();
        scheduler.scheduleUntilTime("A",100,100,2000,task);
        try {Thread.sleep(3000);} catch (Exception e) {}
        Assert.assertEquals(19,run.size());
    }

    @Test
    public void testBasics() {
        this.testBasic(1);
        this.testBasic(3);
        /*
         * slow down time.
         */
        this.testBasic(0.3);
    }
    private final static double EPSILON = 0.0000001;

    @Test
    public void testPause() {
        double hyper = 1;
        long time = 1000;

        MultiTimeScheduler scheduler = new MultiTimeScheduler();
        scheduler.initialise();
        scheduler.start();
        scheduler.setHypertime(hyper);
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Long> run = new ArrayList<Long>();
        Runnable task = new Runnable() {

            @Override
            public void run() {
                long t = System.currentTimeMillis();
                System.out.println("Executed: " + Text.renderTime(t));
                run.add(t);
                latch.countDown();
            }
        };
        System.out.println("Sent: " + Text.renderTime(System.currentTimeMillis()));
        scheduler.pause();
        Assert.assertTrue(scheduler.isPaused());
        scheduler.schedule("X",time, task);
        try {
            latch.await((time + 1000), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        Assert.assertEquals(run.size(), 0, EPSILON);
        scheduler.resume(true);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Assert.assertTrue(run.size() > 0);
        System.out.println("Received: " + Text.renderTime(run.get(0)));
        Assert.assertFalse(scheduler.isPaused());

    }

    @Test
    public void testClickPlayMissed() {
        double hyper = 1;
        long time = 1000;

        MultiTimeScheduler scheduler = new MultiTimeScheduler();
        scheduler.initialise();
        scheduler.start();
        scheduler.setHypertime(hyper);
        scheduler.setClickMode(true);
        Assert.assertEquals(scheduler.isClickMode(), true);
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Long> run = new ArrayList<Long>();
        Runnable task = new Runnable() {

            @Override
            public void run() {
                long t = System.currentTimeMillis();
                System.out.println("Executed: " + Text.renderTime(t));
                run.add(t);
                latch.countDown();
            }
        };
        System.out.println("Sent: " + Text.renderTime(System.currentTimeMillis()));
        scheduler.schedule("X",time, task);
        try {
            latch.await((time + 1000), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        Assert.assertEquals(run.size(), 0, EPSILON);
        try {scheduler.next(true,false); } catch (Exception e){}
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Assert.assertTrue(run.size() > 0);
        System.out.println("Received: " + Text.renderTime(run.get(0)));

        scheduler.setClickMode(true);
        Assert.assertEquals(scheduler.isClickMode(), true);
    }

    @Test
    public void testClickNoPlay() {

        double hyper = 1;
        long time = 1000;

        MultiTimeScheduler scheduler = new MultiTimeScheduler();
        scheduler.initialise();
        scheduler.start();
        scheduler.setHypertime(hyper);
        scheduler.setClickMode(true);
        Assert.assertEquals(scheduler.isClickMode(), true);
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Long> run = new ArrayList<Long>();
        Runnable task = new Runnable() {

            @Override
            public void run() {
                long t = System.currentTimeMillis();
                System.out.println("Executed: " + Text.renderTime(t));
                run.add(t);
                latch.countDown();
            }
        };
        System.out.println("Sent: " + Text.renderTime(System.currentTimeMillis()));
        scheduler.schedule("X",time, task);
        try {
            latch.await((time + 1000), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        Assert.assertEquals(run.size(), 0, EPSILON);
        try { scheduler.next(false,false); } catch (Exception e){}
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Assert.assertEquals(run.size(), 0, EPSILON);
        //System.out.println("Received: " + Text.renderTime(run.get(0)));

    }

    private void testBasic(double h) {
        double hyper = h;
        long time = 1000;

        MultiTimeScheduler scheduler = new MultiTimeScheduler();
        scheduler.initialise();
        scheduler.start();
        scheduler.setHypertime(hyper);
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Long> run = new ArrayList<Long>();
        Runnable task = new Runnable() {

            @Override
            public void run() {
                run.add(System.currentTimeMillis());
                latch.countDown();
            }
        };
        System.out.println("Sent: " + Text.renderTime(System.currentTimeMillis()));
        scheduler.schedule("X",time, task);
        try {
            latch.await((long) ((time / hyper) + 1000), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        Assert.assertTrue(run.size() > 0);
        System.out.println("Received: " + Text.renderTime(run.get(0)));


    }
}
