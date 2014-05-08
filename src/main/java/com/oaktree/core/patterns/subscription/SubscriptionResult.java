package com.oaktree.core.patterns.subscription;

/**
 * Created by ianjames on 06/05/2014.
 */
public enum SubscriptionResult {
    SUBSCRIPTION_OK,SUBSCRIPTION_FAILURE, NONE;

    public boolean isSuccess() {
        return this.equals(SUBSCRIPTION_OK);
    }
    public boolean isFailure() {
        return this.equals(SUBSCRIPTION_FAILURE);
    }
}
