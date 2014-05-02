package com.oaktree.core.container;

import java.util.List;

/**
 * A filter of a list of components to come up with one option. For example, round robin, random, primary/secondary.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public interface IComponentFilter {

	/**
	 * Select one component from a collection of possible collections.
	 * @param possibles
	 * @return
	 */
	public IComponent filter(List<IComponent> possibles);

}
