package com.oaktree.core.data.subscription;

import com.oaktree.core.data.sequence.IDataProvider;

import java.util.Collection;

/**
 * Filter providers by an interest key.
 * Created by ianjames on 08/05/2014.
 */
public interface IDataProviderFilter<T> {
    /**
     * Filter down our providers to something that can process our key. May return
     * more than one.
     * @param providers
     * @param key
     * @return
     */
    Collection<IDataProvider<T>> filter(Collection<IDataProvider<T>> providers, String key);
}
