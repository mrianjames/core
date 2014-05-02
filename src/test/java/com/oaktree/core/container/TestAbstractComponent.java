package com.oaktree.core.container;

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.oaktree.core.utils.Text;

/**
 * Test common component functionality.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class TestAbstractComponent extends AbstractComponent {

	@Before
	public void setup() {
		this.setName("IO");
		//this.setComponentType(ComponentType.CONTAINER, null);
	}
	
	@Test
	public void testComponentType() {
		Assert.assertEquals(this.getComponentType(),null);
		Assert.assertEquals(this.getComponentSubType(),null);
		this.setComponentType(ComponentType.UPSTREAM, "XX");
		Assert.assertEquals(this.getComponentType(),ComponentType.UPSTREAM);
		Assert.assertEquals(this.getComponentSubType(),"XX");
		this.setComponentType(null, null);
	}
	
	@Test
	public void testDynamic() {
		Assert.assertFalse(this.isDynamic());
		this.setDynamic(true);
		Assert.assertTrue(this.isDynamic());
	}
	
	@Test
	public void testPauseResume() {
		Assert.assertFalse(this.isPaused());
		this.pause();
		Assert.assertTrue(this.isPaused());
		this.resume();
		Assert.assertFalse(this.isPaused());
	}
	
	@Test(timeout=1000)
	public void testComponentListener() {
		final CountDownLatch cd = new CountDownLatch(1);
		IComponentListener listener = new IComponentListener() {

			@Override
			public void onComponentStateChange(IComponent component,
					ComponentState oldState, ComponentState newState,
					String changeReason) {
				Assert.assertEquals(component,TestAbstractComponent.this);
				Assert.assertEquals(newState,ComponentState.STARTED);
				Assert.assertEquals(oldState,ComponentState.STARTING);
				Assert.assertEquals(changeReason,"TEST");
				cd.countDown();
			}			
		};
		this.setState(ComponentState.STARTING);
		this.setState(ComponentState.STARTED);
		this.addComponentListener(listener);
		this.notifyListeners("TEST");
		try {
			cd.await();
		} catch (InterruptedException e) {
		}
	}
	
	@Test
	public void testState() {
		this.setState(ComponentState.AVAILABLE);
		Assert.assertEquals(this.getState(),ComponentState.AVAILABLE);		
	}
	
	@Test
	public void testNaming() {
		this.setName("Mavis");
		Assert.assertEquals(this.getName(),"Mavis");
	}
	
	@Test
	public void testType() {
		this.setComponentType(ComponentType.DOWNSTREAM, "XLON");
		Assert.assertEquals(this.getComponentType(),ComponentType.DOWNSTREAM);
		Assert.assertEquals(this.getComponentSubType(),"XLON");
		
	}
	
	@Override
	public void onComponentStateChange(IComponent component,
			ComponentState oldState, ComponentState newState,
			String changeReason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(IMessage message) {
		
	}

	@Test
	public void testToString() {
		IComponent c = new Container();
		Assert.assertEquals(c.toString(),Text.COLON + Text.COLON);
		c.setName("BOO");
		Assert.assertEquals(c.toString(),Text.COLON + Text.COLON + "BOO");
		c.setComponentType(ComponentType.CONTAINER);
		Assert.assertEquals(c.toString(),"CONTAINER" + Text.COLON+ Text.COLON + "BOO");
		c.setComponentSubType("CHEESE");
		Assert.assertEquals(c.toString(),"CONTAINER" + Text.COLON+ "CHEESE" + Text.COLON + "BOO");
	}
	
	@Test
	public void testSetComponentType() {
		IComponent c = new Container();
		Assert.assertEquals(c.getComponentType(),null);
		c.setComponentType(ComponentType.CONTAINER);
		Assert.assertEquals(c.getComponentType(),ComponentType.CONTAINER);
	}
	
	@Test
	public void testSetComponentSubType() {
		IComponent c = new Container();
		Assert.assertEquals(c.getComponentSubType(),null);
		c.setComponentSubType("CHEESE");
		Assert.assertEquals(c.getComponentSubType(),"CHEESE");
	}

        @Test
        public void testEqualsNoName() {
            IComponent c = new Container();
            //c.setName(null);
            IComponent d = new Container();
            //c.setName(null);
            Assert.assertTrue(c.equals(d));
        }
}
