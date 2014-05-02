package com.oaktree.core.container;

/**
 * Flavour of a component. This is used to group components into logical bundles.
 *  
 * NAMED - this tells a router that the component you wish to target is named explicitly.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public enum ComponentType {
	UPSTREAM,DOWNSTREAM,SERVICE,STRATEGY_FACTORY,TASK, DYNAMIC_COMPONENT_MANAGER, CONTAINER, NAMED, DISPATCHER, LOGGER
}
