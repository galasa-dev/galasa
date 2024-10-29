/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.mq;

import javax.jms.Message;

public interface IMessageQueue {
	
	/**
	 * puts the provided messages onto the queue
	 * @param messages the set of messages to send
	 */
	public void sendMessage(Message... messages);
	
	/**
	 * Retrieves the first message from the queue, waiting until a message is ready
	 * @return the message from the queue
	 */
	public Message getMessage();
	
	/**
	 * Retrieves the first message from the queue, waiting for the timeout
	 * @param timeout - long timeout in milliseconds
	 * @return the message - or null if no message is available before timeout
	 */
	public Message getMessage(long timeout);
	
	/**
	 * Retrieves the first message from the queue returning immediately if no message is available
	 * @return the message from the queue or null
	 */
	public Message getMessageNoWait();
	
	/**
	 * Clears the queue by consuming messages until queue is empty
	 * messages consumed are not archived regardless of the '@queue' annotation
	 */
	public void clearQueue();
}
