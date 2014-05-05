package com.oaktree.core.binlog.read;

import com.oaktree.core.utils.ByteUtils;
import com.oaktree.core.utils.ResultTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Reader of binary log file.
 * A file has a schema that we derive by reading the file header.
 * The schema is considered as the header constructed as
 * int - num fields
 * and a byte per field describing the format of the value.
 * @see ByteUtils.Types
 *
 * This class allows a caller to randomly access records within the file and present them as a
 * bytebuffer or as a converted array of objects.
 *
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 10/04/13
 * Time: 07:40
 * To change this template use File | Settings | File Templates.
 */
public class GenericFileBinaryLogReader implements IBinaryLogReader {
    private static final int MAX_STRING_SIZE = 256;
    private final String fileName;
    private RandomAccessFile file;
    private FileChannel fc;
    private final static Logger log = LoggerFactory.getLogger(GenericFileBinaryLogReader.class);
    private byte[] schema;
    private long headerEnd; //header end position.
	private RandomAccessFile indexfile;
	private FileChannel indexChannel;

    public GenericFileBinaryLogReader(String fileName) {
        this.fileName = fileName;
    }
    @Override
    public void open() {
        try {
            this.file = new RandomAccessFile(fileName,"r");
            this.fc = file.getChannel();
            this.schema = readHeader();
            if (log.isTraceEnabled()) {
                log.trace("Schema:" + classesToString(schema));
            }
        } catch (Exception e) {
            log.error("Error opening file " + fileName + ": " + e.getMessage());
        }
   }

    private String classesToString(byte[] schema) {
        final StringBuilder b = new StringBuilder(256);
        for (byte c:schema) {
            b.append(ByteUtils.toDescription(c) +",");
        }
        return b.toString();
    }

    @Override
    public long getNumRecords() {
//        long records = 0;
//        try {
//            long bytes = fc.size();
//            records = bytes/ByteUtils.calcSchemaSize(schema,MAX_STRING_SIZE);
//        } catch (Exception e) {
//            log.error("Error getting filesize: " + e.getMessage());
//        }
//        return records;
    	try {
	    	this.indexfile = new RandomAccessFile(fileName+".index","r");
	        this.indexChannel = indexfile.getChannel();
	        MappedByteBuffer mbb = indexChannel.map(MapMode.READ_ONLY, 0, indexChannel.size());
	       
	        long records = mbb.getLong()+1;
	        indexChannel.close();
	        return records;
    	} catch (Exception e) {
    		throw new IllegalStateException("Problem opening or operating index file",e);
    	}
        
    }

    /**
     * Gets a record and prints it to logger
     * @param record
     */
    public void printRecord(long record) {
        StringBuilder l = new StringBuilder(256);
        ByteBuffer buffer = getRecord(record);
        if (buffer != null) {
            buffer.flip();
            for (byte b:schema) {
                Object obj = ByteUtils.getObject(buffer,b);
                l.append(obj);
                l.append(",");
            }
        }   else {
            log.warn("Cannot retreive record " + record);
        }
        log.info("Record " + record +": " + l.toString());
    }

    /**
     * Gets a record as a bytebuffer
     * @param record
     * @return record as a byte buffer.
     */
    public ByteBuffer getRecord(long record) {
        try {
            int recordSize = ByteUtils.calcSchemaSize(schema,MAX_STRING_SIZE);
            long position = headerEnd + (record * recordSize);
            fc.position(position);
            ByteBuffer buffer = ByteBuffer.allocate(recordSize);
            fc.read(buffer,position);
            //buffer.flip();
            return buffer;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Convenience static method to avoid creating the object etc.
     * @param fileName
     * @param index
     * @return record
     */
    public static Object[] getRecordByIndex(String fileName, long index) {
        GenericFileBinaryLogReader r = new GenericFileBinaryLogReader(fileName);
        r.open();
        Object[] objects = r.getRecordAsObjects(index);
        r.close();
        return objects;
    }

    @Override
    public Object[] getRecordAsObjects(long index) {
        ByteBuffer buffer = getRecord(index);
        List<Object> o = new ArrayList<Object>();
        if (buffer != null) {
            buffer.flip();
            for (byte b : getSchema()) {
                o.add(ByteUtils.getObject(buffer, b));
            }
        }
        return o.toArray(new Object[o.size()]);
    }



    public byte[] readHeader() throws IOException {
       ByteBuffer buf = ByteBuffer.allocate(4);
       fc.read(buf);
       buf.flip();
       int fields = buf.getInt();

       buf = ByteBuffer.allocate(fields);
       fc.read(buf,4);
       buf.flip();

       byte[] classes = new byte[fields];
       for (int i=0;i<fields;i++) {
           classes[i] = buf.get();
       }
       this.headerEnd = 4+fields;

       return classes;
   }

    @Override
    public void close() {
        try {
            this.fc.close();
            log.info("File "+fileName+" closed");
        } catch (Exception e) {
            log.error("Error opening file " + fileName + ": " + e.getMessage());
        }
    }

    public final static void printAll(String fileName) {
        GenericFileBinaryLogReader r = new GenericFileBinaryLogReader(fileName);
        r.open();
        long numRecords = r.getNumRecords();
        for (long l = 0;l < numRecords;l++) {
            r.printRecord(l);
        }
        r.close();
    }
    public static long getNumberRecords(String fileName) {
        GenericFileBinaryLogReader r = new GenericFileBinaryLogReader(fileName);
        r.open();
        long numRecords = r.getNumRecords();
        r.close();
        return numRecords;
    }


    public byte[] getSchema() {
        return schema;
    }

    @Override
    public boolean validate() {
        //validate the schema holds for every record
        long numRecords = getNumRecords();
        for (long l = 0;l < numRecords;l++) {
            try {
                Object[] values = getRecordAsObjects(l);
            } catch (Exception e) {
                log.warn("Record " + l + " failed validation: " + e.getMessage());
                return false;
            }
        }
        log.info("File " + fileName + " is validated");
        return true;
    }


    /**
     * SAMPLE TEST RUNS.
     * @param args
     */
    public static void main(String[] args) {
        //GenericFileBinaryLogReader.printAll("test.bl");

        GenericFileBinaryLogReader reader = new GenericFileBinaryLogReader("test.bl");
        reader.open();
        reader.validate();
        long numRecords = reader.getNumRecords();
        log.info("Records: " + numRecords);
        ResultTimer timer = new ResultTimer(10000);
        for (long l =0; l < numRecords; l++) {
            timer.startSample();
            Object[] record = reader.getRecordAsObjects(l);
            timer.endSample();
            System.out.println("Record #"+l+": " + Arrays.asList(record));
        }
        log.info(timer.toString(TimeUnit.MICROSECONDS));
        //avg 15us to read random.
        reader.close();
    }

}

