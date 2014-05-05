/*
 * Created on 20-Mar-2005 by Ian James
 * 
 */
package com.oaktree.core.network.multicast;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.logging.Log;
import com.oaktree.core.network.INetworkMessageListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will only work when you are connected to a network or have a loopback device.
 *
 * @author Ian James com.ij.util.network.multicast.MulticastClient
 */
public class MulticastReceiver extends AbstractComponent {

	/**
	 * Should we serialize?
	 */
	private boolean serialize = true;
	private final static Logger logger = LoggerFactory
			.getLogger(MulticastReceiver.class.getName());
	/**
	 * Our port to listen on
	 */
	private int port;
	/**
	 * Our location to listen to
	 */
	private String address;
	/**
	 * Our socket to listen on.
	 */
	private MulticastSocket socket;
	/**
	 * Our internet address to listen to.
	 */
	private InetAddress add;
	/**
	 * Our object that will receive the incoming messages.
	 */
	private INetworkMessageListener listener;
	/**
	 * Size of the receiving buffer.
	 */
	private int bufferSize = 2056;
	private Thread listeningThread;

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public MulticastReceiver() {
	}

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
	 * Create a multicast client listening on a port at a given ip.
	 * 
	 * @param port
	 * @param host
	 */
	public MulticastReceiver(String host, int port, int bufferSize) {
		this.address = host;
		this.port = port;
		this.bufferSize = bufferSize;
	}

	public MulticastReceiver(String host) {
		this.address = host;
		// default values.
		this.bufferSize = 2056;
		this.port = 1111;
	}

	public MulticastReceiver(String host, int port) {
		this.address = host;
		this.port = port;
		this.bufferSize = 2056;
	}

	/**
	 * Associate a handler to receive messages from this listener.
	 * 
	 * @param listener
	 */
	public void setNetworkMessageListener(INetworkMessageListener listener) {
		this.listener = listener;
	}

	@Override
	public void start() {
		try {
			this.doStart();
		} catch (IOException ex) {
			throw new IllegalStateException(ex.getMessage());
		}
	}

	/**
	 * Open our connection.
	 * 
	 * @throws IOException
	 */
	private void doStart() throws IOException {
		this.socket = new MulticastSocket(this.port);
		this.add = InetAddress.getByName(this.address);
		this.socket.joinGroup(this.add);
		if (logger.isInfoEnabled()) {
			logger.info("Multicast receiver opened & Joined group: "
					+ this.address + ":" + this.port);
		}
		this.listen();
	}

	@Override
	public void stop() {
		try {
			super.stop();
			this.doStop();
		} catch (IOException ex) {
			throw new IllegalStateException(ex.getMessage());
		}
	}

	/**
	 * Close our connection.
	 * 
	 * @throws IOException
	 * 
	 */
	public void doStop() throws IOException {
		// if (this.socket != null && this.socket.isConnected()) {
		if (this.socket != null && this.socket.isBound()) {
			this.socket.leaveGroup(this.add);
			this.socket.close();
		}
		logger.info("Closed socket and left group");
	}

	/**
	 * Listen for message and delegate message to handler.
	 * 
	 */
	private void listen() {
		this.listeningThread = new Thread(new Runnable() {

			@Override
			public void run() {
				DatagramPacket packet;
				/*
				 * While we are still connected, try and receive a message Block
				 * until we do.
				 */
				byte message[] = new byte[bufferSize];
				while (!MulticastReceiver.this.socket.isClosed()) {

					try {
						//
						packet = new DatagramPacket(message, message.length);
						// block until message.
						try {
							logger.info("Waiting to receive data into buffer "
									+ bufferSize);
							MulticastReceiver.this.socket.receive(packet);
							logger.info("Bytes received: " + packet.getLength()
									+ " bytes");
						} catch (Throwable e2) {
							if (MulticastReceiver.this.getState().equals(
									ComponentState.STOPPING)
									|| MulticastReceiver.this.getState()
											.equals(ComponentState.STOPPED)) {
								break;
							} else {
								Log.exception(logger, e2);
							}
						}
						/*
						 * Notify our listener, if there is one.
						 */
						try {
							if (MulticastReceiver.this.listener != null) {
								if (!MulticastReceiver.this.serialize) {
									byte[] bts = new byte[packet.getLength()];
									System.arraycopy(packet.getData(), 0, bts,
											0, packet.getLength());
									MulticastReceiver.this.listener
											.receive(bts);
								} else {

									ByteArrayInputStream bis = new ByteArrayInputStream(
											message);
									ObjectInputStream o_in = new ObjectInputStream(
											bis);
									Object o = o_in.readObject();
									MulticastReceiver.this.listener.receive(o);

								}
							}
						} catch (Throwable e) {
							Log.exception(logger, e);
						}
					} catch (Throwable t) {
						Log.exception(logger, t);
					}
				}

				logger.info("Client has ceased to listen on socket.");
			}
		});
		this.listeningThread.setName("MulticastReceiver" + (id++));
		this.listeningThread.start();
	}

	private static int id = 0;

	/**
	 * For Testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		MulticastReceiver client = new MulticastReceiver(args[0], Integer
				.valueOf(args[1]), 256);
		final Charset encoding = Charset.defaultCharset();				
		client.setSerialize(false);
		client.setNetworkMessageListener(new INetworkMessageListener() {

			@Override
			public void receive(byte[] message) {
				logger.info(">>>" + new String(message,encoding).trim());
			}

			@Override
			public void receive(Object object) {
				logger.info("INCOMING OBJECT: " + object);
			}
		});
		client.initialise();
		client.start();
		try {
			while (true) {
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Set if we should serialize responses, or pass on raw bytes.
	 * 
	 * @param serialize
	 */
	public void setSerialize(boolean serialize) {
		this.serialize = serialize;
	}
	
	public void boot() {
		this.initialise();
		this.start();
	}
}
