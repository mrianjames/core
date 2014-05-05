package com.oaktree.core.threading.message;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.container.IMessage;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 25/07/11
 * Time: 19:19
 */
public interface IQueueProcessor {

    /**
     * Dispatch the routing of a key to a component
     * @param key
     * @param component
     * @param msg
     */
    public void add(int key, IComponent component, IMessage msg);

    public void start();

    public void stop();

    void setName(String s);
}
