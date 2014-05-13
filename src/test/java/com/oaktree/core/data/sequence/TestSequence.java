package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;
import com.oaktree.core.data.IDataReceiver;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.ITimeScheduler;
import com.oaktree.core.time.JavaTime;
import com.oaktree.core.time.MultiTimeScheduler;

import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ianjames on 13/05/2014.
 */
public class TestSequence {

    private static class MockDataObject implements IData<String> {
        private final String key;
        private final double value;
        public String toString() {
            return key + " " + value;
        }
        public MockDataObject(String key, double value) {
            this.key = key;
            this.value = value;
        }
        @Override
        public String getDataKey() {
            return key;
        }
        public double getValue() {
            return value;
        }
    }



    public static void main(String[] args) {

        //test a source -> throttle -> log
        //              -> log
        ThroughputDispatcher dispatch = new ThroughputDispatcher("test.dispatcher",1); dispatch.start();
        final String key = "test.key";
        final long duration = 1;
        ITime time = new JavaTime();

        final DataSequence<MockDataObject,MockDataObject> producer = new DataSequence<MockDataObject,MockDataObject>("producer");
        final ThrottlingSequence<MockDataObject,MockDataObject> throttle = new ThrottlingSequence<>("throttle",250, TimeUnit.MILLISECONDS,time);
        final LoggingSequence<MockDataObject,MockDataObject> loggera = new LoggingSequence<>("loggera");
        final LoggingSequence<MockDataObject,MockDataObject> loggerb = new LoggingSequence<>("loggerb");
        final DispatchSequence<MockDataObject,MockDataObject> dispatcher = new DispatchSequence<>("dispatcher",dispatch);

        producer.addDataReceiver(throttle);
        producer.addDataReceiver(dispatcher);
        throttle.addDataReceiver(loggera);
        dispatcher.addDataReceiver(loggerb);

        //pump some data into it. stand back and watch it flow.
        ITimeScheduler scheduler = new MultiTimeScheduler();
        scheduler.initialise();scheduler.start();
        final AtomicInteger ai = new AtomicInteger(0);
        scheduler.schedule(key,duration,duration,new Runnable(){
            public void run() {
                int x = ai.getAndIncrement();
                long time = System.currentTimeMillis();
                producer.onData(new MockDataObject(key,x),producer,time);
            }
        });

    }
}
