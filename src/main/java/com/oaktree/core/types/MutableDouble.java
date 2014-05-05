package com.oaktree.core.types;

import com.oaktree.core.utils.MathUtils;

/**
 * Wrapper object round an double primitive that can be reused. Not thread-safe.
 * 
 * @author IJLAPTOP
 * 
 */
public class MutableDouble implements Comparable<Double> {
	public MutableDouble(double value) {
		this.value = value;
	}

	public double value;

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int compareTo(Double o) {
		if (MathUtils.areEquals(o.doubleValue(),value)) {
			return 0;
		}
		if (o.doubleValue() > value) {
			return -1;
		} else {
			return 1;
		}
	}
	
	@Override
	public boolean equals(Object a) {
		double v = Double.NaN;
		if (a instanceof MutableDouble) {
			v = ((MutableDouble)a).value;
		}
		if (a instanceof Double) {
			v = ((Double)(a)).doubleValue();
		}
		if (MathUtils.areEquals(v,value)) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (int)(value);
	}
}
