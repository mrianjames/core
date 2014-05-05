package com.oaktree.core.collection;

import java.util.Map;

/**
 * A maker of different implementations of maps.
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 18/01/12
 * Time: 23:09
 */
public interface IMapFactory<E,F> {
    /**
     * Make a map.
     * @return map
     */
    public Map<E,F> create();
}
