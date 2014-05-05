package com.oaktree.core.threading.policy;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.container.IMessage;

/**
 * A threading policy that deals with component name.
 * @author ij
 *
 */
public class ComponentNameThreadingPolicy implements IThreadingPolicy {

    private IComponent component;

    public ComponentNameThreadingPolicy(IComponent component) {
        this.component = component;
    }

	@Override
	public String getDispatchKey(IMessage msg) {
		return component.getName();
	}

}
