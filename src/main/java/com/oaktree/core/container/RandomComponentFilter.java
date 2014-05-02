package com.oaktree.core.container;

import java.util.List;
import java.util.Random;

/**
 * Distribute components in a random distribution.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class RandomComponentFilter implements IComponentFilter {

	@Override
	public IComponent filter(List<IComponent> possibles) {
		Random rnd = new Random();
		int i = rnd.nextInt(possibles.size());
		return possibles.get(i);
	}

}
