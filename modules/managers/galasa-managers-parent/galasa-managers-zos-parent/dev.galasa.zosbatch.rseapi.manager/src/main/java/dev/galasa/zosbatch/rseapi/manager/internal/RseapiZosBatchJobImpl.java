/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.rseapi.manager.internal;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutput;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobcard;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.spi.IZosBatchJobOutputSpi;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * Implementation of {@link IZosBatchJob} using zOS/MF
 *
 */
public class RseapiZosBatchJobImpl implements IZosBatchJob {
    
    private IRseapiRestApiProcessor rseapiApiProcessor;    
    private RseapiZosBatchManagerImpl zosBatchManager;

	private IZosImage jobImage;
    private IZosBatchJobname jobname;
    private final ZosBatchJobcard jobcard;
    private String jcl;
    private int jobWaitTimeout;
    
    private String jobid;         
    private String owner;         
    private String type;         
    private JobStatus status;         
    private String statusString;
    private boolean jobNotFound;
    private String retcode;
    private boolean jobComplete;
    private boolean outputComplete;
    private boolean jobArchived;
    private boolean jobPurged;
    private String jobPath;
    private String jobFilesPath;
    private IZosBatchJobOutputSpi jobOutput;
    private boolean useSysaff;
    
    private boolean shouldArchive = true;

    private boolean shouldCleanup = true;

	private static final String PROP_REASON = "reason";
    private static final String PROP_RC = "rc";
    private static final String PROP_CATEGORY = "category";
    private static final String PROP_JOBID = "jobId";
    private static final String PROP_OWNER = "owner";
    private static final String PROP_TYPE = "type";
    private static final String PROP_RETCODE = "returnCode";
    private static final String PROP_ID = "id";
    private static final String PROP_CONTENT = "content";
	private static final String PROP_STATUS = "status";
    
    private static final String SLASH = "/";
    private static final String QUERY = "?";
    protected static final String RESTJOBS_PATH = SLASH + "rseapi" + SLASH + "api" + SLASH + "v1" + SLASH + "jobs";
    
    private static final String LOG_JOB_NOT_SUBMITTED = "Job has not been submitted by manager";
    
    private static final Log logger = LogFactory.getLog(RseapiZosBatchJobImpl.class);

