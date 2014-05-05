package com.oaktree.core.container.examples;

import java.util.logging.Level;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentManager;
import com.oaktree.core.container.ComponentType;
import com.oaktree.core.container.Container;
import com.oaktree.core.container.FilteredMessageComponent;
import com.oaktree.core.container.IComponent;
import com.oaktree.core.container.IComponentManager;
import com.oaktree.core.container.IContainer;
import com.oaktree.core.container.IDynamicComponentFactory;
import com.oaktree.core.container.IFilteredMessageListener;
import com.oaktree.core.container.RankingComponentFilter;
import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;

public class ExampleContainer {
	/**
	 * A TEST container.
	 * @param args
	 */
	public static void main(String[] args) {
		
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
		
		IDispatcher dispatcher = new ThroughputDispatcher();
		dispatcher.setName("ThroughputDispatcher");
		dispatcher.setThreads(2);
		dispatcher.start();
		con.setDispatcher(dispatcher);
		
		IComponent upstream = new ExampleUpstream();
		upstream.setName("FIX42");
		upstream.setComponentType(ComponentType.UPSTREAM, "FIX42");
		upstream.setMessageListener(con);
		
		IComponent downstream = new ExampleDownstream();
		downstream.setName("XLON_DownstreamPrimary");
		downstream.setComponentType(ComponentType.DOWNSTREAM, "XLON");
		downstream.setMessageListener(con);
		downstream.setPriority(AbstractComponent.SECONDARY);
		
		IComponent downstream2 = new ExampleDownstream();
		downstream2.setName("XLON_DownstreamBackup");
		downstream2.setComponentType(ComponentType.DOWNSTREAM, "XLON");
		downstream2.setMessageListener(con);
		downstream2.setPriority(AbstractComponent.PRIMARY);
		
		//con.setComponentFilter(new RandomComponentFilter());
		//con.setComponentFilter(new RoundRobinComponentFilter());
		con.setComponentFilter(new RankingComponentFilter());
		
		IFilteredMessageListener logger = new FilteredMessageComponent();
		logger.setFilterLevel(Level.FINEST.intValue());
		logger.setName("Logger");
		//logger.setPublicationListener(new SystemOutLogger());
		//logger.ignoreMessagesFrom("FIX42");
		logger.ignoreMessagesTo(ComponentType.DOWNSTREAM.name(),null);
		logger.setComponentType(ComponentType.LOGGER, "logger");
		con.addMessageProcessor(logger);
		
		con.getComponentManager().addComponent(upstream);
		con.getComponentManager().addComponent(downstream);
		con.getComponentManager().addComponent(downstream2);
		con.getComponentManager().addComponent(logger);
		
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
