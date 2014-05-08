package com.oaktree.core.patterns.subscription;

/**
 * Created by ianjames on 06/05/2014.
 */
public class SubscriptionResponse<T> implements ISubscriptionResponse<T> {
    private T initial;
    public void setInitial(T data) {
        this.initial = data;
    }
    String failureReason;
    public void setFailureReason(String reason) {
        this.failureReason = reason;
    }
    SubscriptionResult result = SubscriptionResult.NONE;
    public void setSubscriptionResult(SubscriptionResult result) {
        this.result = result;
    }
    @Override
    public SubscriptionResult getResult() {
        return result;
    }

    @Override
    public T getInitialSnapshot() {
        return initial;
    }


    @Override
    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String toString() {
        return result + (result.isFailure() ? " "+failureReason:"")+" initial: "+initial;
    }

    @Override
    public boolean equals(Object re) {
        if (!(re instanceof SubscriptionResponse)) {
            return false;
        }
        SubscriptionResponse r = (SubscriptionResponse)(re);
        if (!result.equals(r.getResult())) {
            return false;
        }
        if (failureReason != null ) {
            if (!failureReason.equals(r.getFailureReason())) {
                return false;
            }
        } else if (r.getFailureReason() != null) {
            return false;
        }
        if (initial != null) {
            return initial.equals(r.getInitialSnapshot());
        } else {
            return r.getInitialSnapshot() == null;
        }
    }

    @Override
    public int hashCode() {
        return 13+
                (failureReason != null ? failureReason.hashCode() : 0)+
                (initial != null ? initial.hashCode() : 0)+
                (result != null ? result.hashCode() : 0);

    }
}
