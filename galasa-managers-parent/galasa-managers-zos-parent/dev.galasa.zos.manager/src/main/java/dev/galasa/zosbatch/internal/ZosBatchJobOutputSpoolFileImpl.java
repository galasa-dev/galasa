/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal;

import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.ZosBatchException;

/**
 * Implementation of {@link IZosBatchJobOutputSpoolFile}
 *
 */
public class ZosBatchJobOutputSpoolFileImpl implements IZosBatchJobOutputSpoolFile {

	private IZosBatchJob batchJob;
    private String jobname;
    private String jobid;
    private String stepname;
    private String procstep;
    private String ddname;
    private String id;
    private long size;
    private String records;
    
    /**
     * Constructor for creating spool file
     * @param records
     * @param jobname 
     * @param jobid 
     */
    public ZosBatchJobOutputSpoolFileImpl(IZosBatchJob batchJob, String jobname, String jobid, String stepname, String procstep, String ddname, String id, String records) {

    	this.batchJob = batchJob;
        this.jobname = jobname;
        this.jobid = jobid;
        this.stepname = stepname;
        this.procstep = procstep;
        this.ddname = ddname;
        this.id = id;
        this.setRecords(records);
    }

    @Override
    public String getJobname() {
        return this.jobname;
    }

    @Override
    public String getJobid() {
        return this.jobid;
    }

    @Override
    public String getStepname() {
        return this.stepname;
    }

    @Override
    public String getProcstep() {
        return this.procstep;
    }

    @Override
    public String getDdname() {
        return this.ddname;
    }

    @Override
	public String getId() {
		return this.id;
	}

    @Override
	public long getSize() throws ZosBatchException {
    	if (this.records == null) {
    		return retrieve();
    	}
		return this.size;
	}

	@Override
    public String getRecords() {
        return this.records;        
    }

    @Override
    public String toString() {
        return "JOB=" + jobname + " JOBID=" + jobid + " STEP=" + stepname +  " PROCSTEP=" + procstep + " DDNAME=" + ddname;
    }

    @Override
    public void setRecords(String records) {
        this.records = records;
        if (this.records != null) {
            this.size = this.records.length();
        } else {
            this.size = 0;
        }
    }

	@Override
	public long retrieve() throws ZosBatchException {
		this.records = this.batchJob.getSpoolFile(this.ddname).getRecords();
        if (this.records != null) {
        	this.size = this.records.length();
        } else {
        	this.size = 0;
        }
        return this.size;
	}

	@Override
	public void saveToResultsArchive(String rasPath) throws ZosBatchException {
        this.batchJob.saveSpoolFileToResultsArchive(this, rasPath);
	}
}
