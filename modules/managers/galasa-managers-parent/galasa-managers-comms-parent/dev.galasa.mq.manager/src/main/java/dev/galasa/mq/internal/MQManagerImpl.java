/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.mq.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ICredentials;
import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.mq.IMessageQueue;
import dev.galasa.mq.IMessageQueueManager;
import dev.galasa.mq.MqManagerException;
import dev.galasa.mq.MqManagerField;
import dev.galasa.mq.Queue;
import dev.galasa.mq.QueueManager;
import dev.galasa.mq.internal.properties.InstanceChannelName;
import dev.galasa.mq.internal.properties.InstanceCredentials;
import dev.galasa.mq.internal.properties.InstanceForTag;
import dev.galasa.mq.internal.properties.InstanceHost;
import dev.galasa.mq.internal.properties.MqPropertiesSingleton;
import dev.galasa.mq.internal.properties.QueueNameForTag;
import dev.galasa.mq.internal.properties.InstanceName;
import dev.galasa.mq.internal.properties.InstancePort;

@Component(service = { IManager.class })
public class MQManagerImpl extends AbstractManager {

    private static final Log  logger = LogFactory.getLog(MQManagerImpl.class);
    private static final String NAMESPACE   = "mq";
    
    private Path storedArtifactsRoot;
    private ICredentialsService credentialService;
    
    private HashMap<String,MessageQueueManagerImpl> queueManagers = new HashMap<>();
    private HashMap<String,MessageQueueImpl> queuesByName = new HashMap<>();
    private HashMap<String,MessageQueueImpl> queuesByTag = new HashMap<>();
    //private List<MessageQueueImpl> queues = new ArrayList<>();
    
    private String currentMethod = new String();

    @GenerateAnnotatedField(annotation = Queue.class)
    public IMessageQueue generateMessageQueue(Field field, List<Annotation> annotations) throws MqManagerException {
    	Queue annotation = field.getAnnotation(Queue.class);
    	
    	//check that we have provisioned the qmgr for this queue
    	String qmgrTag = annotation.queueMgrTag();
    	if(!queueManagers.containsKey(qmgrTag)) {
    		throw new MqManagerException("Unable to provision queue: " + annotation.name() + " no QMGR found tagged: " + qmgrTag);
    	}
    	
    	//check if the name is specified in the queue or in the CPS
    	String queueName = annotation.name();
    	String queueTag = annotation.tag();
    	
    	//check that either queueName OR queueTag is specified
    	if(queueName.isEmpty() && queueTag.isEmpty())
    		throw new MqManagerException("Either name or tag must be specified in @Queue annotation");
    	
    	if(!queueName.isEmpty() && !queueTag.isEmpty())
    		throw new MqManagerException("Both name and tag are specified in @Queue annotation, these are mutually exclusive");

    	//if queueName was specified generate the queue
    	if(!queueName.isEmpty()) {
    		MessageQueueImpl queue = new MessageQueueImpl(queueName,queueManagers.get(qmgrTag), annotation.archive(), this);
    		this.queuesByName.put(queueName, queue);
    		registerAnnotatedField(field, queue);
    		return queue;
    	}else {
    		//if the queue name was empty then the tag will have been filled
    		if(queuesByTag.get(queueTag) != null) {
    			registerAnnotatedField(field, queuesByTag.get(queueTag));
    			return queuesByTag.get(queueTag);
    		} else {
    			String name = QueueNameForTag.get(queueTag);
    			MessageQueueImpl queue = new MessageQueueImpl(name,queueManagers.get(qmgrTag), annotation.archive(), this);
    			this.queuesByTag.put(queueTag, queue);
    			registerAnnotatedField(field, queue);
    			return queue;
    		}
    	}
    	
    	//if only tag was specified, see if we have provisioned a queue with the same tag
    }
    
