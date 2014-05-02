package com.oaktree.core.utils;

import java.util.LinkedList;
import java.util.ListIterator;

@SuppressWarnings("serial")

/**
 * A data structure (linked list) that allows iteration to come go back to the start when completed.
 * Useful for testing other code that requires repetitions of entities to be used.
 * <pre>
 * CircularQueue b = new CircularQueue();
b.add("Who");
b.add("Are");
b.add("You");
ListIterator it = b.listIterator();
System.out.println(it.next());
System.out.println(it.next());
System.out.println(it.next());
System.out.println(it.next());
System.out.println(it.next());
 * </pre>
 * 
 * Output:
 * <pre>
 * 	Who
	Are
	You
	Who
	Are
 * </pre>
 */
public class CircularQueue<E> extends LinkedList<E> {

	
	private class CircularIterator implements ListIterator<E> {
		private int index = 0;
		public CircularIterator(int index) {
			this.index = index;
		}
		@Override
		public void add(E e) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean hasNext() {
			return CircularQueue.this.size() > 0;
		}
		@Override
		public boolean hasPrevious() {
			return CircularQueue.this.size() > 0;
		}
		@Override
		public E next() {
			E e =  CircularQueue.this.get(this.index);
			this.index = this.nextIndex();			
			return e;
		}
		@Override
		public int nextIndex() {
			if (this.index == CircularQueue.this.size()-1) {
				return 0;
			} 
			return this.index + 1;			
		}
		@Override
		public E previous() {
			E e =  CircularQueue.this.get(this.index);
			this.index= this.previousIndex();
			return e;
		}
		@Override
		public int previousIndex() {
			if (this.index <= 0) {
				return CircularQueue.this.size()-1;
			}
			return this.index-1;
		}
		@Override
		public void remove() {
			CircularQueue.this.remove(this.index);
		}
		@Override
		public void set(E e) {
			throw new UnsupportedOperationException();
			
		}
		
	}
    
	public ListIterator<E> listIterator(int index) {
    	return new CircularIterator(index);
    }

}
