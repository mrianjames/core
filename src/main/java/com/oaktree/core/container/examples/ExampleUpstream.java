package com.oaktree.core.container.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import com.oaktree.core.logging.Log;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.JavaTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.container.ComponentType;
import com.oaktree.core.container.IComponent;
import com.oaktree.core.container.IMessage;
import com.oaktree.core.container.MessageFactory;
import com.oaktree.core.container.MessageType;

/**
 * An example of an upstream that pumps messages into a system and times responses coming back.
 * Rate is controlled by TESTS field and is the number of messages to send per approximate time period specified
 * by the INTERVAL field. For 100 per second laptop records 0.04ms after full "warming" of container. 
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class ExampleUpstream extends AbstractComponent implements IComponent {

	static class MessagePumper extends TimerTask {
		private ExampleUpstream upstream;
		public MessagePumper(ExampleUpstream upstream) {
			this.upstream = upstream;
		}
		@Override
		public void run() {
			upstream.invoke();
		}
		
	}
	
	@Override
	public void onComponentStateChange(IComponent component,
			ComponentState oldState, ComponentState newState,
			String changeReason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(final IMessage message) {
		//logger.info("UPSTREAM onMessage: " + message.toString());
		latch.countDown();
	}
	
	private Logger logger = LoggerFactory.getLogger(ExampleUpstream.class.getName());

	private Timer timer = new Timer();
	
	public final static int INTERVAL = 1000;
	
	public void start() {
		timer.schedule(new MessagePumper(this), INTERVAL, INTERVAL);
		super.start();
		this.setState(ComponentState.AVAILABLE);
	}
	
	public void stop() {
		timer.cancel();
		super.stop();
	}
	
	public static final double TESTS = 200;
		
	private List<IMessage> makeMessages() {
		List<IMessage> messages = new ArrayList<IMessage>();
		boolean f = true;
		for (int i = 0; i < TESTS; i++) {
			messages.add(MessageFactory.makeMessage(f ? "VOD.L" : "BARC.L",MessageType.APPLICATION, ComponentType.DYNAMIC_COMPONENT_MANAGER, "XLON", "REQUEST", this, "REQUEST"));
			f = !f;
		}
		return messages;
	}
	
	long batchStart = 0;
	int batch = 0;
	CountDownLatch latch = new CountDownLatch(0);
    private ITime time = new JavaTime();
	public void invoke() {
		batchStart = time.getNanoTime();
		latch = new CountDownLatch((int)TESTS);
		for (IMessage message: makeMessages()) {
			this.messageListener.onMessage(message);
		}
		long end = time.getNanoTime();
		try {
			latch.await();
		} catch (Exception e) {
			Log.exception(logger,e);
		}
		long bend = time.getNanoTime();
		double pms = (((double)(bend-batchStart))/TESTS)/1000000;
		logger.info("Sending " + TESTS +" in batch " + batch + " took " + (((double)end-(double)batchStart)/1000000) + "ms. Received in " + format.format(((bend-batchStart)/1000000d)) + " ms, avg:" + format.format(pms) + " ms");
		batch++;
		
	}
	private DecimalFormat format = new DecimalFormat("#,###.##");
}
