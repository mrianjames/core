/*
 * Created on 20-Mar-2005 by Ian James
 * 
 */
package com.oaktree.core.network.multicast;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.logging.Log;
import com.oaktree.core.network.INetworkMessageSender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ian James
 * com.ij.util.network.multicast.MulticastServer
 * 
 * In a multicast server, the roles of server-client are reversed. The multicast server
 * is a service that sends out a broadcast message to listening "client" @see MulticastClient 
 * applications.
 *
 * Multicasting is akin to broadcasting a message to a virtual address which is being listened
 * to by any number of client applications. 
 * 
 * Addresses for multicasting are in the range 224.0.0.0 through 239.255.255.255
 * @see <a href="http://www.iana.org/assignments/multicast-addresses">this</a>
 * 
 * Note: 224.0.0.0 and 224.0.0.255 are reserved for lowlevel router processes.
 */
public class MulticastSender extends AbstractComponent implements INetworkMessageSender {

    /**
     * The clientPort is the port that the multicast clients are listening for messages on.
     */
    private int port;

    /**
     * The host is the host location that clients are listening to.
     */
    private String address;
    /**
     * Our socket object that we will send the message out on.
     */
    private DatagramSocket socket;

    /**
     * THE logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(MulticastSender.class.getName());

    /*
     * Inet address of the multicast group we are joining.
     */
    private InetAddress add;

	private Charset encoding = Charset.defaultCharset();
	public void setEncoding(String encoding) {
		this.encoding = Charset.forName(encoding);
	}

    public MulticastSender() {}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Create a multicast server.
     * @param host
     * @param clientPort
     */
    public MulticastSender(String host, int clientPort) {
        this.address = host;
        this.port = clientPort;
    }

    /**
     * Send a string
     * @param message
     * @throws IOException 
     */
    public void send(String message) throws IOException {
        send(message.getBytes(encoding ));
    }

    /**
     * Send an object
     * @param message
     */
    public void sendObject(Serializable message) {
        try {

            ByteArrayOutputStream b_out = new ByteArrayOutputStream();
            ObjectOutputStream o_out = new ObjectOutputStream(b_out);

            o_out.writeObject(message);

            this.send(b_out.toByteArray());
        } catch (IOException e) {
            Log.exception(logger, e);
        }

    }

    @Override
    public void start() {
        try {
            this.add = InetAddress.getByName(this.address);
            this.doStart();
        } catch (UnknownHostException e) {
            Log.exception(logger,e);
        }
    }

    private void doStart() {
        try {
            this.socket = new DatagramSocket();
            logger.info("Sender Multicast socket created for sender " + this.getName() + ". Will be sending on " + this.address + ":" + this.port);
        } catch (SocketException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (this.socket != null) {
            this.socket.close();
        }
        logger.info("Connection is closed");
    }

    /**
     * Send some bytes
     * This creates a connection on outPort, to the host/port, and
     * sends the bytes to that location.
     * @param message
     * @throws IOException 
     */
    public void send(byte[] message) throws IOException {
        if (this.socket == null) {
            logger.warn("socket not open before write attempt. Starting");
            doStart();
        }
        DatagramPacket packet = new DatagramPacket(message, message.length, this.add, this.port);
        logger.info("Sending " + packet.getLength());
        socket.send(packet);
        if (logger.isInfoEnabled()) {
            logger.info("Sent message " + new String(message,Charset.defaultCharset()).trim() + " to " + this.address + " on " + this.port);
        }
    }

    /**
     * For testing.
     * @param args
     */
    public static void main(String[] args) {
        MulticastSender server = new MulticastSender("239.0.0.1", 1234);
        try {
            server.initialise();
            server.start();
            for (int i = 0; i < 100; i++) {
            	//server.sendObject("Hello"+i);
            	server.send("Hello"+i);
            }
            server.stop();
        } catch (Exception e) {
            System.err.println("Exception sending multicast message: " + e.toString());
        }
    }
    
    public void boot() {
		this.initialise();
		this.start();
	}
}
