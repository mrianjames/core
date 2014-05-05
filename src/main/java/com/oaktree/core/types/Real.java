package com.oaktree.core.types;

import java.io.Serializable;

import com.oaktree.core.utils.MathUtils;
import com.oaktree.core.utils.Text;

/**
 * Basic decimal implementation - a number is represented by
 * a mantissa and exponent.
 * For example
 * 1005 mantissa, -1 exponent = 100.5
 * Mantissa values from Long.MIN_VALUE -> Long.MAX_VALUE-1
 * A real is initialized as LONG.MAX_VALUE for both - test with isSet
 *
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 30/07/11
 * Time: 21:04
 */
public class Real implements Serializable{
    private long mantissa = MathUtils.NOT_SET_LONG;
    private long exponent = MathUtils.NOT_SET_LONG;

    /**
     * 
     * @return mantissa
     */
    public long getMantissa() {
        return mantissa;
    }
    /**
     * 
     * @return exponent
     */
    public long getExponent() {
        return exponent;
    }

    public Real(long exponent,long mantissa) {
        this.exponent = exponent;
        this.mantissa = mantissa;
    }
    public Real() {
    }
    public Real(Real value) {
        this(value.getExponent(),value.getMantissa());
    }
    /**
     * 
     * @param value
     */
    public void copy(Real value) {
        this.exponent = value.exponent;
        this.mantissa = value.mantissa;
    }

    /**
     * 
     * @param exponent
     */
    public void setExponent(long exponent) {
        this.exponent = exponent;
    }

    /**
     * 
     * @param mantissa
     */
    public void setMantissa(long mantissa) {
        this.mantissa = mantissa;
    }

    /**
     * 
     */
    public void unset() {
        mantissa = MathUtils.NOT_SET_LONG;
        //exponent = MathUtils.NOT_SET_LONG;
    }
    
    @Override
    public String toString() {
        if (isSet()) {
//            StringBuilder b = new StringBuilder();
//            b.append(mantissa);
//            b.append(Text.LEFT_SQUARE_BRACKET);
//            b.append(exponent);
//            b.append(Text.RIGHT_SQUARE_BRACKET);
//            return b.toString();
            return Text.NOTHING +mantissa + Text.LEFT_SQUARE_BRACKET + exponent + Text.RIGHT_SQUARE_BRACKET; //seems bit quicker.
            //return Text.NOTHING + MathUtils.toDecimal(mantissa,exponent);          //seems faster still?

        } else {
            return Text.NOTSET;
        }

    }

    @Override
    public boolean equals(Object e) {
        if (e instanceof Real) {
            Real f = (Real)e;
            if (this.isNotSet()) {
                if (f.isNotSet()) {
                    return true;
                } else {
                    return false;
                }
            }
            return f.getExponent() == exponent && f.getMantissa() == mantissa;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int)((17 * mantissa) + exponent);
    }


    public boolean isSet() {
        return mantissa != MathUtils.NOT_SET_LONG;
    }

    /**
     * Check if this real is either 0 or not set.
     * @return true if zero or not set.
     */
    public boolean isZeroOrNotSet() {
        return (this.mantissa == 0 || this.mantissa == MathUtils.NOT_SET_LONG);
    }

    /**
     * 
     * @return true if not set
     */
    public boolean isNotSet() {
        return mantissa == MathUtils.NOT_SET_LONG;
    }

    /**
     * 
     * @return true if zero
     */
    public boolean isZero() {
        return mantissa == 0;
    }
    /**
     * 
     * @return true if not zero.
     */
    public boolean isNotZero() {
            return mantissa != 0;
    }

    /**
     * 
     * @param x
     * @return 0 if equal, -ve if less, +ve if more.
     */
    public int compareTo(Real x) {
        return (int)(mantissa - x.getMantissa());
    }

    /**
     * Return copy of this real.
     * @return
     */
    public Real copy() {
        Real copy = new Real();
        copy.setExponent(exponent);
        copy.setMantissa(mantissa);
        return copy;
    }
}
