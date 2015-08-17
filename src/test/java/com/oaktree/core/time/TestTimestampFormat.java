package com.oaktree.core.time;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ij on 08/08/15.
 */
public class TestTimestampFormat {
    private static double EPSILON = 0.00000000000000001;

    @Test
    public void testFormatParsing() {
        String strTime = "  12:23:34.123456789";
        long timeInNanos = TimestampUtils.getTodayMidnight(Precision.Nanos).getTimestamp() +
                (12* TimestampUtils.NANOS_PER_HOUR) +
                (23* TimestampUtils.NANOS_PER_MIN) +
                (34* TimestampUtils.NANOS_PER_SEC) +
                (123* TimestampUtils.NANOS_PER_MILLI) +
                (456* TimestampUtils.NANOS_PER_MICRO) +
                (789);
        TimestampFormat f = new TimestampFormat("  hh:mm:ss.iiiuuunnn");
        Assert.assertEquals(12, f.getHour(strTime,false), EPSILON);
        Assert.assertEquals(23,f.getMinute(strTime,false),EPSILON);
        Assert.assertEquals(34,f.getSecond(strTime,false),EPSILON);
        Assert.assertEquals(123,f.getMillis(strTime,false),EPSILON);
        Assert.assertEquals(456,f.getMicros(strTime,false),EPSILON);
        Assert.assertEquals(789, f.getNanos(strTime,false), EPSILON);

        Timestamp t= f.asTimestamp(strTime);
        Assert.assertEquals(t.getTimestamp(),timeInNanos,EPSILON);
    }

    @Test
    public void testSmallformat() {
        String strTime = "  12:23:34.123";
        long timeInNanos = TimestampUtils.getTodayMidnight(Precision.Nanos).getTimestamp() +
                (12* TimestampUtils.NANOS_PER_HOUR) +
                (23* TimestampUtils.NANOS_PER_MIN) +
                (34* TimestampUtils.NANOS_PER_SEC) +
                (123* TimestampUtils.NANOS_PER_MILLI) ;
        TimestampFormat f = new TimestampFormat("  hh:mm:ss.iii");
        Assert.assertEquals(12, f.getHour(strTime, false), EPSILON);
        Assert.assertEquals(23,f.getMinute(strTime, false),EPSILON);
        Assert.assertEquals(34,f.getSecond(strTime, false),EPSILON);
        Assert.assertEquals(123,f.getMillis(strTime, false),EPSILON);

        Timestamp t= f.asTimestamp(strTime,Precision.Nanos);
        Assert.assertEquals(t.getTimestamp(),timeInNanos,EPSILON);
    }

    @Test
    public void testShortString() {
        String strTime = "  12:23";
        long timeInNanos = TimestampUtils.getTodayMidnight(Precision.Nanos).getTimestamp() +
                (12* TimestampUtils.NANOS_PER_HOUR) +
                (23* TimestampUtils.NANOS_PER_MIN);
        TimestampFormat f = new TimestampFormat("  hh:mm:ss.iiiuuunnn");
        Assert.assertEquals(12, f.getHour(strTime,false), EPSILON);
        Assert.assertEquals(23,f.getMinute(strTime,false),EPSILON);
        Assert.assertEquals(TimestampFormat.NOT_SET,f.getSecond(strTime, false),EPSILON);
        Assert.assertEquals(TimestampFormat.NOT_SET,f.getMillis(strTime, false),EPSILON);
        Assert.assertEquals(TimestampFormat.NOT_SET,f.getMicros(strTime, false),EPSILON);
        Assert.assertEquals(TimestampFormat.NOT_SET,f.getNanos(strTime, false),EPSILON);

        Timestamp t= f.asTimestamp(strTime,Precision.Milliseconds);
        Assert.assertEquals(t.getTimestamp(),timeInNanos/1000000,EPSILON);
    }

    @Test
    public void testMillisString() {
        long ms = System.currentTimeMillis();
        Timestamp tms = new Timestamp(ms,Precision.Milliseconds);
        String strFormat = "iiiiiiiiiiiii";
        TimestampFormat f = new TimestampFormat(strFormat);
        Assert.assertEquals(0, f.getTimeStart(), EPSILON);
        Assert.assertEquals(strFormat.length(), f.getTimeEnd() + 1, EPSILON);
        Assert.assertEquals(f.NOT_SET, f.getHour("" + ms, false), EPSILON);
        Assert.assertEquals(f.NOT_SET, f.getMinute(""+ms,false),EPSILON);
        Assert.assertEquals(f.NOT_SET, f.getSecond(""+ms,false),EPSILON);
        Assert.assertEquals(ms, f.getMillis(""+ms,false),EPSILON);
        Assert.assertEquals(f.NOT_SET, f.getMicros(""+ms,false),EPSILON);
        Assert.assertEquals(f.NOT_SET, f.getNanos(""+ms,false),EPSILON);

    }
}
