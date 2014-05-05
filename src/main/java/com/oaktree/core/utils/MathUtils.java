package com.oaktree.core.utils;

import com.oaktree.core.logging.*;
import com.oaktree.core.types.Price;
import com.oaktree.core.types.Quantity;
import com.oaktree.core.types.Real;

import java.math.BigDecimal;

public class MathUtils {


	public final static double EPSILON = 0.000000001;
	public static final double NANOS_IN_MILLI = 1000000;
	public final static double NOT_SET = Double.NEGATIVE_INFINITY;
    public final static long NOT_SET_LONG = Long.MIN_VALUE;
	public static final double ZERO = 0.0d;
    public final static Real notSet = new Real();
    public final static Price notSetPrice = new Price();
    public final static Quantity notSetQuantity = new Quantity();
    public static final long defaultPrxExponent = -8;
    public static final long defaultQtyExponent = -3;
    public static Quantity zeroQuantity = new Quantity(defaultQtyExponent,0);
    public static Price zeroPrice = new Price(defaultPrxExponent,0);
    public static Real zeroReal = new Real(defaultQtyExponent,0);


    public static boolean areEqualsBasic(double x, double y) {
		return Double.isNaN(x) && Double.isNaN(y) || x == y;
	}
	
	public static boolean areEquals(double x, double y) {
		return areEqualsBasic(x,y) || Math.abs(y-x) <= EPSILON;
	}
	
	/**
	 * compare x against y
	 * @param x
	 * @param y
	 * @return -1 (x is < y), 1 (x > y), 0 values are equals (with precision error).
	 */
	public static int compareTo(double x, double y) {
		if (areEquals(x,y)) {
			return 0;
		} else if (x < y) {
			return -1;
		} else {
			return 1;
		}
	}
	
	public static boolean areNotEqual(double x, double y) {
		return !areEquals(x,y);
	}
	
	public final static void main(String[] args) {
		double x = 1.10000000000000000001;
		double y = new BigDecimal("1.1").doubleValue();
		
		System.out.println(x + ", " + y + ": " + areEquals(x,y));
	}
	
	public final static double getMid(double x, double y) {
		return x + (y-x)/2;
	}

    public final static Price getMid(Price a, Price b) {
        Price r = new Price();
        r.setExponent(a.getExponent());
        MathUtils.subtract(b,a,r);
        r.setMantissa(r.getMantissa()/2);
        add(r,a);
        return r;
    }

	public static boolean isZero(double x) {
		return MathUtils.areEquals(x, 0);
	}

    public static boolean isGreaterThanZero(double notMySize) {
       if (isZero(notMySize)) {
           return false;
       }
       if (notMySize > 0) {
           return true;
       }
       return false;
    }

    /**
     * Return the higher of two numbers
     * @param qty1
     * @param qty2
     * @return
     */
    public static double max(double qty1, double qty2) {
        return qty1 > qty2 ? qty1 : qty2;
    }


    /**
     * Return the lower of two numbers
     * @param qty1
     * @param qty2
     * @return
     */
    public static double min(double qty1, double qty2) {
        return qty1 < qty2 ? qty1 : qty2;
    }

    static long[] powers = new long[]{};
	static {
		int max = 10;
		powers = new long[max];
		for (int i = 0; i < max; i++) {
			powers[i] = (long)Math.pow(10,i);
		}
	}

	public static long power(long radix, long exp) {
		if (exp < powers.length && exp > 0) {
			return powers[(int)exp];
		} else {
			return (long)Math.pow(radix, exp);
		}
	}

	public static double toDecimal(long mantissa,long exponent) {
		if (exponent < 0) {
			double p = power(10,-exponent);
			return mantissa * (1/p);
		}
		double p = power(10,exponent);
		return mantissa * (p);
	}

    public static long toLong(Real real) {
        return toLong(real.getMantissa(),real.getExponent());
    }
    public static long toLong(long mantissa,long exponent) {
        if (exponent < 0) {
			double p = power(10,-exponent);
			return (long)(mantissa * (1/p));
		}
		double p = power(10,exponent);
		return (long)(mantissa * (p));
    }

