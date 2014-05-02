package com.oaktree.core.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.oaktree.core.types.MutableLong;
import com.oaktree.core.utils.MathUtils;

public class TestReverseBinarySearch {
	
	@Test
	public void testSearch() {
		List<MutableLong> numbers = new ArrayList<MutableLong>();
		numbers.add(new MutableLong(200));
		numbers.add(new MutableLong(190));
		numbers.add(new MutableLong(180));
		numbers.add(new MutableLong(170));
		numbers.add(new MutableLong(160));
		numbers.add(new MutableLong(150));
		numbers.add(new MutableLong(140));
		Assert.assertEquals(1,ReverseBinarySearch.search(numbers, 190),MathUtils.EPSILON);
		Assert.assertEquals(0,ReverseBinarySearch.search(numbers, 200),MathUtils.EPSILON);
		Assert.assertEquals(6,ReverseBinarySearch.search(numbers, 140),MathUtils.EPSILON);
		Assert.assertEquals(-8,ReverseBinarySearch.search(numbers, 130),MathUtils.EPSILON);
		Assert.assertEquals(-1,ReverseBinarySearch.search(numbers, 1000),MathUtils.EPSILON);
		Assert.assertEquals(-2,ReverseBinarySearch.search(numbers, 195),MathUtils.EPSILON);
		Assert.assertEquals(-5,ReverseBinarySearch.search(numbers, 165),MathUtils.EPSILON);		
	}
	
	@Test
	public void testNormalSearch() {
		List<MutableLong> numbers = new ArrayList<MutableLong>();
		numbers.add(new MutableLong(100));
		numbers.add(new MutableLong(101));
		numbers.add(new MutableLong(102));
		Assert.assertEquals(2,Collections.binarySearch(numbers, 102l),MathUtils.EPSILON);
	}


    @Test
	public void testArraySearch() {
		long[] numbers = new long[8];
		numbers[0] = 200;
        numbers[1] = 190;
        numbers[2] = 180;
        numbers[3] = 170;
        numbers[4] = 160;
        numbers[5] = 150;
        numbers[6] = 140;

		Assert.assertEquals(1,ReverseBinarySearch.search(7,numbers, 190),MathUtils.EPSILON);
		Assert.assertEquals(0,ReverseBinarySearch.search(7,numbers, 200),MathUtils.EPSILON);
		Assert.assertEquals(6,ReverseBinarySearch.search(7,numbers, 140),MathUtils.EPSILON);
		Assert.assertEquals(-8,ReverseBinarySearch.search(7,numbers, 130),MathUtils.EPSILON);
		Assert.assertEquals(-1,ReverseBinarySearch.search(7,numbers, 1000),MathUtils.EPSILON);
		Assert.assertEquals(-2,ReverseBinarySearch.search(7,numbers, 195),MathUtils.EPSILON);
		Assert.assertEquals(-5,ReverseBinarySearch.search(7,numbers, 165),MathUtils.EPSILON);
	}

    @Test
	public void testArrayNormalSearch() {

        long[] numbers = new long[8];
		numbers[0] = 100;
        numbers[1] = 101;
        numbers[2] = 102;

        Assert.assertEquals(2,Arrays.binarySearch(numbers,0,3, 102l),MathUtils.EPSILON);
        Assert.assertEquals(1,Arrays.binarySearch(numbers,0,3, 101l),MathUtils.EPSILON);

	}
}
