package com.oaktree.core.container;

/**
 *
 * @author ij
 */
public abstract class AbstractEvent extends Message implements IEvent {

    @Override
    public int getEventType() {
        return 0;
    }


}
