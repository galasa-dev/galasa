/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.manager.ivt;

import static org.assertj.core.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.StoredArtifactRoot;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutput;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatch;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobcard;
import dev.galasa.zosbatch.ZosBatchJobname;

@Test
public class ZosManagerBatchIVT {
    
    @Logger
    public Log logger;
    
    @ZosImage(imageTag =  "PRIMARY")
    public IZosImage imagePrimary;
    
    @ZosBatch(imageTag = "PRIMARY")
    public IZosBatch batch;
    
    @ZosBatchJobname(imageTag = "PRIMARY")
    public IZosBatchJobname jobName;
    
    @ZosBatchJobname(imageTag = "PRIMARY")
    public IZosBatchJobname jobName2;
    
    @BundleResources
    public IBundleResources resources;    
    
    @CoreManager
    public ICoreManager coreManager;
    
    @StoredArtifactRoot
    public Path rasRoot;
    
    
    
    @Test
    public void preFlightTestsBasic() {
    	assertThat(resources).isNotNull();
        assertThat(logger).isNotNull();
        assertThat(coreManager).isNotNull();
    }
    @Test
    public void preFlightTestsZos() throws Exception {
        assertThat(imagePrimary).isNotNull();        
        assertThat(imagePrimary.getDefaultCredentials()).isNotNull();
    }
    
    @Test
    public void preFlightTestsBatch() {
    	assertThat(batch).isNotNull();
        assertThat(jobName).isNotNull();
        assertThat(jobName2).isNotNull();
        assertThat(jobName.getName()).isUpperCase();
        assertThat(jobName2.getName()).isUpperCase();
        assertThat(jobName.getName()).isNotEqualTo(jobName2.getName());
    }

    /**
     * Running a piece of JCL with no steps will fail 
     * as there is no job to find after execution, but we should not throw an exception
     * @throws TestBundleResourceException
     * @throws IOException
     * @throws ZosBatchException
     */
    @Test 
    public void submitJCLNoSteps() throws TestBundleResourceException, IOException, ZosBatchException {
    	String jclInput = resources.retrieveFileAsString("/jcl/noSteps.jcl");
    	IZosBatchJob job = batch.submitJob(jclInput, null);
    	int returnCode = job.waitForJob();
    	IZosBatchJobOutputSpoolFile spool = job.getSpoolFile("COBOL");
    	logger.info("test" + returnCode);
    }
    
    /*
     * Basic submission and wait for a job 
     * The job submitted doesn't do anything of interest
     */
    @Test
    public void submitJCLDoNothing() throws TestBundleResourceException, IOException, ZosBatchException {
    	String jclInput = resources.retrieveFileAsString("/jcl/doNothing.jcl");
    	IZosBatchJob job = batch.submitJob(jclInput, null);
    	int returnCode = job.waitForJob();
    	assertThat(returnCode).isEqualTo(0);
    }
    
    /*
     * Submits the basic job but with a provisioned
     * job name - we perform an extra check that the job name
     * was used in the job
     */
    @Test
    public void submitJCLDoNothingJobName() throws TestBundleResourceException, IOException, ZosBatchException {
    	String jclInput = resources.retrieveFileAsString("/jcl/doNothing.jcl");
    	IZosBatchJob job = batch.submitJob(jclInput, jobName);
    	int returnCode = job.waitForJob();
    	assertThat(job.getJobname().getName()).isEqualTo(jobName.getName());
    	assertThat(returnCode).isEqualTo(0);
    }
    
    /*
     * Submits the basic job but with a provisioned
     * job name and a job card
     */
    @Test
    public void submitJCLDoNothingJobNameAndBlankCard() throws TestBundleResourceException, IOException, ZosBatchException {
    	String jclInput = resources.retrieveFileAsString("/jcl/doNothing.jcl");  	
    	ZosBatchJobcard jobCard = new ZosBatchJobcard();
    	IZosBatchJob job = batch.submitJob(jclInput, jobName2, jobCard);
    	int returnCode = job.waitForJob();
    	assertThat(job.getJobname().getName()).isEqualTo(jobName2.getName());
    	assertThat(returnCode).isEqualTo(0);
    }
    
    /*
     * Runs a Batch Job and checks the output in the RAS
     */
    @Test
    public void checkOutputIsStoredInRAS() throws TestBundleResourceException, IOException, ZosBatchException {
    	String jclInput = resources.retrieveFileAsString("/jcl/doNothing.jcl");
    	IZosBatchJob job = batch.submitJob(jclInput, null);
    	job.setShouldArchive(true);
    	job.waitForJob();
    	Path jobOutput = rasRoot.resolve("zosBatchJobs").resolve("checkOutputIsStoredInRAS");
    	logger.info("Checking that the path: " + jobOutput.toString() + " exists");
    	assertThat(Files.exists(jobOutput)).isTrue();
    	
    }
    
    /*
     * Retrieve the output for a job 
     */
    @Test
    public void retrieveJobs() throws TestBundleResourceException, IOException, ZosBatchException {
    	String jclInput = resources.retrieveFileAsString("/jcl/doNothing.jcl");
    	IZosBatchJob job = batch.submitJob(jclInput, null);
    	job.waitForJob();
    	List<IZosBatchJob> jobs = batch.getJobs(job.getJobname().getName(), job.getOwner());
    	assertThat(jobs).hasSize(1);
    }
    
    //@Test
    public void getAsString() throws TestBundleResourceException, IOException, ZosBatchException {
    	String message = "HELLO WORLD FROM GALASA";
    	Map<String,Object> parameters = new HashMap<>();
    	parameters.put("MESSAGE", message);
    	String jclInput = resources.retrieveSkeletonFileAsString("/jcl/helloWorld.jcl", parameters);
    	assertThat(jclInput).contains(message);
    	
    	//submit the job, check that it completes and we got some output
    	IZosBatchJob job = batch.submitJob(jclInput, null);
    	String output = job.retrieveOutputAsString();
    	assertThat(output).contains(job.getJobId());
    	assertThat(output).contains(job.getJobname().getName());
    	assertThat(output).contains(job.getOwner());
    	assertThat(output).contains(job.getStatus().toString());
    	assertThat(output).contains(job.getStatusString());
    }
    
    /*
     * Runs a batch job and interrogates the output
     */
    @Test
    public void retrieveAndCheckOutput() throws TestBundleResourceException, IOException, ZosBatchException {
    	//prepare the input JCL with our message
    	String message = "HELLO WORLD FROM GALASA";
    	Map<String,Object> parameters = new HashMap<>();
    	parameters.put("MESSAGE", message);
    	String jclInput = resources.retrieveSkeletonFileAsString("/jcl/helloWorld.jcl", parameters);
    	assertThat(jclInput).contains(message);
    	
    	//submit the job, check that it completes and we got some output
    	IZosBatchJob job = batch.submitJob(jclInput, null);
    	assertThat(job.waitForJob()).isEqualTo(0);
    	IZosBatchJobOutput output = job.retrieveOutput();
    	assertThat(output.isEmpty()).isFalse();
    	
    	//Find the SYSUT2 output and check the message was output
    	List<IZosBatchJobOutputSpoolFile> files = output.getSpoolFiles();
    	for(IZosBatchJobOutputSpoolFile f : files) {
    		if(f.getDdname().equals("SYSUT2")) {
    			assertThat(f.getRecords()).isEqualToIgnoringWhitespace(message);
    			assertThat(f.getStepname()).isEqualTo("HELLO");
    			//length is message+1 to account for a new line break
    			assertThat(f.getSize()).isEqualTo(message.length()+1);
    		}
    	}
    }
}
