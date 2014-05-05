/**
 * 
 */
package com.oaktree.core.threading;

import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for making prioritised and named threads.
 * @author ij
 *
 */
public class PrioritisedNamedThreadFactory implements ThreadFactory {

	private static final Logger logger = LoggerFactory.getLogger(PrioritisedNamedThreadFactory.class.getName());
	
	/**
	 * The name of the thread. Will be name[id].
	 */
	private String name = "dispatcher";
	
	/**
	 * The thread priority every created thread will have.
	 */
	private int priority = Thread.NORM_PRIORITY;
	
	/**
	 * Make me.
	 * @param name
	 * @param priority
	 */
	public PrioritisedNamedThreadFactory(String name, int priority) {
		this.name = name;
		this.priority = priority;
	}
	
	public PrioritisedNamedThreadFactory() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setPriority(this.priority);
		t.setName(this.name + "[" + t.getId() + "]");
		
		if (logger.isDebugEnabled()) {
			logger.debug("Created thread " + t.getName() + " (" + t.getId() +") with priority " + this.priority);
		}
		return t;
	}

}
