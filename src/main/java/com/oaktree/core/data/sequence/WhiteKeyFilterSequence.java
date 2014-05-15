package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Filters out data that has a key that is not in the supplied list.
 *
 * Created by ianjames on 15/05/2014.
 * I is input type and output type - for this sequence they are the same thing.
 * T is the type of the key that I Data object has.
 */
public class WhiteKeyFilterSequence<I extends IData<T>,T> extends DataSequence<I,I> {

    /**
     * Keys to include.
     */
    private final Collection<T> whiteList;

    /**
     *
     * @param name
     * @param list - white-list of keys of type T
     */
    public WhiteKeyFilterSequence(String name, Collection<T> list) {
        super(name);
        if (list == null) {
            list = new ArrayList<>();
        }
        whiteList = list;
    }

    /**
     * Override
     * @param data
     * @param from
     * @param receivedTime
     * @return
     */
    @Override
    protected I process(I data, IComponent from, long receivedTime) {
        if (whiteList.contains(data.getDataKey())) {
            return data;
        }
        return null;
    }
}
