package com.oaktree.core.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import com.oaktree.core.types.Price;
import com.oaktree.core.types.Quantity;
import com.oaktree.core.types.Real;
import junit.framework.Assert;

import org.junit.Test;


public class TestMathUtils {

	private final static double EPSILON = 0.000000001;
	
	@Test
	public void testAreEquals() {
		double x = 1.10000000000000000001;
		double y = new BigDecimal("1.1").doubleValue();
		Assert.assertTrue(MathUtils.areEquals(x, y));
		//Assert.assertFalse(x == y);
	}
	
	@Test
	public void testCompareTo() {
		double x = 1;
		double y = 2;
		Assert.assertEquals(-1,MathUtils.compareTo(x, y),EPSILON);
		x = 2; y = 1;
		Assert.assertEquals(1,MathUtils.compareTo(x, y),EPSILON);
		x = 2;y =2;
		Assert.assertEquals(0,MathUtils.compareTo(x, y),EPSILON);
		x = 2;y =2.00000000000000000000000000002;
		Assert.assertEquals(0,MathUtils.compareTo(x, y),EPSILON);
		//Assert.assertFalse(x == y);
	}
	
	@Test
	public void testNotEquals() {
		double x = 1.1;
		double y = 2.2;
		double z = 1.1;
		Assert.assertTrue(MathUtils.areNotEqual(x, y));
		Assert.assertFalse(MathUtils.areNotEqual(x, z));
	}
	
	@Test
	public void getMid() {
		double x = 0;
		double y = 100;
		Assert.assertEquals(MathUtils.getMid(x, y), 50.0,EPSILON);
		x = 100; y = 0;
		Assert.assertEquals(MathUtils.getMid(x, y), 50.0,EPSILON);
		x = 100; y = 100;
		Assert.assertEquals(MathUtils.getMid(x, y), 100.0,EPSILON);
	}
	
	@Test
	public void testIsZero() {
		double x = 0.01;
		Assert.assertFalse(MathUtils.isZero(x));
		x = 0.000000000000000001;
		Assert.assertTrue(MathUtils.isZero(x));
		x = 0;
		Assert.assertTrue(MathUtils.isZero(x));
	}
	

	@Test
	public void testEqualsPerformance() {
		ResultTimer timer = new ResultTimer(10);
		//Test equals
		
		int TESTS = 1000000;
		double a = 0.000001;
		double b = 0.000001;
		if (a == b) {
			
		}
		boolean x = false;
		boolean y = false;
		for (int i = 0; i < TESTS; i++) {

			timer.startSample();
			x = (a == b);
			timer.endSample();
		}
		System.out.println("double: " + timer.toString(TimeUnit.MICROSECONDS));
		timer.clear();
		
		for (int i = 0; i < TESTS; i++) {
			timer.startSample();
			y = MathUtils.areEquals(a, b);
			timer.endSample();
		}
		System.out.println("PrecisionO: " + timer.toString(TimeUnit.MICROSECONDS));
		System.out.println(x + " " + y);
		
		//interesting conclusion - existing method is fast, as fast as conventional
		//==....no point in modifying to use shortcircuit...
		//however...avg and stddev are much better in 
	}

    @Test
    public void testReal() {
        long mantissa = 10234;
        Assert.assertEquals(MathUtils.toDecimal(mantissa,-3),10.234,EPSILON);
        Assert.assertEquals(MathUtils.toDecimal(mantissa,-4),1.0234,EPSILON);
        Assert.assertEquals(MathUtils.toDecimal(mantissa,-5),.10234,EPSILON);
        Assert.assertEquals(MathUtils.toDecimal(mantissa,-2),102.34,EPSILON);
        Assert.assertEquals(MathUtils.toDecimal(mantissa,-1),1023.4,EPSILON);
        Assert.assertEquals(MathUtils.toDecimal(mantissa,0),10234,EPSILON);
        Assert.assertEquals(MathUtils.toDecimal(mantissa,1),102340,EPSILON);

        Real r = MathUtils.fromDecimal(12.240,-3);
        Assert.assertEquals(r.getMantissa(),12240);
        Assert.assertEquals(r.getExponent(),-3);

        double v = 1.0;
        long exponent = -8;
        r = MathUtils.fromDecimal(v,exponent);
        Assert.assertEquals(MathUtils.toDecimal(r),v,EPSILON);
    }

    @Test
       public void testRealToLong() {
           long mantissa = 10234;
           Assert.assertEquals(MathUtils.toLong(mantissa,-3),10);
           Assert.assertEquals(MathUtils.toLong(mantissa,-4),1);
            Assert.assertEquals(MathUtils.toLong(mantissa,0),10234);


       }



    @Test
    public void testAdd() {
         Real a = MathUtils.fromDecimal(12.0,-8);
        Real b = MathUtils.fromDecimal(12.0,-8);
        MathUtils.add(a,b);
        Assert.assertEquals(MathUtils.toDecimal(a),24.0,EPSILON);

        a = MathUtils.fromDecimal(12.0,-8);
        b = MathUtils.fromDecimal(12.0,-9);
        MathUtils.add(a,b);
        Assert.assertEquals(MathUtils.toDecimal(a),24.0,EPSILON);
    }

