/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.mq;

import javax.jms.BytesMessage;
import javax.jms.TextMessage;

public interface IMessageQueueManager {
	
	/**
	 * creates a new JMS Message of type TEXT
	 * @return the message
	 * @throws MqManagerException 
	 */
	public TextMessage createTextMessage(String content) throws MqManagerException;
	
	/**
	 * creates a new JMS Message
	 * @return the message
	 */
	public BytesMessage createBytesMessage(byte [] input) throws MqManagerException;

    public void close();

}
