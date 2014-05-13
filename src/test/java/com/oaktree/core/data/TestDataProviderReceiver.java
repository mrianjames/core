package com.oaktree.core.data;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.subscription.SubscriptionService;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ianjames on 13/05/2014.
 */
public class TestDataProviderReceiver {

    private MockDataProvider p;
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
            this.receiver = null;
        }

        @Override
        public void registerInterest(Object key, IDataReceiver<MockDataObject> from) {

        }

        @Override
        public void removeInterest(Object key, IDataReceiver<MockDataObject> from) {

        }

        public void update(MockDataObject o) {
            if (receiver != null) {
                receiver.onData(o, this, System.currentTimeMillis());
            }
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

        this.receiver = new MockReceiver();
        this.p.addDataReceiver(receiver);
        receiver.setName("MOCKREC");
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testBasics() {
        this.p.update(new MockDataObject(key,12.0));
        Assert.assertEquals(1, receiver.getData().size());
        this.p.removeReceiver(receiver);
        this.p.update(new MockDataObject(key,12.0));
        Assert.assertEquals(1, receiver.getData().size());
    }

}
