package com.oaktree.core.time;

import com.oaktree.core.utils.Text;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Helper functions for timestamp manipulation.
 * Mainly a collection of string to long functions of varying precisions.
 *
 * Created by ij on 28/07/15.
 */
public class TimestampUtils {

    public static final long NANOS_PER_MICRO = 1000;
    public static final long NANOS_PER_MILLI = NANOS_PER_MICRO * 1000;
    public static final long NANOS_PER_SEC = NANOS_PER_MILLI * 1000;
    public static final long NANOS_PER_MIN = NANOS_PER_SEC * 60;
    public static final long NANOS_PER_HOUR = NANOS_PER_MIN * 60;
    public static final long NANOS_PER_DAY = NANOS_PER_HOUR * 24;
    public static final long MILLISECONDS_PER_DAY = 24*60*60*1000;
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

    /**
     * Get the most coarse (least granular) precision.
     * @param a
     * @param b
     * @return
     */
    public static Precision getLeastGranularPrecision(Timestamp a, Timestamp b) {
        int i = Math.min(a.getPrecision().getGranularity(),b.getPrecision().getGranularity());
        return Precision.values()[i];
    }

    /**
     * Get the least coarse (most granular) precision.
     * @param a
     * @param b
     * @return
     */
    public static Precision getMostGranularPrecision(Timestamp a, Timestamp b) {
        int i = Math.max(a.getPrecision().getGranularity(), b.getPrecision().getGranularity());
        return Precision.values()[i];
    }

    /**
     * Get the precision for a string rep of a long timestamp.
     * This helps convert System.currentMillis and other derivatives into
     * something meaningful.
     *
     * @param time
     * @return
     */
    public static Precision getLongTimestampPrecision(long time) {
        return getTimestampPrecision(Text.EMPTY_STRING+time);
    }
    /**
     * Get the precision for a string rep of a long timestamp.
     * This helps convert System.currentMillis and other derivatives into
     * something meaningful.
     *
     * @param time
     * @return
     */
    public static Precision getTimestampPrecision(String time) {
        switch (time.length()) {
            case 6:
                return Precision.Hours;
            case 8:
                return Precision.Minutes;
            case 10:
                return Precision.Seconds;
            case 13:
                return Precision.Milliseconds;
            case 16:
                return Precision.Microseconds;
            case 19:
                return Precision.Nanos;
        }
        throw new IllegalStateException("No precision matches the string provided: " + time);
    }

    /**
     * Get a fly-weight time of day object from a timestamp.
     * Not all timestamps are time-of-days so use this with half
     * a brain.
     *
     * @param ts
     * @return
     */
    public static TimeOfDay timeOfDayFromTimestamp(Timestamp ts) {
        long nanos = ts.convertToPrecision(Precision.Nanos).getTimestamp();
        nanos = nanos - TimestampUtils.getTodayMidnight(Precision.Nanos).getTimestamp();
        TimeOfDay tod = new TimeOfDay();
        tod.setHour((nanos / TimestampUtils.NANOS_PER_HOUR) % 24);
        tod.setMinute((nanos / TimestampUtils.NANOS_PER_MIN) % 60);
        tod.setSeconds((nanos / TimestampUtils.NANOS_PER_SEC) % 60);
        tod.setMilliseconds((nanos / TimestampUtils.NANOS_PER_MILLI) % 1000);
        tod.setMicroseconds((nanos / TimestampUtils.NANOS_PER_MICRO) % 1000);
        tod.setNanoseconds(nanos %1000);
        return tod;
    }

    /**
     * Take a time of day object and make a long timestamp in nano precision.
     * @param tod
     * @return
     */
    public static long timeOfDayToNanoLong(TimeOfDay tod) {
        long ns = TimestampUtils.getTodayMidnight(Precision.Nanos).getTimestamp();
        ns += (TimestampUtils.NANOS_PER_HOUR * tod.getHour());
        ns += (TimestampUtils.NANOS_PER_MIN * tod.getMinute());
        ns += (TimestampUtils.NANOS_PER_SEC * tod.getSeconds());
        ns += (TimestampUtils.NANOS_PER_MILLI * tod.getMilliseconds());
        ns += (TimestampUtils.NANOS_PER_MICRO * tod.getMicroseconds());
        ns += tod.getNanoseconds();
        return ns;
    }

    /**
     * Get a nano length long timestamp but set to a supplied granularity.
     * Use this if you say care about hours, mins but not the rest of the values, but
     * still need nano length for comparison.
     *
     * @param tod
     * @param granularity
     * @return
     */
    public static long timeOfDayToNanoLong(TimeOfDay tod,Precision granularity) {
        if (granularity.equals(Precision.Nanos)) return timeOfDayToNanoLong(tod);
        long ns = TimestampUtils.getTodayMidnight(Precision.Nanos).getTimestamp();
        ns += (TimestampUtils.NANOS_PER_HOUR * tod.getHour());
        if (granularity.getGranularity() >= Precision.Minutes.getGranularity()) ns += (TimestampUtils.NANOS_PER_MIN * tod.getMinute());
        if (granularity.getGranularity() >= Precision.Seconds.getGranularity()) ns += (TimestampUtils.NANOS_PER_SEC * tod.getSeconds());
        if (granularity.getGranularity() >= Precision.Milliseconds.getGranularity()) ns += (TimestampUtils.NANOS_PER_MILLI * tod.getMilliseconds());
        if (granularity.getGranularity() >= Precision.Microseconds.getGranularity()) ns += (TimestampUtils.NANOS_PER_MICRO * tod.getMicroseconds());
        if (granularity.getGranularity() >= Precision.Nanos.getGranularity()) ns += tod.getNanoseconds();
        return ns;
    }

    /**
     * Render a nice representation of TimeOfDay object.
     * @param timeOfDay
     * @return
     */
    public static String toString(TimeOfDay timeOfDay) {
        StringBuilder b = new StringBuilder(18);
        b.append(Text.twoDigits(timeOfDay.getHour()));
        b.append(Text.COLON);
        b.append(Text.twoDigits(timeOfDay.getMinute()));
        b.append(Text.COLON);
        b.append(Text.twoDigits(timeOfDay.getSeconds()));
        b.append(Text.PERIOD);
        b.append(Text.threeDigits(timeOfDay.getMilliseconds()));
        b.append(Text.threeDigits(timeOfDay.getMicroseconds()));
        b.append(Text.threeDigits(timeOfDay.getNanoseconds()));
        return b.toString();
    }
    private static Date todaysDate = new Date();

    public static long getMidnightForDate(Date date) {
        long diff = todaysDate.getTime()-date.getTime();
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        return getTodayMidnight(Precision.Nanos).getTimestamp()-(days*NANOS_PER_DAY);
    }
}
