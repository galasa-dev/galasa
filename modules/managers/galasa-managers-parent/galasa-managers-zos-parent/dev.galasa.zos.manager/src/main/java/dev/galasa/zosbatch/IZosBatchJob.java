/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch;

/**
 * <p>Represents a zOS Batch Job.</p>
 * 
 *  
 *
 */
public interface IZosBatchJob {
    
    /**
     * Enumeration of Job Status:
     * <li>{@link #INPUT}</li>
     * <li>{@link #ACTIVE}</li>
     * <li>{@link #OUTPUT}</li>
     * <li>{@link #NOTFOUND}</li>
     * <li>{@link #UNKNOWN}</li>
     */
    public enum JobStatus {
    	INPUT("INPUT"),
    	ACTIVE("ACTIVE"),
    	OUTPUT("OUTPUT"),
    	NOTFOUND("NOTFOUND"),
    	UNKNOWN("UNKNOWN");
    	
    	private String value;
    	
    	private JobStatus(String value) {
			this.value = value;
		}
    	
    	public static JobStatus valueOfLabel(String jobStatus) {
    		for (JobStatus element : values()) {
                if (element.value.equals(jobStatus)) {
                    return element;
                }
            }
            return JobStatus.UNKNOWN;
		}
    }
    
    /**
     * The {@link IZosBatchJobname} associated with this job
     * 
     * @return batch job name
     */
    public IZosBatchJobname getJobname();
    
    /**
     * The jobid for this Job. Returns "????????" if no jobid has been associated
     * 
     * @return batch jobid
     */
    public String getJobId();
    
    /**
     * The owner for this Job. Returns "????????" if no owner has been associated
     * 
     * @return batch job owner
     */
    public String getOwner();
    
    /**
     * The type for this Job, i.e. "JOB", "STC" or "TSU". Returns "???" if no type has been associated
     * 
     * @return batch job type
     */
    public String getType();

    /**
     * The batch job value
     * 
     * @return batch job value
     */
    public JobStatus getStatus();

    /**
     * The batch job value as a {@link String}, e.g.<br>
     * "INPUT", "ACTIVE", "OUTPUT" etc.<br>
     * Returns "????????" if the job has not been submitted
     * <p>
     * N.B. Values are implementation dependent
     * 
     * @return batch job value
     */
    public String getStatusString();
    
    /**
     * The batch job completion return code, e.g.<br>
     * "CC 0000", "CC 0020", "JCL ERROR", "ABEND S0C4" etc.
     * Returns "????" if the job has not been submitted
     * 
     * @return
     */
    public String getRetcode();
    
    /**
     * Wait for a job to complete. Return the highest return code for the job. The method will wait for the default 
     * resource wait time before timing out. Returns {@link Integer#MIN_VALUE} if return code is non numeric. 
     * Use {@link #getRetcode()} to get the {@link String} value
     * 
     * @return highest CC
     * @throws ZosBatchException
     */
    public int waitForJob() throws ZosBatchException;
    
    /**
     * Wait for a job to complete. Return the highest return code for the job. The method will wait for the default 
     * resource wait time before timing out. Returns {@link Integer#MIN_VALUE} if return code is non numeric. 
     * Use {@link #getRetcode()} to get the {@link String} value
     * 
     * @param timeout in seconds
     * @return highest CC
     * @throws ZosBatchException
     */
    public int waitForJob(long timeout) throws ZosBatchException;

    /**
     * Provides a list of the batch job spool files as an {@link IZosBatchJobOutput} object without retrieving spool file content
     * 
     * @return The job output 
     * @throws ZosBatchException
     */
    public IZosBatchJobOutput listSpoolFiles() throws ZosBatchException;

    /**
     * Retrieve the batch job output as an {@link IZosBatchJobOutput} object
     * 
     * @return The job output 
     * @throws ZosBatchException
     */
    public IZosBatchJobOutput retrieveOutput() throws ZosBatchException;

    /**
     * Retrieve the batch job output as an {@link String} object
     * 
     * @return The job output
     * @throws ZosBatchException
     */
    public String retrieveOutputAsString() throws ZosBatchException;
    
    /**
     * Convenience method to retrieve the content of a spool file from the batch job given the ddname.<p>
     * <b>NOTE:</b> Returns the first matching instance in the list. If the batch job has multiple steps, there may be multiple 
     * instances of the ddname. 
     * 
     * @param ddname of the spool file
     * @return the content of the first found spool file with the specified ddname
     * @throws ZosBatchException
     */
    public IZosBatchJobOutputSpoolFile getSpoolFile(String ddname) throws ZosBatchException;
    
    /**
     * Cancel the batch job
     * 
     * @throws ZosBatchException
     */
    public void cancel() throws ZosBatchException;

    /**
     * Cancel the batch job and purge output from the queue
     * 
     * @throws ZosBatchException
     */
    public void purge() throws ZosBatchException;

    /**
     * Save the job output to the Results Archive Store
     * 
     * @param rasPath path in Results Archive Store  
     * @throws ZosBatchException
     */
    public void saveOutputToResultsArchive(String rasPath) throws ZosBatchException;

    /**
     * Set flag to control if the job output should be automatically stored to the test output. Defaults to true
     */    
    public void setShouldArchive(boolean shouldArchive);

    /**
     * Return flag that controls if the job output should be automatically stored to the test output
     */    
    public boolean shouldArchive();

    /**
     * Set flag to control if the job output should be automatically purged from zOS. Defaults to true
     */    
    public void setShouldCleanup(boolean shouldCleanup);

    /**
     * Return flag that controls if the job output should be automatically purged from zOS
     */    
    public boolean shouldCleanup();

    /**
     * Save the supplied spool file to the Results Archive Store
     * @param spoolFile the spool file to save
     * @param rasPath path in Results Archive Store  
     * @throws ZosBatchException
     */
	public void saveSpoolFileToResultsArchive(IZosBatchJobOutputSpoolFile spoolFile, String rasPath) throws ZosBatchException;
}
