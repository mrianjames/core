package com.oaktree.core.syslog.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class TcpChannelSyslogRecordSource extends
		AbstractChannelSyslogRecordSource {

	private ServerSocketChannel c;


	public TcpChannelSyslogRecordSource(String name, int port) {
		super(name, port);
	}

	@Override
	protected SelectableChannel makeChannel() {
		try {
			
			this.c = ServerSocketChannel.open();
		    ServerSocket socket = c.socket();
		    socket.setReuseAddress(true);
		    //todo interface
		    InetSocketAddress add = new InetSocketAddress("127.0.0.1",getPort());
		    socket.bind(add);
		    
		    c.configureBlocking(false);

		    
		    //OP_ACCEPT
			return c;
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Cannot bind tcp server on "+getPort(),e);
		}
		return null;
	}

	@Override
	protected int getKeys() {
		return SelectionKey.OP_ACCEPT;
	}




}
