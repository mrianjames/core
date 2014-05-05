package com.oaktree.core.utils;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.DescriptiveStatisticsImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

//import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
//import com.google.monitoring.runtime.instrumentation.Sampler;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 08/06/11
 * Time: 13:07
 */
public class TestDoubleStatistics {

    private final static double EPSILON = 0.000000000001;

    @Test
    public void testMean() {
        DoubleStatistics ds = new DoubleStatistics(100);
        ds.addValue(10);
        ds.addValue(20);
        ds.addValue(30);
        ds.addValue(40);
        ds.addValue(50);
        Assert.assertEquals(30,ds.getMean(),EPSILON);
        ds.addValue(90);
        Assert.assertEquals(40,ds.getMean(),EPSILON);
    }

    @Test
    public void testCount() {
        DoubleStatistics ds = new DoubleStatistics(100);
        ds.addValue(10);
        ds.addValue(20);
        ds.addValue(30);
        ds.addValue(40);
        ds.addValue(50);
        Assert.assertEquals(ds.getCount(),5);
    }


    @Test
    public void testMedian() {
        //test odd num elements
        DoubleStatistics ds = new DoubleStatistics(100);
        ds.addValue(10);
        Assert.assertEquals(ds.getMedian(),10,EPSILON);
        ds.addValue(20);
        Assert.assertEquals(ds.getMedian(),10,EPSILON);
        ds.addValue(30);
        ds.addValue(40);
        ds.addValue(50);
        Assert.assertEquals(ds.getMedian(),30,EPSILON);
        //even number chooses lhs of middle.
        ds.addValue(90);
        Assert.assertEquals(ds.getMedian(),30,EPSILON);
    }

    @Test
    public void testMax() {

        DoubleStatistics ds = new DoubleStatistics(100);
        Assert.assertEquals(ds.getMax(),Double.NaN,EPSILON);
        ds.addValue(10);
        ds.addValue(20);
        ds.addValue(30);
        ds.addValue(40);
        ds.addValue(50);
        ds.addValue(90);
        Assert.assertEquals(ds.getMax(),90,EPSILON);
    }

    @Test
    public void testClear() {

        DoubleStatistics ds = new DoubleStatistics(100);
        ds.addValue(10);
        Assert.assertEquals(ds.getMax(),10,EPSILON);
        ds.clear();
        Assert.assertEquals(ds.getMax(),Double.NaN,EPSILON);

    }

    @Test
    public void testMin() {

        DoubleStatistics ds = new DoubleStatistics(100);
        Assert.assertEquals(ds.getMin(),Double.NaN,EPSILON);
        ds.addValue(10);
        ds.addValue(20);
        ds.addValue(30);
        ds.addValue(40);
        ds.addValue(50);
        ds.addValue(90);
        Assert.assertEquals(ds.getMin(),10,EPSILON);
    }

    @Test
    public void testPercentile() {

        DoubleStatistics ds = new DoubleStatistics(100);
        for (int i = 0;i < 100; i++) {
            ds.addValue(i+1);
        }
        Assert.assertEquals(ds.getPercentile(1),1,EPSILON);
        Assert.assertEquals(ds.getPercentile(20),20,EPSILON);
        Assert.assertEquals(ds.getPercentile(40),40,EPSILON);
        Assert.assertEquals(ds.getPercentile(50),50,EPSILON);
        Assert.assertEquals(ds.getPercentile(60),60,EPSILON);
        Assert.assertEquals(ds.getPercentile(80),80,EPSILON);
        Assert.assertEquals(ds.getPercentile(100),100,EPSILON);

    }
    
