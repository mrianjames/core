package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Filters out data that has a data that is not in the supplied list.
 *
 * Created by ianjames on 15/05/2014.
 * I is input type and output type - for this sequence they are the same thing.
 * T is the type of the key that I Data object has.
 */
public class WhiteDataFilterSequence<I extends IData<?>> extends DataSequence<I,I> {

    /**
     * Keys to include.
     */
    private final Collection<I> whiteList;

    /**
     *
     * @param name
     * @param list - white-list of datas that are allowed.
     */
    public WhiteDataFilterSequence(String name, Collection<I> list) {
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
        if (whiteList.contains(data)) {
            return data;
        }
        return null;
    }
}
