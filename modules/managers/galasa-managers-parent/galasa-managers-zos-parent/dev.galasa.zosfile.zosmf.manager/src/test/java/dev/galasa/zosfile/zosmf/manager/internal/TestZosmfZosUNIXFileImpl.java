/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.zosmf.manager.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
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
import com.google.gson.JsonPrimitive;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileType;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;
import dev.galasa.zosunixcommand.ssh.manager.internal.ZosUNIXCommandManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LogFactory.class})
public class TestZosmfZosUNIXFileImpl {
//    
//    private ZosmfZosUNIXFileImpl zosUNIXFile;
//    
//    private ZosmfZosUNIXFileImpl zosUNIXFileSpy;
//    
//    @Mock
//    private IZosUNIXFile zosUnixFileMockDirectory;
//    
//    @Mock
//    private IZosUNIXFile zosUnixFileMockFile;
//    
//    @Mock
//    private IZosUNIXFile zosUnixFileMockFile2;
//    
//    @Mock
//    private IZosUNIXFile zosUnixFileMockUnknown;
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
//    private IZosUNIXCommand zosUNIXCommandMock;
//    
//    @Mock
//    private ZosUNIXCommandManagerImpl zosUNIXCommandManagerMock;
//    
//    @Mock
//    private IZosmfRestApiProcessor zosmfApiProcessorMock;
//    
//    @Mock
//    private IZosmfResponse zosmfResponseMock;
//    
//    @Mock
//    private Log logMock;
//    
//    private static String logMessage;
//
//    private static final String UNIX_DIRECTORY = "/path1/path2/path3/path4/";
//
//    private static final String UNIX_DIRECTORY_PATH = UNIX_DIRECTORY.substring(0, UNIX_DIRECTORY.length()-1);
//
//    private static final String UNIX_FILE = "file";
//
//    private static final String UNIX_PATH = UNIX_DIRECTORY + UNIX_FILE;
//
//    private static final String UNIX_PATH2 = "/" + UNIX_PATH;
//
//    private static final String UNIX_UNKNOWN = UNIX_DIRECTORY + "unknown";  
//    
//    private static final String TYPE_DIRECTORY = "directory";
//    
//    private static final String MODE = "rwxrwxr-x";
//    
//    private static final String IMAGE = "IMAGE";
//    
//    private static final String CONTENT = "content";
//    
//    private static final String EXCEPTION = "exception";
//    
//    private static final String ERROR = "error";
//
//    private static final String PROPERTY = "property";
//    
//    private static final String VALUE = "value";
//
//    private static final int MAX_ROWS = 1000;
//
//	private static final String RAS_PATH = "RAS_PATH";
//	
//	private static final Set<PosixFilePermission> ACCESS_PERMISSIONS = PosixFilePermissions.fromString("rwxrwxrwx");
//    
//    @Before
//    public void setup() throws Exception {
//		Mockito.when(zosUnixFileMockDirectory.getFileType()).thenReturn(UNIXFileType.DIRECTORY);
//		Mockito.when(zosUnixFileMockFile.getFileType()).thenReturn(UNIXFileType.FILE);
//		Mockito.when(zosUnixFileMockFile2.getFileType()).thenReturn(UNIXFileType.FILE);
//		Mockito.when(zosUnixFileMockUnknown.getFileType()).thenReturn(UNIXFileType.UNKNOWN);
//		
//		Mockito.when(zosUnixFileMockDirectory.getUnixPath()).thenReturn(UNIX_DIRECTORY);
//		Mockito.when(zosUnixFileMockFile.getUnixPath()).thenReturn(UNIX_PATH);
//		Mockito.when(zosUnixFileMockFile2.getUnixPath()).thenReturn(UNIX_PATH2);
//		Mockito.when(zosUnixFileMockUnknown.getUnixPath()).thenReturn(UNIX_UNKNOWN);
//		
//		Mockito.when(zosUnixFileMockFile.getFileName()).thenReturn(UNIX_FILE);
//		Mockito.when(zosUnixFileMockFile2.getFileName()).thenReturn(UNIX_FILE);
//		Mockito.when(zosUnixFileMockUnknown.getFileName()).thenReturn(UNIX_PATH);
//		
//		Mockito.when(zosUnixFileMockDirectory.toString()).thenReturn(UNIX_DIRECTORY);
//		Mockito.when(zosUnixFileMockFile.toString()).thenReturn(UNIX_PATH);
//		Mockito.when(zosUnixFileMockFile2.toString()).thenReturn(UNIX_PATH2);
//		Mockito.when(zosUnixFileMockUnknown.toString()).thenReturn("unknown");
//
//		
//        PowerMockito.mockStatic(LogFactory.class);
//        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
//        Answer<String> answer = new Answer<String>() {
//            @Override
//            public String answer(InvocationOnMock invocation) throws Throwable {
//            	if (invocation.getArgument(0) instanceof String) {
//            		logMessage = invocation.getArgument(0);
//            	}
//                System.err.println("Captured Log Message:\n" + logMessage);
//                if (invocation.getArguments().length > 1 && invocation.getArgument(1) instanceof Throwable) {
//                    ((Throwable) invocation.getArgument(1)).printStackTrace();
//                }
//                return null;
//            }
//        };
//        Mockito.doAnswer(answer).when(logMock).trace(Mockito.any());
//        Mockito.doAnswer(answer).when(logMock).info(Mockito.any());
//    	
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
//        
//        Mockito.when(zosManagerMock.getZosFilePropertyFileRestrictToImage(Mockito.any())).thenReturn(true);
//        Mockito.when(zosManagerMock.getZosFilePropertyDirectoryListMaxItems(Mockito.any())).thenReturn(MAX_ROWS);
//        Mockito.when(zosManagerMock.getZosFilePropertyUnixFilePermissions(Mockito.any())).thenReturn(MODE);
//        
//        Mockito.when(zosManagerMock.getZosFilePropertyFileRestrictToImage(Mockito.any())).thenReturn(true);
//        Mockito.when(zosFileManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        
//        PowerMockito.doReturn(zosmfApiProcessorMock).when(zosmfManagerMock).newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//        Mockito.when(zosFileHandlerMock.getZosmfManager()).thenReturn(zosmfManagerMock);
//        Mockito.when(zosFileHandlerMock.getZosManager()).thenReturn(zosManagerMock);
//        Mockito.when(zosFileHandlerMock.getZosFileManager()).thenReturn(zosFileManagerMock);
//        Mockito.when((zosFileManagerMock).getZosUnixCommandManager()).thenReturn(zosUNIXCommandManagerMock);
//        Mockito.when((zosUNIXCommandManagerMock).getZosUNIXCommand(Mockito.any())).thenReturn(zosUNIXCommandMock);
//
//    	Path pathMock = Mockito.mock(Path.class);
//    	Mockito.doReturn(pathMock).when(pathMock).resolve(Mockito.anyString());
//    	Mockito.doReturn("PATH_NAME").when(pathMock).toString();
//    	Mockito.doReturn(pathMock).when(zosFileManagerMock).getUnixPathArtifactRoot();
//        Mockito.when(zosFileManagerMock.getUnixPathCurrentTestMethodArchiveFolder()).thenReturn(pathMock);
//
//        zosUNIXFile = new ZosmfZosUNIXFileImpl(zosFileHandlerMock, zosImageMock, UNIX_PATH);
//        zosUNIXFileSpy = Mockito.spy(zosUNIXFile);
//    }
//    
//    @Test
//    public void testConstructorException1() throws ZosmfManagerException, ZosFileManagerException {
//    	Assert.assertEquals("getZosFileHandler() should return expected value", zosFileHandlerMock, zosUNIXFileSpy.getZosFileHandler());
//        String expectedMessage = "UNIX path must be absolute not be relative";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	new ZosmfZosUNIXFileImpl(zosFileHandlerMock, zosImageMock, "PATH");
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testConstructorException2() throws ZosmfManagerException, ZosFileManagerException {
//        String expectedMessage = EXCEPTION;
//        Mockito.doThrow(new ZosmfManagerException(EXCEPTION)).when(zosmfManagerMock).newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	new ZosmfZosUNIXFileImpl(zosFileHandlerMock, zosImageMock, UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testConstructorException3() throws ZosmfManagerException, ZosFileManagerException {
//        String expectedMessage = EXCEPTION;
//        Mockito.when(zosManagerMock.getZosFilePropertyDirectoryListMaxItems(Mockito.any())).thenThrow(new ZosFileManagerException(EXCEPTION));
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	new ZosmfZosUNIXFileImpl(zosFileHandlerMock, zosImageMock, UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testCreate() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).createPath(Mockito.any(), Mockito.any(), Mockito.any());
//       
//        zosUNIXFileSpy.create();
//        Assert.assertFalse("created() should return false", zosUNIXFileSpy.created());
//        
//        PowerMockito.doReturn(false).doReturn(true).when(zosUNIXFileSpy).exists();
//        zosUNIXFileSpy.create();
//        Assert.assertTrue("created() should return false", zosUNIXFileSpy.created());
//        
//        PowerMockito.doReturn(false).doReturn(true).when(zosUNIXFileSpy).exists();
//        zosUNIXFileSpy.create();
//        Assert.assertTrue("created() should return false", zosUNIXFileSpy.created());
//        
//        PowerMockito.doReturn(false).doReturn(true).when(zosUNIXFileSpy).exists();
//        Whitebox.setInternalState(zosUNIXFileSpy, "fileName", (String) null);
//        zosUNIXFileSpy.create();
//        Assert.assertTrue("created() should return false", zosUNIXFileSpy.created());
//        
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();  
//        String expectedMessage = "UNIX path '" + UNIX_PATH + "' already exists on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.create();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testDirectoryDeleteNonEmpty() throws ZosUNIXFileException {
//        PowerMockito.doNothing().when(zosUNIXFileSpy).delete(Mockito.anyString(), Mockito.anyBoolean());
//       
//        Assert.assertFalse("directoryDeleteNonEmpty() should return false", zosUNIXFileSpy.directoryDeleteNonEmpty());
//        
//    }
//    
//    @Test
//    public void testDelete() throws ZosUNIXFileException {
//        PowerMockito.doNothing().when(zosUNIXFileSpy).delete(Mockito.anyString(), Mockito.anyBoolean());
//       
//        Assert.assertFalse("delete() should return false", zosUNIXFileSpy.delete());
//        
//    }
//    
//    @Test
//    public void testExists() throws ZosUNIXFileException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//       
//        Assert.assertTrue("exists() should return true", zosUNIXFileSpy.exists());
//    }
//    
//    @Test
//    public void testStoreText() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
//       
//        zosUNIXFileSpy.storeText(CONTENT);
//        
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
//        zosUNIXFileSpy.storeText(CONTENT);
//
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists();
//        String expectedMessage = "UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeText(CONTENT);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreTextIsDirectoryException() throws ZosUNIXFileException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory();
//        String expectedMessage = "Invalid request, '" + UNIX_PATH + "' is a directory";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeText(CONTENT);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreTextZosmfException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = EXCEPTION;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeText(CONTENT);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testStoreTextBadHttpResponseException1() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = ERROR;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeText(CONTENT);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreTextBadHttpResponseException2() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        String expectedMessage = "Unable to write to UNIX path '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeText(CONTENT);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreBinary() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
//       
//        zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
//        zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists();
//        String expectedMessage = "UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreBinaryIsDirectoryException() throws ZosUNIXFileException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory();
//        String expectedMessage = "Invalid request, '" + UNIX_PATH + "' is a directory";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreBinaryZosmfException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = EXCEPTION;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testStoreBinaryBadHttpResponseException1() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = ERROR;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreBinaryBadHttpResponseException2() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        String expectedMessage = "Unable to write to UNIX path '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsText() throws ZosUNIXFileException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(CONTENT).when(zosUNIXFileSpy).retrieveAsText(Mockito.any());
//        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosUNIXFileSpy.retrieveAsText());
//    }
//    
//    @Test
//    public void testRetrieveAsTextException1() throws ZosUNIXFileException {
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists();
//        String expectedMessage = "UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsText();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsTextException2() throws ZosUNIXFileException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory();
//        String expectedMessage = "Invalid request, '" + UNIX_PATH + "' is a directory";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsText();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsBinary() throws ZosUNIXFileException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosUNIXFileSpy).retrieveAsBinary(Mockito.any());
//        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, new String(zosUNIXFileSpy.retrieveAsBinary(), StandardCharsets.UTF_8));
//    }
//    
//    @Test
//    public void testRetrieveAsBinaryException1() throws ZosUNIXFileException {
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists();
//        String expectedMessage = "UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsBinary();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsBinaryException2() throws ZosUNIXFileException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory();
//        String expectedMessage = "Invalid request, '" + UNIX_PATH + "' is a directory";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsBinary();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testOneLineMethods() throws ZosUNIXFileException {
//		zosUNIXFileSpy.setShouldArchive(true);
//        PowerMockito.doNothing().when(zosUNIXFileSpy).saveToResultsArchive(Mockito.any());
//        zosUNIXFileSpy.saveToResultsArchive(RAS_PATH);
//
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        zosUNIXFileSpy.isDirectory();
//
//        SortedMap<String, IZosUNIXFile> dirList = new TreeMap<>();
//        dirList.put(UNIX_PATH, zosUnixFileMockFile);
//        PowerMockito.doReturn(dirList).when(zosUNIXFileSpy).listDirectory(Mockito.any(), Mockito.anyBoolean());
//        Assert.assertEquals("directoryList() should return expected content", dirList, zosUNIXFileSpy.directoryList());
//
//        Assert.assertEquals("directoryListRecursive() should return expected content", dirList, zosUNIXFileSpy.directoryListRecursive());
//
//        Assert.assertEquals("getUnixPath() should return the expected value", UNIX_PATH, zosUNIXFileSpy.getUnixPath());
//
//        Assert.assertEquals("getFileName() should return the expected value", UNIX_FILE, zosUNIXFileSpy.getFileName());
//
//        Assert.assertEquals("getDirectoryPath() should return the expected value", UNIX_DIRECTORY_PATH, zosUNIXFileSpy.getDirectoryPath());
//
//        PowerMockito.doReturn(new JsonObject()).when(zosUNIXFileSpy).getAttributes(Mockito.any());
//        PowerMockito.doReturn(MODE).when(zosUNIXFileSpy).attributesToString(Mockito.any());
//        Assert.assertEquals("getAttributesAsString() should return the expected value", MODE, zosUNIXFileSpy.getAttributesAsString());
//
//        Assert.assertEquals("quoted() should return the expected value", "'" + UNIX_PATH + "'", zosUNIXFileSpy.quoted(UNIX_PATH));
//
//        Assert.assertEquals("logOnImage() should return the expected value", " on image " + IMAGE , zosUNIXFileSpy.logOnImage());
//
//        Assert.assertEquals("toString() should return the expected value", UNIX_PATH, zosUNIXFileSpy.toString());
//        
//        Assert.assertFalse("deleted() should return false", zosUNIXFileSpy.deleted());
//    }
//    
//    @Test
//    public void testDataType() {
//        zosUNIXFileSpy.setDataType(UNIXFileDataType.TEXT);
//        Assert.assertEquals("getDataType() should return the expected value", UNIXFileDataType.TEXT, zosUNIXFileSpy.getDataType());
//        zosUNIXFileSpy.setDataType(UNIXFileDataType.BINARY);
//        Assert.assertEquals("getDataType() should return the expected value", UNIXFileDataType.BINARY, zosUNIXFileSpy.getDataType());
//    }
//    
//    @Test
//    public void testSetAccessPermissions() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, false);
//        
//        zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, true);
//      
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = EXCEPTION;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, true);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//        
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        expectedMessage = "Unable to change file access permissions of UNIX path '" + UNIX_PATH + "' on image " + IMAGE;
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, true);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage()); 
//    }
//            
//    @Test
//    public void testSetAccessPermissionsException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        JsonObject responseBody = new JsonObject();
//		Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(responseBody);
//		PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = ERROR;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, true);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttributesAsString() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        JsonArray jsonArray = new JsonArray();
//        JsonObject items = new JsonObject();
//        items.addProperty("name", UNIX_FILE);
//        items.addProperty("type", "type");    
//        items.addProperty("mode", "-rwxrwxrwx");    
//        items.addProperty("size", 0);    
//        items.addProperty("uid", "uid");     
//        items.addProperty("user", "user");    
//        items.addProperty("gid", "gid");     
//        items.addProperty("group", "group");   
//        items.addProperty("mtime", "modified");
//        items.addProperty("target", "target");  
//        jsonArray.add(items);
//        jsonObject.add("items", jsonArray);
//
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        PowerMockito.doReturn(UNIXFileType.FILE).when(zosUNIXFileSpy).determineType(Mockito.any());
//        Answer<?> emptyStringWhenNullValue = new Answer<String>() {
//            @Override
//            public String answer(InvocationOnMock invocation) throws Throwable {
//            	if (invocation.getArgument(1).equals("mode") || 
//            		invocation.getArgument(1).equals("size")) {
//            		return (String) invocation.callRealMethod();
//                }
//                return invocation.getArgument(1);
//            }
//        };
//        PowerMockito.doAnswer(emptyStringWhenNullValue).when(zosUNIXFileSpy).emptyStringWhenNull(Mockito.any(), Mockito.any());
//        String returnedValue = "Name=name,Type=file,Mode=-rwxrwxrwx,Size=0,UID=uid,User=user,GID=gid,Group=group,Modified=mtime,Target=target";
//        Assert.assertEquals("getAttributesAsString() should return the expected value", returnedValue, zosUNIXFileSpy.attributesToString(zosUNIXFileSpy.getAttributes(UNIX_PATH)));
//        
//        Assert.assertEquals("getAttributesAsString() should return the expected value", returnedValue, zosUNIXFileSpy.attributesToString(zosUNIXFileSpy.getAttributes(UNIX_DIRECTORY + "/")));
//    }
//    
//    @Test
//    public void testGetAttributesAsStringNotExistException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        String expectedMessage = "UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.getAttributes(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttributesAsStringZosmfException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = EXCEPTION;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.getAttributes(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testGetAttributesAsStringZosmfResponseException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = "Unable to list UNIX path '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.getAttributes(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttributesAsStringBadHttpResponseException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = ERROR;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.getAttributes(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testDetermineType() {
//        Assert.assertEquals("determineType() should return the expected value", UNIXFileType.FILE, zosUNIXFileSpy.determineType("-"));
//        Assert.assertEquals("determineType() should return the expected value", UNIXFileType.CHARACTER, zosUNIXFileSpy.determineType("c"));
//        Assert.assertEquals("determineType() should return the expected value", UNIXFileType.DIRECTORY, zosUNIXFileSpy.determineType("d"));
//        Assert.assertEquals("determineType() should return the expected value", UNIXFileType.EXTLINK, zosUNIXFileSpy.determineType("e"));
//        Assert.assertEquals("determineType() should return the expected value", UNIXFileType.SYMBLINK, zosUNIXFileSpy.determineType("l"));
//        Assert.assertEquals("determineType() should return the expected value", UNIXFileType.FIFO, zosUNIXFileSpy.determineType("p"));
//        Assert.assertEquals("determineType() should return the expected value", UNIXFileType.SOCKET, zosUNIXFileSpy.determineType("s"));
//        Assert.assertEquals("determineType() should return the expected value", UNIXFileType.UNKNOWN, zosUNIXFileSpy.determineType("?"));
//    }
//    
//    @Test
//    public void testCreatePath() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
//        
//        PowerMockito.doNothing().when(zosUNIXFileSpy).setAccessPermissions(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
//        Assert.assertTrue("createPath() should return true", zosUNIXFileSpy.createPath(UNIX_PATH, UNIXFileType.FILE, ACCESS_PERMISSIONS));
//      
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = EXCEPTION;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.createPath(UNIX_PATH, UNIXFileType.FILE, ACCESS_PERMISSIONS);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testCreatePathBadHttpResponseException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = ERROR;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.createPath(UNIX_PATH, UNIXFileType.FILE, ACCESS_PERMISSIONS);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testCreatePathZosmfResponseException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = "Unable to create UNIX path '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.createPath(UNIX_PATH, UNIXFileType.FILE, ACCESS_PERMISSIONS);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalDelete() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
//        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
//        
//        zosUNIXFileSpy.delete(UNIX_PATH, false);
//        Assert.assertTrue("delete() should set deleted to true", Whitebox.getInternalState(zosUNIXFileSpy, "deleted"));
//        
//        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        PowerMockito.doNothing().when(zosUNIXFileSpy).unlinkSymlink(Mockito.any(), Mockito.anyBoolean());
//        Whitebox.setInternalState(zosUNIXFileSpy, "fileType", UNIXFileType.SYMBLINK);
//        zosUNIXFileSpy.delete(UNIX_PATH, false);
//        Assert.assertTrue("delete() should set deleted to true", Whitebox.getInternalState(zosUNIXFileSpy, "deleted"));
//        
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        PowerMockito.doNothing().when(zosUNIXFileSpy).unlinkSymlink(Mockito.any(), Mockito.anyBoolean());
//        zosUNIXFileSpy.delete(UNIX_FILE, true);
//        Assert.assertFalse("delete() should set deleted to false", Whitebox.getInternalState(zosUNIXFileSpy, "deleted"));
//    }
//    
//    @Test
//    public void testUnlinkSymlink() throws ZosUNIXFileException, ZosUNIXCommandException {
//    	PowerMockito.doReturn("RC=0").when(zosUNIXCommandMock).issueCommand(Mockito.any());
//    	zosUNIXFileSpy.unlinkSymlink(UNIX_DIRECTORY, true);
//
//    	zosUNIXFileSpy.unlinkSymlink(UNIX_DIRECTORY, false);
//
//    	PowerMockito.doReturn("RC=99").when(zosUNIXCommandMock).issueCommand(Mockito.any());
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.unlinkSymlink(UNIX_DIRECTORY, true);
//        });
//		Assert.assertEquals("exception should contain expected message", "Unable to delete symbolic link(s) - path " + UNIX_DIRECTORY, expectedException.getMessage());
//		Assert.assertEquals("exception should contain expected cause", "Command failed: RC=99", expectedException.getCause().getMessage());
//    	
//    }
//    
//    @Test
//    public void testInternalDeletenotExistException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        String expectedMessage = "UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.delete(UNIX_PATH, false);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalDeleteNotDirectory() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        String expectedMessage = "Invalid request, UNIX path '" + UNIX_PATH + "' is not a directory";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.delete(UNIX_PATH, true);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalDeleteZosmfException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = EXCEPTION;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.delete(UNIX_PATH, false);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalDeleteBadHttpresponseException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = ERROR;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.delete(UNIX_PATH, false);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalDeleteZosmfResposeException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = "Unable to delete UNIX path '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.delete(UNIX_PATH, false);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalExists() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Assert.assertTrue("exists() should set deleted to true", zosUNIXFileSpy.exists(UNIX_DIRECTORY));
//
//        JsonObject jsonObject = new JsonObject();
//        JsonArray jsonArray = new JsonArray();
//        JsonObject items = new JsonObject();
//        items = new JsonObject();
//        items.addProperty("name", UNIX_PATH);  
//        items.addProperty("mode", "lrwxrwxrwx");
//        items.addProperty("size", 0);
//        items.addProperty("mtime", "2021-03-11T09:31:24");
//        items.addProperty("user", "USER");
//        items.addProperty("group", "GROUP");
//        jsonArray.add(items);
//        jsonObject.add("items", jsonArray);
//        jsonObject.addProperty("returnedRows", 1);
//        jsonObject.addProperty("totalRows", 1);
//        
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Assert.assertTrue("exists() should set deleted to true", zosUNIXFileSpy.exists(UNIX_PATH));
//
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Assert.assertFalse("exists() should set deleted to false", zosUNIXFileSpy.exists(UNIX_DIRECTORY + "/"));
//    }
//    
//    @Test
//    public void testInternalZosmfExistsException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = EXCEPTION;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.exists(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalExistsZosmfResponseException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = "Unable to list UNIX path '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.exists(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalExistsException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_ACCEPTABLE);
//        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = ERROR;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.exists(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveAsText() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
//        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosmfResponseMock.getTextContent()).thenReturn(CONTENT);
//        
//        Assert.assertEquals("retrieve() should return the expected value", CONTENT, zosUNIXFileSpy.retrieveAsText(UNIX_PATH));
//    }
//    
//    @Test
//    public void testInternalRetrieveAsTextZosmfException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        String expectedMessage = EXCEPTION;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsText(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveAsTextGetTextContentException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//
//        Mockito.when(zosmfResponseMock.getTextContent()).thenThrow(new ZosmfException(EXCEPTION));
//        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = "Unable to retrieve content of '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsText(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveAsTextZosmfBadHttpResponseException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = ERROR;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsText(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveAsTextGetJsonContentException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = "Unable to retrieve content of '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsText(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveAsBinary() throws ZosUNIXFileException, ZosmfException, IOException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
//        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosmfResponseMock.getContent()).thenReturn(IOUtils.toInputStream(CONTENT, "UTF-8"));
//        
//        Assert.assertEquals("retrieve() should return the expected value", CONTENT, new String((byte[])zosUNIXFileSpy.retrieveAsBinary(UNIX_PATH), StandardCharsets.UTF_8));
//    }
//    
//    @Test
//    public void testInternalRetrieveAsBinaryZosmfException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        String expectedMessage = EXCEPTION;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsBinary(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveAsBinaryGetTextContentException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//
//        Mockito.when(zosmfResponseMock.getContent()).thenThrow(new ZosmfException(EXCEPTION));
//        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = "Unable to retrieve content of '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsBinary(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveAsBinaryZosmfBadHttpResponseException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = ERROR;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsBinary(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveAsBinaryGetJsonContentException() throws ZosUNIXFileException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = "Unable to retrieve content of '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieveAsBinary(UNIX_PATH);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSaveToResultsArchive() throws ZosUNIXFileException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosUNIXFileSpy).retrieveAsBinary(Mockito.any());
//        PowerMockito.doReturn(RAS_PATH).when(zosUNIXFileSpy).storeArtifact(Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any());
//        Whitebox.setInternalState(zosUNIXFileSpy, "dataType", UNIXFileDataType.BINARY);
//        zosUNIXFileSpy.saveToResultsArchive(RAS_PATH);
//        Assert.assertEquals("saveToResultsArchive() should log expected message", "'" + UNIX_PATH + "' archived to " + RAS_PATH, logMessage);
//
//        PowerMockito.doReturn(CONTENT).when(zosUNIXFileSpy).retrieveAsText(Mockito.any());
//        Whitebox.setInternalState(zosUNIXFileSpy, "dataType", UNIXFileDataType.TEXT);
//        zosUNIXFileSpy.saveToResultsArchive(RAS_PATH);
//        Assert.assertEquals("saveToResultsArchive() should log expected message", "'" + UNIX_PATH + "' archived to " + RAS_PATH, logMessage);
//
//        Whitebox.setInternalState(zosUNIXFileSpy, "unixPath", UNIX_DIRECTORY);
//        Whitebox.setInternalState(zosUNIXFileSpy, "directoryPath", (String) null);
//        Whitebox.setInternalState(zosUNIXFileSpy, "fileType", (String) null);
//        zosUNIXFileSpy.splitUnixPath();
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(UNIX_DIRECTORY);
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(UNIX_PATH);
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(UNIX_UNKNOWN);
//        SortedMap<String, IZosUNIXFile> paths = new TreeMap<>();
//        paths.put(UNIX_DIRECTORY, zosUnixFileMockDirectory);
//        paths.put(UNIX_PATH, zosUnixFileMockFile);
//        paths.put(UNIX_UNKNOWN, zosUnixFileMockUnknown);
//        paths.put(UNIX_PATH2, zosUnixFileMockFile2);
//        PowerMockito.doReturn(paths).when(zosUNIXFileSpy).listDirectory(Mockito.any(), Mockito.anyBoolean());
//        zosUNIXFileSpy.saveToResultsArchive(RAS_PATH);
//
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        String expectedMessage = "UNIX path '" + UNIX_DIRECTORY + "' does not exist on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.saveToResultsArchive(RAS_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testIsDirectory() throws ZosUNIXFileException {
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_PATH));
//
//        PowerMockito.doReturn(new JsonObject()).when(zosUNIXFileSpy).getAttributes(Mockito.any());
//        
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_DIRECTORY));
//
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_PATH));
//
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_PATH));
//        
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        Whitebox.setInternalState(zosUNIXFileSpy, "fileType", UNIXFileType.DIRECTORY);
//        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_DIRECTORY));
//        
//        PowerMockito.doReturn(null).when(zosUNIXFileSpy).getAttributes(Mockito.any());
//        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_DIRECTORY));
//        
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("mode", "drwxrwxrwx");
//		PowerMockito.doReturn(jsonObject ).when(zosUNIXFileSpy).getAttributes(Mockito.any());
//        Assert.assertTrue("listDirectory() should return true", zosUNIXFileSpy.isDirectory(UNIX_DIRECTORY));
//    }
//    
//    @Test
//    public void testListDirectory() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject );
//        Map<String, String> paths = new TreeMap<>();
//        paths.put("path1", TYPE_DIRECTORY);
//        PowerMockito.doReturn(paths).when(zosUNIXFileSpy).getPaths(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
//        
//        Assert.assertEquals("listDirectory() should return expected content", paths, zosUNIXFileSpy.listDirectory("path1/", false));
//        
//        
//        paths.put("path2", TYPE_DIRECTORY);
//        paths.put("path3", TYPE_DIRECTORY);
//        paths.put("path4", TYPE_DIRECTORY);
//        
//        Assert.assertEquals("listDirectory() should return expected content", paths, zosUNIXFileSpy.listDirectory(UNIX_DIRECTORY, true));
//    }
//    
//    @Test
//    public void testListDirectoryNotDirectory() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        String expectedMessage = "Invalid request, 'path1' is not a directory";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.listDirectory("path1", false);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testListDirectoryZosmfException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = EXCEPTION;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.listDirectory("path1", false);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testListDirectoryZosmfResponseException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
//        String expectedMessage = "Unable to list UNIX path 'path1' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.listDirectory("path1", false);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testListDirectoryBadHttpResponseException() throws ZosUNIXFileException, ZosmfException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
//        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
//        String expectedMessage = ERROR;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.listDirectory("path1", false);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetPaths() throws ZosUNIXFileException {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("returnedRows", 0);
//        jsonObject.addProperty("totalRows", 0);    
//        Map<String, IZosUNIXFile> paths = new TreeMap<>();    
//        Map<String, IZosUNIXFile> result = new TreeMap<>();
//        PowerMockito.doReturn(paths).when(zosUNIXFileSpy).listDirectory(Mockito.any(), Mockito.anyBoolean());
//        
//        Assert.assertEquals("getPaths() should return expected content", result, zosUNIXFileSpy.getPaths("/root", jsonObject, false));
//        
//        JsonArray jsonArray = new JsonArray();
//        JsonObject items = new JsonObject();
//        items.addProperty("name", ".");
//        items.addProperty("mode", "drwxrwxrwx");
//        items.addProperty("size", 0);
//        items.addProperty("mtime", "2021-03-11T09:31:24");
//        items.addProperty("user", "USER");
//        items.addProperty("group", "GROUP");
//        jsonArray.add(items);
//        items = new JsonObject();
//        items.addProperty("name", "..");   
//        items.addProperty("mode", "drwxrwxrwx");
//        items.addProperty("size", 0);
//        items.addProperty("mtime", "2021-03-11T09:31:24");
//        items.addProperty("user", "USER");
//        items.addProperty("group", "GROUP");
//        jsonArray.add(items);
//        items = new JsonObject();
//        items.addProperty("name", "path1");   
//        items.addProperty("mode", "drwxrwxrwx");
//        items.addProperty("size", 0);
//        items.addProperty("mtime", "2021-03-11T09:31:24");
//        items.addProperty("user", "USER");
//        items.addProperty("group", "GROUP");
//        jsonArray.add(items);
//        items = new JsonObject();
//        items.addProperty("name", "file1");   
//        items.addProperty("mode", "-rwxrwxrwx");
//        items.addProperty("size", 0);
//        items.addProperty("mtime", "2021-03-11T09:31:24");
//        items.addProperty("user", "USER");
//        items.addProperty("group", "GROUP");
//        jsonArray.add(items);
//        jsonObject.add("items", jsonArray);
//        jsonObject.addProperty("returnedRows", 4);
//        jsonObject.addProperty("totalRows", 4);
//        
//        Mockito.when(zosUnixFileMockDirectory.getUnixPath()).thenReturn("/root/path1");
//        Mockito.when(zosUnixFileMockFile.getUnixPath()).thenReturn("/root/file1");
//        result.put(zosUnixFileMockDirectory.getUnixPath(), zosUnixFileMockDirectory);
//        result.put(zosUnixFileMockFile.getUnixPath(), zosUnixFileMockFile);
//        
//        for (Map.Entry<String, IZosUNIXFile> entry : zosUNIXFileSpy.getPaths("/root/", jsonObject, false).entrySet()) {
//        	Assert.assertEquals("getPaths() should return expected content", result.get(entry.getKey()).getUnixPath(), entry.getValue().getUnixPath());
//        }
//        
//        for (Map.Entry<String, IZosUNIXFile> entry : zosUNIXFileSpy.getPaths("/root/", jsonObject, true).entrySet()) {
//        	Assert.assertEquals("getPaths() should return expected content", result.get(entry.getKey()).getUnixPath(), entry.getValue().getUnixPath());
//        }
//        
//        jsonObject.addProperty("returnedRows", MAX_ROWS);
//        jsonObject.addProperty("totalRows", 9999);
//        String expectedMessage = "The number of files and directories (9999) in UNIX path '/root/' is greater than the maximum allowed rows (" + MAX_ROWS + ")";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.getPaths("/root/", jsonObject, true);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetters() throws ZosUNIXFileException {
//        Assert.assertEquals("getFileType() should return the expected value", UNIXFileType.FILE, zosUNIXFileSpy.getFileType());
//
//    	Assert.assertEquals("getDataType() should return the expected value", UNIXFileDataType.TEXT, zosUNIXFileSpy.getDataType());
//        zosUNIXFileSpy.setDataType(UNIXFileDataType.BINARY);
//        Assert.assertEquals("getDataType() should return the expected value", UNIXFileDataType.BINARY, zosUNIXFileSpy.getDataType());
//
//    	JsonObject responseBody = new JsonObject();
//        PowerMockito.doReturn(responseBody).when(zosUNIXFileSpy).getAttributes(Mockito.any());
//    	zosUNIXFileSpy.setAttributeValues(responseBody);
//
//        Assert.assertNull("getFilePermissions() should return the expected value", zosUNIXFileSpy.getFilePermissions());
//        responseBody.addProperty("mode", "-rwxrwxrwx");
//    	zosUNIXFileSpy.setAttributeValues(responseBody);
//        Assert.assertEquals("getFilePermissions() should return the expected value", ACCESS_PERMISSIONS, zosUNIXFileSpy.getFilePermissions());
//
//        Assert.assertEquals("getSize() should return the expected value", -1, zosUNIXFileSpy.getSize());
//        responseBody.addProperty("size", 99);
//    	zosUNIXFileSpy.setAttributeValues(responseBody);
//        Assert.assertEquals("getSize() should return the expected value", 99, zosUNIXFileSpy.getSize());
//
//        Assert.assertNull("getLastModified() should return the expected value", zosUNIXFileSpy.getLastModified());
//        responseBody.addProperty("mtime", "LAST-MODIFIED");
//    	zosUNIXFileSpy.setAttributeValues(responseBody);
//        Assert.assertEquals("getLastModifiedSize() should return the expected value", "LAST-MODIFIED", zosUNIXFileSpy.getLastModified());
//
//        Assert.assertNull("getUser() should return the expected value", zosUNIXFileSpy.getUser());
//        responseBody.addProperty("user", "USER");
//    	zosUNIXFileSpy.setAttributeValues(responseBody);
//        Assert.assertEquals("getUser() should return the expected value", "USER", zosUNIXFileSpy.getUser());
//
//        Assert.assertNull("getGroup() should return the expected value", zosUNIXFileSpy.getGroup());
//        responseBody.addProperty("group", "GROUP");
//    	zosUNIXFileSpy.setAttributeValues(responseBody);
//        Assert.assertEquals("getGroup() should return the expected value", "GROUP", zosUNIXFileSpy.getGroup());
//    }
//    
//    @Test
//    public void testStoreArtifact() throws ZosFileManagerException, IOException {
//        setupTestStoreArtifact();
//        
//        Assert.assertEquals("storeArtifact() should return the supplied mock value", "artifactPath", zosUNIXFileSpy.storeArtifact(RAS_PATH, CONTENT, true, "pathElement"));
//        
//        Assert.assertEquals("storeArtifact() should return the supplied mock value", "artifactPath", zosUNIXFileSpy.storeArtifact(RAS_PATH, CONTENT, false, "pathElement/output.file"));
//        
//        Assert.assertEquals("storeArtifact() should return the supplied mock value", "artifactPath", zosUNIXFileSpy.storeArtifact(RAS_PATH, CONTENT.getBytes(), false, "pathElement/output.file"));
//    }
//    
//    @Test
//    public void testStoreArtifactException1() throws ZosFileManagerException, IOException {
//        setupTestStoreArtifact();
//        String expectedMessage = "Unable to store artifact. Invalid content object type: " + Object.class.getName();
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeArtifact(RAS_PATH, new Object(), false, "pathElement.output.file");
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreArtifactException2() throws ZosFileManagerException, IOException {
//        FileSystemProvider fileSystemProviderMock = setupTestStoreArtifact();
//        Mockito.when(fileSystemProviderMock.newByteChannel(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new IOException());
//        String expectedMessage = "Unable to store artifact";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeArtifact(RAS_PATH, CONTENT, false, "pathElement.output.file");
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
// 
//    private FileSystemProvider setupTestStoreArtifact() throws IOException {
//        Path archivePathMock = Mockito.mock(Path.class);
//        Mockito.when(archivePathMock.toString()).thenReturn("artifactPath");
//        FileSystem fileSystemMock = Mockito.mock(FileSystem.class);
//        FileSystemProvider fileSystemProviderMock = Mockito.mock(FileSystemProvider.class);
//        OutputStream outputStreamMock = Mockito.mock(OutputStream.class);
//        Mockito.when(archivePathMock.resolve(Mockito.anyString())).thenReturn(archivePathMock);
//        Mockito.when(archivePathMock.getFileSystem()).thenReturn(fileSystemMock);
//        Mockito.when(fileSystemMock.provider()).thenReturn(fileSystemProviderMock);
//        SeekableByteChannel seekableByteChannelMock = Mockito.mock(SeekableByteChannel.class);
//        Mockito.when(fileSystemProviderMock.newByteChannel(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(seekableByteChannelMock);
//        Mockito.when(fileSystemProviderMock.newOutputStream(Mockito.any(Path.class), Mockito.any())).thenReturn(outputStreamMock);
//        Mockito.when(fileSystemMock.getPath(Mockito.anyString(), Mockito.any())).thenReturn(archivePathMock);
//        zosFileManagerMock.setUnixPathArtifactRoot(archivePathMock);
//        zosFileManagerMock.setCurrentTestMethodArchiveFolderName("testStoreArtifact");
//        Mockito.when(zosFileHandlerMock.getArtifactsRoot()).thenReturn(archivePathMock);
//        Whitebox.setInternalState(zosFileManagerMock, "currentTestMethodArchiveFolderName", "testStoreArtifact");
//        Mockito.when(zosFileManagerMock.getUnixPathArtifactRoot()).thenReturn(archivePathMock);
//        Mockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
//        
//        return fileSystemProviderMock;
//    }
//    @Test
//    public void testSplitUnixPath() {
//        Whitebox.setInternalState(zosUNIXFileSpy, "unixPath", UNIX_PATH);
//        Whitebox.setInternalState(zosUNIXFileSpy, "directoryPath", (String) null);
//        Whitebox.setInternalState(zosUNIXFileSpy, "fileType", (String) null);
//        
//        zosUNIXFileSpy.splitUnixPath();
//        Assert.assertEquals("splitUnixPath() should set the expected value", UNIX_FILE, Whitebox.getInternalState(zosUNIXFileSpy, "fileName"));
//        Assert.assertEquals("splitUnixPath() should set the expected value", UNIX_DIRECTORY_PATH, Whitebox.getInternalState(zosUNIXFileSpy, "directoryPath"));
//        Assert.assertEquals("splitUnixPath() should set the expected value", UNIXFileType.FILE, Whitebox.getInternalState(zosUNIXFileSpy, "fileType"));
//
//        Whitebox.setInternalState(zosUNIXFileSpy, "unixPath", UNIX_DIRECTORY);
//        Whitebox.setInternalState(zosUNIXFileSpy, "directoryPath", (String) null);
//        Whitebox.setInternalState(zosUNIXFileSpy, "fileType", (String) null);
//        
//        zosUNIXFileSpy.splitUnixPath();
//        Assert.assertEquals("splitUnixPath() should set the expected value", (String) null, Whitebox.getInternalState(zosUNIXFileSpy, "fileName"));
//        Assert.assertEquals("splitUnixPath() should set the expected value", UNIX_DIRECTORY, Whitebox.getInternalState(zosUNIXFileSpy, "directoryPath"));
//        Assert.assertEquals("splitUnixPath() should set the expected value", UNIXFileType.DIRECTORY, Whitebox.getInternalState(zosUNIXFileSpy, "fileType"));
//    }
//    
//    @Test
//    public void testEmptyStringWhenNull() {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty(PROPERTY, VALUE);
//        
//        Assert.assertEquals("emptyStringWhenNull() should return the expected value", "", zosUNIXFileSpy.emptyStringWhenNull(jsonObject, "xxxx"));
//        Assert.assertEquals("emptyStringWhenNull() should return the expected value", VALUE, zosUNIXFileSpy.emptyStringWhenNull(jsonObject, PROPERTY));
//    }
//    
//    @Test
//    public void testBuildErrorString() {
//        String expectedString = "Error action";
//        String returnString = zosUNIXFileSpy.buildErrorString("action", new JsonObject(), UNIX_PATH);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("returnedRows", 1);
//        JsonArray jsonArray = new JsonArray();
//        JsonObject items = new JsonObject();
//        items.addProperty("path", UNIX_PATH);
//        jsonArray.add(items);
//        jsonObject.add("items", jsonArray);
//        jsonObject.addProperty("category", 0);
//        jsonObject.addProperty("rc", 0);
//        jsonObject.addProperty("reason", 0);
//        jsonObject.addProperty("message", "message");
//        jsonObject.addProperty("id", 1);
//        zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
//        
//        jsonObject.addProperty("details", "details");
//        expectedString = "Error action UNIX path '" + UNIX_PATH + "', category:0, rc:0, reason:0, message:message\n" + 
//                "details:details";
//        returnString = zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        jsonObject.addProperty("stack", "stack");
//        zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
//        
//        jsonObject.addProperty("details", "details");
//        expectedString = "Error action UNIX path '" + UNIX_PATH + "', category:0, rc:0, reason:0, message:message\n" + 
//                "details:details\n" + 
//                "stack:\n" + 
//                "stack";
//        returnString = zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//        jsonObject.addProperty("details", 1);
//        zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
//        
//        jsonObject.remove("details");
//        jsonArray = new JsonArray();
//        JsonPrimitive item = new JsonPrimitive("details line 1");
//        jsonArray.add(item);
//        item = new JsonPrimitive("details line 2");
//        jsonArray.add(item);
//        jsonObject.add("details", jsonArray);
//        expectedString = "Error action UNIX path '" + UNIX_PATH + "', category:0, rc:0, reason:0, message:message\n" + 
//                "details:\n" +
//                "details line 1\n" +
//                "details line 2\n" + 
//                "stack:\n" + 
//                "stack";
//        returnString = zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
//        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
//        
//    }
//    
//    @Test
//    public void testCleanCreatedPath() throws ZosUNIXFileException {
//        zosUNIXFileSpy.cleanCreatedPath();        
//        Whitebox.setInternalState(zosUNIXFileSpy, "createdPath", UNIX_PATH);
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        zosUNIXFileSpy.cleanCreatedPath();
//        
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doNothing().when(zosUNIXFileSpy).cleanCreatedPathStore();
//        PowerMockito.doNothing().when(zosUNIXFileSpy).cleanCreatedDelete();
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).shouldArchive();
//        zosUNIXFileSpy.cleanCreatedPath();
//
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).shouldArchive();
//        zosUNIXFileSpy.cleanCreatedPath();
//        
//        Mockito.doThrow(new ZosUNIXFileException(EXCEPTION)).when(zosUNIXFileSpy).exists(Mockito.any());
//        zosUNIXFileSpy.cleanCreatedPath();
//        
//        Mockito.verify(zosUNIXFileSpy, Mockito.times(5)).cleanCreatedPath();
//    }
//    
//    @Test
//    public void testCleanCreatedPathStore() throws ZosUNIXFileException {
//        PowerMockito.doNothing().when(zosUNIXFileSpy).saveToResultsArchive(Mockito.any());
//        zosUNIXFileSpy.cleanCreatedPathStore();
//        
//        PowerMockito.doThrow(new ZosUNIXFileException(EXCEPTION)).when(zosUNIXFileSpy).saveToResultsArchive(Mockito.any());
//        zosUNIXFileSpy.cleanCreatedPathStore();
//        
//        Mockito.verify(zosUNIXFileSpy, Mockito.times(2)).cleanCreatedPathStore();
//    }
//    
//    @Test
//    public void testCleanCreatedDelete() throws ZosUNIXFileException {
//        PowerMockito.doNothing().when(zosUNIXFileSpy).delete(Mockito.any(), Mockito.anyBoolean());
//        zosUNIXFileSpy.cleanCreatedDelete();
//        
//        PowerMockito.doThrow(new ZosUNIXFileException(EXCEPTION)).when(zosUNIXFileSpy).delete(Mockito.any(), Mockito.anyBoolean());
//        zosUNIXFileSpy.cleanCreatedDelete();
//        
//        Mockito.verify(zosUNIXFileSpy, Mockito.times(2)).cleanCreatedDelete();
//    }
//    
//    @Test
//    public void testArchiveContent() throws ZosUNIXFileException {
//    	Mockito.doNothing().when(zosUNIXFileSpy).saveToResultsArchive(Mockito.any());
//    	Mockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
//    	Mockito.when(zosUNIXFileSpy.shouldArchive()).thenReturn(false);
//    	zosUNIXFileSpy.archiveContent();
//    	Mockito.verify(zosUNIXFileSpy, Mockito.times(0)).saveToResultsArchive(Mockito.any());
//    	
//    	Mockito.when(zosUNIXFileSpy.shouldArchive()).thenReturn(true);
//    	zosUNIXFileSpy.archiveContent();
//    	Mockito.verify(zosUNIXFileSpy, Mockito.times(1)).saveToResultsArchive(Mockito.any());
//    }
//    
//    @Test
//    public void testShouldCleanup() {
//    	zosUNIXFileSpy.setShouldCleanup(false);
//    	Assert.assertFalse("setShouldCleanup() should return false", zosUNIXFileSpy.shouldCleanup());
//    	zosUNIXFileSpy.setShouldCleanup(true);
//    	Assert.assertTrue("setShouldCleanup() should return true", zosUNIXFileSpy.shouldCleanup());
//    }
}
