package com.oaktree.core.gc;

import java.util.Collection;

import com.oaktree.core.container.IComponent;

/**
 * Service that exposes GC information.
 * 
 * @author ij
 *
 */
public interface IGCService extends IComponent {
	public Collection<GC> getAllGCEvents();	
	public Collection<GC> getAllGCEvents(String type);
	public Collection<String> getGCEventTypes();
	public Collection<GC> getAllGCEventsBetween(long start, long end);
	public Collection<GC> getAllGCEventsBetween(String type,long start, long end);
}
