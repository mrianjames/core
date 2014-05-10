package com.oaktree.core.data.cache;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.sequence.IDataReceiver;

/**
 * A cache of data.
 * 
 * @author ij
 *
 * @param <T> - type of data stored in the cache.
 * @param <K> - type of key of the data stored in the cache.
 */
public interface IDataCache<T,K> extends IComponent, IDataReceiver<T> {

	/**
	 * Snap data out the cache using a key.
	 * 
	 * @param key
	 * @return
	 */
	public T snap(K key);
	
	/**
	 * Remove all elements in the cache.
	 */
	public void clearCache();
	
	/**
	 * Remove a selected element from the cache.
	 * @param key
	 */
	public void clearCache(K key);
		
}
