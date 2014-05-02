package com.oaktree.core.binlog.write;

import com.oaktree.core.utils.ByteUtils;
import com.oaktree.core.utils.ResultTimer;
import com.oaktree.core.utils.UnsafeMemory;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Write collections of random sized records to file.
 * each record is written with its own header relating to:
 * num fields (int)|fields
 * each field is written as
 * field type (byte)|field length (int)|field value
 *
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 09/04/13
 * Time: 18:44
 * To change this template use File | Settings | File Templates.
 */
public class GenericFileBinaryLogWriter extends AbstractFileBinaryLogWriter implements IGenericBinaryLogWriter {


   
	public GenericFileBinaryLogWriter(boolean useByteBuffer,byte[] schema, String name, String fileName) {
        super(useByteBuffer,schema,name, fileName);
    }


    @Override
    public void log(Object[] values) {
        if (useByteBuffer ) {
	        int i = 0;
	        ByteBuffer buffer = bufferManager.get();
	        buffer.clear();
	        for (Object object:values) {
	            byte type = this.schema[i];
	            ByteUtils.writeObject(object, buffer, false,type);
	            i++;
	        }
	        writeBytes(buffer,false);
	        bufferManager.free(buffer);
        } else {
        	//unsafe...
        	UnsafeMemory um = pool.get();
        	um.reset();
        	int i = 0;
	        for (Object object:values) {
	            byte type = this.schema[i];
	            um.writeObject(object, false,type);
	            i++;
	        }
	        writeBytes(um.getBytes());
        	pool.free(um);
        }
    }

	public void log(long[] longs) {
		if (useByteBuffer) {
			ByteBuffer buffer = bufferManager.get();
			buffer.clear();
			for (long l : longs) {
				buffer.putLong(l);
			}
			writeBytes(buffer,false);
			bufferManager.free(buffer);
		} else {
			// unsafe...
			UnsafeMemory um = pool.get();
			um.reset();
			int i = 0;
			for (long l : longs) {
				um.putLong(l);
			}
			writeBytes(um.getBytes());
			pool.free(um);
		}
	}

    /**
     * Sample "custom" format.
     * @param time
     * @param a
     * @param b
     * @param c
     */
    public void log(long time, short a, short b, short c) {
    	if (useByteBuffer) {
	        ByteBuffer buffer = bufferManager.get();
	        buffer.clear();
	        buffer.putLong(time);
	        buffer.putShort(a);
	        buffer.putShort(b);
	        buffer.putShort(c);
	
	        writeBytes(buffer,false);
	        bufferManager.free(buffer);
    	} else {
    		UnsafeMemory um = pool.get();
    		um.reset();
    		um.putLong(time);
	        um.putShort(a);
	        um.putShort(b);
	        um.putShort(c);
			writeBytes(um.getBytes());
			pool.free(um);
    	}
    }

    public static void main(String[] args) throws Exception {
    	boolean useByteBuffer = true;
      long TESTS = 500000;
      ResultTimer t = new ResultTimer(10000);
      GenericFileBinaryLogWriter logger;
//         logger = new GenericFileBinaryLogWriter(useByteBuffer,new byte[]{ByteUtils.Types.LONG,ByteUtils.Types.LONG},"GFBL","test.bl");
//        logger.setUseByteBuffer(useByteBuffer);
//        logger.start();
//

//        for (long l = 0; l < TESTS; l++) {
//            t.startSample();
//            logger.log(new Object[]{12l+l,32l});
//            t.endSample();
//        }
//        logger.stop();
//        System.out.println("Object WriteDuration:" +t.toString(TimeUnit.MICROSECONDS));
//
//        t = new ResultTimer(10000);
//        for (long l = 0; l < TESTS; l++) {
//            t.startSample();
//            logger.log(new long[]{12l+l,32l});
//            t.endSample();
//        }
//        System.out.println("primative WriteDuration:" +t.toString(TimeUnit.MICROSECONDS));
//        logger.stop();
//
//
//        logger = new GenericFileBinaryLogWriter(useByteBuffer,new byte[]{ByteUtils.Types.LONG,ByteUtils.Types.SHORT,ByteUtils.Types.SHORT,ByteUtils.Types.SHORT},"GFBL","test.bl");
//        logger.setUseByteBuffer(useByteBuffer);
//        logger.start();
//
//        t = new ResultTimer(10000);
//        for (long l = 0; l < TESTS; l++) {
//            long time = System.currentTimeMillis();
//            t.startSample();
//            logger.log(time,(short)1,(short)1,(short)1);
//            t.endSample();
//        }
//        logger.stop();
//        System.out.println("set WriteDuration:" +t.toString(TimeUnit.MICROSECONDS));

        logger = new GenericFileBinaryLogWriter(useByteBuffer,new byte[]{ByteUtils.Types.LONG,ByteUtils.Types.LONG,ByteUtils.Types.INT,ByteUtils.Types.LONG,ByteUtils.Types.DOUBLE,ByteUtils.Types.CHAR,ByteUtils.Types.STRING_128},"GFBL","test.bl");
        logger.setUseByteBuffer(useByteBuffer);
        logger.start();

        t = new ResultTimer(10000);
        for (long l = 0; l < TESTS; l++) {
            t.startSample();
            logger.log(new Object[]{l,32l,567,345l,67.8,'a',"Fuckoffbigstringgettingpushed"});
            t.endSample();
        }
        logger.stop();
        System.out.println("BigObject WriteDuration:" +t.toString(TimeUnit.MICROSECONDS));

    }
}
