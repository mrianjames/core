package com.oaktree.core.id;

import com.oaktree.core.container.INameable;

/**
 * Definition of a generator of ids. The generator can make unique ids for a component (set "name") for a given
 * subject
 * @author Oak Tree Designs Ltd
 *
 */
public interface IIdGenerator extends INameable {

	/**
	 * Get the subject this generator is making ids for.
	 * @return
	 */
	public String getSubject();
	/**
	 * Set the subject this generator will make for.
	 * @param subject
	 */
	public void setSubject(String subject);

	/**
	 * Generate the next id.
	 * @return
	 */
	public String next();
	
	/**
	 * Generate the next id for a subject.
	 * @param subject
	 * @return
	 */
	public String next(String subject);
	
	/**
	 * Set the start id number for recovery.
	 * @param start
	 */
	public void setStart(long start);
	
	/**
	 * any initialisation required?
	 */
	public void initialise() ;
	
}
