package com.oaktree.core.http;

import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.gc.GCService;
import com.oaktree.core.logging.ILogger;
import com.oaktree.core.logging.Level;
import com.oaktree.core.logging.LowLatencyLogger;
import com.oaktree.core.process.MemoryService;
import com.oaktree.core.threading.dispatcher.Monitoring.DispatcherMonitor;
import com.oaktree.core.threading.dispatcher.Monitoring.DispatcherService;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import com.oaktree.core.time.MultiTimeScheduler;

public class ProcessHttpServerTest {
	private static final Logger logger = LoggerFactory.getLogger(ProcessHttpServerTest.class);
	public static void main(String[] args) {
		ThroughputDispatcher dispatcher = new ThroughputDispatcher("TEST",2);
		dispatcher.initialise();dispatcher.start();
		MultiTimeScheduler scheduler = new MultiTimeScheduler(dispatcher);
		scheduler.initialise();scheduler.start();
		DispatcherService dispservice = new DispatcherService("dispService");
		DispatcherMonitor dispmon = new DispatcherMonitor("DW", dispatcher, scheduler, 60000);
		dispmon.setDispatchListener(dispservice);
		dispmon.initialise();dispmon.start();
		MemoryService ms = new MemoryService(60000);
		ms.setScheduler(scheduler);
		ms.initialise();
		ms.start();
		
		GCService s = new GCService("gc.service",scheduler);
		s.initialise();s.start();
		ProcessHttpServer server = new ProcessHttpServer(1234);
		server.addService("gc", s);
		server.addService("memory",ms);
		server.addService("dispatcher",dispservice);
		server.start();
		
		((LowLatencyLogger)logger).setLevel(Level.OFF);
		int i = 0;
        while (true) {
        	logger.info("Making random garbage..."+i);
            i++;
            LockSupport.parkNanos(100000);
        }
		
	}
}
