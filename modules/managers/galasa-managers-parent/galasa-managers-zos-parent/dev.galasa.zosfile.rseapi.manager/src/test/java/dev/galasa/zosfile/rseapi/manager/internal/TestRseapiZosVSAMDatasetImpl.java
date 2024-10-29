/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

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

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.IZosDataset.DatasetDataType;
import dev.galasa.zosfile.IZosVSAMDataset.BWOOption;
import dev.galasa.zosfile.IZosVSAMDataset.DatasetOrganisation;
import dev.galasa.zosfile.IZosVSAMDataset.EraseOption;
import dev.galasa.zosfile.IZosVSAMDataset.FRLogOption;
import dev.galasa.zosfile.IZosVSAMDataset.LogOption;
import dev.galasa.zosfile.IZosVSAMDataset.RecatalogOption;
import dev.galasa.zosfile.IZosVSAMDataset.ReuseOption;
import dev.galasa.zosfile.IZosVSAMDataset.SpanOption;
import dev.galasa.zosfile.IZosVSAMDataset.SpeedRecoveryOption;
import dev.galasa.zosfile.IZosVSAMDataset.VSAMSpaceUnit;
import dev.galasa.zosfile.IZosVSAMDataset.WriteCheckOption;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.RseapiManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LogFactory.class})
public class TestRseapiZosVSAMDatasetImpl {
//    
//    private RseapiZosVSAMDatasetImpl zosVSAMDataset;
//    
//    private RseapiZosVSAMDatasetImpl zosVSAMDatasetSpy;
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
//    private RseapiZosDatasetImpl zosDatasetMock;
//    
//    @Mock
//    private RseapiZosDatasetImpl reproDatasetMock;
//    
//    @Mock
//    private IRseapiRestApiProcessor rseapiApiProcessorMock;
//    
//    @Mock
//    private IRseapiResponse rseapiResponseMock;
//    
//    @Mock
//    private Log logMock;
//    
//    private static String logMessage;
//    
//    private static int idcamsRc;
//
//    private static final String VSAM_DATASET_NAME = "VSAM.DATA.SET.NAME";
//
//    private static final String VSAM_DATASET_DATA_NAME = "VSAM.DATA.SET.NAME.DATA";
//
//    private static final String VSAM_DATASET_INDEX_NAME = "VSAM.DATA.SET.NAME.INDEX";
//
//    private static final String REPRO_DATASET_NAME = "REPRO.DATA.SET.NAME";
//    
//    private static final String IMAGE = "IMAGE";
//    
//    private static final String IDCAMS_COMMAND = "IDCAMS COMMAND NAME------VALUE ";
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
//    	PowerMockito.mockStatic(LogFactory.class);
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
//        Mockito.doAnswer(answer).when(logMock).error(Mockito.any(), Mockito.any());
//        
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
//        
//        Mockito.when(zosManagerMock.getZosFilePropertyFileRestrictToImage(Mockito.any())).thenReturn(true);
//        Mockito.when(zosFileManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        
//        PowerMockito.doReturn(rseapiApiProcessorMock).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//        Mockito.when(zosFileHandlerMock.getRseapiManager()).thenReturn(rseapiManagerMock);
//        Mockito.when(zosFileHandlerMock.newDataset(Mockito.any(), Mockito.any())).thenReturn(zosDatasetMock);
//        Mockito.when(zosFileHandlerMock.getZosFileManager()).thenReturn(zosFileManagerMock);
//        Mockito.when(zosFileHandlerMock.getZosManager()).thenReturn(zosManagerMock);
//
//    	Path pathMock = Mockito.mock(Path.class);
//    	Mockito.doReturn(pathMock).when(pathMock).resolve(Mockito.anyString());
//    	Mockito.doReturn("PATH_NAME").when(pathMock).toString();
//    	Mockito.doReturn(pathMock).when(zosFileManagerMock).getUnixPathArtifactRoot();
//        Mockito.when(zosFileManagerMock.getVsamDatasetCurrentTestMethodArchiveFolder()).thenReturn(pathMock);
//        
//        zosVSAMDataset = new RseapiZosVSAMDatasetImpl(zosFileHandlerMock, zosImageMock, VSAM_DATASET_NAME);
//        zosVSAMDatasetSpy = Mockito.spy(zosVSAMDataset);
//    }
//    
//    @Test
//    public void testConstructor() throws RseapiManagerException, ZosFileManagerException {
//    	Assert.assertEquals("getZosFileHandler() should return the expected object", zosFileHandlerMock, zosVSAMDatasetSpy.getZosFileHandler());
//        Mockito.when(zosFileManagerMock.newZosFileHandler()).thenReturn(zosFileHandlerMock);
//        Mockito.when(zosFileHandlerMock.newDataset(Mockito.any(), Mockito.any())).thenThrow(new ZosDatasetException(EXCEPTION));
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "zosFileHandler", zosFileHandlerMock);
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	new RseapiZosVSAMDatasetImpl(zosFileHandlerMock, zosImageMock, VSAM_DATASET_NAME);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testCreate() throws ZosVSAMDatasetException, RseapiException {
//        PowerMockito.doReturn(false).doReturn(true).doReturn(false).doReturn(false).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getDefineCommand();
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());
//
//        Assert.assertEquals("create() should return the IZosVSAMDataset instance", zosVSAMDatasetSpy, zosVSAMDatasetSpy.create());
//        
//        String expectedMessage = "VSAM data set \"" + VSAM_DATASET_NAME + "\" not created on image " + IMAGE;
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.create();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testCreateExists() throws ZosVSAMDatasetException {
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//
//        String expectedMessage = "VSAM data set \"" + VSAM_DATASET_NAME + "\" already exists on image " + IMAGE;
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.create();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testDelete() throws ZosVSAMDatasetException, RseapiException {
//        PowerMockito.doReturn(true).doReturn(false).doReturn(true).doReturn(true).doReturn(false).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getDeleteCommand();
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());
//
//        Assert.assertTrue("delete() should return true", zosVSAMDatasetSpy.delete());
//
//        Assert.assertFalse("delete() should return false", zosVSAMDatasetSpy.delete());
//        
//        String expectedMessage = "VSAM data set \"" + VSAM_DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.delete();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testExists() throws ZosDatasetException, ZosVSAMDatasetException {
//        PowerMockito.doReturn(true).when(zosDatasetMock).exists();
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "zosDataset", zosDatasetMock);
//        Assert.assertTrue("exists() should return true", zosVSAMDatasetSpy.exists());
//
//        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetMock).exists();
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.exists();
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testStoreText() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(reproDatasetMock).when(zosVSAMDatasetSpy).createReproDataset(Mockito.any());
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).store(Mockito.any());
//        PowerMockito.doReturn(true).when(reproDatasetMock).delete();
//        zosVSAMDatasetSpy.storeText(CONTENT);
//        Mockito.verify(zosVSAMDatasetSpy, Mockito.times(1)).storeText(Mockito.any());
//        
//        PowerMockito.doThrow(new ZosDatasetException()).when(reproDatasetMock).delete();  
//        String expectedMessage = "Unable to delete IDCAMS REPRO temporary dataset";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.storeText(CONTENT);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreBinary() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(reproDatasetMock).when(zosVSAMDatasetSpy).createReproDataset(Mockito.any());
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).store(Mockito.any());
//        PowerMockito.doReturn(true).when(reproDatasetMock).delete();
//        zosVSAMDatasetSpy.storeBinary(CONTENT.getBytes());
//        Mockito.verify(zosVSAMDatasetSpy, Mockito.times(1)).storeBinary(Mockito.any());
//        
//        PowerMockito.doThrow(new ZosDatasetException()).when(reproDatasetMock).delete(); 
//        String expectedMessage = "Unable to delete IDCAMS REPRO temporary dataset";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.storeBinary(CONTENT.getBytes());
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStore() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(true).when(reproDatasetMock).exists();
//        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getReproFromCommand(Mockito.any());
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());
//        zosVSAMDatasetSpy.store(reproDatasetMock);
//        Mockito.verify(zosVSAMDatasetSpy, Mockito.times(1)).store(Mockito.any());
//
//        PowerMockito.doThrow(new ZosVSAMDatasetException(EXCEPTION)).when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any()); 
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.store(reproDatasetMock);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreNotExistException() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosVSAMDatasetSpy).exists();
//        
//        String expectedMessage = "VSAM data set \"" + VSAM_DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.store(reproDatasetMock);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreFromNotExist() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(false).when(reproDatasetMock).exists();
//        Mockito.doReturn(REPRO_DATASET_NAME).when(reproDatasetMock).getName();
//        
//        String expectedMessage = "From data set \"" + REPRO_DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.store(reproDatasetMock);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreFromDatasetException() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();    
//        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(reproDatasetMock).exists();       
//        
//        String expectedMessage = ZosDatasetException.class.getName() + ": " + EXCEPTION;
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.store(reproDatasetMock);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsText() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(reproDatasetMock).when(zosVSAMDatasetSpy).createReproDataset(Mockito.any());
//        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getReproToCommand(Mockito.any());        
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());
//        PowerMockito.doReturn(CONTENT).when(reproDatasetMock).retrieveAsText();
//        PowerMockito.doReturn(true).when(reproDatasetMock).delete();
//        
//        Assert.assertEquals("store() should return the expected value", CONTENT, zosVSAMDatasetSpy.retrieveAsText());
//    }
//    
//    @Test
//    public void testRetrieveAsTextNotExist() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosVSAMDatasetSpy).exists();
//        
//        String expectedMessage = "VSAM data set \"" + VSAM_DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.retrieveAsText();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsTextRetrieveException() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(reproDatasetMock).when(zosVSAMDatasetSpy).createReproDataset(Mockito.any());
//        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getReproToCommand(Mockito.any());        
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());
//        PowerMockito.doThrow(new ZosDatasetException()).when(reproDatasetMock).retrieveAsText();
//        
//        String expectedMessage = "Unable to retrieve content from IDCAMS REPRO temporary dataset";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.retrieveAsText();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsTextDeleteException() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(reproDatasetMock).when(zosVSAMDatasetSpy).createReproDataset(Mockito.any());
//        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getReproToCommand(Mockito.any());        
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());
//        PowerMockito.doReturn(CONTENT).when(reproDatasetMock).retrieveAsText();
//        PowerMockito.doThrow(new ZosDatasetException()).when(reproDatasetMock).delete();
//        
//        String expectedMessage = "Unable to delete IDCAMS REPRO temporary dataset";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.retrieveAsText();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsBinary() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(reproDatasetMock).when(zosVSAMDatasetSpy).createReproDataset(Mockito.any());
//        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getReproToCommand(Mockito.any());        
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(reproDatasetMock).retrieveAsBinary();
//        PowerMockito.doReturn(true).when(reproDatasetMock).delete();
//        
//        Assert.assertArrayEquals("store() should return the expected content", CONTENT.getBytes(), zosVSAMDatasetSpy.retrieveAsBinary());
//    }
//    
//    @Test
//    public void testRetrieveAsBinaryNotExist() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(false).when(zosVSAMDatasetSpy).exists();
//        
//        String expectedMessage = "VSAM data set \"" + VSAM_DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.retrieveAsBinary();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsBinaryRetrieveException() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(reproDatasetMock).when(zosVSAMDatasetSpy).createReproDataset(Mockito.any());
//        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getReproToCommand(Mockito.any());        
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());
//        PowerMockito.doThrow(new ZosDatasetException()).when(reproDatasetMock).retrieveAsBinary();
//        
//        String expectedMessage = "Unable to retrieve content from IDCAMS REPRO temporary dataset";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.retrieveAsBinary();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsBinaryDeleteException() throws ZosVSAMDatasetException, ZosDatasetException {
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(reproDatasetMock).when(zosVSAMDatasetSpy).createReproDataset(Mockito.any());
//        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getReproToCommand(Mockito.any());        
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(reproDatasetMock).retrieveAsBinary();
//        PowerMockito.doThrow(new ZosDatasetException()).when(reproDatasetMock).delete();
//        
//        String expectedMessage = "Unable to delete IDCAMS REPRO temporary dataset";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.retrieveAsBinary();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSaveToResultsArchive() throws IOException, ZosManagerException {
//		zosVSAMDatasetSpy.setShouldArchive(true);
//		
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "dataType", DatasetDataType.TEXT);
//        PowerMockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
//        Path pathMock = newMockedPath(false);
//        zosFileManagerMock.setVsamDatasetArtifactRoot(pathMock);
//        Mockito.when(zosFileHandlerMock.getArtifactsRoot()).thenReturn(pathMock);
//        Whitebox.setInternalState(zosFileManagerMock, "currentTestMethodArchiveFolderName", "testMethod");
//        PowerMockito.doReturn(CONTENT).when(zosVSAMDatasetSpy).retrieveAsText();
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosVSAMDatasetSpy).retrieveAsBinary();
//        PowerMockito.doReturn("0").when(zosVSAMDatasetSpy).getValueFromListcat(Mockito.any());
//        
//        logMessage = null;
//        String expectedMessage = "Archiving \"" + VSAM_DATASET_NAME + "\"" + " to " + PATH_MOCK;
//        zosVSAMDatasetSpy.saveToResultsArchive(RAS_PATH);
//		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//        
//        PowerMockito.doReturn("99").when(zosVSAMDatasetSpy).getValueFromListcat(Mockito.any());        
//        logMessage = null;
//        zosVSAMDatasetSpy.saveToResultsArchive(RAS_PATH);
//		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "dataType", DatasetDataType.BINARY);       
//        logMessage = null;
//        zosVSAMDatasetSpy.saveToResultsArchive(RAS_PATH);
//		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//        
//        PowerMockito.doReturn("XX").when(zosVSAMDatasetSpy).getValueFromListcat(Mockito.any());        
//        logMessage = null;
//        zosVSAMDatasetSpy.saveToResultsArchive(RAS_PATH);
//		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//        
//        PowerMockito.doThrow(new ZosVSAMDatasetException()).when(zosVSAMDatasetSpy).getValueFromListcat(Mockito.any());        
//        logMessage = null;
//        expectedMessage = "Unable to get value of REC-TOTAL from LISTCAT output";
//        zosVSAMDatasetSpy.saveToResultsArchive(RAS_PATH);
//		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//        
//        PowerMockito.doThrow(new ZosManagerException(EXCEPTION)).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());        
//        logMessage = null;
//        expectedMessage = "Unable to save VSAM data set to archive";
//        zosVSAMDatasetSpy.saveToResultsArchive(RAS_PATH);
//		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
//        
//        PowerMockito.doReturn(false).when(zosVSAMDatasetSpy).exists();
//        zosVSAMDatasetSpy.saveToResultsArchive(RAS_PATH);
//		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
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
//    public void testSetDataType() {
//        zosVSAMDatasetSpy.setDataType(DatasetDataType.RECORD);
//        Assert.assertEquals("getDataType() should set the expected value", DatasetDataType.RECORD, zosVSAMDatasetSpy.getDataType());
//    }
//    
//    @Test
//    public void testSetSpace() {
//        zosVSAMDatasetSpy.setSpace(VSAMSpaceUnit.CYLINDERS, 11, 22);
//        Assert.assertEquals("setSpace() should set the expected value", VSAMSpaceUnit.CYLINDERS, Whitebox.getInternalState(zosVSAMDatasetSpy, "spaceUnit"));
//        Assert.assertEquals("setSpace() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "primaryExtents"));
//        Assert.assertEquals("setSpace() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "secondaryExtents"));
//    }
//
//    @Test
//    public void testSetVolumes() {
//        zosVSAMDatasetSpy.setVolumes("VOLUMES");
//        Assert.assertEquals("setVolumes() should set the expected value", "VOLUMES", Whitebox.getInternalState(zosVSAMDatasetSpy, "volumes"));
//    }
//
//    @Test
//    public void testSetAccountInfo() {
//        zosVSAMDatasetSpy.setAccountInfo("ACCOUNT-INFO");
//        Assert.assertEquals("setAccountInfo() should set the expected value", "ACCOUNT-INFO", Whitebox.getInternalState(zosVSAMDatasetSpy, "accountInfo"));
//    }
//
//    @Test
//    public void testSetBufferspace() {
//        zosVSAMDatasetSpy.setBufferspace(5L);
//        Assert.assertEquals("setBufferspace() should set the expected value", Long.valueOf(5L), Whitebox.getInternalState(zosVSAMDatasetSpy, "bufferspace"));
//    }
//
//    @Test
//    public void testSetBwoOption() {
//        zosVSAMDatasetSpy.setBwoOption(BWOOption.NO);
//        Assert.assertEquals("setBwoOption() should set the expected value", BWOOption.NO, Whitebox.getInternalState(zosVSAMDatasetSpy, "bwoOption"));
//    }
//
//    @Test
//    public void testSetControlInterval() {
//        zosVSAMDatasetSpy.setControlInterval("CONTROL-INTERVAL");
//        Assert.assertEquals("setControlInterval() should set the expected value", "CONTROL-INTERVAL", Whitebox.getInternalState(zosVSAMDatasetSpy, "controlInterval"));
//    }
//
//    @Test
//    public void testSetDataclass() {
//        zosVSAMDatasetSpy.setDataclass("DATACLASS");
//        Assert.assertEquals("setDataclass() should set the expected value", "DATACLASS", Whitebox.getInternalState(zosVSAMDatasetSpy, "dataclass"));
//    }
//
//    @Test
//    public void testSetEraseOption() {
//        zosVSAMDatasetSpy.setEraseOption(EraseOption.ERASE);
//        Assert.assertEquals("setEraseOption() should set the expected value", EraseOption.ERASE, Whitebox.getInternalState(zosVSAMDatasetSpy, "eraseOption"));
//    }
//
//    @Test
//    public void testSetExceptionExit() {
//        zosVSAMDatasetSpy.setExceptionExit("EXCEPTION-EXIT");
//        Assert.assertEquals("setExceptionExit() should set the expected value", "EXCEPTION-EXIT", Whitebox.getInternalState(zosVSAMDatasetSpy, "exceptionExit"));
//    }
//
//    @Test
//    public void testSetFreeSpaceOptions() {
//        zosVSAMDatasetSpy.setFreeSpaceOptions(11, 22);
//        Assert.assertEquals("setFreeSpaceOptions() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "freeSpaceControlInterval"));
//        Assert.assertEquals("setFreeSpaceOptions() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "freeSpaceControlArea"));
//    }
//
//    @Test
//    public void testSetFrlogOption() {
//        zosVSAMDatasetSpy.setFrlogOption(FRLogOption.NONE);
//        Assert.assertEquals("setFrlogOption() should set the expected value", FRLogOption.NONE, Whitebox.getInternalState(zosVSAMDatasetSpy, "frlogOption"));
//    }
//
//    @Test
//    public void testSetDatasetOrg() {
//        zosVSAMDatasetSpy.setDatasetOrg(DatasetOrganisation.INDEXED);
//        Assert.assertEquals("setFrlogOption() should set the expected value", DatasetOrganisation.INDEXED, Whitebox.getInternalState(zosVSAMDatasetSpy, "dataOrg"));
//    }
//
//    @Test
//    public void testSetKeyOptions() {
//        zosVSAMDatasetSpy.setKeyOptions(11, 22);
//        Assert.assertEquals("setKeyOptions() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "keyLength"));
//        Assert.assertEquals("setKeyOptions() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "keyOffset"));
//    }
//
//    @Test
//    public void testSetLogOption() {
//        zosVSAMDatasetSpy.setLogOption(LogOption.ALL);
//        Assert.assertEquals("setLogOption() should set the expected value", LogOption.ALL, Whitebox.getInternalState(zosVSAMDatasetSpy, "logOption"));
//    }
//
//    @Test
//    public void testSetLogStreamID() {
//        zosVSAMDatasetSpy.setLogStreamID("LOGSTREAM-ID");
//        Assert.assertEquals("setLogStreamID() should set the expected value", "LOGSTREAM-ID", Whitebox.getInternalState(zosVSAMDatasetSpy, "logStreamID"));
//    }
//
//    @Test
//    public void testSetManagementClass() {
//        zosVSAMDatasetSpy.setManagementClass("MANAGEMENT-CLASS");
//        Assert.assertEquals("setManagementClass() should set the expected value", "MANAGEMENT-CLASS", Whitebox.getInternalState(zosVSAMDatasetSpy, "managementClass"));
//    }
//
//    @Test
//    public void testSetModel() {
//        zosVSAMDatasetSpy.setModel("MODEL-ENTRY-NAME", "MODEL-CAT-NAME");
//        Assert.assertEquals("setModel() should set the expected value", "MODEL-ENTRY-NAME", Whitebox.getInternalState(zosVSAMDatasetSpy, "modelEntryName"));
//        Assert.assertEquals("setModel() should set the expected value", "MODEL-CAT-NAME", Whitebox.getInternalState(zosVSAMDatasetSpy, "modelCatName"));
//    }
//
//    @Test
//    public void testSetOwner() {
//        zosVSAMDatasetSpy.setOwner("OWNER");
//        Assert.assertEquals("setOwner() should set the expected value", "OWNER", Whitebox.getInternalState(zosVSAMDatasetSpy, "owner"));
//    }
//
//    @Test
//    public void testSetRecatalogOption() {
//        zosVSAMDatasetSpy.setRecatalogOption(RecatalogOption.RECATALOG);
//        Assert.assertEquals("setRecatalogOption() should set the expected value", RecatalogOption.RECATALOG, Whitebox.getInternalState(zosVSAMDatasetSpy, "recatalogOption"));
//    }
//
//    @Test
//    public void testSetRecordSize() {
//        zosVSAMDatasetSpy.setRecordSize(11, 22);
//        Assert.assertEquals("setRecordSize() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "averageRecordSize"));
//        Assert.assertEquals("setRecordSize() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "maxRecordSize"));
//    }
//
//    @Test
//    public void testSetReuseOption() {
//        zosVSAMDatasetSpy.setReuseOption(ReuseOption.REUSE);
//        Assert.assertEquals("setReuseOption() should set the expected value", ReuseOption.REUSE, Whitebox.getInternalState(zosVSAMDatasetSpy, "reuseOption"));
//    }
//
//    @Test
//    public void testSetShareOptions() {
//        zosVSAMDatasetSpy.setShareOptions(11, 22);
//        Assert.assertEquals("setShareOptions() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "crossRegionShareOption"));
//        Assert.assertEquals("setShareOptions() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "crossSystemShareOption"));
//    }
//
//    @Test
//    public void testSetSpanOption() {
//        zosVSAMDatasetSpy.setSpanOption(SpanOption.SPANNED);
//        Assert.assertEquals("setSpanOption() should set the expected value", SpanOption.SPANNED, Whitebox.getInternalState(zosVSAMDatasetSpy, "spanOption"));
//    }
//
//    @Test
//    public void testSetSpeedRecoveryOption() {
//        zosVSAMDatasetSpy.setSpeedRecoveryOption(SpeedRecoveryOption.RECOVERY);
//        Assert.assertEquals("setSpeedRecoveryOption() should set the expected value", SpeedRecoveryOption.RECOVERY, Whitebox.getInternalState(zosVSAMDatasetSpy, "speedRecoveryOption"));
//    }
//
//    @Test
//    public void testSetStorageClass() {
//        zosVSAMDatasetSpy.setStorageClass("STORAGE-CLASS");
//        Assert.assertEquals("setStorageClass() should set the expected value", "STORAGE-CLASS", Whitebox.getInternalState(zosVSAMDatasetSpy, "storageClass"));
//    }
//
//    @Test
//    public void testSetWriteCheckOption() {
//        zosVSAMDatasetSpy.setWriteCheckOption(WriteCheckOption.WRITECHECK);
//        Assert.assertEquals("setWriteCheckOption() should set the expected value", WriteCheckOption.WRITECHECK, Whitebox.getInternalState(zosVSAMDatasetSpy, "writeCheckOption"));
//    }
//
//    @Test
//    public void testSetUseDATA() {
//        zosVSAMDatasetSpy.setUseDATA(true, true);
//        Assert.assertTrue("setUseDATA() should set the expected value", Whitebox.getInternalState(zosVSAMDatasetSpy, "useDATA"));
//        Assert.assertTrue("setUseDATA() should set the expected value", Whitebox.getInternalState(zosVSAMDatasetSpy, "uniqueDATA"));
//    }
//
//    @Test
//    public void testSetDataName() {
//        zosVSAMDatasetSpy.setDataName("DATA_NAME");
//        Assert.assertEquals("setLogStreamID() should set the expected value", "DATA_NAME", Whitebox.getInternalState(zosVSAMDatasetSpy, "dataName"));
//    }
//
//    @Test
//    public void testSetDataSpace() {
//        zosVSAMDatasetSpy.setDataSpace(VSAMSpaceUnit.CYLINDERS, 11, 22);
//        Assert.assertEquals("setDataSpace() should set the expected value", VSAMSpaceUnit.CYLINDERS, Whitebox.getInternalState(zosVSAMDatasetSpy, "dataSpaceUnit"));
//        Assert.assertEquals("setDataSpace() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "dataPrimaryExtents"));
//        Assert.assertEquals("setDataSpace() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "dataSecondaryExtents"));
//    }
//
//    @Test
//    public void testSetDataVolumes() {
//        zosVSAMDatasetSpy.setDataVolumes("VOLUME");
//        Assert.assertEquals("setDataVolumes() should set the expected value", "VOLUME", Whitebox.getInternalState(zosVSAMDatasetSpy, "dataVolumes"));
//    }
//
//    @Test
//    public void testSetDataBufferspace() {
//        zosVSAMDatasetSpy.setDataBufferspace(11L);
//        Assert.assertEquals("setDataBufferspace() should set the expected value", Long.valueOf(11L), Whitebox.getInternalState(zosVSAMDatasetSpy, "dataBufferspace"));
//    }
//
//    @Test
//    public void testSetDataControlInterval() {
//        zosVSAMDatasetSpy.setDataControlInterval("DATA-CONTROL-INTERVAL");
//        Assert.assertEquals("setDataControlInterval() should set the expected value", "DATA-CONTROL-INTERVAL", Whitebox.getInternalState(zosVSAMDatasetSpy, "dataControlInterval"));
//    }
//
//    @Test
//    public void testSetDataEraseOption() {
//        zosVSAMDatasetSpy.setDataEraseOption(EraseOption.ERASE);
//        Assert.assertEquals("setDataEraseOption() should set the expected value", EraseOption.ERASE, Whitebox.getInternalState(zosVSAMDatasetSpy, "dataEraseOption"));
//    }
//
//    @Test
//    public void testSetDataExceptionExit() {
//        zosVSAMDatasetSpy.setDataExceptionExit("DATA-EXCEPTION-EXIT");
//        Assert.assertEquals("setDataExceptionExit() should set the expected value", "DATA-EXCEPTION-EXIT", Whitebox.getInternalState(zosVSAMDatasetSpy, "dataExceptionExit"));
//    }
//
//    @Test
//    public void testSetDataFreeSpaceOptions() {
//        zosVSAMDatasetSpy.setDataFreeSpaceOptions(11, 22);
//        Assert.assertEquals("setDataFreeSpaceOptions() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "dataFreeSpaceControlInterval"));
//        Assert.assertEquals("setDataFreeSpaceOptions() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "dataFreeSpaceControlArea"));
//    }
//
//    @Test
//    public void testSetDataKeyOptions() {
//        zosVSAMDatasetSpy.setDataKeyOptions(11, 22);
//        Assert.assertEquals("setDataKeyOptions() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "dataKeyLength"));
//        Assert.assertEquals("setDataKeyOptions() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "dataKeyOffset"));
//    }
//
//    @Test
//    public void testSetDataModel() {
//        zosVSAMDatasetSpy.setDataModel("DATA-MODEL-ENTRY-NAME", "DATA-MODEL-CAT-NAME");
//        Assert.assertEquals("setDataModel() should set the expected value", "DATA-MODEL-ENTRY-NAME", Whitebox.getInternalState(zosVSAMDatasetSpy, "dataModelEntryName"));
//        Assert.assertEquals("setDataModel() should set the expected value", "DATA-MODEL-CAT-NAME", Whitebox.getInternalState(zosVSAMDatasetSpy, "dataModelCatName"));
//    }
//
//    @Test
//    public void testSetDataOwner() {
//        zosVSAMDatasetSpy.setDataOwner("OWNER");
//        Assert.assertEquals("setLogStreamID() should set the expected value", "OWNER", Whitebox.getInternalState(zosVSAMDatasetSpy, "dataOwner"));
//    }
//
//    @Test
//    public void testSetDataRecordSize() {
//        zosVSAMDatasetSpy.setDataRecordSize(11, 22);
//        Assert.assertEquals("setDataRecordSize() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "dataAverageRecordSize"));
//        Assert.assertEquals("setDataRecordSize() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "dataMaxRecordSize"));
//    }
//
//    @Test
//    public void testSetDataReuseOption() {
//        zosVSAMDatasetSpy.setDataReuseOption(ReuseOption.REUSE);
//        Assert.assertEquals("setDataReuseOption() should set the expected value", ReuseOption.REUSE, Whitebox.getInternalState(zosVSAMDatasetSpy, "dataReuseOption"));
//    }
//
//    @Test
//    public void testSetDataShareOptions() {
//        zosVSAMDatasetSpy.setDataShareOptions(11, 22);
//        Assert.assertEquals("setDataShareOptions() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "dataCrossRegionShareOption"));
//        Assert.assertEquals("setDataShareOptions() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "dataCrossSystemShareOption"));
//    }
//
//    @Test
//    public void testSetDataSpanOption() {
//        zosVSAMDatasetSpy.setDataSpanOption(SpanOption.SPANNED);
//        Assert.assertEquals("setDataSpanOption() should set the expected value", SpanOption.SPANNED, Whitebox.getInternalState(zosVSAMDatasetSpy, "dataSpanOption"));
//    }
//
//    @Test
//    public void testSetDataSpeedRecoveryOption() {
//        zosVSAMDatasetSpy.setDataSpeedRecoveryOption(SpeedRecoveryOption.RECOVERY);
//        Assert.assertEquals("setDataSpeedRecoveryOption() should set the expected value", SpeedRecoveryOption.RECOVERY, Whitebox.getInternalState(zosVSAMDatasetSpy, "dataSpeedRecoveryOption"));
//    }
//
//    @Test
//    public void testSetDataWriteCheckOption() {
//        zosVSAMDatasetSpy.setDataWriteCheckOption(WriteCheckOption.WRITECHECK);
//        Assert.assertEquals("setDataWriteCheckOption() should set the expected value", WriteCheckOption.WRITECHECK, Whitebox.getInternalState(zosVSAMDatasetSpy, "dataWriteCheckOption"));
//    }
//
//    @Test
//    public void testSetUseINDEX() {
//        zosVSAMDatasetSpy.setUseINDEX(true, true);
//        Assert.assertTrue("setUseINDEX() should set the expected value", Whitebox.getInternalState(zosVSAMDatasetSpy, "useINDEX"));
//        Assert.assertTrue("setUseINDEX() should set the expected value", Whitebox.getInternalState(zosVSAMDatasetSpy, "uniqueINDEX"));;
//    }
//
//    @Test
//    public void testSetIndexName() {
//        zosVSAMDatasetSpy.setIndexName("INDEX-NAME");
//        Assert.assertEquals("setIndexName() should set the expected value", "INDEX-NAME", Whitebox.getInternalState(zosVSAMDatasetSpy, "indexName"));
//    }
//
//    @Test
//    public void testSetIndexSpace() {
//        zosVSAMDatasetSpy.setIndexSpace(VSAMSpaceUnit.CYLINDERS, 11, 22);
//        Assert.assertEquals("setIndexSpace() should set the expected value", VSAMSpaceUnit.CYLINDERS, Whitebox.getInternalState(zosVSAMDatasetSpy, "indexSpaceUnit"));
//        Assert.assertEquals("setIndexSpace() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "indexPrimaryExtents"));
//        Assert.assertEquals("setIndexSpace() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "indexSecondaryExtents"));
//    }
//
//    @Test
//    public void testSetIndexVolumes() {
//        zosVSAMDatasetSpy.setIndexVolumes("INDEX-VOLUMES");
//        Assert.assertEquals("setIndexVolumes() should set the expected value", "INDEX-VOLUMES", Whitebox.getInternalState(zosVSAMDatasetSpy, "indexVolumes"));
//    }
//
//    @Test
//    public void testSetIndexControlInterval() {
//        zosVSAMDatasetSpy.setIndexControlInterval("INDEX-CONTROL-INTERVAL");
//        Assert.assertEquals("setIndexControlInterval() should set the expected value", "INDEX-CONTROL-INTERVAL", Whitebox.getInternalState(zosVSAMDatasetSpy, "indexControlInterval"));
//    }
//
//    @Test
//    public void testSetIndexExceptionExit() {
//        zosVSAMDatasetSpy.setIndexExceptionExit("INDEX-EXCEPTION-EXIT");
//        Assert.assertEquals("setIndexExceptionExit() should set the expected value", "INDEX-EXCEPTION-EXIT", Whitebox.getInternalState(zosVSAMDatasetSpy, "indexExceptionExit"));
//    }
//
//    @Test
//    public void testSetIndexModel() {
//        zosVSAMDatasetSpy.setIndexModel("INDEX-MODEL-ENTRY-NAME", "INDEX-MODEL-CAT-NAME");
//        Assert.assertEquals("setIndexModel() should set the expected value", "INDEX-MODEL-ENTRY-NAME", Whitebox.getInternalState(zosVSAMDatasetSpy, "indexModelEntryName"));
//        Assert.assertEquals("setIndexModel() should set the expected value", "INDEX-MODEL-CAT-NAME", Whitebox.getInternalState(zosVSAMDatasetSpy, "indexModelCatName"));
//    }
//
//    @Test
//    public void testSetIndexOwner() {
//        zosVSAMDatasetSpy.setIndexOwner("INDEX-OWNER");
//        Assert.assertEquals("setIndexOwner() should set the expected value", "INDEX-OWNER", Whitebox.getInternalState(zosVSAMDatasetSpy, "indexOwner"));
//    }
//
//    @Test
//    public void testSetIndexReuseOption() {
//        zosVSAMDatasetSpy.setIndexReuseOption(ReuseOption.REUSE);
//        Assert.assertEquals("setIndexReuseOption() should set the expected value", ReuseOption.REUSE, Whitebox.getInternalState(zosVSAMDatasetSpy, "indexReuseOption"));
//    }
//
//    @Test
//    public void testSetIndexShareOptions() {
//        zosVSAMDatasetSpy.setIndexShareOptions(11, 22);
//        Assert.assertEquals("setIndexShareOptions() should set the expected value", Integer.valueOf(11), Whitebox.getInternalState(zosVSAMDatasetSpy, "indexCrossRegionShareOption"));
//        Assert.assertEquals("setIndexShareOptions() should set the expected value", Integer.valueOf(22), Whitebox.getInternalState(zosVSAMDatasetSpy, "indexCrossSystemShareOption"));
//    }
//
//    @Test
//    public void testSetIndexWriteCheckOption() {
//        zosVSAMDatasetSpy.setIndexWriteCheckOption(WriteCheckOption.WRITECHECK);
//        Assert.assertEquals("setIndexWriteCheckOption() should set the expected value", WriteCheckOption.WRITECHECK, Whitebox.getInternalState(zosVSAMDatasetSpy, "indexWriteCheckOption"));
//    }
//
//    @Test
//    public void testSetCatalog() {
//        zosVSAMDatasetSpy.setCatalog("CATALOG");
//        Assert.assertEquals("setCatalog() should set the expected value", "CATALOG", Whitebox.getInternalState(zosVSAMDatasetSpy, "catalog"));
//        Assert.assertTrue("setCatalog() should set the expected value", Whitebox.getInternalState(zosVSAMDatasetSpy, "useCATALOG"));
//    }
//
//    @Test
//    public void testGetName() {
//        Assert.assertEquals("getName() should return the expected value", VSAM_DATASET_NAME, zosVSAMDatasetSpy.getName());
//    }
//    
//    @Test
//    public void testGetDefineCommand() throws ZosVSAMDatasetException {
//        String defineCommand = "DEFINE -\n" + 
//                "  CLUSTER(  NAME('" + VSAM_DATASET_NAME + "') -\n" + 
//                "  CYLINDERS(99 99) -\n" + 
//                "  )";
//        zosVSAMDatasetSpy.setSpace(VSAMSpaceUnit.CYLINDERS, 99, 99);
//        Assert.assertEquals("getDefineCommand() should return the expected value", defineCommand, zosVSAMDatasetSpy.getDefineCommand());
//        
//        zosVSAMDatasetSpy.setBufferspace(99L);
//        zosVSAMDatasetSpy.setFreeSpaceOptions(99, 99);
//        defineCommand = "DEFINE -\n" + 
//                "  CLUSTER(  NAME('" + VSAM_DATASET_NAME + "') -\n" + 
//                "  CYLINDERS(99 99) -\n" + 
//                "  BUFSP(99) -\n" +
//                "  FSPC(99 99) -\n" +
//                "  )";
//        Assert.assertEquals("getDefineCommand() should return the expected value", defineCommand, zosVSAMDatasetSpy.getDefineCommand());
//        
//        zosVSAMDatasetSpy.setFreeSpaceOptions(99, 99);
//        zosVSAMDatasetSpy.setKeyOptions(99, 99);
//        zosVSAMDatasetSpy.setModel("MODEL-ENTRY-NAME", "MODEL-CAT-NAME");
//        zosVSAMDatasetSpy.setRecordSize(99, 99);
//        zosVSAMDatasetSpy.setShareOptions(99, 99);
//        defineCommand = "DEFINE -\n" + 
//                "  CLUSTER(  NAME('" + VSAM_DATASET_NAME + "') -\n" + 
//                "  CYLINDERS(99 99) -\n" +  
//                "  BUFSP(99) -\n" +
//                "  FSPC(99 99) -\n" +
//                "  KEYS(99 99) -\n" + 
//                "  MODEL(MODEL-ENTRY-NAME MODEL-CAT-NAME) -\n" + 
//                "  RECSZ(99 99) -\n" + 
//                "  SHR(99 99) -\n" +
//                "  )";
//        Assert.assertEquals("getDefineCommand() should return the expected value", defineCommand, zosVSAMDatasetSpy.getDefineCommand());
//        
//        zosVSAMDatasetSpy.setUseDATA(true, true);
//        zosVSAMDatasetSpy.setUseINDEX(true, true);
//        zosVSAMDatasetSpy.setCatalog("CATALOG");
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).getDataDefineCommand(Mockito.any());
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).getIndexDefineCommand(Mockito.any());
//        defineCommand = defineCommand + "  CAT(CATALOG)";
//        Assert.assertEquals("getDefineCommand() should return the expected value", defineCommand, zosVSAMDatasetSpy.getDefineCommand());
//    }
//    
//    @Test
//    public void testGetDeleteCommand() throws ZosVSAMDatasetException {
//        String deleteCommand = "DELETE -\n  '" + VSAM_DATASET_NAME + "' -\n  PURGE";
//        Assert.assertEquals("getDeleteCommand() should return the expected value", deleteCommand, zosVSAMDatasetSpy.getDeleteCommand());
//    }
//
//    @Test
//    public void testGetReproToCommand() {
//        String reproCommand = "REPRO -\n  INDATASET('" + VSAM_DATASET_NAME + "') -\n  OUTDATASET('" + REPRO_DATASET_NAME + "')";
//        Assert.assertEquals("getReproToCommand() should return the expected value", reproCommand, zosVSAMDatasetSpy.getReproToCommand(REPRO_DATASET_NAME));        
//    }
//
//    @Test
//    public void testGetReproFromCommand() {
//        String reproCommand = "REPRO -\n  INDATASET('" + REPRO_DATASET_NAME + "') -\n  OUTDATASET('" + VSAM_DATASET_NAME + "')";
//        Assert.assertEquals("getReproFromCommand() should return the expected value", reproCommand, zosVSAMDatasetSpy.getReproFromCommand(REPRO_DATASET_NAME));   
//    }
//    
//    @Test
//    public void testGetListcatOutput() throws ZosVSAMDatasetException {
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsOutput", IDCAMS_COMMAND);
//        Assert.assertEquals("getListcatOutput() should return the expected value", IDCAMS_COMMAND, zosVSAMDatasetSpy.getListcatOutput());
//
//        PowerMockito.doReturn(false).when(zosVSAMDatasetSpy).exists();
//        String expectedMessage = "VSAM data set \"" + VSAM_DATASET_NAME + "\" does not exist on image " + IMAGE;
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.getListcatOutput();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttibutesAsString() throws ZosVSAMDatasetException {
//        PowerMockito.doReturn(false).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getDefineCommand();
//        Assert.assertEquals("getAttibutesAsString() should return the expected value", IDCAMS_COMMAND, zosVSAMDatasetSpy.getAttibutesAsString());
//
//        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();
//        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getListcatOutput();
//        Assert.assertEquals("getAttibutesAsString() should return the expected value", IDCAMS_COMMAND, zosVSAMDatasetSpy.getAttibutesAsString());
//    }
//    
//    @Test
//    public void testCreateReproDataset() throws ZosFileManagerException {
//        Mockito.when(zosFileManagerMock.getRunDatasetHLQ(Mockito.any())).thenReturn(REPRO_DATASET_NAME);
//        PowerMockito.doReturn("CYLINDER").when(zosVSAMDatasetSpy).getValueFromListcat(Mockito.eq("SPACE-TYPE"));
//        PowerMockito.doReturn("0").when(zosVSAMDatasetSpy).getValueFromListcat(Mockito.eq("MAXLRECL"));
//        PowerMockito.doReturn("0").when(zosVSAMDatasetSpy).getValueFromListcat(Mockito.eq("SPACE-PRI"));
//        PowerMockito.doReturn("0").when(zosVSAMDatasetSpy).getValueFromListcat(Mockito.eq("SPACE-SEC"));
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "maxRecordSize", 0);
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "spaceUnit", VSAMSpaceUnit.CYLINDERS);
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "zosFileHandler", zosFileHandlerMock);
//        PowerMockito.doReturn(reproDatasetMock).when(zosFileHandlerMock).newDataset(Mockito.any(), Mockito.any());
//        Assert.assertEquals("createReproDataset() should return the expected value", reproDatasetMock, zosVSAMDatasetSpy.createReproDataset(null));
//        
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "maxRecordSize", 4);
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "spaceUnit", VSAMSpaceUnit.TRACKS);
//        Assert.assertEquals("createReproDataset() should return the expected value", reproDatasetMock, zosVSAMDatasetSpy.createReproDataset(CONTENT));
//        
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "spaceUnit", VSAMSpaceUnit.RECORDS);
//        Assert.assertEquals("createReproDataset() should return the expected value", reproDatasetMock, zosVSAMDatasetSpy.createReproDataset(CONTENT.getBytes()));
//        
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "spaceUnit", (VSAMSpaceUnit) null);
//        Assert.assertEquals("createReproDataset() should return the expected value", reproDatasetMock, zosVSAMDatasetSpy.createReproDataset(CONTENT));
//    }
//    
//    @Test
//    public void testCreateReproDatasetException1() throws ZosFileManagerException {
//        Mockito.when(zosFileManagerMock.getRunDatasetHLQ(Mockito.any())).thenThrow(new ZosFileManagerException(EXCEPTION));
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.createReproDataset(CONTENT);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testCreateReproDatasetException2() throws ZosFileManagerException {
//        Mockito.when(zosFileManagerMock.getRunDatasetHLQ(Mockito.any())).thenReturn(REPRO_DATASET_NAME);
//        PowerMockito.doReturn("0").doReturn("CYLINDER").doReturn("0").doReturn("0").when(zosVSAMDatasetSpy).getValueFromListcat(Mockito.any());
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "zosFileHandler", zosFileHandlerMock);
//        PowerMockito.doReturn(reproDatasetMock).when(zosFileHandlerMock).newDataset(Mockito.any(), Mockito.any());
//        String expectedMessage = "Invalid content type - java.lang.Object";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.createReproDataset(new Object());
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testCreateReproDatasetException3() throws ZosFileManagerException {
//        Mockito.when(zosFileManagerMock.getRunDatasetHLQ(Mockito.any())).thenReturn(REPRO_DATASET_NAME);
//        PowerMockito.doReturn("0").doReturn("CYLINDER").doReturn("0").doReturn("0").when(zosVSAMDatasetSpy).getValueFromListcat(Mockito.any());
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "zosFileHandler", zosFileHandlerMock);
//        Mockito.when(zosFileHandlerMock.newDataset(Mockito.any(), Mockito.any())).thenThrow(new ZosDatasetException());
//        String expectedMessage = "Unable to create temporary dataset for IDCAMS REPRO";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.createReproDataset(CONTENT);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetValueFromListcat() throws ZosVSAMDatasetException {
//        PowerMockito.doReturn("").when(zosVSAMDatasetSpy).getListcatOutput();
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsOutput", IDCAMS_COMMAND);
//        Assert.assertEquals("getValueFromListcat() should return the expected value", "VALUE", zosVSAMDatasetSpy.getValueFromListcat("NAME"));
//        
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsCommand", "LISTCAT");
//        Assert.assertEquals("getValueFromListcat() should return the expected value", "VALUE", zosVSAMDatasetSpy.getValueFromListcat("NAME"));
//        
//        String findString = "XXXX";
//        String expectedMessage = "Unable to find \"" + findString  + "\" in LISTCAT output";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.getValueFromListcat(findString);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testParseOutput() throws ZosVSAMDatasetException {
//        PowerMockito.doReturn(0).when(zosVSAMDatasetSpy).setIdcamsRc();
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsOutput", "");        
//        JsonObject jsonObject =  new JsonObject();
//        jsonObject.addProperty("exit code", 0);
//        JsonObject output = new JsonObject();
//        output.addProperty("stdout", IDCAMS_COMMAND);
//        jsonObject.add("output", output);
//        zosVSAMDatasetSpy.parseOutput(jsonObject);
//        String idcamsOutput = Whitebox.getInternalState(zosVSAMDatasetSpy, "idcamsOutput");
//        Assert.assertEquals("setIdcamsOutput() should return the expected value", IDCAMS_COMMAND, idcamsOutput);
//
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsOutput", "UNTOUCHED");
//        jsonObject.addProperty("exit code", 4);
//        zosVSAMDatasetSpy.parseOutput(jsonObject);
//        idcamsOutput = Whitebox.getInternalState(zosVSAMDatasetSpy, "idcamsOutput");
//        Assert.assertEquals("setIdcamsOutput() should return the expected value", "UNTOUCHED", idcamsOutput);
//    }
//    
//    @Test
//    public void testParseOutputException1() throws ZosVSAMDatasetException {   
//        JsonObject jsonObject =  new JsonObject();
//        String expectedMessage = "Unable to find \"exit code\" member in response body";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.parseOutput(jsonObject);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testParseOutputException2() throws ZosVSAMDatasetException {   
//        JsonObject jsonObject =  new JsonObject();
//        jsonObject.addProperty("exit code", 0);
//        String expectedMessage = "Unable to find \"output\" member in response body";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.parseOutput(jsonObject);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testParseOutputException3() throws ZosVSAMDatasetException {   
//        JsonObject jsonObject =  new JsonObject();
//        jsonObject.addProperty("exit code", 0);
//        JsonObject output = new JsonObject();
//        jsonObject.add("output", output);
//        String expectedMessage = "Unable to find \"stdout\" member in response body";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.parseOutput(jsonObject);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSetLastcc() throws ZosVSAMDatasetException {
//    	Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsOutput", IDCAMS_COMMAND);
//    	Assert.assertEquals("setLastcc() should return the expected value", 0, zosVSAMDatasetSpy.setIdcamsRc());
//    	
//    	Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsOutput", "LASTCC=");
//    	Assert.assertEquals("setLastcc() should return the expected value", 0, zosVSAMDatasetSpy.setIdcamsRc());
//    	
//    	Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsOutput", "LASTCC=99" + IDCAMS_COMMAND + "LASTCC=4");    	
//    	Assert.assertEquals("setLastcc() should return the expected value", 4, zosVSAMDatasetSpy.setIdcamsRc());
//    	
//    	Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsOutput", (String) null); 
//        String expectedMessage = "Unable to parse IDCAMS output for LASTCC - no output";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.setIdcamsRc();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testIdcamsRequest() throws ZosVSAMDatasetException, RseapiException {
//    	setupTestIdcamsRequest();
//        zosVSAMDatasetSpy.idcamsRequest(IDCAMS_COMMAND);
//        Mockito.verify(zosVSAMDatasetSpy, Mockito.times(1)).idcamsRequest(Mockito.any());
//
//        idcamsRc = 4;
//        zosVSAMDatasetSpy.idcamsRequest(IDCAMS_COMMAND);
//        Mockito.verify(zosVSAMDatasetSpy, Mockito.times(2)).idcamsRequest(Mockito.any());
//    }
//
//    @Test
//    public void testIdcamsRequestException1() throws ZosVSAMDatasetException, RseapiException {
//    	setupTestIdcamsRequest();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.idcamsRequest(IDCAMS_COMMAND);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testIdcamsRequestException2() throws ZosVSAMDatasetException, RseapiException {
//    	setupTestIdcamsRequest();
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        String expectedMessage = "Error zOS UNIX command, HTTP Status Code 404 : NOT_FOUND";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.idcamsRequest(IDCAMS_COMMAND);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testIdcamsRequestException3() throws ZosVSAMDatasetException, RseapiException {
//    	setupTestIdcamsRequest();
//    	Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//        String expectedMessage = "Unable to execute IDCAMS command";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.idcamsRequest(IDCAMS_COMMAND);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testIdcamsRequestException4() throws ZosVSAMDatasetException, RseapiException {
//    	setupTestIdcamsRequest();
//        idcamsRc = 20;
//    	Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsOutput", IDCAMS_COMMAND);
//        String expectedMessage = "IDCAMS processing failed: RC=" + idcamsRc + "\n" + IDCAMS_COMMAND;
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.idcamsRequest(IDCAMS_COMMAND);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    private void setupTestIdcamsRequest() throws ZosVSAMDatasetException, RseapiException {
//        idcamsRc = 0;
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "rseapiApiProcessor", rseapiApiProcessorMock);
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(new JsonObject());
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenCallRealMethod();
//        Answer<String> setIdcamsRc = new Answer<String>() {
//            @Override
//            public String answer(InvocationOnMock invocation) throws Throwable {
//            	Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsOutput", IDCAMS_COMMAND);
//            	Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsRc", idcamsRc);
//                return null;
//            }
//        };
//		Mockito.doAnswer(setIdcamsRc).when(zosVSAMDatasetSpy).parseOutput(Mockito.any());    	
//    }
//
//    @Test
//    public void testGetCommandInput() {
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsInput", IDCAMS_COMMAND);
//        Assert.assertEquals("getCommandInput() should return the expected value", IDCAMS_COMMAND, zosVSAMDatasetSpy.getCommandInput());
//    }
//    
//    @Test public void testgetCommandOutput( ) {
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "idcamsOutput", IDCAMS_COMMAND);
//        Assert.assertEquals("getCommandOutput() should return the expected value", IDCAMS_COMMAND, zosVSAMDatasetSpy.getCommandOutput());
//    }
//    
//    @Test
//    public void testGetDefineDataCommand() throws ZosVSAMDatasetException {
//        StringBuilder sb = new StringBuilder();
//        zosVSAMDatasetSpy.setSpace(VSAMSpaceUnit.CYLINDERS, 11, 22);
//        
//        zosVSAMDatasetSpy.getDataDefineCommand(sb);
//        String returnValue = "  DATA(   NAME('" + VSAM_DATASET_DATA_NAME + "') -\n" + 
//                "  )";
//        Assert.assertEquals("getDefineDataCommand() should return the expected value", returnValue, sb.toString());
//        
//        sb = new StringBuilder();
//        zosVSAMDatasetSpy.setSpace(VSAMSpaceUnit.CYLINDERS, 11, 22);
//        zosVSAMDatasetSpy.setDataFreeSpaceOptions(99, 99);
//        
//        zosVSAMDatasetSpy.getDataDefineCommand(sb);
//        returnValue = "  DATA(   NAME('" + VSAM_DATASET_DATA_NAME + "') -\n" +
//                "  FSPC(99 99) -\n" +
//                "  )";
//        Assert.assertEquals("getDefineDataCommand() should return the expected value", returnValue, sb.toString());
//        
//        sb = new StringBuilder();
//        zosVSAMDatasetSpy.setDataSpace(VSAMSpaceUnit.CYLINDERS, 11, 22);
//        zosVSAMDatasetSpy.setDataBufferspace(99);
//        zosVSAMDatasetSpy.setDataKeyOptions(99, 99);
//        zosVSAMDatasetSpy.setDataModel("DATA-MODEL-ENTRY-NAME", "DATA-MODEL-CAT-NAME");
//        zosVSAMDatasetSpy.setDataRecordSize(11, 22);
//        zosVSAMDatasetSpy.setDataShareOptions(11, 22);
//        zosVSAMDatasetSpy.setUseDATA(true, true);
//        
//        zosVSAMDatasetSpy.getDataDefineCommand(sb);
//        returnValue = "  DATA(   NAME('" + VSAM_DATASET_DATA_NAME + "') -\n" +
//                "  CYLINDERS(11 22) -\n" + 
//                "  BUFSP(99) -\n" + 
//                "  FSPC(99 99) -\n" + 
//                "  KEYS(99 99) -\n" + 
//                "  MODEL(DATA-MODEL-ENTRY-NAME DATA-MODEL-CAT-NAME) -\n" + 
//                "  RECSZ(11 22) -\n" + 
//                "  SHR(11 22) -\n" + 
//                "  UNIQUE)";
//        Assert.assertEquals("getDefineDataCommand() should return the expected value", returnValue, sb.toString());
//    }
//    
//    @Test
//    public void testGetIndexDefineCommand() throws ZosVSAMDatasetException {
//        StringBuilder sb = new StringBuilder();
//        zosVSAMDatasetSpy.setIndexSpace(VSAMSpaceUnit.CYLINDERS, 0, 0);
//        
//        zosVSAMDatasetSpy.getIndexDefineCommand(sb);
//        String returnValue = "  INDEX(   NAME('" + VSAM_DATASET_INDEX_NAME + "') -\n" + 
//                "  )";
//        Assert.assertEquals("getIndexDefineCommand() should return the expected value", returnValue, sb.toString());
//        
//        
//        sb = new StringBuilder();
//        zosVSAMDatasetSpy.setIndexSpace(VSAMSpaceUnit.CYLINDERS, 11, 22);
//        zosVSAMDatasetSpy.setIndexModel("INDEX-MODEL-ENTRY-NAME", "INDEX-MODEL-CAT-NAME");
//        zosVSAMDatasetSpy.setIndexShareOptions(11, 22);
//        zosVSAMDatasetSpy.setUseINDEX(true, true);
//        
//        zosVSAMDatasetSpy.getIndexDefineCommand(sb);
//        returnValue = "  INDEX(   NAME('" + VSAM_DATASET_INDEX_NAME + "') -\n" + 
//                "  CYLINDERS(11 22) -\n" + 
//                "  MODEL(INDEX-MODEL-ENTRY-NAME INDEX-MODEL-CAT-NAME) -\n" + 
//                "  SHR(11 22) -\n" + 
//                "  UNIQUE)";
//        Assert.assertEquals("getIndexDefineCommand() should return the expected value", returnValue, sb.toString());
//    }
//    
//    @Test
//    public void testAppendDeclaration() {
//        StringBuilder sb = new StringBuilder();
//        zosVSAMDatasetSpy.appendDeclaration(null, sb);
//        String returnValue = "";
//        Assert.assertEquals("appendDeclaration() should return the expected value", returnValue, sb.toString());
//        
//        sb = new StringBuilder();
//        zosVSAMDatasetSpy.appendDeclaration("VALUE", sb);
//        returnValue = " VALUE";
//        Assert.assertEquals("appendDeclaration() should return the expected value", returnValue, sb.toString());
//        
//    }
//    
//    @Test
//    public void testAppendParameterException() throws ZosVSAMDatasetException {
//        StringBuilder sb = new StringBuilder();
//        String parameter = "PARAMETER";
//        String expectedMessage = "Required parameter '" + parameter + "' has not been set";
//        ZosVSAMDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosVSAMDatasetException.class, ()->{
//        	zosVSAMDatasetSpy.appendParameter(parameter, null, sb, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testToString() {
//        Assert.assertEquals("toString() should return the expected value", VSAM_DATASET_NAME, zosVSAMDatasetSpy.toString());
//    }
//
//    @Test
//    public void testCreated() {
//        Whitebox.setInternalState(zosVSAMDatasetSpy, "datasetCreated", true);
//        Assert.assertTrue("created() should return the expected value", zosVSAMDatasetSpy.created());
//    }
//    
//    @Test
//    public void testArchiveContent() throws ZosVSAMDatasetException {
//    	Mockito.doNothing().when(zosVSAMDatasetSpy).saveToResultsArchive(Mockito.any());
//    	Mockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
//    	Mockito.when(zosVSAMDatasetSpy.shouldArchive()).thenReturn(false);
//    	zosVSAMDatasetSpy.archiveContent();
//    	Mockito.verify(zosVSAMDatasetSpy, Mockito.times(0)).saveToResultsArchive(Mockito.any());
//    	
//    	Mockito.when(zosVSAMDatasetSpy.shouldArchive()).thenReturn(true);
//    	zosVSAMDatasetSpy.archiveContent();
//    	Mockito.verify(zosVSAMDatasetSpy, Mockito.times(1)).saveToResultsArchive(Mockito.any());
//    }
//    
//    @Test
//    public void testShouldCleanup() {
//    	zosVSAMDatasetSpy.setShouldCleanup(false);
//    	Assert.assertFalse("setShouldCleanup() should return false", zosVSAMDatasetSpy.shouldCleanup());
//    	zosVSAMDatasetSpy.setShouldCleanup(true);
//    	Assert.assertTrue("setShouldCleanup() should return true", zosVSAMDatasetSpy.shouldCleanup());
//    }
}
