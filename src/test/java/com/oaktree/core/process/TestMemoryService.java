package com.oaktree.core.process;

import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import com.oaktree.core.time.MultiTimeScheduler;

public class TestMemoryService {

	public static void main(String[] args) throws Exception{
		ThroughputDispatcher dispatcher = new ThroughputDispatcher("TEST",2);
		dispatcher.initialise();dispatcher.start();
		MultiTimeScheduler scheduler = new MultiTimeScheduler(dispatcher);
		scheduler.initialise();scheduler.start();
		MemoryService ms = new MemoryService(2000);
		ms.setScheduler(scheduler);
		ms.initialise();
		ms.start();
	}

}
