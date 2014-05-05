package com.oaktree.core.network.multicast;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.network.INetworkMessageListener;

public class TestMulticast implements INetworkMessageListener {

	private final static Logger logger = LoggerFactory.getLogger(TestMulticast.class.getName());
	private List<byte[]> bmsgs = new ArrayList<byte[]>();
	private List<Object> omsgs = new ArrayList<Object>();
	private MulticastSender sender;
	private MulticastReceiver receiver;
	private static class Msg implements Serializable {
		public String msg;
		public Msg(String msg) {
			this.msg = msg;
		}
	}
	
	@Before
	public void setup() {
		bmsgs.clear();
		omsgs.clear();
		
		this.receiver = new MulticastReceiver();
		receiver.setNetworkMessageListener(this);
		receiver.setAddress("239.0.0.1");
		receiver.setSerialize(false);
		receiver.setPort(1234);
		receiver.initialise();
        receiver.start();

		
		this.sender = new MulticastSender();
		sender.setAddress("239.0.0.1");
		sender.setPort(1234);
		sender.initialise();
		sender.start();
	}
	
	private final static double EPSILON = 0.0000000000001;
	
	@Test
	@Ignore
	public void testMulticastSendingAndReceiving() {		
		try {
			sender.send("HELLO".getBytes());
			Thread.sleep(100);
			Assert.assertEquals(this.bmsgs.size(),1,EPSILON);
			Assert.assertEquals("HELLO",new String(this.bmsgs.get(0)));
			
			sender.send("HELLO".getBytes());
			Thread.sleep(100);
			Assert.assertEquals(this.bmsgs.size(),2,EPSILON);
			Assert.assertEquals("HELLO",new String(this.bmsgs.get(1)));
			
			this.receiver.setSerialize(true);
			Msg msg = new Msg("HELLO");
			sender.sendObject(msg);
			Thread.sleep(100);
			Assert.assertEquals(this.omsgs.size(),1,EPSILON);
			Assert.assertEquals(msg.msg,((Msg)(this.omsgs.get(0))).msg);
		} catch (Exception e) {
			if (!e.getMessage().contains("No such device")) {
                Assert.fail(e.getMessage());
            }
		}
		
	}
	
	@Test
    @Ignore
	public void testPerformance() {
		try {
			sender.send("HELLO1".getBytes());
			Thread.sleep(1000);
			sender.send("HELLO2".getBytes());
			Thread.sleep(1000);
			sender.send("HELLO3".getBytes());
			Thread.sleep(1000);
			sender.send("HELLO4".getBytes());
			Thread.sleep(1000);
			sender.send("HELLO5".getBytes());
			Thread.sleep(1000);
			sender.send("HELLO6".getBytes());
			Thread.sleep(1000);
			sender.send("HELLO7".getBytes());
			Thread.sleep(1000);
			sender.send("HELLO8".getBytes());
			Thread.sleep(1000);
			
		} catch (Exception e) {
			if (!e.getMessage().contains("No such device")) {
                Assert.fail(e.getMessage());
            }
		}
		
	}

	@Override
	public void receive(byte[] message) {
		logger.info("Incoming: " + new String(message));
		bmsgs.add(message);
	}

	@Override
	public void receive(Object object) {
		logger.info("Incoming: " + object);
		omsgs.add(object);
	}
	
	public static void main(String[] args) {
		
	}
	
}
