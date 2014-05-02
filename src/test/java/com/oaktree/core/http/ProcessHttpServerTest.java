package com.oaktree.core.http;

import com.oaktree.core.process.MemoryService;
import com.oaktree.core.threading.dispatcher.Monitoring.DispatcherMonitor;
import com.oaktree.core.threading.dispatcher.Monitoring.DispatcherService;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import com.oaktree.core.time.MultiTimeScheduler;

public class ProcessHttpServerTest {
	   
	public static void main(String[] args) {
		ThroughputDispatcher dispatcher = new ThroughputDispatcher("TEST",2);
		dispatcher.initialise();dispatcher.start();
		MultiTimeScheduler scheduler = new MultiTimeScheduler(dispatcher);
		scheduler.initialise();scheduler.start();
		DispatcherService dispservice = new DispatcherService("dispService");
		DispatcherMonitor dispmon = new DispatcherMonitor("DW", dispatcher, scheduler, 5000);
		dispmon.setDispatchListener(dispservice);
		dispmon.initialise();dispmon.start();
		MemoryService ms = new MemoryService(5000);
		ms.setScheduler(scheduler);
		ms.initialise();
		ms.start();
		
		MockService s = new MockService();
		ProcessHttpServer server = new ProcessHttpServer(1234);
		server.addService("service", s);
		server.addService("memory",ms);
		server.addService("dispatcher",dispservice);
		server.start();
	}
}
