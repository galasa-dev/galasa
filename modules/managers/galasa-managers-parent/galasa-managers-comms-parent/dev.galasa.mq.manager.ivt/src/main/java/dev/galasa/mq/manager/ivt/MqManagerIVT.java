/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.mq.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;

import dev.galasa.Before;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.mq.IMessageQueue;
import dev.galasa.mq.IMessageQueueManager;
import dev.galasa.mq.MqManagerException;
import dev.galasa.mq.Queue;
import dev.galasa.mq.QueueManager;


@Test
public class MqManagerIVT {
	
	private static String testData = "Hello World";
	
	@QueueManager(tag = "bob")
	public IMessageQueueManager qmgr;
	
	@Queue(archive = true, name = "GALASA.INPUT.QUEUE", tag = "ggbvg", queueMgrTag = "bob")
	public IMessageQueue queue;
	
	@Queue(archive = false, name = "GALASA.INPUT.QUEUE2")
	public IMessageQueue queue2;
	
//	@Queue(tag = "NEWQUEUE")
//	public IMessageQueue queue3;
	
    @Logger
    public Log logger;


    @Before
    public void clearQueues() {
    	queue.clearQueue();
    	queue2.clearQueue();
    }
    
    @Test
    public void testPutMessage() throws MqManagerException {
    	TextMessage tm = qmgr.createTextMessage(testData);
    	queue.sendMessage(tm);
    }
    
    @Test
    public void testGetMessage() throws MqManagerException, JMSException {
    	testPutMessage();
    	Message m = queue.getMessage();
    	String response = m.getBody(String.class);
    	assertThat(response).isEqualTo(testData);
    }
    
    @Test
    public void clearQueue() throws MqManagerException {
    	TextMessage tm = qmgr.createTextMessage(testData);
    	queue.sendMessage(tm,tm,tm,tm,tm,tm,tm,tm);
    	
    	queue.clearQueue();
    	Message m = queue.getMessageNoWait();
    	assertThat(m).isNull();
    }
    
    @Test
    public void checkQueuesAreSeparate() throws MqManagerException {
    	TextMessage m = qmgr.createTextMessage("This message is on queue1");
    	queue.sendMessage(m);
    	
    	assertThat(queue2.getMessageNoWait()).isNull();
    }   
    
    @Test
    public void putGetBinary() throws MqManagerException{
    	byte[] input = {41,01,33,76};
    	BytesMessage m = qmgr.createBytesMessage(input);
    	
    	queue.sendMessage(m);
    	queue.getMessageNoWait();
    }
}
