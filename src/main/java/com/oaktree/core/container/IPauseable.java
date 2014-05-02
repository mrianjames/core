package com.oaktree.core.container;

/**
 * Specification of an entity that supports pause and resume functionality.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public interface IPauseable {
	
	/**
	 * Pause this entity.
	 */
	public void pause();
	
	/**
	 * Resume this entity.
	 */
	public void resume();
	
	/**
	 * Check if this entity is currently paused.
	 */
	public boolean isPaused();
	
}
