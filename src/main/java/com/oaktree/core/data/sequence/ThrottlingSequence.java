package com.oaktree.core.data.sequence;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.oaktree.core.container.IComponent;

import com.oaktree.core.data.IData;
import com.oaktree.core.data.IDataReceiver;
import com.oaktree.core.time.ITime;

/**
 * Sequence that throttles incoming data to a specified output rate. 
 * If an input event occurs > duration from last update on that key then update is immediately sent.
 * If an input event occurs <= duration from last update on that key then we will wait until the next 
 * time barrier (which is defined as in throttle - (event receive time - last publish time)).
 * For example, throttle to 1 update every 10ms. Time now is 1231123120 (ms since epoch). 
 * We last published 3ms ago (1231123117). Update comes in, we do 10 - (1231123120-1231123117) = 7ms.
 * 
 * TODO look at timeunit.
 * 
 * @author ij
 *
 * @param <I>
 * @param <O>
 */
public class ThrottlingSequence<I extends IData<? extends String>,O extends IData<? extends String>> extends DataSequence<I,O> implements	IDataSequence<I, O> {


	private long minDurationBetweenUpdates;
	private TimeUnit timeUnit;
	private ITime time;
	
	/**
	 * Keyed on the data key (String), a map of times we last updated on.
	 */
	private ConcurrentMap<String,Long> lastUpdateTimes = new ConcurrentHashMap<String,Long>();

	
	public void setTime(ITime time) {
		this.time = time;
	}

	public ThrottlingSequence(String name, long minDurationBetweenUpdates, TimeUnit tu, ITime time) {
		super(name);
		this.timeUnit = tu;
		this.time = time;
		this.minDurationBetweenUpdates = minDurationBetweenUpdates;
	}
	
	
	@Override
	public void onData(I data, IComponent from,final long receivedTime) {
		
		final O updated = process(data,from,receivedTime);
		synchronized (data.getDataKey().intern()) {			
			
			long now = getTime();
			if (shouldSend(data.getDataKey(),now)) {
				lastUpdateTimes.put(data.getDataKey(), getTime());
				//TODO review if we can exist sync block...
				for (IDataReceiver<O> receiver:getReceivers()) {
					receiver.onData(updated, this, now);
				}
			}
		}		
		
	}

	/**
	 * Decide if we can or can't send data on.
	 * @param dataKey
	 * @param now
	 * @return
	 */
    private boolean shouldSend(String dataKey, long now) {
    	Long lastUpdateTime = lastUpdateTimes.get(dataKey);
    	if (lastUpdateTime == null) {
    		return true;
    	}
    	if (now - lastUpdateTime >= minDurationBetweenUpdates) {
    		return true;
    	}
    	return false;
	}

	private Long getTime() {
		return time.getTimeOfDay();
	}

	@Override
    public String toString() {
    	return "ThrottlingSequence["+getName()+"] " + minDurationBetweenUpdates +timeUnit.name();
    }
}
