package com.oaktree.core.container;

import java.io.Serializable;

/**
 * Definition of a message that can be sent between components, either via a
 * container, or directly. Messages contain a pay-load (getMessageContents) that
 * contains anything you wish; the rest of the data is used by the
 * infrastructure to ensure correct routing to a suitable component. Messages
 * contain a level which can be used by components to quickly and configurably
 * filter messages. Messages also have a priority which can be used, if enabled,
 * by a possible dispatcher to use a prioritised queue to ensure that high
 * priority messages are processed sooner than low priority ones, while
 * maintaining sequentiality at the prioritisation level.
 * 
 * Messages should be uniquely identified in the system for the duration of the
 * life-span of that process. If messages are created using a standard factory
 * then creating unique ids for that time period should be taken care of (e.g.
 * MessageFactory). If you create your own messages then you should implement
 * your own message id creation.
 * 
 * <h2>Routing Messages</h2>
 * 
 * <h3>Sending a message to a categorised target</h3>
 * ========================================= Normal practice for a message is to
 * ask a message router e.g. a container to route a message to a categorised
 * target. If more than one target is found then the container will reduce the
 * options to only one using a pre-configured allocation policy (e.g. round
 * robin). To categorise a recipient of a message you should set the
 * targetComponentType (to anything other than NAMED) and optionally the
 * targetSubType to further reduce your options before filtering.
 * 
 * <h3>Sending a message to a specific target</h3>
 * ======================================== 
 * It is also possible to send messages to a named target. Set the targetComponentType to NAMED 
 * and the targetsubType to the name of the component you wish to receive the message. Note that 
 * sending messages directly will mean you do not benefit from container added.
 * 
 * @author Oak Tree Designs Ltd
 * 
 */
public interface IMessage extends Serializable {

	/**
	 * A standard priority of message.
	 */
	public static int LOW_PRIORITY = 0;

	/**
	 * A normal and default priority of message
	 */
	public static int NORMAL_PRIORITY = 5;

	/**
	 * A high priority.
	 */
	public static int HIGH_PRIORITY = 10;

	/**
	 * An id that this message should be dispatched on by an IDispatcher, if
	 * required to ensure this message is correctly sequenced to the receiver.
	 * 
	 * @return
	 */
	public String getDispatchId();

	/**
	 * Get the message level. This is used to filter messages that have a lower
	 * level than we want.
	 * 
	 * @return
	 */
	public int getLevel();

	/**
	 * Get the payload. This can be anything you want to send in the framework.
	 * 
	 * @return
	 */
	public Object getMessageContents();

	/**
	 * Get the message id, unique in the system.
	 * 
	 * @return
	 */
	public String getMessageId();

	/**
	 * Get the sender of this message; useful for sending responses back to.
	 * 
	 * @return
	 */
	public IMessageSender getMessageSender();

	/**
	 * Get the type of this message. Useful for receivers to easily route the
	 * message to a particular message handler without examining the payload.
	 * 
	 * @return
	 */
	public MessageType getMessageType();

	/**
	 * Get the priority that this message was sent with. The higher the priority
	 * the more important it is and the quicker it may be processed in a
	 * sequential message queue.
	 * 
	 * @return
	 */
	public int getPriority();

	/**
	 * Get the textual reason we are sending this message. For information
	 * purposes.
	 * 
	 * @return
	 */
	public String getReason();

	/**
	 * Get the type of component we are targeting as a reason.
	 * 
	 * @return
	 */
	public ComponentType getTargetComponentType();

	public String getTargetSubtype();

	public void setDispatchId(String key);

	public void setLevel(int level);

	public void setMessageContents(Object msg);

	public void setMessageId(String id);

	public void setMessageSender(IMessageSender sender);

	public void setMessageType(MessageType type);

	public void setPriority(int priority);

	public void setReason(String reason);

	public void setSubtype(String subtype);

	public void setTargetComponentType(ComponentType type);

	public long getTransactTime();

	public void setTransactTime(long t);

	/**
	 * Get a textual debug string for this message.
	 * 
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Get the flag that indicates that we can send this message to an unavailable target.
	 * @return
	 */
	public boolean isSendToUnavailableTarget();
	
	/**
	 * Set a flag to say this message can be delivered to unavailable components. 
	 * For example, we may want to return a confirmation of completion to a 
	 * stopping component.
	 * @param send
	 */
	public void setSendToUnavailableTarget(boolean send);
}
