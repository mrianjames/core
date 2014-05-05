package com.oaktree.core.kdb;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 12/10/11
 * Time: 08:02
 */
public class KdbConnection extends AbstractComponent implements IConnection {
    private String host;
    private int port;
    private String username;
    private String password;
    private c connection;
    private Logger logger = LoggerFactory.getLogger(KdbConnection.class);

    public KdbConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public KdbConnection(String host, int port, String username, String password) {
        this(host,port);
        this.username = username;
        this.password = password;
    }
    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isConnected() {
        return this.getState().equals(ComponentState.AVAILABLE);
    }

    @Override
    public void start() {
        //only boot once
        if (this.getState().isStarting() || this.getState().isStarted() || this.getState().isAvailable()) {
            return;
        }
        this.setState(ComponentState.STARTING);
        //fire up the kdb connection.
        try {
            if (username == null) {
                connection = new c(host,port);
            } else {
                connection = new c(host,port,username); //pword?
            }
        } catch (Exception e) {
            Log.exception(logger, e);
        }
        this.setState(ComponentState.AVAILABLE);
    }


    @Override
    public void stop() {
        if (this.isConnected()) {
            this.setState(ComponentState.STOPPING);
            //fire up the kdb connection.
            try {
                connection.close();
            } catch (Exception e) {
                Log.exception(logger, e);
            }
            this.setState(ComponentState.STOPPED);
        }
    }

    @Override
    public String toString() {
        return host + ":" + port + ":" + username + ":" + password + ". Connected: " + this.isConnected();
    }

    /**
     * Insert data into specified table. The data is
     * columns of row data e.g.
     * data = { dates, times, syms };
     * @param table
     * @param data
     * @return
     */
    public boolean insert(String table, Object[][] data) {
        try {
            connection.ks("insert", table, data);
            return true;
        } catch (Exception e) {
            Log.exception(logger, e);
        }
        return false;
    }
}
