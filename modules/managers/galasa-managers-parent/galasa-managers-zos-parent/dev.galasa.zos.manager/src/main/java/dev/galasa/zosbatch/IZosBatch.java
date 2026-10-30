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
     * Submit a job with a unique jobname that is provisioned by the Manager.
     * <p>
     * The Manager generates a JOB statement and puts it in front of the supplied JCL, so the JCL must not include a JOB statement.
     * To submit JCL that already contains its own JOB statement, use {@link #submitJobWithSuppliedJobcard(String)}.
     * To submit the job with a jobname chosen by the test, use {@link #submitJob(String, IZosBatchJobname)} with {@link ZosBatchJobnames}.
     * <p>
     * This is the same as calling {@link #submitJob(String, IZosBatchJobname)} with a <code>null</code> jobname.
     *
     * @param jcl - The JCL to submit.   Must not include the JOB statement, unless submitted using {@link #submitJobWithSuppliedJobcard(String)}
     * @return {@link IZosBatchJob} A representation of the zOS Batch Job
     * @throws ZosBatchException
     */
    @NotNull
    public default IZosBatchJob submitJob(@NotNull String jcl) throws ZosBatchException {
        return submitJob(jcl, null);
    }
    
    /**
     * Submit a job.
     * <p>
     * The Manager generates a JOB statement and puts it in front of the supplied JCL, so the JCL must not include a JOB statement.
     * To submit JCL that already contains its own JOB statement, use {@link #submitJobWithSuppliedJobcard(String)}.
     *
     * @param jcl - The JCL to submit.   Must not include the JOB statement, unless submitted using {@link #submitJobWithSuppliedJobcard(String)}
     * @param jobname - {@link IZosBatchJobname} A provisioned jobname, if null, a new unique jobname will be provisioned.
     * To submit the job with a jobname chosen by the test, use {@link ZosBatchJobnames#exact(String)}.
     * An exact jobname is not made unique, so tests that run at the same time and use the same jobname can collide.
     * See {@link ZosBatchJobnames} for details.
     * @return {@link IZosBatchJob} A representation of the zOS Batch Job
     * @throws ZosBatchException
     */
    @NotNull
    public IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname) throws ZosBatchException;

    /**
     * Submit a job.
     * <p>
     * The Manager generates a JOB statement and puts it in front of the supplied JCL, so the JCL must not include a JOB statement.
     * To submit JCL that already contains its own JOB statement, use {@link #submitJobWithSuppliedJobcard(String)}.
     *
     * @param jcl - The JCL to submit.   Must not include the JOB statement, unless submitted using {@link #submitJobWithSuppliedJobcard(String)}
     * @param jobname - {@link IZosBatchJobname} A provisioned jobname, if null, a new unique jobname will be provisioned.
     * To submit the job with a jobname chosen by the test, use {@link ZosBatchJobnames#exact(String)}.
     * An exact jobname is not made unique, so tests that run at the same time and use the same jobname can collide.
     * See {@link ZosBatchJobnames} for details.
     * @Param jobcard - {@link IZosBatchJobcard} Overrides for the job card values
     * @return {@link IZosBatchJob} A representation of the zOS Batch Job
     * @throws ZosBatchException
     */
    @NotNull
    public IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname, ZosBatchJobcard jobCard) throws ZosBatchException;

    /**
     * Submit a job using JCL that contains its own JOB statement, for example a member of a JCL library.
     * <p>
     * The JCL is submitted as supplied. The Manager does not generate a JOB statement, so:
     * <ul>
     * <li>the job name is taken from the supplied JOB statement, converted to upper case,
     * and is returned by {@link IZosBatchJob#getJobname()}. A Galasa provisioned jobname is not used.
     * The same job name is used to track the job, so {@link IZosBatchJob#waitForJob()} and the retrieval of spool files work as usual.
     * Jobs submitted with the same job name by tests running at the same time are distinguished by their job IDs.</li>
     * <li>the values that are set on a {@link ZosBatchJobcard}, and the default input class, message class and message level
     * from the CPS properties, are not applied. Everything required must be specified in the supplied JOB statement.</li>
     * </ul>
     * The first record of the JCL that is not blank and is not a comment (<code>//*</code>) must be the JOB statement,
     * <code>//jobname JOB ...</code>. The statement can be continued over several records.
     * If a valid JOB statement is not found, a {@link ZosBatchException} is thrown and the JCL is not submitted.
     * Only the first JOB statement is considered if the JCL contains more than one.
     * <p>
     * If the CPS property <code>zosbatch.batchjob.[imageid].use.sysaff</code> is true,
     * a <code>/*JOBPARM SYSAFF=[imageid]</code> record is inserted immediately after the last record of the JOB statement.
     * It is not inserted if the supplied JCL already contains a <code>/*JOBPARM</code> record that specifies <code>SYSAFF=</code>.
     * <p>
     * The supplied JCL is stored in the test results archive in the same way as for {@link #submitJob(String, IZosBatchJobname)}.
     *
     * @param jcl - The JCL to submit.   Must start with a JOB statement
     * @return {@link IZosBatchJob} A representation of the zOS Batch Job
     * @throws ZosBatchException if the JCL does not start with a valid JOB statement or the job cannot be submitted
     */
    @NotNull
    public IZosBatchJob submitJobWithSuppliedJobcard(@NotNull String jcl) throws ZosBatchException;

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
