package com.oaktree.core.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * maker of array lists. copyonwrite for thread safety.
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 18/01/12
 * Time: 23:10
 */
public class CopyOnWriteArrayListFactory<F> implements ICollectionFactory<F> {

    public CopyOnWriteArrayListFactory(){}

    @Override
    public List<F> create() {
        return new CopyOnWriteArrayList<>();
    }
}
