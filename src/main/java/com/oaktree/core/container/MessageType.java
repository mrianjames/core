package com.oaktree.core.container;

/**
 * A type of message that is can be passed between components.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public enum MessageType {
	APPLICATION,CONTROL, MESSAGE_NACK, MESSAGE_ACK,DATA,HEARTBEAT
}
