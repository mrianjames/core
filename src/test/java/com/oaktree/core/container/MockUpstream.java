package com.oaktree.core.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockUpstream extends AbstractComponent {
	private Logger logger = LoggerFactory.getLogger(MockUpstream.class.getName());
	public MockUpstream() {
		this.setName("MockUpstream");
		this.setComponentSubType("MOCK");
		this.setComponentType(ComponentType.UPSTREAM);
	}
	
	public void onMessage(IMessage message) {
		logger.info("UPSTREAM received messages");
	}
        
}
