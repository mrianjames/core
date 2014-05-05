package com.oaktree.core.syslog;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.container.ComponentType;
import com.oaktree.core.utils.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Log messages to std syslog formats.
 * http://en.wikipedia.org/wiki/Syslog#Format_of_a_Syslog_packet
 * 
 * This is BSD format...
 * 
 * <PRI>HEADER MSG
 * 
 * The PRI part is a number that is enclosed in angle brackets. This represents both the Facility and Severity of the message. 
 * This number is an eight bit number. The first 3 least significant bits represent the 
 * Severity of the message (with 3 bits you can represent 8 different Severities) 
 * and the other 5 bits represent the Facility of the message. You can use the Facility and the 
 *
 * Calculating Priority Value[edit]
 * The Priority value is calculated by first multiplying the Facility number by 8 and then adding the numerical value of the Severity. For example, a kernel message (Facility=0) with a Severity of Emergency (Severity=0) would have a Priority value of 0. Also, a "local use 4" message (Facility=20) with a Severity of Notice (Severity=5) would have a Priority value of 165. In the PRI part of a Syslog message, these values would be placed between the angle brackets as <0> and <165> respectively.

 * <B>Header</B> (This is applied by this class)
 * The HEADER part contains the following things:
 * 
 * Timestamp -- The Time stamp is the date and time at which the message was generated. Formatted MMM dd HH:mm:ss
 * Hostname or IP address of the device.
 * 
 * <B>Message</B>
 * The MSG part will fill the remainder of the Syslog packet. This will usually contain some additional information of the process that generated the message, and then the text of the message. The MSG part has two fields:

 * TAG field
 * CONTENT field
 * The value in the TAG field will be the name of the program or process that generated the message. The CONTENT contains the details of the message.
 *
 * To use this class:
 * String url = "@<hostname/ip>:<port, default 514>"; //use @@ for TCP protocol, UDP is @.
 * SysLogger sl = new SysLogger("myprogramname","testlogger",url);
 * sl.initialise();
 * sl.start();
 * ...
 * sl.log(SysLogger.Priority
 * @author ij
 *
 */
public class SysLogger extends AbstractComponent {
	private final static Logger logger = LoggerFactory.getLogger(SysLogger.class);
	private static final int DEFAULT_UDP_PORT = 514;
	private static final int DEFAULT_TCP_PORT = 1468;
	private String programName;
	private Protocol protocol;
	private InetAddress host;
	private int port;
	private Channel channel;
	
	private String tag;
	private String ip;
	public SysLogger(String programName, String loggerName) {
		this.programName = programName;
		this.protocol = Protocol.UDP;
		try {
			this.host = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			logger.error("Cannot resolve host" + host,e);
		}
		setComponent(loggerName);
	}
	public SysLogger(String programName, String loggerName, String url) {
		this.programName = programName;
		parseUrl(url);
		setComponent(loggerName);
		setTag();
	}
	private void setTag() {
		this.tag = programName+Text.PERIOD+getName();
	}
	/**
	 * Parse the url format into useful info.
	 * @param url
	 */
	private void parseUrl(String url) {
		if (url == null) {
			throw new IllegalArgumentException("Invalid url: " + url);
		}
		int start;
		if (url.contains("@@")) {
			this.protocol = Protocol.TCP;
			start = 2;
		} else {
			this.protocol = Protocol.UDP;
			start = 1;
		}
		String[] bits = url.substring(start).split("[:]");
		try {
			if (bits.length == 0) {
				host = InetAddress.getLocalHost();
				if (protocol.isUdp()) {
					port = DEFAULT_UDP_PORT;
				} else {
					port = DEFAULT_TCP_PORT;
				}
			} else if (bits.length == 1) {
				host = InetAddress.getByName(bits[0]);
				if (protocol.isUdp()) {
					port = DEFAULT_UDP_PORT;
				} else {
					port = DEFAULT_TCP_PORT;
				}
			} else if (bits.length == 2) {
				host = InetAddress.getByName(bits[0]);
				port = Integer.valueOf(bits[1]);
			} else {
				throw new IllegalArgumentException("Cannot parse url: "+ url);
			}
		} catch (UnknownHostException e) {
			logger.error("Cannot resolve host" + host,e);
		}
	}
	@Override
	public void initialise() {
		this.setState(ComponentState.INITIALISING);
		try {
			this.ip = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e1) {
			logger.error("Cannot resolve my ip address",e1);
		}
		logger.info("initialising syslogger "+toString());
		try {
			if (protocol.isUdp()) {
				channel = openUdp();
			} else if (protocol.isTcp()) {
				channel = openTcp();				
			} else {
				throw new IllegalStateException("Invalid procol" + protocol.name());
			}
			logger.info("initialised syslogger "+toString());
			if (channel.isOpen()) {
				setState(ComponentState.AVAILABLE);
			}			
		} catch (Exception e) {
			logger.error("Cannot open connection: ",e);
			setState(ComponentState.UNAVAILABLE);
		}
	}
	
	@Override
	public void start() {
		//nothing todo.
	}
	
	@Override
	public void stop() {
		logger.info("Stopping "+ toString());
		setState(ComponentState.STOPPING);
		if (channel != null) {
			try {
				channel.close();
				setState(ComponentState.UNAVAILABLE);
				logger.info("Stopped "+ toString());
			} catch (IOException e) {
				logger.error("Cannot close channel",e);
			}
		}
	}
	private Channel openUdp() throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		channel.connect(new InetSocketAddress(host,port));
		return channel;
	}
	
	private Channel openTcp() throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.connect(new InetSocketAddress(host,port));
		return channel;
	}
	@Override
	public String toString() {
		return programName+"."+getName()+" ["+protocol.name()+"] "+ host.getCanonicalHostName()+"["+host.getHostAddress()+"]:"+port+Text.SPACE+getState().name() ;
	}
	
	private void setComponent(String loggerName) {
		this.setName(loggerName);
		this.setComponentType(ComponentType.SERVICE);
		this.setComponentSubType("SysLog");
	}

	public void log(int priority, String message) {
		try {
			if (protocol.isUdp()) {
				logUdp(priority,message);
			} else {
				logTcp(priority,message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Cannot send message " ,e);
		}
	}
	private void logTcp(int priority, String message) throws IOException {
		((SocketChannel)channel).write(makeBuffer(priority,message));
	}
	private void logUdp(int priority, String message) throws IOException {
		((DatagramChannel)channel).write(makeBuffer(priority,message));
	}
	private ByteBuffer makeBuffer(int priority, String message) {
		ByteBuffer buf = ByteBuffer.allocate(1024);
		String msg = makeMsg(priority,message);
		if (logger.isInfoEnabled()) {
			logger.info("OUT: " + msg);
		}
		buf.put(msg.getBytes());
		buf.flip();
		return buf;
	}
	private DateFormat tsf = new SimpleDateFormat("MMM dd HH:mm:ss");
	private String makeMsg(int priority, String message) {
		return "<"+priority+">"+getHeader()+Text.SPACE+tag+Text.SPACE+message;
	}
	private String getHeader() {
		return tsf.format(new Date(getTimestamp()))+Text.SPACE+getIp();
		//return "";
	}
	private String getIp() {
		return this.ip;
	}
	private long getTimestamp() {
		return System.currentTimeMillis();
	}
	public void log(int facility,int severity, String message) {
		int priority = resolvePriority(facility,severity);
		log(priority,message);
	}
	
	private int resolvePriority(int facility, int severity) {
		return (facility * 8)+severity;
	}

}
