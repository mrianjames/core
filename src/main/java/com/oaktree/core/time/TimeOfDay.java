package com.oaktree.core.time;

/**
 * Created by ij on 13/08/15.
 */
public class TimeOfDay {
    public TimeOfDay() {}
    @Override
    public String toString() {
        return TimestampUtils.toString(this);
    }
    private long hour;
    private long minute;
    private long seconds;
    private long milliseconds;
    private long microseconds;
    private long nanoseconds;

    public long getHour() {
        return this.hour;
    }

    public long getMinute() {
        return minute;
    }

    public void setMinute(long minute) {
        this.minute = minute;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public long getMicroseconds() {
        return microseconds;
    }

    public void setMicroseconds(long microseconds) {
        this.microseconds = microseconds;
    }

    public long getNanoseconds() {
        return nanoseconds;
    }

    public void setNanoseconds(long nanoseconds) {
        this.nanoseconds = nanoseconds;
    }

    public void setHour(long hour) {

        this.hour = hour;
    }
}
