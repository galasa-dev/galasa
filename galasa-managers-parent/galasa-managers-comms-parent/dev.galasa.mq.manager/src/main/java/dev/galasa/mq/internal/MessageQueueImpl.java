/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.mq.internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.mq.IMessageQueue;

public class MessageQueueImpl implements IMessageQueue {

	//Fields received from the constructor
	private String queueName;
	private MessageQueueManagerImpl qmgr;
	private boolean archive;
	private MQManagerImpl manager;

	private static String QUEUE_PROTOCOL = "queue:///";
	private static String RAS_NAMESPACE = "mq";
	private static String RAS_MESSAGING = "messages";
	private static String ARCHIVE_FILE_PREFIX = "message_";

	//Generated Fields
	private Destination destination;
	private JMSConsumer consumer;
	private JMSProducer producer;

	//Internal State
	private boolean started = false;
	private String currentMethod = new String();
	private int numberOfMessagesLoggedInThisMethod = 1;

	private static final Log  logger = LogFactory.getLog(MessageQueueImpl.class);

	public MessageQueueImpl(String name, MessageQueueManagerImpl qmgr, boolean archive, MQManagerImpl manager) {
		this.queueName = name;
		this.qmgr = qmgr;
		this.archive = archive;
		this.manager = manager;
	}

	/**
	 * Starts the connection to the queue by obtaining the context from the qmgr
	 * If we have already been started then just exit
	 */
	public void startup() {
		if(started) {
			logger.info("Connection to queue: " + this.queueName + " already started");
			return;
		}
		JMSContext context = qmgr.getContext();
		destination = context.createQueue(QUEUE_PROTOCOL + this.queueName);
		producer = context.createProducer();
		consumer = context.createConsumer(destination);
		this.started = true;
		logger.info("Connection to queue: " + this.queueName + " complete");
	}

	@Override
	public void sendMessage(Message... messages) {
		for(Message message : messages) {
			archiveMessage(message,MessageDirection.OUTBOUND);
			producer.send(destination, message);
		}
	}

	@Override
	public Message getMessage() {
		Message m = consumer.receive();
		archiveMessage(m, MessageDirection.INBOUND);
		return m;
	}

	@Override
	public Message getMessage(long timeout) {
		Message m = consumer.receive(timeout);
		archiveMessage(m, MessageDirection.INBOUND);
		return m;
	}

	@Override
	public Message getMessageNoWait() {
		Message m = consumer.receiveNoWait();
		archiveMessage(m, MessageDirection.INBOUND);
		return m;
	}

	@Override
	public void clearQueue() {
		while(consumer.receiveNoWait() != null) {}
	}

	public String getName() {
		return this.queueName;
	}

	public MessageQueueManagerImpl getQmgr() {
		return this.qmgr;
	}
	/**
	 * If archival has been set in the annotation for this queue then
	 * we archive this message in the RAS in the structure:
	 *
	 * mq
	 * |
	 * messages
	 * |
	 * ---<method name>
	 *      |
	 *      ---<queue name>
	 *           |
	 *           ---<inbound/outbound>
	 *               |
	 *               ---message_<id>
	 * @param m the message we are archiving
	 * @param direction the direction in which this message is traveling from the perspective of the test
	 */
	private void archiveMessage(Message m, MessageDirection direction) {
		if(m == null || !archive)
			return;
		byte[] content;
		try {
			content = getContentOfMessage(m);
		}catch (JMSException e) {
			logger.warn("Unable to retrieve the content of a message while archiving");
			return;
		}

		Path folder = manager.getStoredArtifactRoot()
							 .resolve(RAS_NAMESPACE)
							 .resolve(RAS_MESSAGING)
							 .resolve(getCurrentMethod())
							 .resolve(this.queueName)
							 .resolve(direction.toString().toLowerCase())
							 .resolve(ARCHIVE_FILE_PREFIX + Integer.toString(numberOfMessagesLoggedInThisMethod));
		try {
			Files.write(folder, content, StandardOpenOption.CREATE);
		} catch (Exception e) {
			logger.info("Unable to log message for a queue", e);
		}
		this.numberOfMessagesLoggedInThisMethod++;
	}

	private byte[] getContentOfMessage(Message m) throws JMSException {
		byte[] content = new byte[0];

		if(m instanceof TextMessage) {
			content = m.getBody(String.class).getBytes();
		}

		if(m instanceof BytesMessage) {
			BytesMessage bm = (BytesMessage)m;
			bm.reset();
			content = new byte[Math.toIntExact(bm.getBodyLength())];
			bm.readBytes(content);
			content = Hex.encodeHexString(content).getBytes();
		}
		return content;
	}

	/**
	 * Internal check that the current method is the same as the
	 * method that we last checked  This allows us to create an index of the
	 * messages we have archived
	 * @return
	 */
	private String getCurrentMethod() {
		String managerCurrentMethod = this.manager.getCurrentMethod();
		if(this.currentMethod.equals(managerCurrentMethod)) {
			return currentMethod;
		}else {
			this.currentMethod = managerCurrentMethod;
			this.numberOfMessagesLoggedInThisMethod = 1;
		}
		return this.currentMethod;
	}
}
