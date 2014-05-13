package com.oaktree.core.data.subscription;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;
import com.oaktree.core.data.IDataProvider;
import com.oaktree.core.data.IDataReceiver;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import junit.framework.Assert;

/**
 * Created by ianjames on 09/05/2014.
 */
public class TestSubscriptionService {

    private MockDataProvider p;
    private ThroughputDispatcher dispatcher;
    private SubscriptionService<MockDataObject> ss;
    private MockReceiver receiver;

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
    private static class MockDataProvider extends AbstractComponent implements IDataProvider<MockDataObject> {
        private IDataReceiver<MockDataObject> receiver;
        @Override
        public void addDataReceiver(IDataReceiver<MockDataObject> receiver) {
            this.receiver = receiver;
        }

        @Override
        public void setDataReceiver(IDataReceiver<MockDataObject> receiver) {
            this.receiver = receiver;
        }

        @Override
        public Collection<IDataReceiver<MockDataObject>> getReceivers() {
            ArrayList<IDataReceiver<MockDataObject>> l = new ArrayList<IDataReceiver<MockDataObject>>();
            l.add(this.receiver);
            return l;
        }

        @Override
        public void removeReceiver(IDataReceiver<MockDataObject> receiver) {

        }

        @Override
        public void registerInterest(Object key, IDataReceiver<MockDataObject> from) {

        }

        @Override
        public void removeInterest(Object key, IDataReceiver<MockDataObject> from) {

        }

        public void update(MockDataObject o) {
            receiver.onData(o,this,System.currentTimeMillis());
        }
    }

    private static class MockReceiver extends AbstractComponent implements IDataReceiver<MockDataObject> {
        private final static Logger logger = LoggerFactory.getLogger(MockReceiver.class);
        List<MockDataObject> datas = new ArrayList<MockDataObject>();
        @Override
        public void onData(MockDataObject data, IComponent from, long receivedTime) {
            //logger.info("INCOMING:"+data.toString());
            //LockSupport.parkNanos(1000); //100us
            datas.add(data);
        }
        public Collection<MockDataObject> getData() {
            return datas;
        }
        public void clear() {
            datas.clear();
        }
    }

    String key = "TEST.KEY";

    @Before
    public void setup(){
        this.p = new MockDataProvider();
        this.dispatcher = new ThroughputDispatcher("D",2);
        dispatcher.start();

        this.ss = new SubscriptionService<MockDataObject>("ss");
        this.ss.setDispatcher(dispatcher);
        ss.addProvider(p);


        this.receiver = new MockReceiver();
        receiver.setName("MOCKREC");
    }

    @After
    public void tearDown() {
        dispatcher.stop();
    }

    @Test
    public void testSsubscribeToRequest() {
        SubscriptionRequest<MockDataObject> request = new SubscriptionRequest<MockDataObject>(key,receiver,SubscriptionType.ASYNC_SNAP_AND_SUBSCRIBE);
        ss.subscribe(request);

        p.update(new MockDataObject(key,12.0));
        LockSupport.parkNanos(100000);
        Assert.assertEquals(1,receiver.getData().size());
        receiver.clear();
        p.update(new MockDataObject(key,13.0));
        LockSupport.parkNanos(100000);
        Assert.assertEquals(1,receiver.getData().size());
        MockDataObject o = receiver.getData().iterator().next();
        Assert.assertEquals(13.0,o.getValue(),0.0000000001);
    }

    @Test
    public void testConflation() {
        ss.setConflation(true);
        SubscriptionRequest<MockDataObject> request = new SubscriptionRequest<MockDataObject>(key,receiver,SubscriptionType.ASYNC_SNAP_AND_SUBSCRIBE);
        ss.subscribe(request);
        int tests = 1000;
        for (int i = 0; i < tests;i++) {
            p.update(new MockDataObject(key, 12.0));
            //LockSupport.parkNanos(100);
        }
        Assert.assertTrue(receiver.getData().size() < tests);
        System.out.println("Got "+receiver.getData().size() + " from " + tests);
    }

    @Test
    public void testSyncSnap() {
        SubscriptionRequest<MockDataObject> request = new SubscriptionRequest<MockDataObject>(key,receiver,SubscriptionType.SNAP);
        ISubscriptionResponse<MockDataObject> response = ss.subscribe(request);
        Assert.assertNull(response.getInitialSnapshot());
        MockDataObject update = new MockDataObject(key,12.0);
        p.update(update);
        response = ss.subscribe(request);
        Assert.assertNotNull(response.getInitialSnapshot());
        Assert.assertEquals(response.getInitialSnapshot(),update);
        Assert.assertEquals(0,receiver.getData().size()); //check no sub updates.
    }


    @Test
    public void testUnsubscribeToRequest() {
        SubscriptionRequest<MockDataObject> request = new SubscriptionRequest<MockDataObject>(key,receiver,SubscriptionType.ASYNC_SNAP_AND_SUBSCRIBE);
        ss.subscribe(request);

        p.update(new MockDataObject(key,12.0));
        LockSupport.parkNanos(100000);
        Assert.assertEquals(1,receiver.getData().size());
        receiver.clear();
        p.update(new MockDataObject(key,13.0));
        LockSupport.parkNanos(100000);
        Assert.assertEquals(1,receiver.getData().size());
        MockDataObject o = receiver.getData().iterator().next();
        Assert.assertEquals(13.0,o.getValue(),0.0000000001);

        //remove subscription
        receiver.clear();
        ss.unsubscribe(request);
        p.update(new MockDataObject(key,13.2));
        Assert.assertEquals(0,receiver.getData().size());
    }

    public static void main(String[] args) {

       TestSubscriptionService tss = new TestSubscriptionService();
       tss.setup();
       tss.testUnsubscribeToRequest();
    }
}