    public static double toDecimal(Real real) {
        return toDecimal(real.getMantissa(),real.getExponent());
    }

    public static Real fromDecimal(double value,long exponent) {
		Real real = new Real();
        fromDecimal(real,value,exponent);
        return real;
	}

    public static Price fromDecimalPrice(double value, long exponent) {
        Price price = new Price();
        fromDecimal(price,value,exponent);
        return price;
    }

    public static Quantity fromDecimalQuantity(double value, long exponent) {
        Quantity price = new Quantity();
        fromDecimal(price,value,exponent);
        return price;
    }

    public static Quantity fromDecimalQuantity(double value) {
        return fromDecimalQuantity(value,MathUtils.defaultQtyExponent);
    }

    public static Price fromDecimalPrice(double value) {
            return fromDecimalPrice(value,MathUtils.defaultPrxExponent);
        }


    public static void fromDecimal(Real real, double value, long exponent) {
        //TODO ROUND
        if (exponent < 0) {
			long p = power(10,-exponent);
			long mantissa = (long)(value * p);
			real.setMantissa(mantissa);
            real.setExponent(exponent);
		} else {
            long p = power(10,exponent);
            long mantissa = (long)(value / p);
            real.setMantissa(mantissa);
            real.setExponent(exponent);
        }
    }

    /**
     * Add realb to reala, normalising the exponents if neccesary.
     * @param reala
     * @param realb
     */
    public static void add(Real reala,Real realb) {
        if (reala == null) {
            return;
        }
        long mantissa = reala.getMantissa();
        if(reala.isNotSet()) {
            mantissa = 0;
        }
        //do the maths
        reala.setMantissa(mantissa+norm(reala,realb));
    }

    /**
     * Subtract realb from reala, normalising the exponents if neccesary
     * @param reala
     * @param realb
     */
    public static void subtract(Real reala,Real realb) {
        if (reala == null || reala.isNotSet()) {
            return;
        }
        if (realb == null || realb.isNotSet()) {
            return;
        }
        //do the maths
        reala.setMantissa(reala.getMantissa()-norm(reala,realb));
    }

    /**
     * Subtract realb from reala, normalising the exponents if neccesary
     * @param reala
     * @param realb
     */
    public static void subtract(Real reala,Real realb, Real target) {
        if (reala == null || reala.isNotSet()) {
        	throw new IllegalArgumentException("Cannot subtract real from null");
        }
        if (realb == null || realb.isNotSet()) {
        	//a-null = a
            target.copy(reala);
            return;
        }
        //do the maths
        target.setMantissa(reala.getMantissa()-norm(reala,realb));
    }

    /**
     * Multiply realb and reala, normalising the exponents if neccesary
     * TODO only works for negative exponent so far
     * @param reala
     * @param realb
     */
    public static void multiply(Real reala,Real realb) {
        if (reala == null || reala.equals(MathUtils.notSet)) {
            return;
        }
        //do the maths
        reala.setMantissa((reala.getMantissa()*norm(reala,realb))/power(10,-reala.getExponent()));
    }

    public static void multiply(Real reala,Real realb, Real target) {
            if (reala == null || reala.equals(MathUtils.notSet)) {
                return;
            }
            //do the maths
        long norm = norm(reala,realb);
        long power = power(10,-reala.getExponent());
            target.setMantissa((reala.getMantissa()*norm)/power);
        target.setExponent(reala.getExponent());
//        long pwr = power(10,-reala.getExponent()); //suspision of badness - wont work
//            long am = reala.getMantissa() * pwr;
//            long norm = norm(reala,realb);
//
//            //do the maths
//            target.setMantissa((am*norm));
//            target.setExponent(reala.getExponent());
        }

    /**
         * Divide realb and reala, normalising the exponents if neccesary
         * TODO only works for negative exponent so far
         * @param reala
         * @param realb
         */
        public static void divide(Real reala,Real realb) {
            if (reala == null || reala.equals(MathUtils.notSet)) {
                return;
            }
            long pwr = power(10,-reala.getExponent());
            long am = reala.getMantissa() * pwr;
            long norm = norm(reala,realb);

            //do the maths
            reala.setMantissa((am/norm));
        }


