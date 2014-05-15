package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ianjames on 15/05/2014.
 *
 * Filters out data that has a key that is in the supplied list.
 *
 * I is input type and output type - for this sequence they are the same thing.
 * T is the type of the key that I Data object has.
 */
public class BlackKeyFilterSequence<I extends IData<T>,T> extends DataSequence<I,I> {

    /**
     * Keys to include.
     */
    private final Collection<T> blackList;

    /**
     *
     * @param name
     * @param list - black-list of keys of type T
     */
    public BlackKeyFilterSequence(String name, Collection<T> list) {
        super(name);
        if (list == null) {
            list = new ArrayList<>();
        }
        blackList = list;
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
        if (blackList.contains(data.getDataKey())) {
            return null;
        }
        return data;
    }
}
