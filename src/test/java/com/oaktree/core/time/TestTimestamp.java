package com.oaktree.core.time;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ij on 03/08/15.
 */
public class TestTimestamp {
    static double EPSION = 0.000000000000000000001;
    @Test
    public void testSamePrecision() {
        long ms = System.currentTimeMillis();
        Timestamp t =  new Timestamp(ms,Precision.Milliseconds);
        Assert.assertEquals(t.getTimestamp(),ms);
        Assert.assertEquals(t.getPrecision(),Precision.Milliseconds);
        Assert.assertEquals(0,t.getDifference(t),EPSION);
        Timestamp u = new Timestamp(ms,Precision.Milliseconds);
        Assert.assertEquals(t,u);
        Assert.assertEquals(0, t.getDifference(u), EPSION);

    }
    @Test
    public void testConvert() {
        long ms = System.currentTimeMillis();
        Timestamp t = new Timestamp(ms, Precision.Milliseconds);
        Timestamp us = t.convertToPrecision(Precision.Microseconds);
        Assert.assertEquals(ms*1000,us.getTimestamp());
        Assert.assertNotSame(System.identityHashCode(t),System.identityHashCode(us));
    }


    @Test
    public void testDifferentPrecision() {
        long ms = System.currentTimeMillis();
        Timestamp t =  new Timestamp(ms,Precision.Milliseconds);
        Timestamp v = new Timestamp(ms*1000,Precision.Microseconds);
        long diff = t.getDifferenceAndConvertIfRequired(v);
        Assert.assertEquals(0, diff);
    }

    @Test
    public void testEqual() {
        long ms = System.currentTimeMillis();
        Timestamp t =  new Timestamp(ms,Precision.Milliseconds);
        Timestamp u =  new Timestamp(ms,Precision.Milliseconds);
        Assert.assertTrue(t.isEqual(u));
        Assert.assertEquals(0,t.compareTo(u),EPSION);
        try {
            Timestamp v = new Timestamp(ms * 1000, Precision.Microseconds);
            t.isEqual(v);
            Assert.fail();
        } catch (Exception e) {}

    }

    @Test
    public void testIsBefore() {
        long ms = System.currentTimeMillis();
        Timestamp t =  new Timestamp(ms,Precision.Milliseconds);
        Timestamp u =  new Timestamp(ms-1,Precision.Milliseconds);
        Assert.assertTrue(u.isBefore(t));
        try {
            Timestamp v = new Timestamp((ms-1) * 1000, Precision.Microseconds);
            t.isBefore(v);
            Assert.fail();
        } catch (Exception e) {}

    }

    @Test
    public void testIsAfter() {
        long ms = System.currentTimeMillis();
        Timestamp t =  new Timestamp(ms,Precision.Milliseconds);
        Timestamp u =  new Timestamp(ms+1,Precision.Milliseconds);
        Assert.assertTrue(u.isAfter(t));
        Assert.assertEquals(1, u.compareTo(t), EPSION);
        Assert.assertEquals(-1,t.compareTo(u),EPSION);

        try {
            Timestamp v = new Timestamp((ms+1) * 1000, Precision.Microseconds);
            t.isAfter(v);
            Assert.fail();
        } catch (Exception e) {}

    }
}
