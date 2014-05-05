package com.oaktree.core.syslog.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
<<<<<<< HEAD
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
=======
import java.nio.channels.DatagramChannel;
>>>>>>> 426dd0eb3474e0dd4b23ed9d039174b68fb9fbba
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public class UdpChannelSyslogRecordSource extends
		AbstractChannelSyslogRecordSource {

	private DatagramChannel c;


	public UdpChannelSyslogRecordSource(String name, int port) {
		super(name, port);
	}

	@Override
	protected SelectableChannel makeChannel() {
		try {
			
			this.c = DatagramChannel.open();
		    DatagramSocket datagramSocket = c.socket();
		    datagramSocket.setReuseAddress(true);
		    datagramSocket.setBroadcast(true);
		    //todo interface
		    InetSocketAddress add = new InetSocketAddress("127.0.0.1",getPort());
<<<<<<< HEAD
            c.bind(add);

		    //c.connect(add);
=======
		    c.connect(add);
>>>>>>> 426dd0eb3474e0dd4b23ed9d039174b68fb9fbba
		    //datagramSocket.bind(add);
		    //datagramSocket.connect(address);
		    c.configureBlocking(false);

		    
		    //OP_ACCEPT
			return c;
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Cannot bind udp server on "+getPort(),e);
		}
		return null;
	}

	@Override
	protected int getKeys() {
		return SelectionKey.OP_READ;
	}


<<<<<<< HEAD
    protected void read() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        (c).receive(buffer);
        buffer.flip();
        CharBuffer cb = getDecoder().decode(buffer);
        System.out.println(cb.toString());
    }
=======

>>>>>>> 426dd0eb3474e0dd4b23ed9d039174b68fb9fbba

}
