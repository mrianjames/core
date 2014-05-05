package com.oaktree.core.container;

/**
 * Specification of basic component life-cycle events.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public interface IBasicLifecycle {
	
	/**
	 * Initialise this entity. Normally setup is down here instead of 
	 * relying on the constructor.
	 */
	public void initialise();
	
	/**
	 * Start this entity.
	 */
	public void start();
	
	/**
	 * Stop this entity.
	 */
	public void stop();
	
	/**
	 * Restart the entity.
	 */
	public void restart();
}
