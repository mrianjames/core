package com.oaktree.core.threading.message;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.container.IComponent;
import com.oaktree.core.container.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 25/07/11
 * Time: 19:04
 */
public class ArrayDispatcher extends AbstractComponent implements IMessageDispatcher {

    private final static Logger logger = LoggerFactory.getLogger(ArrayDispatcher.class.getName());
    private IQueueProcessor[] qp = new IQueueProcessor[]{};
    private int[] threadBindings = new int[]{};
    private int threadCount = 4;
    private int maxKeyCount = 100000;
    private int maxBufferSize = 1048576;

    public ArrayDispatcher(String name, int threads, int maxKeys) {
        this.setName(name);
        this.threadCount = threads;
        this.maxKeyCount = maxKeys;
    }

    @Override
    public void start() {
        logger.info("Starting " + getName()  + " with " + threadCount + " threads. MaxKeys: " + maxKeyCount +" MaxBuffer:" + maxBufferSize);
        setState(ComponentState.STARTING);
        qp = new IQueueProcessor[threadCount];
        threadBindings = new int[maxKeyCount];
        for (int i = 0; i < threadCount; i++) {
            qp[i] = new DisruptorQueueProcessor(maxBufferSize);
            qp[i].setName(""+i);
            qp[i].start();
        }
        //assign for each key a thread id.
        int td = 0;
        for (int j = 0; j < maxKeyCount; j++) {
            if (j % threadCount == 0) {
                td = 0;
            } else {
                td +=1;
            }
            threadBindings[j] = td;
        }
        setState(ComponentState.AVAILABLE);
    }

    @Override
    public void stop() {
        setState(ComponentState.STOPPING);
        for (int i = 0; i < threadCount; i++) {
            qp[i].stop();
        }
        setState(ComponentState.STOPPED);
    }

    @Override
    public void dispatch(int key, IComponent component,IMessage msg) {
        IQueueProcessor thread = qp[threadBindings[key]];
        if (thread != null) {
            thread.add(key,component,msg);
        }
    }

}
