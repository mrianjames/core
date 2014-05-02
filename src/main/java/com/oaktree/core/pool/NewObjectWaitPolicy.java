package com.oaktree.core.pool;

/**
 * Wait policy that doesnt wait! It just makes a fresh
 * object for the consumer. Object does not get added to 
 * the pool; its GCable.
 * 
 * @author IJ
 *
 * @param <T>
 */
public class NewObjectWaitPolicy<T> implements IWaitPolicy<T> {

	@Override
	public T wait(SimplePool<T> pool) {
		return pool.getObjectFactory().make();
	}

}
