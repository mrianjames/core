package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IDataReceiver;

/**
 * log and hand off to next stages.
 *
 * Created by ianjames on 13/05/2014.
 */
public class LoggingSequence<I,O> extends DataSequence<I,O> {

    public LoggingSequence(String name) {
        super(name);
    }

    @Override
    public void onData(I data, IComponent from, long receivedTime) {
        //do what you need with the data...then distribute the result.
        O updated = process(data,from,receivedTime);
        if (logger.isInfoEnabled()) {
            logger.info(getName()+" onData: " + data + " from " + from.getName());
        }
        for (IDataReceiver<O> sink: getReceivers()) {
            sink.onData(updated, this, receivedTime);
        }
    }

    protected O process(I data, IComponent from, long receivedTime) {
        return (O)data;
    }

}
