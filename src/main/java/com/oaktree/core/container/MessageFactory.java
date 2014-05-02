package com.oaktree.core.container;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default manufacturer of messages. This implementation provides a basic id generation mechanism using
 * a counter.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class MessageFactory {
	private static AtomicInteger counter = new AtomicInteger(0);
	private static String base = "";
		
	/**
	 * Make a message; we will assign and id.
	 * @param type
	 * @param target
	 * @param subtype
	 * @param message
	 * @param sender
	 * @param reason
	 * @return
	 */
	public static IMessage makeMessage(final String dispatchId,final MessageType type, final ComponentType target, final String subtype, final Object message, final IMessageSender sender, String reason) {
		return makePrioritisedMessage(dispatchId, type, target, subtype, message, sender, reason, IMessage.NORMAL_PRIORITY);
	}

	/**
	 * Make a message with a particular priority.
	 * @param dispatchId
	 * @param type
	 * @param target
	 * @param subtype
	 * @param message
	 * @param sender
	 * @param reason
	 * @param priority
	 * @return
	 */
	public static IMessage makePrioritisedMessage(final String dispatchId,final MessageType type, final ComponentType target, final String subtype, final Object message, final IMessageSender sender, String reason, int priority) {
		String id = MessageFactory.getId();
		return new Message(dispatchId,id,type,target,subtype,message,sender,reason,priority);
	}

        public static String getId() {
            return base + counter.incrementAndGet();
        }
}
