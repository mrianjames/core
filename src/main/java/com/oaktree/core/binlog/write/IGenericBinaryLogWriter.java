package com.oaktree.core.binlog.write;

/**
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 09/04/13
 * Time: 18:43
 * To change this template use File | Settings | File Templates.
 */
public interface IGenericBinaryLogWriter extends IBinaryLogWriter {
    public void log(Object[] values);
}
