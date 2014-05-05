package com.oaktree.core.container.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.container.ComponentType;
import com.oaktree.core.container.IComponent;
import com.oaktree.core.container.IMessage;
import com.oaktree.core.container.MessageFactory;
import com.oaktree.core.container.MessageType;

public class ExampleDownstream extends AbstractComponent implements IComponent {

	@Override
	public void onComponentStateChange(IComponent component,
			ComponentState oldState, ComponentState newState,
			String changeReason) {
		// TODO Auto-generated method stub

	}

	private Logger logger = LoggerFactory.getLogger(ExampleDownstream.class.getName());


	@Override
	public void onMessage(IMessage message) {
		logger.info("DOWNSTREAM " + this.getName() + " onMessage: " + message.toString());
		if (message.getMessageType().equals(MessageType.APPLICATION)) {
			if (message.getMessageContents().getClass() == String.class) {
				message.getMessageSender().onMessage(MessageFactory.makeMessage(message.getDispatchId(),MessageType.MESSAGE_ACK, ComponentType.TASK, message.getMessageSender().getName(), "RESPONSE", this, "Acking"));
			}
		}
	}
	
	public void start() {
		super.start();
		this.setState(ComponentState.AVAILABLE);
	}

}
