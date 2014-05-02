package com.oaktree.core.container;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockDownstream extends AbstractComponent implements IComponent {

	private String receiveThreadName = "UNKNOWN";
	private List<IMessage> incoming = new ArrayList<IMessage>();
	@Override
	public void onComponentStateChange(IComponent component,
			ComponentState oldState, ComponentState newState,
			String changeReason) {
		// TODO Auto-generated method stub

	}
	public List<IMessage> getMessages() {
		return incoming;
	}

	private Logger logger = LoggerFactory.getLogger(MockDownstream.class.getName());


	@Override
	public void onMessage(IMessage message) {
		
		this.receiveThreadName = Thread.currentThread().getName();
		incoming.add(message);
		logger.info("DOWNSTREAM " + this.getName() + " onMessage: " + message.toString());
		MockFixMessage tfm = (MockFixMessage)(message.getMessageContents());
		String id  = tfm.id;
		if (message.getMessageType().equals(MessageType.APPLICATION)) {
			if (tfm.msg != null) {
				IMessageSender sender = message.getMessageSender();
				MockFixMessage ack = new MockFixMessage(id,"RESPONSE");
				this.messageListener.onMessage(MessageFactory.makeMessage(null,MessageType.APPLICATION, ComponentType.NAMED, sender.getName(), ack, this, "Acking"));
			}
		}
	}
	
	public String getReceiveThreadName() {
		return receiveThreadName;
	}
	public void start() {
		super.start();
		this.setState(ComponentState.AVAILABLE);
		incoming.clear();
	}

}