    @Test
    public void testSubtract() {
        Real a = MathUtils.fromDecimal(12.0,-8);
        Real b = MathUtils.fromDecimal(8.0,-8);
        MathUtils.subtract(a,b);
        Assert.assertEquals(MathUtils.toDecimal(a),4.0,EPSILON);

        a = MathUtils.fromDecimal(12.0,-8);
        b = MathUtils.fromDecimal(8.0,-9);
        MathUtils.subtract(a,b);
        Assert.assertEquals(MathUtils.toDecimal(a),4.0,EPSILON);
    }

    @Test
    public void testMultiply() {
        Real a = MathUtils.fromDecimal(100,-3);
        Real b = MathUtils.fromDecimal(10,-3);
        MathUtils.multiply(a,b);
        Assert.assertEquals(MathUtils.toDecimal(a),1000,EPSILON);

        a = MathUtils.fromDecimal(2.5,-3);
        b = MathUtils.fromDecimal(4,-3);
        MathUtils.multiply(a,b);
        Assert.assertEquals(MathUtils.toDecimal(a),10,EPSILON);

        a = MathUtils.fromDecimal(2.5,-8);
        b = MathUtils.fromDecimal(4,-3);
        Real c = new Real();
        c.setExponent(-3);
        MathUtils.multiply(a,b,c);
        Assert.assertEquals(MathUtils.toDecimal(c),10,EPSILON);
    }

    @Test
    public void testAddAndMultiply() {
        Real a = MathUtils.fromDecimal(100,-3);
        Real b = MathUtils.fromDecimal(10,-3);
        Real c = new Real();
        c.setExponent(-3);
        MathUtils.multiply(a,b,c);
        Assert.assertEquals(MathUtils.toDecimal(c),1000,EPSILON);
    }

    @Test
       public void testDivide() {
           Real a = MathUtils.fromDecimal(100,-3);
           Real b = MathUtils.fromDecimal(10,-3);
           MathUtils.divide(a,b);
           Assert.assertEquals(MathUtils.toDecimal(a),10,EPSILON);

           a = MathUtils.fromDecimal(10,-3);
           b = MathUtils.fromDecimal(4,-3);
           MathUtils.divide(a,b);
           Assert.assertEquals(MathUtils.toDecimal(a),2.5,EPSILON);

           a = MathUtils.fromDecimal(1,-3);
           b = MathUtils.fromDecimal(4,-3);
           MathUtils.divide(a,b);
           Assert.assertEquals(MathUtils.toDecimal(a),0.25,EPSILON);

            a = MathUtils.fromDecimal(10,-8);
            b = MathUtils.fromDecimal(4,-3);
            Real c = new Real();
            c.setExponent(-8);
            MathUtils.divide(a,b,c);
            Assert.assertEquals(MathUtils.toDecimal(c),2.5,EPSILON);

       }



    @Test
    public void testRealVBigDecimalPerformance() {
        int tests = 1000;
        long s = System.nanoTime();
        BigDecimal bd = null;
        for (int i = 0;i<tests;i++) {
             bd = new BigDecimal(12.0);

        }
        long e = System.nanoTime();
        System.out.println(bd);
        long bdc = e-s;
        Real r = null;
        s = System.nanoTime();
        for (int i = 0;i<tests;i++) {
            //r = MathUtils.fromDecimal(12.0,-8);
            r = new Real(-8,1200000000);

        }
        e = System.nanoTime();
        System.out.println(r);
        long rc = e-s;
        s = System.nanoTime();
        for (int i = 0;i<tests;i++) {
            bd = new BigDecimal(12.0);

        }
        e = System.nanoTime();
        System.out.println(bd);
        bdc = e-s;
        s = System.nanoTime();
        for (int i = 0;i<tests;i++) {
            //r = MathUtils.fromDecimal(12.0,-8);
            r = new Real(-8,1200000000);
        }
        e = System.nanoTime();
        System.out.println(MathUtils.toDecimal(r));
        rc = e-s;
        System.out.println("BD-Creation: " + bdc);
        System.out.println("R-Creation: " + rc);

        bd = new BigDecimal(12.0);
        s = System.nanoTime();
        for (int i = 0;i<tests;i++) {
            bd = bd.add(new BigDecimal(1.0));
        }
        e = System.nanoTime();
        System.out.println(bd);
        bdc = e-s;
        r = new Real(0,12);
        s = System.nanoTime();

        for (int i = 0;i<tests;i++) {
            MathUtils.add(r,new Real(0,1l));      //FIXME
        }
        e = System.nanoTime();
        System.out.println(MathUtils.toDecimal(r));
        rc = e-s;
        System.out.println("BD-Addition: " + bdc);
        System.out.println("R-Addition: " + rc);

    }


