package com.oaktree.core.binlog.write;

import com.oaktree.core.logging.Level;

/**
 * Writer of std looking logging fields. A very simple version of what you would have with 
 * slf4j.
 * 
 * @author ianjames
 *
 */
public interface ILogRecordWriter {
	/**
	 * Log a std type of set of fields (like slf4j would). 
	 * @param time - in precision of your choice.
	 * @param threadname - max 20 chars.
	 * @param threadId
	 * @param level
	 * @param message - max 128 chars.
	 */
	public void log(long time, String threadname, short threadId, Level level, String message);
}
