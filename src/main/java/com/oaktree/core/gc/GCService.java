package com.oaktree.core.gc;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.container.ComponentType;

/**
 * This class receives GC update events (JDK 7 onwards) and stores them
 * for later use by other components. 
 * 
 * @author ij
 *
 */
public class GCService extends AbstractComponent implements IGCService {
	
	private final static Logger logger = LoggerFactory.getLogger(GCService.class);
	
	public GCService(String name) {
		this.setName(name);
		this.setComponentType(ComponentType.SERVICE);
		this.setComponentSubType("GCService");
	}
	
	private List<GC> allEvents = new CopyOnWriteArrayList<GC>();
	
	@Override
	public void start() {
		super.start();
		this.registerForJmxUpdates();
		this.setState(ComponentState.AVAILABLE);
	}
	
	private void registerForJmxUpdates() {
		checkJavaVersionSupportsJmxUpdates();
		//TODO regi
	}

	private void checkJavaVersionSupportsJmxUpdates() {
		String specv = ManagementFactory.getRuntimeMXBean().getSpecVersion();
		int jv = Integer.valueOf(specv.substring(specv.indexOf(".")+1));
		if (jv < 7) {
			logger.warn("NO GC INFORMATION WILL BE AVAILABLE DUE TO LOW JAVA VERSION. UPGRADE to Java 1.7");
		}
	}
	
	public static void main(String[] args) {
		GCService gcs = new GCService("test");
		gcs.initialise();gcs.start();
	}

	@Override
	public Collection<GC> getAllGCEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<GC> getAllGCEvents(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getGCEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<GC> getAllGCEventsBetween(long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<GC> getAllGCEventsBetween(String type, long start,
			long end) {
		// TODO Auto-generated method stub
		return null;
	}

}
