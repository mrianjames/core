package com.oaktree.core.patterns.subscription;

import com.oaktree.core.container.INameable;
import com.oaktree.core.patterns.sequence.IDataReceiver;

/**
 * A request for a subscription
 *
 * Created by ianjames on 06/05/2014.
 */
public interface ISubscriptionRequest<T> {
    /**
     * Get the request maker/callback target.
     * @return
     */
    public IDataReceiver<T> getDataReceiver();
    public String getKey();
    public SubscriptionType getSubscriptionType();

}
