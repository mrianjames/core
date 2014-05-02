package com.oaktree.core.latency;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.oaktree.core.time.ITime;
import com.oaktree.core.time.JavaTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.logging.Log;

/**
 * Low latency latency recorder; takes details of timing, puts on background thread and periodically
 * flushes to an output stream.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class LatencyRecorder extends AbstractComponent implements ILatencyRecorder {

    private final static Logger logger = LoggerFactory.getLogger(LatencyRecorder.class.getName());
    private ILatencyWriter writer;
    private ITime timeSystem = new JavaTime();
    
    public LatencyRecorder() {}
    public LatencyRecorder(ILatencyWriter writer, int bufferSize) {
    	this.bufferSize = bufferSize;
    	this.writer = writer;
    }
    public LatencyRecorder(ILatencyWriter writer, int bufferSize, int writerDelay) {
    	this.bufferSize = bufferSize;
    	this.writer = writer;
    	this.writeDelay = writerDelay;
    }

    public void setWriter(ILatencyWriter writer) {
        this.writer = writer;
    }
    private int bufferSize = 100000;
    private final AtomicInteger currentWritePos = new AtomicInteger(0);
    private final AtomicInteger currentReadPos = new AtomicInteger(0);
    private boolean pauseIfMaxBufferExceeded = true;
    private boolean[] begin = new boolean[]{};
    private String[] type = new String[]{};
    private String[] subtype = new String[]{};
    private String[] id = new String[]{};
    private long[] subid = new long[]{};
    private long[] time = new long[]{};

    /**
     * Record our data into the array; if we run out of room we will pause the recorder and future
     * records will return straight away.
     * @param begin
     * @param type
     * @param subtype
     * @param id
     * @param subid
     * @param time
     */
    private void record(boolean begin, String type, String subtype, String id, long subid, long time) {
        try {
            if (!this.isActive()) {
                return;
            }
            int writeSlot, nextWriteSlot, lastReadPos = this.currentReadPos.get();
            do {
                writeSlot = this.currentWritePos.get();
                nextWriteSlot = (writeSlot + 1) % this.bufferSize;
                if (nextWriteSlot == lastReadPos) {
                    /*
                     * ignore this record. actually, turn ourselves off if we are set that way
                     */
                    if (pauseIfMaxBufferExceeded) {
                        this.pause();
                    }
                    return;
                }
            } while (!this.currentWritePos.compareAndSet(writeSlot, nextWriteSlot));
            this.begin[writeSlot] = begin;
            this.type[writeSlot] = type;
            this.subtype[writeSlot] = subtype;
            this.id[writeSlot] = id;
            this.subid[writeSlot] = subid;
            this.time[writeSlot] = time;

        } catch (Exception e) {
            Log.exception(logger, e);
        }
    }

    public boolean isActive() {
        return (!this.isPaused() && this.getState().isAvailable());
    }

    @Override
    public void begin(String type, String subtype, String id, long subid) {
        this.record(true, type, subtype, id, subid, timeSystem.getNanoTime());
    }

    @Override
    public void beginAt(String type, String subtype, String id, long subid,
            long time) {
        this.record(true, type, subtype, id, subid, time);
    }

    @Override
    public void end(String type, String subtype, String id, long subid) {
        this.record(false, type, subtype, id, subid, timeSystem.getNanoTime());
    }

    @Override
    public void endAt(String type, String subtype, String id, long subid, long time) {
        this.record(false, type, subtype, id, subid, time);
    }

    public void stop() {
        if (this.writer != null) {
            try {
                this.writer.stop();
            } catch (Exception e) {
                Log.exception(logger, e);
            }
        }

        this.timer.cancel();
        this.timer = null;
        super.stop();
    }
    private Timer timer;
    private long writeDelay = 300;

    public void pause() {
        logger.warn(this.getName() + " is pausing");
        super.pause();
    }

    public void resume() {
        logger.warn(this.getName() + " is resuming");
        super.resume();        
    }
    private final static String BEGIN = "BEG";
    private final static String END = "END";

    @Override
    public void initialise() {
        begin = new boolean[this.bufferSize];
        type = new String[this.bufferSize];
        subtype = new String[this.bufferSize];
        id = new String[this.bufferSize];
        subid = new long[this.bufferSize];
        time = new long[this.bufferSize];

        super.initialise();
    }
    //final StringBuilder flushBuffer = new StringBuilder(50 * this.bufferSize);

    @Override
    public void start() {
    	if (this.writer == null) {
    		throw new IllegalStateException("No writer configured for writerlatencyrecorder.");
    	}
        if (logger.isInfoEnabled()) {
            logger.info("Starting latency recorder " + this.getName() + " with buffer size of " + this.bufferSize + " and writer of " + (this.writer != null ? this.writer.getClass().getName() : " nothing! Flushing every " + this.writeDelay + " ms."));
        }
                TimerTask writerTask = new TimerTask() {

            @Override
            public void run() {
                LatencyRecorder.this.run();
            }
        };
        if (this.timer == null) {
            this.timer = new Timer();
        }
        this.timer.schedule(writerTask, writeDelay, writeDelay);
        super.start();
        this.setState(ComponentState.AVAILABLE);
        
    }

    protected void run() {
        int from = LatencyRecorder.this.currentReadPos.get();
        
        int to = LatencyRecorder.this.currentWritePos.get();
        //for (int i = from; i < to; i++) {
        while (from != to) {
            writer.add(LatencyRecorder.this.begin[from] ? BEGIN : END,
            		LatencyRecorder.this.type[from],
            		LatencyRecorder.this.subtype[from],
            		LatencyRecorder.this.id[from],
            		LatencyRecorder.this.subid[from],
            		LatencyRecorder.this.time[from]
            		);            
            from = (from + 1) % this.bufferSize;
        }

        try {
            LatencyRecorder.this.writer.flush();
//			/*
//			 * wrap around.
//			 */
//			if (to == WriterLatencyRecorder.this.bufferSize) {
//				to = 0;
//			}
            LatencyRecorder.this.currentReadPos.set(to);

        } catch (Exception e) {
            Log.exception(logger, e);
        }
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void setBufferSize(int buffersize) {
        this.bufferSize = buffersize;
    }

    public boolean isPauseIfMaxBufferExceeded() {
        return pauseIfMaxBufferExceeded;
    }

    public void setPauseIfMaxBufferExceeded(boolean pauseIfMaxBufferExceeded) {
        this.pauseIfMaxBufferExceeded = pauseIfMaxBufferExceeded;
    }

    public long getWriteDelay() {
        return writeDelay;
    }

    public void setWriteDelay(long writeDelay) {
        this.writeDelay = writeDelay;
    }
}
