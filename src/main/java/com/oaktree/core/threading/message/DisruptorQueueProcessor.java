package com.oaktree.core.threading.message;

import com.lmax.disruptor.*;
import com.oaktree.core.container.IComponent;
import com.oaktree.core.container.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A queue processor that uses the lmax disruptor.
 *
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 25/07/11
 * Time: 19:22
 */
public class DisruptorQueueProcessor implements IQueueProcessor, Runnable {

    private final static Logger logger = LoggerFactory.getLogger(DisruptorQueueProcessor.class);
    private RingBuffer<MessageDeliveryTask> ringBuffer;
    private SequenceBarrier consumerBarrier;
    //private ProducerBarrier<MessageDeliveryTask> producerBarrier;
    private Thread thread;
    private BatchEventProcessor<MessageDeliveryTask> batchConsumer;

    private int bufferSize;
    private String name;
    private int executedCount;
    private long count = 0;


    public static final class MessageDeliveryTask  {
        private IComponent component;
        private IMessage msg;
        private int key;

        public IComponent getValue() {
            return component;
        }

        public IMessage getMessage() {
            return msg;
        }

        public void setMessage(IMessage msg) {
            this.msg = msg;
        }

        public void setComponent(final IComponent component) {
            this.component = component;
        }

        public void setKey(int key) {
            this.key = key;
        }

        public int getKey() {
            return key;
        }

        public final static EventFactory<MessageDeliveryTask> ENTRY_FACTORY = new EventFactory<MessageDeliveryTask>() {
            public MessageDeliveryTask newInstance() {
                return new MessageDeliveryTask();
            }
        };
    }

    final EventHandler<MessageDeliveryTask> batchHandler = new EventHandler<MessageDeliveryTask>() {
        @Override
        public void onEvent(MessageDeliveryTask entry, long sequence, boolean endOfBatch) throws Exception {
            thread.setName("" + entry.getKey());
            entry.component.onMessage(entry.getMessage()); //deliver the msg.
            executedCount += 1;
        }
    };

    public DisruptorQueueProcessor(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void start() {
        this.ringBuffer =
                new RingBuffer<MessageDeliveryTask>(MessageDeliveryTask.ENTRY_FACTORY,
                        new SingleThreadedClaimStrategy(bufferSize),
                        new SleepingWaitStrategy());
        //this.ringBuffer.setGatingSequences(new Sequence);
        this.consumerBarrier = ringBuffer.newBarrier();
        this.batchConsumer = new BatchEventProcessor<MessageDeliveryTask>(ringBuffer,consumerBarrier, batchHandler);
        //this.producerBarrier = ringBuffer.createProducerBarrier(batchConsumer);
        this.thread = new Thread(this);
        this.thread.start();
        this.thread.setName(name);
    }

    @Override
    public void run() {
        batchConsumer.run();

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void stop() {
        batchConsumer.halt();
    }

    public long getExecutedTasks() {
        return executedCount;
    }

    public long getSize() {
        return count - executedCount;
    }

    @Override
    public void add(int key, IComponent component, IMessage msg) {
        long sequence = ringBuffer.next();
        MessageDeliveryTask runner = ringBuffer.get(sequence);
        runner.setComponent(component);
        runner.setMessage(msg);
        runner.setKey(key);
        count += 1;
        ringBuffer.publish(sequence);
    }

}
