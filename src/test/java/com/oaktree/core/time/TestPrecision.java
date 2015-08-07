package com.oaktree.core.time;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by ij on 03/08/15.
 */
public class TestPrecision {
    static double EPSILON = 0.00000000000001;

    @Test
    public void testGetMultiplierMoreGranular() {
        Precision a = Precision.Microseconds;
        Precision b = Precision.Nanos;

        PrecisionMultiplier modifier = TimestampUtils.getMultiplier(a, b);
        Assert.assertNotNull(modifier);
        Assert.assertEquals(1000, modifier.getMultiplier(), EPSILON);
        Assert.assertTrue(modifier.shouldMultiply());
        Assert.assertFalse(modifier.isNoMultiply());

        long l = System.nanoTime()/1000;
        long adjusted = modifier.adjust(l);
        Assert.assertEquals(l * 1000, adjusted, EPSILON);
    }

    @Test
    public void testGetMultiplierMoreCoarse() {
        Precision a = Precision.Nanos;
        Precision b = Precision.Microseconds;

        PrecisionMultiplier modifier = TimestampUtils.getMultiplier(a, b);
        Assert.assertNotNull(modifier);
        Assert.assertEquals(1000, modifier.getMultiplier(), EPSILON);
        Assert.assertFalse(modifier.shouldMultiply());
        Assert.assertFalse(modifier.isNoMultiply());
        long l = System.nanoTime();
        long adjusted = modifier.adjust(l);
        Assert.assertEquals(l/1000,adjusted,EPSILON);
    }

    @Test
    public void testSamePrecision() {
        Precision a = Precision.Nanos;
        Precision b = Precision.Nanos;

        PrecisionMultiplier modifier = TimestampUtils.getMultiplier(a, b);
        Assert.assertNotNull(modifier);
        Assert.assertEquals(1, modifier.getMultiplier(), EPSILON);
        Assert.assertTrue(modifier.isNoMultiply());
        Assert.assertTrue(modifier.shouldMultiply());
        Assert.assertEquals(modifier, PrecisionMultiplier.NO_MULTIPLY);
        long l = System.nanoTime();
        long adjusted = a.adjust(l,Precision.Nanos);
        Assert.assertEquals(l,adjusted,EPSILON);
    }

}