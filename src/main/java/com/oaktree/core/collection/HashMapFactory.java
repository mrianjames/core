package com.oaktree.core.collection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 18/01/12
 * Time: 23:11
 */
public class HashMapFactory<E,F> implements IMapFactory<E,F> {

    private boolean concurrent = false;
    private int capacity = 1000;

    public HashMapFactory() {}
    public HashMapFactory(int capacity, boolean concurrent) {
        this.capacity = capacity;
        this.concurrent = concurrent;
    }
    @Override
    public Map<E, F> create() {
        if (concurrent) {
            return new ConcurrentHashMap<E, F>(capacity);
        }
        return new HashMap<E, F>(capacity);
    }
}
