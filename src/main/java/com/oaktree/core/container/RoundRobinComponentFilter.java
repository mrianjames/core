package com.oaktree.core.container;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinComponentFilter implements IComponentFilter {

	private Map<ComponentType,Map<String,AtomicInteger>> state = new ConcurrentHashMap<ComponentType, Map<String,AtomicInteger>>();
	
	@Override
	public IComponent filter(List<IComponent> possibles) {
		if (possibles.size() == 0) {
			return null;
		}
		if (possibles.size() == 1) {
			return possibles.get(0);
		}
		ComponentType t = possibles.get(0).getComponentType();
		String subtype = possibles.get(0).getComponentSubType();
		
		Map<String,AtomicInteger> m = state.get(t);
		if (m == null) {
			m = new ConcurrentHashMap<String,AtomicInteger>();
			state.put(t, m);
		}
		
		AtomicInteger i = m.get(subtype);
		if (i == null) {
			i = new AtomicInteger(-1);
			m.put(subtype, i);
		}
		int index = i.incrementAndGet();
		if (index == possibles.size()) {
			i.set(0);
			index = 0;
		}
		return possibles.get(index);
	}

}
