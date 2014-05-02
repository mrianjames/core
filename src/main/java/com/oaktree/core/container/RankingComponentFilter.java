package com.oaktree.core.container;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implement prioritised picking of components based on a ranking qualifier.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class RankingComponentFilter implements IComponentFilter {
	
	private Comparator<IComponent> comp = new Comparator<IComponent>() {

		@Override
		public int compare(IComponent comp1,IComponent comp2) {
			return comp1.getPriority()-comp2.getPriority();
		}
		
	};
	
	@Override
	public IComponent filter(List<IComponent> possibles) {
		if (possibles.size() == 0) {
			return null;
		}
		if (possibles.size() == 1) {
			return possibles.get(0);
		}
		Collections.sort(possibles,comp);
		return possibles.get(0);
	}

}
