package com.oaktree.core.utils;


import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Common utilities for operations on bytes.
 * 
 * @author ij
 *
 */
public class ByteUtils {
	public final static int NO_ARRAY = -1;
	public static void putCharArray(char[] array, ByteBuffer buffer) {
		if (array == null) {
			buffer.putInt(NO_ARRAY);
			return;
		}
		buffer.putInt(array.length);
		for (char c:array) {
			buffer.putChar(c); //efficient?
		}
	}
	
	public static char[] getCharArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		if (size == 0) {
			return null;
		}
		char[] array = new char[size];
		for (int i = 0; i < size;i++) {
			array[i] = buffer.getChar();
		}
		return array;
	}
	
	public static void putByteArray(byte[] array, ByteBuffer buffer) {
		if (array == null) {
			buffer.putInt(NO_ARRAY);
			return;
		}
		buffer.putInt(array.length);
		for (byte c:array) {
			buffer.put(c); //efficient?
		}
	}
	
	public static byte[] getByteArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		if (size == 0) {
			return null;
		}
		byte[] array = new byte[size];
		for (int i = 0; i < size;i++) {
			array[i] = buffer.get();
		}
		return array;
	}
	
	
	public static void putShortArray(short[] array, ByteBuffer buffer) {
		if (array == null) {
			buffer.putInt(NO_ARRAY);
			return;
		}
		buffer.putInt(array.length);
		for (short c:array) {
			buffer.putShort(c); //efficient?
		}
	}
	
	public static short[] getShortArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		if (size == 0) {
			return null;
		}
		short[] array = new short[size];
		for (int i = 0; i < size;i++) {
			array[i] = buffer.getShort();
		}
		return array;
	}
	
	public static void putIntArray(int[] array, ByteBuffer buffer) {
		if (array == null) {
			buffer.putInt(NO_ARRAY);
			return;
		}
		buffer.putInt(array.length);
		for (int c:array) {
			buffer.putInt(c); //efficient?
		}
	}
	
	public static int[] getIntArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		if (size == 0) {
			return null;
		}
		int[] array = new int[size];
		for (int i = 0; i < size;i++) {
			array[i] = buffer.getInt();
		}
		return array;
	}
	
	public static void putLongArray(long[] array, ByteBuffer buffer) {
		if (array == null) {
			buffer.putInt(NO_ARRAY);
			return;
		}
		buffer.putInt(array.length);
		for (long c:array) {
			buffer.putLong(c); //efficient?
		}
	}
	
	public static long[] getLongArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		if (size == 0) {
			return null;
		}
		long[] array = new long[size];
		for (int i = 0; i < size;i++) {
			array[i] = buffer.getLong();
		}
		return array;
	}
	
	public static void putFloatArray(float[] array, ByteBuffer buffer) {
		if (array == null) {
			buffer.putInt(NO_ARRAY);
			return;
		}
		buffer.putInt(array.length);
		for (float c:array) {
			buffer.putFloat(c); //efficient?
		}
	}
	
	public static float[] getFloatArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		if (size == 0) {
			return null;
		}
		float[] array = new float[size];
		for (int i = 0; i < size;i++) {
			array[i] = buffer.getFloat();
		}
		return array;
	}
	
	public static void putDoubleArray(double[] array, ByteBuffer buffer) {
		if (array == null) {
			buffer.putInt(NO_ARRAY);
			return;
		}
		buffer.putInt(array.length);
		for (double c:array) {
			buffer.putDouble(c); //efficient?
		}
	}
	
	public static double[] getDoubleArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		if (size == 0) {
			return null;
		}
		double[] array = new double[size];
		for (int i = 0; i < size;i++) {
			array[i] = buffer.getDouble();
		}
		return array;
	}
	
	public static void putStringArray(String[] array, ByteBuffer buffer) {
		if (array == null) {
			buffer.putInt(NO_ARRAY);
			return;
		}
		buffer.putInt(array.length);
		for (String c:array) {
			ByteUtils.putString(c, buffer);
		}
	}
	
	public static String[] getStringArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		if (size == 0) {
			return null;
		}
		String[] array = new String[size];
		for (int i = 0; i < size;i++) {
			array[i] = ByteUtils.getString(buffer);
		}
		return array;
	}
	
	public static void putBooleanArray(boolean[] array, ByteBuffer buffer) {
		if (array == null) {
			buffer.putInt(NO_ARRAY);
			return;
		}
		buffer.putInt(array.length);
		for (boolean c:array) {
			ByteUtils.putBoolean(c, buffer);
		}
	}
	
	public static boolean[] getBooleanArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		if (size == 0) {
			return null;
		}
		boolean[] array = new boolean[size];
		for (int i = 0; i < size;i++) {
			array[i] = ByteUtils.getBoolean(buffer);
		}
		return array;
	}
	
    /**
     * Get the size that a class schema is given a length of strings.
     * @param schema
     * @return schema size.
     */
    public static int calcSchemaSize(Class[] schema, int stringSize){
        int size = 0;
        for (Class o:schema) {
            byte x = ByteUtils.getObjectTypeByClass(o); //TODO no class for set length strings.
            switch (x) {
                case ByteUtils.Types.LONG:
                    size+=8;
                    break;
                case ByteUtils.Types.FLOAT:
                    size+=4;
                    break;
                case ByteUtils.Types.DOUBLE:
                    size+=8;
                    break;
                case ByteUtils.Types.BYTE:
                    size+=1;
                    break;
                case ByteUtils.Types.CHAR:
                    size+=2;
                    break;
                case ByteUtils.Types.SHORT:
                    size+=2;
                    break;
                case ByteUtils.Types.INT:
                    size+=4;
                    break;
                case ByteUtils.Types.BOOLEAN:
                    size+=1;
                    break;
                case ByteUtils.Types.STRING:
                    size+=(stringSize*2); //this is a total guess, probably wrong and cause havoc.
                    break;
                case ByteUtils.Types.STRING_20:
                    size+=(20) + 2; //20 chars + 2 for a short for length.
                    break;
                case ByteUtils.Types.STRING_40:
                    size+=(40)+2;
                    break;
                case ByteUtils.Types.STRING_80:
                    size+=(80)+2;
                    break;
                case ByteUtils.Types.STRING_128:
                    size+=(128*2)+2;
                    break;
                case ByteUtils.Types.STRING_256:
                    size+=(256)+2;
                    break;
                case ByteUtils.Types.STRING_512:
                    size+=(512)+2;
                    break;
                default:
                	throw new IllegalStateException("Invalid schema field: " + x);

            }                             }
        return size;
    }

    /**
     * Get the size that a class schema is given a length of strings.
     * @param schema
     * @return schema size
     */
    public static int calcSchemaSize(byte[] schema, int stringSize){
    	if (schema == null) {
    		return 256;
    	}
        int size = 0;
        for (byte x:schema) {
            switch (x) {
                case ByteUtils.Types.LONG:
                    size+=8;
                    break;
                case ByteUtils.Types.FLOAT:
                    size+=4;
                    break;
                case ByteUtils.Types.DOUBLE:
                    size+=8;
                    break;
                case ByteUtils.Types.BYTE:
                    size+=1;
                    break;
                case ByteUtils.Types.CHAR:
                    size+=2;
                    break;
                case ByteUtils.Types.SHORT:
                    size+=2;
                    break;
                case ByteUtils.Types.INT:
                    size+=4;
                    break;
                case ByteUtils.Types.BOOLEAN:
                    size+=1;
                    break;
                case ByteUtils.Types.STRING:
                    size+=(stringSize*2)+2;
                    break;
                case ByteUtils.Types.STRING_20:
                	size+=(20) + 2; //20 chars + 2 for a short for length.
                    break;
                case ByteUtils.Types.STRING_40:
                    size+=(40)+2;
                    break;
                case ByteUtils.Types.STRING_80:
                    size+=(80)+2;
                    break;
                case ByteUtils.Types.STRING_128:
                    size+=(128)+2;
                    break;
                case ByteUtils.Types.STRING_256:
                    size+=(256)+2;
                    break;
                case ByteUtils.Types.STRING_512:
                    size+=(512)+2;
                    break;
                default:
                	throw new IllegalStateException("Invalid schema field: " + x);
            }
        }
        return size;
    }


    /**
     * Get the size that a class schema is given a length of strings.
     * @param schema
     * @return schema size
     */
    public static int calcSchemaSizeForObjects(Object[] schema){
        int size = 0;
        for (Object o:schema) {
            byte x = ByteUtils.getObjectType(o);
            switch (x) {
                case ByteUtils.Types.LONG:
                    size+=8;
                    break;
                case ByteUtils.Types.FLOAT:
                    size+=4;
                    break;
                case ByteUtils.Types.DOUBLE:
                    size+=8;
                    break;
                case ByteUtils.Types.BYTE:
                    size+=1;
                    break;
                case ByteUtils.Types.CHAR:
                    size+=2;
                    break;
                case ByteUtils.Types.SHORT:
                    size+=2;
                    break;
                case ByteUtils.Types.INT:
                    size+=4;
                    break;
                case ByteUtils.Types.BOOLEAN:
                    size+=1;
                    break;
                case ByteUtils.Types.STRING:
                    size+=(((String)(o)).length()*2);
                    break;
                default:
                	throw new IllegalStateException("Invalid schema field: " + x);

            }
        }
        return size;
    }

    /**
     * String rep for a byte.
     * @param c
     * @return
     */
    public static String toDescription(byte c) {
         return Types.toClass(c).getName();
    }

    /**
     * Get object from a byte buffer of a type.
     * @param buffer
     * @param b
     * @return retrieved object
     */
    public static Object getObject(ByteBuffer buffer, byte b) {
        switch (b) {
            case ByteUtils.Types.LONG:
                return buffer.getLong();
            case ByteUtils.Types.FLOAT:
                return buffer.getFloat();
            case ByteUtils.Types.DOUBLE:
                return buffer.getDouble();
            case ByteUtils.Types.BYTE:
                return buffer.get();
            case ByteUtils.Types.CHAR:
                return buffer.getChar();
            case ByteUtils.Types.SHORT:
                return buffer.getShort();
            case ByteUtils.Types.INT:
                return buffer.getInt();
            case ByteUtils.Types.BOOLEAN:
                return getBoolean(buffer);
            case ByteUtils.Types.STRING:
                return getString(buffer);
            case ByteUtils.Types.STRING_20:
                return getString(buffer,20);
            case ByteUtils.Types.STRING_40:
                return getString(buffer,40);
            case ByteUtils.Types.STRING_80:
                return getString(buffer,80);
            case ByteUtils.Types.STRING_128:
                return getString(buffer,128);
            case ByteUtils.Types.STRING_256:
                return getString(buffer,256);
            case ByteUtils.Types.STRING_512:
                return getString(buffer,512);
        }
        return null;
    }
    
    /**
     * Get object from a byte buffer of a type.
     * @param buffer
     * @param b
     * @return retrieved object
     */
    public static void writeObjectToStringBuilder(ByteBuffer buffer, byte b, StringBuilder builder) {
        switch (b) {
            case ByteUtils.Types.LONG:
                builder.append(buffer.getLong());
                return;
            case ByteUtils.Types.FLOAT:
            	 builder.append(buffer.getFloat());
            	 return;
            case ByteUtils.Types.DOUBLE:
            	 builder.append(buffer.getDouble());
            	 return;
            case ByteUtils.Types.BYTE:
            	 builder.append(buffer.get());
            	 return;
            case ByteUtils.Types.CHAR:
            	 builder.append(buffer.getChar());
            	 return;
            case ByteUtils.Types.SHORT:
            	 builder.append(buffer.getShort());
            	 return;
            case ByteUtils.Types.INT:
            	 builder.append(buffer.getInt());
            	 return;
            case ByteUtils.Types.BOOLEAN:
            	 builder.append(getBoolean(buffer));
            	 return;
            case ByteUtils.Types.STRING:
            	 builder.append(getString(buffer));
            	 return;
            case ByteUtils.Types.STRING_20:
            	 builder.append(getString(buffer,20));
            	 return;
            case ByteUtils.Types.STRING_40:
            	 builder.append(getString(buffer,40));
            	 return;
            case ByteUtils.Types.STRING_80:
            	 builder.append( getString(buffer,80));
            	 return;
            case ByteUtils.Types.STRING_128:
            	 builder.append( getString(buffer,128));
            	 return;
            case ByteUtils.Types.STRING_256:
            	 builder.append( getString(buffer,256));
            	 return;
            case ByteUtils.Types.STRING_512:
            	 builder.append( getString(buffer,512));
            	 return;
        }
    }

    /**
	 * Byte identifiers for primiative and String types.
	 * @author ij
	 *
	 */
	public static class Types {
		public final static byte SHORT = 0;
		public final static byte INT = 1;
		public final static byte LONG = 2;
		public final static byte DOUBLE = 3;
		public final static byte FLOAT = 4;
		public final static byte BYTE = 5;
		public final static byte CHAR = 6;
		public final static byte STRING = 7;
		public final static byte BARRAY = 8;
        public final static byte BOOLEAN = 9;
        
        //string set lengths...
        public final static byte STRING_20 = 10;
        public final static byte STRING_40 = 11;
        public final static byte STRING_80 = 12;
        public final static byte STRING_128 = 13;
        public final static byte STRING_256 = 14;
        public final static byte STRING_512 = 15;

        /**
         * Convert a byte to a class.
         * @param b
         * @return
         */
        public static Class toClass(byte b) {
            switch (b){
                case SHORT:
                    return Short.class;
                case LONG:
                    return Long.class;
                case INT:
                    return Integer.class;
                case DOUBLE:
                    return Double.class;
                case FLOAT:
                    return Float.class;
                case BYTE:
                    return Byte.class;
                case CHAR:
                    return Character.class;
                case STRING:
                case STRING_20:
                case STRING_40:
                case STRING_80:
                case STRING_128:
                case STRING_256:
                case STRING_512:
                    return String.class;
                case BARRAY:
                    return byte[].class;
                case BOOLEAN:
                    return Boolean.class;
                    
            }
            return null;
        }
    }
	/**
	 * Byte representations for values of a boolean primative.
	 * @author ij
	 *
	 */
	public static class Bool {
		private static final byte FALSE = 0;
		private static final byte TRUE = 1;
	}

	/**
	 * Write a boolean value to a byte buffer.
	 * @param v
	 * @param buffer
	 */
	public static void putBoolean(boolean v, ByteBuffer buffer) {
		buffer.put((byte)(v?1:0));		
	}
	/**
	 * Read a boolean from a byte buffer.
	 * @param b
	 * @return boolean from buffer
	 */
	public static boolean getBoolean(ByteBuffer b) {
		byte bt = b.get();
		if (bt == Bool.FALSE) {
			return false;
		} 
		return true;
	}
	/**
	 * 
	 * Get our string from byte buffer.
	 * 
	 * @param buffer
	 * 
	 * @return string from buffer
	 */
	
	public static String getString(ByteBuffer buffer) {
		short len = buffer.getShort();
		if (len == 0) {
			return null;
		}
		byte[] bites = new byte[(int) len];
		buffer.get(bites);
		return new String(bites,Charset.defaultCharset());
	}
	
	/**
	 * 
	 * Get our string from byte buffer.
	 * 
	 * @param buffer
	 * 
	 * @return string from buffer
	 */
	
	public static String getString(ByteBuffer buffer,int size) {
		short len = buffer.getShort();
		if (len == 0) {
			return null;
		}
		byte[] bites = new byte[size];
		buffer.get(bites);
		return new String(bites,0,len,Charset.defaultCharset());
	}
	/**
	 * 
	 * String is short and the bytes of length short.
	 * 
	 * @param string
	 * 
	 * @param buffer
	 */
	
	public static void putString(String string, ByteBuffer buffer) {
		putString(string,buffer,string.length());
	}
	
	private final static Charset charset = Charset.defaultCharset();
	
	/**
	 * Write a string up to a limit.
	 * @param string
	 * @param buffer
	 */
	
	public static void putString(String string, ByteBuffer buffer, int size) {
		if (string == null) {
			buffer.putShort((short) 0);
		} else {
			int p = buffer.position();
			byte[] bites = string.getBytes(charset);
			int len = string.length();
			buffer.putShort((short) size);
			int min = len < size ? len : size;
			buffer.put(bites,0,min);
			buffer.position(p+size+2);
		}
	}
	
	
	/**
	 * 
	 * Convert an object type to a byte definition of its type.
	 * 
	 * @param value
	 * 
	 * @return byte rep of object type
	 */
	
	public static byte getObjectType(Object value) {
		return getObjectTypeByClass(value.getClass());
	}

	/**
	 * get byte rep of class
	 * @param value
	 * @return byte representing that class.
	 */
    public static byte getObjectTypeByClass(Class<?> value) {
        if (value == Integer.class) {
            return Types.INT;
        } else if (value == Long.class) {
            return Types.LONG;
        } else if (value == Short.class) {
            return Types.SHORT;
        } else if (value == Character.class) {
            return Types.CHAR;
        } else if (value == Byte.class) {
            return Types.BYTE;
        } else if (value == String.class) {
            return Types.STRING;
        } else if (value == Double.class) {
            return Types.DOUBLE;
        } else if (value == Float.class) {
            return Types.FLOAT;
        } else if (value == Boolean.class) {
            return Types.BOOLEAN;
        }else {
            return Types.BARRAY;
        }
    }

    /**
     * Write object to byte buffer
     * @param object
     * @param buffer
     * @param writeType
     */
    public static void writeObject(Object object, ByteBuffer buffer, boolean writeType) {
        byte type = ByteUtils.getObjectType(object);
        writeObject(object,buffer,writeType,type);
    }
    
    /**
     * Write object to buffer
     * @param object
     * @param buffer
     * @param writeType
     * @param type
     */
    public static void writeObject(Object object, ByteBuffer buffer, boolean writeType,byte type) {

        if (writeType) {
            buffer.put(type);
        }
        switch (type) {
            case ByteUtils.Types.STRING:
                ByteUtils.putString((String)(object),buffer);
                break;
            case ByteUtils.Types.STRING_128:
                ByteUtils.putString((String)(object),buffer,128);
                break;
            case ByteUtils.Types.STRING_20:
                ByteUtils.putString((String)(object),buffer,20);
                break;
            case ByteUtils.Types.STRING_40:
                ByteUtils.putString((String)(object),buffer,40);
                break;
            case ByteUtils.Types.STRING_80:
                ByteUtils.putString((String)(object),buffer,80);
                break;
            case ByteUtils.Types.STRING_256:
                ByteUtils.putString((String)(object),buffer,256);
                break;
            case ByteUtils.Types.STRING_512:
                ByteUtils.putString((String)(object),buffer,512);
                break;
            case ByteUtils.Types.LONG:
                buffer.putLong((Long)(object));
                break;
            case ByteUtils.Types.SHORT:
                buffer.putShort((Short)(object));
                break;
            case ByteUtils.Types.CHAR:
                buffer.putChar((Character)(object));
                break;
            case ByteUtils.Types.DOUBLE:
                buffer.putDouble((Double)(object));
                break;
            case ByteUtils.Types.INT:
                buffer.putInt((Integer)(object));
                break;
            case ByteUtils.Types.BYTE:
                buffer.put((Byte)(object));
                break;
            case ByteUtils.Types.BOOLEAN:
                ByteUtils.putBoolean((Boolean)(object),buffer);
                break;
            default:
                throw new IllegalArgumentException("Invalid parameter type: "+ object.getClass());
        }

    }


}
