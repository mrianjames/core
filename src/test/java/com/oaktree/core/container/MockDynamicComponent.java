package com.oaktree.core.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockDynamicComponent extends AbstractTask implements IDynamicComponent {

	@Override
	public void start() {
		this.setState(ComponentState.AVAILABLE);
	}
	
	@Override
	public void onComponentStateChange(IComponent component,
			ComponentState oldState, ComponentState newState,
			String changeReason) {
		// TODO Auto-generated method stub

	}

	private static Logger logger = LoggerFactory.getLogger(MockDynamicComponent.class.getName());

	
	@Override
	public void onMessage(final IMessage message) {
		MockFixMessage msg = (MockFixMessage)(message.getMessageContents());

		logger.info("TASK " + this.getName() + " has received: " + message.toString());
		if (msg.msg.equals("REQUEST")) {			
			this.messageListener.onMessage(MessageFactory.makeMessage(null,MessageType.APPLICATION, ComponentType.DOWNSTREAM, "XLON", new MockFixMessage("XX2","REQUEST"), this, "Making request"));
		} 
		if (msg.msg.equals("RESPONSE")) {
			this.messageListener.onMessage(MessageFactory.makeMessage(null,MessageType.APPLICATION, ComponentType.UPSTREAM, "FIX42", new MockFixMessage(this.getName(),msg.msg), this, "Response back upstream"));
		} 
	}

    

}
