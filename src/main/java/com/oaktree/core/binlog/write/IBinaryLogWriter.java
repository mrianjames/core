package com.oaktree.core.binlog.write;

/**
 * Archetype binary logger.
 *
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 09/04/13
 * Time: 18:30
 * To change this template use File | Settings | File Templates.
 */
public interface IBinaryLogWriter {
    public void start();
    public void stop();
    public boolean canLog();
}
