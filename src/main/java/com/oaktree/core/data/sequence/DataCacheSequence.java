package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.IData;
import com.oaktree.core.data.IDataReceiver;
import com.oaktree.core.data.cache.DataCache;
import com.oaktree.core.data.cache.IDataCache;

/**
 * Cache sequence that works on string keyed data objects.
 *
 * Created by ianjames on 13/05/2014.
 */
public class DataCacheSequence<I extends IData<String>,O extends IData<String>> extends DataSequence<I,O> {

    public DataCacheSequence(String name) {
        super(name);
    }

    /**
     * The cache.
     */
    private IDataCache<O,String> cache = new DataCache<O,String>();


    @Override
    public void onData(I data, IComponent from,final long receivedTime) {
        final O updated = process(data,from,receivedTime);
        cache.onData(updated,this,receivedTime);
        for (IDataReceiver<O> receiver:getReceivers()) {
            receiver.onData(updated, this, receivedTime);
        }

    }

    @Override
    public String toString() {
        return "DataCacheSequence["+getName()+"]";
    }
}
