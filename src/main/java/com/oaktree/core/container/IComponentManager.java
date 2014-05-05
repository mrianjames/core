package com.oaktree.core.container;

import java.util.List;

/**
 * 
 * A manager of components in a system, both dynamic and non-dynamic.
 * 
 * @author Oak Tree Designs Ltd
 * 
 */
public interface IComponentManager extends IComponent {

	/**
	 * Set a factory for dynamically making components.
	 * 
	 * @param factory
	 */
	public void setDynamicComponentFactory(IDynamicComponentFactory factory);

	/**
	 * Set an optional startup comparator which will start components in a
	 * particular ordering.
	 * 
	 * @param comparator
	 */
	public void setStartupOrdering(List<ComponentType> ordering);

	/**
	 * Should we wait for one startup type to complete startup before moving
	 * onto the next?
	 * 
	 * @param wait
	 */
	public void setWaitBetweenStartupTypes(boolean wait);

	/**
	 * Add a component to be managed.
	 */
	public void addComponent(IComponent component);

	/**
	 * Remove a component from our management, optionally stopping.
	 * 
	 * @param component
	 */
	public void removeComponent(IComponent component, boolean stop);

	/**
	 * Get a named component
	 * 
	 * @param componentName
	 * @return
	 */
	public IComponent getComponentByName(String componentName);

	/**
	 * Get a list of components by a category. A null category does not mean get
	 * anything (or does a subtype of *...yet).
	 * 
	 * @param category
	 * @return
	 */
	public List<IComponent> getComponentsByCategory(ComponentType category, String subtype);

	/**
	 * Get an instance of a catorgorised component - i.e. take the first we
	 * find.
	 * 
	 * @param categor
	 * @param subtype
	 * @return
	 */
	public IComponent getComponentByCategory(ComponentType categor, String subtype);

	/**
	 * Get only available components
	 * 
	 * @param category
	 * @param subtype
	 * @return
	 */
	public List<IComponent> getAvailableComponentsByCategory(ComponentType category, String subtype);

	/**
	 * Resolve a task from a message.
	 * 
	 * @param message
	 * @return
	 */
	public IDynamicComponent[] resolveDynamicComponent(IMessage message);

	public void removeTask(IDynamicComponent task);

	public void startComponent(String task);

	public void stopComponent(String task);

	public void pauseTask(String task);

	public void resumeComponent(String task);

	public IComponent getDynamicComponent(String name);

	public int getNumComponents();

	/**
	 * Add an alternaitve name for a named component.
	 * 
	 * @param clOrdId
	 * @param d
	 */
	public void addAlias(String alias, IComponent d);

	/**
	 * Get all components registered.
	 * 
	 * @return
	 */
	public IComponent[] getComponents();

	/**
	 * get all registered dynamic components
	 * 
	 * @return
	 */
	public IDynamicComponent[] getDynamicComponents();

	/**
	 * get dynamic comps as an array of strings.
	 * 
	 * @return
	 */
	public String[] getDynamicComponentsAsString();

	/**
	 * Set a component listener to listen to events on this manager.
	 * 
	 * @param listener
	 */
	public void setListener(IComponentManagerListener listener);

	public void removeListener(IComponentManagerListener listener);

	/**
	 * Clear all entries from this component manager.
	 */
	public void clear();
}
