/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.ZosBatchException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosBatchJobOutputSpoolFileImpl { 
    
    @Mock
    private IZosBatchJob zosBatchJobMock;
    
    @Mock
    private IZosBatchJobOutputSpoolFile zosBatchJobOutputSpoolFileMock;

    private static final String JOBNAME = "jobname";

    private static final String JOBID = "jobid";
    
    private static final String STEPNAME = "stepname";
    
    private static final String PROCSTEP = "procstep";
    
    private static final String DDNAME = "ddname";
    
    private static final String ID = "id";

    private static final String RECORDS = "records";
    
    @Test
    public void testConstructor() {
        ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock, JOBNAME, JOBID, STEPNAME, PROCSTEP, DDNAME, ID, RECORDS);
        Assert.assertEquals("getJobname() should return the supplied value", JOBNAME, zosBatchJobOutputSpoolFile.getJobname());
        Assert.assertEquals("getJobid() should return the supplied value", JOBID, zosBatchJobOutputSpoolFile.getJobid());
        Assert.assertEquals("getStepname() should return the supplied value", STEPNAME, zosBatchJobOutputSpoolFile.getStepname());
        Assert.assertEquals("getProcstep() should return the supplied value", PROCSTEP, zosBatchJobOutputSpoolFile.getProcstep());
        Assert.assertEquals("getDdname() should return the supplied value", DDNAME, zosBatchJobOutputSpoolFile.getDdname());
        Assert.assertEquals("getId() should return the supplied value", ID, zosBatchJobOutputSpoolFile.getId());
        Assert.assertEquals("getRecords() should return the supplied value", RECORDS, zosBatchJobOutputSpoolFile.getRecords());
        
        String expectedString = "JOB=" + JOBNAME + " JOBID=" + JOBID + " STEP=" + STEPNAME +  " PROCSTEP=" + PROCSTEP + " DDNAME=" + DDNAME;
        Assert.assertEquals("toString() should return the values of JOBNAME JOBID STEPNAME PROCSTEP DDNAME", expectedString , zosBatchJobOutputSpoolFile.toString());
    }
    
    @Test
    public void testGetSize() throws ZosBatchException {
    	ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock, JOBNAME, JOBID, STEPNAME, PROCSTEP, DDNAME, ID, RECORDS);
        Assert.assertEquals("getSize() should return the expected value", RECORDS.length(), zosBatchJobOutputSpoolFile.getSize());
        
    	zosBatchJobOutputSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock, JOBNAME, JOBID, STEPNAME, PROCSTEP, DDNAME, ID, null);
    	ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFileSpy = Mockito.spy(zosBatchJobOutputSpoolFile);
    	Mockito.doReturn(9999L).when(zosBatchJobOutputSpoolFileSpy).retrieve();
        Assert.assertEquals("getSize() should return the expected value", 9999L, zosBatchJobOutputSpoolFileSpy.getSize());
    }
    
    @Test
    public void testRetrieve() throws ZosBatchException {
    	Mockito.when(zosBatchJobMock.getSpoolFile(Mockito.any())).thenReturn(zosBatchJobOutputSpoolFileMock);
    	Mockito.when(zosBatchJobOutputSpoolFileMock.getRecords()).thenReturn(RECORDS);
    	ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock, JOBNAME, JOBID, STEPNAME, PROCSTEP, DDNAME, ID, RECORDS);
        Assert.assertEquals("getRecords() should return the supplied value", RECORDS.length(), zosBatchJobOutputSpoolFile.retrieve());
        Assert.assertEquals("getRecords() should return the supplied value", RECORDS, zosBatchJobOutputSpoolFile.getRecords());

    	Mockito.when(zosBatchJobOutputSpoolFileMock.getRecords()).thenReturn(null);
        Assert.assertEquals("getRecords() should return the supplied value", 0, zosBatchJobOutputSpoolFile.retrieve());
        Assert.assertNull("getRecords() should return the supplied value", zosBatchJobOutputSpoolFile.getRecords());
    }
    
    @Test
    public void testSaveToResultsArchive() throws ZosBatchException {
    	ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock, JOBNAME, JOBID, STEPNAME, PROCSTEP, DDNAME, ID, RECORDS);
    	ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFileSpy = Mockito.spy(zosBatchJobOutputSpoolFile);
        zosBatchJobOutputSpoolFileSpy.saveToResultsArchive("path");
        Mockito.verify(zosBatchJobOutputSpoolFileSpy, Mockito.times(1)).saveToResultsArchive("path");
    }
}
