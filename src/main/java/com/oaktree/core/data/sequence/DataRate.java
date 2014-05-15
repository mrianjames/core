package com.oaktree.core.data.sequence;

import com.oaktree.core.data.IData;

import java.util.Collection;

/**
 * This object can hold others - for example a collection of keyed datarates.
 *
 * Created by ianjames on 14/05/2014.
 */
public class DataRate<T> implements IData<T> {
    public static final String COMPOSITE = "COMPOSITE";
    public static final String ALL = "*";
    private T key;
    private double rate;
    private Collection<DataRate<T>> children;
    public void addChild(DataRate<T> child) {
        children.add(child);
    }
    public Collection<DataRate<T>> getChildren() {
        return children;
    }
    public DataRate(T key, double rate) {
        this.key = key;
        this.rate = rate;
    }
    public DataRate() {
    }
    public boolean isComposite() {
        return children != null;
    }
    @Override
    public T getDataKey() {
        return null;
    }

    @Override
    public String toString() {
        if (!isComposite()) {
            return (key.equals(ALL) ? "" : key) + " rate: " + rate;
        } else {
            StringBuilder b = new StringBuilder(1024);
            for (DataRate<T> rt:children) {
                b.append(rt.toString());
                b.append(",");
            }
            return b.toString();
        }
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object x) {
        if (x instanceof DataRate) {
            return key.equals(((DataRate) (x)).getDataKey());
        }
        return false;
    }
}
