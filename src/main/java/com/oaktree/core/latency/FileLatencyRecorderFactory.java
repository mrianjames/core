package com.oaktree.core.latency;

import java.io.FileWriter;

/**
 * Maker of latency recorders that write to file in a specified directory.
 * @author Oak Tree Designs Ltd
 *
 */
public class FileLatencyRecorderFactory implements ILatencyRecorderFactory {

    private String base = "";
    private String seperator = "//";
    private int bufferSize = 200000;
    private long writeDelay = 300;
    private boolean pauseIfMaxBufferExceeded;
    private FileWriter writer;

    public void setBaseDirectory(String dir) {
        this.base = dir;
    }
   
    public FileLatencyRecorderFactory() {
    }

    public void initialise() {
        String filename = base + seperator + "_latency.csv";
        try {
            this.writer = new FileWriter(filename);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public ILatencyRecorder make(String name) {
    	ILatencyWriter wr = new IOLatencyWriter(writer, bufferSize);
        LatencyRecorder recorder = new LatencyRecorder();
        recorder.setWriter(wr);
        recorder.setName(name);
        recorder.setPauseIfMaxBufferExceeded(this.pauseIfMaxBufferExceeded);
        recorder.setBufferSize(this.bufferSize);
        recorder.setWriteDelay(this.writeDelay);
        recorder.initialise();
        recorder.start();
        return recorder;

    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getSeperator() {
        return seperator;
    }

    public void setSeperator(String seperator) {
        this.seperator = seperator;
    }

    public boolean isPauseIfMaxBufferExceeded() {
        return pauseIfMaxBufferExceeded;
    }

    public void setPauseIfMaxBufferExceeded(boolean pauseIfMaxBufferExceeded) {
        this.pauseIfMaxBufferExceeded = pauseIfMaxBufferExceeded;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public long getWriteDelay() {
        return writeDelay;
    }

    public void setWriteDelay(long writeDelay) {
        this.writeDelay = writeDelay;
    }
}
