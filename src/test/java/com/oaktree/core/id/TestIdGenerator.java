package com.oaktree.core.id;

import com.oaktree.core.id.IdGenerator;
import junit.framework.Assert;

import org.junit.Test;

import com.oaktree.core.utils.Text;

public class TestIdGenerator {
	@Test
	public void testPerformance() {
		IdGenerator i = new IdGenerator();
		i.setName("P1");
		i.setSubject("D");
		i.initialise();
		int TESTS = 1000000;
		
		long begin = System.nanoTime();
		for (int j = 0; j < TESTS; j++) {
			i.next();
		}
		long end = System.nanoTime();
		double duration = (end-begin)/1000000d;
		System.out.println("IdGenerator, " + TESTS + ": " + duration + " ms");	
	}
	private final static double EPSILON = 0.00000001;

	@Test
	public void testBasics() {
		IdGenerator i = new IdGenerator();
		i.setName("P1");
		i.setSubject("D");
		i.initialise();
		Assert.assertEquals(i.getName(),"P1");
		Assert.assertEquals(i.getSubject(),"D");
		
	}
	
	@Test
	public void testNext() {
		IdGenerator i = new IdGenerator("P1","D");
		i.initialise();
		String id = i.next();
		Assert.assertTrue(id.contains("P1"));
		Assert.assertTrue(id.contains("_D_"));
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] bits = id.split(Text.UNDERSCORE);
		Assert.assertEquals(bits.length,3,EPSILON);
		long time = Long.valueOf(bits[2]).longValue();
		System.out.print(time + ":" + System.currentTimeMillis());
		Assert.assertTrue(time < System.currentTimeMillis());
	}
	
	@Test
	public void testStart() {
		IdGenerator i = new IdGenerator();
		i.setName("P1");
		i.setSubject("D");
		i.setStart(0);
		i.initialise();
		String id = i.next();
		Assert.assertTrue(id.contains("P1"));
		Assert.assertTrue(id.contains("_D_"));
		String[] bits = id.split(Text.UNDERSCORE);
		Assert.assertEquals(bits.length,3,EPSILON);
		long time = Long.valueOf(bits[2]).longValue();
		Assert.assertEquals(time,1,EPSILON);
	}
}
