package com.oaktree.core.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component that sends a nanos timestamp to another component x and sends it again when
 * the target responds. this ping-pong x times is timed and an avg posted.
 * 
 * @author ij
 *
 */
public class PingUpstream extends AbstractComponent implements IComponent {
	
	public PingUpstream(int tests) {
		this.tests = tests;
	}
	private int tests;
	private int done;
	private IMessage msg;
	private long endTime;
	private long startTime;
	private final static Logger logger = LoggerFactory.getLogger(PingUpstream.class);

	@Override
	public void start() {
		this.setState(ComponentState.AVAILABLE);
		logger.info("Starting test of " + tests + " messages");
		this.msg = MessageFactory.makeMessage("TEST", MessageType.APPLICATION, ComponentType.DOWNSTREAM, "TEST", "" , this, "");
		this.startTime = System.nanoTime();
		this.msg.setMessageContents("" + startTime);
		this.messageListener.onMessage(this.msg);
	}
	
	@Override
	public void stop() {
		logger.info("Stopping at " + this.endTime);
		long duration = endTime - this.startTime;
		duration /= 1000;
		double avg = ((double)duration)/this.tests;
		logger.info("Avg latency: " + avg + " us - owl: " + (avg/2) + "us TESTS: " + this.done);
		//System.exit(0);		
	}
	
	@Override
	public void onMessage(IMessage msg) {
//		if (logger.isTraceEnabled()) {
//			logger.trace("onMessage");
//		}

		if (done == tests) {
			this.endTime = System.nanoTime();
			this.stop();
			return;
		}
		done++;
		this.messageListener.onMessage(this.msg);
	}
}
