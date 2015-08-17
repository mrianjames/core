package com.oaktree.core.time;

/**
 * A timestamp precision that a timestamp value is in. Essentially the precision is the number of digits
 * that are in the value.
 */
public enum Precision {

    Hours(PrecisionMultiplier.secondToHoursMultiplier),
    Minutes(PrecisionMultiplier.secondToMinutesMultiplier),
    Seconds(PrecisionMultiplier.secondToSecondMultiplier),
    Milliseconds(PrecisionMultiplier.secondToMillisecondMultiplier),
    Microseconds(PrecisionMultiplier.secondToMicrosMultiplier),
    Nanos(PrecisionMultiplier.secondToNanosMultiplier);

    private PrecisionMultiplier nanoMultiplier;
    Precision(PrecisionMultiplier nanoMultiplier) {
        this.nanoMultiplier = nanoMultiplier;
    };
    public PrecisionMultiplier getToNanoMultiplier() {
        return nanoMultiplier;
    }
    /**
     * Get the granularity of this precision, for comparison purposes.
     * 0 is least granular (most coarse)
     */
    public int getGranularity() {
        return ordinal();
    }

    /**
     * Get if this precision is more fine grained than the argument.
     *
     * @param target
     * @return
     */
    public boolean isMoreFineGrainedThan(Precision target) {
        return getGranularity()-target.getGranularity() >= 0;
    }

    /**
     * Get if this precision is more course grained than the argument.
     *
     * @param target
     * @return
     */
    public boolean isMoreCourseGrainedThan(Precision target) {
        return getGranularity()-target.getGranularity() < 0;
    }

    /**
     * Convert a value from a precision into our precision.
     * @param l
     * @param precision
     * @return
     */
    public long adjust(long l, Precision precision) {
        PrecisionMultiplier m = TimestampUtils.getMultiplier(this,precision);
        return m.adjust(l);
    }
}
