/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import dev.galasa.zosmf.IZosmf.ZosmfCustomHeaders;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * Implementation of {@link IZosBatchJob} using zOS/MF
 *
 */
public class ZosmfZosBatchJobImpl implements IZosBatchJob {
    
    private IZosmfRestApiProcessor zosmfApiProcessor;
	private ZosmfZosBatchManagerImpl zosBatchManager;
    
    private IZosImage jobImage;
    private IZosBatchJobname jobname;
    private final ZosBatchJobcard jobcard;
    private String jcl;  
    private int intdrLrecl = 80;  
    private String intdrRecfm = "F";    
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
    
	private Path testMethodArchiveFolder;
    
    private static final String PROP_REASON = "reason";
    private static final String PROP_RC = "rc";
    private static final String PROP_CATEGORY = "category";
    private static final String PROP_JOBID = "jobid";
    private static final String PROP_STATUS = "status";
    private static final String PROP_OWNER = "owner";
    private static final String PROP_TYPE = "type";
    private static final String PROP_RETCODE = "retcode";
    private static final String PROP_ID = "id";
    private static final String PROP_MESSAGE = "message";
    private static final String PROP_DETAILS = "details";
    private static final String PROP_STACK = "stack";
    
    private static final String SLASH = "/";
    private static final String QUERY = "?";
    public static final String RESTJOBS_PATH = SLASH + "zosmf" + SLASH + "restjobs" + SLASH + "jobs";
    
    private static final String LOG_JOB_NOT_SUBMITTED = "Job has not been submitted by manager";
    
    private static final Log logger = LogFactory.getLog(ZosmfZosBatchJobImpl.class);

    public ZosmfZosBatchJobImpl(ZosmfZosBatchManagerImpl zosBatchManager, IZosImage jobImage, IZosBatchJobname jobname, String jcl, ZosBatchJobcard jobcard) throws ZosBatchException {
    	this.zosBatchManager = zosBatchManager;
        this.jobImage = jobImage;
        this.jobname = jobname;
        if (jobcard != null) {
            this.jobcard = jobcard;
        } else {
            this.jobcard = new ZosBatchJobcard();
        }
        this.testMethodArchiveFolder = this.zosBatchManager.getCurrentTestMethodArchiveFolder();
        if (jcl != null) {
        	try {
        		this.zosBatchManager.getZosManager().storeArtifact(testMethodArchiveFolder.resolve(this.zosBatchManager.getZosManager().buildUniquePathName(testMethodArchiveFolder, this.jobname.getName() + "_supplied_JCL")), jcl, ResultArchiveStoreContentType.TEXT);
			} catch (ZosManagerException e) {
				throw new ZosBatchException(e);
			}
            this.jcl = parseJcl(jcl);

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
            this.zosmfApiProcessor = this.zosBatchManager.getZosmfManager().newZosmfRestApiProcessor(jobImage, this.zosBatchManager.getZosManager().getZosBatchPropertyBatchRestrictToImage(jobImage.getImageID()));
        } catch (ZosmfManagerException | ZosBatchManagerException e) {
            throw new ZosBatchException(e);
        }
    }
    
