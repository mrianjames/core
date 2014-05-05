package com.oaktree.core.syslog.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.ComponentState;

public abstract class AbstractChannelSyslogRecordSource extends AbstractSyslogRecordSource implements
		ISysLogRecordSource {
	protected static Logger logger = LoggerFactory.getLogger(AbstractChannelSyslogRecordSource.class);
	private int port;
	private SelectableChannel channel;
	public AbstractChannelSyslogRecordSource(String name,int port) {
		super(name);
		this.port = port;
	}
	public int getPort() {
		return port;
	}
	private Selector selector;
	private Charset charset = Charset.forName("us-ascii");  
	private CharsetDecoder decoder = charset.newDecoder();
	private SelectionKey readKey;  
	
	public void initialise() {
		setState(ComponentState.INITIALISING);
		this.channel = this.makeChannel();
		try {
			this.selector = SelectorProvider.provider().openSelector();
			this.readKey = channel.register(selector, getKeys());
			readKey.isAcceptable();
			
		} catch (Exception e) {
			logger.error(getName()+" Cannot setup selector",e);
			e.printStackTrace();
		}
		logger.info(getName()+" is INIT on port " + port);
		setState(ComponentState.INITIALISED);
		
	}
	protected abstract int getKeys() ;
	protected abstract SelectableChannel makeChannel();
	private Thread thread;
	// Accept connections for current time. Lazy Exception thrown.
    public void listen()  {
    	this.thread = new Thread(new Runnable(){
    		public void run() {
    			try {
    				//if (channel.isConnected()) {
    					logger.info(getName()+" Connected and waiting...");
    					while (selector.select() > 0) {
    						//logger.info(getName()+" Im in");
    						Set readyKeys = selector.selectedKeys();
    					    Iterator i = readyKeys.iterator();

    					    // Walk through the ready keys collection and process date requests.
    					    while (i.hasNext()) {
    							SelectionKey sk = (SelectionKey)i.next();
    							i.remove();
    							if (sk.isReadable()) {
    								//logger.info(getName()+" Reading...");
    								read(sk.channel());
    							}
    							if (sk.isAcceptable()) {
    								//logger.info(getName()+" Accepting");
//    								ServerSocketChannel nextReady = 
//    									    (ServerSocketChannel)sk.channel();
//    								Socket s = nextReady.accept().socket();
//    								nextReady.register(selector, SelectionKey.OP_READ);
//    								byte[] buffer = new byte[1024];
//    								while (true) {    									
//    									int read = s.getInputStream().read(buffer);
//    									if (read > 0) {
//    										System.out.println("in: "+new String(buffer,0,read).trim());
//    									}
//    									LockSupport.parkNanos(10000);
//    								}
    								//read();
    								ServerSocketChannel channel = (ServerSocketChannel)(sk.channel());
    								SocketChannel client = channel.accept();
    								client.configureBlocking(false);
    								SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);    								
    							}
    					    }
    			    	}	
    					
    				//}
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	});
    	this.thread.setName(getName()+"RcvThread");
    	this.thread.start();
    }

    private ByteBuffer buffer = ByteBuffer.allocate(1024);
	protected void read(Channel c) throws IOException {
		buffer.clear();
		int read = ((SocketChannel)c).read(buffer);
		if (read > 0) {
			buffer.flip();
			CharBuffer cb = decoder.decode(buffer);
			//System.out.println(cb.toString());
			this.onStringMessage(cb.toString());
		} 
		//else no data.
		LockSupport.parkNanos(10); //micro sleep to save cpu burn.
	}

    CharsetDecoder getDecoder() {
        return decoder;
    }

	@Override
	public void start() {
		setState(ComponentState.STARTING);
		this.listen();
		if (channel != null && channel.isOpen()) {
			logger.info(getName()+" is UP");
			setState(ComponentState.AVAILABLE);
		}
	}
	
	@Override
	public void stop() {
		setState(ComponentState.STOPPING);
		logger.info(getName() + " is stopping");
		try {
			if (channel == null) {
				setState(ComponentState.STOPPED);
			} else {
				channel.close();
				if (!channel.isOpen()) {
					logger.info(getName()+" is DOWN");
					setState(ComponentState.STOPPED);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot stop " + getName(),e);
		}
		
	}
	
}
