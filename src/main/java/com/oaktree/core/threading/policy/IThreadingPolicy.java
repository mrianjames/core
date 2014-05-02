package com.oaktree.core.threading.policy;

import com.oaktree.core.container.IMessage;

/**
 * A policy for threading. Given a message resolve what key should be used
 * as a dispatch key to talk to a component.
 * 
 * @author ij
 *
 */
public interface IThreadingPolicy {
	
	/**
	 * For a given message return a possible key it should be dispatched on.
	 * @param msg - can be null where we are not dispatching IMessage objects.
	 * @return dispatch key for a given message.
	 */
	public String getDispatchKey(IMessage msg);
}
