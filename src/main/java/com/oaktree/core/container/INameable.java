package com.oaktree.core.container;

/**
 * A specification of a object that has a name.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public interface INameable {
	
	/**
	 * Set the name of this container.
	 * @param name
	 */
	public void setName(String name);
	/**
	 * Get this containers name
	 * @return
	 */
	public String getName();
}
