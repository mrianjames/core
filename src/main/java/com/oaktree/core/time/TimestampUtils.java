package com.oaktree.core.time;

import com.oaktree.core.utils.Text;

import java.util.Calendar;

/**
 * Helper functions for timestamp manipulation.
 * Mainly a collection of string to long functions of varying precisions.
 *
 * Created by ij on 28/07/15.
 */
public class TimestampUtils {



    private static long midnightTodayInMilliseconds;
    private static long midnightTodayInSeconds;
    private static long midnightTodayInNanoseconds;
    private static long midnightTodayInMicroseconds;

    private static final Timestamp midnightTimestampNanoseconds;
    private static final Timestamp midnightTimestampMicroseconds;
    private static final Timestamp midnightTimestampMilliseconds;
    private static final Timestamp midnightTimestampSeconds;
    private static final Timestamp midnightTimestampMinutes;

    /**
     * Get midnight once and once only.
     */
    static {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long ms = cal.getTime().getTime();
        midnightTodayInMilliseconds = ms;
        midnightTodayInMicroseconds = midnightTodayInMilliseconds * 1000;
        midnightTodayInNanoseconds = midnightTodayInNanoseconds * 1000;
        midnightTodayInSeconds = midnightTodayInMilliseconds/1000;

        midnightTimestampNanoseconds = new Timestamp(ms*1000*1000,Precision.Nanos);
        midnightTimestampMicroseconds = new Timestamp(ms*1000,Precision.Microseconds);
        midnightTimestampMilliseconds = new Timestamp(ms,Precision.Milliseconds);
        midnightTimestampSeconds = new Timestamp(ms/1000,Precision.Seconds);
        midnightTimestampMinutes = new Timestamp(ms/1000/60,Precision.Minutes);
    }


    /**
     * Convert a timestamp of the format HH:MM:SS to a long, multiplied to specified precision.
     * @param strFirstTimestamp
     * @return
     */
    public static Timestamp strSecondTimeToTimestamp(String strFirstTimestamp, Precision precision) {
        int hours = Integer.valueOf(strFirstTimestamp.substring(0,2));
        int minutes = Integer.valueOf(strFirstTimestamp.substring(3,5));
        int seconds = Integer.valueOf(strFirstTimestamp.substring(6,8));

        long timeInMs = (hours * Text.MILLIS_PER_HOUR) + (minutes * Text.MILLIS_PER_MIN) + (seconds * Text.MILLIS_PER_SEC);
        Timestamp msTime = new Timestamp(timeInMs,Precision.Milliseconds);
        Timestamp midnight = getTodayMidnight(Precision.Milliseconds); //doesnt matter what precision really...
        msTime = msTime.add(midnight);
        //convert to desired precision.
        if (precision.equals(Precision.Milliseconds)) {
            return msTime;
        }
        return msTime.convertToPrecision(precision);
    }

    /**
     * Get midnight today as a timestamp set to your desired precision.
     * @param precision
     * @return
     */
    public static Timestamp getTodayMidnight(Precision precision) {
        switch (precision) {
            case Milliseconds:
                return midnightTimestampMilliseconds;
            case Nanos:
                return midnightTimestampNanoseconds;
            case Microseconds:
                return midnightTimestampMicroseconds;

            case Seconds:
                return midnightTimestampSeconds;
            case Minutes:
                return midnightTimestampMinutes;
        }
        throw new IllegalArgumentException("Unknown precision: " + precision);
    }

    /**
     * Get a conversion multiplier between two precisions. So for example if you need to
     * convert from a timestamp in Milliseconds and get it in Nanoseconds the result would be 1000000 multiply.
     * If you had nanoseconds but wanted in milliseconds it would be 1000000 divide.
     *
     * @param source - the precision you are coming from
     * @param target - the precision you are going to
     * @return
     */
    public static PrecisionMultiplier getMultiplier(Precision source, Precision target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Invalid precision comparison: source: " + source + " target: " + target);
        }
        if (source.equals(target)) {
            return PrecisionMultiplier.NO_MULTIPLY;
        }
        //triangulate against a common point and return the difference. we choose nanos as its the most granular.
        PrecisionMultiplier sourceToSecondMultiplier = source.getToNanoMultiplier();
        PrecisionMultiplier targetToSecondMultiplier = target.getToNanoMultiplier();
        //example1..millis to nanos...ms_to_ns = *1000000, ns_to_ns = *1. Therefore *1000000. going more, * 1000000
        //example2..nanos to millis ns_to_ns=*1, ms_to_ns=*1000000. Gran = going_less, therefore /1000000
        //example3..millis to minutes => ms_to_ns=*1000000, min_to_ns=*60000000000. going_less, therefore / (60000)
        //example4..minutes to millis => min_to_ns = *60000000000, millis_to_nanos=1000000, more_granular, 60000
        boolean goingMoreGranular = source.isMoreCourseGrainedThan(target);
        long sourceNanoMultiplier=sourceToSecondMultiplier.getMultiplier();
        long targetNanoMultiplier=targetToSecondMultiplier.getMultiplier();
        long value = goingMoreGranular ? targetNanoMultiplier/sourceNanoMultiplier:sourceNanoMultiplier/targetNanoMultiplier;

        PrecisionMultiplier multiplier =  new PrecisionMultiplier(value,goingMoreGranular);
        return multiplier;
    }
}
