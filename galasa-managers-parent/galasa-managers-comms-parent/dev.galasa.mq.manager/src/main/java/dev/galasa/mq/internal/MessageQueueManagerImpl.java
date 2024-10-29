/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.mq.internal;

import javax.jms.BytesMessage;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.mq.IMessageQueueManager;
import dev.galasa.mq.MqManagerException;

public class MessageQueueManagerImpl implements IMessageQueueManager {
	
	private static final Log  log = LogFactory.getLog(MessageQueueManagerImpl.class);
	
	private String tag;
	private String name;
	private String host;
	private int port;
	private String channel;
	private MQManagerImpl manager;
	
	private JMSContext context;
	
	public MessageQueueManagerImpl(String tag, String name, String host, int port, String channel, MQManagerImpl manager) {
		this.tag = tag;
		this.name = name;
		this.host = host;
		this.port = port;
		this.channel = channel;		
		this.manager = manager;
	}
	
	/*
	 * Readies this queue manager for operation
	 * by constructing the JMSContext
	 */
	public void startup() throws MqManagerException{
		log.info("Starting connection to queue manager: " + this.name);
		
		ICredentialsUsernamePassword creds = (ICredentialsUsernamePassword)manager.getCredentials(this.tag);
		
		try {
			JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
			JmsConnectionFactory cf = ff.createConnectionFactory();

			// Set the properties
			cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
			cf.setIntProperty(WMQConstants.WMQ_PORT, port);
			cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
			cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
			cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, name);
			cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsPutGet (JMS)");
			cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
			cf.setStringProperty(WMQConstants.USERID, creds.getUsername());
			cf.setStringProperty(WMQConstants.PASSWORD, creds.getPassword());
			this.context = cf.createContext();
		} catch (JMSException e) {
			throw new MqManagerException("Error while constructing connection to queue manager");
		}
		log.info("Connection to queue manager: " + this.name + " complete");
	}

	@Override
	public void close() {
		log.info("Shutting down connection to queue manager: " + this.getName());
		if(this.context == null)
			return;
		else
			this.context.close();
		
	}
	
	@Override
	public TextMessage createTextMessage(String messageContent) throws MqManagerException {
		TextMessage message = context.createTextMessage();
		try {
			message.setText(messageContent);
		}catch(JMSException e) {
			throw new MqManagerException("Unable to create a new Text Message for queue manager: " + this.name, e);
		}
		
		return message;
	}
	
	@Override
	public BytesMessage createBytesMessage(byte[] input) throws MqManagerException {
		BytesMessage message = context.createBytesMessage();
		try {
			message.writeBytes(input);
		} catch (JMSException e) {
			throw new MqManagerException("Unable to create a new Bytes Message for queue manager: " + this.name, e);
		}
		return message;
	}
	
	protected JMSContext getContext() {
		return this.context;
	}
	
	protected String getName() {
		return this.name;
	}

}
