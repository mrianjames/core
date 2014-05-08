package com.oaktree.core.patterns.subscription;

/**
 * Created by ianjames on 06/05/2014.
 */
public interface ISubscriptionResponse<T> {
    public SubscriptionResult getResult();
    public T getInitialSnapshot();

    public String getFailureReason();
}
