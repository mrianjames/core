package com.oaktree.core.container.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.AbstractTask;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.container.ComponentType;
import com.oaktree.core.container.IComponent;
import com.oaktree.core.container.IDynamicComponent;
import com.oaktree.core.container.IMessage;
import com.oaktree.core.container.MessageFactory;
import com.oaktree.core.container.MessageType;

public class ExampleDynamicComponent extends AbstractTask implements IDynamicComponent {

	@Override
	public void onComponentStateChange(IComponent component,
			ComponentState oldState, ComponentState newState,
			String changeReason) {
		// TODO Auto-generated method stub

	}

	private static Logger logger = LoggerFactory.getLogger(ExampleDynamicComponent.class.getName());

	
	@Override
	public void onMessage(final IMessage message) {
		logger.info("TASK " + this.getName() + " has received: " + message.toString());
		if (message.getMessageContents().equals("REQUEST")) {
			this.messageListener.onMessage(MessageFactory.makeMessage(message.getDispatchId(),MessageType.APPLICATION, ComponentType.DOWNSTREAM, "XLON", "NEW", this, "Making request"));
		} 
		if (message.getMessageContents().equals("RESPONSE")) {
			this.messageListener.onMessage(MessageFactory.makeMessage(message.getDispatchId(),MessageType.APPLICATION, ComponentType.UPSTREAM, "FIX42", message, this, "Response back upstream"));
		} 
	}

}
