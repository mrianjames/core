package com.oaktree.core.threading.message;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.container.IMessage;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 25/07/11
 * Time: 19:03
 */
public interface IMessageDispatcher {

    public void dispatch(int key, IComponent component,IMessage msg);
    public void start();
    public void stop();
}
