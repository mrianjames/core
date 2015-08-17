package com.oaktree.core.time;

import com.oaktree.core.utils.ResultTimer;
import com.oaktree.core.utils.Text;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by ij on 31/07/15.
 */
public class TestTimestampUtils {
    private double EPSILON = 0.000000001;
    @Test
    public void testConvertMillisecondStringToLong() {
        Timestamp midnight = TimestampUtils.getTodayMidnight(Precision.Milliseconds);
        //10:30:25
        long expTime = midnight.getTimestamp() + (Text.MILLIS_PER_HOUR * 10) + (Text.MILLIS_PER_MIN * 30) + (Text.MILLIS_PER_SEC * 25);
        Timestamp t = TimestampUtils.strSecondTimeToTimestamp("10:30:25", Precision.Milliseconds);

        Assert.assertEquals(expTime, t.getTimestamp(), EPSILON);
    }

    @Test
    public void testLongStringPrecision() {
        long ms = System.currentTimeMillis();
        long origms = ms;
        Assert.assertEquals(Precision.Milliseconds,TimestampUtils.getLongTimestampPrecision(ms));
        Assert.assertEquals(Precision.Milliseconds,TimestampUtils.getTimestampPrecision(""+ms));
        ms=ms*1000;
        Assert.assertEquals(Precision.Microseconds,TimestampUtils.getLongTimestampPrecision(ms));
        Assert.assertEquals(Precision.Microseconds,TimestampUtils.getTimestampPrecision(""+ms));
        ms=ms*1000;
        Assert.assertEquals(Precision.Nanos,TimestampUtils.getLongTimestampPrecision(ms));
        Assert.assertEquals(Precision.Nanos,TimestampUtils.getTimestampPrecision("" + ms));
        ms=origms;
        ms=ms/1000;
        Assert.assertEquals(Precision.Seconds,TimestampUtils.getLongTimestampPrecision(ms));
        Assert.assertEquals(Precision.Seconds,TimestampUtils.getTimestampPrecision(""+ms));
        ms=ms/60;
        Assert.assertEquals(Precision.Minutes,TimestampUtils.getLongTimestampPrecision(ms));
        Assert.assertEquals(Precision.Minutes,TimestampUtils.getTimestampPrecision(""+ms));
        ms=ms/60;
        Assert.assertEquals(Precision.Hours,TimestampUtils.getLongTimestampPrecision(ms));
        Assert.assertEquals(Precision.Hours,TimestampUtils.getTimestampPrecision(""+ms));

    }

    @Test
    public void testTimeOfDayToString() {
        TimeOfDay tod = new TimeOfDay();
        tod.setHour(12);
        tod.setMinute(6);
        tod.setSeconds(13);
        tod.setMilliseconds(23);
        tod.setMicroseconds(132);
        tod.setNanoseconds(324);
        Assert.assertEquals("12:06:13.023132324", tod.toString());
        System.out.println(tod.toString());
    }

    @Test
    public void testGetTimesFromTimestamp() {
        System.out.println(System.currentTimeMillis());

        //long ns = 1439724874835000001l;
        long ns = TimestampUtils.getTodayMidnight(Precision.Nanos).getTimestamp() +
                (12*TimestampUtils.NANOS_PER_HOUR) +
                (34*TimestampUtils.NANOS_PER_MIN) +
                (34*TimestampUtils.NANOS_PER_SEC) +
                (835*TimestampUtils.NANOS_PER_MILLI) +
                (0*TimestampUtils.NANOS_PER_MICRO) +
                (1);
        System.out.println("ns: " + ns);
        Timestamp t = new Timestamp(ns, Precision.Nanos);
        TimeOfDay tod = TimestampUtils.timeOfDayFromTimestamp(t);
        Assert.assertEquals(12, tod.getHour());
        Assert.assertEquals(34, tod.getMinute());
        Assert.assertEquals(34, tod.getSeconds());
        Assert.assertEquals(835, tod.getMilliseconds());
        Assert.assertEquals(0, tod.getMicroseconds());
        Assert.assertEquals(1, tod.getNanoseconds());

        System.out.println("Time: " + TimestampUtils.timeOfDayFromTimestamp(new Timestamp(System.currentTimeMillis() * 1000 * 1000, Precision.Nanos)));
        System.out.println("Time: " + TimestampUtils.timeOfDayFromTimestamp(new Timestamp(System.currentTimeMillis() * 1000 * 1000, Precision.Nanos)));
        System.out.println("Time: " + TimestampUtils.timeOfDayFromTimestamp(new Timestamp(System.currentTimeMillis() * 1000 * 1000, Precision.Nanos)));

        //performance of this...
        ResultTimer rt = new ResultTimer(10000);
        tod = null;
        for (int i = 0; i < 100000; i++) {
            long time = System.currentTimeMillis()*1000*1000;
            Timestamp ts = new Timestamp(time,Precision.Nanos);
            rt.startSample();
            tod = TimestampUtils.timeOfDayFromTimestamp(ts);
            rt.endSample();
            System.out.println(tod);
        }
        System.out.println(rt.toString(TimeUnit.MICROSECONDS));
    }

    @Test
    public void testTimeOfDayToLong() {
        long ts = System.currentTimeMillis()*1000*1000;
        TimeOfDay tod = TimestampUtils.timeOfDayFromTimestamp(new Timestamp(ts,Precision.Nanos));
        Assert.assertEquals(TimestampUtils.timeOfDayToNanoLong(tod),ts,EPSILON);
    }

    @Test
    public void testTimeOfDayToLongToGranularity() {
        long ts = (System.currentTimeMillis()*1000*1000)+1023;
        TimeOfDay tod = TimestampUtils.timeOfDayFromTimestamp(new Timestamp(ts,Precision.Nanos));
        Assert.assertEquals(TimestampUtils.timeOfDayToNanoLong(tod,Precision.Nanos),ts,EPSILON);
        Assert.assertEquals(TimestampUtils.timeOfDayToNanoLong(tod,Precision.Milliseconds),(ts/1000000)*1000000,EPSILON);
    }

    @Test
    public void testGetDateMidnight() {
        long now = new Date().getTime();
        Calendar cal = Calendar.getInstance();
        cal.set(2015, Calendar.AUGUST, 14,0,0,0);
        Date date = cal.getTime();
        long days = (now - date.getTime())/TimestampUtils.MILLISECONDS_PER_DAY;
        long m = TimestampUtils.getMidnightForDate(date);
        Timestamp ts = new Timestamp(m,Precision.Nanos);
        long diff = TimestampUtils.getTodayMidnight(Precision.Nanos).getTimestamp()-m;
        Assert.assertEquals(diff,days*TimestampUtils.NANOS_PER_DAY,EPSILON);
    }
}
