package com.oaktree.core.patterns.subscription;

/**
 * Created by ianjames on 06/05/2014.
 */
public enum SubscriptionType {
    /**
     * Asynchronous snap (no subscribe)
     */
    ASYNC_SNAP,
    /**
     * Synchronous snap (no subscribe)
     */
    SYNC_SNAP,ASYNC_SNAP_SUBSCRIBE,SYNC_SNAP_SUBSCRIBE,SUBSCRIBE_NO_SNAP;

    public boolean isSubscribe() {
        return this.equals(SYNC_SNAP_SUBSCRIBE) || this.equals(ASYNC_SNAP_SUBSCRIBE) || this.equals(SUBSCRIBE_NO_SNAP);
    }
    public boolean isSnap() {
        return !this.equals(SUBSCRIBE_NO_SNAP);
    }

    public boolean isAsyncSnap() {
        return this.equals(ASYNC_SNAP_SUBSCRIBE) || this.equals(ASYNC_SNAP);
    }
}
