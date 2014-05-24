package com.oaktree.core.utils;

import junit.framework.Assert;

import org.junit.Test;

import com.oaktree.core.time.ITimer;
import com.oaktree.core.time.UtilTimer;

public class TestUtilTimer {

	@Test
	public void testBasics() {
		UtilTimer timer = new UtilTimer();
		long midnight = timer.getMidnight();
		Assert.assertTrue(midnight>0);
		Assert.assertTrue(midnight<System.currentTimeMillis());
		long before = System.currentTimeMillis();
		try {
			Thread.sleep(100);
		} catch (Exception e) {}
		long now = timer.getNow();
		try {
			Thread.sleep(100);
		} catch (Exception e) {}
		long after = System.currentTimeMillis();
		Assert.assertTrue(now > before && now < after);
		try {
			Thread.sleep(100);
		} catch (Exception e) {}
		
		long time = timer.getTime();
		Assert.assertTrue(time+midnight > after);
	}
	
	@Test
	public void testTasks() {
		ITimer timer = new UtilTimer();
		final long start = System.currentTimeMillis();
		timer.schedule(new Runnable(){

			@Override
			public void run() {
				final long end = System.currentTimeMillis();
				Assert.assertTrue(end-start >= 100);
			}}, 100);
		
	}
	
}
