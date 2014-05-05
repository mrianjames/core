package com.oaktree.core.network;

import java.io.IOException;
import java.io.Serializable;

public interface INetworkMessageSender {
	public void send(String text) throws IOException;
	public void sendObject(Serializable object) throws IOException;
	public void send(byte[] bytes) throws IOException;
}
