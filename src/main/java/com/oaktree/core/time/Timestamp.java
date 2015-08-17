package com.oaktree.core.time;

/**
 * Simple representation of a timestamp.
 * A timestamp has a time and a unit that says what precision this value is in.
 * A timestamp is immutable.
 *
 * Created by ij on 03/08/15.
 */
public class Timestamp implements Comparable {
    /**
     * The timestamp. Can be a point in time or just a lapse since some point.
     */
    private long timestamp;
    /**
     * The unit this timestamp value represents.
     */
    private Precision precision;

    public Timestamp(long time, Precision precision) {
        this.timestamp = time;
        this.precision = precision;
    }

    /**
     * Get the timestamp value.
     * @return
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Get the precision of this timestamp.
     * @return
     */
    public Precision getPrecision() {
        return this.precision;
    }

    @Override
    public String toString() {
        return "Time: " + timestamp + " precision: " + precision;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof Timestamp) {
            Timestamp t = (Timestamp)(o);
            return t.precision.equals(precision) && t.timestamp == timestamp;
        }
        return false;
    }

    /**
     * Compare a timestamp to this, but when both are set to precision p.
     * If p = our precision, then this is equivalent to equals(object).
     * @param t
     * @param p
     * @return
     */
    public boolean equals(Timestamp t, Precision p) {
        if (p.equals(this.getPrecision()) && t.getPrecision().equals(p)) {
            return this.equals(t);
        }
        PrecisionMultiplier pm = TimestampUtils.getMultiplier(this.getPrecision(),p);
        long ourTimestamp = pm.getAdjustedTimestamp(getTimestamp());
        pm = TimestampUtils.getMultiplier(t.getPrecision(),p);
        long theirTimestamp = pm.getAdjustedTimestamp(t.getTimestamp());
        return ourTimestamp == theirTimestamp;
    }

    @Override
    public int hashCode() {
        return (int)(timestamp + precision.hashCode());
    }

    @Override
    public int compareTo(Object o) {
       if (o instanceof Timestamp) {
            Timestamp t = (Timestamp)(o);
            if (!t.getPrecision().equals(precision)) {
                throw new IllegalArgumentException("Incompatible precisions on timestamps.");
            }
            long diff= getTimestamp()-t.getTimestamp();
            if (diff > 0) {
                return 1;
            }
            if (diff < 0) {
                return -1;
            }
            return 0;
        }
        return -1;
    }

    /**
     * Check if timestamp is before the target - will explode if different precisions.
     * @param t
     * @return
     */
    public boolean isBefore(Timestamp t) {
        if (!precision.equals(t.getPrecision())) {
            throw new IllegalArgumentException("Timestamps have different precisions which is not supported in this method.");
        }
        return timestamp < t.getTimestamp();
    }

    /**
     * Check if timestamp is equal to the target - will explode if different precisions.
     * TODO same now as equals....retire?
     * @param t
     * @return
     */
    public boolean isEqual(Timestamp t) {
        if (!precision.equals(t.getPrecision())) {
            throw new IllegalArgumentException("Timestamps have different precisions which is not supported in this method.");
        }
        return timestamp == t.getTimestamp();
    }

    /**
     * Check if timestamp is after the target - will explode if different precisions.
     * @param t
     * @return
     */
    public boolean isAfter(Timestamp t) {
        if (!precision.equals(t.getPrecision())) {
            throw new IllegalArgumentException("Timestamps have different precisions which is not supported in this method.");
        }
        return timestamp > t.getTimestamp();
    }

    /**
     * Check if this timestamp is after timestamp t, when both are in precision p.
     * @param t
     * @param p
     * @return
     */
    public boolean isAfter(Timestamp t, Precision p) {
        if (p.equals(this.getPrecision()) && t.getPrecision().equals(p)) {
            return this.isAfter(t);
        }
        PrecisionMultiplier pm = TimestampUtils.getMultiplier(this.getPrecision(),p);
        long ourTimestamp = pm.getAdjustedTimestamp(getTimestamp());
        pm = TimestampUtils.getMultiplier(t.getPrecision(),p);
        long theirTimestamp = pm.getAdjustedTimestamp(t.getTimestamp());
        return ourTimestamp > theirTimestamp;
    }

    /**
     * Check if this timestamp is after timestamp t, when both are in precision p.
     * @param t
     * @param p
     * @return
     */
    public boolean isBefore(Timestamp t, Precision p) {
        if (p.equals(this.getPrecision()) && t.getPrecision().equals(p)) {
            return this.isBefore(t);
        }
        PrecisionMultiplier pm = TimestampUtils.getMultiplier(this.getPrecision(),p);
        long ourTimestamp = pm.getAdjustedTimestamp(getTimestamp());
        pm = TimestampUtils.getMultiplier(t.getPrecision(),p);
        long theirTimestamp = pm.getAdjustedTimestamp(t.getTimestamp());
        return ourTimestamp < theirTimestamp;
    }

    /**
     * Get difference between two timestamps.
     * Will throw Exception if the precisions are not equivalent.
     * @param t - timestamp to compare to.
     * @return
     */
    public long getDifference(Timestamp t) {
        if (!precision.equals(t.getPrecision())) {
            throw new IllegalArgumentException("Timestamps have different precisions which is not supported in this method.");
        }
        return timestamp-t.getTimestamp();
    }

    /**
     * Get the difference in a specified precision.
     *
     * @param t
     * @param p
     * @return
     */
    public long getDifference(Timestamp t,Precision p) {
        PrecisionMultiplier ourM = TimestampUtils.getMultiplier(precision,p);
        PrecisionMultiplier theirM = TimestampUtils.getMultiplier(t.getPrecision(),p);
        return ourM.getAdjustedTimestamp(getTimestamp()) - theirM.getAdjustedTimestamp(t.getTimestamp());
    }

    /**
     * Get a difference, converting our timestamp into the target timestamps precision.
     * @param t
     * @return
     */
    public long getDifferenceAndConvertIfRequired(Timestamp t) {
        PrecisionMultiplier modifier = TimestampUtils.getMultiplier(getPrecision(),t.getPrecision());
        return modifier.adjust(timestamp)-t.getTimestamp();
    }



    /**
     * Create a new timestamp based on this with different precision.
     *
     * @param targetPrecision
     */
    public Timestamp convertToPrecision(Precision targetPrecision) {
        PrecisionMultiplier modifier = TimestampUtils.getMultiplier(getPrecision(),targetPrecision);
        return new Timestamp(modifier.adjust(getTimestamp()),targetPrecision);
    }

    /**
     * Add timestamp to this timestamp, returning a new object with this new value
     * and our precision.
     * @param ts
     * @return
     */
    public Timestamp add(Timestamp ts) {
        PrecisionMultiplier modifier = TimestampUtils.getMultiplier(precision,ts.getPrecision());
        long v = ts.convertToPrecision(precision).getTimestamp();
        return new Timestamp(v+getTimestamp(),precision);
    }

    /**
     * Take one timestamp off this timestamp and return a new timestamp, all in our precision.
     *
     * @param ts
     * @return
     */
    public Timestamp subtract(Timestamp ts) {
        PrecisionMultiplier modifier = TimestampUtils.getMultiplier(precision,ts.getPrecision());
        long v = ts.convertToPrecision(precision).getTimestamp();
        return new Timestamp(v-getTimestamp(),precision);
    }
}
