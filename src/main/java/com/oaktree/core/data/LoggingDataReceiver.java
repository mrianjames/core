package com.oaktree.core.data;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.IComponent;

/**
 * Simple data receiver that just logs.
 * 
 * @author ij
 *
 * @param <T>
 */
public class LoggingDataReceiver<T> extends AbstractComponent implements IDataReceiver<T> {

	@Override
	public void onData(T data, IComponent from, long receivedTime) {
		if (logger.isInfoEnabled()) {
			logger.info(getName()+"[INCOMING]: "+data + " from " + from + " at " + receivedTime);
		}
	}

}