    public IZosBatchJob submitJob() throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_IBM_JOB_MODIFY_VERSION.toString(), "2.0");
        headers.put(ZosmfCustomHeaders.X_IBM_INTRDR_LRECL.toString(), String.valueOf(this.intdrLrecl));
        headers.put(ZosmfCustomHeaders.X_IBM_INTRDR_RECFM.toString(), this.intdrRecfm);
        headers.put(ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString(), "");
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.PUT_TEXT, RESTJOBS_PATH + SLASH, headers, jclWithJobcard(), new ArrayList<>(Arrays.asList(HttpStatus.SC_CREATED, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException | ZosBatchManagerException e) {
            throw new ZosBatchException(e);
        }

        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
        
        logger.trace(responseBody);
        if (response.getStatusCode() == HttpStatus.SC_CREATED) {
            this.jobid = jsonNull(responseBody, PROP_JOBID);
            this.owner = jsonNull(responseBody, PROP_OWNER);
            this.type = jsonNull(responseBody, PROP_TYPE);
            this.retcode = jsonNull(responseBody, PROP_RETCODE);
            setJobPathValues();
            logger.info("JOB " + this.toString() + " Submitted");
        } else {            
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            String displayMessage = buildErrorString("Submit job", responseBody); 
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
            	String records = getSpoolFileContent(spoolFile.getId(), spoolFile.getStepname(), spoolFile.getProcstep(), ddname);
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
        if(!this.outputComplete){
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
        headers.put(ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString(), "");
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, this.jobFilesPath, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
    
        Object responseBodyObject;
        try {
            responseBodyObject = response.getContent();
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
        
        logger.trace(responseBodyObject);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            // Get the spool files
            JsonArray jsonArray;
            try {
                jsonArray = response.getJsonArrayContent();
            } catch (ZosmfException e) {
                throw new ZosBatchException(e);
            }
            for (JsonElement jsonElement : jsonArray) {
                JsonObject responseBody = jsonElement.getAsJsonObject();
                String id = jsonNull(responseBody, PROP_ID);
                String stepname = jsonNull(responseBody, "stepname");
                String procstep = jsonNull(responseBody, "procstep");
                String ddname = responseBody.get("ddname").getAsString();
                String records = null;
                if (retrieveRecords) {
                	records = getSpoolFileContent(id, stepname, procstep, ddname);
                }
                if(this.jobOutput == null) {
                	this.jobOutput = this.zosBatchManager.getZosManager().newZosBatchJobOutput(this, this.jobname.getName(), this.jobid);
                }

                ((IZosBatchJobOutputSpi) this.jobOutput).addSpoolFile(stepname, procstep, ddname, id, records);
            }
        } else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND && getStatus().equals(JobStatus.ACTIVE)) {
        	return;
        } else {            
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            String displayMessage = buildErrorString("Retrieve job output", (JsonObject) responseBodyObject); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
        
        // Get the JCLIN
        if (retrieveRecords) {
        	getSpoolFileContent("JCL", null, null, null);
        }
        
        if (this.jobComplete  && retrieveRecords) {
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
    	//zOSMF Status: INPUT | ACTIVE | OUTPUT
        this.status = JobStatus.valueOfLabel(statusString);
    }

    protected void cancel(boolean purge) throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_IBM_JOB_MODIFY_VERSION.toString(), "2.0");
        headers.put(ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString(), "");
        IZosmfResponse response;
        try {
            if (purge) {
                response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.DELETE, this.jobPath, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
            } else {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("request", "cancel");
                requestBody.addProperty("version", "2.0");
                response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.PUT_JSON, this.jobPath, headers, requestBody, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
            }
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }

        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (ZosmfManagerException e) {
            throw new ZosBatchException(e);
        }
        
        logger.trace(responseBody);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            this.statusString = null;
            if (purge) {
                this.jobPurged = true; 
            } else {
                this.jobComplete = true;
            }
        } else {            
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            String displayMessage = buildErrorString(purge ? "Purge job" : "Cancel job", responseBody); 
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
              " STATUS=" + this.status.toString() + 
              " RETCODE=" + this.retcode;
    }

    protected void setJobPathValues() {
        this.jobPath = RESTJOBS_PATH + SLASH + this.jobname.getName() + SLASH + this.jobid;
        this.jobFilesPath = this.jobPath + "/files";
    }

    protected void updateJobStatus() throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString(), "");
        
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, RESTJOBS_PATH + SLASH + this.jobname.getName() + "/" + this.jobid, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
        
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
            
        logger.trace(responseBody);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            this.jobNotFound = false;
            this.owner = jsonNull(responseBody, PROP_OWNER);
            this.type = jsonNull(responseBody, PROP_TYPE);
            this.statusString = jsonNull(responseBody, PROP_STATUS);
            if (this.statusString != null && "OUTPUT".equals(this.statusString)) {
                this.jobComplete = true;
            }
            this.status = JobStatus.valueOfLabel(statusString);
            String retcodeProperty = jsonNull(responseBody, PROP_RETCODE);
            if (retcodeProperty != null) {
                this.retcode = retcodeProperty;
            } else {
                this.retcode = StringUtils.repeat(QUERY, 4);
            }
            logger.trace(jobStatus());
        } else {
            if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST &&
                    jsonZero(responseBody, PROP_RC) == 4 &&
                    jsonZero(responseBody, PROP_REASON) == 10) {
                logger.trace("JOBID=" + this.jobid + " JOBNAME=" + this.jobname.getName() + " NOT FOUND");
                this.jobNotFound = true;
                this.status = JobStatus.NOTFOUND;
            } else {
                // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
                String displayMessage = buildErrorString("Update job status", responseBody); 
                logger.error(displayMessage);
                throw new ZosBatchException(displayMessage);
            }
        }            
    }

    protected String getSpoolFileContent(String id, String stepname, String procstep, String ddname) throws ZosBatchException {

    	String path = this.jobFilesPath + "/" + id + "/records";
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString(), "");
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, path, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
    
        String fileOutput;
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            try {
                fileOutput = response.getTextContent();
            } catch (ZosmfException e) {
                throw new ZosBatchException(e);
            }
        } else {            
            // Error case
            JsonObject errorResponseBody;
            try {
                errorResponseBody = response.getJsonContent();
            } catch (ZosmfException e) {
                throw new ZosBatchException(e);
            }
            if (this.jobComplete && spoolFileNotFound(errorResponseBody)) {
                return null; 
            } else {
                String displayMessage = buildErrorString("Retrieve job output", errorResponseBody);
                logger.error(displayMessage);
                throw new ZosBatchException(displayMessage);
            }
        }
        
        return fileOutput;
    }

    protected boolean spoolFileNotFound(JsonObject errorResponseBody) {
        return (jsonZero(errorResponseBody, PROP_CATEGORY) == 6 &&
                jsonZero(errorResponseBody, PROP_RC) == 4 &&
                jsonZero(errorResponseBody, PROP_REASON) == 12);
    }

    protected String parseJcl(String jcl) throws ZosBatchException {
        boolean truncateJCLRecords;
        try {
            truncateJCLRecords = this.zosBatchManager.getZosManager().getZosBatchPropertyTruncateJCLRecords(this.jobImage.getImageID());
        } catch (ZosBatchManagerException e) {
            throw new ZosBatchException("Unable to get trucate JCL records property value", e);
        }

        List<String> jclRecords = new LinkedList<>(Arrays.asList(jcl.split("\n")));
        
        if (truncateJCLRecords) {
            return truncateJcl(jclRecords);
        } else {
            parseJclForVaryingRecordLength(jclRecords);
            return jcl;
        }
    }

    protected String truncateJcl(List<String> jclRecords) {
        List<String> recordsOut = new LinkedList<>();
        List<Integer> truncatedRecords = new ArrayList<>();
        for (int i=0; i < jclRecords.size(); i++) {
            String record = jclRecords.get(i);
            int recordLength = record.length();
            if (recordLength > 80) {
                // Truncate records to 80 bytes
                truncatedRecords.add(i+1);
                recordsOut.add(record.substring(0, 80));
            } else {
                recordsOut.add(record);
            }
        }
        if (!truncatedRecords.isEmpty()) {
            logger.warn("The following record(s) have been truncated to 80 characters: " + truncatedRecords.toString());
        }
        return String.join("\n", recordsOut);
    }

    protected void parseJclForVaryingRecordLength(List<String> jclRecords) {
        int minRecordLength = 0;
        for (int i=0; i < jclRecords.size(); i++) {
            int recordLength = jclRecords.get(i).length();
            if (recordLength > 80) {
                if (recordLength > this.intdrLrecl) {
                    this.intdrLrecl = recordLength;
                    if (minRecordLength == 0) {
                        minRecordLength = recordLength;
                    }
                }
                if (recordLength <= minRecordLength) {
                    minRecordLength = recordLength;
                }
            }
        }
        if (minRecordLength != 0 && minRecordLength != this.intdrLrecl) {
            this.intdrRecfm = "V";
        }
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

    protected static String buildErrorString(String action, JsonObject responseBody) {
        if ("{}".equals(responseBody.toString())) {
            return "Error " + action;
        }
        int errorCategory = responseBody.get(PROP_CATEGORY).getAsInt();
        int errorRc = responseBody.get(PROP_RC).getAsInt();
        int errorReason = responseBody.get(PROP_REASON).getAsInt();
        String errorMessage = responseBody.get(PROP_MESSAGE).getAsString();
        String errorDetails = null;
        JsonElement element = responseBody.get(PROP_DETAILS);
        if (element != null) {
            if (element.isJsonArray()) {
                JsonArray elementArray = element.getAsJsonArray();
                StringBuilder sb = new StringBuilder();
                for (JsonElement item : elementArray) {
                    sb.append("\n");
                    sb.append(item.getAsString());
                }
                errorDetails = sb.toString();
            } else {
                errorDetails = element.getAsString();
            }
        }
        StringBuilder sb = new StringBuilder(); 
        sb.append("Error "); 
        sb.append(action);
        sb.append(", category:");
        sb.append(errorCategory);
        sb.append(", rc:");
        sb.append(errorRc);
        sb.append(", reason:");
        sb.append(errorReason);
        sb.append(", message:");
        sb.append(errorMessage);
        if (errorDetails != null) {
            sb.append("\ndetails:");
            sb.append(errorDetails);
        }
        JsonElement stackElement = responseBody.get(PROP_STACK);
        if (stackElement != null) {
            sb.append("\nstack:\n");
            sb.append(stackElement.getAsString());
        }
        
        return sb.toString();
    }

    protected void archiveJobOutput() throws ZosBatchException {
    	if (shouldArchive() && getStatus() != JobStatus.NOTFOUND && (!isArchived() || !this.jobComplete)) {
    		retrieveOutput();
            String folderName = this.jobname.getName() + "_" + this.jobid + "_" + getRetcode().replace(" ", "-");
            Path rasPath = this.testMethodArchiveFolder.resolve(this.zosBatchManager.getZosManager().buildUniquePathName(testMethodArchiveFolder, folderName));
            saveOutputToResultsArchive(rasPath.toString());
        }
    }
}
