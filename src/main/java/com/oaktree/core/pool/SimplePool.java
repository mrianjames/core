package com.oaktree.core.pool;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple object pool. Of my hand. Initial allocation if allocated and accessible via lock free mechanisms (non concurrent collections
 * as we do not add/remove at runtime). Where we cannot find a free object you may inject optional wait policy that can handle this in
 * different ways e.g.:
 * <li>Make a new object</li>
 * <li>Throw Exception</li>
 * <li>Spin until object found (burn)</li> 
 * <li>Spin until object found (sleep for x ms between attempts)</li>
 * <li>Expand the pool with fresh object(s)</li>
 * 
 * Generally thread pools are considered to be un-required in modern Java systems due to the cheapness of object creation and GC. Whilst in the 
 * main this is true low latency systems require a different magnitude of predictability. Even when using paralell GC the STW pauses will still 
 * define your top latency which will be many magnitudes greater than your standard operation latency. 
 * In these cases (consider a financial trading process that consumes thousands of throw-away ticks of market data) an object pool may help by:
 * <li>removing cost of object creation (retreival < creation)</li>
 * <li>reduction in GC times</li>
 * 
 *  Why not use apache pool? Performance sucks. Results on laptop (early i7):
 *
 *  Test: ALL poolSize: 8 threads: 4 gets: 30000. Repeated to remove warmup factors.
 *  <pre>
    APACHE_COMMONS: avg: 2118.0 ns failures: 0
	STDALLOC: avg: 619.0 ns failures: 0
	SIMPLEPOOL: avg: 252.0 ns Memory: 458752 failures: 0
 *  </pre> 
 *  
 *  Even if one ignores any positive top end jitter removal that could be acheived pooling
 *  can be considered a performance improvement. 
 *  Even so - use with caution; pooling increases complexity and you need to be thorough ensuring
 *  objects are "blanked" to stop bleeding of values from one use to the next. Unless you need reduced gc/jitter 
 *  or the latency improvement use std object creation and let GC sort it out for you.
 *  
 *  Performance will be dependent on many factors
 *  <li>number of entries in pool (scans list of objects from start looking for free)</li>
 *  <li>how realistic initial size was (pre-allocated list is faster than dynamically adjusted list)</li>
 *  <li>size of object; greater benefit for larger/slower to create objects</li>
 * 
 * Note: Dont cast your objects to primatives like this:
 * int x = pool.get();
 * ...
 * pool.free(x);
 * 
 * This wont work - java will not give you the same object for the two operations - the free part makes a new integer object
 * which is "equal" but not the same object you thought you were passing. It will work if you do Integer x = pool.get(); pool.free(x);
 * 
 * @author IJ
 *
 * @param <T> Object type this pool will hold.
 */
public class SimplePool<T> implements IPool<T> {

	/**
	 * Standard, pre allocation object pool. Deliberately non-concurrent collection for performance (we do not add/remove at runtime).
	 */
	private List<T> allObjects = new ArrayList<T>();
	/**
	 * Standard, pre allocation free list. Deliberately non-concurrent collection for performance (we do not add/remove at runtime).
	 */
	private List<AtomicBoolean> freeList = new ArrayList<AtomicBoolean>(); //true for free, false for reserved.
	
	/**
	 * Extended runtime object list that will be checked after std allocation fails (depending on wait policy). This can be added or removed
	 * from at runtime.
	 */
	private List<T> extendedAllObjects = new CopyOnWriteArrayList<T>();
	
	/**
	 * Extendable runtime free list that will be checked after std allocation fails. This can be added or removed from at runtime.
	 */
	private List<AtomicBoolean> extendedFreeList = new CopyOnWriteArrayList<AtomicBoolean>(); //true for free, false for reserved.
	
