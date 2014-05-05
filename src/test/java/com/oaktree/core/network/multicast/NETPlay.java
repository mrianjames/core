package com.oaktree.core.network.multicast;

import java.net.*;

public class NETPlay {
	public static int serverPort = 666;
	public static int clientPort = 999;
	public static int buffer_size = 1024;
	public static DatagramSocket ds;
	public static byte buffer[] = new byte[buffer_size];
	
	public static void TheServer() throws Exception {
		int i = 0;
		while (true) {
			byte[] bits = ("HELLO"+i).getBytes();

			ds.send(new DatagramPacket(bits, bits.length, InetAddress
					.getLocalHost(), clientPort));
			i++;
		}
	}

	public static void TheClient() throws Exception {
		while (true) {
			DatagramPacket p = new DatagramPacket(buffer, buffer.length);
			ds.receive(p);
			System.out.println(new String(p.getData()));
		}
	}

	public static void main(String args[]) throws Exception {
		if (args.length == 1) {
			ds = new DatagramSocket(serverPort);
			TheServer();
		} else {
			ds = new DatagramSocket(clientPort);
			TheClient();
		}
	}
}