    protected static final List<Integer> VALID_STATUS_CODES = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK,
                                                                                          HttpStatus.SC_CREATED,
                                                                                          HttpStatus.SC_NO_CONTENT,
                                                                                          HttpStatus.SC_BAD_REQUEST,
                                                                                          HttpStatus.SC_UNAUTHORIZED,
                                                                                          HttpStatus.SC_FORBIDDEN,
                                                                                          HttpStatus.SC_NOT_FOUND,
                                                                                          HttpStatus.SC_INTERNAL_SERVER_ERROR
                                                                                          ));

    public RseapiZosBatchJobImpl(RseapiZosBatchManagerImpl zosBatchManager, IZosImage jobImage, IZosBatchJobname jobname, String jcl, ZosBatchJobcard jobcard) throws ZosBatchException {
    	this.zosBatchManager = zosBatchManager;
        this.jobImage = jobImage;
        this.jobname = jobname;
        if (jobcard != null) {
            this.jobcard = jobcard;
        } else {
            this.jobcard = new ZosBatchJobcard();
        }
        if (jcl != null) {
        	Path artifactPath = this.zosBatchManager.getCurrentTestMethodArchiveFolder();
        	try {
				this.zosBatchManager.getZosManager().storeArtifact(artifactPath.resolve(this.zosBatchManager.getZosManager().buildUniquePathName(artifactPath, this.jobname.getName() + "_supplied_JCL")), jcl, ResultArchiveStoreContentType.TEXT);
			} catch (ZosManagerException e) {
				throw new ZosBatchException(e);
			}
            this.jcl = jcl;

            try {
                this.useSysaff = this.zosBatchManager.getZosManager().getZosBatchPropertyUseSysaff(this.jobImage.getImageID());
            } catch (ZosBatchManagerException e) {
                throw new ZosBatchException("Unable to get use SYSAFF property value", e);
            }
        }
        
        try {
            this.jobWaitTimeout = this.zosBatchManager.getZosManager().getZosBatchPropertyJobWaitTimeout(this.jobImage.getImageID());
        } catch (ZosBatchManagerException e) {
            throw new ZosBatchException("Unable to get job timeout property value", e);
        }
        
        try {
            this.rseapiApiProcessor = this.zosBatchManager.getRseapiManager().newRseapiRestApiProcessor(jobImage, this.zosBatchManager.getZosManager().getZosBatchPropertyBatchRestrictToImage(jobImage.getImageID()));
        } catch (RseapiManagerException | ZosBatchManagerException e) {
            throw new ZosBatchException(e);
        }
    }
    
    public IZosBatchJob submitJob() throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        IRseapiResponse response;
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("jcl", jclWithJobcard());
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.POST_JSON, RESTJOBS_PATH + SLASH + "string" + SLASH, headers, requestBody, VALID_STATUS_CODES, true);
        } catch (RseapiException | ZosBatchManagerException e) {
            throw new ZosBatchException(e);
        }

        if (response.getStatusCode() == HttpStatus.SC_CREATED) {
            JsonObject responseBody;
            try {
                responseBody = response.getJsonContent();
            } catch (RseapiException e) {
                throw new ZosBatchException(e);
            }
        
            logger.trace(responseBody);
            this.jobid = jsonNull(responseBody, PROP_JOBID);
            this.owner = jsonNull(responseBody, PROP_OWNER);
            this.type = jsonNull(responseBody, PROP_TYPE);
            this.retcode = jsonNull(responseBody, PROP_RETCODE);
            setJobPathValues();
            logger.info("JOB " + this.toString() + " Submitted");
        } else {            
            // Error case
            String displayMessage = buildErrorString("Submit job", response); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
        
        return this;
    }
    
    @Override
    public IZosBatchJobname getJobname() {
        return this.jobname;
    }
    
    @Override
    public String getJobId() {
    	if (this.jobid == null) {
        	try {
    			updateJobStatus();
    		} catch (ZosBatchException e) {
    			logger.error(e);
    		}
    	}
        return (this.jobid != null ? this.jobid : StringUtils.repeat(QUERY, 8));
    }
    
    @Override
    public String getOwner() {
    	if (this.owner == null) {
        	try {
    			updateJobStatus();
    		} catch (ZosBatchException e) {
    			logger.error(e);
    		}
    	}
        return (this.owner != null ? this.owner : StringUtils.repeat(QUERY, 8));
    }
    
    @Override
    public String getType() {
    	if (this.type == null) {
        	try {
    			updateJobStatus();
    		} catch (ZosBatchException e) {
    			logger.error(e);
    		}
    	}
        return (this.type != null ? this.type : StringUtils.repeat(QUERY, 3));
    }
    
    @Override
    public JobStatus getStatus() {
    	if (this.status != JobStatus.OUTPUT) {
	    	try {
				updateJobStatus();
			} catch (ZosBatchException e) {
				logger.error(e);
			}
    	}
    	return (this.status != null ? this.status : JobStatus.UNKNOWN);
    }
    
    @Override
    public String getStatusString() {
    	return getStatus().toString();
    }
    
    @Override
    public String getRetcode() {
    	if (this.retcode == null || this.retcode.equals(StringUtils.repeat(QUERY, 4))) {
        	try {
    			updateJobStatus();
    		} catch (ZosBatchException e) {
    			logger.error(e);
    		}
    	}
        return (this.retcode != null ? this.retcode : StringUtils.repeat(QUERY, 4));
    }

    @Override
    public int waitForJob() throws ZosBatchException {
    	return waitForJob(jobWaitTimeout);
    }

    @Override
    public int waitForJob(long milliSecondTimeout) throws ZosBatchException {
        if (!submitted()) {
            throw new ZosBatchException(LOG_JOB_NOT_SUBMITTED);
        }
        logger.info("Waiting up to " + milliSecondTimeout + " second(s) for "+ this.jobid + " " + this.jobname.getName() + " to complete");
        
        LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(milliSecondTimeout);
        while (LocalDateTime.now().isBefore(timeoutTime)) {
            updateJobStatus();
            if (this.jobNotFound) {
                return Integer.MIN_VALUE;
            }
            try {
                if (isComplete()) {
                    String[] rc = this.retcode.split(" ");
                    if (rc.length == 2) {
                        return StringUtils.isNumeric(rc[1]) ? Integer.parseInt(rc[1]) : Integer.MIN_VALUE;
                    }
                    return Integer.MIN_VALUE;
                }
                
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("waitForJob Interrupted", e);
                Thread.currentThread().interrupt();
                throw new ZosBatchException(e);
            }
        }
        return Integer.MIN_VALUE;
    }
    
    @Override
	public IZosBatchJobOutput listSpoolFiles() throws ZosBatchException {
        if (!this.outputComplete) {
            getOutput(false);
        }
        
        return jobOutput();
	}
    
    @Override
    public IZosBatchJobOutput retrieveOutput() throws ZosBatchException {
        if (!this.outputComplete) {
            getOutput(true);
        }
        
        return jobOutput();
    }

	@Override
	public String retrieveOutputAsString() throws ZosBatchException {
		StringBuilder output = new StringBuilder();
		retrieveOutput().getSpoolFiles().forEach(records -> output.append(records.getRecords()));
        return output.toString();		
	}
    
    @Override
    public void cancel() throws ZosBatchException {
        if (!isComplete()) {
            cancel(false);
        }
    }

    @Override
    public void purge() throws ZosBatchException {
        if (!isPurged()) {
            cancel(true);
        }
    }
    
    @Override
    public IZosBatchJobOutputSpoolFile getSpoolFile(String ddname) throws ZosBatchException {
        Iterator<IZosBatchJobOutputSpoolFile> spoolFilesIterator = listSpoolFiles().iterator();
        while (spoolFilesIterator.hasNext()) {
            IZosBatchJobOutputSpoolFile spoolFile = spoolFilesIterator.next();
            if (spoolFile.getDdname().equals(ddname)) {
            	String records = getOutputFileContent(this.jobFilesPath + "/" + spoolFile.getId() + "/content"); 
            	if (records != null) {
            		return this.zosBatchManager.getZosManager().newZosBatchJobOutputSpoolFile(this, spoolFile.getJobname(), spoolFile.getJobid(), spoolFile.getStepname(), spoolFile.getProcstep(), ddname, spoolFile.getId(), records);
            	}
                throw new ZosBatchException("DDNAME " + ddname + " is empty or not found");
            }
        }
        return null;
    }

    @Override
    public void saveOutputToResultsArchive(String rasPath) throws ZosBatchException {
        if (!this.outputComplete) {
            retrieveOutput();
        }
        Path artifactPath = this.zosBatchManager.getArtifactsRoot().resolve(rasPath).resolve(jobOutput().getJobname());
		logger.info("Archiving batch job " + this.toString() + " to " + artifactPath.toString());
        
		Iterator<IZosBatchJobOutputSpoolFile> iterator = jobOutput().iterator();
        while (iterator.hasNext()) {
            saveSpoolFile(iterator.next(), artifactPath);
        }
        if (isComplete()) {
        	this.jobArchived = true;
        }
    }

    @Override
	public void setShouldArchive(boolean shouldArchive) {
		this.shouldArchive = shouldArchive;
	}

	@Override
	public boolean shouldArchive() {
		return this.shouldArchive;
	}

    @Override
	public void setShouldCleanup(boolean shouldCleanup) {
		this.shouldCleanup = shouldCleanup;
	}

	@Override
	public boolean shouldCleanup() {
		return this.shouldCleanup;
	}

	@Override
	public void saveSpoolFileToResultsArchive(IZosBatchJobOutputSpoolFile spoolFile, String rasPath) throws ZosBatchException {
        Path artifactPath = this.zosBatchManager.getArtifactsRoot().resolve(rasPath);
		logger.info("Archiving spool file " + spoolFile.getDdname() + " to " + artifactPath.toString());
		saveSpoolFile(spoolFile, artifactPath);
	}

	protected void saveSpoolFile(IZosBatchJobOutputSpoolFile spoolFile, Path artifactPath) throws ZosBatchException {
        StringBuilder name = new StringBuilder();
        name.append(spoolFile.getJobid());
        if (!spoolFile.getStepname().isEmpty()){
            name.append("_");
            name.append(spoolFile.getStepname());
        }
        if (!spoolFile.getProcstep().isEmpty()){
            name.append("_");
            name.append(spoolFile.getProcstep());
        }
        name.append("_");
        name.append(spoolFile.getDdname());
        String fileName = this.zosBatchManager.getZosManager().buildUniquePathName(artifactPath, name.toString());
        try {
			this.zosBatchManager.getZosManager().storeArtifact(artifactPath.resolve(fileName), spoolFile.getRecords(), ResultArchiveStoreContentType.TEXT);
		} catch (ZosManagerException e) {
			throw new ZosBatchException(e);
		}
	}

	protected void getOutput(boolean retrieveRecords) throws ZosBatchException {
    
        if (!submitted()) {
            throw new ZosBatchException(LOG_JOB_NOT_SUBMITTED);
        }
        updateJobStatus();
        if (this.jobNotFound) {
            return;
        }
        
        // First, get a list of spool files
        this.jobFilesPath = RESTJOBS_PATH + SLASH + this.jobname.getName() + SLASH + this.jobid + "/files";
        HashMap<String, String> headers = new HashMap<>();
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, this.jobFilesPath, headers, null, VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosBatchException(e);
        }
        
        if (response.getStatusCode() == HttpStatus.SC_OK) {   
            Object responseBodyObject;
            try {
                responseBodyObject = response.getContent();
            } catch (RseapiException e) {
                throw new ZosBatchException(e);
            }
            
            logger.trace(responseBodyObject);
            // Get the spool files
            JsonArray jsonArray = ((JsonObject) responseBodyObject).getAsJsonArray("items");
            for (JsonElement jsonElement : jsonArray) {
                JsonObject responseBody = jsonElement.getAsJsonObject();
                String id = jsonNull(responseBody, PROP_ID);
                String stepname = jsonNull(responseBody, "step name");
                String procstep = jsonNull(responseBody, "proc step");
                String ddname = responseBody.get("ddName").getAsString();
                String records = null;
                if (retrieveRecords) {
                	records = getOutputFileContent(this.jobFilesPath + "/" + id + "/content");
                }
                if(this.jobOutput == null) {
                	this.jobOutput = this.zosBatchManager.getZosManager().newZosBatchJobOutput(this, this.jobname.getName(), this.jobid);
                }
                ((IZosBatchJobOutputSpi) this.jobOutput).addSpoolFile(stepname, procstep, ddname, id, records);
            }
        } else {            
            // Error case
            String displayMessage = buildErrorString("Retrieve job output", response); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
        
        if (this.jobComplete && retrieveRecords) {
            this.outputComplete = true;
        }
    }

    protected void setJobid(String jobid) {
        this.jobid = jobid;
    }
    
    protected void setOwner(String owner) {
        this.owner = owner;
    }
    
    protected void setType(String type) {
        this.type = type;
    }
    
    protected void setStatusString(String statusString) {
        this.statusString = statusString;
        setStatus(statusString);
    }
    
    protected void setStatus(String statusString) {
    	//RSE API Status: HOLD | ACTIVE | ABEND | COMPLETED | COMPLETION | NOT_FOUND
    	if (statusString == null || statusString.equals(StringUtils.repeat(QUERY, 8))) {
    		this.status = JobStatus.UNKNOWN;
    	} else if (statusString.equals("HOLD")) {
    		this.status = JobStatus.INPUT;
    	} else if (statusString.equals("ACTIVE")) {
    		this.status = JobStatus.ACTIVE;
    	} else if (statusString.equals("COMPLETION") || statusString.equals("COMPLETED") || statusString.equals("ABEND")) {
    		this.status = JobStatus.OUTPUT;
    	} else if (statusString.equals("NOT_FOUND")) {
    		this.status = JobStatus.NOTFOUND;
    	}
    }

    protected void cancel(boolean purge) throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        IRseapiResponse response;
        try {
            if (purge) {
                response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.DELETE, this.jobPath, headers, null, VALID_STATUS_CODES, true);
            } else {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("request", "cancel");
                response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.PUT_JSON, this.jobPath, headers, requestBody, VALID_STATUS_CODES, true);
            }
        } catch (RseapiException e) {
            throw new ZosBatchException(e);
        }

        if (response.getStatusCode() == HttpStatus.SC_OK || response.getStatusCode() == HttpStatus.SC_NOT_FOUND || response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            this.status = null;
            if (purge) {
                this.jobPurged = true; 
            } else {
                this.jobComplete = true;
            }
        } else {            
            // Error case
            String displayMessage = buildErrorString(purge ? "Purge job" : "Cancel job", response);
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
    }
        
    @Override
    public String toString() {
    	return this.jobname.getName() + "(" + this.getJobId() + ")";
    }

    protected boolean submitted() {
    	return !getJobId().contains("?");
    }
    
    protected boolean isComplete() {
        return this.jobComplete;
    }

    protected boolean isArchived() {
        return this.jobArchived;
    }

    protected boolean isPurged() {
        return this.jobPurged;
    }

    protected IZosBatchJobOutput jobOutput() throws ZosBatchException {        
        if (this.jobOutput == null) {
        	this.jobOutput = this.zosBatchManager.getZosManager().newZosBatchJobOutput(this, this.jobname.getName(), this.jobid);
        	retrieveOutput();
        }
        return this.jobOutput;
    }

    protected String jobStatus() {
        return "JOBID=" + this.jobid + 
              " JOBNAME=" + this.jobname.getName() + 
              " OWNER=" + this.owner + 
              " TYPE=" + this.type +
              " STATUS=" + this.status + 
              " RETCODE=" + this.retcode;
    }

    protected void setJobPathValues() {
        this.jobPath = RESTJOBS_PATH + SLASH + this.jobname.getName() + SLASH + this.jobid;
        this.jobFilesPath = this.jobPath + "/files";
    }

    protected void updateJobStatus() throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, RESTJOBS_PATH + SLASH + this.jobname.getName() + "/" + this.jobid, headers, null, VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosBatchException(e);
        }
        
        if (response.getStatusCode() == HttpStatus.SC_OK || response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            JsonObject responseBody;
            try {
                responseBody = response.getJsonContent();
            } catch (RseapiException e) {
                throw new ZosBatchException(e);
            }
        
            logger.trace(responseBody);
            this.jobNotFound = false;
            this.owner = jsonNull(responseBody, PROP_OWNER);
            this.type = jsonNull(responseBody, PROP_TYPE);
            this.statusString = jsonNull(responseBody, PROP_STATUS);

            // Update the completion status of this batch job
            RseapiJobStatus status = RseapiJobStatus.getJobStatusFromString(this.statusString);
            this.jobComplete = status.isComplete();

            if (status == RseapiJobStatus.NOTFOUND) {
                logger.trace("JOBID=" + this.jobid + " JOBNAME=" + this.jobname.getName() + " NOT FOUND");
                this.jobNotFound = true;
                this.status = JobStatus.NOTFOUND;
            }
            setStatus(this.statusString);

            String retcodeProperty = jsonNull(responseBody, PROP_RETCODE);
            if (retcodeProperty != null) {
                this.retcode = retcodeProperty;
            } else {
                this.retcode = StringUtils.repeat(QUERY, 4);
            }
            logger.trace(jobStatus());
        } else {
            // Error case
            String displayMessage = buildErrorString("Update job status", response); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }            
    }

    protected String getOutputFileContent(String path) throws ZosBatchException {    
        HashMap<String, String> headers = new HashMap<>();
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, path, headers, null, VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosBatchException(e);
        }
    
        String fileOutput;
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            JsonObject responseBody;
            try {
                responseBody = response.getJsonContent();
            } catch (RseapiException e) {
                throw new ZosBatchException(e);
            }
        
            logger.debug(responseBody);
            fileOutput = jsonNull(responseBody, PROP_CONTENT);
        } else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND && getStatus().equals(JobStatus.ACTIVE)) {
        	return null;
        } else {            
            // Error case
            String displayMessage = buildErrorString("Retrieve job output", response);
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
        return fileOutput;
    }

    protected boolean spoolFileNotFound(JsonObject errorResponseBody) {
        return (jsonZero(errorResponseBody, PROP_CATEGORY) == 6 &&
                jsonZero(errorResponseBody, PROP_RC) == 4 &&
                jsonZero(errorResponseBody, PROP_REASON) == 12);
    }

    protected String jclWithJobcard() throws ZosBatchManagerException {
        StringBuilder jclWithJobcard = new StringBuilder();
        jclWithJobcard.append(this.jobcard.getJobcard(this.jobname.getName(), this.jobImage));
        
        if (this.useSysaff) {
            jclWithJobcard.append("/*JOBPARM SYSAFF=");
            jclWithJobcard.append(this.jobImage.getImageID());
            jclWithJobcard.append("\n");
        }
        
        jclWithJobcard.append(jcl);
        if (!jclWithJobcard.toString().endsWith("\n")) {
            jclWithJobcard.append("\n");
        }
        return jclWithJobcard.toString();
    }

    protected String jsonNull(JsonObject responseBody, String memberName) {
        if (responseBody.get(memberName) != null && !responseBody.get(memberName).isJsonNull()) {
            return responseBody.get(memberName).getAsString();
        }
        return null;
    }

    protected int jsonZero(JsonObject responseBody, String memberName) {
        if (responseBody.get(memberName) != null && !responseBody.get(memberName).isJsonNull()) {
            return responseBody.get(memberName).getAsInt();
        }
        return 0;
    }
    
    protected static String buildErrorString(String action, IRseapiResponse response) {
    	String message = "";
    	try {
    		Object content = response.getContent();
			if (content != null) {
				logger.trace(content);
				if (content instanceof JsonObject) {
					message = "\nstatus: " + ((JsonObject) content).get("status").getAsString() + "\n" + "message: " + ((JsonObject) content).get("message").getAsString(); 
				} else if (content instanceof String) {
					message = " response body:\n" + content;
				}
			}
		} catch (RseapiException e) {
			// NOP
		}
        return "Error " + action + ", HTTP Status Code " + response.getStatusCode() + " : " + response.getStatusLine() + message;
    }

    protected void archiveJobOutput() throws ZosBatchException {
        if (shouldArchive() && getStatus() != JobStatus.NOTFOUND && (!isArchived() || !this.jobComplete)) {
        	retrieveOutput();
            Path rasPath = this.zosBatchManager.getCurrentTestMethodArchiveFolder();
            String folderName = this.jobname.getName() + "_" + this.jobid + "_" + this.retcode.replace(" ", "-").replace(StringUtils.repeat(QUERY, 4), "UNKNOWN");
            rasPath = rasPath.resolve(this.zosBatchManager.getZosManager().buildUniquePathName(rasPath, folderName));
            saveOutputToResultsArchive(rasPath.toString());
        }
    }
}
