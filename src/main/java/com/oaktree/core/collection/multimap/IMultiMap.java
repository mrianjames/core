package com.oaktree.core.collection.multimap;

import java.util.Collection;

/**
 * Light weight implementation of multimap.
 * Other open source ones are performance drains.
 * A MultiMap is a map where the value is another collection that holds multiple values. This collection
 * replaces boilerplate code where you can have n items of a key e.g.
 *
 * <code>
 * List<String> x = map.get(key);
 * if (x == null) {
 *      x = new ArrayList<String>();
 *      map.put(key,x);
 * }
 * x.add(value);
 * </code>
 * replaced with
 * <code>
 *     multimap.put(key,value);
 * </code>
 *
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 18/01/12
 * Time: 23:05
 */
public interface IMultiMap<E,F> {

    /**
     * Get the number of key entries in this multimap.
     * @return number of entries in multimap.
     */
    public int size();

    /**
     * Put a value into a backing collection that has a key.
     * the backing collection can be any Collection type .e.g
     * List, Set.
     *
     * @param key
     * @param value
     */
    public void put(E key, F value);

    /**
     * Get the size of the underlying collection for a key
     * @param key
     * @return size of collection for a key.
     */
    public int getSize(E key);

    /**
     * Get the underlying collection for a key.
     * @param key
     * @return underlying collection for a key
     */
    public Collection<F> get(E key);

    /**
     * Destroy the underlying collection for a key
     * @param key
     */
    public void remove(E key);

    /**
     * Destroy the underlying collection for a key
     * @param key
     */
    public void remove(E key,F value);

    /**
     * Clear the underlying collection for key but leave the
     * data structure linked to that key.
     * @param key
     */
    public void clear(E key);

    /**
     * Clear the entire map.
     */
    public void clear();

    /**
     * Get a collection of the keys.
     * @return collection of keys
     */
    public Collection<E> keySet();
}
