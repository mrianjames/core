package com.oaktree.core.time;

import com.oaktree.core.utils.ResultTimer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Attempt to find inconsistency between nanotime calls between cores
 * In theory we should find some instances of time jumping backwards.
 *
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 20/07/11
 * Time: 07:35
 */
public class TimeSynchronizationTest {

    public static void main(String[] args) {
        final AtomicLong previous = new AtomicLong();
//        int threads = 128;
//        for (int i = 0;i < threads;i++) {
//            final int x = i;
//            Thread t = new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//                    while (true) {
//                        long start = System.nanoTime();
//                        long end = System.nanoTime();
//                        long diff = end-start;
//                        if (diff < 0) {
//                            System.out.println("Thread " + x + " has found backwardisation: " + diff + " nanos");
//                        }
//                    }
//                }
//            });
//            t.start();
//        }

        //test timing between threads and therefore hopefully cores.
        int threads = 128;
        for (int i = 0;i < threads;i++) {
            final int x = i;
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        long start = previous.get();
                        long end = System.nanoTime();
                        long diff = end-start;
                        if (diff < 0) {
                            System.out.println("Thread " + x + " has found backwardisation: " + diff + " nanos");
                        }
                        previous.set(end);
                    }
                }
            });
            t.start();
        }

    }
}
