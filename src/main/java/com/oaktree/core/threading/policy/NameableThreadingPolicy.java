package com.oaktree.core.threading.policy;

import com.oaktree.core.container.IMessage;
import com.oaktree.core.container.INameable;

/**
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 12/08/12
 * Time: 18:15
 * To change this template use File | Settings | File Templates.
 */
public class NameableThreadingPolicy implements IThreadingPolicy{
    private String name;

    public NameableThreadingPolicy(String nameable) {
        this.name = nameable;
    }
    public NameableThreadingPolicy(INameable nameable) {
        this.name = nameable.getName();
    }

    @Override
    public String getDispatchKey(IMessage msg) {
        return name;
    }
}
