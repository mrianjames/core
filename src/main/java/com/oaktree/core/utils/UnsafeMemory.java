package com.oaktree.core.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Native memory manipulation for fast serialization to bytes.
 * This will write/read native endianness - that is on x86 LITTLE endian format
 * which should be fine to read/write against other x86 systems.
 * on ARM or other it may be BIG. No way of changing for now. 
 * 
 * @author ianjames
 *
 */
public class UnsafeMemory {
	private static final Unsafe unsafe;
	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static final long byteArrayOffset = unsafe.arrayBaseOffset(byte[].class);
	private static final long longArrayOffset = unsafe.arrayBaseOffset(long[].class);
	private static final long doubleArrayOffset = unsafe.arrayBaseOffset(double[].class);
	private static final long floatArrayOffset = unsafe.arrayBaseOffset(float[].class);
	private static final long charArrayOffset = unsafe.arrayBaseOffset(char[].class);
	private static final long shortArrayOffset = unsafe.arrayBaseOffset(short[].class);
	private static final long intArrayOffset = unsafe.arrayBaseOffset(int[].class);
	private static final long booleanArrayOffset = unsafe.arrayBaseOffset(boolean[].class);

	private static final int SIZE_OF_BOOLEAN = 1;
	private static final int SIZE_OF_BYTE = 1;
	private static final int SIZE_OF_CHAR = 2;
	private static final int SIZE_OF_SHORT = 2;
	private static final int SIZE_OF_INT = 4;
	private static final int SIZE_OF_ENUM = 4;
	private static final int SIZE_OF_FLOAT = 4;
	private static final int SIZE_OF_LONG = 8;
	private static final int SIZE_OF_DOUBLE = 8;
	public static final int NULL_ARRAY = -1;

