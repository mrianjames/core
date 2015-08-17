package com.oaktree.core.time;

/**
 * Structure of our timestamps used in this file.
 * We are only interested in times, but we need to
 * know where the various bits start and what the precision
 * of timestamps will be e.g. min/sec/ms, min/sec/ms/us
 *
 * Example YYYY-MM-DD hh:mm:ss.iiiuuunnn
 * This will yield methods that point to the start of the time, the bits in it and the end where]
 * the "data" starts
 **/
public class TimestampFormat {

    private int hourStart = -1;
    private int hourEnd = -1;

    private int minStart = -1;
    private int minEnd = -1;

    private int secondStart = -1;
    private int secondEnd = -1;

    private int milliStart = -1;
    private int milliEnd = -1;

    private int microStart = -1;
    private int microEnd = -1;

    private int nanoStart = -1;
    private int nanoEnd = -1;

    private int timeLength = -1;
    private int timeEnd = -1;
    private String strFormat;
    public static int NOT_SET = -1;
    private Precision precision;
    private boolean timestampIncludesDate = false;
    private int timeStart;


    public TimestampFormat(String strFormat) {
        this.parse(strFormat);
    }

    public boolean hasHours() { return hourStart > NOT_SET;}
    public boolean hasMins() { return minStart > NOT_SET; }
    public boolean hasSecond() {return secondStart > NOT_SET;}
    public boolean hasMillis() {return milliStart > NOT_SET;}
    public boolean hasMicros() {return microStart > NOT_SET;}
    public boolean hasNanos() {return nanoStart > NOT_SET;}
    public int getTimeLength() {return timeEnd-timeStart;}
    public int getTimeStart() { return timeStart;}
    public int getTimeEnd() { return timeEnd; }
    public int getPrefix() { return hourStart; }
    public static char HOURS = 'h';
    public static char MINS = 'm';
    public static char SECONDS = 's';
    public static char MILLIS = 'i';
    public static char MICROS ='u';
    public static char NANOS='n';

    @Override
    public String toString() {
        return this.strFormat;
    }
    private int dataStartPosition = 0;
    private void parse(String format) {
        this.strFormat = format;
        int lastTime = -1;
        int firstTime = NOT_SET;
        hourStart = format.indexOf(HOURS);
        hourEnd = format.lastIndexOf(HOURS);
        if (hourEnd > -1) { lastTime = hourEnd; precision=Precision.Hours; if (firstTime==NOT_SET) firstTime = hourStart;}
        minStart = format.indexOf(MINS);
        minEnd = format.lastIndexOf(MINS);
        if (minEnd > -1) { lastTime = minEnd; precision=Precision.Minutes;if (firstTime==NOT_SET) firstTime = minStart;}
        secondStart = format.indexOf(SECONDS);
        secondEnd = format.lastIndexOf(SECONDS);
        if (secondEnd > -1) { lastTime = secondEnd; precision=Precision.Seconds;if (firstTime==NOT_SET) firstTime = secondStart;}
        milliStart = format.indexOf(MILLIS);
        milliEnd = format.lastIndexOf(MILLIS);
        if (milliEnd > -1) { lastTime = milliEnd;precision=Precision.Milliseconds;if (firstTime==NOT_SET) firstTime = milliStart; }
        microStart = format.indexOf(MICROS);
        microEnd = format.lastIndexOf(MICROS);
        if (microEnd > -1) { lastTime = microEnd; precision=Precision.Microseconds;if (firstTime==NOT_SET) firstTime = microStart;}
        nanoStart = format.indexOf(NANOS);
        nanoEnd = format.lastIndexOf(NANOS);
        if (nanoEnd > -1) { lastTime = nanoEnd;precision = Precision.Nanos;if (firstTime==NOT_SET) firstTime = nanoStart;}
        this.timeEnd = lastTime;
        this.timeStart = firstTime;
        this.dataStartPosition = format.length();

        //special cases...ms timestamps (and more precision) have date included in them.
        if (precision.equals(Precision.Milliseconds) && (milliEnd-milliStart) >= 12) {
            this.timestampIncludesDate = true;
        }
    }
    public long getDataStartPosition() {
        return this.dataStartPosition;
    }
    public long getHour(String strTime, boolean isPartial) {
        int prefix = isPartial ? getPrefix() : 0;
        if (hourStart > NOT_SET && hourEnd > NOT_SET&& (prefix+strTime.length()) >= hourEnd) {
            return getTimePart(hourStart-prefix,hourEnd-prefix,strTime);
        } else {
            return NOT_SET;
        }
    }

    public long getMinute(String strTime, boolean isPartial) {
        int prefix = isPartial ? getPrefix() : 0;
        if (minStart > NOT_SET && minEnd > NOT_SET&& (prefix+strTime.length()) >= minEnd) {
            return getTimePart(minStart-prefix,minEnd-prefix,strTime);
        } else {
            return NOT_SET;
        }
    }

    public long getSecond(String strTime, boolean isPartial) {
        int prefix = isPartial ? getPrefix() : 0;
        if (secondStart > NOT_SET && secondEnd > NOT_SET&& (prefix+strTime.length()) >= secondEnd) {
            return getTimePart(secondStart-prefix,secondEnd-prefix,strTime);
        } else {
            return NOT_SET;
        }
    }

