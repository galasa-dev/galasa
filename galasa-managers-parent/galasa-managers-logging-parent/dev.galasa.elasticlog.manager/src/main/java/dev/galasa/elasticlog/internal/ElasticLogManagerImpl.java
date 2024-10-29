/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.elasticlog.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ManagerException;
import dev.galasa.elasticlog.internal.properties.ElasticLogCredentials;
import dev.galasa.elasticlog.internal.properties.ElasticLogEndpoint;
import dev.galasa.elasticlog.internal.properties.ElasticLogIndex;
import dev.galasa.elasticlog.internal.properties.ElasticLogLocalRun;
import dev.galasa.elasticlog.internal.properties.ElasticLogPropertiesSingleton;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.ILoggingManager;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;

/**
 * ElasticLog Manager implementation
 * 
 *  
 */
@Component(service = { IManager.class })
public class ElasticLogManagerImpl extends AbstractManager {

	private static final Log					logger			= LogFactory.getLog(ElasticLogManagerImpl.class);
	public final static String					NAMESPACE		= "elasticlog";
    
	private IFramework							framework;
	private IConfigurationPropertyStoreService	cps;
	private IConfidentialTextService			ctf;
	
	private ICredentialsService                 credService;

	private List<IManager>						otherManagers	= new ArrayList<IManager>();

	private IHttpManagerSpi						httpManager;

	private HashMap<String, Object>				runProperties	= new HashMap<String, Object>(); 

	/**
	 * Initialise the ElasticLogManager, adding a pointer to the other active managers
	 *  
	 * @param framework - the galasa framework
	 * @param allManagers - list of all the managers
	 * @param activeManagers - list of all the active managers
	 * @throws ManagerException
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, galasaTest);

		try {
			this.framework = framework;
			this.cps = framework.getConfigurationPropertyService(NAMESPACE);
			this.ctf = framework.getConfidentialTextService();
			this.credService = framework.getCredentialsService();
			ElasticLogPropertiesSingleton.setCps(this.cps);
		} catch (Exception e) {
			throw new ElasticLogManagerException("Unable to request framework services", e);
		}
		
		if(!framework.getTestRun().isLocal() || ElasticLogLocalRun.get().equals("true"))
			youAreRequired(allManagers, activeManagers, galasaTest);
		
		this.otherManagers = activeManagers;
	}
    
	/**
	 * Makes sure that the elastic log manager is added to the list of active managers, and adds the dependency on http manager.
	 * 
	 * @param allManagers - list of all the managers
	 * @param activeManagers - list of the active managers
	 * @throws ManagerException
	 */
	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
			throws ManagerException {
		if (activeManagers.contains(this))
			return;
		activeManagers.add(this);

		httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
	}
	
    @Override
    public boolean doYouSupportSharedEnvironments() {
        return true;
    }

