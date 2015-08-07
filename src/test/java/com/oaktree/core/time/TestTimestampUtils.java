package com.oaktree.core.time;

import com.oaktree.core.utils.Text;
import org.junit.Assert;
import org.junit.Test;

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
}
