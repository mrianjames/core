package com.oaktree.core.data.subscription;

/**
 * Created by ianjames on 06/05/2014.
 */
public interface IUnsubscribeResponse<T> {
    public UnsubscribeResult getResult();

    public String getFailureReason();
}