	/**
	 * Test class result step, build and send the document request
	 * 
	 * @throws ManagerException
	 * @throws CredentialsException 
	 */
	@Override
	public void testClassResult(@NotNull String finalResult, Throwable finalException) throws ManagerException {
	    
		//Record test information
		this.runProperties.put("testCase", this.framework.getTestRun().getTestClassName());
		this.runProperties.put("runId", this.framework.getTestRunName());
		this.runProperties.put("startTimestamp", Date.from(this.framework.getTestRun().getQueued()));
		this.runProperties.put("endTimestamp", Date.from(Instant.now()));
		this.runProperties.put("requestor", this.framework.getTestRun().getRequestor());
		this.runProperties.put("result", finalResult);
	    
		String testTooling        = "Galasa";
		String testType           = "Galasa";
		String testingEnvironment = "NOT_ASSIGNED";
		String productRelease     = null;
		String buildLevel         = null;
		String customBuild        = null;
		List<String> testingAreas = new ArrayList<String>();
		List<String> tags		  = new ArrayList<String>();
	
		//Ask other managers for additional logging information
		for(IManager manager : this.otherManagers) {
			if(manager instanceof ILoggingManager) {
				ILoggingManager loggingManager = (ILoggingManager) manager;
	    		
				String tooling = loggingManager.getTestTooling();
				if(tooling != null)
					testTooling = tooling;
	    		
				String type = loggingManager.getTestType();
				if(type != null)
					testType = type;
	    		
				String environment = loggingManager.getTestingEnvironment();
				if(environment != null)
					testingEnvironment = environment;
	    		
				String release = loggingManager.getProductRelease();
				if(release != null)
					productRelease = release;
	    		
				String level = loggingManager.getBuildLevel();
				if(level != null)
					buildLevel = level;
	    		
				String custom = loggingManager.getCustomBuild();
				if(custom != null)
					customBuild = custom;
	    		
				List<String> areas = loggingManager.getTestingAreas();
				if(areas != null)
					testingAreas.addAll(areas);
	    		
				List<String> tagList = loggingManager.getTags();
				if(tagList != null)
					tags.addAll(tagList);
			}
		}
		if (testingAreas.isEmpty())
			testingAreas = null;
	    
		if (tags.isEmpty())
			tags = null;
	    
		this.runProperties.put("testTooling", testTooling);
		this.runProperties.put("testType", testType);
		this.runProperties.put("testingEnvironment", testingEnvironment);
		this.runProperties.put("productRelease", productRelease);
		this.runProperties.put("buildLevel", buildLevel);
		this.runProperties.put("customBuild", customBuild);
	
		if(testingAreas != null)
			this.runProperties.put("testingAreas", testingAreas.toArray(new String[testingAreas.size()]));
	
		if(tags != null)
			this.runProperties.put("tags", tags.toArray(new String[tags.size()]));	    	
	
		//Convert HashMap of run properties to a Json String
		Gson gson = new GalasaGsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").getGson();
		JsonObject json = gson.toJsonTree(this.runProperties).getAsJsonObject();

		logger.info("Sending Run Request to ElasticLog Endpoint");
		logger.trace("Document Request -\n" + json.toString());
		
		//Register endpoint data as confidential
		String index = ElasticLogIndex.get();
		String endpoint = ElasticLogEndpoint.get();
		ctf.registerText(index, "ElasticLog Index");
		ctf.registerText(endpoint, "ElasticLog Endpoint");
		try {
		   
		    ICredentials creds = getCreds();
		   
			//Set up http client for requests
			IHttpClient client = this.httpManager.newHttpClient();
			client.setTrustingSSLContext();
			client.addOkResponseCode(201);
			client.setURI(new URI(endpoint));
			
	        if(creds != null && creds instanceof ICredentialsUsernamePassword) {
	              
	              ICredentialsUsernamePassword userPass = (ICredentialsUsernamePassword) creds;
	              String user = userPass.getUsername();
	              String pass = userPass.getPassword();
	              client.setAuthorisation(user, pass);
	           }
			
			
			HttpClientResponse<JsonObject> response = client.postJson(index + "/_doc", json);
			
			int statusCode = response.getStatusCode();
			String message = response.getStatusMessage();
			
			//Send document to index and check response code
			if(statusCode != 201 && statusCode != 200){
			   logger.warn("Error logging to Elastic index " + index + ": " + statusCode + " - " + message);
			}else {
			  logger.info("Run successfully logged to Elastic index " + index); 
			}
        
			//Change index to latest document index
			index = index + "_latest";
		 	String testCase = (String) this.runProperties.get("testCase");
		 	
		 	//Create new doc if doesnt exist, updates if doc already exists
		 	
		 	response = client.postJson(index + "/_doc/" + testCase + testingEnvironment, json);
		 	
			statusCode = response.getStatusCode();
			message = response.getStatusMessage();
			
			if(statusCode != 201 && statusCode != 200) {
			   logger.warn("Error logging to Elastic index " + index + ": " + statusCode + " - " + message);
			}else {
			  logger.info("Run successfully logged to Elastic index " + index); 
			}

		} catch (HttpClientException e) {
			logger.warn("ElasticLog Manager failed to send information to Elastic Endpoint: ", e);
		} catch (URISyntaxException e) {
			logger.warn("ElasticLog Manager failed to send parse URI of Elastic Endpoint: ", e);
		}catch (CredentialsException e) {
		    logger.warn("Problem retrieving credentials: ", e);
		}
	}    
	
	private ICredentials getCreds() throws CredentialsException, ElasticLogManagerException{
	   String credKey = ElasticLogCredentials.get();
	   ICredentials creds = credService.getCredentials(credKey);
	   return creds;
	}
}