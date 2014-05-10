package com.oaktree.core.patterns.subscription;

import com.oaktree.core.patterns.sequence.IDataReceiver;
import com.oaktree.core.utils.Text;

/**
 * Created by ianjames on 08/05/2014.
 */
public class UnsubscribeRequest<T> implements ISubscriptionRequest<T> {

    private IDataReceiver<T> recevier;
    private String key;
    private SubscriptionType type;
    public UnsubscribeRequest(String key, IDataReceiver<T> receiver, SubscriptionType type) {
        if (key == null) {
            throw new IllegalArgumentException("Null key");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Null receiver");
        }
        this.key = key;
        this.recevier = receiver;
        this.type = type;
    }
    @Override
    public IDataReceiver<T> getDataReceiver() {
        return recevier;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public SubscriptionType getSubscriptionType() {
        return type;
    }

    @Override
    public String toString() {
        return type.name() + Text.SPACE  + key + Text.SPACE + recevier.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnsubscribeRequest)) {
            return false;
        }
        UnsubscribeRequest r = (UnsubscribeRequest)(o);
        return recevier.getName().equals(r.getDataReceiver().getName()) && key.equals(r.getKey());
    }

    @Override
    public int hashCode() {
        return 32 + recevier.getName().hashCode()+ key.hashCode();
    }
}
