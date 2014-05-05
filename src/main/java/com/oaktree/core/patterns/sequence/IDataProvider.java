package com.oaktree.core.patterns.sequence;

import java.util.Collection;

import com.oaktree.core.container.IComponent;

/**
 * Something that provides data to receivers. A source of data
 * of type T if you will.
 * 
 * @author ij
 *
 * @param <T> - data type we are providing to receivers.
 */
public interface IDataProvider<T> extends IComponent {

	/**
	 * Add a single receiver to this provider.
	 * 
	 * @param listener
	 */
	public void addDataReceiver(IDataReceiver<T> receiver);
	
	/**
	 * Single "spring happy" setter.
	 * @param receiver
	 */
	public void setDataReceiver(IDataReceiver<T> receiver);
	
	/**
	 * Get all the receivers.
	 * 
	 * @return
	 */
	public Collection<IDataReceiver<T>> getReceivers();
	
	/**
	 * Remove a receiver.
	 * 
	 * @param receiver
	 */
	public void removeReceiver(IDataReceiver<T> receiver);
}
