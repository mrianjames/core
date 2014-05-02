package com.oaktree.core.container;

import com.oaktree.core.container.examples.ExampleDynamicComponentFactory;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.threading.dispatcher.mapped.MappedDispatcher;
import com.oaktree.core.threading.policy.ComponentNameThreadingPolicy;
import com.oaktree.core.threading.policy.IThreadingPolicy;

/**
 * A ping pong component to component test via the container
 */
public class TestContainerPerformance {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int TESTS = 1000000;
		int threads = 2;
		boolean mapped = true;
		
		if (args.length > 0) {
			TESTS = Integer.valueOf(args[0]);
		}
		if (args.length > 1) {
			threads = Integer.valueOf(args[1]);
		}
		if (args.length > 2) {
			mapped = Boolean.parseBoolean(args[2]);
		}
		
		
		IComponentManager manager = new ComponentManager();
		manager.setComponentType(ComponentType.SERVICE, "ComponentManager");
		manager.setName("ComponentManager");
		manager.setWaitBetweenStartupTypes(false);
		
		IContainer con = new Container();
		con.setComponentManager(manager);
		manager.setMessageListener(con);
		con.setName("TestContainer");
		con.setComponentType(ComponentType.CONTAINER, "");

		
		IDynamicComponentFactory dcFactory = new ExampleDynamicComponentFactory();
		manager.setDynamicComponentFactory(dcFactory);
		
		
		IDispatcher dispatcher = mapped ? new MappedDispatcher() : new ThroughputDispatcher();
		dispatcher.setName("ThroughputDispatcher");
		dispatcher.setThreads(threads);
		dispatcher.setKeys(new String[]{"UPSTREAM","TEST"});
		dispatcher.setCanExpand(false);
		dispatcher.start();
		con.setDispatcher(dispatcher);
		

		IComponent upstream = new PingUpstream(TESTS);
        IThreadingPolicy utp = new ComponentNameThreadingPolicy(upstream);

        upstream.setName("UPSTREAM");
		upstream.setComponentType(ComponentType.UPSTREAM, "TEST");
		upstream.setMessageListener(con);
		upstream.setThreadingPolicy(utp);
		
		IComponent downstream = new PingDownstream("UPSTREAM");
        IThreadingPolicy dtp = new ComponentNameThreadingPolicy(downstream);
		downstream.setName("TEST");
		downstream.setComponentType(ComponentType.DOWNSTREAM, "TEST");
		downstream.setMessageListener(con);
		downstream.setThreadingPolicy(dtp);
		
		con.getComponentManager().addComponent(downstream);
		con.getComponentManager().addComponent(upstream);
		
		con.initialise();
		con.start();
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
