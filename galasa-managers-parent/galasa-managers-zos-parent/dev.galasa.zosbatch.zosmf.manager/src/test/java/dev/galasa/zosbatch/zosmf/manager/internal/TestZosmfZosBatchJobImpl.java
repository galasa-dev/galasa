/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.galasa.framework.spi.ras.ResultArchiveStorePath;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosbatch.IZosBatchJob.JobStatus;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobcard;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.spi.IZosBatchJobOutputSpi;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LogFactory.class})
public class TestZosmfZosBatchJobImpl {
//    
//    private ZosmfZosBatchJobImpl zosBatchJob;
//    
//    private ZosmfZosBatchJobImpl zosBatchJobSpy;
//    
//    @Mock
//    private Log logMock;
//    
//    private static String logMessage;
//    
//    private static Throwable logException;
//
//    @Mock
//    private IZosImage zosImageMock;
//
//    @Mock
//    private IZosBatchJobname zosJobnameMock;
//
//    @Mock
//    private ZosBatchJobcard zosBatchJobcardMock;
//
//    @Mock
//    private ZosManagerImpl zosManagerMock;
//
//    @Mock
//    private ZosmfManagerImpl zosmfManagerMock;
//
//    @Mock
//    private ZosmfZosBatchManagerImpl zosBatchManagerMock;
//    
//    @Mock
//    private IZosmfRestApiProcessor zosmfApiProcessorMock;
//    
//    @Mock
//    private IZosmfResponse zosmfResponseMockSubmit;
//    
//    @Mock
//    private IZosmfResponse zosmfResponseMockStatus;
//    
//    @Mock
//    private IZosBatchJobOutputSpi zosBatchJobOutputMock;
//    
//    @Mock
//    private IZosBatchJobOutputSpoolFile zosBatchJobOutputSpoolFileMock;
//    
//    @Mock
//    private Iterator<IZosBatchJobOutputSpoolFile> zosBatchJobOutputSpoolFileIteratorMock;
//    
//    @Mock
//    private ResultArchiveStorePath resultArchiveStorePathMock;
//
//    private static final String FIXED_PATH_NAME = "PATH/NAME";
//
//    private static final String FIXED_IMAGE_ID = "IMAGE";
//
//    private static final String FIXED_JOBNAME = "GAL45678";
//    
//    private static final String FIXED_JOBID = "JOB12345";
//    
//    private static final String FIXED_OWNER = "USERID";
//    
//    private static final String FIXED_JOBCARD = "//" + FIXED_JOBNAME + " JOB \n";
//    
//    private static final String FIXED_TYPE = "TYP";
//    
//    private static final String FIXED_STATUS_OUTPUT = "OUTPUT";
//
//    private static final String FIXED_RETCODE_0000 = "CC 0000";
//
//    private static final String FIXED_RETCODE_0020 = "CC 0020";
//
//	private static final String FIXED_DDNAME = "DDNAME";
//
//	private static final String FIXED_STEPNAME = "STEP";
//
//	private static final String FIXED_PROCSTEP = "PROCSTEP";
//
//	private static final String FIXED_ID = "ID";
//
//	private static final String FIXED_CONTENT = "content";
//
//	private static final String EXCEPTION = "exception";
//
//    @Before
//    public void setup() throws Exception {
//        PowerMockito.mockStatic(LogFactory.class);
//        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
//        Answer<String> answer = new Answer<String>() {
//            @Override
//            public String answer(InvocationOnMock invocation) throws Throwable {
//            	logMessage = null;
//            	logException = null;
//            	if (invocation.getArgument(0) instanceof Throwable) {
//            		logException = invocation.getArgument(0);
//            		System.err.println("Captured Log Exception:");
//            		logException.printStackTrace();
//            	} else {
//            		logMessage = invocation.getArgument(0);
//            		System.err.println("Captured Log Message:\n" + logMessage);
//            	}
//                if (invocation.getArguments().length > 1 && invocation.getArgument(1) instanceof Throwable) {
//                    ((Throwable) invocation.getArgument(1)).printStackTrace();
//                }
//                return null;
//            }
//        };
//        Mockito.doAnswer(answer).when(logMock).info(Mockito.any());
//        Mockito.doAnswer(answer).when(logMock).error(Mockito.any());
//        
//        Mockito.when(zosImageMock.getImageID()).thenReturn(FIXED_IMAGE_ID);
//        
//        Mockito.when(zosJobnameMock.getName()).thenReturn(FIXED_JOBNAME);
//        
//        Mockito.when(zosManagerMock.getZosBatchPropertyJobWaitTimeout(Mockito.any())).thenReturn(2);
//        
//        Mockito.when(zosManagerMock.getZosBatchPropertyUseSysaff(Mockito.any())).thenReturn(false);
//        
//        Mockito.when(zosManagerMock.getZosBatchPropertyBatchRestrictToImage(Mockito.any())).thenReturn(true);
//        
//        Mockito.when(zosManagerMock.getZosBatchPropertyTruncateJCLRecords(Mockito.any())).thenReturn(true);        
//        
//        Path archivePathMock = newMockedPath(false);
//        Whitebox.setInternalState(zosBatchManagerMock, "archivePath", archivePathMock);
//        Mockito.when(zosBatchManagerMock.getCurrentTestMethodArchiveFolder()).thenReturn(archivePathMock);
//        Mockito.when(zosBatchManagerMock.getArchivePath()).thenReturn(archivePathMock);
//        Mockito.when(zosBatchManagerMock.getArtifactsRoot()).thenReturn(archivePathMock);
//
//        PowerMockito.doReturn(zosmfApiProcessorMock).when(zosmfManagerMock).newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//        Mockito.when(zosBatchManagerMock.getZosmfManager()).thenReturn(zosmfManagerMock);
//        Mockito.when(zosBatchManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        Mockito.when(zosManagerMock.buildUniquePathName(Mockito.any(), Mockito.any())).thenReturn(FIXED_PATH_NAME);
//        
//        Mockito.when(zosBatchJobcardMock.getJobcard(Mockito.any(), Mockito.any())).thenReturn(FIXED_JOBCARD);
//        
//        zosBatchJob = new ZosmfZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", zosBatchJobcardMock);
//        zosBatchJobSpy = Mockito.spy(zosBatchJob);
//    }
//    
//    @Test
//    public void testConstructor() throws ZosBatchException {
//        Assert.assertEquals("getJobname() should return the supplied job name", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
//        
//        zosBatchJob = new ZosmfZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", null);
//        Assert.assertEquals("getJobname() should return the supplied job name", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
//        
//        zosBatchJob = new ZosmfZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, null, null);
//        Assert.assertEquals("getJobname() should return the supplied job name", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
//    }
//    
//    @Test
//    public void testConstructorJobWaitTimeoutException() throws ZosBatchManagerException {
//        String expectedMessage = "Unable to get job timeout property value";
//        Mockito.when(zosManagerMock.getZosBatchPropertyJobWaitTimeout(Mockito.anyString())).thenThrow(new ZosBatchManagerException(EXCEPTION));
//
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		new ZosmfZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", null);
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testConstructorUseSysaffException() throws ZosBatchManagerException {
//        String expectedMessage = "Unable to get use SYSAFF property value";
//        Mockito.when(zosManagerMock.getZosBatchPropertyUseSysaff(Mockito.any())).thenThrow(new ZosBatchManagerException(EXCEPTION));
//        
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//        	new ZosmfZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testConstructorRestrictToImageException() throws ZosBatchManagerException {
//        Mockito.when(zosManagerMock.getZosBatchPropertyBatchRestrictToImage(Mockito.any())).thenThrow(new ZosBatchManagerException(EXCEPTION));
//
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		new ZosmfZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", null);
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testConstructorStoreArtifactException() throws ZosManagerException {
//        PowerMockito.doThrow(new ZosManagerException(EXCEPTION)).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
//
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		new ZosmfZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", null);
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testGetJobId() throws ZosBatchException {
//    	Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Assert.assertEquals("getJobId() should return the 'unknown' value", "????????", zosBatchJobSpy.getJobId());
//        zosBatchJobSpy.setJobid(FIXED_JOBID);
//        Assert.assertEquals("getJobId() should return the supplied value", FIXED_JOBID, zosBatchJobSpy.getJobId());
//
//    	Mockito.doThrow(new ZosBatchException(EXCEPTION)).when(zosBatchJobSpy).updateJobStatus();
//        zosBatchJobSpy.setJobid(null);
//        zosBatchJobSpy.getJobId();
//        Assert.assertTrue("method should log expected exception", logException instanceof ZosBatchException);
//        Assert.assertEquals("method should log expected exception message", EXCEPTION, logException.getMessage());
//    }
//    
//    @Test
//    public void testGetOwner() throws ZosBatchException {
//    	Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Assert.assertEquals("getOwner() should return the 'unknown' value", "????????", zosBatchJobSpy.getOwner());
//        zosBatchJobSpy.setOwner(FIXED_OWNER);
//        Assert.assertEquals("getOwner() should return the supplied value", FIXED_OWNER, zosBatchJobSpy.getOwner());
//
//    	Mockito.doThrow(new ZosBatchException(EXCEPTION)).when(zosBatchJobSpy).updateJobStatus();
//        zosBatchJobSpy.setOwner(null);
//        zosBatchJobSpy.getOwner();
//        Assert.assertTrue("method should log expected exception", logException instanceof ZosBatchException);
//        Assert.assertEquals("method should log expected exception message", EXCEPTION, logException.getMessage());
//    }
//    
//    @Test
//    public void testGetType() throws ZosBatchException {
//    	Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Assert.assertEquals("getType() should return the 'unknown' value", "???", zosBatchJobSpy.getType());
//        zosBatchJobSpy.setType(FIXED_TYPE);
//        Assert.assertEquals("getType() should return the supplied value", FIXED_TYPE, zosBatchJobSpy.getType());
//
//    	Mockito.doThrow(new ZosBatchException(EXCEPTION)).when(zosBatchJobSpy).updateJobStatus();
//        zosBatchJobSpy.setType(null);
//        zosBatchJobSpy.getType();
//        Assert.assertTrue("method should log expected exception", logException instanceof ZosBatchException);
//        Assert.assertEquals("method should log expected exception message", EXCEPTION, logException.getMessage());
//    }
//    
//    @Test
//    public void testGetStatus() throws ZosBatchException {
//    	Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Assert.assertEquals("getStatus() should return the UNKNOWN", "UNKNOWN", zosBatchJobSpy.getStatus().toString());
//        zosBatchJobSpy.setStatus(FIXED_STATUS_OUTPUT);
//        Assert.assertEquals("getStatus() should return the 'unknown' value", JobStatus.OUTPUT, zosBatchJobSpy.getStatus());
//
//    	Mockito.doThrow(new ZosBatchException(EXCEPTION)).when(zosBatchJobSpy).updateJobStatus();
//        zosBatchJobSpy.setStatus(null);
//        zosBatchJobSpy.getStatus();
//        Assert.assertTrue("method should log expected exception", logException instanceof ZosBatchException);
//        Assert.assertEquals("method should log expected exception message", EXCEPTION, logException.getMessage());
//    }
//    
//    @Test
//    public void testGetStatusString() throws ZosBatchException {
//    	Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Assert.assertEquals("getStatusString() should return the expected value", JobStatus.UNKNOWN.toString(), zosBatchJobSpy.getStatusString());
//        zosBatchJobSpy.setStatusString(FIXED_STATUS_OUTPUT);
//        Assert.assertEquals("getStatusString() should return the expected value", FIXED_STATUS_OUTPUT, zosBatchJobSpy.getStatusString());
//
//    	Mockito.doThrow(new ZosBatchException(EXCEPTION)).when(zosBatchJobSpy).updateJobStatus();
//        zosBatchJobSpy.setStatusString(null);
//        zosBatchJobSpy.getStatusString();
//        Assert.assertTrue("method should log expected exception", logException instanceof ZosBatchException);
//        Assert.assertEquals("method should log expected exception message", EXCEPTION, logException.getMessage());
//    }
//    
//    @Test
//    public void testGetRetcode() throws ZosBatchException {
//    	Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Whitebox.setInternalState(zosBatchJobSpy, "retcode", (String) null);
//        Assert.assertEquals("getRetcode() should return the 'unknown' value", "????", zosBatchJobSpy.getRetcode());
//        Whitebox.setInternalState(zosBatchJobSpy, "retcode", FIXED_RETCODE_0000);
//        Assert.assertEquals("getRetcode() should return the supplied value", FIXED_RETCODE_0000, zosBatchJobSpy.getRetcode());
//
//    	Mockito.doThrow(new ZosBatchException(EXCEPTION)).when(zosBatchJobSpy).updateJobStatus();
//        Whitebox.setInternalState(zosBatchJobSpy, "retcode", (String) null);
//        zosBatchJobSpy.getRetcode();
//        Assert.assertTrue("method should log expected exception", logException instanceof ZosBatchException);
//        Assert.assertEquals("method should log expected exception message", EXCEPTION, logException.getMessage());
//    }
//    
//    @Test
//    public void testSubmitJob() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockSubmit);
//        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
//        
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        zosBatchJobSpy.submitJob();
//        Assert.assertEquals("getJobname().getName() should return the supplied value", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
//    }
//    
//    @Test
//    public void testSubmitJobIZosmfRestApiProcessorSendRequestException() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.submitJob();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testSubmitIZosmfResponseGetJsonContentException() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockSubmit);
//        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.submitJob();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testSubmitJobNotStatusCodeCreated() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockSubmit);
//        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        String expectedMessage = "Error Submit job, category:0, rc:0, reason:0, message:message\n" + 
//        		"stack:\n" + 
//        		"stack";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.submitJob();
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testWaitForJob() throws ZosmfException, ZosBatchManagerException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//
//        JsonObject responseBody = getJsonObject();
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Assert.assertEquals("waitForJob() should return zero", 0, zosBatchJobSpy.waitForJob());
//
//        responseBody = getJsonObject();
//        responseBody.addProperty("retcode", FIXED_RETCODE_0020);
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Assert.assertEquals("waitForJob() should return the supplied value", 20, zosBatchJobSpy.waitForJob());
//        
//        responseBody.addProperty("retcode", "CC UNKNOWN");
//        Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());
//
//        responseBody.remove("retcode");
//        Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());
//
//        Mockito.doReturn(false).when(zosBatchJobSpy).isComplete();
//        Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());
//
//        Whitebox.setInternalState(zosBatchJobSpy, "jobNotFound", true);
//        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());
//    }
//
//    @Test
//    public void testWaitForJobNotSubmittedException() throws ZosBatchException {
//    	Mockito.doReturn("????????").when(zosBatchJobSpy).getJobId();
//        String expectedMessage = "Job has not been submitted by manager";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.waitForJob();
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testListSpoolFiles() throws Exception {        
//    	Mockito.doNothing().when(zosBatchJobSpy).getOutput(Mockito.anyBoolean());
//    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).jobOutput();
//    	Whitebox.setInternalState(zosBatchJobSpy, "outputComplete", false);
//        Assert.assertEquals("listSpoolFiles() should return expected value", zosBatchJobOutputMock, zosBatchJobSpy.listSpoolFiles());
//        
//        Whitebox.setInternalState(zosBatchJobSpy, "outputComplete", true);
//        Assert.assertEquals("listSpoolFiles() should return expected value", zosBatchJobOutputMock, zosBatchJobSpy.listSpoolFiles());
//    }
//    
//    @Test
//    public void testRetrieveOutput() throws Exception {        
//    	Mockito.doNothing().when(zosBatchJobSpy).getOutput(Mockito.anyBoolean());
//    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).jobOutput();
//    	Whitebox.setInternalState(zosBatchJobSpy, "outputComplete", false);
//        Assert.assertEquals("retrieveOutput() should return expected value", zosBatchJobOutputMock, zosBatchJobSpy.retrieveOutput());
//        
//        Whitebox.setInternalState(zosBatchJobSpy, "outputComplete", true);
//        Assert.assertEquals("retrieveOutput() should return expected value", zosBatchJobOutputMock, zosBatchJobSpy.retrieveOutput());
//    }
//    
//    @Test
//    public void testRetrieveOutputNotSubmittedException() throws ZosBatchException {
//    	Mockito.doReturn("????????").when(zosBatchJobSpy).getJobId();
//        String expectedMessage = "Job has not been submitted by manager";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.retrieveOutput();
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testRetrieveOutputZosmfException() throws ZosBatchException, ZosmfException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Mockito.doReturn(FIXED_CONTENT).when(zosBatchJobSpy).getSpoolFileContent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.retrieveOutput();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testRetrieveOutputZosmfResponseException() throws ZosBatchException, ZosmfException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Mockito.doReturn("").when(zosBatchJobSpy).getSpoolFileContent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//    
//        Mockito.when(zosmfResponseMockStatus.getContent()).thenReturn(getJsonArray());
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosmfResponseMockStatus.getJsonArrayContent()).thenThrow(new ZosmfException(EXCEPTION));
//        
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.retrieveOutput();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testRetrieveOutputZosmfResponseException1() throws ZosBatchException, ZosmfException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Mockito.doReturn(FIXED_CONTENT).when(zosBatchJobSpy).getSpoolFileContent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//    
//        Mockito.when(zosmfResponseMockStatus.getContent()).thenThrow(new ZosmfException(EXCEPTION));
//        
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.retrieveOutput();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testRetrieveOutputBadHttpResponseException() throws Exception {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Mockito.doReturn(FIXED_CONTENT).when(zosBatchJobSpy).getSpoolFileContent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        Whitebox.setInternalState(zosBatchJobSpy, "jobid", FIXED_JOBID);
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//    
//        Mockito.when(zosmfResponseMockStatus.getContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        PowerMockito.doReturn(JobStatus.UNKNOWN).when(zosBatchJobSpy).getStatus();
//        
//        String expectedMessage = "Error Retrieve job output, category:0, rc:0, reason:0, message:message\n" + 
//        		"stack:\n" + 
//        		"stack";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.retrieveOutput();
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveOutputAsString() throws Exception {
//    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).retrieveOutput();
//        Mockito.doReturn("RECORDS\n").when(zosBatchJobOutputSpoolFileMock).getRecords();
//    	List<IZosBatchJobOutputSpoolFile> spoolFiles = new ArrayList<IZosBatchJobOutputSpoolFile>();
//    	spoolFiles.add(zosBatchJobOutputSpoolFileMock);
//    	spoolFiles.add(zosBatchJobOutputSpoolFileMock);
//		Mockito.doReturn(spoolFiles).when(zosBatchJobOutputMock).getSpoolFiles();
//		String expected = "RECORDS\nRECORDS\n";
//		Assert.assertEquals("retrieveOutputAsString() should return the expected value", expected, zosBatchJobSpy.retrieveOutputAsString());
//    }
//
//    @Test
//    public void testCancelJob() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//
//        Assert.assertEquals("isComplete() should return the false", false, zosBatchJobSpy.isComplete());
//        zosBatchJobSpy.cancel();
//        Assert.assertEquals("isComplete() should return the true", true, zosBatchJobSpy.isComplete());
//        
//        zosBatchJobSpy.cancel();
//    }
//
//    @Test
//    public void testCancelZosmfException() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.cancel();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testCancelResponseException() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.cancel();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testCancelBadHttpResponseException() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        
//        String expectedMessage = "Error Cancel job, category:0, rc:0, reason:0, message:message\n" + 
//        		"stack:\n" + 
//        		"stack";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.cancel();
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testPurge() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//
//        Assert.assertEquals("isPurged() should return the false", false, zosBatchJobSpy.isPurged());
//        zosBatchJobSpy.purge();
//        Assert.assertEquals("isPurged() should return the true", true, zosBatchJobSpy.isPurged());
//        
//        zosBatchJobSpy.purge();
//    }
//    
//    @Test
//    public void testGetSpoolFile() throws ZosBatchException {    	
//    	Mockito.when(zosBatchJobOutputMock.iterator()).thenReturn(zosBatchJobOutputSpoolFileIteratorMock);
//    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).listSpoolFiles();
//        Mockito.doReturn(true, true, false).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
//        Mockito.doReturn(zosBatchJobOutputSpoolFileMock).when(zosBatchJobOutputSpoolFileIteratorMock).next();
//        Mockito.doReturn("NO_MATCH").doReturn(FIXED_DDNAME).when(zosBatchJobOutputSpoolFileMock).getDdname();
//        Mockito.doReturn(FIXED_CONTENT).when(zosBatchJobSpy).getSpoolFileContent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        Mockito.doReturn(zosBatchJobOutputSpoolFileMock).when(zosManagerMock).newZosBatchJobOutputSpoolFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        Assert.assertEquals("getSpoolFile() should return the expected object", zosBatchJobOutputSpoolFileMock, zosBatchJobSpy.getSpoolFile(FIXED_DDNAME));
//
//        Assert.assertNull("getSpoolFile() should return null", zosBatchJobSpy.getSpoolFile(FIXED_DDNAME));
//
//        Mockito.doReturn(true).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
//        Mockito.doReturn(zosBatchJobOutputSpoolFileMock).when(zosBatchJobOutputSpoolFileIteratorMock).next();
//        Mockito.doReturn(FIXED_DDNAME).when(zosBatchJobOutputSpoolFileMock).getDdname();
//        Mockito.doReturn(null).when(zosBatchJobSpy).getSpoolFileContent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getSpoolFile(FIXED_DDNAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", "DDNAME " + FIXED_DDNAME + " is empty or not found", expectedException.getMessage());
//    }
//
//    @Test
//    public void testPurgeZosmfException() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.purge();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testPurgeResponseException() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.purge();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testPurgeBadHttpResponseException() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        
//        String expectedMessage = "Error Purge job, category:0, rc:0, reason:0, message:message\n" + 
//        		"stack:\n" + 
//        		"stack";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.purge();
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSaveOutputToTestResultsArchive() throws ZosManagerException {
//    	Mockito.when(zosBatchManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        PowerMockito.doNothing().when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
//        PowerMockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobid", FIXED_JOBID);
//    	Whitebox.setInternalState(zosBatchJobSpy, "retcode", FIXED_RETCODE_0000);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobOutput", zosBatchJobOutputMock);
//        Mockito.doReturn(zosBatchJobOutputSpoolFileIteratorMock).when(zosBatchJobOutputMock).iterator();
//        Mockito.doReturn(true, false).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
//        Mockito.doReturn(zosBatchJobOutputSpoolFileMock).when(zosBatchJobOutputSpoolFileIteratorMock).next();
//        Mockito.doReturn(FIXED_JOBNAME).when(zosBatchJobOutputSpoolFileMock).getJobname();
//        Mockito.doReturn(FIXED_JOBID).when(zosBatchJobOutputSpoolFileMock).getJobid();
//        Mockito.doReturn("").when(zosBatchJobOutputSpoolFileMock).getStepname();
//        Mockito.doReturn("").when(zosBatchJobOutputSpoolFileMock).getProcstep();
//        Mockito.doReturn(FIXED_DDNAME).when(zosBatchJobOutputSpoolFileMock).getDdname();
//        Mockito.doReturn("content").when(zosBatchJobOutputSpoolFileMock).getRecords();
//        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
//
//        String expectedMessage = "Archiving batch job " + FIXED_JOBNAME + "(" + FIXED_JOBID + ") to "+ FIXED_PATH_NAME;
//    	zosBatchJobSpy.saveOutputToResultsArchive(FIXED_PATH_NAME);
//        Assert.assertEquals("saveOutputToTestResultsArchive() should log expected message", expectedMessage, logMessage);
//
//        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
//    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).retrieveOutput();
//        Mockito.doReturn(FIXED_STEPNAME).when(zosBatchJobOutputSpoolFileMock).getStepname();
//        Mockito.doReturn(FIXED_PROCSTEP).when(zosBatchJobOutputSpoolFileMock).getProcstep();
//        Mockito.doReturn(true, false).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
//
//        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
//    	Mockito.doReturn(true).when(zosBatchJobOutputMock).isEmpty();
//    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).retrieveOutput();
//        Mockito.doReturn(FIXED_STEPNAME).when(zosBatchJobOutputSpoolFileMock).getStepname();
//        Mockito.doReturn(FIXED_PROCSTEP).when(zosBatchJobOutputSpoolFileMock).getProcstep();
//        Mockito.doReturn(true, false).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
//		
//    	zosBatchJobSpy.saveOutputToResultsArchive(FIXED_PATH_NAME);
//        Assert.assertEquals("saveOutputToTestResultsArchive() should log expected message", expectedMessage, logMessage);
//
//    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).jobOutput();
//        Mockito.doReturn(true, false).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
//        PowerMockito.doThrow(new ZosManagerException(EXCEPTION)).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.saveOutputToResultsArchive(FIXED_PATH_NAME);
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testGetOutput() throws ZosBatchException, ZosmfException {
//    	PowerMockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        PowerMockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//        Mockito.when(zosManagerMock.newZosBatchJobOutput(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosBatchJobOutputMock);
//        
//        Mockito.when(zosmfResponseMockStatus.getJsonArrayContent()).thenReturn(getJsonArray());
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        PowerMockito.doReturn("").when(zosBatchJobSpy).getSpoolFileContent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        zosBatchJobSpy.getOutput(true);
//    	
//    	PowerMockito.doReturn(JobStatus.ACTIVE).when(zosBatchJobSpy).getStatus();
//    	Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        zosBatchJobSpy.getOutput(true);
//
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobNotFound", true);
//    	zosBatchJobSpy.getOutput(true);
//    	
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobNotFound", false);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
//    	zosBatchJobSpy.getOutput(true);
//    	Assert.assertTrue("getOutput() should set outputComplete to true", Whitebox.getInternalState(zosBatchJobSpy, "outputComplete"));
//    	
//    	Mockito.when(zosManagerMock.newZosBatchJobOutput(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosBatchJobOutputMock);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobOutput", zosBatchJobOutputMock);
//    	zosBatchJobSpy.getOutput(false);
//    	Assert.assertEquals("getOutput() should set jobOutput", zosBatchJobOutputMock, zosBatchJobSpy.jobOutput());
//    }
//
//    @Test
//    public void testUpdateJobStatus() throws ZosBatchException, ZosmfException  {
//        Whitebox.setInternalState(zosBatchJobSpy, "status", (String) null);
//        Whitebox.setInternalState(zosBatchJobSpy, "retcode", (String) null);
//        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//        JsonObject jsonObject = getJsonObject();
//        jsonObject.remove("status");
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        zosBatchJobSpy.updateJobStatus();
//        Assert.assertFalse("jobComplete should be false", zosBatchJobSpy.isComplete());
//        
//
//        Whitebox.setInternalState(zosBatchJobSpy, "status", (String) null);
//        jsonObject.addProperty("status", "STATUS");
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
//        
//        zosBatchJobSpy.updateJobStatus();
//        Assert.assertFalse("jobComplete should be false", zosBatchJobSpy.isComplete());
//
//        
//        Whitebox.setInternalState(zosBatchJobSpy, "status", (String) null);
//        jsonObject.addProperty("status", "OUTPUT");
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
//        
//        zosBatchJobSpy.updateJobStatus();
//        Assert.assertTrue("jobComplete should be true", zosBatchJobSpy.isComplete());
//        
//
//        Whitebox.setInternalState(zosBatchJobSpy, "retcode", (String) null);
//        jsonObject.remove("retcode");
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
//        
//        zosBatchJobSpy.updateJobStatus();
//        Assert.assertEquals("retcode should be ????", "????", zosBatchJobSpy.getRetcode());
//
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
//        jsonObject.addProperty("rc", 4);
//        jsonObject.addProperty("reason", 10);
//        zosBatchJobSpy.updateJobStatus();
//        Assert.assertTrue("jobNotFound should be true", Whitebox.getInternalState(zosBatchJobSpy, "jobNotFound"));
//
//    }
//
//    @Test
//    public void testUpdateJobStatusZosmfException() throws ZosBatchException, ZosmfException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.updateJobStatus();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testUpdateJobStatusZosmfResponseException() throws ZosBatchException, ZosmfException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.updateJobStatus();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testUpdateJobStatusBadHttpResponseException1() throws ZosBatchException, ZosmfException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        String expectedMessage = "Error Update job status, category:0, rc:0, reason:0, message:message\n" + 
//        		"stack:\n" + 
//        		"stack";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.updateJobStatus();
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testUpdateJobStatusBadHttpResponseException2() throws ZosBatchException, ZosmfException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//
//        JsonObject jsonObject = getJsonObject();
//        jsonObject.addProperty("rc", 4);
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
//
//        String expectedMessage = "Error Update job status, category:0, rc:4, reason:0, message:message\n" + 
//        		"stack:\n" + 
//        		"stack";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.updateJobStatus();
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testUpdateJobStatusBadHttpResponseException3() throws ZosBatchException, ZosmfException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//
//        JsonObject jsonObject = getJsonObject();
//        jsonObject.addProperty("reason", 10);
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
//
//        String expectedMessage = "Error Update job status, category:0, rc:0, reason:10, message:message\n" + 
//        		"stack:\n" + 
//        		"stack";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.updateJobStatus();
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testAddOutputFileContent() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Whitebox.setInternalState(zosBatchJobSpy, "jobOutput", zosBatchJobOutputMock);
//
//        zosBatchJobSpy.getSpoolFileContent(null, null, null, null);
//        
//        zosBatchJobSpy.getSpoolFileContent(FIXED_ID, FIXED_STEPNAME, FIXED_PROCSTEP, FIXED_DDNAME);
//
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
//        PowerMockito.doReturn(true).when(zosBatchJobSpy).spoolFileNotFound(Mockito.any());
//        zosBatchJobSpy.getSpoolFileContent(FIXED_ID, FIXED_STEPNAME, FIXED_PROCSTEP, FIXED_DDNAME);
//        
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getSpoolFileContent(null, null, null, null);
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testAddOutputFileContentZosmfResponseException1() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        Mockito.when(zosmfResponseMockStatus.getTextContent()).thenThrow(new ZosmfException(EXCEPTION));
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getSpoolFileContent(null, null, null, null);
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testAddOutputFileContentZosmfResponseException2() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//        Mockito.when(zosmfResponseMockStatus.getTextContent()).thenReturn("content");
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND); 
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getSpoolFileContent(null, null, null, null);
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testAddOutputBadHttpResponseException1() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        
//        String expectedMessage = "Error Retrieve job output, category:0, rc:0, reason:0, message:message\n" + 
//        		"stack:\n" + 
//        		"stack";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getSpoolFileContent(null, null, null, null);
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testAddOutputBadHttpResponseException2() throws ZosBatchException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
//        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
//        
//        String expectedMessage = "Error Retrieve job output, category:0, rc:0, reason:0, message:message\n" + 
//        		"stack:\n" + 
//        		"stack";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getSpoolFileContent(null, null, null, null);
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testJclWithJobcard() throws ZosBatchManagerException {
//        Whitebox.setInternalState(zosBatchJobSpy, "useSysaff", false);
//        String jobcard = "//" + FIXED_JOBNAME + " JOB @@@@,\n" + 
//                         "//         CLASS=A,\n" + 
//                         "//         MSGCLASS=A,\n" + 
//                         "//         MSGLEVEL=(1,1)";
//        jobcard.replace("@@@@", "");
//        
//        
//        String expectedJobcard = "//" + FIXED_JOBNAME + " JOB \nJCL\n";
//        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobSpy.jclWithJobcard());
//        
//        Whitebox.setInternalState(zosBatchJobSpy, "useSysaff", true);
//        expectedJobcard = "//" + FIXED_JOBNAME + " JOB \n/*JOBPARM SYSAFF=" + FIXED_IMAGE_ID + "\nJCL\n";
//        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobSpy.jclWithJobcard());
//
//        Whitebox.setInternalState(zosBatchJobSpy, "jcl", "JCL\n");
//        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobSpy.jclWithJobcard());
//    }
//    
//    @Test
//    public void testBuildErrorString() {
//        String expectedString = "Error action";
//        String returnString = ZosmfZosBatchJobImpl.buildErrorString("action", new JsonObject());
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        JsonObject jsonObject = getJsonObject();
//        jsonObject.addProperty("details", "details");
//        expectedString = "Error action, category:0, rc:0, reason:0, message:message\n" + 
//                "details:details\n" + 
//                "stack:\n" + 
//                "stack";
//        returnString = ZosmfZosBatchJobImpl.buildErrorString("action", jsonObject);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        jsonObject.remove("details");
//        JsonArray jsonArray = new JsonArray();
//        JsonPrimitive item = new JsonPrimitive("details line 1");
//        jsonArray.add(item);
//        item = new JsonPrimitive("details line 2");
//        jsonArray.add(item);
//        jsonObject.add("details", jsonArray);
//        expectedString = "Error action, category:0, rc:0, reason:0, message:message\n" + 
//                "details:\n" +
//                "details line 1\n" +
//                "details line 2\n" + 
//                "stack:\n" + 
//                "stack";
//        returnString = ZosmfZosBatchJobImpl.buildErrorString("action", jsonObject);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//
//        jsonObject.remove("stack");
//        expectedString = "Error action, category:0, rc:0, reason:0, message:message\n" + 
//                "details:\n" +
//                "details line 1\n" +
//                "details line 2";
//        returnString = ZosmfZosBatchJobImpl.buildErrorString("action", jsonObject);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//    }
//    
//    @Test
//    public void testArchiveJobOutput() throws Exception {
//    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).retrieveOutput();
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobid", FIXED_JOBID);
//    	Whitebox.setInternalState(zosBatchJobSpy, "retcode", FIXED_RETCODE_0000);
//    	Mockito.doNothing().when(zosBatchJobSpy).saveOutputToResultsArchive(Mockito.any());
//    	
//    	zosBatchJobSpy.setShouldArchive(true);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", false);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
//    	PowerMockito.doReturn(JobStatus.UNKNOWN).when(zosBatchJobSpy).getStatus();
//    	zosBatchJobSpy.archiveJobOutput();
//    	PowerMockito.verifyPrivate(zosBatchJobSpy, Mockito.times(1)).invoke("saveOutputToResultsArchive", Mockito.any());
//
//    	Mockito.clearInvocations(zosBatchJobSpy);
//    	zosBatchJobSpy.setShouldArchive(true);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", true);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
//    	PowerMockito.doReturn(JobStatus.UNKNOWN).when(zosBatchJobSpy).getStatus();
//    	zosBatchJobSpy.archiveJobOutput();
//    	PowerMockito.verifyPrivate(zosBatchJobSpy, Mockito.times(0)).invoke("saveOutputToResultsArchive", Mockito.any());
//
//    	Mockito.clearInvocations(zosBatchJobSpy);
//    	zosBatchJobSpy.setShouldArchive(true);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", true);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
//    	PowerMockito.doReturn(JobStatus.UNKNOWN).when(zosBatchJobSpy).getStatus();
//    	zosBatchJobSpy.archiveJobOutput();
//    	PowerMockito.verifyPrivate(zosBatchJobSpy, Mockito.times(1)).invoke("saveOutputToResultsArchive", Mockito.any());
//
//    	Mockito.clearInvocations(zosBatchJobSpy);
//    	zosBatchJobSpy.setShouldArchive(false);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", true);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
//    	PowerMockito.doReturn(JobStatus.UNKNOWN).when(zosBatchJobSpy).getStatus();
//    	zosBatchJobSpy.archiveJobOutput();
//    	PowerMockito.verifyPrivate(zosBatchJobSpy, Mockito.times(0)).invoke("saveOutputToResultsArchive", Mockito.any());
//
//    	Mockito.clearInvocations(zosBatchJobSpy);
//    	zosBatchJobSpy.setShouldArchive(true);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", true);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
//    	PowerMockito.doReturn(JobStatus.NOTFOUND).when(zosBatchJobSpy).getStatus();
//    	zosBatchJobSpy.archiveJobOutput();
//    	PowerMockito.verifyPrivate(zosBatchJobSpy, Mockito.times(0)).invoke("saveOutputToResultsArchive", Mockito.any());
//    }
//    
//    @Test
//    public void testJosnNull() {
//        JsonObject JsonObject = new JsonObject();
//        Assert.assertNull("jsonNull() should return null", zosBatchJobSpy.jsonNull(JsonObject, "none"));
//
//        JsonObject.add("empty", JsonNull.INSTANCE);
//        Assert.assertNull("jsonNull() should return null", zosBatchJobSpy.jsonNull(JsonObject, "empty"));
//
//        JsonObject.addProperty("property", "value");
//        Assert.assertEquals("jsonNull() should return value", "value", zosBatchJobSpy.jsonNull(JsonObject, "property"));
//    }
//    
//    @Test
//    public void testSpoolFileNotFound() {
//        JsonObject JsonObject = new JsonObject();
//        Assert.assertFalse("spoolFileNotFound() should return false", zosBatchJobSpy.spoolFileNotFound(JsonObject));
//
//        JsonObject.addProperty("category", 6);
//        Assert.assertFalse("spoolFileNotFound() should return false", zosBatchJobSpy.spoolFileNotFound(JsonObject));
//
//        JsonObject.addProperty("rc", 4);
//        Assert.assertFalse("spoolFileNotFound() should return false", zosBatchJobSpy.spoolFileNotFound(JsonObject));
//
//        JsonObject.addProperty("reason", 12);
//        Assert.assertTrue("spoolFileNotFound() should return true", zosBatchJobSpy.spoolFileNotFound(JsonObject));
//    }
//    
//    @Test
//    public void testJosnZero() {
//        JsonObject JsonObject = new JsonObject();
//        Assert.assertEquals("jsonZero() should return 0", 0, zosBatchJobSpy.jsonZero(JsonObject, "none"));
//
//        JsonObject.add("empty", JsonNull.INSTANCE);
//        Assert.assertEquals("jsonZero() should return 0", 0, zosBatchJobSpy.jsonZero(JsonObject, "empty"));
//
//        JsonObject.addProperty("property", 99);
//        Assert.assertEquals("jsonZero() should return 99", 99, zosBatchJobSpy.jsonZero(JsonObject, "property"));
//    }
//    
//    @Test
//    public void testToString() throws ZosBatchException {
//        PowerMockito.doNothing().when(zosBatchJobSpy).updateJobStatus();        
//        PowerMockito.doReturn(true).when(zosBatchJobSpy).isPurged();
//        Whitebox.setInternalState(zosBatchJobSpy, "jobid", "#JOBID#");
//        Whitebox.setInternalState(zosBatchJobSpy, "status", JobStatus.UNKNOWN);
//        Whitebox.setInternalState(zosBatchJobSpy, "owner", "#OWNER#");
//        Whitebox.setInternalState(zosBatchJobSpy, "type", "#TYPE#");
//        Whitebox.setInternalState(zosBatchJobSpy, "retcode", "#RETCODE#");
//        Whitebox.setInternalState(zosBatchJobSpy, "retcode", "#RETCODE#");
//        String expectedString = FIXED_JOBNAME + "(#JOBID#)";
//        Assert.assertEquals("toString() should return supplied value", expectedString, zosBatchJobSpy.toString());
//        
//        PowerMockito.doReturn(false).when(zosBatchJobSpy).isPurged();
//        Assert.assertEquals("toString() should return supplied value", expectedString, zosBatchJobSpy.toString());
//        
//        PowerMockito.doThrow(new ZosBatchException(EXCEPTION)).when(zosBatchJobSpy).updateJobStatus();
//        zosBatchJobSpy.toString();
//    }
//    
//    @Test
//    public void testTruncateJcl() throws ZosBatchManagerException {
//        Mockito.when(zosManagerMock.getZosBatchPropertyTruncateJCLRecords(Mockito.any())).thenReturn(false);
//        
//        String suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--\n" +
//                             "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--";
//        String returnedJcl = suppliedJcl;
//        Assert.assertEquals("JCL should not be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
//        Assert.assertEquals("The value of intdrLrecl should be 80", 80, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
//        Assert.assertEquals("the value of intdrRecfm should be F", "F", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));
//        
//        Whitebox.setInternalState(zosBatchJobSpy, "intdrLrecl", 80);
//        Whitebox.setInternalState(zosBatchJobSpy, "intdrRecfm", "F");
//        suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9";
//        returnedJcl = suppliedJcl;
//        Assert.assertEquals("JCL should not be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
//        Assert.assertEquals("The value of intdrLrecl should be 90", 90, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
//        Assert.assertEquals("the value of intdrRecfm should be F", "F", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));
//        
//        Whitebox.setInternalState(zosBatchJobSpy, "intdrLrecl", 80);
//        Whitebox.setInternalState(zosBatchJobSpy, "intdrRecfm", "F");
//        suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8";
//        returnedJcl = suppliedJcl;
//        Assert.assertEquals("JCL should not be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
//        Assert.assertEquals("The value of intdrLrecl should be 90", 90, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
//        Assert.assertEquals("the value of intdrRecfm should be V", "V", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));
//        
//        Mockito.when(zosManagerMock.getZosBatchPropertyTruncateJCLRecords(Mockito.any())).thenReturn(true);
//        Whitebox.setInternalState(zosBatchJobSpy, "intdrLrecl", 80);
//        Whitebox.setInternalState(zosBatchJobSpy, "intdrRecfm", "F");
//        suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--";
//        returnedJcl = suppliedJcl;
//        Assert.assertEquals("JCL should not be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
//        Assert.assertEquals("The value of intdrLrecl should be 80", 80, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
//        Assert.assertEquals("the value of intdrRecfm should be F", "F", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));
//    
//        Whitebox.setInternalState(zosBatchJobSpy, "intdrLrecl", 80);
//        Whitebox.setInternalState(zosBatchJobSpy, "intdrRecfm", "F");
//        suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9";
//        returnedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8\n" +
//                        "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8";
//        Assert.assertEquals("JCL should be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
//        Assert.assertEquals("The value of intdrLrecl should be 80", 80, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
//        Assert.assertEquals("the value of intdrRecfm should be F", "F", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));
//        
//        Whitebox.setInternalState(zosBatchJobSpy, "intdrLrecl", 80);
//        Whitebox.setInternalState(zosBatchJobSpy, "intdrRecfm", "F");
//        suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8";
//        returnedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8\n" +
//                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8";
//        Assert.assertEquals("JCL should not be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
//        Assert.assertEquals("The value of intdrLrecl should be 80", 80, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
//        Assert.assertEquals("the value of intdrRecfm should be F", "F", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));
//
//        String expectedMessage = "Unable to get trucate JCL records property value";
//        Mockito.when(zosManagerMock.getZosBatchPropertyTruncateJCLRecords(Mockito.anyString())).thenThrow(new ZosBatchManagerException(EXCEPTION));
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.parseJcl("");
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSubmitted() {
//    	Mockito.doReturn("????????").when(zosBatchJobSpy).getJobId();
//    	Assert.assertFalse("submitted() should return false", zosBatchJobSpy.submitted());
//    	Mockito.doReturn(FIXED_JOBID).when(zosBatchJobSpy).getJobId();
//    	Assert.assertTrue("submitted() should return true", zosBatchJobSpy.submitted());
//    }
//    
//    @Test
//    public void testShouldCleanup() {
//    	zosBatchJobSpy.setShouldCleanup(true);
//    	Assert.assertTrue("shouldCleanup() should return true", zosBatchJobSpy.shouldCleanup());
//    	zosBatchJobSpy.setShouldCleanup(false);
//    	Assert.assertFalse("shouldCleanup() should return false", zosBatchJobSpy.shouldCleanup());
//    }
//    
//    @Test
//    public void testSaveSpoolFileToResultsArchive() throws ZosBatchException {
//    	Mockito.doNothing().when(zosBatchJobSpy).saveSpoolFile(Mockito.any(), Mockito.any());
//    	zosBatchJobSpy.saveSpoolFileToResultsArchive(zosBatchJobOutputSpoolFileMock, "path");
//    	Mockito.verify(zosBatchJobSpy, Mockito.times(1)).saveSpoolFile(Mockito.any(), Mockito.any());
//    }
//                 
//    private Path newMockedPath(boolean fileExists) throws IOException {
//        Path pathMock = Mockito.mock(Path.class);
//        FileSystem fileSystemMock = Mockito.mock(FileSystem.class);
//        FileSystemProvider fileSystemProviderMock = Mockito.mock(FileSystemProvider.class);
//        OutputStream outputStreamMock = Mockito.mock(OutputStream.class);
//        Mockito.when(pathMock.resolve(Mockito.anyString())).thenReturn(pathMock);
//        Mockito.when(pathMock.toString()).thenReturn(FIXED_PATH_NAME);
//        Mockito.when(pathMock.getFileSystem()).thenReturn(fileSystemMock);
//        Mockito.when(fileSystemMock.provider()).thenReturn(fileSystemProviderMock);
//        SeekableByteChannel seekableByteChannelMock = Mockito.mock(SeekableByteChannel.class);
//        Mockito.when(fileSystemProviderMock.newByteChannel(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(seekableByteChannelMock);
//        Mockito.when(fileSystemProviderMock.newOutputStream(Mockito.any(Path.class), Mockito.any())).thenReturn(outputStreamMock);
//        if (!fileExists) {
//            Mockito.doThrow(new IOException()).when(fileSystemProviderMock).checkAccess(Mockito.any(), Mockito.any());
//        }
//        return pathMock;
//    }
//    
//    private JsonObject getJsonObject() {
//        JsonObject responseBody = new JsonObject();
//        responseBody.addProperty("jobname", FIXED_JOBNAME);
//        responseBody.addProperty("jobid", FIXED_JOBID);
//        responseBody.addProperty("owner", FIXED_OWNER);
//        responseBody.addProperty("type", FIXED_TYPE);
//        responseBody.addProperty("retcode", FIXED_RETCODE_0000);
//        responseBody.addProperty("status", FIXED_STATUS_OUTPUT);
//        responseBody.addProperty("category", 0);
//        responseBody.addProperty("rc", 0);
//        responseBody.addProperty("reason", 0);
//        responseBody.addProperty("message", "message");
//        responseBody.addProperty("stack", "stack");
//        responseBody.addProperty("id", 1);
//        responseBody.addProperty("ddname", "ddname");
//        responseBody.addProperty("stepname", "stepname");
//        responseBody.addProperty("procstep", "procstep");
//        return responseBody;
//    }
//    
//    private JsonArray getJsonArray() {
//        JsonArray fileArray = new JsonArray();
//        fileArray.add(getJsonObject());
//        return fileArray;
//    }
}
