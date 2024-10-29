/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({FrameworkUtil.class})
public class TestRseapiZosDatasetAttributesListdsi {
//    
//    private RseapiZosDatasetAttributesListdsi zosmfZosDatasetAttributesListdsi;
//    
//    private RseapiZosDatasetAttributesListdsi zosDatasetAttributesListdsiSpy;
//
//    @Mock
//    private IZosImage zosImageMock;
//
//    @Mock
//    private IZosManagerSpi zosManagerMock;
//    
//    @Mock
//    private RseapiZosFileManagerImpl zosFileManagerMock;
//    
//    @Mock
//    private RseapiZosUnixCommand zosUnixCommandMock; 
//    
//    @Mock
//    private IRseapiRestApiProcessor rseapiApiProcessorMock;
//    
//    @Mock
//    private IRseapiResponse rseapiResponseMock;
//    
//    @Mock
//    private RseapiZosFileHandlerImpl zosFileHandlerMock;
//    
//    @Mock
//    private RseapiZosDatasetImpl execDatasetMock;
//
//    private static final String DATASET_NAME = "DATA.SET.NAME";    
//    private static final String RUN_HLQ = "RUNHLQ";    
//    private static final String RUNID = "RUNID";
//    
//    private static final String PROP_INVOCATION = "invocation";
//    private static final String PROP_PATH = "path";
//    private static final String PROP_OUTPUT = "output";
//    private static final String PROP_STDOUT = "stdout";
//    private static final String PROP_STDERR = "stderr";
//    private static final String PROP_EXIT_CODE = "exit code";
//    
//    private static final String EXCEPTION = "exception";
//    
//    @Before
//    public void setup() throws Exception {
//        Mockito.when(zosManagerMock.getRunDatasetHLQ(Mockito.any())).thenReturn(RUN_HLQ);
//        Mockito.when(zosFileManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        Mockito.when(zosFileManagerMock.getRunId()).thenReturn(RUNID);
//        Mockito.when(zosFileHandlerMock.getZosFileManager()).thenReturn(zosFileManagerMock);
//        
//        zosmfZosDatasetAttributesListdsi = new RseapiZosDatasetAttributesListdsi(zosFileHandlerMock, rseapiApiProcessorMock, zosImageMock);
//        zosDatasetAttributesListdsiSpy = Mockito.spy(zosmfZosDatasetAttributesListdsi);
//    }
//    
//    @Test
//    public void testInitialise() throws ZosDatasetException {
//        PowerMockito.doNothing().when(zosDatasetAttributesListdsiSpy).createExecDataset();
//        zosDatasetAttributesListdsiSpy.initialise();
//        Assert.assertTrue("initialise() should set initialised to true", Whitebox.getInternalState(zosDatasetAttributesListdsiSpy, "initialised"));
//    }
//    
//    @Test
//    public void testInitialiseException1() throws ZosDatasetException {
//    	Mockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetAttributesListdsiSpy).createExecDataset();
//        String expectedMessage = "Unable to create LISTDSI EXEC command";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetAttributesListdsiSpy.initialise();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGet() throws ZosDatasetException {
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "initialised", true);
//        JsonObject jsonObject = buildListdsiJsonObject(true); 
//        PowerMockito.doReturn(jsonObject).when(zosDatasetAttributesListdsiSpy).execListdsi(Mockito.anyString());
//        JsonObject jsonResult = new JsonObject();
//        Assert.assertEquals("get() should return the expected object", jsonResult, zosDatasetAttributesListdsiSpy.get(DATASET_NAME));
//        
//        jsonObject = buildListdsiJsonObject(false);        
//        PowerMockito.doReturn(jsonObject).when(zosDatasetAttributesListdsiSpy).execListdsi(Mockito.anyString());
//        jsonResult = buildListdsiJsonResponse();
//        Assert.assertEquals("get() should return the expected object", jsonResult, zosDatasetAttributesListdsiSpy.get(DATASET_NAME));
//        
//        jsonObject.addProperty("sysused","");
//        jsonObject.addProperty("sysusedpages","6");
//        PowerMockito.doReturn(jsonObject).when(zosDatasetAttributesListdsiSpy).execListdsi(Mockito.anyString());
//        jsonResult.addProperty("used",6);
//        Assert.assertEquals("get() should return the expected object", jsonResult, zosDatasetAttributesListdsiSpy.get(DATASET_NAME));
//
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "initialised", false);
//        PowerMockito.doNothing().when(zosDatasetAttributesListdsiSpy).initialise();
//        zosDatasetAttributesListdsiSpy.get(DATASET_NAME);
//    }
//    
//    @Test
//    public void testGetException() throws ZosDatasetException {
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "initialised", true);
//        JsonObject jsonObject = new JsonObject();
//        PowerMockito.doReturn(jsonObject ).when(zosDatasetAttributesListdsiSpy).execListdsi(Mockito.anyString());
//        String expectedMessage = "Invalid JSON object returend from LISTDSI:\n" + jsonObject;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetAttributesListdsiSpy.get(DATASET_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testFormatDate() {
//        Assert.assertEquals("get() should return the expected object", "***None***", zosDatasetAttributesListdsiSpy.formatDate(null));
//        Assert.assertEquals("get() should return the expected object", "***None***", zosDatasetAttributesListdsiSpy.formatDate(""));
//        Assert.assertEquals("get() should return the expected object", "***None***", zosDatasetAttributesListdsiSpy.formatDate("INVALID"));
//        Assert.assertEquals("get() should return the expected object", "2020/01/01", zosDatasetAttributesListdsiSpy.formatDate("2020/001"));
//    }
//
//    @Test
//    public void testExecListdsi() throws ZosDatasetException, RseapiException {
//    	Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//    	Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(getCommandJsonObject(0));
//        JsonObject jsonObject = buildListdsiJsonObject(false);
//        Mockito.when(execDatasetMock.memberRetrieveAsText(Mockito.any())).thenReturn(jsonObject.toString());
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "execDataset", execDatasetMock);
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "execDatasetName", DATASET_NAME);
//        Assert.assertEquals("get() should return the expected object", jsonObject, zosDatasetAttributesListdsiSpy.execListdsi(DATASET_NAME));
//        
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(getCommandJsonObject(99));
//        String expectedMessage = "Unable to get data set attibutes. Response body:\n" + getCommandJsonObject(99);
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetAttributesListdsiSpy.execListdsi(DATASET_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testExecListdsiException1() throws ZosDatasetException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetAttributesListdsiSpy.execListdsi(DATASET_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getCause().getMessage());
//    }
//
//    @Test
//    public void testExecListdsiException2() throws ZosDatasetException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenCallRealMethod();
//        String expectedMessage = "Error zOS UNIX command, HTTP Status Code 404 : NOT_FOUND";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetAttributesListdsiSpy.execListdsi(DATASET_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testGetExitRc() {
//        JsonObject responseBody = getCommandJsonObject(0);
//        Assert.assertEquals("getExitRc() should return the expected object", "RC=0", zosDatasetAttributesListdsiSpy.getOutputProperty(responseBody, PROP_STDOUT));
//
//        responseBody.remove(PROP_OUTPUT);
//        Assert.assertEquals("getExitRc() should return the expected object", "UNKNOWN", zosDatasetAttributesListdsiSpy.getOutputProperty(responseBody, PROP_STDOUT));
//
//        responseBody.addProperty(PROP_OUTPUT, PROP_OUTPUT);
//
//		JsonObject output = new JsonObject();
//		output.addProperty(PROP_STDERR, "");
//		responseBody.add(PROP_OUTPUT, output);
//        Assert.assertEquals("getExitRc() should return the expected object", "UNKNOWN", zosDatasetAttributesListdsiSpy.getOutputProperty(responseBody, PROP_STDOUT));
//    }
//    
//    @Test
//    public void testCreateExecDataset() throws Exception {
//        Mockito.when(zosFileHandlerMock.newDataset(Mockito.any(), Mockito.any())).thenReturn(execDatasetMock);
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "zosFileHandler", zosFileHandlerMock);
//        Mockito.when(execDatasetMock.exists()).thenReturn(true);
//        Mockito.when(execDatasetMock.memberExists(Mockito.any())).thenReturn(true);
//        Mockito.doReturn("DATA").when(zosDatasetAttributesListdsiSpy).getExecResource();
//        zosDatasetAttributesListdsiSpy.createExecDataset();
//        PowerMockito.verifyPrivate(zosDatasetAttributesListdsiSpy, Mockito.times(0)).invoke("getExecResource");
//
//        Mockito.when(execDatasetMock.exists()).thenReturn(false);
//        Mockito.when(execDatasetMock.memberExists(Mockito.any())).thenReturn(false);
//        zosDatasetAttributesListdsiSpy.createExecDataset();
//        PowerMockito.verifyPrivate(zosDatasetAttributesListdsiSpy, Mockito.times(1)).invoke("getExecResource");
//    }
//    
//    @Test
//    public void testGetExecResource() throws ZosDatasetException, IOException {
//        URL execUrl = Paths.get("src","main","resources","resources","LISTDSI.rexx").toUri().toURL();
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(execUrl.openConnection().getInputStream()));
//        StringBuilder execContent = new StringBuilder();
//        while(bufferedReader.ready()) {
//            execContent.append(bufferedReader.readLine());
//            execContent.append("\n");
//        }
//        bufferedReader.close();
//        String execSource = execContent.toString();
//        PowerMockito.mockStatic(FrameworkUtil.class);
//        Bundle bundleMock = Mockito.mock(Bundle.class);
//        Mockito.when(bundleMock.getResource(Mockito.any())).thenReturn(execUrl);
//        Mockito.when(FrameworkUtil.getBundle(Mockito.any())).thenReturn(bundleMock);
//        Assert.assertEquals("getExecResource() should return the expected value", execSource, zosDatasetAttributesListdsiSpy.getExecResource());
//        
//        Mockito.when(bundleMock.getResource(Mockito.any())).thenReturn(new URL("file://DUMMY"));
//        String expectedMessage = "Unable to get LISTDSI EXEC resource from Manager Bundle";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetAttributesListdsiSpy.getExecResource();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    private JsonObject getCommandJsonObject(int rc) {
//		JsonObject output = new JsonObject();
//		output.addProperty(PROP_STDOUT, "RC=" + rc);
//		output.addProperty(PROP_STDERR, "EXEC '" + DATASET_NAME + "(LISTDSI)' '" + DATASET_NAME + "'");
//		JsonObject responseBody = new JsonObject();
//		responseBody.add(PROP_OUTPUT, output);
//		responseBody.addProperty(PROP_PATH, "path");
//		responseBody.addProperty(PROP_EXIT_CODE, 0);
//		responseBody.addProperty(PROP_INVOCATION, "command");
//		return responseBody;
//	}
//
//	private JsonObject buildListdsiJsonObject(boolean empty) {
//        JsonObject jsonObject = new JsonObject();
//        if (empty) {
//            jsonObject.addProperty("listdsirc","");
//            jsonObject.addProperty("sysreason","");                             
//            jsonObject.addProperty("sysdsname","");
//            jsonObject.addProperty("sysvolume","");
//            jsonObject.addProperty("sysunit","");
//            jsonObject.addProperty("sysdsorg","");
//            jsonObject.addProperty("sysrecfm","");
//            jsonObject.addProperty("syslrecl","");
//            jsonObject.addProperty("sysblksize","");
//            jsonObject.addProperty("syskeylen","");
//            jsonObject.addProperty("sysalloc","");
//            jsonObject.addProperty("sysused","");
//            jsonObject.addProperty("sysusedpages","");
//            jsonObject.addProperty("sysprimary","");
//            jsonObject.addProperty("sysseconds","");
//            jsonObject.addProperty("sysunits","");
//            jsonObject.addProperty("sysextents","");
//            jsonObject.addProperty("syscreate","");
//            jsonObject.addProperty("sysrefdate","");
//            jsonObject.addProperty("sysexdate","");
//            jsonObject.addProperty("syspassword","");
//            jsonObject.addProperty("sysracfa","");
//            jsonObject.addProperty("sysupdated","");
//            jsonObject.addProperty("systrkscyl","");
//            jsonObject.addProperty("sysblkstrk","");
//            jsonObject.addProperty("sysadirblk","");
//            jsonObject.addProperty("sysudirblk","");
//            jsonObject.addProperty("sysmembers","");
//            jsonObject.addProperty("sysdssms","");
//            jsonObject.addProperty("sysdataclass","");
//            jsonObject.addProperty("sysstorclass","");
//            jsonObject.addProperty("sysmgmtclass","");
//            jsonObject.addProperty("sysmsglvl1","");
//            jsonObject.addProperty("sysmsglvl2","");
//        } else {
//            jsonObject.addProperty("listdsirc","0");
//            jsonObject.addProperty("sysreason","0000");                             
//            jsonObject.addProperty("sysdsname",DATASET_NAME);
//            jsonObject.addProperty("sysvolume","VOLUME");
//            jsonObject.addProperty("sysunit","UNIT");
//            jsonObject.addProperty("sysdsorg","DSORG");
//            jsonObject.addProperty("sysrecfm","RECFM");
//            jsonObject.addProperty("syslrecl","1");
//            jsonObject.addProperty("sysblksize","2");
//            jsonObject.addProperty("syskeylen","3");
//            jsonObject.addProperty("sysalloc","4");
//            jsonObject.addProperty("sysused","5");
//            jsonObject.addProperty("sysusedpages","6");
//            jsonObject.addProperty("sysprimary","7");
//            jsonObject.addProperty("sysseconds","8");
//            jsonObject.addProperty("sysunits","UNITS");
//            jsonObject.addProperty("sysextents","9");
//            jsonObject.addProperty("syscreate","2020/001");
//            jsonObject.addProperty("sysrefdate","2020/002");
//            jsonObject.addProperty("sysexdate","2020/003");
//            jsonObject.addProperty("syspassword","PASSWD");
//            jsonObject.addProperty("sysracfa","RACFA");
//            jsonObject.addProperty("sysupdated","UPDTED");
//            jsonObject.addProperty("systrkscyl","10");
//            jsonObject.addProperty("sysblkstrk","11");
//            jsonObject.addProperty("sysadirblk","12");
//            jsonObject.addProperty("sysudirblk","13");
//            jsonObject.addProperty("sysmembers","14");
//            jsonObject.addProperty("sysdssms","TYPE");
//            jsonObject.addProperty("sysdataclass","DATACLASS");
//            jsonObject.addProperty("sysstorclass","STOCLASS");
//            jsonObject.addProperty("sysmgmtclass","MGTCLASS");
//            jsonObject.addProperty("sysmsglvl1","SYSMSGLVL1");
//            jsonObject.addProperty("sysmsglvl2","SYSMSGLVL2");
//        }
//        return jsonObject;
//    }
//
//    private JsonObject buildListdsiJsonResponse() {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("listdsirc",0);
//        jsonObject.addProperty("sysreason",0);
//        jsonObject.addProperty("sysmsglvl1","SYSMSGLVL1");
//        jsonObject.addProperty("sysmsglvl2","SYSMSGLVL2");
//        jsonObject.addProperty("name",DATASET_NAME);
//        jsonObject.addProperty("volumeSerial","VOLUME");
//        jsonObject.addProperty("unit","UNIT");
//        jsonObject.addProperty("dataSetOrganization","DSORG");
//        jsonObject.addProperty("allocationUnit","UNITS");
//        jsonObject.addProperty("creationDate","2020/01/01");
//        jsonObject.addProperty("primary",7);
//        jsonObject.addProperty("secondary",8);
//        jsonObject.addProperty("directoryBlocks",12);
//        jsonObject.addProperty("recordFormat","RECFM");
//        jsonObject.addProperty("blockSize",2);
//        jsonObject.addProperty("recordLength",1);
//        jsonObject.addProperty("storClass","STOCLASS");
//        jsonObject.addProperty("mgmtClass","MGTCLASS");
//        jsonObject.addProperty("dataClass","DATACLASS");
//        jsonObject.addProperty("dsnType","TYPE");
//        jsonObject.addProperty("used",5);
//        jsonObject.addProperty("extents",9);
//        jsonObject.addProperty("expiryDate","2020/01/03");
//        jsonObject.addProperty("referenceDate","2020/01/02");
//        jsonObject.addProperty("syskeylen",3);
//        jsonObject.addProperty("sysalloc",4);
//        jsonObject.addProperty("syspassword","PASSWD");
//        jsonObject.addProperty("sysracfa","RACFA");
//        jsonObject.addProperty("sysupdated","UPDTED");
//        jsonObject.addProperty("systrkscyl",10);
//        jsonObject.addProperty("sysblkstrk",11);
//        jsonObject.addProperty("sysudirblk",13);
//        jsonObject.addProperty("sysmembers",14);
//        return jsonObject;
//    }
}
