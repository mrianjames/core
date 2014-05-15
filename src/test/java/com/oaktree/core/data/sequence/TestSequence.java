package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;
import com.oaktree.core.data.IDataReceiver;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.ITimeScheduler;
import com.oaktree.core.time.JavaTime;
import com.oaktree.core.time.MultiTimeScheduler;
import com.oaktree.core.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;
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
        public int hashCode() {
            return key.hashCode()+(int)value;
        }
        public boolean equals(Object x) {
            if (x instanceof MockDataObject) {
                return ((MockDataObject) x).getDataKey().equals(key) && MathUtils.areEquals(((MockDataObject) x).getValue(), value);
            }
            return false;
        }
    }



    public static void main(String[] args) throws Exception {

        //test a source -> throttle -> log
        //              -> log
        ThroughputDispatcher dispatch = new ThroughputDispatcher("test.dispatcher",1); dispatch.start();
        final String key = "test.key";
        final long duration = 1;
        ITime time = new JavaTime();
        ITimeScheduler scheduler = new MultiTimeScheduler(dispatch);

        scheduler.initialise();scheduler.start();
        int reps = 3000;

        List<String> keyfilterList = new ArrayList<String>();
        keyfilterList.add(key);

        List<MockDataObject> datafilterList = new ArrayList<MockDataObject>();
        datafilterList.add(new MockDataObject(key,15));

        final DataSequence<MockDataObject,MockDataObject> producer = new DataSequence<MockDataObject,MockDataObject>("producer");
        //final BlackKeyFilterSequence<MockDataObject,String> filter = new BlackKeyFilterSequence<>("filter",filterList);
        final WhiteKeyFilterSequence<MockDataObject,String> keyfilter = new WhiteKeyFilterSequence<>("kfilter",keyfilterList);
        //final WhiteDataFilterSequence<MockDataObject> datafilter = new WhiteDataFilterSequence<>("dfilter",datafilterList);
        final BlackDataFilterSequence<MockDataObject> datafilter = new BlackDataFilterSequence<>("dfilter",datafilterList);
        final ThrottlingSequence<MockDataObject,MockDataObject> throttle = new ThrottlingSequence<>("throttle",250, TimeUnit.MILLISECONDS,time);
        final LoggingSequence<MockDataObject,MockDataObject> loggera = new LoggingSequence<>("loggera");
        final LoggingSequence<MockDataObject,MockDataObject> loggerb = new LoggingSequence<>("loggerb");
        final DispatchSequence<MockDataObject,MockDataObject> dispatcher = new DispatchSequence<>("dispatcher",dispatch);
        final CounterSequence<MockDataObject,MockDataObject,String> countera = new CounterSequence<>("countera");
        final CounterSequence<MockDataObject,MockDataObject,String> counterb = new CounterSequence<>("counterb");
        final CompositeRateSequence<MockDataObject,DataRate,String> crate = new CompositeRateSequence<>("crate",scheduler,1000,false,true);
        crate.boot(); //input is our data, output is a datarate object.
        final CompositeRateSequence<MockDataObject,DataRate,String> drate = new CompositeRateSequence<>("drate",scheduler,1000,false,true);
        drate.boot();
        final RateSequence<MockDataObject,DataRate,String> rates = new RateSequence<>("rates",scheduler,1000,true,true);
        rates.boot();
        final LoggingSequence<DataRate,DataRate> rateloggera = new LoggingSequence<DataRate,DataRate>("rtlogger");

        //wiring. producer -> key filter -> data filter ->throttle->counter ->logger (data)
        //                                                                  ->rate
        //                                              ->dispatch->counter ->logger (data)
        //                                                                  ->rate (rate)-> logger

        producer.addDataReceiver(keyfilter);
        keyfilter.addDataReceiver(datafilter);
        datafilter.addDataReceiver(throttle);
        datafilter.addDataReceiver(dispatcher);
        throttle.addDataReceiver(countera);
        dispatcher.addDataReceiver(counterb);
        countera.addDataReceiver(loggera);
        countera.addDataReceiver(crate);
        counterb.addDataReceiver(loggerb);
        counterb.addDataReceiver(rates);
        counterb.addDataReceiver(drate);
        crate.addDataReceiver(rateloggera);
        //rates.addDataReceiver(rateloggera);



        //pump some data into it. stand back and watch it flow.

        final AtomicInteger ai = new AtomicInteger(0);
        scheduler.scheduleUntilReps(key,duration,duration,reps,new Runnable(){
            public void run() {
                int x = ai.getAndIncrement();
                long time = System.currentTimeMillis();
                producer.onData(new MockDataObject(key,x),producer,time);
            }
        });
        Thread.sleep(reps*duration);
        System.out.println("A(Throttled): "+countera.getCount(key));
        System.out.println("B(Dispatched): "+counterb.getCount(key));
        dispatch.stop();
        scheduler.stop();
        System.out.println("Stopped");
        System.out.println(SequenceUtils.print(producer));
    }
}
