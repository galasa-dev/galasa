/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.zosmf.manager.internal;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.IZosDataset.DSType;
import dev.galasa.zosfile.IZosDataset.DatasetDataType;
import dev.galasa.zosfile.IZosDataset.DatasetOrganization;
import dev.galasa.zosfile.IZosDataset.RecordFormat;
import dev.galasa.zosfile.IZosDataset.SpaceUnit;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LogFactory.class})
public class TestZosmfZosDatasetImpl {
//    
//    private ZosmfZosDatasetImpl zosDataset;
//    
//    private ZosmfZosDatasetImpl zosDatasetSpy;
//
//    @Mock
//    private IZosImage zosImageMock;
//
//    @Mock
//    private ZosManagerImpl zosManagerMock;
//    
//    @Mock
//    private ZosmfZosFileManagerImpl zosFileManagerMock;
//    
//    @Mock
//    private ZosmfZosFileHandlerImpl zosFileHandlerMock;
//    
//    @Mock
//    private ZosmfManagerImpl zosmfManagerMock;
//    
//    @Mock
//    private IZosmfRestApiProcessor zosmfApiProcessorMock;
//    
//    @Mock
//    private IZosmfResponse zosmfResponseMock;
//    
//    @Mock
//    private ZosmfZosDatasetAttributesListdsi zosDatasetAttributesListdsiMock;
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
//    private static final String ERROR = "error";
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
//        PowerMockito.doReturn(zosmfApiProcessorMock).when(zosmfManagerMock).newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//        Mockito.when(zosFileHandlerMock.getZosmfManager()).thenReturn(zosmfManagerMock);
//        Mockito.when(zosFileHandlerMock.getZosManager()).thenReturn(zosManagerMock);
//
//    	Path pathMock = Mockito.mock(Path.class);
//    	Mockito.doReturn(pathMock).when(pathMock).resolve(Mockito.anyString());
//    	Mockito.doReturn("PATH_NAME").when(pathMock).toString();
//    	Mockito.doReturn(pathMock).when(zosFileManagerMock).getDatasetCurrentTestMethodArchiveFolder();
//    	Mockito.when(zosFileManagerMock.getDatasetCurrentTestMethodArchiveFolder()).thenReturn(pathMock);
//
//        Mockito.when(zosFileHandlerMock.getZosFileManager()).thenReturn(zosFileManagerMock);
//        zosDataset = new ZosmfZosDatasetImpl(zosFileHandlerMock, zosImageMock, DATASET_NAME);
//        zosDatasetSpy = Mockito.spy(zosDataset);
//    }
//    
//    @Test
//    public void testConstructor() throws ZosmfManagerException, ZosFileManagerException {
//    	Assert.assertEquals("getZosFileHandler() should return expected value", zosFileHandlerMock, zosDatasetSpy.getZosFileHandler());
//        Mockito.doThrow(new ZosmfManagerException(EXCEPTION)).when(zosmfManagerMock).newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	new ZosmfZosDatasetImpl(zosFileHandlerMock, zosImageMock, DATASET_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testCreate() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
//       
//        zosDatasetSpy.create();        
//        Assert.assertFalse("created() should return false", zosDatasetSpy.created());
//
//        Whitebox.setInternalState(zosDatasetSpy, "alcunit", SpaceUnit.TRACKS);
//        Whitebox.setInternalState(zosDatasetSpy, "dstype", DSType.PDSE);
//        zosDatasetSpy.create();
//        
//        Whitebox.setInternalState(zosDatasetSpy, "dstype", DSType.PDS);
//        
//        // First call returns false, second returns true
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
//    public void testCreateZosmfException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.create();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testCreateBadHttpResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//    		zosDatasetSpy.create();
//    	});
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//
//    @Test
//    public void testCreateZosmfResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        
//        String expectedMessage = "Unable to create data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.create();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testDelete() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
//        PowerMockito.doReturn(true).doReturn(false).when(zosDatasetSpy).exists();
//
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
//        
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
//    public void testDeleteZosmfException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.delete();        
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testDeleteBadHttpResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//        
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.delete();        
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//
//    @Test
//    public void testDeleteZosmfResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        
//        String expectedMessage = "Unable to delete data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.delete();        
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testExists() throws ZosDatasetException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        JsonObject jsonObject = getJsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        
//        Assert.assertTrue("exists() should return true", zosDatasetSpy.exists());
//        
//        jsonObject.add("items", getJsonArray("ANOTHER.DATASET.NAME", null, 1, 0));
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        
//        Assert.assertFalse("exists() should return false", zosDatasetSpy.exists());
//        
//        jsonObject = getJsonObject(2);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        
//        Assert.assertFalse("exists() should return false", zosDatasetSpy.exists());
//    }
//
//    @Test
//    public void testExistsZosmfException() throws ZosDatasetException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.exists();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testExistsBadHttpResponseException() throws ZosDatasetException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//        
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.exists();
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//
//    @Test
//    public void testExistsZosmfResponseException() throws ZosDatasetException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        
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
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is a partitioned data set. Use memberStoreText(String memberName, String content) method instead";
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
//        
//        zosDatasetSpy.storeBinary(CONTENT.getBytes());
//        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).inputStreamToByteArray(Mockito.any());
//        
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is a partitioned data set. Use memberStoreBinary(String memberName, String content) method instead";
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
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is a partitioned data set. Use memberRetrieveAsText(String memberName) method instead";
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
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is a partitioned data set. Use memberRetrieveAsBinary(String memberName) method instead";
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
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        PowerMockito.doNothing().when(zosDatasetSpy).savePDSToResultsArchive(RAS_PATH);
//        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
//        PowerMockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
//        Path pathMock = newMockedPath(false);
//        Mockito.when(zosFileManagerMock.getDatasetCurrentTestMethodArchiveFolder()).thenReturn(pathMock);
//        Mockito.when(zosFileHandlerMock.getArtifactsRoot()).thenReturn(pathMock);
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieveAsText();
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieveAsBinary();
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).memberRetrieveAsText(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).memberRetrieveAsBinary(Mockito.any());
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
//        expectedMessage = "Archiving \"" + DATASET_NAME +"\" to " + PATH_MOCK;
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
//        Path pathMock = newMockedPath(false);
//        Mockito.when(zosFileManagerMock.getDatasetCurrentTestMethodArchiveFolder()).thenReturn(pathMock);
//        Mockito.when(zosFileHandlerMock.getArtifactsRoot()).thenReturn(pathMock);
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).memberRetrieveAsText(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).memberRetrieveAsBinary(Mockito.any());
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
//        ZosFileManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosFileManagerException.class, ()->{
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
//        jsonObject.addProperty("dsorg", "PO");
//        PowerMockito.doReturn(jsonObject).when(zosDatasetSpy).getAttibutes();
//        Assert.assertTrue("isPDS() should return true", zosDatasetSpy.isPDS());
//        
//        jsonObject.addProperty("dsorg", "PS");
//        PowerMockito.doReturn(jsonObject).when(zosDatasetSpy).getAttibutes();
//        Assert.assertFalse("isPDS() should return false", zosDatasetSpy.isPDS());
//    }
//    
//    @Test
//    public void testMemberCreate() throws ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doNothing().when(zosDatasetSpy).storeText(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
//        
//        zosDatasetSpy.memberCreate(MEMBER_NAME);
//        Mockito.verify(zosDatasetSpy, Mockito.times(1)).storeText(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
//        
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberCreate(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testMemberDelete() throws ZosDatasetException, ZosmfException {
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
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);        
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
//    public void testMemberDeleteNoPDS() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberDelete(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testMemberDeleteZosmfException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).memberExists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberDelete(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testMemberDeleteBadHttpResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).memberExists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);  
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//        
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberDelete(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberDeleteZosmfResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).memberExists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        
//        String expectedMessage = "Unable to delete member " + MEMBER_NAME + " from data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberDelete(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberExists() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        JsonObject jsonObject =  getJsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);        
//        Assert.assertTrue("memberExists() should return true", zosDatasetSpy.memberExists(MEMBER_NAME));        
//        
//        jsonObject.add("items", getJsonArray("", "REBMEM", 1, 0));
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);        
//        Assert.assertFalse("memberExists() should return false", zosDatasetSpy.memberExists(MEMBER_NAME));
//        
//        
//        jsonObject = getJsonObject(2);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);        
//        Assert.assertFalse("memberExists() should return false", zosDatasetSpy.memberExists(MEMBER_NAME));
//    }
//    
//    @Test
//    public void testMemberExistsNotPDS() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberExists(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testMemberExistsZosmfException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberExists(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testMemberExistsBadHttpResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        JsonObject jsonObject =  getJsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//    
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberExists(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberExistsZosmfResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//
//        String expectedMessage = "Unable to list members of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
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
//
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
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberStoreBinary(MEMBER_NAME, CONTENT.getBytes());
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberStoreBinaryNotPDS() throws ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//
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
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberRetrieveAsText(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberRetrieveAsTextNotPDS() throws ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//
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
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberRetrieveAsBinaryNotPDS() throws ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }     
//    
//    @Test
//    public void testMemberList() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        JsonObject jsonObject = getJsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        Collection<String> memberList = zosDatasetSpy.memberList();
//        Assert.assertEquals("memberlist() should return a list with 1 member", listOfMembers(1), memberList);
//
//        jsonObject = getJsonObject(2);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        
//        memberList = zosDatasetSpy.memberList();
//        Assert.assertEquals("memberlist() should return a list with 2 members", listOfMembers(2), memberList);
//
//        jsonObject = getJsonObject(2);
//        jsonObject.addProperty("moreRows", true);
//        JsonObject jsonObject1 = getJsonObject(2, 2);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject).thenReturn(jsonObject1);
//        
//        memberList = zosDatasetSpy.memberList();
//        Assert.assertEquals("memberlist() should return a list with 4 members", listOfMembers(4), memberList);
//    }
//    
//    @Test
//    public void testMemberListNoPDSException() throws ZosDatasetException, ZosmfException {
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
//    public void testMemberListZosmfException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberList();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testMemberListBadHttpResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);  
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//        
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.memberList();
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//
//    @Test
//    public void testMemberListZosmfResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//
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
//        Path pathMock = newMockedPath(false);
//        Mockito.when(zosFileManagerMock.getDatasetCurrentTestMethodArchiveFolder()).thenReturn(pathMock);
//        Mockito.when(zosFileHandlerMock.getArtifactsRoot()).thenReturn(pathMock);
//        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).memberRetrieveAsText(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).memberRetrieveAsBinary(Mockito.any());
//        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
//        
//        logMessage = null;
//        String expectedMessage = "Archiving \"" + DATASET_NAME + "\"" + " to " + PATH_MOCK;
//        zosDatasetSpy.memberSaveToResultsArchive(MEMBER_NAME, RAS_PATH);
//        Assert.assertEquals("memberSaveToTestArchive() should log specified message", expectedMessage, logMessage);
//
//        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.BINARY);
//        logMessage = null;
//        zosDatasetSpy.memberSaveToResultsArchive(MEMBER_NAME, RAS_PATH);
//        Assert.assertEquals("memberSaveToTestArchive() should log specified message", expectedMessage, logMessage);
//
//        PowerMockito.doThrow(new ZosManagerException(EXCEPTION)).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
//        logMessage = null;
//        expectedMessage = "Unable to save data set member to archive";
//        zosDatasetSpy.memberSaveToResultsArchive(MEMBER_NAME, RAS_PATH);
//        Assert.assertEquals("memberSaveToTestArchive() should log specified message", expectedMessage, logMessage);
//
//        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
//        expectedMessage = "Data set \"" + DATASET_NAME + "\" is not a partitioned data set";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//    		zosDatasetSpy.memberSaveToResultsArchive(MEMBER_NAME, RAS_PATH);
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSetDataType() {
//        DatasetDataType value = DatasetDataType.TEXT;
//        zosDatasetSpy.setDataType(value);
//        Assert.assertEquals("testDataType() should return the supplied value", value, zosDatasetSpy.getDataType());
//        value = DatasetDataType.BINARY;
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
//        zosDatasetSpy.setManagementClass(value);
//        Assert.assertEquals("getManagementClass() should return the supplied value", value, zosDatasetSpy.getManagementClass());
//    }
//    
//    @Test
//    public void testStorageClass() {
//        Assert.assertNull("getStorageClass() should return null", zosDatasetSpy.getStorageClass());
//        String value = "STORAGECLASS";
//        zosDatasetSpy.setStorageClass(value);
//        Assert.assertEquals("getStorageClass() should return the supplied value", value, zosDatasetSpy.getStorageClass());
//    }
//    
//    @Test
//    public void testDataClass() {
//        Assert.assertNull("getDataClass() should return null", zosDatasetSpy.getDataClass());
//        String value = "DATACLASS";
//        zosDatasetSpy.setDataClass(value);
//        Assert.assertEquals("getDataClass() should return the supplied value", value, zosDatasetSpy.getDataClass());
//    }
//    
//    @Test
//    public void testDatasetType() {
//        Assert.assertNull("getDatasetType() should return null", zosDatasetSpy.getDatasetType());
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
//    public void testGetAttibutesAsString() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        JsonObject jsonObject = getJsonObject();
//        jsonObject.addProperty("dsorg", "PO");
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        StringBuffer attributes = new StringBuffer();
//        attributes.append("Data Set Name=");
//        attributes.append(DATASET_NAME);
//        attributes.append(",Volume serial=,");
//        attributes.append("Organization=,");
//        attributes.append("Record format=,");
//        attributes.append("Record length=,");
//        attributes.append("Block size=,");
//        attributes.append("Data set type=,");
//        attributes.append("Allocated extents=,");
//        attributes.append("% Utilized=,");
//        attributes.append("PDS=false,");
//        attributes.append("Creation date=,");
//        attributes.append("Referenced date=,");
//        attributes.append("Expiration date=");
//        Assert.assertEquals("toString() should return the valid String", attributes.toString(), zosDatasetSpy.getAttibutesAsString());
//
//        JsonArray jsonArray = new JsonArray();
//        JsonObject item = new JsonObject();
//        item.addProperty("dsname", DATASET_NAME);
//        item.addProperty("dsorg", "PO");
//        jsonArray.add(item);
//        jsonObject.add("items", jsonArray);
//        Collection<String> memberList = new ArrayList<>();
//        PowerMockito.doReturn(memberList).when(zosDatasetSpy).memberList();
//        attributes = new StringBuffer();
//        attributes.append("Data Set Name=");
//        attributes.append(DATASET_NAME);
//        attributes.append(",Volume serial=,");
//        attributes.append("Organization=PO,");
//        attributes.append("Record format=,");
//        attributes.append("Record length=,");
//        attributes.append("Block size=,");
//        attributes.append("Data set type=,");
//        attributes.append("Allocated extents=,");
//        attributes.append("% Utilized=,");
//        attributes.append("PDS=true,");
//        attributes.append("Number of members=0,");
//        attributes.append("Creation date=,");
//        attributes.append("Referenced date=,");
//        attributes.append("Expiration date=");
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        
//        Assert.assertEquals("toString() should return the valid String", attributes.toString(), zosDatasetSpy.getAttibutesAsString());        
//    }
//    
//    @Test
//    public void testGetAttibutesAsStringNotExist() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutesAsString();        
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testGetAttibutesAsStringNoRows() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("returnedRows", 0);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//
//        String expectedMessage = "Unable to retrieve attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutesAsString();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//        
//    }
//    
//    @Test
//    public void testGetAttibutesAsStringZosmfException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutesAsString();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testGetAttibutesAsStringBadHttpResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//    
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutesAsString();
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//
//    @Test
//    public void testGetAttibutesAsStringZosmfResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//
//        String expectedMessage = "Unable list to attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutesAsString();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttibutesNotExist() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.getAttibutes();        
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAttibutes() throws ZosDatasetException {
//        Whitebox.setInternalState(zosDatasetSpy, "zosmfZosDatasetAttributesListdsi", zosDatasetAttributesListdsiMock);
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
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//    		zosDatasetSpy.retrieveAttibutes();
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAttibutesException() throws ZosDatasetException {
//        String expectedMessage = "Unable to create LISTDSI EXEC command";
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
//        jsonObject.addProperty("volser", "VOLSER");
//        jsonObject.addProperty("unit", "UNIT");
//        jsonObject.addProperty("dsorg", "PO");
//        jsonObject.addProperty("alcunit", "CYLINDER");
//        jsonObject.addProperty("primary", "1");
//        jsonObject.addProperty("secondary", 2);
//        jsonObject.addProperty("dirblk", 3);
//        jsonObject.addProperty("blksize", 4);
//        jsonObject.addProperty("recfm", "FB");
//        jsonObject.addProperty("lrecl", 5);
//        jsonObject.addProperty("dsntype", "PDSE");
//        jsonObject.addProperty("used", 6);
//        jsonObject.addProperty("extx", 7);
//        jsonObject.addProperty("cdate", "CDATE");
//        jsonObject.addProperty("rdate", "RDATE");
//        jsonObject.addProperty("edate", "EDATE");
//        jsonObject.addProperty("dataclass", "DATACLASS");
//        jsonObject.addProperty("storeclass", "STORECLASS");
//        jsonObject.addProperty("mgntclass", "MGNTCLASS");
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
//        jsonObject.addProperty("dsntype", "DATA_LIBRARY");
//        zosDatasetSpy.setAttributes(jsonObject);
//        Assert.assertEquals("setAttributes() should set supplied value", DSType.LIBRARY, zosDatasetSpy.getDatasetType());
//    }
//    @Test
//    public void testInputStreamToByteArray() throws ZosDatasetException, IOException {
//        ByteArrayInputStream contentIs = new ByteArrayInputStream(CONTENT.getBytes());
//        Assert.assertArrayEquals("inputStreamToByteArray() should return the supplied value", CONTENT.getBytes(), zosDatasetSpy.inputStreamToByteArray(contentIs));
//        
//        ByteArrayInputStream contentIsSpy = Mockito.spy(contentIs);
//        PowerMockito.doThrow(new IOException(EXCEPTION)).when(contentIsSpy).read(Mockito.any());
//
//        String expectedMessage = "Failed to collect binary";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.inputStreamToByteArray(contentIsSpy);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieve() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosmfResponseMock.getContent()).thenReturn(CONTENT);
//        
//        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieve(null));
//
//        zosDatasetSpy.setDataType(DatasetDataType.BINARY);
//        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieve(null));
//    }
//    
//    @Test
//    public void testInternalRetrieveZosmfException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.retrieve(null);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveBadHttpResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.retrieve(null);
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//
//    @Test
//    public void testInternalRetrieveBadHttpResponseException1() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//
//        String expectedMessage = "Unable to retrieve content of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.retrieve(null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testInternalRetrieveZosmfResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getContent()).thenThrow(new ZosmfException(EXCEPTION));
//        
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        String expectedMessage = "Unable to retrieve content of data set \"" + DATASET_NAME + "\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.retrieve(null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreText() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
//        
//        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
//        Mockito.verify(zosmfResponseMock, Mockito.times(1)).getStatusCode();
//        
//        Mockito.clearInvocations(zosmfResponseMock);        
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);        
//        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
//        Mockito.verify(zosmfResponseMock, Mockito.times(2)).getStatusCode();        
//        Mockito.clearInvocations(zosDatasetSpy);
//    }
//    
//    @Test
//    public void testInternalStoreTextNotExist() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreTextZosmfException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreTextBadHttpResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//
//    @Test
//    public void testInternalStoreTextZosmfResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//
//        String expectedMessage = "Unable to write to data set \"" + DATASET_NAME + "(" + MEMBER_NAME + ")\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreBinary() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
//        
//        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
//        Mockito.verify(zosmfResponseMock, Mockito.times(1)).getStatusCode();
//        
//        Mockito.clearInvocations(zosmfResponseMock);        
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);        
//        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
//        Mockito.verify(zosmfResponseMock, Mockito.times(2)).getStatusCode();        
//        Mockito.clearInvocations(zosDatasetSpy);
//    }
//    
//    @Test
//    public void testInternalStoreBinaryNotExist() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
//
//        String expectedMessage = "Data set \"" + DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreBinaryZosmfException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalStoreBinaryBadHttpResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//
//    @Test
//    public void testInternalStoreBinaryZosmfResponseException() throws ZosDatasetException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        PowerMockito.doReturn(ERROR).when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
//
//        String expectedMessage = "Unable to write to data set \"" + DATASET_NAME + "(" + MEMBER_NAME + ")\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testAddPropertyWhenSet() throws ZosDatasetException {
//        JsonObject jsonObject = getJsonObject();
//        JsonObject returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(jsonObject, "property", null);
//        Assert.assertEquals("testAddPropertyWhenSet() should return the original JsonObject", jsonObject, returnedJsonObject);
//        
//        jsonObject = getJsonObject();
//        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(jsonObject, "property", "value");
//        Assert.assertEquals("testAddPropertyWhenSet() should return the correct String property value", "value", returnedJsonObject.get("property").getAsString());
//
//        jsonObject = getJsonObject();
//        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(jsonObject, "property", -1);
//        Assert.assertEquals("testAddPropertyWhenSet() should return the original JsonObject", jsonObject, returnedJsonObject);
//        
//        jsonObject = getJsonObject();
//        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(jsonObject, "property", 99);
//        Assert.assertEquals("testAddPropertyWhenSet() should return the correct int property value", 99, returnedJsonObject.get("property").getAsInt());
//
//        String expectedMessage = "Invlaid type of \"" + DummyClass.class.getName() + "\" for property \"property\" on image " + IMAGE;
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetSpy.addPropertyWhenSet(getJsonObject(), "property", new DummyClass());
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetMembers() {
//        Whitebox.setInternalState(zosDatasetSpy, "datasetMembers", new ArrayList<>());
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("returnedRows", 0);
//        Assert.assertFalse("getMembers() should return false", zosDatasetSpy.getMembers(jsonObject));
//        Assert.assertEquals("datasetMembers should return a list with 0 members", listOfMembers(0), Whitebox.getInternalState(zosDatasetSpy,"datasetMembers"));
//        
//        Whitebox.setInternalState(zosDatasetSpy, "datasetMembers", new ArrayList<>());
//        jsonObject = getJsonObject(1);
//        Assert.assertFalse("getMembers() should return false", zosDatasetSpy.getMembers(jsonObject));
//        Assert.assertEquals("datasetMembers should return a list with 1 member", listOfMembers(1), Whitebox.getInternalState(zosDatasetSpy,"datasetMembers"));
//        
//        Whitebox.setInternalState(zosDatasetSpy, "datasetMembers", new ArrayList<>());
//        jsonObject = getJsonObject(2);
//        jsonObject.addProperty("moreRows", true);
//        Assert.assertTrue("getMembers() should return true", zosDatasetSpy.getMembers(jsonObject));
//        Assert.assertEquals("datasetMembers should return a list with 2 members", listOfMembers(2), Whitebox.getInternalState(zosDatasetSpy,"datasetMembers"));
//    }
//    
//    @Test
//    public void testBuildErrorString() {
//        String expectedString = "Error action";
//        String returnString = zosDatasetSpy.buildErrorString("action", new JsonObject());
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        JsonObject jsonObject = getJsonObject();
//        jsonObject.addProperty("category", 0);
//        jsonObject.addProperty("rc", 0);
//        jsonObject.addProperty("reason", 0);
//        jsonObject.addProperty("message", "message");
//        jsonObject.addProperty("id", 1);
//        zosDatasetSpy.buildErrorString("action", jsonObject);
//        
//        jsonObject.addProperty("details", "details");
//        expectedString = "Error action data set \"" + DATASET_NAME + "\", category:0, rc:0, reason:0, message:message\n" + 
//                "details:details";
//        returnString = zosDatasetSpy.buildErrorString("action", jsonObject);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        jsonObject.addProperty("stack", "stack");
//        zosDatasetSpy.buildErrorString("action", jsonObject);
//        
//        jsonObject.addProperty("details", "details");
//        expectedString = "Error action data set \"" + DATASET_NAME + "\", category:0, rc:0, reason:0, message:message\n" + 
//                "details:details\n" + 
//                "stack:\n" + 
//                "stack";
//        returnString = zosDatasetSpy.buildErrorString("action", jsonObject);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        jsonObject.addProperty("details", 1);
//        zosDatasetSpy.buildErrorString("action", jsonObject);
//        
//        jsonObject.remove("details");
//        JsonArray jsonArray = new JsonArray();
//        JsonPrimitive item = new JsonPrimitive("details line 1");
//        jsonArray.add(item);
//        item = new JsonPrimitive("details line 2");
//        jsonArray.add(item);
//        jsonObject.add("details", jsonArray);
//        expectedString = "Error action data set \"" + DATASET_NAME + "\", category:0, rc:0, reason:0, message:message\n" + 
//                "details:\n" +
//                "details line 1\n" +
//                "details line 2\n" + 
//                "stack:\n" + 
//                "stack";
//        returnString = zosDatasetSpy.buildErrorString("action", jsonObject);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
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
//    public void testGetZosmfApiProcessor() {
//        Assert.assertEquals("getZosmfApiProcessor() should return the mocked IZosmfRestApiProcessor", zosmfApiProcessorMock, zosDatasetSpy.getZosmfApiProcessor());
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
//    private JsonObject getJsonObject() {
//        return getJsonObject(1, 0);
//    }
//
//    private JsonObject getJsonObject(int count) {
//        return getJsonObject(count, 0);
//    }
//
//    private JsonObject getJsonObject(int count, int startingAt) {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("returnedRows", count);
//        jsonObject.add("items", getJsonArray(DATASET_NAME, MEMBER_NAME, count, startingAt));
//        return jsonObject;
//    }
//
//    private JsonElement getJsonArray(String datasetName, String memberName, int count, int startingAt) {
//        JsonArray jsonArray = new JsonArray();
//        for (int i = 0; i < count; i++) {
//            String sfx = "";
//            if (startingAt == 0) {
//                if (i > 0) {
//                    sfx = Integer.toString(i);
//                }
//            } else {
//                sfx = Integer.toString(i + startingAt);
//            }
//            JsonObject items = new JsonObject();
//            items.addProperty("dsname", datasetName + sfx);
//            items.addProperty("member", memberName + sfx);
//            jsonArray.add(items);
//        }
//        return jsonArray;
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
