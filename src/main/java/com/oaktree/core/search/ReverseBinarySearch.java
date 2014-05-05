package com.oaktree.core.search;

import java.util.ArrayList;
import java.util.List;

import com.oaktree.core.types.MutableDouble;
import com.oaktree.core.types.MutableLong;
import com.oaktree.core.types.MutableLong;
import com.oaktree.core.utils.MathUtils;

public class ReverseBinarySearch {

	public static void main(String[] args) {
		List<MutableLong> numbers = new ArrayList<MutableLong>();
		numbers.add(new MutableLong(20));
		numbers.add(new MutableLong(19));
		numbers.add(new MutableLong(18));
		numbers.add(new MutableLong(17));
		numbers.add(new MutableLong(16));
		numbers.add(new MutableLong(15));
		numbers.add(new MutableLong(14));
		ReverseBinarySearch.search(numbers, 19);
	}

    public static int search(int endptr,long[] numbers, long value) {
        if (numbers.length == 0 || endptr == 0) {
			return -1; //must be negative less than 0 as 0 is valid index
		}
		if (value < numbers[endptr-1]) {
			return -endptr-1;
		}
		if (value > numbers[0]) {
			return -1;
		}
		//special cases - less then and grtr than this range
		int left = 0;
		int right = endptr;
		int middle = right/2;
		while (true) {
			long v = numbers[middle];
			if (v == value) {
				return middle;
			} else if (value > v) {
				//go left -> lrg
				right = middle;
				middle = left + ((right-left)/2);
				if (middle == right) {
					return -middle-1;
				}
			} else {
				left = middle;
				middle = left + ((right-left)/2);
				if (middle == left) {
					return -middle-2;
				}
			}
		}
    }
	
	/**
	 * Search a sorted set of doubles that are in biggest value to lowest
	 * 
	 * @param numbers
	 * @param value
	 * @return 0 or +ve number representing a found index. a negative number
	 * represents the -(insertionpoint-1). e.g. -2 means insert at array index 1. 0 means 
	 * a match was found at position 0. -1 means insert at array index 0.
	 */
	public static int search(List<MutableLong> numbers, long value) {
		if (numbers.isEmpty()) {
			return -1; //must be negative less than 0 as 0 is valid index
		}
		if (value < numbers.get(numbers.size()-1).value) {
			return -numbers.size()-1;
		}
		if (value > numbers.get(0).value) {
			return -1;
		}
		//special cases - less then and grtr than this range
		int left = 0;
		int right = numbers.size();
		int middle = right/2;
		while (true) {
			long v = numbers.get(middle).value;
			if (v == value) {
				return middle;
			} else if (value > v) {
				//go left -> lrg
				right = middle;
				middle = left + ((right-left)/2);
				if (middle == right) {
					return -middle-1;
				}
			} else {
				left = middle;
				middle = left + ((right-left)/2);
				if (middle == left) {
					return -middle-2;
				}
			}
		}
	}

    public static int search(List<MutableDouble> numbers, double value) {
		if (numbers.isEmpty()) {
			return -1; //must be negative less than 0 as 0 is valid index
		}
		if (value < numbers.get(numbers.size()-1).value) {
			return -numbers.size()-1;
		}
		if (value > numbers.get(0).value) {
			return -1;
		}
		//special cases - less then and grtr than this range
		int left = 0;
		int right = numbers.size();
		int middle = right/2;
		while (true) {
			double v = numbers.get(middle).value;
			if (v == value) {
				return middle;
			} else if (value > v) {
				//go left -> lrg
				right = middle;
				middle = left + ((right-left)/2);
				if (middle == right) {
					return -middle-1;
				}
			} else {
				left = middle;
				middle = left + ((right-left)/2);
				if (middle == left) {
					return -middle-2;
				}
			}
		}
	}
}
