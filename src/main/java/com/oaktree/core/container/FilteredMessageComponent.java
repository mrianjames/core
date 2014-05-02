package com.oaktree.core.container;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of a filterer of incoming messages that passes onto a message listener
 * for further processing. Examples of listeners:
 * * Storyboards
 * * State models
 * * Persistence mediums.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class FilteredMessageComponent extends AbstractComponent implements
		IFilteredMessageListener {

	/**
	 * message filtering level.
	 */
	private int level;
	
	/**
	 * The listener of messages that have been filtered.
	 */
	private IMessageListener listener;

	/**
	 * A list of sender component names we can filter away messages that come from them. 
	 */
	private Set<String> ignoreFromList = new HashSet<String>();

	/**
	 * Map of ignore messages that have a component type name and optional sub-type. 
	 * If the sub-type is null we want all messages for that component type to be ignored.
	 */
	private Map<String,String> ignoreToMap = new HashMap<String,String>();
	
	@Override
	public void ignoreMessagesFrom(String sender) {
		this.ignoreFromList.add(sender);
	}

	@Override
	public void ignoreMessagesTo(String componentType, String subtype) {
		ignoreToMap.put(componentType, subtype);
	}

	@Override
	public void setFilterLevel(int level) {
		this.level = level;
	}

	@Override
	public void setPublicationListener(IMessageListener listener) {
		this.listener = listener;
	}

	@Override
	public void onMessage(final IMessage message) {
		if (this.listener != null && message.getLevel() >= this.level) {
			IMessageSender sender = message.getMessageSender();
			if (sender == null) {
				return;
			}
			String name = sender.getName();
			if (!this.ignoreFromList.contains(name)) {
				/*
				 * with the target, if the component is registered as filter away we check the sub-type.
				 * if null then we want all messages filtered away; if it exists then we filter away only
				 * if we have an exact match with the sub-type on the message.
				 */
				if (this.ignoreToMap.containsKey(message.getTargetComponentType().name())) {
					String toComp = this.ignoreToMap.get(message.getTargetComponentType().name());
					if (toComp != null) {
						if (toComp.equals(message.getTargetSubtype())) {
							return;
						}
					} else {
						return;
					}
				}
				
				this.listener.onMessage(message);
			}
		}
	}

}
