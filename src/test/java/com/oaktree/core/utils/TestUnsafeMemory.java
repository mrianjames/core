package com.oaktree.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TestUnsafeMemory {

	@Test
	public void testUnsafeMemory() {
		
		//values.
		int value_int = 12;
		short value_short = 3;
		boolean value_boolean1 = true;
		boolean value_boolean2 = false;
		int[] value_int_array = new int[]{45,34};
		char value_char= 'x';
		String value_string = "HELLO";
		double value_double = 12.5;
		float value_float = 12.3f;
		long value_long = 1234l;
		long[] value_long_array = new long[]{1234l,234};
		double[] value_double_array = new double[]{1234.32,234.24};
		float[] value_float_array = new float[]{1234f,234.23f};
		char[] value_char_array = new char[]{'h','d'};
		short[] value_short_array = new short[]{1234,234};
		byte[] value_byte_array = new byte[]{(byte)0,(byte)(1)};
		int value_int2 = 45;
		String value_big_string = "12345678901234567890";
		
		byte[] bytes = new byte[1024];
		UnsafeMemory um = new UnsafeMemory(bytes);
		um.putInt(value_int);
		um.putShort(value_short);
		um.putBoolean(value_boolean1);
		um.putBoolean(value_boolean2);
		um.putIntArray(value_int_array);
		um.putChar(value_char);
		um.putString(value_string);
		um.putDouble(value_double);
		um.putFloat(value_float);
		um.putLong(value_long);
		um.putLongArray(value_long_array);
		um.putDoubleArray(value_double_array);
		um.putFloatArray(value_float_array);
		um.putCharArray(value_char_array);
		um.putShortArray(value_short_array);
		um.putByteArray(value_byte_array);
		um.putInt(value_int2);
		um.putString(value_big_string, 10);
		
		UnsafeMemory pm = new UnsafeMemory(bytes);
		System.out.println("INT: "+pm.getInt());
		System.out.println("SHT: "+pm.getShort());
		System.out.println("BOO1: "+pm.getBoolean());
		System.out.println("BOO2: "+pm.getBoolean());
		System.out.println("INTA: "+Arrays.toString(pm.getIntArray()));
		System.out.println("CHAR: "+pm.getChar());		
		System.out.println("STR: "+pm.getString());
		System.out.println("DBL: "+pm.getDouble());
		System.out.println("FLT: "+pm.getFloat());
		System.out.println("LNG: "+pm.getLong());
		System.out.println("LNGA: "+Arrays.toString(pm.getLongArray()));
		System.out.println("DBLA: "+Arrays.toString(pm.getDoubleArray()));
		System.out.println("FLTA: "+Arrays.toString(pm.getFloatArray()));
		System.out.println("CHA: "+Arrays.toString(pm.getCharArray()));
		System.out.println("SHA: "+Arrays.toString(pm.getShortArray()));
		System.out.println("BYA: "+Arrays.toString(pm.getByteArray()));
		System.out.println("INT: "+pm.getInt());
		System.out.println("STR: "+pm.getString());
		pm = new UnsafeMemory(bytes);
		
		Assert.assertEquals(value_int, pm.getInt());
		Assert.assertEquals(value_short,pm.getShort());
		Assert.assertEquals(value_boolean1,pm.getBoolean());
		Assert.assertEquals(value_boolean2,pm.getBoolean());
		Assert.assertArrayEquals(value_int_array,pm.getIntArray());
		Assert.assertEquals(value_char,pm.getChar());
		Assert.assertEquals(value_string,pm.getString());
		Assert.assertEquals(value_double,pm.getDouble(),0.00000001);
		Assert.assertEquals(value_float,pm.getFloat(),0.000000001f);
		Assert.assertEquals(value_long, pm.getLong());
		Assert.assertArrayEquals(value_long_array,pm.getLongArray());
		Assert.assertArrayEquals(value_double_array,pm.getDoubleArray(),0.000001);
		Assert.assertArrayEquals(value_float_array,pm.getFloatArray(),0.0000001f);
		Assert.assertArrayEquals(value_char_array,pm.getCharArray());
		Assert.assertArrayEquals(value_short_array,pm.getShortArray());
		Assert.assertArrayEquals(value_byte_array,pm.getByteArray());
		Assert.assertEquals(value_int2,pm.getInt());
		Assert.assertEquals(value_big_string.substring(0,10),pm.getString());
		
	}

}
