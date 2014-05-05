package com.oaktree.core.container;

/**
 * Types of message failure reasons sent by an intermediary component to a sender when a message
 * cannot be delivered.
 * @author Oak Tree Designs Ltd
 *
 */
public enum MessageDeliveryFailure {
	NO_COMPONENT_FOUND,UNEXPECTED_EXCEPTION,MALFORMED_MESSAGE, NO_COMPONENT_COULD_BE_MANUFACTURED
}
