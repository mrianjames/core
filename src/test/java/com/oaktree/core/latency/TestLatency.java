package com.oaktree.core.latency;

import java.io.StringWriter;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test harness for latency tracking.
 *
 */
public class TestLatency {

	private final static double EPSILON = 200;
	
	@Test
	public void testFileLatencyRecorderFactory() {
		System.out.println(System.getProperty("user.dir"));
		FileLatencyRecorderFactory lrf = new FileLatencyRecorderFactory();
		lrf.setBase(System.getProperty("user.dir"));
		lrf.initialise();
		ILatencyRecorder recorder = lrf.make(TestLatency.class.getName());
		Assert.assertNotNull(recorder);
		Assert.assertTrue(recorder instanceof LatencyRecorder);
		LatencyRecorder rec = (LatencyRecorder)(recorder);
		Assert.assertEquals(rec.getWriteDelay(),lrf.getWriteDelay(),EPSILON);
		Assert.assertEquals(rec.getWriteDelay(), 300,EPSILON);
		Assert.assertEquals(rec.getBufferSize(),lrf.getBufferSize(),EPSILON);
		Assert.assertEquals(rec.getBufferSize(), 200000,EPSILON);
		Assert.assertTrue(rec.isActive());
		Assert.assertFalse(rec.isPaused());
	}

	
	@Test
	public void testLatencyRecorderManager() {
		FileLatencyRecorderFactory lrf = new FileLatencyRecorderFactory();
		System.out.println("UserDir:" + System.getProperty("user.dir"));
		lrf.setBase(System.getProperty("user.dir"));
		lrf.initialise();
		
		LatencyRecorderManager.getInstance().setLatencyRecorderFactory(lrf);
		ILatencyRecorder recorder = LatencyRecorderManager.getInstance().getLatencyRecorder(TestLatency.class.getName());
		Assert.assertNotNull(recorder);
		LatencyRecorder rec = (LatencyRecorder)(recorder);
		Assert.assertEquals(rec.getWriteDelay(),lrf.getWriteDelay(),EPSILON);
		Assert.assertEquals(rec.getWriteDelay(), 300,EPSILON);
		Assert.assertEquals(rec.getBufferSize(),lrf.getBufferSize(),EPSILON);
		Assert.assertEquals(rec.getBufferSize(), 200000,EPSILON);
		Assert.assertTrue(rec.isActive());
		Assert.assertFalse(rec.isPaused());
		
		rec.begin("OO", "AA", "100", 100);
		rec.begin("OO", "AA", "100", 101);
		
	}
	
	@Test
	public void testLatencyRecorder() {
		int bs = 3;
		StringWriter sw = new StringWriter();
		ILatencyWriter w = new IOLatencyWriter(sw,bs);
		LatencyRecorder recorder = new LatencyRecorder();
		recorder.setBufferSize(bs);
		recorder.setName("Penguin");
		recorder.initialise();
		recorder.setWriter(w);
		recorder.start();
//		System.out.println("Buffer: "+ sw.getBuffer());
		recorder.begin("X", "Y", "1000", 100);
		try {Thread.sleep(500);} catch (Exception e) {}
		String str = sw.getBuffer().toString();
		Assert.assertNotNull(str);
		//System.out.println("Buffer: "+ str);
		Assert.assertTrue(str.length() > 0);
		Assert.assertTrue(str.length() <100);
		sw.getBuffer().delete(0, sw.getBuffer().length());
		recorder.beginAt("X", "Y", "2000", 200,System.nanoTime());
		try {Thread.sleep(500);} catch (Exception e) {}
		str = sw.getBuffer().toString();
		Assert.assertNotNull(str);
//		System.out.println("Buffer: "+ str);
		Assert.assertTrue(str.length() > 0);
		Assert.assertTrue(str.length() <100);
		
		sw.getBuffer().delete(0, sw.getBuffer().length());
		recorder.beginAt("X", "Y", "3000", 300,System.nanoTime());
		try {Thread.sleep(500);} catch (Exception e) {}
		str = sw.getBuffer().toString();
		Assert.assertNotNull(str);
//		System.out.println("Buffer: "+ str);
		Assert.assertTrue(str.length() > 0);
		Assert.assertTrue(str.length() <100);
		
		sw.getBuffer().delete(0, sw.getBuffer().length());
		recorder.beginAt("X", "Y", "4000", 400,System.nanoTime());
		try {Thread.sleep(500);} catch (Exception e) {}
		str = sw.getBuffer().toString();
		Assert.assertNotNull(str);
//		System.out.println("Buffer: "+ str);
		Assert.assertTrue(str.length() > 0);
		Assert.assertTrue(str.length() <100);
				
		try
		{
			Thread.sleep(300);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		 * test stop/start
		 */
		recorder.stop();
		Assert.assertFalse(recorder.isActive());
		sw.getBuffer().delete(0, sw.getBuffer().length());
		recorder.beginAt("X", "Y", "4000", 400,System.nanoTime());
		try {Thread.sleep(500);} catch (Exception e) {}
		str = sw.getBuffer().toString();
		Assert.assertEquals(str,"");
		recorder.start();
		Assert.assertTrue(recorder.isActive());
		sw.getBuffer().delete(0, sw.getBuffer().length());
		recorder.beginAt("X", "Y", "4000", 400,System.nanoTime());
		try {Thread.sleep(500);} catch (Exception e) {}
		str = sw.getBuffer().toString();
		Assert.assertTrue(str.length() > 0);
		
		/*
		 * Test pausing/resuming.
		 */
		recorder.pause();
		Assert.assertFalse(recorder.isActive());
		sw.getBuffer().delete(0, sw.getBuffer().length());
		recorder.beginAt("X", "Y", "4000", 400,System.nanoTime());
		try {Thread.sleep(500);} catch (Exception e) {}
		str = sw.getBuffer().toString();
		Assert.assertEquals(str,"");
		recorder.resume();
		Assert.assertTrue(recorder.isActive());
		sw.getBuffer().delete(0, sw.getBuffer().length());
		recorder.beginAt("X", "Y", "4000", 400,System.nanoTime());
		try {Thread.sleep(500);} catch (Exception e) {}
		str = sw.getBuffer().toString();
		Assert.assertTrue(str.length() > 0);
		
	}

	@Test
	public void testShutOff() {
		int bs = 1000;
		StringWriter sw = new StringWriter();
		ILatencyWriter w = new IOLatencyWriter(sw,bs);
		LatencyRecorder recorder = new LatencyRecorder();
		recorder.setBufferSize(bs);
		recorder.setName("Penguin");
		recorder.setPauseIfMaxBufferExceeded(true);
		recorder.initialise();
		recorder.setWriter(w);
		recorder.start();
		for (int i = 0; i < 20003; i++) {
			recorder.begin("X", "Y", "1000", 100+i);
		}
		Assert.assertFalse(recorder.isActive());
		Assert.assertTrue(recorder.isPaused());
		System.out.println(sw.getBuffer().toString());

	}

	@Test
	public void testPerformance() {
		int bs = 1000;
		StringWriter sw = new StringWriter();
		ILatencyWriter w = new IOLatencyWriter(sw,bs);
		LatencyRecorder recorder = new LatencyRecorder();
		recorder.setBufferSize(bs);
		recorder.setName("Penguin");
		recorder.initialise();
		recorder.setWriter(w);
		recorder.start();
		for (int i = 0; i < 20003; i++) {
			recorder.begin("X", "Y", "1000", 100+i);
		}
		System.out.println(sw.getBuffer().toString().split("[\r]").length);
		try
		{
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
