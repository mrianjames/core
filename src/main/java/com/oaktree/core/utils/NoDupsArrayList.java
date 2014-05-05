package com.oaktree.core.utils;

import java.util.ArrayList;
import java.util.Iterator;

public class NoDupsArrayList<E> extends ArrayList<E> {
	
	
	@Override
	public boolean add(E element) {
		if (this.size() > 0) {
			boolean dup = isDuplicate(element);
			if (dup) {
				return false;
			}
		}
		return super.add(element);
	}
	
	@Override
	public void add(int i, E element) {
		if (this.size() > 0) {
			boolean dup = isDuplicate(element);
			if (dup) {
				return;
			}
		}
		super.add(i,element);
	}

	private boolean isDuplicate(E element) {
		Iterator<E> i = this.iterator();
		while (i.hasNext()) {
			if (i.next().equals(element)) {
				return true;
			}
		}
		return false;
	}
}
