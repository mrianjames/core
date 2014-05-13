package com.oaktree.core.data;

import com.oaktree.core.container.IComponent;

/**
 * Generic listener of data of type T.
 * 
 * @author ij
 *
 * @param <T> - data type. 
 */
public interface IDataReceiver<T> extends IComponent {
	
	/**
	 * Receive data.
	 * @param data
	 * @param from
	 * @param receivedTime
	 */
	public void onData(T data, IComponent from, long receivedTime);
}
