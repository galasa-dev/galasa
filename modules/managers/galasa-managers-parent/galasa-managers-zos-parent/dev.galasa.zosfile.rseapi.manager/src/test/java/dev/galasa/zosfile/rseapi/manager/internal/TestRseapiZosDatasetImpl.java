/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.NotImplementedException;
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
import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.IZosDataset.DSType;
import dev.galasa.zosfile.IZosDataset.DatasetDataType;
import dev.galasa.zosfile.IZosDataset.DatasetOrganization;
import dev.galasa.zosfile.IZosDataset.RecordFormat;
import dev.galasa.zosfile.IZosDataset.SpaceUnit;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.RseapiManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LogFactory.class})
public class TestRseapiZosDatasetImpl {
//    
//    private RseapiZosDatasetImpl zosDataset;
//    
//    private RseapiZosDatasetImpl zosDatasetSpy;
//
//    @Mock
//    private IZosImage zosImageMock;
//
//    @Mock
//    private ZosManagerImpl zosManagerMock;
//
//    @Mock
//    private RseapiZosFileManagerImpl zosFileManagerMock;
//    
//    @Mock
//    private RseapiManagerImpl rseapiManagerMock;
//    
//    @Mock
//    private RseapiZosFileHandlerImpl zosFileHandlerMock;
//    
//    @Mock
//    private IZosUNIXFile zosUNIXFileMock;
//    
//    @Mock
//    private IRseapiRestApiProcessor rseapiApiProcessorMock;
//    
//    @Mock
//    private IRseapiResponse rseapiResponseMock;
//    
//    @Mock
//    private RseapiZosDatasetAttributesListdsi zosDatasetAttributesListdsiMock;
//    
//    @Mock
//    private Log logMock;
//    
//    private static String logMessage;
//    
//    private static final String DATASET_NAME = "DATA.SET.NAME";
//    
//    private static final String MEMBER_NAME = "MEMBER";
//    
//    private static final String IMAGE = "IMAGE";
//    
//    private static final String CONTENT = "content";
//    
//    private static final String EXCEPTION = "exception";
//
//	private static final String PATH_MOCK = "PATH_MOCK";
//
//	private static final String RAS_PATH = "RAS_PATH";
//    
//    @Before
//    public void setup() throws Exception {
//        PowerMockito.mockStatic(LogFactory.class);
//        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
//        Answer<String> answer = new Answer<String>() {
//            @Override
//            public String answer(InvocationOnMock invocation) throws Throwable {
//                logMessage = invocation.getArgument(0);
//                System.err.println("Captured Log Message:\n" + logMessage);
//                if (invocation.getArguments().length > 1 && invocation.getArgument(1) instanceof Throwable) {
//                    ((Throwable) invocation.getArgument(1)).printStackTrace();
//                }
//                return null;
//            }
//        };
//        Mockito.doAnswer(answer).when(logMock).info(Mockito.any());
//        Mockito.doAnswer(answer).when(logMock).warn(Mockito.any());
//        Mockito.doAnswer(answer).when(logMock).error(Mockito.any(), Mockito.any());
//        
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
//        
//        Mockito.when(zosManagerMock.getZosFilePropertyFileRestrictToImage(Mockito.any())).thenReturn(true);
//        Mockito.when(zosFileManagerMock.getZosManager()).thenReturn(zosManagerMock);
//
//        PowerMockito.doReturn(rseapiApiProcessorMock).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//        Mockito.when(zosFileHandlerMock.getRseapiManager()).thenReturn(rseapiManagerMock);
//        Mockito.when(zosFileHandlerMock.getZosFileManager()).thenReturn(zosFileManagerMock);
//        Mockito.when(zosFileHandlerMock.getZosManager()).thenReturn(zosManagerMock);
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenCallRealMethod();
//
//    	Path pathMock = Mockito.mock(Path.class);
//    	Mockito.doReturn(pathMock).when(pathMock).resolve(Mockito.anyString());
//    	Mockito.doReturn("PATH_NAME").when(pathMock).toString();
//    	Mockito.doReturn(pathMock).when(zosFileManagerMock).getDatasetCurrentTestMethodArchiveFolder();
//    	Mockito.when(zosFileManagerMock.getDatasetCurrentTestMethodArchiveFolder()).thenReturn(pathMock);
//        
//        zosDataset = new RseapiZosDatasetImpl(zosFileHandlerMock, zosImageMock, DATASET_NAME);
//        zosDatasetSpy = Mockito.spy(zosDataset);
//    }
//    
//    @Test
//    public void testConstructor() throws RseapiManagerException, ZosFileManagerException {
//    	Assert.assertEquals("getZosFileHandler() should return the expected object", zosFileHandlerMock, zosDatasetSpy.getZosFileHandler());
//    	ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//    		Mockito.doThrow(new RseapiManagerException(EXCEPTION)).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//    		new RseapiZosDatasetImpl(zosFileHandlerMock, zosImageMock, DATASET_NAME);
//    	});
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testCreate() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject responseBody = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
//       
//        zosDatasetSpy.create();        
//        Assert.assertFalse("created() should return false", zosDatasetSpy.created());
//
//        Whitebox.setInternalState(zosDatasetSpy, "alcunit", SpaceUnit.TRACKS);
//        Whitebox.setInternalState(zosDatasetSpy, "dstype", DSType.PDSE);
//        zosDatasetSpy.create();
//        
//        Whitebox.setInternalState(zosDatasetSpy, "dstype", DSType.PDS);
//        zosDatasetSpy.create();
//        
//        Whitebox.setInternalState(zosDatasetSpy, "dstype", DSType.LIBRARY);
//        Whitebox.setInternalState(zosDatasetSpy, "dirblk", 0);
//        zosDatasetSpy.create();
//
//        PowerMockito.doReturn(false).doReturn(true).when(zosDatasetSpy).exists();
//        zosDatasetSpy.create();
//        Assert.assertTrue("created() should return true", zosDatasetSpy.created());
//    }
//    
//    @Test
//    public void testCreateExists() throws ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" already exists on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.create();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testCreateRseapiException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.create();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testCreateBadHttpResponseException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject responseBody = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        String expectedMessage = "Error Create data set, HTTP Status Code 404 : NOT_FOUND";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.create();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testDelete() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
//        PowerMockito.doReturn(true).doReturn(false).when(zosDatasetSpy).exists();
//        PowerMockito.doNothing().when(zosDatasetSpy).delete(Mockito.any());
//        Assert.assertTrue("delete() should return true", zosDatasetSpy.delete());
//        
//        PowerMockito.doReturn(true).doReturn(true).when(zosDatasetSpy).exists();        
//        Assert.assertFalse("delete() should return false", zosDatasetSpy.delete());
//    }
//    
//    @Test
//    public void testDeleteNotExists() throws ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        String expectedMessage = "\"" + DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.delete();        
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testExists() throws ZosDatasetException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        JsonObject responseBody = new JsonObject();        
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);        
//        Assert.assertFalse("exists() should return false", zosDatasetSpy.exists());
//
//        JsonArray itemsArray = new JsonArray();
//        responseBody.add("items", itemsArray);        
//        Assert.assertFalse("exists() should return false", zosDatasetSpy.exists());
//        
//        JsonObject item = new JsonObject();
//        item.addProperty("name", "ANOTHER.DATASET.NAME");
//        itemsArray.add(item);
//        Assert.assertFalse("exists() should return false", zosDatasetSpy.exists());
//        
//        item.addProperty("name", DATASET_NAME);
//        Assert.assertTrue("exists() should return true", zosDatasetSpy.exists());
//    }
//
//    @Test
//    public void testExistsRseapiException() throws ZosDatasetException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.exists();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testExistsBadHttpResponseException() throws ZosDatasetException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        String expectedMessage = "Error List data set, HTTP Status Code 404 : NOT_FOUND";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.exists();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testExistsRseapiResponseException() throws ZosDatasetException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//        String expectedMessage = "Unable to list data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.exists();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testStoreText() throws ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        PowerMockito.doNothing().when(zosDatasetSpy).storeText(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
//        
//        zosDatasetSpy.storeText(CONTENT);
//        
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is a partitioned data set. Use memberStore(String memberName, String content) method instead";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeText(CONTENT);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testStoreBinary() throws ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        PowerMockito.doNothing().when(zosDatasetSpy).storeBinary(Mockito.any(), Mockito.any(), Mockito.anyBoolean());        
//        zosDatasetSpy.storeBinary(CONTENT.getBytes());
//        
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is a partitioned data set. Use memberStore(String memberName, String content) method instead";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeBinary(CONTENT.getBytes());
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testRetrieveAsText() throws ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieve(Mockito.any());
//        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieveAsText());
//        
//        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieve(Mockito.any());
//        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieveAsText());
//        
//        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).inputStreamToByteArray(Mockito.any());
//        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieveAsText());
//        
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is a partitioned data set. Use retrieve(String memberName) method instead";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.retrieveAsText();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testRetrieveAsBinary() throws ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieve(Mockito.any());
//        Assert.assertEquals("retrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.retrieveAsBinary()));
//
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieve(Mockito.any());
//        Assert.assertEquals("retrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.retrieveAsBinary()));
//
//        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).inputStreamToByteArray(Mockito.any());        
//        Assert.assertEquals("retrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.retrieveAsBinary()));
//        
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS(); 
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is a partitioned data set. Use retrieve(String memberName) method instead";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.retrieveAsBinary();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSaveToResultsArchive() throws IOException, ZosManagerException {
//		zosDatasetSpy.setShouldArchive(true);
//
//    	PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        PowerMockito.doNothing().when(zosDatasetSpy).savePDSToResultsArchive(RAS_PATH);
//        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
//        PowerMockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
//        Whitebox.setInternalState(zosFileManagerMock, "datasetArtifactRoot", newMockedPath(false));
//        Whitebox.setInternalState(zosFileManagerMock, "currentTestMethodArchiveFolderName", "testMethod");
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieveAsText();
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieveAsBinary();
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).memberRetrieveAsText(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).memberRetrieveAsBinary(Mockito.any());
//        Path pathMock = newMockedPath(false);
//        zosFileManagerMock.setVsamDatasetArtifactRoot(pathMock);
//        Mockito.when(zosFileHandlerMock.getArtifactsRoot()).thenReturn(pathMock);
//        Mockito.when(zosFileManagerMock.getDatasetCurrentTestMethodArchiveFolder()).thenReturn(pathMock);
//        
//        logMessage = null;
//        String expectedMessage = "Archiving \"" + DATASET_NAME + "\"" + " to " + PATH_MOCK;
//        zosDatasetSpy.saveToResultsArchive(RAS_PATH);
//		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//
//        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.BINARY);
//        logMessage = null;
//        zosDatasetSpy.saveToResultsArchive(RAS_PATH);
//		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//
//        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        logMessage = null;
//        expectedMessage = null;
//        zosDatasetSpy.saveToResultsArchive(RAS_PATH);
//		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        logMessage = null;
//        expectedMessage = null;
//        zosDatasetSpy.saveToResultsArchive(RAS_PATH);
//        Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//        
//        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetSpy).exists();
//        logMessage = null;
//        expectedMessage = "Unable to save data set to archive";
//        zosDatasetSpy.saveToResultsArchive(RAS_PATH);
//        Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//        
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        PowerMockito.doThrow(new ZosManagerException(EXCEPTION)).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
//        zosDatasetSpy.saveToResultsArchive(RAS_PATH);
//        Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//    }
//    
//    @Test
//    public void testSavePDSToResultsArchive() throws IOException, ZosManagerException {
//        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
//        PowerMockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
//        Whitebox.setInternalState(zosFileManagerMock, "datasetArtifactRoot", newMockedPath(false));
//        Whitebox.setInternalState(zosFileManagerMock, "currentTestMethodArchiveFolderName", "testMethod");
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).memberRetrieveAsText(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).memberRetrieveAsBinary(Mockito.any());
//        Path pathMock = newMockedPath(false);
//        zosFileManagerMock.setVsamDatasetArtifactRoot(pathMock);
//        Mockito.when(zosFileHandlerMock.getArtifactsRoot()).thenReturn(pathMock);
//        Mockito.when(zosFileManagerMock.getDatasetCurrentTestMethodArchiveFolder()).thenReturn(pathMock);
//        
//        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
//        Collection<String> datasetMembers = new ArrayList<>();
//        PowerMockito.doReturn(datasetMembers).when(zosDatasetSpy).memberList();
//        logMessage = null;
//        String expectedMessage = null;
//        zosDatasetSpy.savePDSToResultsArchive(RAS_PATH);
//		Assert.assertEquals("savePDSToResultsArchive() should log specified message", expectedMessage, logMessage);
//		
//        datasetMembers.add(MEMBER_NAME);
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieve(Mockito.any());
//        logMessage = null;
//        expectedMessage = "\"" + DATASET_NAME + "(" + MEMBER_NAME + ")\" archived to " + PATH_MOCK;
//        zosDatasetSpy.savePDSToResultsArchive(RAS_PATH);
//		Assert.assertEquals("savePDSToResultsArchive() should log specified message", expectedMessage, logMessage);
//		
//        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.BINARY);
//        logMessage = null;
//        zosDatasetSpy.savePDSToResultsArchive(RAS_PATH);
//		Assert.assertEquals("savePDSToResultsArchive() should log specified message", expectedMessage, logMessage);
//		
//		PowerMockito.doThrow(new ZosManagerException(EXCEPTION)).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.savePDSToResultsArchive(RAS_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    private Path newMockedPath(boolean fileExists) throws IOException {
//        Path pathMock = Mockito.mock(Path.class);
//        Mockito.when(pathMock.toString()).thenReturn(PATH_MOCK);
//        FileSystem fileSystemMock = Mockito.mock(FileSystem.class);
//        FileSystemProvider fileSystemProviderMock = Mockito.mock(FileSystemProvider.class);
//        OutputStream outputStreamMock = Mockito.mock(OutputStream.class);
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
//    @Test
//    public void testIsPDS() throws ZosDatasetException {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("dataSetOrganization", "PO");
//        PowerMockito.doReturn(jsonObject).when(zosDatasetSpy).getAttibutes();
//        Assert.assertTrue("isPDS() should return true", zosDatasetSpy.isPDS());
//        
//        jsonObject.addProperty("dataSetOrganization", "PS");
//        PowerMockito.doReturn(jsonObject).when(zosDatasetSpy).getAttibutes();
//        Assert.assertFalse("isPDS() should return false", zosDatasetSpy.isPDS());
//    }
//    
//    @Test
//    public void testMemberCreate() throws ZosFileManagerException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doNothing().when(zosDatasetSpy).storeText(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
//        RseapiZosUnixCommand zosUnixCommandMock = Mockito.mock(RseapiZosUnixCommand.class);
//        Mockito.when(zosUnixCommandMock.execute(Mockito.any(), Mockito.any())).thenReturn(null);
//        Mockito.when(zosFileManagerMock.getRunUNIXPathPrefix(Mockito.any())).thenReturn("/prefix");
//        Mockito.when(zosFileManagerMock.getRunId()).thenReturn("runid");
//        Mockito.when(zosFileManagerMock.newZosFileHandler()).thenReturn(zosFileHandlerMock);
//        PowerMockito.doReturn(zosUNIXFileMock).when(zosFileHandlerMock).newUNIXFile(Mockito.any(), Mockito.any());
//        PowerMockito.doReturn(false).doReturn(true).when(zosUNIXFileMock).exists();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).memberExists(Mockito.any());
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        JsonObject responseBody = new JsonObject();
//        responseBody.addProperty("exit code", 0);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        zosDatasetSpy.memberCreate(MEMBER_NAME);
//        String expectedMessage = "Member " + MEMBER_NAME + " created in Data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//
//        PowerMockito.doReturn(false).when(zosDatasetSpy).memberExists(Mockito.any());        
//        zosDatasetSpy.memberCreate(MEMBER_NAME);
//        expectedMessage = "Member " + MEMBER_NAME + " not created in Data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//    	
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberCreate(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", "Error zOS UNIX command, HTTP Status Code 404 : null", expectedException.getCause().getMessage());
//
//        PowerMockito.doThrow(new ZosUNIXFileException(EXCEPTION)).doReturn(true).when(zosUNIXFileMock).exists();
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberCreate(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testMemberCreateException1() throws ZosFileManagerException {
//    	PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberCreate(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testMemberDelete() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        zosDatasetSpy.memberDelete(MEMBER_NAME);
//        Mockito.verify(zosDatasetSpy, Mockito.times(1)).exists();
//        
//        Mockito.clearInvocations(zosDatasetSpy);
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        PowerMockito.doReturn(false).when(zosDatasetSpy).memberExists(Mockito.any());
//        zosDatasetSpy.memberDelete(MEMBER_NAME);
//        Mockito.verify(zosDatasetSpy, Mockito.times(1)).memberExists(Mockito.any());
//        
//        Mockito.clearInvocations(zosDatasetSpy);
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).memberExists(Mockito.any());
//        PowerMockito.doNothing().when(zosDatasetSpy).delete(Mockito.any());
//        zosDatasetSpy.memberDelete(MEMBER_NAME);
//        Mockito.verify(zosDatasetSpy, Mockito.times(2)).memberExists(Mockito.any());
//        
//        Mockito.clearInvocations(zosDatasetSpy);
//        PowerMockito.doReturn(true).doReturn(false).when(zosDatasetSpy).memberExists(Mockito.any());        
//        zosDatasetSpy.memberDelete(MEMBER_NAME);
//        Mockito.verify(zosDatasetSpy, Mockito.times(2)).memberExists(Mockito.any());
//    }
//    
//    @Test
//    public void testMemberDeleteNoPDS() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberDelete(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberExists() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        
//        ArrayList<String> datasetMembers = new ArrayList<>();
//        Whitebox.setInternalState(zosDatasetSpy, "datasetMembers", datasetMembers);
//        PowerMockito.doReturn(datasetMembers).when(zosDatasetSpy).memberList();
//        Assert.assertFalse("memberExists() should return false", zosDatasetSpy.memberExists(MEMBER_NAME));        
//
//        datasetMembers.add(MEMBER_NAME);
//        Assert.assertTrue("memberExists() should return true", zosDatasetSpy.memberExists(MEMBER_NAME));
//    }
//    
//    @Test
//    public void testMemberExistsNotPDS() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberExists(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberStoreText() throws ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doNothing().when(zosDatasetSpy).storeText(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
//        
//        zosDatasetSpy.memberStoreText(MEMBER_NAME, CONTENT);
//        
//        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetSpy).storeText(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberStoreText(MEMBER_NAME, CONTENT);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberStoreTextNotPDS() throws ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberStoreText(MEMBER_NAME, CONTENT);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberStoreBinary() throws ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doNothing().when(zosDatasetSpy).storeBinary(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
//        
//        zosDatasetSpy.memberStoreBinary(MEMBER_NAME, CONTENT.getBytes());
//        
//        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetSpy).storeBinary(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberStoreBinary(MEMBER_NAME, CONTENT.getBytes());
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberStoreBinaryNotPDS() throws ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberStoreBinary(MEMBER_NAME, CONTENT.getBytes());
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberRetrieveAsText() throws ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieve(Mockito.any());        
//        Assert.assertEquals("memberRetrieveText() should return the supplied value", CONTENT, zosDatasetSpy.memberRetrieveAsText(MEMBER_NAME));
//
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieve(Mockito.any());
//        Assert.assertEquals("memberRetrieveText() should return the supplied value", CONTENT, zosDatasetSpy.memberRetrieveAsText(MEMBER_NAME));
//
//        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).inputStreamToByteArray(Mockito.any());
//        Assert.assertEquals("memberRetrieveText() should return the supplied value", CONTENT, zosDatasetSpy.memberRetrieveAsText(MEMBER_NAME));
//        
//        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetSpy).retrieve(Mockito.any());
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberRetrieveAsText(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberRetrieveAsTextNotPDS() throws ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberRetrieveAsText(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberRetrieveAsBinary() throws ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieve(Mockito.any());
//        Assert.assertEquals("memberRetrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME)));
//        
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).inputStreamToByteArray(Mockito.any());       
//        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
//        Assert.assertEquals("memberRetrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME)));
//        
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieve(Mockito.any());
//        Assert.assertEquals("memberRetrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME)));
//        
//        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetSpy).retrieve(Mockito.any());
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberRetrieveAsBinaryNotPDS() throws ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }     
//    
//    @Test
//    public void testMemberList() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);        
//        Collection<String> memberList = zosDatasetSpy.memberList();
//        Assert.assertEquals("memberlist() should return a list with 0 member", listOfMembers(0), memberList);        
//        
//        JsonObject responseBody = new JsonObject();
//        JsonArray items = new JsonArray();
//        items.add("MEMBER");
//        responseBody.add("items", items);        
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);        
//        memberList = zosDatasetSpy.memberList();
//        Assert.assertEquals("memberlist() should return a list with 1 member", listOfMembers(1), memberList);
//
//        items.add("MEMBER1");
//        memberList = zosDatasetSpy.memberList();
//        Assert.assertEquals("memberlist() should return a list with 2 members", listOfMembers(2), memberList);
//    }
//    
//    @Test
//    public void testMemberListNoPDSException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberList();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testMemberListRseapiException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberList();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testMemberListBadHttpResponseException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject responseBody = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("BAD_REQUEST");
//        String expectedMessage = "Error List data set members, HTTP Status Code 400 : BAD_REQUEST";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberList();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberListRseapiResponseException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//        String expectedMessage = "Unable to retrieve member list of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberList();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberSaveToResultsArchive() throws IOException, ZosManagerException {
//		zosDatasetSpy.setShouldArchive(true);
//		
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
//        Whitebox.setInternalState(zosFileManagerMock, "datasetArtifactRoot", newMockedPath(false));
//        Whitebox.setInternalState(zosFileManagerMock, "currentTestMethodArchiveFolderName", "testMethod");
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).memberRetrieveAsText(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).memberRetrieveAsBinary(Mockito.any());
//        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
//        Path pathMock = newMockedPath(false);
//        zosFileManagerMock.setVsamDatasetArtifactRoot(pathMock);
//        Mockito.when(zosFileHandlerMock.getArtifactsRoot()).thenReturn(pathMock);
//        Mockito.when(zosFileManagerMock.getDatasetCurrentTestMethodArchiveFolder()).thenReturn(pathMock);
//        
//        logMessage = null;
//        String expectedMessage = "Archiving \"" + DATASET_NAME + "\"" + " to " + PATH_MOCK;
//        zosDatasetSpy.memberSaveToResultsArchive(MEMBER_NAME,RAS_PATH);
//        Assert.assertEquals("memberSaveToTestArchive() should log specified message", expectedMessage, logMessage);
//
//        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.BINARY);
//        logMessage = null;
//        zosDatasetSpy.memberSaveToResultsArchive(MEMBER_NAME,RAS_PATH);
//        Assert.assertEquals("memberSaveToTestArchive() should log specified message", expectedMessage, logMessage);
//
//        PowerMockito.doThrow(new ZosManagerException(EXCEPTION)).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
//        logMessage = null;
//        expectedMessage = "Unable to save data set member to archive";
//        zosDatasetSpy.memberSaveToResultsArchive(MEMBER_NAME,RAS_PATH);
//        Assert.assertEquals("memberSaveToTestArchive() should log specified message", expectedMessage, logMessage);
//
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberSaveToResultsArchive(MEMBER_NAME,RAS_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSetDataType() {
//        DatasetDataType value = DatasetDataType.TEXT;
//        zosDatasetSpy.setDataType(value);
//        Assert.assertEquals("testDataType() should return the supplied value", value, zosDatasetSpy.getDataType());
//        value = DatasetDataType.BINARY;
//        
//        zosDatasetSpy.setDataType(value);
//        Assert.assertEquals("testDataType() should return the supplied value", value, zosDatasetSpy.getDataType());
//    }
//    
//    @Test
//    public void testUnit() {
//        Assert.assertNull("getUnit() should return null", zosDatasetSpy.getUnit());
//        String value = "UNIT";
//        zosDatasetSpy.setUnit(value);
//        Assert.assertEquals("getUnit() should return the supplied value", value, zosDatasetSpy.getUnit());
//    }
//    
//    @Test
//    public void testVolumes() {
//        Assert.assertNull("getVolumes() should return null", zosDatasetSpy.getVolumes());
//        String value = "VOLUMES";
//        zosDatasetSpy.setVolumes(value);
//        Assert.assertEquals("getVolumes() should return the supplied value", value, zosDatasetSpy.getVolumes());
//    }
//    
//    @Test
//    public void testDatasetOrganization() {
//        Assert.assertNull("getDatasetOrganization() should return null", zosDatasetSpy.getDatasetOrganization());
//        DatasetOrganization value = DatasetOrganization.SEQUENTIAL;
//        zosDatasetSpy.setDatasetOrganization(value);
//        Assert.assertEquals("getDatasetOrganization() should return the supplied value", value, zosDatasetSpy.getDatasetOrganization());
//    }
//    
//    @Test
//    public void testSpace() {
//        Assert.assertNull("getSpace() should return null", zosDatasetSpy.getSpaceUnit());
//        Assert.assertEquals("getPrimaryExtents() should return -1", -1, zosDatasetSpy.getPrimaryExtents());
//        Assert.assertEquals("getSecondaryExtents() should return -1", -1, zosDatasetSpy.getSecondaryExtents());
//        SpaceUnit spaceUnit = SpaceUnit.CYLINDERS;
//        int primaryExtents = 99;
//        int secondaryExtents = 99;
//        zosDatasetSpy.setSpace(spaceUnit, primaryExtents, secondaryExtents);
//        Assert.assertEquals("getSpaceUnit() should return the supplied value", spaceUnit, zosDatasetSpy.getSpaceUnit());
//        Assert.assertEquals("getPrimaryExtents() should return the supplied value", primaryExtents, zosDatasetSpy.getPrimaryExtents());
//        Assert.assertEquals("getSecondaryExtents() should return the supplied value", secondaryExtents, zosDatasetSpy.getSecondaryExtents());
//    }
//    
//    @Test
//    public void testDirectoryBlocks() {
//        Assert.assertEquals("getDirectoryBlocks() should return -1", -1, zosDatasetSpy.getDirectoryBlocks());
//        int value = 99;
//        zosDatasetSpy.setDirectoryBlocks(value);
//        Assert.assertEquals("getDirectoryBlocks() should return the supplied value", value, zosDatasetSpy.getDirectoryBlocks());
//    }
//    
//    @Test
//    public void testRecordFormat() {
//        Assert.assertNull("getRecordFormat() should return null", zosDatasetSpy.getRecordFormat());
//        RecordFormat value = RecordFormat.FIXED;
//        zosDatasetSpy.setRecordFormat(value);
//        Assert.assertEquals("getRecordFormat() should return the supplied value", value, zosDatasetSpy.getRecordFormat());
//    }
//    
//    @Test
//    public void testBlockSize() {
//        Assert.assertEquals("getBlockSize() should return -1", -1, zosDatasetSpy.getBlockSize());
//        int value = 99;
//        zosDatasetSpy.setBlockSize(value);
//        Assert.assertEquals("getBlockSize() should return the supplied value", value, zosDatasetSpy.getBlockSize());
//    }
//    
//    @Test
//    public void testRecordlength() {
//        Assert.assertEquals("getRecordlength() should return -1", -1, zosDatasetSpy.getRecordlength());
//        int value = 99;
//        zosDatasetSpy.setRecordlength(value);
//        Assert.assertEquals("getRecordlength() should return the supplied value", value, zosDatasetSpy.getRecordlength());
//    }
//    
//    @Test
//    public void testManagementClass() {
//        Assert.assertNull("getManagementClass() should return null", zosDatasetSpy.getManagementClass());
//        String value = "MANAGEMENTCLASS";
//        
//        zosDatasetSpy.setManagementClass(value);        
//        Assert.assertEquals("getManagementClass() should return the supplied value", value, zosDatasetSpy.getManagementClass());
//    }
//    
//    @Test
//    public void testStorageClass() {
//        Assert.assertNull("getStorageClass() should return null", zosDatasetSpy.getStorageClass());
//        String value = "STORAGECLASS";
//        
//        zosDatasetSpy.setStorageClass(value);
//        Assert.assertEquals("getStorageClass() should return the supplied value", value, zosDatasetSpy.getStorageClass());
//    }
//    
//    @Test
//    public void testDataClass() {
//        Assert.assertNull("getDataClass() should return null", zosDatasetSpy.getDataClass());
//        String value = "DATACLASS";
//        
//        zosDatasetSpy.setDataClass(value);
//        Assert.assertEquals("getDataClass() should return the supplied value", value, zosDatasetSpy.getDataClass());
//    }
//    
//    @Test
//    public void testDatasetType() {
//        Assert.assertNull("getDatasetType() should return null", zosDatasetSpy.getDatasetType());
//        
//        zosDatasetSpy.setDatasetType(DSType.BASIC);
//        Assert.assertEquals("getDatasetType() should return DSType.BASIC", DSType.BASIC, zosDatasetSpy.getDatasetType());
//    }
//    
//    @Test
//    public void testExtents() {
//        int value = 99;
//        Whitebox.setInternalState(zosDatasetSpy, "extents", value);
//        Assert.assertEquals("getExtents() should return expected value", value, zosDatasetSpy.getExtents());
//    }
//    
//    @Test
//    public void testUsed() {
//        int value = 99;
//        Whitebox.setInternalState(zosDatasetSpy, "used", value);
//        Assert.assertEquals("getExtents() should return expected value", value, zosDatasetSpy.getUsed());
//    }
//    
//    @Test
//    public void testCreateDate() {
//        String value = "01/01/2000";
//        Whitebox.setInternalState(zosDatasetSpy, "createDate", value);
//        Assert.assertEquals("getCreateDate() should return expected value", value, zosDatasetSpy.getCreateDate());
//    }
//    
//    @Test
//    public void testReferencedDate() {
//        String value = "01/01/2000";
//        Whitebox.setInternalState(zosDatasetSpy, "referencedDate", value);
//        Assert.assertEquals("getReferencedDate() should return expected value", value, zosDatasetSpy.getReferencedDate());
//    }
//    
//    @Test
//    public void testExpirationDate() {
//        String value = "01/01/2000";
//        Whitebox.setInternalState(zosDatasetSpy, "expirationDate", value);
//        Assert.assertEquals("getExpirationDate() should return expected value", value, zosDatasetSpy.getExpirationDate());
//    }
//    
//    @Test
//    public void testGetName() {
//        Assert.assertEquals("getName() should return DATASET_NAME", DATASET_NAME, zosDatasetSpy.getName());
//        Assert.assertEquals("toString() should return DATASET_NAME", DATASET_NAME, zosDatasetSpy.toString());
//    }
//    
//    @Test
//    public void testGetAttibutesAsString() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        JsonObject jsonObject = new JsonObject();
//        PowerMockito.doReturn(jsonObject).when(zosDatasetSpy).getAttibutes();
//        StringBuilder attributes = new StringBuilder();
//        attributes.append("Data Set Name=,");
//        attributes.append("Volume serial=,");
//        attributes.append("Organization=,");
//        attributes.append("Record format=,");
//        attributes.append("Record length=,");
//        attributes.append("Block size=,");
//        attributes.append("Data set type=,");
//        attributes.append("Allocated extents=,");
//        attributes.append("PDS=false,");
//        attributes.append("Creation date=,");
//        attributes.append("Referenced date=");
//        Assert.assertEquals("toString() should return the valid String", attributes.toString(), zosDatasetSpy.getAttibutesAsString());
//
//        jsonObject.addProperty("dataSetOrganization", "PO");
//        attributes = new StringBuilder();
//        attributes.append("Data Set Name=,");
//        attributes.append("Volume serial=,");
//        attributes.append("Organization=PO,");
//        attributes.append("Record format=,");
//        attributes.append("Record length=,");
//        attributes.append("Block size=,");
//        attributes.append("Data set type=,");
//        attributes.append("Allocated extents=,");
//        attributes.append("PDS=true,");
//        attributes.append("Creation date=,");
//        attributes.append("Referenced date=");
//        Assert.assertEquals("toString() should return the valid String", attributes.toString(), zosDatasetSpy.getAttibutesAsString());
//    }
//    
//    @Test
//    public void testGetAttibutesAsStringNotExist() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutesAsString();        
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttibutes() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject responseBody = new JsonObject();
//        JsonArray items = new JsonArray();
//        JsonObject attributes = new JsonObject();
//        attributes.addProperty("name", DATASET_NAME);
//        items.add(attributes);
//        responseBody.add("items", items);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//    	Assert.assertEquals("getAttibutes() should return the expected value", attributes, zosDatasetSpy.getAttibutes());
//    }
//    
//    @Test
//    public void testGetAttibutesNotExistException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutes();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttibutesRseapiException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutes();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testGetAttibutesBadHttpResponseException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        String expectedMessage = "Error list data set, HTTP Status Code 404 : NOT_FOUND";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutes();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttibutesGetContentException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException());
//        String expectedMessage = "Unable list to attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutes();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttibutesException1() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject responseBody = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        String expectedMessage = "Unable to retrieve attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutes();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttibutesException2() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject responseBody = new JsonObject();
//        JsonArray items = new JsonArray();
//        responseBody.add("items", items);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        String expectedMessage = "Unable to retrieve attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutes();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttibutesException3() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject responseBody = new JsonObject();
//        JsonArray items = new JsonArray();
//        JsonObject attributes = new JsonObject();
//        attributes.addProperty("no name", DATASET_NAME);
//        items.add(attributes);
//        responseBody.add("items", items);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        String expectedMessage = "Unable to retrieve attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutes();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttibutesException4() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject responseBody = new JsonObject();
//        JsonArray items = new JsonArray();
//        JsonObject attributes = new JsonObject();
//        attributes.addProperty("name", "ANOTHER.DATASET.NAME");
//        items.add(attributes);
//        responseBody.add("items", items);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        String expectedMessage = "Unable to retrieve attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutes();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAttibutes() throws ZosDatasetException {
//        Whitebox.setInternalState(zosDatasetSpy, "rseapiZosDatasetAttributesListdsi", zosDatasetAttributesListdsiMock);
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("listdsirc", 0);
//        Mockito.when(zosDatasetAttributesListdsiMock.get(Mockito.anyString())).thenReturn(jsonObject);
//        PowerMockito.doNothing().when(zosDatasetSpy).setAttributes(Mockito.any());
//        zosDatasetSpy.retrieveAttibutes();
//        Mockito.verify(zosDatasetSpy, Mockito.times(1)).setAttributes(Mockito.any());
//        PowerMockito.doReturn("MESSAGE").when(zosDatasetSpy).emptyStringWhenNull(Mockito.any(),Mockito.any());
//        
//        jsonObject.addProperty("listdsirc", 4);
//        zosDatasetSpy.retrieveAttibutes();
//        Mockito.verify(zosDatasetSpy, Mockito.times(2)).setAttributes(Mockito.any());
//        
//        jsonObject.addProperty("sysreason", 12);
//        zosDatasetSpy.retrieveAttibutes();
//        String expectedMessage = "Unable to get full attributes for data set \"" + DATASET_NAME + "\". LISTDSI RC=4\n" + 
//                "SYSREASON=12\n" + 
//                "SYSMSGLVL1: MESSAGE\n" + 
//                "SYSMSGLVL2: MESSAGE";
//        Assert.assertEquals("retrieveAttibutes() should log specified message", expectedMessage , logMessage);
//        
//        jsonObject.addProperty("listdsirc", 12);
//        expectedMessage = "Unable to get attributes for data set \"" + DATASET_NAME + "\". LISTDSI RC=12\n" + 
//                "SYSREASON=12\n" + 
//                "SYSMSGLVL1: MESSAGE\n" + 
//                "SYSMSGLVL2: MESSAGE";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.retrieveAttibutes();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSetAttributes() {
//        JsonObject jsonObject = new JsonObject();
//        zosDatasetSpy.setAttributes(jsonObject);
//        
//        jsonObject.addProperty("volumeSerial", "VOLSER");
//        jsonObject.addProperty("unit", "UNIT");
//        jsonObject.addProperty("dataSetOrganization", "PO");
//        jsonObject.addProperty("allocationUnit", "CYLINDER");
//        jsonObject.addProperty("primary", "1");
//        jsonObject.addProperty("secondary", 2);
//        jsonObject.addProperty("directoryBlocks", 3);
//        jsonObject.addProperty("blockSize", 4);
//        jsonObject.addProperty("recordFormat", "FB");
//        jsonObject.addProperty("recordLength", 5);
//        jsonObject.addProperty("dataClass", "DATACLASS");
//        jsonObject.addProperty("storageClass", "STORECLASS");
//        jsonObject.addProperty("managementClass", "MGNTCLASS");        
//        jsonObject.addProperty("dsnType", "PDSE");
//        jsonObject.addProperty("used", 6);
//        jsonObject.addProperty("extents", 7);
//        jsonObject.addProperty("creationDate", "CDATE");
//        jsonObject.addProperty("referenceDate", "RDATE");
//        jsonObject.addProperty("expiryDate", "EDATE");
//        zosDatasetSpy.setAttributes(jsonObject);
//
//        Assert.assertEquals("setAttributes() should set supplied value", "VOLSER", zosDatasetSpy.getVolumes());
//        Assert.assertEquals("setAttributes() should set supplied value", "UNIT", zosDatasetSpy.getUnit());
//        Assert.assertEquals("setAttributes() should set supplied value", DatasetOrganization.PARTITIONED, zosDatasetSpy.getDatasetOrganization());
//        Assert.assertEquals("setAttributes() should set supplied value", SpaceUnit.CYLINDERS, zosDatasetSpy.getSpaceUnit());
//        Assert.assertEquals("setAttributes() should set supplied value", 1, zosDatasetSpy.getPrimaryExtents());
//        Assert.assertEquals("setAttributes() should set supplied value", 2, zosDatasetSpy.getSecondaryExtents());
//        Assert.assertEquals("setAttributes() should set supplied value", 3, zosDatasetSpy.getDirectoryBlocks());
//        Assert.assertEquals("setAttributes() should set supplied value", 4, zosDatasetSpy.getBlockSize());
//        Assert.assertEquals("setAttributes() should set supplied value", RecordFormat.FIXED_BLOCKED, zosDatasetSpy.getRecordFormat());
//        Assert.assertEquals("setAttributes() should set supplied value", 5, zosDatasetSpy.getRecordlength());
//        Assert.assertEquals("setAttributes() should set supplied value", DSType.PDSE, zosDatasetSpy.getDatasetType());
//        Assert.assertEquals("setAttributes() should set supplied value", 6, zosDatasetSpy.getUsed());
//        Assert.assertEquals("setAttributes() should set supplied value", 7, zosDatasetSpy.getExtents());
//        Assert.assertEquals("setAttributes() should set supplied value", "CDATE", zosDatasetSpy.getCreateDate());
//        Assert.assertEquals("setAttributes() should set supplied value", "RDATE", zosDatasetSpy.getReferencedDate());
//        Assert.assertEquals("setAttributes() should set supplied value", "EDATE", zosDatasetSpy.getExpirationDate());
//        Assert.assertEquals("setAttributes() should set supplied value", "DATACLASS", zosDatasetSpy.getDataClass());
//        Assert.assertEquals("setAttributes() should set supplied value", "STORECLASS", zosDatasetSpy.getStorageClass());
//        Assert.assertEquals("setAttributes() should set supplied value", "MGNTCLASS", zosDatasetSpy.getManagementClass());
//        
//        jsonObject.addProperty("dsnType", "DATA_LIBRARY");
//        zosDatasetSpy.setAttributes(jsonObject);
//        Assert.assertEquals("setAttributes() should set supplied value", DSType.LIBRARY, zosDatasetSpy.getDatasetType());
//    }
//    
//    @Test
//    public void testInputStreamToByteArray() throws ZosDatasetException, IOException {
//        ByteArrayInputStream contentIs = new ByteArrayInputStream(CONTENT.getBytes());
//        Assert.assertArrayEquals("inputStreamToByteArray() should return the supplied value", CONTENT.getBytes(), zosDatasetSpy.inputStreamToByteArray(contentIs));
//        
//        ByteArrayInputStream contentIsSpy = Mockito.spy(contentIs);
//        PowerMockito.doThrow(new IOException(EXCEPTION)).when(contentIsSpy).read(Mockito.any());
//        String expectedMessage = "Failed to collect binary";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.inputStreamToByteArray(contentIsSpy);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieve() throws ZosDatasetException, RseapiException {
//    	zosDatasetSpy.setDataType(DatasetDataType.TEXT);    
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        JsonObject responseBody = new JsonObject();
//		Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Assert.assertEquals("retrieve() should return the supplied value", "", zosDatasetSpy.retrieve(null));
//
//        responseBody.addProperty("records", CONTENT);
//        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieve(null));
//        zosDatasetSpy.setDataType(DatasetDataType.BINARY);
//        
//        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieve(null));
//    }
//    
//    @Test
//    public void testInternalRetrieveRseapiException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.retrieve(null);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveBadHttpResponseException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        JsonObject responseBody = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        String expectedMessage = "Error retrieve content of data set, HTTP Status Code 404 : NOT_FOUND";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.retrieve(null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testInternalRetrieveRseapiResponseException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//        String expectedMessage = "Unable to retrieve content of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.retrieve(null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//        Mockito.verify(rseapiResponseMock, Mockito.times(1)).getStatusCode();
//    }
//    
//    @Test
//    public void testInternalDelete() throws ZosDatasetException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject responseBody = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
//        zosDatasetSpy.delete(DATASET_NAME);
//        Mockito.verify(rseapiResponseMock, Mockito.times(1)).getStatusCode();
//    }
//    
//    @Test
//    public void testInternalDeleteResponseException() throws ZosDatasetException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.delete(DATASET_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalInvalidHttpResponseDelete() throws ZosDatasetException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject responseBody = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        String expectedMessage = "Error delete DATA.SET.NAME, HTTP Status Code 404 : NOT_FOUND";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.delete(DATASET_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreText() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
//        Mockito.verify(rseapiResponseMock, Mockito.times(1)).getStatusCode();
//        
//        Mockito.clearInvocations(rseapiResponseMock);        
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);        
//        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
//        Mockito.verify(rseapiResponseMock, Mockito.times(2)).getStatusCode();        
//        Mockito.clearInvocations(zosDatasetSpy);
//    }
//    
//    @Test
//    public void testInternalStoreTextNotExist() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreTextRseapiException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreTextBadHttpResponseException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        JsonObject responseBody = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        String expectedMessage = "Error writing to data set, HTTP Status Code 404 : NOT_FOUND";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreBinary() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
//        Mockito.verify(rseapiResponseMock, Mockito.times(1)).getStatusCode();
//        
//        Mockito.clearInvocations(rseapiResponseMock);        
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);        
//        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
//        Mockito.verify(rseapiResponseMock, Mockito.times(2)).getStatusCode();        
//        Mockito.clearInvocations(zosDatasetSpy);
//    }
//    
//    @Test
//    public void testInternalStoreBinaryNotExist() throws ZosDatasetException, RseapiException {  
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreBinaryRseapiException() throws ZosDatasetException, RseapiException { 
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreBinaryBadHttpResponseException() throws ZosDatasetException, RseapiException { 
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        JsonObject responseBody = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        String expectedMessage = "Error write to data set, HTTP Status Code 404 : NOT_FOUND";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testAddPropertyWhenSet() throws ZosDatasetException {
//        JsonObject responseBody = new JsonObject();
//        JsonObject returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(responseBody, "property", null);
//        Assert.assertEquals("testAddPropertyWhenSet() should return the original JsonObject", responseBody, returnedJsonObject);
//        
//        responseBody = new JsonObject();
//        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(responseBody, "property", "value");
//        Assert.assertEquals("testAddPropertyWhenSet() should return the correct String property value", "value", returnedJsonObject.get("property").getAsString());
//
//        responseBody = new JsonObject();
//        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(responseBody, "property", -1);
//        Assert.assertEquals("testAddPropertyWhenSet() should return the original JsonObject", responseBody, returnedJsonObject);
//        
//        responseBody = new JsonObject();
//        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(responseBody, "property", 99);
//        Assert.assertEquals("testAddPropertyWhenSet() should return the correct int property value", 99, returnedJsonObject.get("property").getAsInt());
//
//        String expectedMessage = "Invlaid type of \"" + DummyClass.class.getName() + "\" for property \"property\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.addPropertyWhenSet(new JsonObject(), "property", new DummyClass());
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//        
//    @Test
//    public void testSplitDSN() {
//        zosDatasetSpy.splitDSN(DATASET_NAME);
//        Assert.assertEquals("getName() should return DATASET_NAME", DATASET_NAME, zosDatasetSpy.getName());
//        zosDatasetSpy.splitDSN(DATASET_NAME + "(" + MEMBER_NAME + ")");
//        Assert.assertEquals("getName() should return DATASET_NAME", DATASET_NAME, zosDatasetSpy.getName());
//    }
//    
//    @Test
//    public void testArchiveContent() throws ZosDatasetException {
//    	Mockito.doNothing().when(zosDatasetSpy).saveToResultsArchive(Mockito.any());
//    	Mockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
//    	Mockito.when(zosDatasetSpy.shouldArchive()).thenReturn(false);
//    	zosDatasetSpy.archiveContent();
//    	Mockito.verify(zosDatasetSpy, Mockito.times(0)).saveToResultsArchive(Mockito.any());
//    	
//    	Mockito.when(zosDatasetSpy.shouldArchive()).thenReturn(true);
//    	zosDatasetSpy.archiveContent();
//    	Mockito.verify(zosDatasetSpy, Mockito.times(1)).saveToResultsArchive(Mockito.any());
//    }
//    
//    @Test
//    public void testShouldCleanup() {
//    	zosDatasetSpy.setShouldCleanup(false);
//    	Assert.assertFalse("setShouldCleanup() should return false", zosDatasetSpy.shouldCleanup());
//    	zosDatasetSpy.setShouldCleanup(true);
//    	Assert.assertTrue("setShouldCleanup() should return true", zosDatasetSpy.shouldCleanup());
//    }
//    
//    @Test
//    public void testGetRseapiApiProcessor() {
//        Assert.assertEquals("getRseapiApiProcessor() should return the mocked IRseapiRestApiProcessor", rseapiApiProcessorMock, zosDatasetSpy.getRseapiApiProcessor());
//    }
//
//    private Collection<String> listOfMembers(int count) {
//        Collection<String> memberList = new ArrayList<>();
//        for (int i = 0; i < count; i++) {
//            if (i == 0) {
//                memberList.add(MEMBER_NAME);
//            } else {
//                memberList.add(MEMBER_NAME + i);
//            }
//        }
//        return memberList;
//    }
//    
//    class DummyClass {
//        @Override
//        public String toString() {
//            throw new NotImplementedException("Not Implemented");
//        }
//    }
}