    public static void divide(Real reala, Real realb, Real target) {
        if (reala == null || reala.equals(MathUtils.notSet)) {
                return;
            }
            long pwr = power(10,-reala.getExponent()); //suspision of badness - wont work
            long am = reala.getMantissa() * pwr;
            long norm = norm(reala,realb);

            //do the maths
            target.setMantissa((am/norm));
            target.setExponent(reala.getExponent());
    }

    private static long norm(Real reala, Real realb) {
        if (realb == null || realb.equals(MathUtils.notSet)) {
            return 0;
        }
        //normalise exponents to a
        long diff = reala.getExponent() - realb.getExponent();
        if (diff > 0) {
            diff = power(10,diff);
            return realb.getMantissa() / diff;
        } else if (diff < 0) {
            diff = power(10,-diff);
            return realb.getMantissa() * diff;
        } else {
           return realb.getMantissa();
        }
    }

    private static long getMultiplier(Real reala, Real realb) {
        long diff = reala.getExponent() - realb.getExponent();
        if (diff == 0) {
            return 1;
        }
        if (diff > 0) {
            diff = power(10,diff);
        } else {
            diff = -(power(10,-diff));
        }
        return diff;
    }

    public static boolean isZero(Real real) {
        return real.getMantissa() == 0;
    }

    public static Quantity minQty(Quantity a, Quantity b) {
         return (Quantity)(min(a,b));
    }

    public static Real min(Real a,Real b) {
        if (a.getExponent() == b.getExponent()) {
            return (a.getMantissa() - b.getMantissa() > 0) ? b : a;
        } else {
            long diff = getMultiplier(a,b);
            return (a.getMantissa() - (b.getMantissa()/diff) > 0) ? b : a;
        }
    }

    public static Price minPrx(Price a, Price b) {
        return (Price)(min(a,b));
    }

    public static boolean isLessThanZero(Real a) {
        return a.getMantissa() < 0;
    }

    public static boolean isGreaterThanZero(Real a) {
        return a.getMantissa() > 0;
    }

    /**
     * 'is a < b
     * @param a
     * @param b
     * @return
     */
    public static boolean isLessThan(Real a, Real b) {
        long diff = getMultiplier(a,b);
        return a.getMantissa() < (b.getMantissa()/diff);
    }

    public static boolean isLessThanOrEqual(Real a, Real b) {
        long diff = getMultiplier(a,b);
        return a.getMantissa() <= (b.getMantissa()/diff);
    }

    public static boolean isGreaterThan(Real a, Real b) {
        long diff = getMultiplier(a,b);
        return a.getMantissa() > (b.getMantissa()/diff);
    }

    public static boolean isGreaterThanOrEqual(Real a, Real b) {
        long diff = getMultiplier(a,b);
        return a.getMantissa() >= (b.getMantissa()/diff);
    }

    public static void abs(Real price) {
        if (price.getMantissa() < 0) {
            price.setMantissa(-price.getMantissa());
        }
    }

    public static Price absPrice(Price real) {
        Price prx = new Price(real);
        if (real.getMantissa() < 0) {
            prx.setMantissa(-real.getMantissa());
        }
        return prx;
    }

/**
     * pd = chars from rhs -> dot that are not trailing zeros.
     *
     * 21.234 -> 2123400000...
     * 0.0034 -> 0000340000...
     *
     * @param number
     * @param exponent
     * @return
     */
    public static Real stringToReal(String number, int exponent) {
        String[] bits = number.split("[.]");
        StringBuilder composite = new StringBuilder(bits[0] + bits[1]);
        int pd = number.length()-number.indexOf(Text.PERIOD)-1;
        int buf = -exponent - pd;
        for (int i = 0; i < buf; i++) {
            composite.append(Text.ZERO);
        }
        long mantissa = Long.valueOf(composite.toString());
        return new Real(exponent,mantissa);
    }

}
