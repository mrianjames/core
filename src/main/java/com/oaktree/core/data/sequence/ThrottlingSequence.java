package com.oaktree.core.data.sequence;

import java.util.concurrent.TimeUnit;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.cache.IData;
import com.oaktree.core.threading.dispatcher.IDispatcher;

/**
 * Sequence that throttles incoming data to a specified output rate. 
 * If an input event occurs > duration from last update on that key then update is immediately sent.
 * If an input event occurs <= duration from last update on that key then we will wait until the next 
 * time barrier (which is defined as in throttle - (event receive time - last publish time)).
 * For example, throttle to 1 update every 10ms. Time now is 1231123120 (ms since epoch). 
 * We last published 3ms ago (1231123117). Update comes in, we do 10 - (1231123120-1231123117) = 7ms.
 * 
 * @author ij
 *
 * @param <I>
 * @param <O>
 */
public class ThrottlingSequence<I extends IData<?>,O extends IData<?>> extends DataSequence<I,O> implements	IDataSequence<I, O> {


	private long minDurationBetweenUpdates;
	private TimeUnit timeUnit;

	public ThrottlingSequence(String name, long minDurationBetweenUpdates, TimeUnit tu) {
		super(name);
		this.timeUnit = tu;
		this.minDurationBetweenUpdates = minDurationBetweenUpdates;
	}
	


	@Override
	public void onData(I data, IComponent from,final long receivedTime) {
		
		final O updated = process(data,from,receivedTime);
		long lastUpdateTime = lastUpdateTimes.get(data.getDataKey());
		
	}

    @Override
    public String toString() {
    	return "ThrottlingSequence["+getName()+"] " + minDurationBetweenUpdates +timeUnit.name();
    }
}
