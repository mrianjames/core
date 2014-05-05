package com.oaktree.core.types;

import com.oaktree.core.utils.MathUtils;
import org.apache.commons.math.distribution.ExponentialDistribution;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 30/07/11
 * Time: 21:07
 */
public class Quantity extends Real {
    public Quantity(long exponent, long mantissa) {
        super(exponent, mantissa);
    }
    public Quantity() {
        super(MathUtils.defaultQtyExponent,MathUtils.NOT_SET_LONG);
    }
    public Quantity(Quantity qty) {
        super(qty.getExponent(),qty.getMantissa());
    }
    public Quantity copy() {
        return new Quantity(this.getExponent(),this.getMantissa());
    }


}
