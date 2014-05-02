package com.oaktree.core.utils;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestResultTimer {

	private ResultTimer resultTimer;
	 private final static double ERROR = 0.000000000001;

	@Before
	public void setup(){
		this.resultTimer = new ResultTimer();
	}
	
	@Test
	public void testBasics() {
		Assert.assertEquals(0,this.resultTimer.getCount());
		resultTimer.add(10);
		Assert.assertEquals(1,this.resultTimer.getCount());
		Assert.assertEquals(10,this.resultTimer.getMean(TimeUnit.NANOSECONDS),ERROR);		
		Assert.assertEquals(10,this.resultTimer.getMedian(TimeUnit.NANOSECONDS),ERROR);
		Assert.assertEquals(10,this.resultTimer.getPercentile(90,TimeUnit.NANOSECONDS),ERROR);
		Assert.assertEquals(10,this.resultTimer.getPercentile(10,TimeUnit.NANOSECONDS),ERROR);
		Assert.assertEquals(10,this.resultTimer.getMin(TimeUnit.NANOSECONDS),ERROR);
		Assert.assertEquals(10,this.resultTimer.getMax(TimeUnit.NANOSECONDS),ERROR);
		
		resultTimer.startSample();
		try {Thread.sleep(1000);}catch(Exception e){}
		resultTimer.endSample();
		Assert.assertEquals(2,this.resultTimer.getCount());
		Assert.assertEquals(resultTimer.getMax(TimeUnit.NANOSECONDS),resultTimer.getValues()[1],ERROR);
		Assert.assertEquals(resultTimer.getMin(TimeUnit.NANOSECONDS),resultTimer.getValues()[0],ERROR);
		
	}
	
	@Test
	public void testIgnores() {
		resultTimer = new ResultTimer(2);
		Assert.assertEquals(0,this.resultTimer.getCount());
		resultTimer.add(10);
		Assert.assertEquals(0,this.resultTimer.getCount());
		resultTimer.add(11);
		resultTimer.add(12);
		Assert.assertEquals(1,this.resultTimer.getCount());
	}
	
	@After
	public void tearDown(){}
	
}
