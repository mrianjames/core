package com.oaktree.core.container;

import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import com.oaktree.core.threading.policy.ComponentNameThreadingPolicy;
import com.oaktree.core.threading.policy.IThreadingPolicy;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * 
 * A very simple rig of a container with an upstream, a downstream and a strategy utilising stock components
 * in our new framework. It is used to show how easy it is to assemble a working application. 
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class TestContainer implements IMessageSender {

	private IContainer con;
	private IDispatcher dispatcher;
	private MockDownstream downstream;
	private MockDownstream downstream2;
	private List<IMessage> msgs = new ArrayList<IMessage>();
	private List<MessageDeliveryFailure> errors = new ArrayList<MessageDeliveryFailure>();
	private List<String> errorMessages = new ArrayList<String>();
	
	@Before
	public void setup() {
		msgs.clear();
		errors.clear();
		errorMessages.clear();
		IComponentManager manager = new ComponentManager();
		manager.setComponentType(ComponentType.SERVICE, "ComponentManager");
		manager.setName("ComponentManager");
		manager.setWaitBetweenStartupTypes(false);
		
		this.con = new Container();
		con.setComponentManager(manager);
		con.setName("TestContainer");
		con.setComponentType(ComponentType.CONTAINER, "");
		manager.setMessageListener(con);
		
		IDynamicComponentFactory dcFactory = new MockDynamicComponentFactory();
        IThreadingPolicy strategyPolicy = new ComponentNameThreadingPolicy(dcFactory);
        manager.setDynamicComponentFactory(dcFactory);
		manager.setThreadingPolicy(strategyPolicy);
		
		this.dispatcher = new ThroughputDispatcher();
		dispatcher.setName("ThroughputDispatcher");
		dispatcher.setThreads(2);
		dispatcher.start();
		con.setDispatcher(dispatcher);


		IComponent upstream = new MockUpstream();
        IThreadingPolicy cnamepolicy = new ComponentNameThreadingPolicy(upstream);
        upstream.setName("FIX42");
		upstream.setComponentType(ComponentType.UPSTREAM, "FIX42");
		upstream.setMessageListener(con);
		upstream.setThreadingPolicy(cnamepolicy);
		upstream.initialise();
		upstream.start();
		upstream.setState(ComponentState.AVAILABLE);
		
		this.downstream = new MockDownstream();
        cnamepolicy = new ComponentNameThreadingPolicy(downstream);
        downstream.setName("XLON_DownstreamPrimary");
		downstream.setComponentType(ComponentType.DOWNSTREAM, "XLON");
		downstream.setMessageListener(con);
		downstream.setPriority(AbstractComponent.SECONDARY);
		downstream.setThreadingPolicy(cnamepolicy);
		
		this.downstream2 = new MockDownstream();
        cnamepolicy = new ComponentNameThreadingPolicy(downstream2);
		downstream2.setName("XLON_DownstreamBackup");
		downstream2.setComponentType(ComponentType.DOWNSTREAM, "XLON");
		downstream2.setMessageListener(con);
		downstream2.setPriority(AbstractComponent.PRIMARY);
		downstream2.setThreadingPolicy(cnamepolicy);
		
		con.setComponentFilter(new RankingComponentFilter());
		
		IFilteredMessageListener logger = new FilteredMessageComponent();
		logger.setFilterLevel(Level.FINEST.intValue());
		logger.setComponentType(ComponentType.SERVICE, "LOGGER");
		logger.setName("Logger");
		//logger.setPublicationListener(l);
		logger.ignoreMessagesTo(ComponentType.DOWNSTREAM.name(),null);
		con.addMessageProcessor(logger);
		
		//con.getComponentManager().addComponent(upstream);
		con.getComponentManager().addComponent(downstream);
		con.getComponentManager().addComponent(upstream);
		con.getComponentManager().addComponent(downstream2);
		con.getComponentManager().addComponent(logger);
		
		con.initialise();

	}
	
	@Test
	public void testAddComponentListener() {
		final List<ComponentState> states = new ArrayList<ComponentState>();
		this.con.addComponentListener(new IComponentListener(){

			@Override
			public void onComponentStateChange(IComponent component,
					ComponentState oldState, ComponentState newState,
					String changeReason) {
				if (component.equals(TestContainer.this.con)) {
					System.out.println(oldState +"->" + newState +" cos: " + changeReason);
				}
				states.add(newState);
			}});
		this.con.start();
		Assert.assertEquals(ComponentState.STARTING, states.get(0));
		Assert.assertEquals(ComponentState.STARTED, states.get(1));
		
		try {
			this.con.addComponentListener(null);
			Assert.fail("Should have barfed");
		} catch (Exception e) {}	
	}
	
	
	@Test
	public void testDispatcher() {
		Assert.assertEquals(this.dispatcher,this.con.getDispatcher());
		this.con.setDispatcher(null);
		Assert.assertEquals(this.con.getDispatcher(),null);
		this.con.setDispatcher(this.dispatcher);
		Assert.assertEquals(this.dispatcher,this.con.getDispatcher());
		
	}

	@Test
	public void testSendMessageToUnavailableComponent() {
		
		downstream2.setState(ComponentState.AVAILABLE);
		IMessage msg = new Message("XXX","XX1",MessageType.APPLICATION,ComponentType.NAMED,"XLON_DownstreamBackup",null,this,"",IMessage.NORMAL_PRIORITY);
		this.con.onMessage(msg);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}
		downstream2.setState(ComponentState.STOPPING);
		msg = new Message("XXX","XX2",MessageType.APPLICATION,ComponentType.NAMED,"XLON_DownstreamBackup",null,this,"",IMessage.NORMAL_PRIORITY);
		this.con.onMessage(msg);
		msg = new Message("XXX","XX3",MessageType.APPLICATION,ComponentType.NAMED,"XLON_DownstreamBackup",null,this,"",IMessage.NORMAL_PRIORITY);
		msg.setSendToUnavailableTarget(true);
		this.con.onMessage(msg);
	}
	
	@Test
	public void testSendMessageToNamedComponent() {
		downstream.setState(ComponentState.AVAILABLE);
		downstream2.setState(ComponentState.AVAILABLE);
		IMessage msg = new Message("XXX","XX1",MessageType.APPLICATION,ComponentType.NAMED,"XLON_DownstreamBackup",null,this,"",IMessage.NORMAL_PRIORITY);
		this.con.onMessage(msg);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}
		Assert.assertTrue(downstream2.getMessages().size() == 1);
		Assert.assertEquals(downstream2.getReceiveThreadName(),"XLON_DownstreamBackup");
	}

	@Test
	public void testRouteToDynamicComponentFactory() {
		downstream.setState(ComponentState.AVAILABLE);
		downstream2.setState(ComponentState.UNAVAILABLE);
		IMessage msg = new Message("XXX","XX1",MessageType.APPLICATION,ComponentType.DYNAMIC_COMPONENT_MANAGER,null,new MockFixMessage("XXX","REQUEST"),this,"",IMessage.NORMAL_PRIORITY);
		this.con.onMessage(msg);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {			
		}
		IMessage response = this.downstream.getMessages().get(0);
		Assert.assertEquals(downstream.getReceiveThreadName(),"XLON_DownstreamPrimary");
		Assert.assertNotNull(response);
		Assert.assertTrue(response.getMessageSender() instanceof IDynamicComponent);
		
	}
	
	@Test
	public void testRouteToAvailableComponent() {
		downstream.setState(ComponentState.AVAILABLE);
		downstream2.setState(ComponentState.UNAVAILABLE);
		IMessage msg = new Message("XXX","XX1",MessageType.APPLICATION,ComponentType.DOWNSTREAM,"XLON",new MockFixMessage("XXX","REQUEST"),this,"",IMessage.NORMAL_PRIORITY);
		this.con.onMessage(msg);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {			
		}
		//IMessage response = this.msgs.get(0);
		List<IMessage> msgs = downstream.getMessages();
		Assert.assertEquals(downstream.getReceiveThreadName(),"XLON_DownstreamPrimary");
		Assert.assertEquals(msgs.size(),1,0.000000001);
		Assert.assertEquals(msgs.get(0),msg);
		
	}

	@Override
	public void setName(String name) {}

	@Override
	public String getName() {
		return "TEST";
	}

	@Override
	public void onMessage(IMessage message) {
		msgs.add(message);
	}

	@Override
	public void onMessageDeliveryFailure(IMessage message, MessageDeliveryFailure failure, String reason) {
		this.errors.add(failure);
		this.errorMessages.add(reason);
	}
}
