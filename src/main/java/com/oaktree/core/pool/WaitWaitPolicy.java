package com.oaktree.core.pool;

public class WaitWaitPolicy<T> implements IWaitPolicy<T> {
	private int wait;
	public WaitWaitPolicy(int waitInMillis) {
		this.wait = waitInMillis;
	}
	@Override
	public T wait(SimplePool<T> pool) {
		T obj = pool.getFreeObject();
		while (obj == null) {
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
			}
			obj = pool.getFreeObject();
		}
		return obj;
	}

}
