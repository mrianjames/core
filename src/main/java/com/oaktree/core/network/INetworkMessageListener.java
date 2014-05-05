/*
 * Created on 20-Mar-2005 by Ian James
 * 
 */
package com.oaktree.core.network;

/**
 * @author Ian James
 */
public interface INetworkMessageListener {

    /**
     * Handle incoming bytes
     * @param message
     */
    public void receive(byte[] message);

    /**
     * Handle an incoming object off the wire.
     * @param object
     */
    public void receive(Object object);
}
