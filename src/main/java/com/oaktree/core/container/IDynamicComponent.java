package com.oaktree.core.container;

/**
 * A runnable task with basic life-cycle events. ITasks are run in an IContainer; the invocation of them
 * is up to an IDynamicComponentManager within the container. Tasks are the logic or transformation component of the 
 * application and can virtually anything they like. Some examples:
 * 1) Take an input application message, analyse it and send a new message to a component via the container.
 * 2) Take an input application message, analyse it and send a message to a discovered component directly.
 * 3) Startup and commence publishing messages to components either directly or via the container.
 * 
 * In most applications directing messages back via the container to route the messages to the correct location
 * is the best approach as this ensures that we do all the necesary checks on filtering multiple downstream
 * possible components to find a correct one, logging, dispatching etc.
 * 
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public interface IDynamicComponent extends IComponent {
	/**
	 * Set the object that can vend services this task requires.
	 * @param container
	 */
	public void setComponentManager(IComponentManager manager);

        /**
         * Get hold of the component manager.
         * @return
         */
        public IComponentManager getComponentManager();
}
