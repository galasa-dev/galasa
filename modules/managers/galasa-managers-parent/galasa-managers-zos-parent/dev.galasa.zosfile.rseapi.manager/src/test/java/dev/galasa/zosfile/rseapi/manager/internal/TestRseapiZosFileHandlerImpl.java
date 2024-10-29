/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.nio.file.Path;
import java.util.ArrayList;
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

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.internal.RseapiManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LogFactory.class})
public class TestRseapiZosFileHandlerImpl {
//    
//    private RseapiZosFileHandlerImpl zosFileHandler;
//
//    private RseapiZosFileHandlerImpl zosFileHandlerSpy;
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
//    private IRseapiRestApiProcessor rseapiApiProcessorMock;
//    
//    @Mock
//    private RseapiZosDatasetImpl zosDatasetImplMock;
//    
//    @Mock
//    private RseapiZosVSAMDatasetImpl zosVSAMDatasetImplMock;
//    
//    @Mock
//    private RseapiZosUNIXFileImpl zosUNIXFileImplMock;
//    
//    @Mock
//    private Log logMock;
//    
//    private static String logMessage;
//
//    private static final String DATASET_NAME = "DATA.SET.NAME";
//
//    private static final String UNIX_FILE_NAME = "/unix/file";
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
//        Mockito.doAnswer(answer).when(logMock).error(Mockito.any(), Mockito.any());
//        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
//        Mockito.when(zosManagerMock.getZosFilePropertyFileRestrictToImage(Mockito.any())).thenReturn(true);
//        PowerMockito.doReturn(rseapiApiProcessorMock).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//
//        zosFileHandler = new RseapiZosFileHandlerImpl(zosFileManagerMock);
//        zosFileHandlerSpy = Mockito.spy(zosFileHandler);
//    }
//    
//    @Test
//    public void testConstructor() {
//        Assert.assertEquals("Constructor should return ", zosFileHandler.toString(), new RseapiZosFileHandlerImpl(zosFileManagerMock, "INTERNAL").toString());
//        Path pathMock = Mockito.mock(Path.class);
//        Mockito.when(zosFileManagerMock.getArtifactsRoot()).thenReturn(pathMock);
//		Assert.assertEquals("Constructor should return ", pathMock, zosFileHandlerSpy.getArtifactsRoot());
//    }
//    
//    @Test
//    public void testNewDataset() throws ZosDatasetException {
//    	Mockito.when(zosFileHandlerSpy.getRseapiManager()).thenReturn(rseapiManagerMock);
//    	Mockito.when(zosFileHandlerSpy.getZosManager()).thenReturn(zosManagerMock);
//        Object obj = zosFileHandlerSpy.newDataset(DATASET_NAME, zosImageMock);
//        Assert.assertTrue("Error in newDataset() method", obj instanceof RseapiZosDatasetImpl);
//    }
//    
//    @Test
//    public void testNewUNIXFile() throws Exception {
//    	Mockito.when(zosFileHandlerSpy.getRseapiManager()).thenReturn(rseapiManagerMock);
//    	Mockito.when(zosFileHandlerSpy.getZosManager()).thenReturn(zosManagerMock);
//        Object obj = zosFileHandlerSpy.newUNIXFile(UNIX_FILE_NAME, zosImageMock);
//        Assert.assertTrue("Error in newUNIXFile() method", obj instanceof RseapiZosUNIXFileImpl);
//    }
//    
//    @Test
//    public void testNewVSAMDataset() throws ZosVSAMDatasetException {
//    	Mockito.when(zosFileHandlerSpy.getRseapiManager()).thenReturn(rseapiManagerMock);
//    	Mockito.when(zosFileHandlerSpy.getZosManager()).thenReturn(zosManagerMock);
//        Object obj = zosFileHandlerSpy.newVSAMDataset(DATASET_NAME, zosImageMock);
//        Assert.assertTrue("Error in newVSAMDataset() method", obj instanceof RseapiZosVSAMDatasetImpl);
//    }
//    
//    @Test
//    public void testCleanupMethods() throws Exception {
//        Mockito.doNothing().when(zosFileHandlerSpy).cleanupDatasets();
//        Mockito.doNothing().when(zosFileHandlerSpy).cleanupVsamDatasets();
//        Mockito.doNothing().when(zosFileHandlerSpy).cleanupUnixFiles();
//        zosFileHandlerSpy.cleanup();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupUnixFiles");
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupVsamDatasets");
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupUnixFiles");
//    }
//    
//    @Test
//    public void testCleanupDatasets() throws Exception {
//        List<RseapiZosDatasetImpl> zosDatasets = new ArrayList<>();        
//        Mockito.doReturn(false).when(zosDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosDatasetImplMock).exists();
//        zosDatasets.add(zosDatasetImplMock);
//        Whitebox.setInternalState(zosFileHandlerSpy, "zosDatasets", zosDatasets);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupDatasets");
//        
//        Mockito.doReturn(false).when(zosDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosDatasetImplMock).shouldArchive();        
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupDatasets");
//        
//        Mockito.doReturn(true).when(zosDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosDatasetImplMock).exists(); 
//        Mockito.doReturn(false).when(zosDatasetImplMock).shouldArchive();       
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupDatasets");
//        
//        Mockito.doReturn(true).when(zosDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosDatasetImplMock).shouldArchive();
//        Mockito.doReturn(false).when(zosDatasetImplMock).shouldCleanup();
//        Mockito.doReturn(true).when(zosDatasetImplMock).delete();
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupDatasets");
//        
//        Mockito.doReturn(true).when(zosDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosDatasetImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosDatasetImplMock).shouldCleanup();
//        Mockito.doReturn(true).when(zosDatasetImplMock).delete();
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(5)).invoke("cleanupDatasets");
//        
//        Mockito.doReturn(true).when(zosDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosDatasetImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosDatasetImplMock).shouldCleanup();
//        Mockito.doReturn(true).when(zosDatasetImplMock).delete();
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(6)).invoke("cleanupDatasets");
//        
//        Mockito.doThrow(new ZosDatasetException()).when(zosDatasetImplMock).exists();        
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        Assert.assertEquals("cleanupDatasets() should log expected message ", "Problem in data set cleanup phase", logMessage);
//    }
//    
//    @Test
//    public void testCleanupVsamDatasets() throws Exception {
//        List<RseapiZosVSAMDatasetImpl> zosVsamDatasets = new ArrayList<>();        
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        Whitebox.setInternalState(zosFileHandlerSpy, "zosVsamDatasets", zosVsamDatasets);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(5)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(6)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldArchive();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldCleanup();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(7)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldCleanup();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(8)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldCleanup();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(9)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldArchive();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldCleanup();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(10)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doThrow(new ZosVSAMDatasetException()).when(zosVSAMDatasetImplMock).exists();        
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        Assert.assertEquals("cleanupVsamDatasets() should log expected message ", "Problem in VSAM data set cleanup phase", logMessage);
//    }
//    
//    @Test
//    public void testCleanupUnixFiles() throws Exception {
//        List<RseapiZosUNIXFileImpl> zosUnixFiles = new ArrayList<>();        
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        Whitebox.setInternalState(zosFileHandlerSpy, "zosUnixFiles", zosUnixFiles);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(5)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(6)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(7)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(8)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(9)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(10)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(11)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(12)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).isDirectory();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(13)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(14)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(15)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(16)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(17)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(18)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(19)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doThrow(new ZosUNIXFileException()).when(zosUNIXFileImplMock).exists();        
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        Assert.assertEquals("cleanupUnixFiles() should log expected message ", "Problem in UNIX file cleanup phase", logMessage);
//    }
//    
//    @Test
//    public void testBuildErrorString() throws RseapiException {
//    	String action = "action";
//		IRseapiResponse response = Mockito.mock(IRseapiResponse.class);
//		Mockito.when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//		Mockito.when(response.getStatusLine()).thenReturn("OK");
//		Mockito.when(response.getContent()).thenReturn((null));
//		String expectedValue = "Error " + action + ", HTTP Status Code " + HttpStatus.SC_OK + " : OK";
//		Assert.assertEquals("buildErrorString() should return the expected value", expectedValue, zosFileHandlerSpy.buildErrorString(action, response));
//
//		JsonObject jsonObject = new JsonObject();
//		jsonObject.addProperty("status", "status");
//		jsonObject.addProperty("message", "message");
//		Mockito.when(response.getContent()).thenReturn((jsonObject));
//		expectedValue = "Error " + action + ", HTTP Status Code " + HttpStatus.SC_OK + " : OK\nstatus: status\nmessage: message";
//		Assert.assertEquals("buildErrorString() should return the expected value", expectedValue, zosFileHandlerSpy.buildErrorString(action, response));
//
//		String content = "content";
//		Mockito.when(response.getContent()).thenReturn((content));
//		expectedValue = "Error " + action + ", HTTP Status Code " + HttpStatus.SC_OK + " : OK response body:\n" + content;
//		Assert.assertEquals("buildErrorString() should return the expected value", expectedValue, zosFileHandlerSpy.buildErrorString(action, response));
//
//		Mockito.when(response.getContent()).thenReturn((0));
//		expectedValue = "Error " + action + ", HTTP Status Code " + HttpStatus.SC_OK + " : OK";
//		Assert.assertEquals("buildErrorString() should return the expected value", expectedValue, zosFileHandlerSpy.buildErrorString(action, response));
//
//		Mockito.when(response.getContent()).thenThrow(new RseapiException());
//		expectedValue = "Error " + action + ", HTTP Status Code " + HttpStatus.SC_OK + " : OK";
//		Assert.assertEquals("buildErrorString() should return the expected value", expectedValue, zosFileHandlerSpy.buildErrorString(action, response));
//    }
}
