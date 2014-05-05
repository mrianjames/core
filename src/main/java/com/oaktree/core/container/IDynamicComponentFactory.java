package com.oaktree.core.container;

/**
 * A factory of components we create on the fly.
 * @author Oak Tree Designs Ltd
 *
 */
public interface IDynamicComponentFactory extends IComponent {

	/**
	 * Make a dynamic component(s) from an incoming message.
         * Normally you resolve one dynamic object; however you
         * may in certain circumstances decide to make multiple objects.
	 * @param message
	 * @return
	 */
	public IDynamicComponent[] resolve(IMessage message);
}
