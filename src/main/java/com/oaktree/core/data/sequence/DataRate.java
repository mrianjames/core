package com.oaktree.core.data.sequence;

import com.oaktree.core.data.IData;

/**
 *
 * Created by ianjames on 14/05/2014.
 */
public class DataRate implements IData<String> {
    public static String ALL = "*";
    private final String key;
    private final double rate;

    public DataRate(String key, double rate) {
        this.key = key;
        this.rate = rate;
    }
    @Override
    public String getDataKey() {
        return null;
    }

    @Override
    public String toString() {
        return (key.equals(ALL) ? "" : key) + " rate: " + rate;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object x) {
        if (x instanceof DataRate) {
            return key.equals(((DataRate)(x)).getDataKey());
        }
        return false;
    }
}
