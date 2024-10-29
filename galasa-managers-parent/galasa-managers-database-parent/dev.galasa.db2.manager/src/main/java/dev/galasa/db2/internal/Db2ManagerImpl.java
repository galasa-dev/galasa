/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.db2.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.db2.Db2Instance;
import dev.galasa.db2.Db2ManagerException;
import dev.galasa.db2.Db2ManagerField;
import dev.galasa.db2.Db2Schema;
import dev.galasa.db2.IDb2Instance;
import dev.galasa.db2.IDb2Schema;
import dev.galasa.db2.internal.properties.Db2PropertiesSingleton;
import dev.galasa.db2.spi.IDb2ManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

/**
 * Db2 Manager Impl
 * 
 * Provides two annotations, one for a Db2 Instance connections and one for
 * a Schema impl
 * 
 *  
 *
 */
@Component(service = { IManager.class })
public class Db2ManagerImpl extends AbstractManager implements IDb2ManagerSpi{
	private IFramework 							framework;
	
	private Map<String,Db2InstanceImpl> 					connections = new HashMap<>();
	
	protected final String               		NAMESPACE = "db2";
    private final static Log                    logger = LogFactory.getLog(Db2ManagerImpl.class);
    
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
    	super.initialise(framework, allManagers, activeManagers, galasaTest);
    	this.framework = framework;
    	
    	if(galasaTest.isJava()) {
    		List<AnnotatedField> annotatedFields = findAnnotatedFields(Db2ManagerField.class);
    		if(!annotatedFields.isEmpty()) {
    			youAreRequired(allManagers, activeManagers, galasaTest);
    		}
    	}
    	try {
    		Db2PropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
    	} catch(ConfigurationPropertyStoreException e) {
    		throw new Db2ManagerException("Failed to set CPS for the 'db2' namespace", e);
    	}
    	logger.info("Db2 manager initialised");
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
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		logger.info("Db2 Manager provision Generating");
		List<AnnotatedField> annotatedFields = findAnnotatedFields(Db2ManagerField.class);
    	
    	for(AnnotatedField annotatedField : annotatedFields) {
    		Field field = annotatedField.getField();
    		if(field.getType() == IDb2Instance.class) {
    			Db2Instance annotation = field.getAnnotation(Db2Instance.class);
    			if(annotation != null) {
    				generateDb2Instance(field, annotatedField.getAnnotations());
    			}
    			
    		}
    	}
        generateAnnotatedFields(Db2ManagerField.class);
	}
	
	@Override
	public void provisionDiscard() {
		for (String db2Tag : connections.keySet()) {
			logger.info("Closing connection to " + db2Tag);
			try {
				connections.get(db2Tag).getConnection().close();
			} catch (SQLException e) {
				logger.error("Failed to close connection", e);
			}
		}
	}
	
	@GenerateAnnotatedField(annotation = Db2Instance.class)
	public IDb2Instance generateDb2Instance(Field field, List<Annotation> annotations) throws Db2ManagerException {
		Db2Instance annotation = field.getAnnotation(Db2Instance.class);
		
		Db2InstanceImpl instance = new Db2InstanceImpl(framework, this, annotation.tag());
		connections.put(annotation.tag(), instance);
		registerAnnotatedField(field, instance);
		return instance;
	}
	
	@GenerateAnnotatedField(annotation = Db2Schema.class)
	public IDb2Schema generateDb2Schema(Field field, List<Annotation> annotations) throws Db2ManagerException {
		Db2Schema annotation = field.getAnnotation(Db2Schema.class);
		
		Db2InstanceImpl conn = connections.get(annotation.db2Tag());
		if (conn == null) {
			throw new Db2ManagerException("Requested db2 connection not valid. Please define the Db2 instance tagged: " + annotation.tag());
		}
		
		IDb2Schema schema = new Db2SchemaImpl(framework, conn, annotation.tag(), annotation.archive(), annotation.resultSetType(), annotation.resultSetConcurrency());
		registerAnnotatedField(field, schema);
		return schema;
	}
	
	
	// SPI Methods 
	@Override
	public IDb2Instance getInstanceFromTag(String tag) throws Db2ManagerException {
		return new Db2InstanceImpl(framework, this, tag);
	}

	@Override
	public IDb2Schema getSchemaFromTag(String tag, String db2Tag) throws Db2ManagerException {
		return getSchemaFromTag(tag, db2Tag, false, ResultSet.CONCUR_READ_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE);
	}

	@Override
	public IDb2Schema getSchemaFromTag(String tag, String db2Tag, boolean archive) throws Db2ManagerException {
		return getSchemaFromTag(tag, db2Tag, archive, ResultSet.CONCUR_READ_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE);
	}

	@Override
	public IDb2Schema getSchemaFromTag(String tag, String db2Tag, boolean archive, int resultSetType,
			int resultSetConcurrency) throws Db2ManagerException {
		Db2InstanceImpl instance = new Db2InstanceImpl(framework, this, db2Tag);
		connections.put(db2Tag, instance);
		
		return new Db2SchemaImpl(framework, instance, tag, archive, resultSetType, resultSetConcurrency);
		
	}
}