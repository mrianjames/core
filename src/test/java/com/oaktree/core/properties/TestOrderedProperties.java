package com.oaktree.core.properties;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestOrderedProperties {

	@Test
	public void testEnumerate() {
		OrderedProperties props = new OrderedProperties();
		props.setProperty("A", "vala");
		props.setProperty("C", "valc");
		props.setProperty("B", "valb");
		Enumeration<Object> e = props.elements();
		List<Object> r = new ArrayList<Object>();
		while (e.hasMoreElements()) {
			Object key = e.nextElement();
			Object v = props.get(key);
			System.out.println("key: " + key + " value: " + v);
			r.add(key);
		}
		Assert.assertEquals("A",r.get(0));
		Assert.assertEquals("C",r.get(1));
		Assert.assertEquals("B",r.get(2));
	}
	
	@Test
	public void testFileLoad() {
		OrderedProperties props = new OrderedProperties();
		try {
			props.load(new FileReader("test.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertEquals(3,props.size());
		Enumeration<Object> e = props.elements();
		List<Object> r = new ArrayList<Object>();
		while (e.hasMoreElements()) {
			Object key = e.nextElement();
			Object v = props.get(key);
			System.out.println("key: " + key + " value: " + v);
			r.add(key);
		}
		Assert.assertEquals("A",r.get(0));
		Assert.assertEquals("C",r.get(1));
		Assert.assertEquals("B",r.get(2));
		
		props.list(System.out);
	}
	
	@Test
	public void testGetter() {
		OrderedProperties props = new OrderedProperties();
		props.setProperty("A", "vala");
		Assert.assertEquals("vala",props.get("A"));
	}
	
	@Test
	public void testPropGetter() {
		OrderedProperties props = new OrderedProperties();
		props.setProperty("A", "vala");
		Assert.assertEquals("vala",props.getProperty("A"));
	}
	
	@Test
	public void testPropGetterDefault() {
		OrderedProperties props = new OrderedProperties();
		props.setProperty("A", null);
		Assert.assertEquals("vala",props.getProperty("A","vala"));
	}
	
	@Test
	public void testSize() {
		OrderedProperties props = new OrderedProperties();
		Assert.assertEquals(0,props.size());
		props.setProperty("A", "vala");
		Assert.assertEquals(1,props.size());		
	}
	
	@Test
	public void testClear() {
		OrderedProperties props = new OrderedProperties();
		Assert.assertEquals(0,props.size());
		props.setProperty("A", "vala");
		Assert.assertEquals("vala",props.getProperty("A"));
		Assert.assertEquals(1,props.size());
		props.clear();
		Assert.assertEquals(0,props.size());		
	}
	
	@Test
	public void testIsEmpty() {
		OrderedProperties props = new OrderedProperties();
		Assert.assertTrue(props.isEmpty());
		props.setProperty("A", "vala");
		Assert.assertFalse(props.isEmpty());
	}
	
	@Test
	public void testRemove() {
		OrderedProperties props = new OrderedProperties();
		props.setProperty("A", "vala");
		Assert.assertEquals(1,props.size());
		props.remove("A");
		Assert.assertEquals(0,props.size());
	}
	
	@Test
	public void testContains() {
		OrderedProperties props = new OrderedProperties();
		Assert.assertFalse(props.containsKey("A"));
		Assert.assertFalse(props.containsValue("vala"));
		Assert.assertFalse(props.contains("vala"));
		props.setProperty("A", "vala");
		Assert.assertTrue(props.containsKey("A"));
		Assert.assertTrue(props.containsValue("vala"));
		Assert.assertTrue(props.contains("vala"));
	}
}
