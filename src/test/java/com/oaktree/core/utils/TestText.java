package com.oaktree.core.utils;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 27/12/12
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 */
public class TestText {

    @Test
    public void testTimeFormatting() {
        int tests = 50000;
        long time = System.currentTimeMillis()+(1*Text.MILLIS_PER_HOUR);
        ResultTimer timer = new ResultTimer(10000);
        for (int i  = 0; i < tests;i++) {
            timer.startSample();
            String x = Text.toTimeSinceEpoch(time);
            timer.endSample();
            System.out.println(x);

        }
        System.out.println(timer.toString(TimeUnit.NANOSECONDS));
    }


}
