package com.oaktree.core.pool;
/**
 * Expand the pool when called by a percentage of current capacity.
 * 
 * @author IJ
 *
 * @param <T>
 */
public class ExpandPoolWaitPolicy<T> implements IWaitPolicy<T> {

	/**
	 * Expansion of pool to request in percentage.
	 */
	private int pctIncrease = 10;
	/**
	 * Max number of increases we are allowed todo before we blow up. Required as it
	 * suggests something very wrong in your configuration or setup if we have large numbers
	 * of pct increases going on.
	 */
	private int maxIncreases = 10;
	/**
	 * Optional delegate wait policy when we fail to get object still after max attempts to expand the pool.
	 */
	private IWaitPolicy<T> delegateWaitPolicy = new ExceptionWaitPolicy<T>();
	/**
	 * current number of increases occuring to pool
	 */
	private int increases = 0;

	/**
	 * Create me.
	 * @param pctIncrease
	 * @param maxIncreases
	 */
	public ExpandPoolWaitPolicy(int pctIncrease, int maxIncreases) {
		this.pctIncrease  = pctIncrease;
		this.maxIncreases  = maxIncreases;
	}
	
	/**
	 * Set a delegate wait policy for if we cannot get an object even after expanding the pool by 
	 * the pctage increase a max number of times.
	 * @param waitPolicy
	 */
	public void setDelegateWaitPolicy(IWaitPolicy<T> waitPolicy) {
		this.delegateWaitPolicy = waitPolicy;
	}
	
	@Override
	public T wait(SimplePool<T> pool) {
		for (int i = this.increases ; i < maxIncreases;i++) {
			int inc = (int)(pool.getCapacity()*((100d+pctIncrease)/100d));
			if (inc <= 0 || inc == pool.getCapacity()) {
				inc = pool.getCapacity()+1;
			}
			pool.setCapacity(inc);
			this.increases = increases+1;
			T obj = pool.getFreeObject();
			if (obj != null) {
				return obj;
			}
		}
		if (delegateWaitPolicy != null) {
			return delegateWaitPolicy.wait(pool);
		}
		return null;
	}

}
