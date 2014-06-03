package com.oaktree.core.collection.multimap;

import com.oaktree.core.collection.ArrayListFactory;
import com.oaktree.core.collection.HashMapFactory;
import com.oaktree.core.collection.ICollectionFactory;
import com.oaktree.core.collection.IMapFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Multimap that delegates both map and backing collection implementations to injectable factories.
 * This class is as threadsafe as the collection types you configure.
 *
 * By default it makes a standard HashMap with ArrayList backing.
 *
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 18/01/12
 * Time: 23:08
 */
public class MultiMap<E,F> implements IMultiMap<E,F> {

    private Map<E,Collection<F>> map;
    private ICollectionFactory<F> collectionFactory = new ArrayListFactory<F>(1000);

    public MultiMap(){
        map = new HashMapFactory<E,Collection<F>>().create();
        collectionFactory = new ArrayListFactory<F>();
    }

    public MultiMap(boolean concurrent){
        map = new HashMapFactory<E,Collection<F>>(concurrent).create();
        collectionFactory = new ArrayListFactory<F>(concurrent);
    }

    public MultiMap(IMapFactory<E, Collection<F>> mapFactory, ICollectionFactory<F> collectionFactory) {
        map = mapFactory.create();
        this.collectionFactory = collectionFactory;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void put(E key, F value) {
        Collection<F> c = map.get(key);
        if (c == null) {
            c = collectionFactory.create();
            map.put(key,c);
        }
        c.add(value);
    }

    @Override
    public int getSize(E key) {
        Collection<F> col = map.get(key);
        if (col == null) {
            return 0;
        }
        return col.size();
    }

    @Override
    public Collection<F> get(E key) {
        if (key == null) {
            return null;
        }
        return map.get(key);
    }

    @Override
    public void remove(E key) {
        map.remove(key);
    }

    @Override
    public void remove(E key, F value) {
        Collection<F> c = get(key);
        if (c == null) {
            return;
        }
        c.remove(value);
    }

    @Override
    public void clear(E key) {
        Collection<F> col = get(key);
        if (col == null) {
            return;
        }
        col.clear();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Collection<E> keySet() {
        return new HashSet<E>(map.keySet());
    }
}
