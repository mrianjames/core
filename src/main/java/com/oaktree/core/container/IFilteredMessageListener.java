package com.oaktree.core.container;


/**
 * A component that listens to messages, filters them and publishes them to some medium.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public interface IFilteredMessageListener extends IComponent {
	/**
	 * Set a filtering level that allows us to ignore messages that are too
	 * fine grained.
	 * @param level
	 * @return
	 */
	public void setFilterLevel(int level);
	
	/**
	 * Set a listener of the filtered messages.
	 * @param listener
	 */
	public void setPublicationListener(IMessageListener listener);

	/**
	 * Ignore all message from the name of a particular sender of the message.
	 * @sender
	 */

	public void ignoreMessagesFrom(String sender);
	
	/**
	 * Ignore all message to a particular component. If no sub-type is supplied (null) then
	 * we assume all messages to the component type will be ignored. 
	 * @param componenType
	 * @param subtype
	 */
	public void ignoreMessagesTo(String componenType, String subtype);
	
}
