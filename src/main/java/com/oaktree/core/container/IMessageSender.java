package com.oaktree.core.container;

public interface IMessageSender extends INameable, IMessageListener {
	/**
	 * Handle notification that a message has failed to be delivered to the desired component.
	 * @param message
	 * @param failure
	 */
	public void onMessageDeliveryFailure(IMessage message, MessageDeliveryFailure failure, String reason);
}
