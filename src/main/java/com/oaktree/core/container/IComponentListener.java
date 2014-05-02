package com.oaktree.core.container;

/**
 * A listener of changes to component state.
 * 
 * @author Oak Tree Designs Ltd.
 *
 */
public interface IComponentListener {
	
	/**
	 * Notification that a components state has changed.
	 * @param component
	 * @param oldState
	 * @param newState
	 * @param changeReason
	 */
	public void onComponentStateChange(IComponent component, ComponentState oldState, ComponentState newState, String changeReason);
}
