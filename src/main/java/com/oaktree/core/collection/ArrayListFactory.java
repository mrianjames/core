package com.oaktree.core.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * maker of array lists.
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 18/01/12
 * Time: 23:10
 */
public class ArrayListFactory<F> implements ICollectionFactory<F> {

    private int capacity = 1000;
    private boolean concurrent = false;
    public ArrayListFactory(){}
    public ArrayListFactory(int capacity) {
        this.capacity = capacity;
    }
    public ArrayListFactory(boolean concurrent){this.concurrent = concurrent;}
    public ArrayListFactory(boolean concurrent,int capacity) {
        this.concurrent = concurrent;this.capacity = capacity;
    }

    @Override
    public List<F> create() {
        if (concurrent) {
            return new CopyOnWriteArrayList<>();
        } else {
            return new ArrayList<F>(capacity);
        }
    }
}
