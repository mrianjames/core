package com.oaktree.core.container;

import com.oaktree.core.threading.dispatcher.IDispatcher;
import java.util.List;

/**
 * A container is an application, handling the basic mechanics of a 
 * widely used "container" design pattern. In essence a container is a class that
 * can run and manage a number of runnable tasks that may or may not require components or services
 * that can be looked up and are themselves managed by the container. 
 * The container communicates with running tasks by passing messages of varying types,
 * for example data or control signals and the tasks can communicate with the container in a similar fashion.
 * 
 * A container can be paused or resumed as can all individual tasks as it itself is a component.
 * 
 * The container passes messages from a component to other components. For example, an upstream component 
 * that receives messages from another system is given the message with a target of DYNAMIC_COMPONENT_MANAGER; The 
 * container finds there (via a component manager) is one task resolver component. This component is
 * given the message and makes a new task for that message; it then gives the task the message with a 
 * target of TASK. The task then does what it needs; when it desires it can then fire messages to the container
 * for a downstream component with a target of DOWNSTREAM, "XLON". If the container finds multiple then an optional
 * filter is applied to pick the one it should use (e.g. round robin, random, load) and the message is delegated.
 * 
 * @author ij
 *
 */
public interface IContainer extends IComponent, IMessageListener, IMessageSender {

    /**
     * Get what time the container started
     * @return
     */
    public long getStartupTime();

	/**
	 * Set our component manager who will look after all the components.
	 * @param manager
	 */
	public void setComponentManager(IComponentManager manager);
	
	/**
	 * Get our component manager this is managing all our components.
	 * @return
	 */
	public IComponentManager getComponentManager();
	
	/**
	 * Register a listener on a component. 
	 * @param target
	 * @param listener
	 */
	public void registerAsComponentListener(String target, IComponentListener listener);
	
		/**
	 * Set our dispatcher.
	 * @param dispatcher
	 */
	public void setDispatcher(IDispatcher dispatcher);

	/**
	 * Set our optional component filter.
	 * @param filter
	 */
	public void setComponentFilter(IComponentFilter filter);

	/**
	 * Add additional listeners to message flow, after the primary target has been given the message.
	 * @param component
	 */
	public void addMessageProcessor(IComponent component);

        /**
         * Set a collection of message listening components.
         * 
         * @param list
         */
        public void setMessageProcessingComponents(List<IComponent> list) ;
	/**
	 * Get a named component
	 * @param name
	 * @return
	 */
	public IComponent getComponent(String name);
	
	/**
	 * Get the containers dispatcher, if set.
	 * @return
	 */
	public IDispatcher getDispatcher();
}
