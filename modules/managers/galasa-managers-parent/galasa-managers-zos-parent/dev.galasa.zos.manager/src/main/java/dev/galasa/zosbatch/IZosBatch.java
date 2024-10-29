/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch;

import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * Provides the test code access to zOS Batch jobs via the zOS Manager 
 * 
 *  
 *
 */
public interface IZosBatch {
    
    /**
     * Submit a job.
     * 
     * @param jcl - The JCL to submit.   Must not include the JOB statement
     * @param jobname - {@link IZosBatchJobname} A provisioned jobname, if null, a new unique jobname will be provisioned.
     * @return {@link IZosBatchJob} A representation of the zOS Batch Job
     * @throws ZosBatchException 
     */
    @NotNull
    public IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname) throws ZosBatchException;

    /**
     * Submit a job.
     * 
     * @param jcl - The JCL to submit.   Must not include the JOB statement
     * @param jobname - {@link IZosBatchJobname} A provisioned jobname, if null, a new unique jobname will be provisioned.
     * @Param jobcard - {@link IZosBatchJobcard} Overrides for the job card values
     * @return {@link IZosBatchJob} A representation of the zOS Batch Job
     * @throws ZosBatchException 
     */
    @NotNull
    public IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname, ZosBatchJobcard jobCard) throws ZosBatchException;

    /**
     * Return a list of zOS Batch jobs with the given jobname and/or the owner. Jobname and owner can be the full value 
     * or use the * wild card.
     * @param jobname - The jobname. If null, defaults to *
     * @param owner - The User ID of the job owner. If null, defaults to the user ID of the requester. Use * for all owners.  
     * @return a {@link List} of {@link IZosBatchJob} objects with the given jobname prefix
     * @throws ZosBatchException 
     */
    public List<IZosBatchJob> getJobs(String jobname, String owner) throws ZosBatchException;
}
