package com.oaktree.core.syslog.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.AbstractComponent;

public class SyslogServer extends AbstractComponent {
	private final static Logger logger = LoggerFactory.getLogger(SyslogServer.class);
	
	private List<ISysLogRecordSource> sources = new ArrayList<ISysLogRecordSource>();

	public SyslogServer() {
	}
	public void addSysLogRecordSource(ISysLogRecordSource src) {
		sources.add(src);
	}
	
	@Override
	public void initialise() {
		logger.info("Initialising server "+toString());
		for (ISysLogRecordSource src:sources) {
			src.initialise();
		}
	}
	
	@Override
	public void start() {
		logger.info("Starting server "+toString());
		for (ISysLogRecordSource src:sources) {
			src.start();
		}
	}

}
