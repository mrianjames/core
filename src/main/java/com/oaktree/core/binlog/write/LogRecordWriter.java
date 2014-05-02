package com.oaktree.core.binlog.write;

import com.oaktree.core.logging.Level;
import com.oaktree.core.utils.ByteUtils;
import com.oaktree.core.utils.UnsafeMemory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Writers records that have std log info.
 * 
 * @author ianjames
 *
 */
public class LogRecordWriter extends AbstractFileBinaryLogWriter implements
		ILogRecordWriter {
	private static final byte[] schema = new byte[]{ByteUtils.Types.LONG,ByteUtils.Types.STRING_20,ByteUtils.Types.SHORT,
			ByteUtils.Types.INT,ByteUtils.Types.STRING_128};
	
	public LogRecordWriter(boolean useByteBuffer, String name,
			String fileName) {
		super(useByteBuffer, schema, name, fileName);
	}

	@Override
	public void log(long time, String threadname, short threadId, Level level,
			String message) {
		if (useByteBuffer) {
	        ByteBuffer buffer = bufferManager.get();
	        buffer.clear();
	        buffer.putLong(time);
	        ByteUtils.putString(threadname, buffer,20);
	        buffer.putShort(threadId);
	        buffer.putInt(level.intValue());
	        ByteUtils.putString(message,buffer,128);
	
	        writeBytes(buffer,false);
	        bufferManager.free(buffer);
    	} else {
    		UnsafeMemory um = pool.get();
    		um.reset();
    		um.putLong(time);
    		um.putString(threadname,20);
    		um.putShort(threadId);
    		um.putInt(level.intValue());
    		um.putString(message,128);
			writeBytes(um.getBytes());
			pool.free(um);
    	}
	}

	
	/**
	 * Simple test.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
//		String message = "I AM A TEST MESSAGE";
//		int MESSAGES = 10000000;
//		LogRecordWriter writer = new LogRecordWriter(true,"test","log.txt");
//		writer.start();
//		Thread t = Thread.currentThread();
//		long s = System.nanoTime();
//		for (int i = 0; i < MESSAGES;i++) {
//			writer.log(System.currentTimeMillis(), t.getName(), (short)t.getId(), Level.INFO, message);
//		}
//		long e = System.nanoTime();
//		long d = e-s;
//		System.out.println("Wrote "+MESSAGES + " messages with avg " + (d/MESSAGES) + "ns.");
		
		
	}
}
