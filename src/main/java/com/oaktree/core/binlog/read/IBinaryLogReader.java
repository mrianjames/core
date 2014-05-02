package com.oaktree.core.binlog.read;

import java.nio.ByteBuffer;

/**
 * Reader of binary logs.
 *
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 10/04/13
 * Time: 07:39
 * To change this template use File | Settings | File Templates.
 */
public interface IBinaryLogReader {
	/**
	 * Open the reader.
	 */
    public void open();
    /**
     * Close the reader.
     */
    public void close();

    /**
     * Get a record as collection of objects.
     * @param index
     * @return
     */
    public Object[] getRecordAsObjects(long index);

    /**
     * Get a record as a byte buffer.
     * @param record
     * @return
     */
    public ByteBuffer getRecord(long record);

    /**
     * Get the schema this file has.
     * @return schema in bytes
     */
    public byte[] getSchema();

    /**
     * Validate the consistency of the file.
     * @return
     */
    public boolean validate();

    long getNumRecords();
}
