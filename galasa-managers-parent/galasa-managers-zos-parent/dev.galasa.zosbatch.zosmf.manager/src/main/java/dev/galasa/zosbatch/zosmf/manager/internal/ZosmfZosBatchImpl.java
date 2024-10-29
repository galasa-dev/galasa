/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJob.JobStatus;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobcard;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * Implementation of {@link IZosBatch} using zOS/MF
 *
 */
public class ZosmfZosBatchImpl implements IZosBatch {
    
    private List<ZosmfZosBatchJobImpl> zosBatchJobs = new ArrayList<>();
	private ZosmfZosBatchManagerImpl zosBatchManager;
    private IZosImage image;
    private static final Log logger = LogFactory.getLog(ZosmfZosBatchImpl.class);
    
    public ZosmfZosBatchImpl(ZosmfZosBatchManagerImpl zosBatchManager, IZosImage image) {
    	this.zosBatchManager = zosBatchManager;
        this.image = image;
    }
    
    @Override
    public @NotNull IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname) throws ZosBatchException {
        return submitJob(jcl, jobname, null);
    }
    
    @Override
    public @NotNull IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname, ZosBatchJobcard jobcard) throws ZosBatchException {
        if (jobname == null) {
            try {
				jobname = this.zosBatchManager.newZosBatchJobname(this.image);
			} catch (ZosBatchManagerException e) {
				throw new ZosBatchException(e);
			}
        }
        
        if (jobcard == null) {
            jobcard = new ZosBatchJobcard();
        }
        
        ZosmfZosBatchJobImpl zosBatchJob = new ZosmfZosBatchJobImpl(this.zosBatchManager, this.image, jobname, jcl, jobcard);
        this.zosBatchJobs.add(zosBatchJob);
        
        return zosBatchJob.submitJob();
    }


    @Override
    public List<IZosBatchJob> getJobs(String jobname, String owner) throws ZosBatchException {
        if (jobname != null) {
        	jobname = jobname.trim();
        	if (jobname.isEmpty() || jobname.length() > 8) {
        		throw new ZosBatchException("Jobname must be between 1 and 8 characters or null");
        	}
        }
        if (owner != null) {
        	owner = owner.trim();
        	if (owner.isEmpty() || owner.length() > 8) {
        		throw new ZosBatchException("Owner must be between 1 and 8 characters or null");
        	}
        }
        return getBatchJobs(jobname, owner);
    }

    /**
     * Clean up any existing batch jobs
     * @throws ZosBatchException
     */
    public void cleanup(boolean endOfTest) throws ZosBatchException {        
        Iterator<ZosmfZosBatchJobImpl> iterator = zosBatchJobs.iterator();
        while (iterator.hasNext()) {
            ZosmfZosBatchJobImpl zosBatchJobImpl = iterator.next();
            try {
				if (zosBatchJobImpl.submitted()) {
				    if (!zosBatchJobImpl.isComplete()) {
				    	if (zosBatchJobImpl.getStatus() != JobStatus.NOTFOUND && endOfTest) {
					    	zosBatchJobImpl.cancel();
					    	zosBatchJobImpl.archiveJobOutput();
					    	if (zosBatchJobImpl.shouldCleanup()) {
					    		zosBatchJobImpl.purge();
					    	}
					        iterator.remove();
				    	}
				    } else {
				    	if (zosBatchJobImpl.getStatus() != JobStatus.NOTFOUND) {
					        if (!zosBatchJobImpl.isArchived()) {
					            zosBatchJobImpl.archiveJobOutput();
					        }
					        if (!zosBatchJobImpl.isPurged() && zosBatchJobImpl.shouldCleanup()) {
					            zosBatchJobImpl.purge();
					        }
				    	}
			            iterator.remove();
				    }
				}
			} catch (ZosBatchException e) {
				logger.error("Problem in cleanup phase", e);
			}
        }
    }

    protected List<IZosBatchJob> getBatchJobs(String suppliedJobname, String suppliedOwner) throws ZosBatchException {
        IZosmfRestApiProcessor zosmfApiProcessor;
        try {
            zosmfApiProcessor = this.zosBatchManager.getZosmfManager().newZosmfRestApiProcessor(image, this.zosBatchManager.getZosManager().getZosBatchPropertyBatchRestrictToImage(image.getImageID()));
        } catch (ZosmfManagerException | ZosBatchManagerException e) {
            throw new ZosBatchException(e);
        }
        String jobnameQueryString = suppliedJobname == (null) ? "prefix=*" : "prefix=" + suppliedJobname;
        String ownerQueryString = suppliedOwner == (null) ? "" : "owner=" + suppliedOwner;
        String listJobsPath = ZosmfZosBatchJobImpl.RESTJOBS_PATH + "?" + jobnameQueryString + "&" + ownerQueryString;
        HashMap<String, String> headers = new HashMap<>();
        IZosmfResponse response;
        try {
            response = zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, listJobsPath, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
        
        List<IZosBatchJob> zosBatchJobList = new ArrayList<>();

        try {
        	logger.trace(response.getContent());
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
        
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            // Get the jobs
            JsonArray jsonArray;
            try {
                jsonArray = response.getJsonArrayContent();
            } catch (ZosmfException e) {
                throw new ZosBatchException(e);
            }
            for (JsonElement jsonElement : jsonArray) {
            	JsonObject responseBody = jsonElement.getAsJsonObject();
                String jobnameString = responseBody.get("jobname").getAsString();
                IZosBatchJobname jobname = this.zosBatchManager.newZosBatchJobname(jobnameString);
                ZosmfZosBatchJobImpl zosBatchJob = new ZosmfZosBatchJobImpl(this.zosBatchManager, this.image, jobname, null, null);
                zosBatchJob.setJobid(responseBody.get("jobid").getAsString());
                zosBatchJob.setOwner(responseBody.get("owner").getAsString());
                zosBatchJob.setType(responseBody.get("type").getAsString());
                zosBatchJob.setStatusString(responseBody.get("status").getAsString());
                zosBatchJob.setJobPathValues();
                zosBatchJobList.add(zosBatchJob);
            }
        } else {
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
        	JsonObject responseBody;
        	String displayMessage;
			try {
				responseBody = response.getJsonContent();
	            displayMessage = ZosmfZosBatchJobImpl.buildErrorString("List jobs", responseBody);
			} catch (ZosmfException e) { 
	            try {
	            	displayMessage = "Error with List jobs. Output: " + response.getTextContent();
				} catch (ZosmfException e1) {
					displayMessage = "Error with List jobs";
				}
			} 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
        return zosBatchJobList;
    }

}
