package com.oaktree.core.container;

/**
 * Specification of an entity that wants to listen to messages.
 * @author Oak Tree Designs Ltd
 *
 */
public interface IMessageListener {
	/**
	 * Handle a notification of a message. A message can be anything configured in the system
	 * of a specified message type. Tasks will then break that down into their own handlers
	 * for each type.
	 * 
	 * @param message 
	 */
	public void onMessage(IMessage message);
}
