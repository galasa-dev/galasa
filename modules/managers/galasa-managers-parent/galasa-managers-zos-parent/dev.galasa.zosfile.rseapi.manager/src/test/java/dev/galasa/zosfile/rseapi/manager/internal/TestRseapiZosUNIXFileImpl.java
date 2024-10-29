/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.rseapi.manager.internal;

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

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileType;
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
public class TestRseapiZosUNIXFileImpl {
//    
//    private RseapiZosUNIXFileImpl zosUNIXFile;
//    
//    private RseapiZosUNIXFileImpl zosUNIXFileSpy;
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
//    private IZosUNIXFile zosUnixFileMockTilde;
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
//    private RseapiZosFileHandlerImpl zosFileHandlerMock;
//    
//    @Mock
//    private IRseapiResponse rseapiResponseMock;
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
//    private static final String UNIX_TILDE_FILE = UNIX_PATH + "~" + UNIX_FILE;    
//    
//    private static final String TYPE_FILE = "FILE";
//    
//    private static final String TYPE_DIRECTORY = "DIRECTORY";
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
//		Mockito.when(zosUnixFileMockTilde.getFileType()).thenReturn(UNIXFileType.FILE);
//		
//		Mockito.when(zosUnixFileMockDirectory.getUnixPath()).thenReturn(UNIX_DIRECTORY);
//		Mockito.when(zosUnixFileMockFile.getUnixPath()).thenReturn(UNIX_PATH);
//		Mockito.when(zosUnixFileMockFile2.getUnixPath()).thenReturn(UNIX_PATH2);
//		Mockito.when(zosUnixFileMockUnknown.getUnixPath()).thenReturn(UNIX_UNKNOWN);		
//		Mockito.when(zosUnixFileMockTilde.getUnixPath()).thenReturn(UNIX_TILDE_FILE);
//		
//		Mockito.when(zosUnixFileMockFile.getFileName()).thenReturn(UNIX_FILE);
//		Mockito.when(zosUnixFileMockFile2.getFileName()).thenReturn(UNIX_FILE);
//		Mockito.when(zosUnixFileMockUnknown.getFileName()).thenReturn(UNIX_PATH);		
//		Mockito.when(zosUnixFileMockTilde.getFileName()).thenReturn(UNIX_PATH);
//		
//		Mockito.when(zosUnixFileMockDirectory.toString()).thenReturn(UNIX_DIRECTORY);
//		Mockito.when(zosUnixFileMockFile.toString()).thenReturn(UNIX_PATH);
//		Mockito.when(zosUnixFileMockFile2.toString()).thenReturn(UNIX_PATH2);
//		Mockito.when(zosUnixFileMockUnknown.toString()).thenReturn("unknown");
//		Mockito.when(zosUnixFileMockTilde.toString()).thenReturn(UNIX_TILDE_FILE);
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
//        PowerMockito.doReturn(rseapiApiProcessorMock).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//        Mockito.when(zosFileManagerMock.getRseapiManager()).thenReturn(rseapiManagerMock);
//        Mockito.when(zosFileHandlerMock.getZosFileManager()).thenReturn(zosFileManagerMock);
//        Mockito.when(zosFileHandlerMock.getZosManager()).thenReturn(zosManagerMock);
//
//    	Path pathMock = Mockito.mock(Path.class);
//    	Mockito.doReturn(pathMock).when(pathMock).resolve(Mockito.anyString());
//    	Mockito.doReturn("PATH_NAME").when(pathMock).toString();
//    	Mockito.doReturn(pathMock).when(zosFileManagerMock).getUnixPathArtifactRoot();
//        Mockito.when(zosFileManagerMock.getUnixPathCurrentTestMethodArchiveFolder()).thenReturn(pathMock);
//        
//        zosUNIXFile = new RseapiZosUNIXFileImpl(zosFileHandlerMock, zosImageMock, UNIX_PATH);
//        zosUNIXFileSpy = Mockito.spy(zosUNIXFile);
//    }
//    
//    @Test
//    public void testConstructor() throws RseapiManagerException, ZosFileManagerException {
//    	Assert.assertEquals("getZosFileHandler() should return the expected object", zosFileHandlerMock, zosUNIXFileSpy.getZosFileHandler());
//        String expectedMessage = "UNIX path must be absolute not be relative";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	new RseapiZosUNIXFileImpl(zosFileHandlerMock, zosImageMock, "PATH");
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testConstructorException1() throws RseapiManagerException, ZosFileManagerException {
//        Mockito.doThrow(new RseapiManagerException(EXCEPTION)).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	new RseapiZosUNIXFileImpl(zosFileHandlerMock, zosImageMock, UNIX_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testCreate() throws ZosUNIXFileException, RseapiException {
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
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
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
//    public void testStoreText() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        zosUNIXFileSpy.setDataType(UNIXFileDataType.TEXT);
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        zosUNIXFileSpy.storeText(CONTENT);
//        Assert.assertEquals("store() should log expected message", "UNIX path '" + UNIX_DIRECTORY_PATH + "' updated on image " + IMAGE, logMessage);
//
//        zosUNIXFileSpy.setDataType(UNIXFileDataType.BINARY);
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        zosUNIXFileSpy.storeText(CONTENT);
//        Assert.assertEquals("store() should log expected message", "UNIX path '" + UNIX_DIRECTORY_PATH + "' updated on image " + IMAGE, logMessage);
//    }
//    
//    @Test
//    public void testStoreTextBadHttpresponseException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        zosUNIXFileSpy.setDataType(UNIXFileDataType.TEXT);
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenCallRealMethod();
//        String expectedMessage = "Error writing to '" + UNIX_PATH + "', HTTP Status Code 404 : NOT_FOUND";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeText(CONTENT);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreTextNotExistException() throws ZosUNIXFileException {
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists();
//        String expectedMessage = "UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeText(CONTENT);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
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
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreTextRseapiException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeText(CONTENT);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testStoreTextBadHttpResponseException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenCallRealMethod();
//        String expectedMessage = "Error writing to '" + UNIX_PATH + "', HTTP Status Code " + HttpStatus.SC_NOT_FOUND + " : NOT_FOUND";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeText(CONTENT);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreBinary() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        zosUNIXFileSpy.setDataType(UNIXFileDataType.TEXT);
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        Assert.assertEquals("store() should log expected message", "UNIX path '" + UNIX_DIRECTORY_PATH + "' updated on image " + IMAGE, logMessage);
//
//        zosUNIXFileSpy.setDataType(UNIXFileDataType.BINARY);
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        Assert.assertEquals("store() should log expected message", "UNIX path '" + UNIX_DIRECTORY_PATH + "' updated on image " + IMAGE, logMessage);
//    }
//    
//    @Test
//    public void testStoreBinaryBadHttpresponseException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        zosUNIXFileSpy.setDataType(UNIXFileDataType.TEXT);
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenCallRealMethod();
//        String expectedMessage = "Error writing to '" + UNIX_PATH + "', HTTP Status Code 404 : NOT_FOUND";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreBinaryNotExistException() throws ZosUNIXFileException {
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists();
//        String expectedMessage = "UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
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
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreBinaryRseapiException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testStoreBinaryBadHttpResponseException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenCallRealMethod();
//        String expectedMessage = "Error writing to '" + UNIX_PATH + "', HTTP Status Code " + HttpStatus.SC_NOT_FOUND + " : NOT_FOUND";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeBinary(CONTENT.getBytes());
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsText() throws ZosUNIXFileException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(CONTENT).when(zosUNIXFileSpy).retrieve(Mockito.any());
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
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
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
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testRetrieveAsBinary() throws ZosUNIXFileException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosUNIXFileSpy).retrieve(Mockito.any());
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
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
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
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testOneLineMethods() throws ZosUNIXFileException {
//		zosUNIXFileSpy.setShouldArchive(true);
//
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
//        PowerMockito.doReturn(MODE).when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
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
//    public void testSetAccessPermissions() throws RseapiException, ZosUNIXFileException {
//    	Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject requestBody =  new JsonObject();
//        requestBody.addProperty("exit code", 0);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(requestBody);
//    	zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, false);
//    	
//        JsonObject output = new JsonObject();
//        requestBody.add("output", output);
//    	zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, true);
//
//        output.addProperty("stderr", "");
//    	zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, true);
//
//        output.addProperty("stderr", "stderr 1");
//    	zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, true);
//    	
//    	output.addProperty("stderr", "\nstderr 1");
//    	zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, true);
//    	
//    	output.addProperty("stderr", "\nstderr 1\nstderr 2");
//    	zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, true);
//
//    	output.addProperty("stderr", "EDC5139I");
//        String expectedMessage = "Unable to change file access permissions of UNIX path '" + UNIX_PATH + "'.\ndetails:\nEDC5139I";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, true);
//        });
//        Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);        
//        expectedMessage = "Unable to set zOS UNIX file access permissions of " + UNIX_PATH;
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.setAccessPermissions(ACCESS_PERMISSIONS, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
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
//        responseBody.addProperty("permissionsSymbolic", "-rwxrwxrwx");
//    	zosUNIXFileSpy.setAttributeValues(responseBody);
//        Assert.assertEquals("getFilePermissions() should return the expected value", ACCESS_PERMISSIONS, zosUNIXFileSpy.getFilePermissions());
//
//        Assert.assertEquals("getSize() should return the expected value", -1, zosUNIXFileSpy.getSize());
//        responseBody.addProperty("size", 99);
//    	zosUNIXFileSpy.setAttributeValues(responseBody);
//        Assert.assertEquals("getSize() should return the expected value", 99, zosUNIXFileSpy.getSize());
//
//        Assert.assertNull("getLastModified() should return the expected value", zosUNIXFileSpy.getLastModified());
//        responseBody.addProperty("lastModified", "LAST-MODIFIED");
//    	zosUNIXFileSpy.setAttributeValues(responseBody);
//        Assert.assertEquals("getLastModifiedSize() should return the expected value", "LAST-MODIFIED", zosUNIXFileSpy.getLastModified());
//
//        Assert.assertNull("getUser() should return the expected value", zosUNIXFileSpy.getUser());
//        responseBody.addProperty("fileOwner", "USER");
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
//    public void testGetAttributesAsString() throws ZosUNIXFileException, RseapiException {
//        
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject requestBody = new JsonObject();
//        requestBody.addProperty("name", "name");
//        requestBody.addProperty("type", TYPE_FILE);    
//        requestBody.addProperty("permissionsSymbolic", "-rwxrwxrwx");
//        requestBody.addProperty("size", 0);    
//        requestBody.addProperty("fileOwner", "fileOwner");     
//        requestBody.addProperty("group", "group");    
//        requestBody.addProperty("lastModified", "lastModified");     
//        requestBody.addProperty("encoding", "encoding");
//
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(requestBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Answer<?> emptyStringWhenNullValue = new Answer<String>() {
//            @Override
//            public String answer(InvocationOnMock invocation) throws Throwable {
//            	if (invocation.getArgument(1).equals("type") ||
//            		invocation.getArgument(1).equals("permissionsSymbolic") || 
//                	invocation.getArgument(1).equals("size")) {
//            		return (String) invocation.callRealMethod();
//            	}
//                return invocation.getArgument(1);
//            }
//        };
//        PowerMockito.doReturn(TYPE_FILE).when(zosUNIXFileSpy).emptyStringWhenNull(Mockito.any(), Mockito.eq(TYPE_FILE));
//        PowerMockito.doReturn(TYPE_DIRECTORY).when(zosUNIXFileSpy).emptyStringWhenNull(Mockito.any(), Mockito.eq(TYPE_DIRECTORY));
//        PowerMockito.doAnswer(emptyStringWhenNullValue).when(zosUNIXFileSpy).emptyStringWhenNull(Mockito.any(), Mockito.any());
//        String returnedValue = "Name=" + UNIX_FILE + ",Type=" + TYPE_FILE + ",Mode=-rwxrwxrwx,Size=0,User=fileOwner,Group=group,Modified=lastModified,Encoding=encoding";
//        Assert.assertEquals("getAttributesAsString() should return the expected value", returnedValue, zosUNIXFileSpy.getAttributesAsString(UNIX_FILE));
//
//        requestBody.addProperty("type", TYPE_DIRECTORY);
//        returnedValue = "Name=" + UNIX_PATH + "/,Type=" + TYPE_DIRECTORY + ",IsEmpty=true,Mode=-rwxrwxrwx,Size=0,User=fileOwner,Group=group,Modified=lastModified,Encoding=encoding";
//        Assert.assertEquals("getAttributesAsString() should return the expected value", returnedValue, zosUNIXFileSpy.getAttributesAsString(UNIX_PATH + "/"));
//
//        JsonArray children = new JsonArray();
//        requestBody.add("children", children);
//        returnedValue = "Name=" + UNIX_PATH + "/,Type=" + TYPE_DIRECTORY + ",IsEmpty=false,Mode=-rwxrwxrwx,Size=0,User=fileOwner,Group=group,Modified=lastModified,Encoding=encoding";
//        Assert.assertEquals("getAttributesAsString() should return the expected value", returnedValue, zosUNIXFileSpy.getAttributesAsString(UNIX_PATH + "/"));
//    }
//    
//    @Test
//    public void testGetAttributesAsStringNotExistException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        
//        String expectedMessage = "UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttributesAsStringRseapiException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testGetAttributesAsStringRseapiResponseException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//        
//        String expectedMessage = "Unable to list UNIX path '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetAttributesAsStringBadHttpResponseException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenReturn(ERROR);
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testCreatePath() throws ZosUNIXFileException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
//        
//        Assert.assertTrue("createPath() should return true", zosUNIXFileSpy.createPath(UNIX_PATH, UNIXFileType.FILE, ACCESS_PERMISSIONS));
//      
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.createPath(UNIX_PATH, UNIXFileType.FILE, ACCESS_PERMISSIONS);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testCreatePathBadHttpResponseException() throws ZosUNIXFileException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenReturn(ERROR);
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.createPath(UNIX_PATH, UNIXFileType.FILE, ACCESS_PERMISSIONS);
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalDelete() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn("Type=" + TYPE_FILE).when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);        
//        zosUNIXFileSpy.delete(UNIX_FILE, false);
//
//        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn("Type=" + TYPE_DIRECTORY + "IsEmpty=true").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
//        zosUNIXFileSpy.delete(UNIX_DIRECTORY, true);
//        Assert.assertTrue("delete() should set deleted to true", Whitebox.getInternalState(zosUNIXFileSpy, "deleted"));
//        
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        zosUNIXFileSpy.delete(UNIX_FILE, false);
//        Assert.assertFalse("delete() should set deleted to false", Whitebox.getInternalState(zosUNIXFileSpy, "deleted"));
//    }
//    
//    @Test
//    public void testInternalDeletenotExistException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        String expectedMessage = "UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.delete(UNIX_PATH, false);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalDeleteNotDirectory() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn("Type=FILE").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
//        String expectedMessage = "Invalid request, UNIX path '" + UNIX_FILE + "' is not a directory";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.delete(UNIX_FILE, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalDeleteNotEmptyDirectory() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn("Type=DIRECTORY,IsEmpty=false").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
//        String expectedMessage = "Invalid request, UNIX path '" + UNIX_FILE + "' is a directory and is not empty. Use the directoryDeleteNonEmpty() method";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.delete(UNIX_FILE, false);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalDeleteRseapiException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn("Type=FILE").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.delete(UNIX_FILE, false);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalDeleteBadHttpresponseException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn("Type=FILE").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenReturn(ERROR);
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.delete(UNIX_FILE, false);
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalExists() throws ZosUNIXFileException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Assert.assertTrue("exists() should set deleted to true", zosUNIXFileSpy.exists(UNIX_DIRECTORY));
//
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Assert.assertFalse("exists() should set deleted to false", zosUNIXFileSpy.exists(UNIX_DIRECTORY + "/"));
//    }
//    
//    @Test
//    public void testInternalRseapiExistsException() throws ZosUNIXFileException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.exists(UNIX_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalExistsException() throws ZosUNIXFileException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_ACCEPTABLE);
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenReturn(ERROR);
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.exists(UNIX_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieve() throws ZosUNIXFileException, RseapiException, IOException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
//        PowerMockito.doReturn(UNIXFileDataType.TEXT).when(zosUNIXFileSpy).getDataType();
//        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        JsonObject responseBody = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Assert.assertEquals("retrieve() should return the expected value", "", zosUNIXFileSpy.retrieve(UNIX_PATH));
//        
//        responseBody.addProperty(CONTENT, CONTENT);
//        Assert.assertEquals("retrieve() should return the expected value", CONTENT, zosUNIXFileSpy.retrieve(UNIX_PATH));
//        
//        Mockito.when(rseapiResponseMock.getContent()).thenReturn(IOUtils.toInputStream(CONTENT, StandardCharsets.UTF_8));
//        PowerMockito.doReturn(UNIXFileDataType.BINARY).when(zosUNIXFileSpy).getDataType();
//        Assert.assertEquals("retrieve() should return the expected value", CONTENT, new String((byte[]) zosUNIXFileSpy.retrieve(UNIX_PATH), StandardCharsets.UTF_8));
//    }
//    
//    @Test
//    public void testInternalRetrieveRseapiException() throws ZosUNIXFileException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieve(UNIX_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveGetContentException() throws ZosUNIXFileException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenReturn(ERROR);
//        String expectedMessage = "Unable to retrieve content of '" + UNIX_PATH + "' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieve(UNIX_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveRseapiBadHttpResponseException() throws ZosUNIXFileException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenReturn(ERROR);
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieve(UNIX_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testInternalRetrieveGetJsonContentException() throws ZosUNIXFileException, RseapiException {
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenCallRealMethod();
//        String expectedMessage = "Error retrieve content '" + UNIX_PATH + "', HTTP Status Code 404 : NOT_FOUND";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.retrieve(UNIX_PATH);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSaveToResultsArchive() throws ZosUNIXFileException {  	
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        PowerMockito.doReturn(CONTENT.getBytes()).when(zosUNIXFileSpy).retrieve(Mockito.any());
//        PowerMockito.doReturn(RAS_PATH).when(zosUNIXFileSpy).storeArtifact(Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any());
//        Whitebox.setInternalState(zosUNIXFileSpy, "dataType", UNIXFileDataType.BINARY);
//        zosUNIXFileSpy.saveToResultsArchive(RAS_PATH);
//        Assert.assertEquals("saveToResultsArchive() should log expected message", "'" + UNIX_PATH + "' archived to " + RAS_PATH, logMessage);
//
//        PowerMockito.doReturn(CONTENT).when(zosUNIXFileSpy).retrieve(Mockito.any());
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
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(UNIX_TILDE_FILE);
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(UNIX_UNKNOWN);
//        SortedMap<String, IZosUNIXFile> paths = new TreeMap<>();
//        paths.put(UNIX_DIRECTORY, zosUnixFileMockDirectory);
//        paths.put(UNIX_PATH, zosUnixFileMockFile);
//        paths.put(UNIX_TILDE_FILE, zosUnixFileMockTilde);
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
//        PowerMockito.doReturn("Type=FILE").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
//        
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_DIRECTORY));
//
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_PATH));
//
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_PATH));
//    }
//    
//    @Test
//    public void testListDirectory() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject );
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
//    public void testListDirectoryNotDirectory() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        String expectedMessage = "Invalid request, 'path1' is not a directory";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.listDirectory("path1", false);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testListDirectoryRseapiException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.listDirectory("path1", false);
//        });
//    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testListDirectoryRseapiResponseException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//        String expectedMessage = "Unable to list UNIX path 'path1' on image " + IMAGE;
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.listDirectory("path1", false);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testListDirectoryBadHttpResponseException() throws ZosUNIXFileException, RseapiException {
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        JsonObject jsonObject = new JsonObject();
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(zosFileHandlerMock.buildErrorString(Mockito.any(), Mockito.any())).thenReturn(ERROR);
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.listDirectory("path1", false);
//        });
//    	Assert.assertEquals("exception should contain expected message", ERROR, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetPaths() throws ZosUNIXFileException {
//    	JsonObject requestBody = new JsonObject();
//        Map<String, IZosUNIXFile> expectedResult = new TreeMap<>();        
//        Assert.assertEquals("getPaths() should return expected content", expectedResult, zosUNIXFileSpy.getPaths("/root", requestBody, false));
//        
//        IZosUNIXFile unixFileMock = Mockito.mock(IZosUNIXFile.class);
//        Mockito.when(unixFileMock.isDirectory()).thenReturn(false);
//        Mockito.when(unixFileMock.toString()).thenReturn(TYPE_FILE);
//        Mockito.when(unixFileMock.getFileType()).thenReturn(UNIXFileType.FILE);
//        PowerMockito.doReturn(unixFileMock).when(zosUNIXFileSpy).newUnixFile(Mockito.any());
//        JsonArray children = new JsonArray();
//        JsonObject fileChild = new JsonObject();
//        fileChild.addProperty("name", "file");
//        fileChild.addProperty("type", TYPE_FILE);
//        children.add(fileChild);
//        requestBody.add("children", children);
//        expectedResult.put("/root/file", unixFileMock);        
//        Map<String, IZosUNIXFile> result = zosUNIXFileSpy.getPaths("/root/", requestBody, false);
//        Assert.assertEquals("getPaths() should return expected content", expectedResult, result);
//
//        IZosUNIXFile unixDirectoryMock = Mockito.mock(IZosUNIXFile.class);
//        Mockito.when(unixDirectoryMock.isDirectory()).thenReturn(true);
//        Mockito.when(unixDirectoryMock.toString()).thenReturn(TYPE_DIRECTORY);
//        Mockito.when(unixDirectoryMock.getFileType()).thenReturn(UNIXFileType.DIRECTORY);
//        PowerMockito.doReturn(unixFileMock).doReturn(unixDirectoryMock).when(zosUNIXFileSpy).newUnixFile(Mockito.any());
//        JsonObject fileDirectory = new JsonObject();
//        fileDirectory.addProperty("name", "directory");
//        fileDirectory.addProperty("type", TYPE_DIRECTORY);
//        children.add(fileDirectory);
//        expectedResult.put("/root/directory", unixDirectoryMock);
//        PowerMockito.doReturn(new TreeMap<>()).when(zosUNIXFileSpy).listDirectory(Mockito.any(), Mockito.anyBoolean());
//        result = zosUNIXFileSpy.getPaths("/root/", requestBody, false);
//        Assert.assertEquals("getPaths() should return expected content", expectedResult, result);
//
//        PowerMockito.doReturn(unixFileMock).doReturn(unixDirectoryMock).when(zosUNIXFileSpy).newUnixFile(Mockito.any());
//        result = zosUNIXFileSpy.getPaths("/root/", requestBody, true);
//        Assert.assertEquals("getPaths() should return expected content", expectedResult, result);
//
//        JsonObject thisDirectory = new JsonObject();
//        thisDirectory.addProperty("name", "/.");
//        thisDirectory.addProperty("type", TYPE_DIRECTORY);
//        children.add(thisDirectory);
//        JsonObject parentDirectory = new JsonObject();
//        parentDirectory.addProperty("name", "/..");
//        parentDirectory.addProperty("type", TYPE_DIRECTORY);
//        children.add(parentDirectory);
//        Assert.assertEquals("getPaths() should return expected content", expectedResult, result);
//    }
//    
//    @Test
//    public void testDetermineType() {
//    	Assert.assertEquals("determineType() should return the expected value", UNIXFileType.FILE, zosUNIXFileSpy.determineType("-rwxrwxrwx"));
//    	Assert.assertEquals("determineType() should return the expected value", UNIXFileType.CHARACTER, zosUNIXFileSpy.determineType("crwxrwxrwx"));
//    	Assert.assertEquals("determineType() should return the expected value", UNIXFileType.DIRECTORY, zosUNIXFileSpy.determineType("drwxrwxrwx"));
//    	Assert.assertEquals("determineType() should return the expected value", UNIXFileType.EXTLINK, zosUNIXFileSpy.determineType("erwxrwxrwx"));
//    	Assert.assertEquals("determineType() should return the expected value", UNIXFileType.SYMBLINK, zosUNIXFileSpy.determineType("lrwxrwxrwx"));
//    	Assert.assertEquals("determineType() should return the expected value", UNIXFileType.FIFO, zosUNIXFileSpy.determineType("prwxrwxrwx"));
//    	Assert.assertEquals("determineType() should return the expected value", UNIXFileType.SOCKET, zosUNIXFileSpy.determineType("srwxrwxrwx"));
//    	Assert.assertEquals("determineType() should return the expected value", UNIXFileType.UNKNOWN, zosUNIXFileSpy.determineType("?rwxrwxrwx"));
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
//        String expectedMessage = "Unable to store artifact. Invalid content object type: java.lang.Object";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeArtifact(RAS_PATH, new Object(), false, "pathElement/output.file");
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testStoreArtifactException2() throws ZosFileManagerException, IOException {
//        FileSystemProvider fileSystemProviderMock = setupTestStoreArtifact();
//        Mockito.when(fileSystemProviderMock.newByteChannel(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new IOException());
//        String expectedMessage = "Unable to store artifact";
//        ZosUNIXFileException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXFileException.class, ()->{
//        	zosUNIXFileSpy.storeArtifact(RAS_PATH, CONTENT, false, "pathElement/output.file");
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
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
//        Mockito.when(zosFileManagerMock.getUnixPathArtifactRoot()).thenReturn(archivePathMock);
//        Mockito.when(zosFileHandlerMock.getArtifactsRoot()).thenReturn(archivePathMock);
//        Mockito.when(zosFileManagerMock.getCurrentTestMethodArchiveFolderName()).thenReturn("testStoreArtifact");
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
//        Whitebox.setInternalState(zosUNIXFileSpy, "fileType", (UNIXFileType) null);
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
//    public void testCleanCreatedPath() throws ZosUNIXFileException {
//        zosUNIXFileSpy.cleanCreatedPath();        
//        Whitebox.setInternalState(zosUNIXFileSpy, "createdPath", UNIX_PATH);
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//
//        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
//        zosUNIXFileSpy.cleanCreatedPath();
//        
//        zosUNIXFileSpy.setShouldArchive(false);
//        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
//        PowerMockito.doNothing().when(zosUNIXFileSpy).cleanCreatedPathStore();
//        PowerMockito.doNothing().when(zosUNIXFileSpy).cleanCreatedDelete();
//        zosUNIXFileSpy.cleanCreatedPath();
//
//        zosUNIXFileSpy.setShouldArchive(true);
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