    @Test
    public void testToDecimal() {
        double val = 24.45;
        long valBits = Double.doubleToLongBits(val);
        int sign = ((valBits >> 63)==0 ? 1 : -1);
        int exponent = (int) ((valBits >> 52) & 0x7ffL);
        long significand = (exponent==0 ? (valBits & ((1L<<52) - 1)) << 1
                    : (valBits & ((1L<<52) - 1)) | (1L<<52));
        exponent -= 1075;
        while((significand & 1) == 0) {    //  i.e., significand is even
                 significand >>= 1;
                 exponent++;
             }
 	// Calculate intVal and scale
 	long intVal = sign * significand;
        long scale = 0;
 	if (exponent < 0) {
 	    intVal = //intVal.multiply(BigInteger.valueOf(5).pow(-exponent));
                 intVal * MathUtils.power(5,-exponent);
 	    scale = -exponent;
 	} else if (exponent > 0) {
 	    intVal = intVal* MathUtils.power(2,exponent);
 	}
    //    intCompact = compactValFor(intVal);


        System.out.println("Val: " + val);
        System.out.println("ValBits: " + valBits);
        System.out.println("Sign: " + sign);
        System.out.println("Exp: " + exponent);
        System.out.println("SigAnd: " + significand);
        System.out.println("IntVal: " + intVal);
        System.out.println("Scale: " + scale);

    }

    @Test
    public void testFromDecimal() {
        //10.2 gives rounding error
        double y = 10.26;
        Real r = MathUtils.fromDecimalPrice(y);
        double x = MathUtils.toDecimal(r);
        Assert.assertEquals(x,y,MathUtils.EPSILON);
    }

    @Test
    public void testMin() {
        Quantity r = MathUtils.fromDecimalQuantity(12d);
        Quantity s = MathUtils.fromDecimalQuantity(13d);
        Quantity x = MathUtils.minQty(r,s);
        Assert.assertEquals(x,r);
        s = MathUtils.fromDecimalQuantity(-13d);
        x = MathUtils.minQty(r,s);
        Assert.assertEquals(x,s);
    }

    @Test
    public void testLessThan() {
        Quantity r = MathUtils.fromDecimalQuantity(12d);
        Quantity s = MathUtils.fromDecimalQuantity(13d);
        Assert.assertTrue(MathUtils.isLessThan(r,s));
        Assert.assertFalse(MathUtils.isLessThan(s,r));
    }

    @Test
    public void testLessThanOrEqual() {
        Quantity r = MathUtils.fromDecimalQuantity(12d);
        Quantity s = MathUtils.fromDecimalQuantity(13d);
        Assert.assertTrue(MathUtils.isLessThanOrEqual(r,s));
        Assert.assertFalse(MathUtils.isLessThanOrEqual(s,r));

        s = MathUtils.fromDecimalQuantity(12d);
        Assert.assertTrue(MathUtils.isLessThanOrEqual(r,s));
    }

    @Test
    public void testGreaterThanOrEqual() {
        Quantity r = MathUtils.fromDecimalQuantity(12d);
        Quantity s = MathUtils.fromDecimalQuantity(13d);
        Assert.assertTrue(MathUtils.isGreaterThanOrEqual(s,r));
        Assert.assertFalse(MathUtils.isGreaterThanOrEqual(r,s));

        s = MathUtils.fromDecimalQuantity(12d);
        Assert.assertTrue(MathUtils.isGreaterThanOrEqual(r,s));
    }

     @Test
    public void testGreaterThan() {
        Quantity r = MathUtils.fromDecimalQuantity(12d);
        Quantity s = MathUtils.fromDecimalQuantity(13d);
        Assert.assertTrue(MathUtils.isGreaterThan(s,r));
        Assert.assertFalse(MathUtils.isGreaterThan(r,s));
     }

    @Test
    public void testAbs() {
        Real x= MathUtils.fromDecimalPrice(12.3d);
        Real y= MathUtils.fromDecimalPrice(-12.3d);
        MathUtils.abs(x)    ;
        Assert.assertEquals(x,MathUtils.fromDecimalPrice(12.3d));
        MathUtils.abs(y)    ;
        Assert.assertEquals(y,MathUtils.fromDecimalPrice(12.3d));


    }

    @Test
    public void testRealMid() {
        Price x= MathUtils.fromDecimalPrice(12d);
        Price y= MathUtils.fromDecimalPrice(14d);
        Real z = MathUtils.getMid(x, y);
        Assert.assertEquals(z,MathUtils.fromDecimal(13d,x.getExponent()));
    }

    @Test
    public void testStringToReal() {
         String num = "23.335";
        Real real = MathUtils.stringToReal(num, -8);
        System.out.println(num + " = " + real);
        Assert.assertEquals(real,new Real(-8,2333500000l));
        num = "0.243";
        real = MathUtils.stringToReal(num, -8);
        System.out.println(num + " = " + real);
        Assert.assertEquals(real,new Real(-8,24300000l));
        num = "0.00243";
        real = MathUtils.stringToReal(num, -8);
        System.out.println(num + " = " + real);
        Assert.assertEquals(real,new Real(-8,243000l));
    }
}
