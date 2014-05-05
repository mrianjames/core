package com.oaktree.core.collection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A maker of different implementations of lists.
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 18/01/12
 * Time: 23:09
 */
public interface ICollectionFactory<F> {
    /**
     * Make a map.
     * @return
     */
    public Collection<F> create();
}
