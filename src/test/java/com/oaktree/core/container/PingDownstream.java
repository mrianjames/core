package com.oaktree.core.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component that receives a nanos timestamp to another component x and sends it straight off.
 * 
 * @author ij
 *
 */
public class PingDownstream extends AbstractComponent implements IComponent {
	
	private String contents;
	private String upstreamName;
	private IMessage msg;
	private final static Logger logger = LoggerFactory.getLogger(PingDownstream.class);

	public PingDownstream(String upstreamName) {
		this.upstreamName = upstreamName;
	}
	
	@Override
	public void start() {
		this.setState(ComponentState.AVAILABLE);
		logger.info("Waiting for messages");
		this.msg = MessageFactory.makeMessage(upstreamName, MessageType.APPLICATION, ComponentType.NAMED, upstreamName, "" , this, "");		
	}
	
	
	@Override
	public void onMessage(IMessage msg) {
//		if (logger.isTraceEnabled()) {
//			logger.trace("onMessage");
//		}
		if (contents != null) {
			contents = (String)msg.getMessageContents();
			msg.setMessageContents(contents);
		}
		this.messageListener.onMessage(this.msg);
	}
}
