package com.oaktree.core.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 10/06/12
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */
public class SetFactory <F> implements ICollectionFactory<F> {

    private int capacity = 1000;
    private boolean concurrent;

    public SetFactory(){}
    public SetFactory(int capacity, boolean concurrent) {
        this.capacity = capacity;
        this.concurrent = concurrent;
    }

    @Override
    public Collection<F> create() {
        if (concurrent) {
            return new CopyOnWriteArraySet<F>();
        }
        return new HashSet<F>(capacity);
    }
}
