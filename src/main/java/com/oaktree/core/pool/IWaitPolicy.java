package com.oaktree.core.pool;
/**
 * A policy that dictates the behaviour that occurs when a free object cannot be resolved from the object pool.
 * 
 * @author IJ
 *
 * @param <T>
 */
public interface IWaitPolicy<T> {
	/**
	 * Return an object when we cannot resolve an object through conventional means.
	 * 
	 * @param pool
	 * @return
	 */
	public T wait(SimplePool<T> pool);
}