	/**
	 * The std pool capacity.
	 */
	private AtomicInteger capacity;
	/**
	 * Factory to create objects.
	 */
	private IObjectFactory<T> factory;
	/**
	 * Optional thing todo when no entry found e.g. block, spin, barf....etc.
	 */
	private IWaitPolicy<T> waitPolicy;
	/**
	 * Lock for ensuring pool expansion only occurs one at a time.
	 */
	private Object expandLock = new Object(); 
	
	/**
	 * 
	 * @param capacity
	 * @param factory
	 */
	public SimplePool(int capacity, IObjectFactory<T> factory) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Invalid capacity. Must be positive integer");
		}
		if (factory == null) {
			throw new IllegalArgumentException("Invalid factory. Must be not-null so we can make objects.");
		}

		this.capacity = new AtomicInteger(capacity);
		this.factory = factory;
		for (int i = 0; i < capacity;i++) {
			allObjects.add(factory.make());
			freeList.add(new AtomicBoolean(true));
		}
	}
	
	/**
	 * Return the object factory
	 * @return object factory.
	 */
	IObjectFactory<T> getObjectFactory() {
		return factory;
	}
	/**
	 * Inject an optional wait policy invoked when there is no
	 * pooled object free.
	 * 
	 * @param waitPolicy
	 */
	public void setWaitPolicy(IWaitPolicy<T> waitPolicy) {
		this.waitPolicy = waitPolicy;
	}
	
	@Override
	public T get() {
		//find a "free" slot, reserve and dispense.
		T obj = getFreeObject();
		if (obj != null) {
			return obj;
		}
		//could not find anything...
		if (waitPolicy != null) {
			return waitPolicy.wait(this);
		}
		return null;
	}
	
	/**
	 * Find matching object
	 * 
	 * @param record
	 * @param list
	 * @return
	 */
	public int getIndexOfRecord(T record,List<T> list) {
		int i = 0;
		for (T c:list) {
			if (record == c) {
				return i;
			}
			i++;
		}
		return -1;
	}

	@Override
	public void free(T record) {
		int indexOf = getIndexOfRecord(record,allObjects);
		if (indexOf == -1) {
			//not in std pool, lets check the free pool.
			int extIndexOf = getIndexOfRecord(record,extendedAllObjects);
			if (extIndexOf >= 0) {
				extendedFreeList.get(extIndexOf).set(true);
			}
			return; //wtf nought to do with us.
		}
		//System.err.println("Setting free on index "+indexOf);
		freeList.get(indexOf).set(true);
	}

	@Override
	public int getCapacity() {
		return capacity.get();
	}

	@Override
	public void setCapacity(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Invalid size for object pool. Must be positive integer");
		}
		if (capacity < this.capacity.get()) {
			throw new IllegalArgumentException("Reduction of object pool is not supported");
		}
		synchronized (expandLock ) {
			int expandBy = capacity - this.capacity.get();
			if (expandBy > 0) {
				expandPoolBy(expandBy);
			}
			this.capacity.set(capacity);
		}
	}

	/**
	 * Expand the pool (the extended pool)
	 * 
	 * @param expandBy
	 */
	void expandPoolBy(int expandBy) {
		synchronized (expandLock ) {
			for (int i = 0; i < expandBy; i++) {
				extendedAllObjects.add(factory.make());
				extendedFreeList.add(new AtomicBoolean(true));
			}
		}
	}

	/**
	 * Get a free object from pool(s)
	 * @return free object, or null if none found.
	 */
	public T getFreeObject() {
		for (int i = 0; i < allObjects.size(); i++) {
			AtomicBoolean state = freeList.get(i);
			if (state.compareAndSet(true, false)) {
				return allObjects.get(i);
			}
		}
		//nothing in std pool; go onto dynamic...
		for (int i = 0; i < extendedAllObjects.size(); i++) {
			AtomicBoolean state = extendedFreeList.get(i);
			if (state.compareAndSet(true, false)) {
				return extendedAllObjects.get(i);
			}
		}
		return null;
	}

}
