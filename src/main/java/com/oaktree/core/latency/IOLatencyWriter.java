package com.oaktree.core.latency;

import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.logging.Log;
import com.oaktree.core.utils.Text;

/**
 * Write latency records to a standard java.io.Writer implementation.
 * 
 * @author ij
 *
 */
public class IOLatencyWriter implements ILatencyWriter {

	private final static Logger logger = LoggerFactory.getLogger(IOLatencyWriter.class.getName());
	private StringBuilder buffer;
	private Writer writer;
	
	/**
	 * 
	 * @param writer
	 * @param bufferSize
	 */
	public IOLatencyWriter(Writer writer, int bufferSize) {
		this.writer = writer;
		this.buffer = new StringBuilder(50 * bufferSize);
	}
	
	@Override
	public void add(String phase, String type, String subtype, String id,
			long subid, long time) {
		buffer.append(time);
        buffer.append(Text.COMMA);
        buffer.append(phase);
        buffer.append(Text.COMMA);
        buffer.append(type);
        buffer.append(Text.COMMA);
        buffer.append(subtype);
        buffer.append(Text.COMMA);
        buffer.append(id);
        buffer.append(Text.COMMA);
        buffer.append(subid);
        buffer.append(Text.COMMA);
        buffer.append(0);//additional unused id.
        buffer.append(Text.NEW_LINE);

	}

	@Override
	public void flush() {
		try {
			writer.write(buffer.toString());
			writer.flush();
			buffer.delete(0, buffer.length());
		} catch (Exception e) {
			Log.exception(logger,e);
		}
	}

	@Override
	public void clear() {
		this.buffer.delete(0, buffer.length());
	}

	@Override
	public void start() {
		final StringBuilder headerBuffer = new StringBuilder();
        headerBuffer.append("Time");
        headerBuffer.append(Text.COMMA);
        headerBuffer.append("Section");
        headerBuffer.append(Text.COMMA);
        headerBuffer.append("Subsection");
        headerBuffer.append(Text.COMMA);
        headerBuffer.append("ID");
        headerBuffer.append(Text.COMMA);
        headerBuffer.append("SubID");
        headerBuffer.append(Text.COMMA);
        headerBuffer.append("NumericSubID");
        headerBuffer.append(Text.COMMA);
        headerBuffer.append(0);//additional unused id.
        headerBuffer.append(Text.NEW_LINE);
        try {
        	if (this.writer != null) {
        		this.writer.write(headerBuffer.toString());
        	}
        } catch (Exception e) {
            Log.exception(logger, e);
        }
	}

	@Override
	public void stop() {
		try {
			this.flush();
			this.writer.close();
		} catch (Exception e) {
			Log.exception(logger,e);
		}
	}

}

