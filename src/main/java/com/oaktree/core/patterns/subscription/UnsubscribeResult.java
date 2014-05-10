package com.oaktree.core.patterns.subscription;

/**
 * Created by ianjames on 06/05/2014.
 */
public enum UnsubscribeResult {
    UNSUBSCRIBE_OK,UNSUBSCRIBE_FAILURE, NONE;

    public boolean isSuccess() {
        return this.equals(UNSUBSCRIBE_OK);
    }
    public boolean isFailure() {
        return this.equals(UNSUBSCRIBE_FAILURE);
    }
}
