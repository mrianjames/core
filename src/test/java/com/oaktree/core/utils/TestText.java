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
        ResultTimer timera = new ResultTimer(10000);
        ResultTimer timerb = new ResultTimer(10000);
        for (int i  = 0; i < tests;i++) {
            timera.startSample();
            String x = Text.toTimeSinceEpoch(time);
            long da = timera.endSample();
            System.out.println(x+ " dura: "+da+"ns");
            timerb.startSample();
            String y = Text.renderTime(time); //thread local date formatter.
            long db = timerb.endSample();
            System.out.println(y + " durb: "+db+"ns");

        }
        System.out.println(timera.toString(TimeUnit.NANOSECONDS));
        System.out.println(timerb.toString(TimeUnit.NANOSECONDS));
    }


}