    public long getMillis(String strTime, boolean isPartial) {
        int prefix = isPartial ? getPrefix() > NOT_SET ? getPrefix() : 0 : 0;
        if (milliStart > NOT_SET && milliEnd > NOT_SET && (prefix+strTime.length()) >= milliEnd) {
            return getTimePart(milliStart-prefix,milliEnd-prefix,strTime);
        } else {
            return NOT_SET;
        }
    }

    public long getMicros(String strTime, boolean isPartial) {
        int prefix = isPartial ? getPrefix() : 0;
        if (microStart > NOT_SET && microEnd > NOT_SET&& (prefix+strTime.length()) >= microEnd) {
            return getTimePart(microStart-prefix,microEnd-prefix,strTime);
        } else {
            return NOT_SET;
        }
    }

    public long getNanos(String strTime, boolean isPartial) {
        int prefix = isPartial ? getPrefix() : 0;
        if (nanoStart > NOT_SET && nanoEnd > NOT_SET&& (prefix+strTime.length()) >= nanoEnd) {
            return getTimePart(nanoStart-prefix,nanoEnd-prefix,strTime);
        } else {
            return NOT_SET;
        }
    }

    private long getTimePart(int start, int end, String time) {
        try {
            return Long.valueOf(time.substring(start,end+1));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid timestamp supplied for timestampFormat: " + time + ". Expected timestampFormat: " + strFormat);
        }
    }

    /**
     * Parse a string time into a Timestamp object, nanosecond precision.
     * @param strTime
     * @return
     */
    public Timestamp asTimestamp(String strTime) {
        return asTimestamp(strTime,getPrecision());
    }


    /**
     * Parse a string time into a Timestamp object in a specified precision.
     * @param strTime
     * @param p
     * @return
     */
    public Timestamp asTimestamp(String strTime, Precision p) {
        return new Timestamp(asLong(strTime,p,false),p);
    }

    public Timestamp asTimestampFromPartial(String strTime) {
        Precision p = getPrecisionOf(strTime);
        return new Timestamp(asLong(strTime, p,true),p);
    }

    /**
     * Get the precision of a given time with reference to what we expect in our
     * format. For example, our format may be HH:MM:SS.iii but we may be asked for
     * a timestamp in a shorter format e.g. HH:MM.
     * This only works if it is purely time - no prefix crap is in strTime.
     * @param strTime
     * @return
     */
    private Precision getPrecisionOf(String strTime) {
        int pos = this.getPrefix() + strTime.length();
        if (this.hasNanos() && pos-1 > this.microEnd) return Precision.Nanos;
        if (this.hasMicros() && pos-1 > this.milliEnd) return Precision.Microseconds;
        if (this.hasMillis() && pos-1 > this.secondEnd) return Precision.Milliseconds;
        if (this.hasSecond() && pos-1 > this.minEnd) return Precision.Seconds;
        if (this.hasMins() && pos-1 > this.hourEnd) return Precision.Minutes;
        if (this.hasHours()) return Precision.Hours;
        throw new IllegalArgumentException("Invalid time for format precision check: " + strTime);
    }

    /**
     * Parse a string time of today, into nanoseconds.
     * @param strTime
     * @return
     */
    public long asLong(String strTime,Precision p,boolean partialTimestamp) {
        long hour = this.getHour(strTime,partialTimestamp);
        long min = this.getMinute(strTime,partialTimestamp);
        long sec = this.getSecond(strTime,partialTimestamp);
        long millis = this.getMillis(strTime,partialTimestamp);
        long us = this.getMicros(strTime,partialTimestamp);
        long ns = this.getNanos(strTime,partialTimestamp);
        long time = 0;
        //TODO check against the P granularity?
        time = hour > NOT_SET ? time + (hour*TimestampUtils.NANOS_PER_HOUR) : time;
        time = min > NOT_SET ? time + (min*TimestampUtils.NANOS_PER_MIN) : time;
        time = sec > NOT_SET ? time + (sec*TimestampUtils.NANOS_PER_SEC) : time;
        time = millis > NOT_SET ? time + (millis*TimestampUtils.NANOS_PER_MILLI) : time;
        time = us > NOT_SET ? time + (us*TimestampUtils.NANOS_PER_MICRO) : time;
        time = ns >  NOT_SET ? time + ns : time;
        PrecisionMultiplier pm = TimestampUtils.getMultiplier(Precision.Nanos,p);
        long nanos = timestampIncludesDate ? time : TimestampUtils.getTodayMidnight(Precision.Nanos).getTimestamp()+time;
        return pm.getAdjustedTimestamp(nanos);//long timestamps already have the date in them...
    }

    public Precision getPrecision() {
        return precision;
    }

    /**
     * Given a line, split it into two bits, a timestamp and the rest of the line.
     * @param line
     * @return
     */
    public Object[] split(String line) {
        Object[] bits = new Object[2];
        bits[0] = asTimestamp(line.substring(getTimeStart(),getTimeEnd()+1));
        bits[1] = line.substring((int)this.getDataStartPosition()).trim();
        return bits;
    }
}
