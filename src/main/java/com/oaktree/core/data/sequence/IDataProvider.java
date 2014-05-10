package com.oaktree.core.data.sequence;

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
	 * @param receiver
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

    /**
     * A receiver of our data may make explicit interest requests.
     * Our implementation will decide if we do anything with this,
     * we may be listening for all data anyway (*).
     * You should expect to have duplicate requests and handle accordingly
     * i.e. dont deluge source system with duplicate requests.
     *
     * @param key
     */
    public void registerInterest(Object key,IDataReceiver<T> from);

    /**
     * Remove an interest
     *
     * @param key
     */
    public void removeInterest(Object key, IDataReceiver<T> from);
}
