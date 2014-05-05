package com.oaktree.core.patterns.cache;

/**
 * Data that has a key we can use to store it with or lookup quickly.
 * 
 * @author ij
 *
 */
public interface IData<T> {

	/**
	 * Get the key for a piece of data.
	 * 
	 * @return
	 */
	public T getDataKey();
}
