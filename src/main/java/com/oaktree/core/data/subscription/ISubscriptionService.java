package com.oaktree.core.data.subscription;

import com.oaktree.core.container.IComponent;

/**
 * Created by ianjames on 06/05/2014.
 */
public interface ISubscriptionService<T> extends IComponent {

    /**
     * Make a subscription.
     * @param request
     * @return
     */
    public ISubscriptionResponse<T> subscribe(ISubscriptionRequest<T> request);

    /**
     *
     * @param request
     * @return
     */
    public IUnsubscribeResponse<T> unsubscribe(IUnsubscribeRequest<T> request);
}
