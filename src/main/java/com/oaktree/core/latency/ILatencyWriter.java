package com.oaktree.core.latency;

/**
 * Specification of a writer of latency records.
 * 
 * @author ij
 *
 */
public interface ILatencyWriter {
	/**
	 * Add the record to a possible buffer - note that an implementation may flush this to its
	 * desired medium without an explicit flush.
	 * @param phase
	 * @param type
	 * @param subtype
	 * @param id
	 * @param subid
	 * @param time
	 */
	public void add(String phase,String type, String subtype, String id, long subid, long time);
	/**
	 * Force a flush to the medium.
	 */
	public void flush();
	/**
	 * Clear a possible write buffer
	 */
	public void clear();
	
	/**
	 * Start the writer. Implementations will open a connection to their medium and flush a header.
	 */
	public void start();
	
	/**
	 * Stop the writer. Implementations could flush and stop any connections.
	 */
	public void stop();
}
