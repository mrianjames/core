package com.oaktree.core.collection;

import java.util.ArrayList;
import java.util.List;

/**
 * maker of array lists.
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 18/01/12
 * Time: 23:10
 */
public class ArrayListFactory<F> implements ICollectionFactory<F> {

    private int capacity = 1000;
    public ArrayListFactory(){}
    public ArrayListFactory(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public List<F> create() {
        return new ArrayList<F>(capacity);
    }
}
