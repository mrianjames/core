package com.oaktree.core.types;

/**
 * Wrapper object round an integer primitive that can be reused.
 * Not thread-safe.
 * @author IJLAPTOP
 *
 */
public class MutableInteger implements Comparable<Integer>{
	
	public int value;

	public MutableInteger(int value) {
		this.value = value;
	}

	
	public String toString() {
		return String.valueOf(value);
	}
	
	@Override
	public int compareTo(Integer o) {
		if (o == value) {
			return 0;
		}
		if (o > value) {
			return -1;
		} else {
			return 1;
		}
	}
	
	@Override
	public boolean equals(Object a) {
		if (a instanceof MutableLong) {
			return ((MutableLong)a).value == value;
		}
		if (a instanceof Long) {
			return ((Long)a).longValue() == value;
		}
		if (a instanceof Integer) {
			return ((Integer)a).longValue() == value;
		}
		if (a instanceof Short) {
			return ((Short)a).longValue() == value;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return value;
	}
}
