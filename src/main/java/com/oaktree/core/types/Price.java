package com.oaktree.core.types;

import com.oaktree.core.utils.MathUtils;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 30/07/11
 * Time: 21:04
 */
public class Price extends Real {
    public Price(long exponent, long mantissa) {
        super(exponent, mantissa);
    }
    public Price(Price prx) {
        super(prx.getExponent(),prx.getMantissa());
    }
    public Price() {
        super(MathUtils.defaultPrxExponent,MathUtils.NOT_SET_LONG);
    }
    public Price copy() {
        return new Price(this.getExponent(),this.getMantissa());
    }
}
