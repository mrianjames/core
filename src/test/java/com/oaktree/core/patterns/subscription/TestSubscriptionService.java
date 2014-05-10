package com.oaktree.core.patterns.subscription;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.IComponent;
import com.oaktree.core.patterns.cache.IData;
import com.oaktree.core.patterns.sequence.IDataProvider;
import com.oaktree.core.patterns.sequence.IDataReceiver;
import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ianjames on 09/05/2014.
 */
public class TestSubscriptionService {

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
        public double getvalue() {
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
            logger.info("INCOMING:"+data.toString());
            datas.add(data);
        }
        public Collection<MockDataObject> getData() {
            return datas;
        }
        public void clear() {
            datas.clear();
        }
    }

    public static void main(String[] args) {

        MockDataProvider p = new MockDataProvider();
        IDispatcher dispatcher = new ThroughputDispatcher("D",2);
        dispatcher.start();

        SubscriptionService<MockDataObject> ss = new SubscriptionService<MockDataObject>("ss");
        ss.addProvider(p);

        String key = "TEST.KEY";
        IDataReceiver<MockDataObject> receiver = new MockReceiver();
        ss.subscribe(new SubscriptionRequest<MockDataObject>(key,receiver,SubscriptionType.ASYNC_SNAP_SUBSCRIBE));

        p.update(new MockDataObject(key,12.0));
    }
}
