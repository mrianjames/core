package com.oaktree.core.latency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.logging.Log;

/**
 * A singleton latency recorder manager. This maintains recorders we wish to use for different purposes. 
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class LatencyRecorderManager {

	private final Logger logger = LoggerFactory.getLogger(LatencyRecorderManager.class.getName());
	
    /**
     * THIS.
     */
    private static final LatencyRecorderManager INSTANCE = new LatencyRecorderManager();

    private LatencyRecorderManager() {
    	try {
	        recorderFactory = new FileLatencyRecorderFactory();
	        ((FileLatencyRecorderFactory)recorderFactory).setBaseDirectory(System.getProperty("user.dir"));
	        ((FileLatencyRecorderFactory)recorderFactory).initialise();
    	} catch (Exception e){
    		Log.exception(logger, e);
    	}
    }

    public static LatencyRecorderManager getInstance() {
        return INSTANCE;
    }
    private ILatencyRecorderFactory recorderFactory;
    private Map<String, ILatencyRecorder> recorders = new ConcurrentHashMap<String, ILatencyRecorder>();

    public void registerLatencyRecorder(ILatencyRecorder recorder) {
        this.recorders.put(recorder.getName(), recorder);
    }

    /**
     * Get (and make if not one existing) a latency recorder.
     * @param name
     * @return
     */
    public ILatencyRecorder getLatencyRecorder(String name) {
        ILatencyRecorder recorder = this.recorders.get(name);
        if (recorder == null) {
            if (this.recorderFactory != null) {
                synchronized (this) {
                    /*
                     * try again now noone else can make one at the same time.
                     */
                    recorder = this.recorders.get(name);
                    if (recorder == null) {
                        recorder = this.recorderFactory.make(name);
                        this.recorders.put(name, recorder);
                    }
                }
            } else {
                throw new IllegalStateException("No recorders or any way to make a recorder.");
            }
        }
        return recorder;
    }

    public void start() {
        for (ILatencyRecorder recorder : recorders.values()) {
            recorder.start();
        }
    }

    public void stop() {
        for (ILatencyRecorder recorder : recorders.values()) {
            recorder.stop();
        }
    }

    public void pause() {
        for (ILatencyRecorder recorder : recorders.values()) {
            recorder.pause();
        }
    }

    public void resume() {
        for (ILatencyRecorder recorder : recorders.values()) {
            recorder.resume();
        }
    }

    public void pause(String recorder) {
        ILatencyRecorder rec = this.recorders.get(recorder);
        if (rec != null) {
            rec.pause();
        }
    }

    public boolean isActive(String recorder) {
        ILatencyRecorder rec = this.recorders.get(recorder);
        if (rec != null) {
            return rec.isActive();
        }
        return false;
    }

    public void resume(String recorder) {
        ILatencyRecorder rec = this.recorders.get(recorder);
        if (rec != null) {
            rec.resume();
        }
    }

    public void setLatencyRecorderFactory(FileLatencyRecorderFactory lrf) {
        this.recorderFactory = lrf;
    }
}
