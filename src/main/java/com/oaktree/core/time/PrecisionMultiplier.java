package com.oaktree.core.time;

/**
 * A multiplier of precisions. Holds the multiplier and whether to multiply or divide.
 */
public class PrecisionMultiplier {

    /**
     * Indicator of the "length" that a long timestamp.
     */


    /**
     * Useful shortcut for no multiplication or division required - same precisions.
     */
    private static long NO_MULTIPLY_VALUE = 1;
    public static PrecisionMultiplier NO_MULTIPLY = new PrecisionMultiplier(NO_MULTIPLY_VALUE,true);

    /**
     * Well known or requested multipliers. For minutes...
     */
    public static PrecisionMultiplier minuteToMinutesMultiplier = NO_MULTIPLY;
    public static PrecisionMultiplier minuteToSecondMultiplier = new PrecisionMultiplier(60l,true);
    public static PrecisionMultiplier minuteToMillisecondMultiplier = new PrecisionMultiplier(60000l,true);
    public static PrecisionMultiplier minuteToMicrosMultiplier = new PrecisionMultiplier(60000000l,true);
    public static PrecisionMultiplier minuteToNanosMultiplier = new PrecisionMultiplier(60000000000l,true);

    /**
     * Well known or requested multipliers. For seconds...
     * so for 1 second what do we need to multiply/divide by to see it in microseconds.
     */
    public static PrecisionMultiplier secondToMinutesMultiplier = new PrecisionMultiplier(60l,false);
    public static PrecisionMultiplier secondToSecondMultiplier = NO_MULTIPLY;
    public static PrecisionMultiplier secondToMillisecondMultiplier = new PrecisionMultiplier(1000l,true);
    public static PrecisionMultiplier secondToMicrosMultiplier = new PrecisionMultiplier(1000000l,true);
    public static PrecisionMultiplier secondToNanosMultiplier = new PrecisionMultiplier(1000000000l,true);

    /**
     * Well known or requested multipliers. For seconds...
     * so for 1 ms what do we need to multiply/divide by to see it in microseconds.
     */
    public static PrecisionMultiplier millisecondsToMinutesMultiplier = new PrecisionMultiplier(60000l,false);
    public static PrecisionMultiplier millisecondsToSecondMultiplier = new PrecisionMultiplier(1000l,false);
    public static PrecisionMultiplier millisecondsToMillisecondMultiplier = NO_MULTIPLY;
    public static PrecisionMultiplier millisecondsToMicrosMultiplier = new PrecisionMultiplier(1000,true);
    public static PrecisionMultiplier millisecondsToNanosMultiplier = new PrecisionMultiplier(1000000,true);

    /**
     * Well known or requested multipliers. For micros...
     */
    public static PrecisionMultiplier microToMinutesMultiplier = new PrecisionMultiplier(60000000l,false);
    public static PrecisionMultiplier microToSecondMultiplier = new PrecisionMultiplier(1000000,false);
    public static PrecisionMultiplier microToMillisecondMultiplier = new PrecisionMultiplier(1000,false);
    public static PrecisionMultiplier microToMicrosMultiplier = NO_MULTIPLY;
    public static PrecisionMultiplier microToNanosMultiplier = new PrecisionMultiplier(1000,true);

    /**
     * Well known or requested multipliers. For nanoseconds...
     */
    public static PrecisionMultiplier nanoToMinutesMultiplier = new PrecisionMultiplier(60,false);
    public static PrecisionMultiplier nanoToSecondMultiplier = new PrecisionMultiplier(1000000000,false);
    public static PrecisionMultiplier nanoToMillisecondMultiplier = new PrecisionMultiplier(1000000,false);
    public static PrecisionMultiplier nanoToMicrosMultiplier = new PrecisionMultiplier(1000,false);
    public static PrecisionMultiplier nanoToNanosMultiplier = NO_MULTIPLY;


    /**
     * The value to multiply or divide the value by.
     */
    private long multiplier;
    /**
     * Whether to multiply or divide.
     */
    private boolean multiply;
    public PrecisionMultiplier(long multiplier, boolean multiply) {
        this.multiplier = multiplier;
        this.multiply = multiply;
    }
    public long getMultiplier() {
        return this.multiplier;
    }

    /**
     * Should we multiply (as opposed to divide) by the multiplier.
     * @return
     */
    public boolean shouldMultiply() {
        return multiply;
    }

    /**
     * Should we divide (as opposed to multiply) by the multiplier.
     * @return
     */
    public boolean shouldDivide() {
        return !multiply;
    }

    /**
     * Is this a no multiplication or division required value?
     * @return
     */
    public boolean isNoMultiply() {
        return multiplier == NO_MULTIPLY_VALUE;
    }

    @Override
    public String toString() {
        return multiply ? "Multiply by " + multiplier : " Divide by " + multiplier;
    }
    @Override
    public boolean equals(Object t) {
        if (t instanceof PrecisionMultiplier) {
            return ((PrecisionMultiplier) t).multiplier == multiplier && ((PrecisionMultiplier) t).multiply == multiply;
        }
        return false;
    }
    @Override
    public int hashCode() {
        return (int)(32 + (int)multiplier + ((multiply?1:0)+23));
    }

    /**
     * Adjust a value by this multiplier.
     * @param l
     * @return
     */
    public long adjust(long l) {
        if (multiply) {
            return l * multiplier;
        } else {
            return l / multiplier;
        }
    }
}
