/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.rseapi.manager.internal;

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
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.internal.RseapiManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LogFactory.class})
public class TestRseapiZosBatchJobImpl {
//    
//    private RseapiZosBatchJobImpl zosBatchJob;
//    
//    private RseapiZosBatchJobImpl zosBatchJobSpy;
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
//    private RseapiManagerImpl rseapiManagerMock;
//    
//    @Mock
//    private RseapiZosBatchManagerImpl zosBatchManagerMock;
//    
//    @Mock
//    private IRseapiRestApiProcessor rseapiApiProcessorMock;
//    
//    @Mock
//    private IRseapiResponse rseapiResponseMock;
//    
//    @Mock
//    private IRseapiResponse rseapiResponseMockSubmit;
//    
//    @Mock
//    private IRseapiResponse rseapiResponseMockStatus;
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
//    private static final String FIXED_STATUS_COMPLETED = "COMPLETED";
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
//	private static final String FIXED_CONTENT = "PROCSTEP";
//
//	private static final String FIXED_PATH = "PATH";
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
//        Mockito.when(zosManagerMock.newZosBatchJobOutput(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosBatchJobOutputMock);        
//        
//        Path archivePathMock = newMockedPath(false);
//        Mockito.when(zosBatchManagerMock.getArchivePath()).thenReturn(archivePathMock);
//        Mockito.when(zosBatchManagerMock.getArtifactsRoot()).thenReturn(archivePathMock);
//        Path currentTestMethodArchiveFolderMock = newMockedPath(false);
//        Mockito.when(zosBatchManagerMock.getCurrentTestMethodArchiveFolder()).thenReturn(currentTestMethodArchiveFolderMock);
//
//        PowerMockito.doReturn(rseapiApiProcessorMock).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//        Mockito.when(zosBatchManagerMock.getRseapiManager()).thenReturn(rseapiManagerMock);
//        Mockito.when(zosBatchManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        Mockito.when(zosManagerMock.buildUniquePathName(Mockito.any(), Mockito.any())).thenReturn(FIXED_PATH_NAME);
//        
//        Mockito.when(zosBatchJobcardMock.getJobcard(Mockito.any(), Mockito.any())).thenReturn(FIXED_JOBCARD);
//        
//        zosBatchJob = new RseapiZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", zosBatchJobcardMock);
//        zosBatchJobSpy = Mockito.spy(zosBatchJob);
//    }
//
//    @Test
//    public void testConstructor() throws ZosBatchException {
//        Assert.assertEquals("getJobname() should return the supplied job name", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
//        
//        zosBatchJob = new RseapiZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", null);
//        Assert.assertEquals("getJobname() should return the supplied job name", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
//        
//        zosBatchJob = new RseapiZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, null, null);
//        Assert.assertEquals("getJobname() should return the supplied job name", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
//    }
//    
//    @Test
//    public void testConstructorJobWaitTimeoutException() throws ZosBatchManagerException {
//        String expectedMessage = "Unable to get job timeout property value";
//        Mockito.when(zosManagerMock.getZosBatchPropertyJobWaitTimeout(Mockito.anyString())).thenThrow(new ZosBatchManagerException(EXCEPTION));
//    	ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//    		new RseapiZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", null);
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testConstructorUseSysaffException() throws ZosBatchManagerException {
//        String expectedMessage = "Unable to get use SYSAFF property value";
//        Mockito.when(zosManagerMock.getZosBatchPropertyUseSysaff(Mockito.any())).thenThrow(new ZosBatchManagerException(EXCEPTION));
//    	ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//    		new RseapiZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", null);
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testConstructorRestrictToImageException() throws ZosBatchManagerException {
//        Mockito.when(zosManagerMock.getZosBatchPropertyBatchRestrictToImage(Mockito.any())).thenThrow(new ZosBatchManagerException(EXCEPTION));
//    	ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//    		new RseapiZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", null);
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testConstructorStoreArtifactException() throws ZosManagerException {
//        PowerMockito.doThrow(new ZosManagerException(EXCEPTION)).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
//    	ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//    		new RseapiZosBatchJobImpl(zosBatchManagerMock, zosImageMock, zosJobnameMock, "JCL", null);
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
//        Assert.assertEquals("getStatus() should return the expected value", JobStatus.UNKNOWN, zosBatchJobSpy.getStatus());
//        zosBatchJobSpy.setStatus("????????");
//        Assert.assertEquals("getStatus() should return the expected value", JobStatus.UNKNOWN, zosBatchJobSpy.getStatus());
//        zosBatchJobSpy.setStatus("HOLD");
//        Assert.assertEquals("getStatus() should return the expected value", JobStatus.INPUT, zosBatchJobSpy.getStatus());
//        zosBatchJobSpy.setStatus("ACTIVE");
//        Assert.assertEquals("getStatus() should return the expected value", JobStatus.ACTIVE, zosBatchJobSpy.getStatus());
//        zosBatchJobSpy.setStatus("ABEND");
//        Assert.assertEquals("getStatus() should return the expected value", JobStatus.OUTPUT, zosBatchJobSpy.getStatus());
//        zosBatchJobSpy.setStatus("COMPLETED");
//        Assert.assertEquals("getStatus() should return the expected value", JobStatus.OUTPUT, zosBatchJobSpy.getStatus());
//        zosBatchJobSpy.setStatus("COMPLETION");
//        Assert.assertEquals("getStatus() should return the expected value", JobStatus.OUTPUT, zosBatchJobSpy.getStatus());
//
//        zosBatchJobSpy.setStatus(null);
//    	Mockito.doThrow(new ZosBatchException(EXCEPTION)).when(zosBatchJobSpy).updateJobStatus();
//        zosBatchJobSpy.getStatus();
//        Assert.assertTrue("method should log expected exception", logException instanceof ZosBatchException);
//        Assert.assertEquals("method should log expected exception message", EXCEPTION, logException.getMessage());
//    }
//    
//    @Test
//    public void testGetStatusString() throws ZosBatchException {
//    	Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Assert.assertEquals("getStatusString() should return the expected value", JobStatus.UNKNOWN.toString(), zosBatchJobSpy.getStatusString());
//        zosBatchJobSpy.setStatusString(FIXED_STATUS_COMPLETED);
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
//    public void testSubmitJob() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockSubmit);
//        Mockito.when(rseapiResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(rseapiResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
//        
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        zosBatchJobSpy.submitJob();
//        Assert.assertEquals("getJobname().getName() should return the supplied value", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
//    }
//    
//    @Test
//    public void testSubmitJobIRseapiRestApiProcessorSendRequestException() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.submitJob();
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testSubmitIRseapiResponseGetJsonContentException() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockSubmit);
//        Mockito.when(rseapiResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
//        Mockito.when(rseapiResponseMockSubmit.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.submitJob();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testSubmitJobNotStatusCodeCreated() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockSubmit);
//        Mockito.when(rseapiResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMockSubmit.getStatusLine()).thenReturn("NOT_FOUND");
//        Mockito.when(rseapiResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
//        String expectedMessage = "Error Submit job, HTTP Status Code 404 : NOT_FOUND";
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//        	zosBatchJobSpy.submitJob();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testWaitForJob() throws RseapiException, ZosBatchManagerException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//
//        JsonObject responseBody = getJsonObject();
//        responseBody.addProperty("status", "COMPLETION");
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Assert.assertEquals("waitForJob() should return zero", 0, zosBatchJobSpy.waitForJob());
//
//        responseBody = getJsonObject();
//        responseBody.addProperty("returnCode", FIXED_RETCODE_0020);
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Assert.assertEquals("waitForJob() should return the supplied value", 20, zosBatchJobSpy.waitForJob());
//        
//        responseBody.addProperty("returnCode", "CC UNKNOWN");
//        Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());
//
//        responseBody.remove("returnCode");
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
//    	Assert.assertEquals("retrieveOutput() should return expected value", zosBatchJobOutputMock, zosBatchJobSpy.listSpoolFiles());
//        
//        Whitebox.setInternalState(zosBatchJobSpy, "outputComplete", true);
//        Assert.assertEquals("retrieveOutput() should return expected value", zosBatchJobOutputMock, zosBatchJobSpy.listSpoolFiles());
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
//    public void testRetrieveOutputRseapiException() throws ZosBatchException, RseapiException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Mockito.doReturn(FIXED_CONTENT).when(zosBatchJobSpy).getOutputFileContent(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.retrieveOutput();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
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
//    public void testCancel() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//
//        Assert.assertEquals("isComplete() should return the false", false, zosBatchJobSpy.isComplete());
//        zosBatchJobSpy.cancel();
//        Assert.assertEquals("isComplete() should return the true", true, zosBatchJobSpy.isComplete());
//        
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
//        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
//        zosBatchJobSpy.cancel();
//        Assert.assertEquals("isComplete() should return the true", true, zosBatchJobSpy.isComplete());
//        
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
//        zosBatchJobSpy.cancel();
//        Assert.assertEquals("isComplete() should return the true", true, zosBatchJobSpy.isComplete());
//        
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
//        zosBatchJobSpy.cancel();
//        Assert.assertEquals("isComplete() should return the true", true, zosBatchJobSpy.isComplete());
//    }
//
//    @Test
//    public void testCancelRseapiException() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.cancel();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testCancelBadHttpResponseException() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);
//        Mockito.when(rseapiResponseMockStatus.getStatusLine()).thenReturn("SC_FORBIDDEN");
//        String expectedMessage = "Error Cancel job, HTTP Status Code 403 : SC_FORBIDDEN";
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//        	zosBatchJobSpy.cancel();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testPurge() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Assert.assertEquals("isPurged() should return the false", false, zosBatchJobSpy.isPurged());
//        zosBatchJobSpy.purge();
//        Assert.assertEquals("isPurged() should return the true", true, zosBatchJobSpy.isPurged());
//        
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
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
//        Mockito.doReturn(FIXED_CONTENT).when(zosBatchJobSpy).getOutputFileContent(Mockito.any());
//        Mockito.doReturn(zosBatchJobOutputSpoolFileMock).when(zosManagerMock).newZosBatchJobOutputSpoolFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        Assert.assertEquals("getSpoolFile() should return the expected object", zosBatchJobOutputSpoolFileMock, zosBatchJobSpy.getSpoolFile(FIXED_DDNAME));
//
//        Assert.assertNull("getSpoolFile() should return null", zosBatchJobSpy.getSpoolFile(FIXED_DDNAME));
//
//        Mockito.doReturn(true).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
//        Mockito.doReturn(zosBatchJobOutputSpoolFileMock).when(zosBatchJobOutputSpoolFileIteratorMock).next();
//        Mockito.doReturn(FIXED_DDNAME).when(zosBatchJobOutputSpoolFileMock).getDdname();
//        Mockito.doReturn(null).when(zosBatchJobSpy).getOutputFileContent(Mockito.any());
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getSpoolFile(FIXED_DDNAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", "DDNAME " + FIXED_DDNAME + " is empty or not found", expectedException.getMessage());
//    }
//
//    @Test
//    public void testPurgeRseapiException() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.purge();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testPurgeBadHttpResponseException() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);
//        Mockito.when(rseapiResponseMockStatus.getStatusLine()).thenReturn("SC_FORBIDDEN");
//        String expectedMessage = "Error Purge job, HTTP Status Code 403 : SC_FORBIDDEN";
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//        	zosBatchJobSpy.purge();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    
//    @Test
//    public void testSaveOutputToTestResultsArchive() throws ZosManagerException {
//    	Mockito.when(zosBatchManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        PowerMockito.doNothing().when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
//        PowerMockito.doReturn(FIXED_PATH_NAME).when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
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
//    public void testGetOutput() throws ZosBatchException, RseapiException {
//    	PowerMockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        PowerMockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiResponseMockStatus.getContent()).thenReturn(getJsonObject());
//        PowerMockito.doReturn(FIXED_CONTENT).when(zosBatchJobSpy).getOutputFileContent(Mockito.any());
//    	zosBatchJobSpy.getOutput(true);
//    	
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobNotFound", true);
//    	zosBatchJobSpy.getOutput(true);
//    	
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobNotFound", false);
//    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
//    	zosBatchJobSpy.getOutput(true);
//    	Assert.assertTrue("getOutput() should set outputComplete to true", Whitebox.getInternalState(zosBatchJobSpy, "outputComplete"));
//
//    	zosBatchJobSpy.getOutput(false);
//    	Assert.assertEquals("getOutput() should set jobOutput", zosBatchJobOutputMock, zosBatchJobSpy.jobOutput());
//    }
//    
//    @Test
//    public void testGetOutputException1() throws ZosBatchException, RseapiException {
//    	PowerMockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        PowerMockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//        
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMockStatus.getStatusLine()).thenReturn("NOT_FOUND");
//        String expectedMessage = "Error Retrieve job output, HTTP Status Code 404 : NOT_FOUND";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getOutput(true);
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetOutputException2() throws ZosBatchException, RseapiException {
//    	PowerMockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        PowerMockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiResponseMockStatus.getContent()).thenThrow(new RseapiException(EXCEPTION));
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getOutput(true);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testUpdateJobStatus() throws ZosBatchException, RseapiException  {
//        Whitebox.setInternalState(zosBatchJobSpy, "status", (String) null);
//        Whitebox.setInternalState(zosBatchJobSpy, "retcode", (String) null);
//        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//        JsonObject jsonObject = getJsonObject();
//        jsonObject.remove("status");
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        zosBatchJobSpy.updateJobStatus();
//        Assert.assertFalse("jobComplete should be false", zosBatchJobSpy.isComplete());
//        
//
//        Whitebox.setInternalState(zosBatchJobSpy, "status", (String) null);
//        jsonObject.addProperty("status", "STATUS");
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
//        
//        zosBatchJobSpy.updateJobStatus();
//        Assert.assertFalse("jobComplete should be false", zosBatchJobSpy.isComplete());
//
//        
//        Whitebox.setInternalState(zosBatchJobSpy, "status", (String) null);
//        jsonObject.addProperty("status", "COMPLETION");
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
//
//        
//        Whitebox.setInternalState(zosBatchJobSpy, "status", (String) null);
//        jsonObject.addProperty("status", "ABEND");
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
//        
//        zosBatchJobSpy.updateJobStatus();
//        Assert.assertTrue("jobComplete should be true", zosBatchJobSpy.isComplete());
//        
//
//        Whitebox.setInternalState(zosBatchJobSpy, "retcode", (String) null);
//        jsonObject.remove("returnCode");
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
//        
//        zosBatchJobSpy.updateJobStatus();
//        Assert.assertEquals("retcode should be ????", "????", zosBatchJobSpy.getRetcode());
//
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        jsonObject.addProperty("status", "NOT_FOUND");
//        zosBatchJobSpy.updateJobStatus();
//        Assert.assertEquals("status should be NOTFOUND", JobStatus.NOTFOUND, zosBatchJobSpy.getStatus());
//        
//
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
//        Mockito.when(rseapiResponseMockStatus.getStatusLine()).thenReturn("BAD_REQUEST");
//        String expectedMessage = "Error Update job status, HTTP Status Code 400 : BAD_REQUEST";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.updateJobStatus();
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testUpdateJobStatusGetJsonContentException() throws ZosBatchException, RseapiException {
//        Whitebox.setInternalState(zosBatchJobSpy, "status", (String) null);
//        Whitebox.setInternalState(zosBatchJobSpy, "retcode", (String) null);
//        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.updateJobStatus();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testUpdateJobStatusRseapiException() throws ZosBatchException, RseapiException {
//        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.updateJobStatus();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testGetOutputFileContent() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        PowerMockito.doReturn(JobStatus.ACTIVE).when(zosBatchJobSpy).getStatus();
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
//        Whitebox.setInternalState(zosBatchJobSpy, "jobOutput", zosBatchJobOutputMock);
//
//        zosBatchJobSpy.getOutputFileContent(null);
//        
//        zosBatchJobSpy.getOutputFileContent(FIXED_PATH);
//
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Assert.assertNull("getOutputFileContent() should return null", zosBatchJobSpy.getOutputFileContent(FIXED_PATH));
//
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);
//        Mockito.when(rseapiResponseMockStatus.getStatusLine()).thenReturn("FORBIDDEN");
//        String expectedMessage = "Error Retrieve job output, HTTP Status Code 403 : FORBIDDEN";
//    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getOutputFileContent(FIXED_PATH);
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//
//        PowerMockito.doReturn(JobStatus.UNKNOWN).when(zosBatchJobSpy).getStatus();
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMockStatus.getStatusLine()).thenReturn("NOT_FOUND");
//        expectedMessage = "Error Retrieve job output, HTTP Status Code 404 : NOT_FOUND";
//    	expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getOutputFileContent(FIXED_PATH);
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetOutputFileContentException1() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
//        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getOutputFileContent(null);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testGetOutputFileContentException2() throws ZosBatchException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
//    		zosBatchJobSpy.getOutputFileContent(null);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
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
//    public void testBuildErrorString() throws RseapiException {
//    	Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//    	Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("OK");
//        String expectedString = "Error action, HTTP Status Code 200 : OK";
//        String returnString = RseapiZosBatchJobImpl.buildErrorString("action", rseapiResponseMock);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        JsonObject jsonObject = getJsonObject();
//    	Mockito.when(rseapiResponseMock.getContent()).thenReturn(jsonObject);
//        expectedString = "Error action, HTTP Status Code 200 : OK\n" +
//        				 "status: status\n" +
//        				 "message: message";
//        returnString = RseapiZosBatchJobImpl.buildErrorString("action", rseapiResponseMock);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        Mockito.when(rseapiResponseMock.getContent()).thenReturn("message");
//        expectedString = "Error action, HTTP Status Code 200 : OK response body:\n" +
//        				 "message";
//        returnString = RseapiZosBatchJobImpl.buildErrorString("action", rseapiResponseMock);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        Mockito.when(rseapiResponseMock.getContent()).thenReturn(0);
//        expectedString = "Error action, HTTP Status Code 200 : OK";
//        returnString = RseapiZosBatchJobImpl.buildErrorString("action", rseapiResponseMock);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        Mockito.when(rseapiResponseMock.getContent()).thenThrow(new RseapiException());
//        expectedString = "Error action, HTTP Status Code 200 : OK";
//        returnString = RseapiZosBatchJobImpl.buildErrorString("action", rseapiResponseMock);
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
//        Mockito.when(pathMock.toString()).thenReturn(FIXED_PATH_NAME);
//        Mockito.when(pathMock.resolve(Mockito.anyString())).thenReturn(pathMock);        
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
//        responseBody.addProperty("jobId", FIXED_JOBID);
//        responseBody.addProperty("owner", FIXED_OWNER);
//        responseBody.addProperty("type", FIXED_TYPE);
//        responseBody.addProperty("returnCode", FIXED_RETCODE_0000);
//        responseBody.addProperty("status", FIXED_STATUS_OUTPUT);
//        responseBody.addProperty("category", 0);
//        responseBody.addProperty("rc", 0);
//        responseBody.addProperty("reason", 0);
//        responseBody.addProperty("id", 1);
//        responseBody.addProperty("ddName", "ddname");
//        responseBody.addProperty("step name", "stepname");
//        responseBody.addProperty("proc step", "procstep");
//        responseBody.addProperty("status", "status");
//        responseBody.addProperty("message", "message");
//        
//        JsonArray fileArray = new JsonArray();
//        fileArray.add(responseBody.deepCopy());
//    	responseBody.add("items", fileArray);
//        return responseBody;
//    }
}
