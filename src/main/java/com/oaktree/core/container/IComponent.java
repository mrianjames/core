package com.oaktree.core.container;


/**
 * A managed service in an application or container that has a defined range of 
 * life-cycle events that change its state.
 * 
 * A component can be listened to by other components as it deems its state has changed; it
 * can also listen to others.
 * 
 * IComponent A component is the very foundation of any system. A component has the following attributes: 
 *
 * Name e.g. MARKET_DATA_SERVICE. Should be unique in system.
 * Type - a "flavour" for the component. An enum exists to define:
 * SERVICE - a helper module that can be used by other components e.g. A component that manages market data registrations/updates
 * TASK - a possibly dynamically constructed module or strategy. There tends to be many of these in a system and they tend to be receipients of data and provide the business logic.
 * DOWNSTREAM - A speciailisation of SERVICE that provides routing and connectivity to systems below us in the technology stack.
 * UPSTREAM - A specialisation of SERVICE that provides routing and connectivity to systems above us in the technology stack. For example, if we were to receive FIX messages from an OMS to our engine we would have a FixUpstream to manage this.
 * SubType - A speciailisation of the type. For example our system may have multiple downstream connectivity components, each capable of sending orders to a different downstream endpoint - DOWNSTREAM/XLON, DOWNSTREAM/XETR. The subtype is a logical name, you can use any string as long as other components are aware of the nomenclature used to be able to find the component. There couple be multiple subtypes so one component can be found in multiple ways.
 * Paused/Resumed - A component implments IPausable meaning it has a paused state flag that can be invoked and checked independently of "normal" state.
 * ComponentState - a defined lifecycle state model that a component migrates through
 * Listener - zero or more possible listeners that would receive state change notifications
 * Dynamic - dynamic components are components of type TASK that are manufactured via an IDynamicComponentFactory at run time, normally in response to an incoming message. For example, an algorithimic trading strategy is a dynamic component, created by an IDynamicComponentFactory when it is shown the INewOrderSingle message.
 * 
 * Components have a few simple life cycle methods:
 * Constructor
 * initialise - one time initialisation
 * start - perform further initialisation, that may be repeated after a stop
 * stop - can call start after
 * You can extend AbstractComponent to hide all the above implementation detail. 
 * 
 * @author Oak Tree Designs Ltd.
 *
 */
public interface IComponent extends INameable, IBasicLifecycle, IPauseable, IComponentListener, IMessageListener, IMessageSender, IDispatchable  {

	/**
	 * Set if this component was constructed dynamically.
	 * @param dynamic
	 */
	public void setDynamic(boolean dynamic);

	/**
	 * Was this component dynamically constructed?
	 * @param b
	 */
	
	public boolean isDynamic();

	/**
	 * Set the ranking priority of this component in a system.
	 * @param priority
	 */
	public void setPriority(int priority);
	
	/**
	 * Get the ranking priority of this component in a system.
	 * Defaults to 0, primary.
	 * @return
	 */
	public int getPriority();
	
	/**
	 * Set the entity we can send messages to.
	 * @param listener
	 */
	public void setMessageListener(IMessageListener listener);
	
	/**
	 * Return the current state of this component.
	 * @return
	 */
	public ComponentState getState();
	
	/**
	 * Set the current state of this component.
	 * @param state
	 */
	public void setState(ComponentState state);
	
	/**
	 * Add a listener to this components state changes.
	 * @param listener
	 */
	public void addComponentListener(IComponentListener listener);
	
	/**
	 * Remove a listener to this component state changes.
	 * @param listener
	 */
	public void removeComponentListener(IComponentListener listener);
	
	/**
	 * Get the component type of this component.
	 * @return
	 */
	public ComponentType getComponentType();
	
	/**
	 * Get the sub type of this component.
	 * @return
	 */
	public String getComponentSubType();
	
	/**
	 * Set the components type and subtype.
	 * @param type
	 * @param subtype
	 */
	public void setComponentType(ComponentType type, String subtype);
	
	/**
	 * Set a max time to wait for startup; 0 means, dont worry about startup times.
	 * @param mandatory
	 */
	public void setStartupTimeout(long maxmillis);
	
	/**
	 * Get timeouts for components that are mandatory. Will be 0 if timeout is inconsequential.
	 */
	public long getStartupTimeout();

	/**
	 * Handle an unexpected exception encountered when a message was attempted to be given to this component.
	 * @param message
	 * @param e
	 */
	public void onUnexpectedException(IMessage message, Exception e);

	/**
	 * Set just the component type. Dangerous to do after creation and registration as no notification
	 * of a change of component type will be given to any manager or other interested party.
	 * @param container
	 */
	public void setComponentType(ComponentType container);
	
	/**
	 * Set just the component sub type. Dangerous to do after creation and registration as no notification
	 * of a change of component type will be given to any manager or other interested party.
	 * @param subtype
	 */
	public void setComponentSubType(String subtype);
	
	/**
	 * It may be required that this component is known as more than one subtype - supply
	 * any additional names as a string array.
	 * 
	 * @param subtypes
	 */
	public void setAlternativeSubTypes(String[] subtypes);
	
	/**
	 * Get a string array of possible names this component is also known as (aliases).
	 * @return
	 */
	public String[] getAlternativeSubTypes();

}
