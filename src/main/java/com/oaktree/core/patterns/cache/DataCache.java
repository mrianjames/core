package com.oaktree.core.patterns.cache;

import java.util.concurrent.ConcurrentHashMap;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.IComponent;
import com.oaktree.core.patterns.sequence.IDataProvider;

/**
 * Simple firm cache implementation. We store objects of type T, which are keyed on type K.
 * T objects must be able to tell us what their key is as we use this to store and you 
 * must use this to extract or remove entries.
 * 
 * Other cache implementations we could do:
 * 1) TimedCache - we wrap IData objects with object that has the timetamp - we then have sched task to purge elements.
 * 2) Weak refs?
 * 3) DataCache that allows object class (i.e. any data) from a different interface - i.e. we supply the key on the data call back.
 * 
 * @author ij
 *
 * @param <T>
 * @param <K>
 */
public class DataCache<T extends IData<K>,K> extends AbstractComponent implements IDataCache<T,K> {
	private final static int defaultInitialSize = 100;
	/**
	 * The cache.
	 */
	private ConcurrentHashMap<K,T> cache;
	
	public DataCache() {
		this(defaultInitialSize);
	}
	public DataCache(int initialSize) {
		cache = new ConcurrentHashMap<K,T>(initialSize);
	}
	
	@Override
	public void onData(T data, IComponent from, long receivedTime) {
		cache.put(data.getDataKey(),data);
		if (logger.isTraceEnabled()) {
			logger.trace(getName() + " stored: " + data + " from " + from + " received at " + receivedTime);
		}
	}

	@Override
	public T snap(K key) {
		return cache.get(key);
	}

	@Override
	public void clearCache() {
		cache.clear();
	}

	@Override
	public void clearCache(K key) {
		cache.remove(key);
	}
	
}