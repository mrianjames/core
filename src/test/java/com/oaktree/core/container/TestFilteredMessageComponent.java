package com.oaktree.core.container;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Test filtering of component targets.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class TestFilteredMessageComponent implements IMessageSender,IMessageListener {

	private FilteredMessageComponent comp;
	private List<IMessage> messages = new ArrayList<IMessage>();
	@Before
	public void setup() {
		this.messages.clear();
		this.comp = new FilteredMessageComponent();
		comp.setName("TEST");
		comp.initialise();
		comp.setMessageListener(this);
		comp.setPublicationListener(this);
		comp.start();

	}
	private double EPSILON = 0.0000001;
	
	@Test
	public void testIgnoreMessagesTo() {
		comp.ignoreMessagesTo(ComponentType.DOWNSTREAM.name(), "XLON");
		IMessage message = MessageFactory.makeMessage("A", MessageType.APPLICATION, ComponentType.DOWNSTREAM, "XLON","Hello", this, "I want to");
		comp.onMessage(message);
		Assert.assertEquals(this.messages.size(),0,EPSILON);
	}
	
	@Test
	public void testIgnoreMessagesFrom() {
		comp.ignoreMessagesFrom(this.getName());
		IMessage message = MessageFactory.makeMessage("A", MessageType.APPLICATION, ComponentType.DOWNSTREAM, "XLON","Hello", this, "I want to");
		comp.onMessage(message);
		Assert.assertEquals(this.messages.size(),0,EPSILON);
	}

	@Test
	public void testIgnoreMessagesFromPerformance() {
		comp.ignoreMessagesFrom(this.getName());
		List<IMessage> msgs = new ArrayList<IMessage>();
		int TESTS = 100000;
		for(int i = 0; i < TESTS; i++) {
			msgs.add(MessageFactory.makeMessage(""+i, MessageType.APPLICATION, ComponentType.DOWNSTREAM, "XLON","Hello", this, "I want to"));
		}
		long s = System.nanoTime();
		for(int i = 0; i < TESTS; i++) {
			comp.onMessage(msgs.get(i));
		}
		long e = System.nanoTime();
		double d = ((double)(e-s))/1000000;
		System.out.println("D: " + d);
		Assert.assertEquals(this.messages.size(),0,EPSILON);
	}
	
	@Test
	public void testIgnoreMessagesToNoSubtype() {
		comp.ignoreMessagesTo(ComponentType.DOWNSTREAM.name(), null);
		IMessage message = MessageFactory.makeMessage("A", MessageType.APPLICATION, ComponentType.DOWNSTREAM, "XLON","Hello", this, "I want to");
		comp.onMessage(message);
		Assert.assertEquals(this.messages.size(),0,EPSILON);
	}
	
	@Test
	public void testNoFiltering() {
		IMessage message = MessageFactory.makeMessage("A", MessageType.APPLICATION, ComponentType.DOWNSTREAM, "XLON","Hello", this, "I want to");
		comp.onMessage(message);
		Assert.assertEquals(this.messages.size(),1,EPSILON);
	}

	@Test
	public void testDodgyToFilter() {
		comp.ignoreMessagesTo(ComponentType.DOWNSTREAM.name(), "");
		comp.ignoreMessagesTo(ComponentType.DOWNSTREAM.name(), null);
		comp.ignoreMessagesTo(null, "XLON");
	}
	
	@Override
	public void onMessage(IMessage message) {
		this.messages.add(message);
	}

	@Override
	public String getName() {
		return "Borris";
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessageDeliveryFailure(IMessage message,
			MessageDeliveryFailure failure, String rsn) {
		// TODO Auto-generated method stub
		
	}
	
}