	private int pos = 0;
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}
	private final byte[] buffer;

	public UnsafeMemory(final byte[] buffer) {
		if (null == buffer) {
			throw new NullPointerException("buffer cannot be null");
		}

		this.buffer = buffer;
	}

	public void reset() {
		this.pos = 0;
	}

	public void putString(final String value, int size) {
		char[] chars = value.toCharArray();
		putCharArray(chars,size);
	}
	
	public void putCharArray(final char[] values, int size) {
		if (values == null) {
			putInt(NULL_ARRAY);
			return;
		}
		putInt(size);

		long bytesToCopy = size << 1;
		unsafe.copyMemory(values, charArrayOffset, buffer, byteArrayOffset
				+ pos, bytesToCopy);
		pos += bytesToCopy;
	}
    public void writeObject(Object object,  boolean writeType,byte type) {

        if (writeType) {
            putByte(type);
        }
        switch (type) {
            case ByteUtils.Types.STRING:
                putString((String)(object));
                break;
            case ByteUtils.Types.LONG:
                putLong((Long)(object));
                break;
            case ByteUtils.Types.SHORT:
                putShort((Short)(object));
                break;
            case ByteUtils.Types.CHAR:
                putChar((Character)(object));
                break;
            case ByteUtils.Types.DOUBLE:
                putDouble((Double)(object));
                break;
            case ByteUtils.Types.FLOAT:
                putFloat((Float)(object));
                break;
            case ByteUtils.Types.INT:
                putInt((Integer)(object));
                break;
            case ByteUtils.Types.BYTE:
                putByte((Byte)(object));
                break;
            case ByteUtils.Types.BOOLEAN:
                putBoolean((Boolean)(object));
                break;
            default:
                throw new IllegalArgumentException("Invalid parameter type: "+ object.getClass());
        }

    }

	public void putBoolean(final boolean value) {
		unsafe.putBoolean(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_BOOLEAN;
	}
	
	public void putChar(final char value) {
		unsafe.putChar(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_CHAR;
	}
	
	public void putShort(final short value) {
		unsafe.putShort(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_SHORT;
	}
	
	public void putFloat(final float value) {
		unsafe.putFloat(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_FLOAT;
	}
	
	public void putDouble(final double value) {
		unsafe.putDouble(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_DOUBLE;
	}
	
	public void putByte(final byte value) {
		unsafe.putByte(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_BYTE;
	}

	public boolean getBoolean() {
		boolean value = unsafe.getBoolean(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_BOOLEAN;

		return value;
	}

	public void putInt(final int value) {
		unsafe.putInt(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_INT;
	}

	public int getInt() {
		int value = unsafe.getInt(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_INT;

		return value;
	}
	
	public byte getByte() {
		byte value = unsafe.getByte(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_BYTE;

		return value;
	}
	
	
	public short getShort() {
		short value = unsafe.getShort(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_SHORT;

		return value;
	}
	
	public char getChar() {
		char value = unsafe.getChar(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_CHAR;

		return value;
	}
	
	public double getDouble() {
		double value = unsafe.getDouble(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_DOUBLE;

		return value;
	}
	
	public float getFloat() {
		float value = unsafe.getFloat(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_FLOAT;

		return value;
	}

	public void putLong(final long value) {
		unsafe.putLong(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_LONG;
	}

	public long getLong() {
		long value = unsafe.getLong(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_LONG;

		return value;
	}

	public void putCharArray(final char[] values) {
		if (values == null) {
			putInt(NULL_ARRAY);
			return;
		}
		putInt(values.length);

		long bytesToCopy = values.length << 1;
		unsafe.copyMemory(values, charArrayOffset, buffer, byteArrayOffset
				+ pos, bytesToCopy);
		pos += bytesToCopy;
	}

	public void putByteArray(final byte[] values) {
		if (values == null) {
			putInt(NULL_ARRAY);
			return;
		}
		putInt(values.length);

		long bytesToCopy = values.length << 0;
		unsafe.copyMemory(values, byteArrayOffset, buffer, byteArrayOffset
				+ pos, bytesToCopy);
		pos += bytesToCopy;
	}
	
	public void putBooleanArray(final boolean[] values) {
		if (values == null) {
			putInt(NULL_ARRAY);
			return;
		}
		putInt(values.length);

		long bytesToCopy = values.length << 0;
		unsafe.copyMemory(values, booleanArrayOffset, buffer, byteArrayOffset
				+ pos, bytesToCopy);
		pos += bytesToCopy;
	}

	public static void main(String[] args) {
		int size = 3;
		long x = new long[]{123l,123l,123l,456l}.length;
		System.out.println(x + " = " + (x << 0));
	}
	
	//char,short = 1
	//int, float 2
	//long,double, 3
	
	public void putLongArray(final long[] values) {
		if (values == null) {
			putInt(NULL_ARRAY);
			return;
		}
		putInt(values.length);

		long bytesToCopy = values.length << 3;
		unsafe.copyMemory(values, longArrayOffset, buffer, byteArrayOffset
				+ pos, bytesToCopy);
		pos += bytesToCopy;
	}

	public void putShortArray(final short[] values) {
		if (values == null) {
			putInt(NULL_ARRAY);
			return;
		}
		putInt(values.length);

		long bytesToCopy = values.length << 1;
		unsafe.copyMemory(values, shortArrayOffset, buffer, byteArrayOffset
				+ pos, bytesToCopy);
		pos += bytesToCopy;
	}
	
	public void putIntArray(final int[] values) {
		if (values == null) {
			putInt(NULL_ARRAY);
			return;
		}
		putInt(values.length);

		long bytesToCopy = values.length << 2;
		unsafe.copyMemory(values, intArrayOffset, buffer, byteArrayOffset
				+ pos, bytesToCopy);
		pos += bytesToCopy;
	}
	public long[] getLongArray() {
		int arraySize = getInt();
		long[] values = new long[arraySize];

		long bytesToCopy = getBytesToCopy(values);
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values,
				longArrayOffset, bytesToCopy);
		pos += bytesToCopy;

		return values;
	}
	
	private long getBytesToCopy(long[] values) {
		return values.length << 3;
	}
	public String getString() {
		int arraySize = getInt();
		if (arraySize == NULL_ARRAY) {
			return null;
		}
		char[] values = new char[arraySize];

		long bytesToCopy = values.length << 1;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values,
				charArrayOffset, bytesToCopy);
		pos += bytesToCopy;

		return new String(values);
	}

	public void putDoubleArray(final double[] values) {
		if (values == null) {
			putInt(NULL_ARRAY);
			return;
		}
		putInt(values.length);

		long bytesToCopy = values.length << 3;
		unsafe.copyMemory(values, doubleArrayOffset, buffer, byteArrayOffset
				+ pos, bytesToCopy);
		pos += bytesToCopy;
	}
	
	public void putString(final String value) {
		char[] chars = value.toCharArray();
		putCharArray(chars);
	}


	public void putFloatArray(final float[] values) {
		if (values == null) {
			putInt(NULL_ARRAY);
			return;
		}
		putInt(values.length);

		long bytesToCopy = values.length << 2;
		unsafe.copyMemory(values, floatArrayOffset, buffer, byteArrayOffset
				+ pos, bytesToCopy);
		pos += bytesToCopy;
	}

	
	public double[] getDoubleArray() {
		int arraySize = getInt();
		if (arraySize == NULL_ARRAY) {
			return null;
		}
		double[] values = new double[arraySize];

		long bytesToCopy = values.length << 3;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values,
				doubleArrayOffset, bytesToCopy);
		pos += bytesToCopy;

		return values;
	}
	
	public boolean[] getBooleanArray() {
		int arraySize = getInt();
		if (arraySize == NULL_ARRAY) {
			return null;
		}
		boolean[] values = new boolean[arraySize];

		long bytesToCopy = values.length << 0;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values,
				booleanArrayOffset, bytesToCopy);
		pos += bytesToCopy;

		return values;
	}
	
	
	public float[] getFloatArray() {
		int arraySize = getInt();
		if (arraySize == NULL_ARRAY) {
			return null;
		}
		float[] values = new float[arraySize];

		long bytesToCopy = values.length << 2;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values,
				floatArrayOffset, bytesToCopy);
		pos += bytesToCopy;

		return values;
	}
	
	public int[] getIntArray() {
		int arraySize = getInt();
		if (arraySize == NULL_ARRAY) {
			return null;
		}
		int[] values = new int[arraySize];

		long bytesToCopy = values.length << 2;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values,
				intArrayOffset, bytesToCopy);
		pos += bytesToCopy;

		return values;
	}
	
	public short[] getShortArray() {
		int arraySize = getInt();
		if (arraySize == NULL_ARRAY) {
			return null;
		}
		short[] values = new short[arraySize];

		long bytesToCopy = values.length << 1;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values,
				shortArrayOffset, bytesToCopy);
		pos += bytesToCopy;

		return values;
	}
	
	public char[] getCharArray() {
		int arraySize = getInt();
		if (arraySize == NULL_ARRAY) {
			return null;
		}
		char[] values = new char[arraySize];

		long bytesToCopy = values.length << 1;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values,
				charArrayOffset, bytesToCopy);
		pos += bytesToCopy;

		return values;
	}
	
	public byte[] getByteArray() {
		int arraySize = getInt();
		if (arraySize == NULL_ARRAY) {
			return null;
		}
		byte[] values = new byte[arraySize];

		long bytesToCopy = values.length << 0;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values,
				byteArrayOffset, bytesToCopy);
		pos += bytesToCopy;

		return values;
	}

	public byte[] getBytes() {
		return buffer;
	}
}