package com.oaktree.core.id;

import java.util.concurrent.atomic.AtomicLong;

import com.oaktree.core.utils.Text;

/**
 * A very simple id generator for making system or process wide unique ids. It
 * uses SystemCurrentTimeMillis as a base and increments it. This may be
 * adjusted in recovery situations to take a specific start base.
 * 
 * Multiple generators can exist in the system for different subjects. Name the
 * dispatcher unique accross processes to ensure system-wide uniqueness. Ids
 * will be unique accross days as well due to the time/date element.
 * 
 * Id will be of the following format (generator name "PROC1", subject "DATA"):
 * PROC1_DATA_123453455
 * 
 * The shorter the text for name and data (if required at all), the better the
 * performance.
 * 
 * 
 * @author OakTreeDesignsLtd.
 * 
 */
public final class IdGenerator implements IIdGenerator {

	/**
	 * Set the subject of this generator.
	 */
	private String subject = "";

	/**
	 * Name of the id generator.
	 */
	private String name;

	private String seperator = Text.UNDERSCORE;

	/**
	 * Make an idgenerator. Will default first id to System.currentTimeMillis.
	 */
	public IdGenerator() {
		this.current = new AtomicLong(System.currentTimeMillis());
		this.prelim = "ID" + this.seperator + subject + this.seperator;
	}

	/**
	 * Create an idgenerator with a name and subject, relying on
	 * the default initial and seperator values. 
	 * @param name
	 * @param subject
	 */
	public IdGenerator(String name, String subject) {
		this.setName(name);
		this.setSubject(subject);
		this.current = new AtomicLong(System.currentTimeMillis());
		this.prelim = this.getName() + this.seperator + subject + this.seperator;
	}

	/**
	 * The most popular constructor as you pass it all information upfront
	 * 
	 * @param name
	 * @param subject
	 * @param seperator
	 * @param initial
	 */
	public IdGenerator(String name, String subject, String seperator, long initial) {
		this.setName(name);
		this.setSubject(subject);
		this.seperator = seperator;
		this.current = new AtomicLong(initial);
		//for next() method we create the string upfront
		this.prelim = this.getName() + this.seperator + subject + this.seperator;		
	}
	
	@Override
	public void initialise() {
		this.prelim = this.getName() + this.seperator + subject + this.seperator;
	}
	
	/**
	 * Current unique part of the id.
	 */
	private AtomicLong current = new AtomicLong(0);

	/**
	 * A premade string of name, subject etc. Use next() to use
	 * this optimised string, or next(subject) to rebuild the 
	 * string every time.
	 */
	private String prelim = "";

	@Override
	public String getSubject() {
		return this.subject;
	}

	@Override
	public String next() {
		//return this.next(this.subject);
		return this.prelim + this.current.incrementAndGet();
	}

	@Override
	public void setStart(long start) {
		this.current = new AtomicLong(start);
	}

	@Override
	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String next(String subject) {
		return this.getName() + this.seperator + subject + this.seperator + this.current.incrementAndGet();
	}
}
