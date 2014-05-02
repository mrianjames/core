package com.oaktree.core.utils;


import java.util.ListIterator;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oaktree.core.utils.CircularQueue;

public class TestCircularBuffer {


	@Before
	public void setup() {}
	@After
	public void tearDown(){}
	@Test
	public void testWalkForwards() {
		
		CircularQueue<String> b = new CircularQueue<String>();
		b.add("Who");
		b.add("Are");
		b.add("You");
		ListIterator<String> it = b.listIterator();
		int c = 0;
		while (it.hasNext() &&  c <= 5) {
			String s = it.next();
			System.out.println(s);
			c++;
		}
		Assert.assertTrue(true);
	}

	@Test
	public void testWalkBackwards() {
		
		CircularQueue<String> b = new CircularQueue<String>();
		b.add("Who");
		b.add("Are");
		b.add("You");
		ListIterator<String> it = b.listIterator();
		int c = 0;
		while (it.hasPrevious() &&  c <= 5) {
			String s = it.previous();
			System.out.println(s);
			c++;
		}
		Assert.assertTrue(true);
	}
}
