package com.oaktree.core.container;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class TestComponentManager {

	private ComponentManager componentManager;

	@Before
	public void setup() {
		this.componentManager = new ComponentManager();
	}
	
	public final static double EPSILON = 0.00000000001;
	
	@Test
	public void testBasics() {
		Assert.assertNull(this.componentManager.getName());
		this.componentManager.setName("A");
		Assert.assertEquals(this.componentManager.getName(),"A");
	}
	
	@Test
	public void testAddComponent() {
		testAdd(null);
		testAdd("BOO");
	}

	public void testAdd(String sub) {
		try {
			this.componentManager.addComponent(null);
			Assert.fail("Should have barfed");
		} catch (Exception e) {}
		
		Container con = new Container();
		try {
			this.componentManager.addComponent(con);
			Assert.fail("Should have barfed");
		} catch (Exception e) {}
		con.setName("BB");
		try {
			this.componentManager.addComponent(con);
			Assert.fail("Should have barfed");
		} catch (Exception e) {}
		con.setComponentType(ComponentType.CONTAINER, sub);
		con.setName("AAAA"+sub);
		this.componentManager.addComponent(con);
		Assert.assertNotNull(this.componentManager.getComponentsByCategory(con.getComponentType(), sub));
		Assert.assertEquals(this.componentManager.getComponentsByCategory(con.getComponentType(), sub).size(),1,EPSILON);
		Assert.assertEquals(this.componentManager.getComponentsByCategory(con.getComponentType(), sub).get(0),con);
	}

	/**
	 * Adding 2 components of the same name should fail.
	 * @param sub
	 */
	@Test
	public void testAddSameName() {
		Container con = new Container();
		
		con.setName("BB");		
		con.setComponentType(ComponentType.CONTAINER, "");
		this.componentManager.addComponent(con);
		try {
			this.componentManager.addComponent(con);
			Assert.fail("Should have barfed");
		} catch (Exception e) {
			
		}
		
		con.setDynamic(true);
		try {
			this.componentManager.addComponent(con);
			Assert.fail("Should have barfed");
		} catch (Exception e) {
			
		}
		
		con.setName("CHEESE");
		this.componentManager.addComponent(con);
		try {
			this.componentManager.addComponent(con);
			Assert.fail("Should have barfed");
		} catch (Exception e) {
			
		}
	}
	
	
	@Test
	public void testStart() {
		IComponent com = new MockDownstream();
		com.setName("BoozyMcguire");
		com.setComponentType(ComponentType.DOWNSTREAM, null);
		this.componentManager.addComponent(com);
		List<ComponentType> list = new ArrayList<ComponentType>();
		list.add(ComponentType.CONTAINER);
		this.componentManager.setStartupOrdering(list);
		this.componentManager.initialise();
		this.componentManager.start();
	}
	
	@Test
	public void testRemoveComponent() {
		testRemove(null);
		testRemove("");
	}
	
	public void testRemove(String sub) {
		IComponent comp = new MockDownstream();
		comp.setName("Bossis");
		comp.setComponentType(ComponentType.CONTAINER, sub);
		comp.start();
		Assert.assertEquals(comp.getState().haveStarted(), true);
		this.componentManager.addComponent(comp);
		
		Assert.assertEquals(this.componentManager.getNumComponents(),1,EPSILON);
		this.componentManager.removeComponent(comp, true);		
		Assert.assertEquals(this.componentManager.getNumComponents(),0,EPSILON);
		Assert.assertTrue(comp.getState().isStopped());
		Assert.assertNull(this.componentManager.getComponentsByCategory(comp.getComponentType(), sub));
		
		comp.start();
		Assert.assertEquals(comp.getState().haveStarted(), true);
		this.componentManager.addComponent(comp);		
		Assert.assertEquals(this.componentManager.getNumComponents(),1,EPSILON);
		this.componentManager.removeComponent(comp, false);		
		Assert.assertEquals(this.componentManager.getNumComponents(),0,EPSILON);
		Assert.assertFalse(comp.getState().isStopped());
	}
	
	@Test
	public void testGettingComponents() {
//		testGet(ComponentType.CONTAINER,null);
//		testGet(ComponentType.CONTAINER,"");
//		testGet(ComponentType.CONTAINER,"ALLY");
//		testGet(null,null);
//		testGet(ComponentType.NAMED,"ALLY");
//		testGet(ComponentType.NAMED,null);
	}
	
	@Test
	public void testMultipleComponentSubTypes() {
		this.componentManager.clear();
		IComponent a = new MockUpstream();
		a.setComponentType(ComponentType.UPSTREAM);
		a.setComponentSubType("XLON");
		a.setName("XLON_DS_1");
		//alias
		a.setAlternativeSubTypes(new String[]{"LONDON","GB"});
		
		this.componentManager.addComponent(a);
		
		Assert.assertNull(this.componentManager.getComponentByName("LONDON"));
		Assert.assertNull(this.componentManager.getComponentByCategory(ComponentType.UPSTREAM, null));
		Assert.assertNull(this.componentManager.getComponentByCategory(ComponentType.UPSTREAM, "X"));
		Assert.assertEquals(a,this.componentManager.getComponentByCategory(ComponentType.UPSTREAM, "XLON"));
		Assert.assertEquals(a,this.componentManager.getComponentByCategory(ComponentType.UPSTREAM, "LONDON"));
		Assert.assertEquals(a,this.componentManager.getComponentByCategory(ComponentType.UPSTREAM, "GB"));
		
		
	}
	
	@Test
	public void testAddDynamicComponent() {
			Container con = new Container();
			con.setName("BB");
			con.setDynamic(true);
			con.setComponentType(ComponentType.TASK);
			con.setComponentSubType("1234");
			try {
				this.componentManager.addComponent(con);
			} catch (Exception e) {}
			
	}
}