    public static void main(String[] args) {
    	final long[] sz = new long[]{0};
//    	AllocationRecorder.addSampler(new Sampler() {
//
//
//    	    public void sampleAllocation(int count, String desc,
//    	      Object newObj, long size) {
//    	    	if (newObj instanceof double[]) {
//    	    		System.out.println("double[] created of size " + size + " count "+ count + " desc: " + desc);
//    	    		sz[0]+=size;
//    	    	}
//    	    }
//    	  });
    	
    	ResultTimer a =new ResultTimer(10000);
    	ResultTimer b = new ResultTimer(10000);
    	ResultTimer c = new ResultTimer(10000);
    	
    	long sizeb = 0;
    	long sizea = 0;
    	long sizec = 0;
    	
//    	sizeb = testMyStats(b,sz);
//    	sizea = testDescStats(a,sz);
    	sizec = testOptimisedMyStats(c,sz);
    	
    	
    	

    	
    	System.out.println("***********************************");
    	System.out.println("DESCSTATS: "+a.toString(TimeUnit.MICROSECONDS) + " " + Text.to2Dp(sizea) +" bytes");
    	System.out.println("MYSTATS: "+b.toString(TimeUnit.MICROSECONDS) + " "+Text.to2Dp(sizeb)+" bytes");
    	System.out.println("OPTSTATS: "+c.toString(TimeUnit.MICROSECONDS) + " " + Text.to2Dp(sizec) + " bytes");
    	
    	System.out.println("***********************************");
    }
    static int TESTS = 30000;
    private static long testDescStats(ResultTimer a, long[] sz) {
    	long sizea = 0;
    	for (int x = 0; x < 4; x++) {
    		System.gc();
	    	//test Descriptive Stats vs this.
	    	DescriptiveStatistics ds = new DescriptiveStatisticsImpl();
	    	ResultTimer rt = new ResultTimer();
	    	for (int i = 0; i < TESTS;i++) {
	    		rt.startSample();
	    		for (int j = 0; j < 100;j++) {
		    		ds.addValue(12.0);
		    		ds.addValue(34.3);
		    		ds.addValue(12.23);
	    		}
	    		
	    		System.out.println("MEAN:"+ds.getMean());
	    		System.out.println("MAX:"+ds.getMax());
	    		System.out.println("MIN:"+ds.getMin());
	    		System.out.println("MED:"+ds.getPercentile(50));
	    		System.out.println("90%:"+ds.getPercentile(90));
	    		System.out.println("95%:"+ds.getPercentile(95));
	    		System.out.println("99%:"+ds.getPercentile(99));
	    		ds.clear();
	    		rt.endSample();
	    	}
	    	
    		sizea = sz[0];
    		sz[0] = 0;
    		a.clear();a.add(rt);
    	}
    	return sizea;
    }

    private static long testMyStats(ResultTimer b, long[] sz) {
    	
		long sizeb = 0;
    	for (int x = 0; x < 4; x++) {
    		System.gc();
	    	//test Descriptive Stats vs this.
	    	DoubleStatistics ds = new DoubleStatistics(TESTS*3*100);
	    	ResultTimer rt = new ResultTimer();
	    	for (int i = 0; i < TESTS;i++) {
	    		rt.startSample();
	    		for (int j = 0; j < 100;j++) {
		    		ds.addValue(12.0);
		    		ds.addValue(34.3);
		    		ds.addValue(12.23);
	    		}
	    		
	    		System.out.println("MEAN:"+ds.getMean());
	    		System.out.println("MAX:"+ds.getMax());
	    		System.out.println("MIN:"+ds.getMin());
	    		double[] results = ds.getBulkPercentiles(DoubleStatistics.STD_PERCENTILES);
	    		System.out.println("MED:"+results[0]);
	    		System.out.println("90%:"+results[1]);
	    		System.out.println("95%:"+results[2]);
	    		System.out.println("99%:"+results[3]);
	    		ds.clear();
	    		rt.endSample();
	    	}
    		sizeb = sz[0];
	    	sz[0] = 0;
	    	b.clear();b.add(rt);
    	}
    	return sizeb;
    }
    
	private static long testOptimisedMyStats(ResultTimer c, long[] sz) {
		
		long sizec = 0;
		for (int x = 0; x < 4; x++) {
    		System.gc();
	    	
	    	//test Descriptive Stats vs this.
	    	DoubleStatistics ds = new DoubleStatistics(TESTS*3*100);
	    	ResultTimer rt = new ResultTimer();
	    	for (int i = 0; i < TESTS;i++) {
	    		rt.startSample();
	    		for (int j = 0; j < 100;j++) {
		    		ds.addValue(12.0);
		    		ds.addValue(34.3);
		    		ds.addValue(12.23);
	    		}
	    		StatisticalSummary s = new StatisticalSummary();
	    		ds.getAllStatistics(s);
	    		System.out.println("MEAN:"+s.getMean());
	    		System.out.println("MAX:"+s.getMax());
	    		System.out.println("MIN:"+s.getMin());
	    		System.out.println("MED:"+s.getMedian());
	    		System.out.println("90%:"+s.getNinety());
	    		System.out.println("95%:"+s.getNinetyFive());
	    		System.out.println("99%:"+s.getNinetyNine());
	    		ds.clear();
	    		rt.endSample();
	    	}
	    	//c = rt; //297902064
	    	sizec = sz[0];
	    	sz[0] = 0;
	    	c.clear();c.add(rt);
    	}
		return sizec;
	}
}
