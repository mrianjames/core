package com.oaktree.core.latency;

/**
 * A maker of latency recorders.
 * @author Oak Tree Designs Ltd
 *
 */
public interface ILatencyRecorderFactory {
	
	/**
	 * Make a named latency recorder.
	 * @param name
	 * @return
	 */
	public ILatencyRecorder make(String name);
}
