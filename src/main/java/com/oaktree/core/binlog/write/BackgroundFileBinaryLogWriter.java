package com.oaktree.core.binlog.write;

import com.oaktree.core.utils.ByteUtils;
import com.oaktree.core.utils.ResultTimer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A binary logger that takes the object array and pushes to a bounded
 * or unbounded queue. This is much quicker to push data but comes at the
 * loss of guaranteed write (possible for logs to be enqueued before being
 * flushed) and means you have to have an object array to give to the
 * logger (no object free direct calls). For most in-the-latency-path
 * areas this is fine tradeoff.
 *
 * Note - stop will block until queue is emptied.
 *
 * illegal state exception if queue is full
 * To be unbounded set size = 0
 *
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 13/04/13
 * Time: 18:53
 * To change this template use File | Settings | File Templates.
 */
public class BackgroundFileBinaryLogWriter extends GenericFileBinaryLogWriter implements Runnable {

    private BlockingQueue<Object[]> queue;
    private Thread thread;

    public void start() {
        thread = new Thread(this);
        thread.setName("bin.log.processor");
        thread.start();
        super.start();
    }

    public BackgroundFileBinaryLogWriter(boolean useByteBuffer,byte[] schema, String name, String fileName, int maxQueueSize) {
        super(useByteBuffer,schema, name, fileName);
        queue = new LinkedBlockingQueue<Object[]>(maxQueueSize);
    }

    @Override
    public void log(Object[] values) {
        if (canLog()) {
            queue.add(values); //illegal state if full.
        }
    }

    public void stop() {
        logger.info("Stopping processing thread...queue size: "+ queue.size());
        //TODO queue might not be emptied yet. shame.
        while (queue.size() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.warn(e.getMessage());
            }
            logger.info("Pending termination...queue: " + queue.size());
        }
        logger.info("Queue is empty. Terminating");
        thread.interrupt();
    }

    @Override
    public void run() {
        try {
            logger.info("Processing thread is started");
            int i = 0;
            while (true) {
                Object[] o = queue.take();
                //logger.info("Processing " + i + " " + o);
                i++;
                super.log(o);
            }

        }catch (InterruptedException e) {
            logger.info("Processing thread terminated");
            super.stop();
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        long TESTS = 500000;
        boolean useByteBuffer = true;
        BackgroundFileBinaryLogWriter logger = new BackgroundFileBinaryLogWriter(useByteBuffer,new byte[]{ByteUtils.Types.LONG,ByteUtils.Types.LONG},"GFBL","test.bl",(int)TESTS);
        logger.start();

        ResultTimer x = new ResultTimer();
        x.startSample();
        ResultTimer t = new ResultTimer(10000);
        for (long l = 0; l < TESTS; l++) {
            t.startSample();
            logger.log(new Object[]{12l+l,32l});
            t.endSample();
        }
        System.out.println("Object WriteDuration:" +t.toString(TimeUnit.MICROSECONDS));

        ///Thread.sleep(30000);

        logger.stop();
        x.endSample();
        System.out.println("Whole write: " + x.toString(TimeUnit.SECONDS));
    }

}
