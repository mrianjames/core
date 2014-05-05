package com.oaktree.core.threading.message;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.IMessage;
import com.oaktree.core.container.Message;
import com.oaktree.core.utils.CircularQueue;
import com.oaktree.core.utils.LongStatistics;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 25/07/11
 * Time: 22:20
 */
@Ignore
public class TestMessageDispatcher extends AbstractComponent {

    private Logger logger = LoggerFactory.getLogger(TestMessageDispatcher.class);
    private IMessageDispatcher dispatcher;
    private int keys = 32;
    private int threads = 8;
    private CountDownLatch latch = new CountDownLatch(0);
    private boolean wait = false; //fast as possible = false,with 1ms wait
    private Iterator<Integer> it;
    private int tests = 1000000;
    private IMessage[] msg = new IMessage[]{};

    private IMessageDispatcher setupArrayDispatcher(int threads, int maxKeys) {
        ArrayDispatcher dispatcher = new ArrayDispatcher("Test",threads,maxKeys);
        return dispatcher;
    }

    public void setup(int threads, int keys) {

        CircularQueue<Integer> q = new CircularQueue<Integer>();
        for (int i = 0; i < keys; i++) {
            q.add(i);
        }
        this.it = q.iterator();

        msg = new Message[tests];
        for (int i = 0; i < tests; i++) {
            msg[i] = new Message();
            msg[i].setMessageId(""+i);
        }


        this.dispatcher = setupArrayDispatcher(threads,keys);
        this.dispatcher.start();
    }

    @Before
    public void setup() {
        setup(threads,keys);

    }


    @Test
    public void testLatency() {

        latch = new CountDownLatch(tests);
        for (int i = 0; i < tests;i++) {
            msg[i].setMessageContents(System.nanoTime());
            this.dispatcher.dispatch(it.next(),this,msg[i]);
            if (wait) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        try {
            latch.await();
        } catch (Exception e) {}
        logger.info("All tests received");
        LongStatistics stats = new LongStatistics(tests);
        for (IMessage m:msg) {
            stats.addValue((Long) m.getMessageContents());
        }
        logger.info("Median: " + stats.getMedian()/1000 + " us.");
    }

    @Override
    public void onMessage(IMessage msg) {
        //logger.info("Thanks: " + msg.getMessageId());
        long time = System.nanoTime();
        long duration = time - ((Long)(msg.getMessageContents()));
        msg.setMessageContents(duration);
        latch.countDown();
    }
}
