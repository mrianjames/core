package com.oaktree.core.data.subscription;

import com.oaktree.core.data.sequence.IDataReceiver;

/**
 * A request for an unsubscribe.
 *
 * Created by ianjames on 06/05/2014.
 */
public interface IUnsubscribeRequest<T> {
    /**
     * Get the request maker/callback target.
     * @return
     */
    public IDataReceiver<T> getDataReceiver();
    public String getKey();
    public int getSubscriptionType();

}
