package com.oaktree.core.container;

/**
 * listener to the addition and removal of components.
 * @author ij
 */
public interface IComponentManagerListener {

    public void onComponentAdded(IComponent c);

    public void onComponentRemoved(IComponent c);
}