    @GenerateAnnotatedField(annotation = QueueManager.class)
    public IMessageQueueManager generateMessageQueueManager(Field field, List<Annotation> annotations) throws MqManagerException{
    	//obtain the tag for this queue manager
    	QueueManager annotation = field.getAnnotation(QueueManager.class);
    	String tag = annotation.tag();
    	
    	//if we already have created a qmgr for this tag just return it
    	if(this.queueManagers.get(tag) != null) {
    		return this.queueManagers.get(tag);
    	}
		
		//obtain the configuration for this queue manager
		String instanceid = getInstanceForTag(tag);
		logger.trace("Obtaining configuration information for instance: " + instanceid);
		String host = InstanceHost.get(instanceid);
		int port = InstancePort.get(instanceid);
		String channel = InstanceChannelName.get(instanceid);
		String name = InstanceName.get(instanceid);

		//construct the queue manager and add it to the list
		logger.info("Queue manager tagged: " + tag + " is name: " + name + " (" + host + ":" + port + ")");
        MessageQueueManagerImpl qmgr = new MessageQueueManagerImpl(tag, name, host, port, channel, this);
        this.queueManagers.put(tag, qmgr);
        registerAnnotatedField(field, qmgr);
        return qmgr;
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
    	List<AnnotatedField> annotatedFields = findAnnotatedFields(MqManagerField.class);
    	
    	//First generate the queue managers
    	for(AnnotatedField af : annotatedFields) {
    		Field f = af.getField();
    		if(f.getType() == IMessageQueueManager.class) {
    			QueueManager annotation = f.getAnnotation(QueueManager.class);
    			if(annotation != null) {
    				generateMessageQueueManager(f, af.getAnnotations());
    			}
    			
    		}
    	}
        generateAnnotatedFields(MqManagerField.class);
    }

    @Override
    public void shutdown() {
    	
    }

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
			MqPropertiesSingleton.setCps(getFramework().getConfigurationPropertyService(NAMESPACE));
		} catch (ConfigurationPropertyStoreException e1) {
			throw new MqManagerException("Unable to access framework services", e1); 	
		}
        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(MqManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
        
        this.storedArtifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        try {
			this.credentialService = getFramework().getCredentialsService();
		} catch (CredentialsException e) {
			throw new MqManagerException("Unable to access credentials service",e);
		}
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }
        activeManagers.add(this);
    }
    
    @Override
    public boolean doYouSupportSharedEnvironments() {
        return true;   // this manager does not provision resources, therefore support environments 
    }
    
    @Override
    public void provisionStart() throws ManagerException, ResourceUnavailableException {
    	/*
    	 * This method of starting the qmgrs and then the queues is not the most effective
    	 * If we had a test with lots of qmgrs and queues then it would be better to 
    	 * start the qmgrs async and on completion start the required queues, however this 
    	 * is unlikely to be a real issue
    	 */
    	//start the queue managers first 
    	for(Entry<String, MessageQueueManagerImpl> entry : this.queueManagers.entrySet()) {
    		MessageQueueManagerImpl qmgr = entry.getValue();
    		try {
    			qmgr.startup();
    		}catch(MqManagerException mqme) {
    			logger.error("Unable to start Queue Manager");
    			throw mqme;
    		}	
    	}
    	
    	//Now that the queue managers are active start the queues
    	//begin with the queues by name
    	for(Entry<String, MessageQueueImpl> entry : this.queuesByName.entrySet()) {
    		MessageQueueImpl queue = entry.getValue();
    		queue.startup();
    	}
    	//Now the queues by tag
    	for(Entry<String, MessageQueueImpl> entry : this.queuesByTag.entrySet()) {
    		MessageQueueImpl queue = entry.getValue();
    		queue.startup();
    	}
    }
    
    @Override
    public void provisionStop() {
    	for(Entry<String, MessageQueueManagerImpl> entry : this.queueManagers.entrySet()) {
    		MessageQueueManagerImpl qmgr = entry.getValue();
    		qmgr.close();	
    	}
    }
    
    @Override
    public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
    	super.startOfTestMethod(galasaMethod);
    	this.currentMethod = galasaMethod.getJavaExecutionMethod().getName();
    }

	public String getCurrentMethod() {
		return currentMethod;
	}
	
	public ICredentials getCredentials(String tag) throws MqManagerException {
		
		//get the credentials id for this instance
		String instanceid = getInstanceForTag(tag);
		String credentialsid = InstanceCredentials.get(instanceid);
		
		//pull the access credentials
    	ICredentials credentials;;
    	logger.trace("Obtaining credentials for id: " + credentialsid);
		try {
			credentials = credentialService.getCredentials(credentialsid);
		} catch (CredentialsException e) {
			throw new MqManagerException("Unable to locate credentials for MQ Queue Manager with tag: " + tag);
		}
		
		if(credentials == null) {
			throw new MqManagerException("Unable to obtain credentials for namespace: " + credentialsid);
		}
		return credentials;
	}
	
	public Path getStoredArtifactRoot() {
		return this.storedArtifactsRoot;
	}
	
	private String getInstanceForTag(String tag) throws MqManagerException {
    	//obtain the instanceid for the tag
    	logger.trace("Obtaining instance ID for tag: " + tag);
    	String instanceid = InstanceForTag.get(tag);
    	if(instanceid == null) {
    		throw new MqManagerException("Could not find an instance for tag: " + tag);
    	}
    	return instanceid;
    }

}
