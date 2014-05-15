package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ianjames on 15/05/2014.
 *
 * Filters out data that is in the supplied list.
 *
 * I is input type and output type - for this sequence they are the same thing.
 * T is the type of the key that I Data object has.
 */
public class BlackDataFilterSequence<I extends IData<?>> extends DataSequence<I,I> {

    /**
     * Keys to include.
     */
    private final Collection<I> blackList;

    /**
     *
     * @param name
     * @param list - black-list of data of key type T
     */
    public BlackDataFilterSequence(String name, Collection<I> list) {
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
        if (blackList.contains(data)) {
            return null;
        }
        return data;
    }
}
