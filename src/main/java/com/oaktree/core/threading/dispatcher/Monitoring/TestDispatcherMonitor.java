package com.oaktree.core.threading.dispatcher.Monitoring;

import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import com.oaktree.core.time.ITimeScheduler;
import com.oaktree.core.time.MultiTimeScheduler;
import com.oaktree.core.utils.CircularQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: IJ
 * Date: 21/02/12
 * Time: 16:06
 * To change this template use File | Settings | File Templates.
 */
public class TestDispatcherMonitor {
    
    private final static Logger logger = LoggerFactory.getLogger(TestDispatcherMonitor.class);
    public static void main(String[] args) {
        IDispatcher dispatcher = new ThroughputDispatcher("TD",3);
        dispatcher.start();
        MultiTimeScheduler scheduler = new MultiTimeScheduler(dispatcher);
        scheduler.initialise();
        scheduler.start();
        DispatcherMonitor monitor = new DispatcherMonitor("IJMON",dispatcher,scheduler,1000);
        monitor.initialise();
        monitor.start();
        CircularQueue<String> q = new CircularQueue<String>();
        q.add("X");
        q.add("Y");
        q.add("Z");
        Iterator<String> it = q.iterator();
        for (int i = 0; i < 10000; i ++) {
            final int x = i;
            dispatcher.dispatch(it.next(),new Runnable() {
                @Override
                public void run() {
                    logger.info("HELLO " + x);
                }
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }
}
