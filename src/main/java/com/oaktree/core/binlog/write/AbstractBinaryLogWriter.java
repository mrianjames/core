package com.oaktree.core.binlog.write;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 09/04/13
 * Time: 18:31
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractBinaryLogWriter implements IBinaryLogWriter {
    protected final static Logger logger = LoggerFactory.getLogger(AbstractBinaryLogWriter.class);
    private String name;
    private volatile boolean canLog = false;
    public AbstractBinaryLogWriter(String name) {
        this.name = name;
    }
    @Override
    public void start() {
        logger.info("Starting binary logger " + name);
    }

    @Override
    public void stop() {
        logger.info("Stopping binary logger " + name);
    }

    protected void setCanLog(boolean canLog) {
        this.canLog = canLog;
    }

    @Override
    public boolean canLog() {
        return canLog;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
