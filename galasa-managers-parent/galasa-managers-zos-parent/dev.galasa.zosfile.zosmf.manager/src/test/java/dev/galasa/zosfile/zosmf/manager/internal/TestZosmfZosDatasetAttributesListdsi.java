/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.zosmf.manager.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;

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
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;
import dev.galasa.zosunixcommand.spi.IZosUNIXCommandSpi;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(FrameworkUtil.class)
public class TestZosmfZosDatasetAttributesListdsi {
//    
//    private ZosmfZosDatasetAttributesListdsi zosmfZosDatasetAttributesListdsi;
//    
//    private ZosmfZosDatasetAttributesListdsi zosDatasetAttributesListdsiSpy;
//
//    @Mock
//    private IZosImage zosImageMock;
//
//    @Mock
//    private IZosManagerSpi zosManagerMock;
//    
//    @Mock
//    private ZosmfZosFileManagerImpl zosFileManagerMock;
//    
//    @Mock
//    private IZosUNIXCommandSpi zosUNIXCommandSpiMock;
//    
//    @Mock
//    private IZosUNIXCommand zosUNIXCommandMock;
//    
//    @Mock
//    private ZosmfZosFileHandlerImpl zosFileHandlerMock;
//    
//    @Mock
//    private ZosmfZosDatasetImpl execDatasetMock;
//
//    private static final String DATASET_NAME = "DATA.SET.NAME";
//    
//    private static final String RUN_HLQ = "RUNHLQ";
//    
//    private static final String RUNID = "RUNID";
//    
//    @Before
//    public void setup() throws Exception {
//        Mockito.when(zosManagerMock.getRunDatasetHLQ(Mockito.any())).thenReturn(RUN_HLQ);
//        Mockito.when(zosFileManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        Mockito.when(zosFileManagerMock.getRunId()).thenReturn(RUNID);
//        Mockito.when(zosFileManagerMock.getZosUnixCommandManager()).thenReturn(zosUNIXCommandSpiMock);
//        
//        zosmfZosDatasetAttributesListdsi = new ZosmfZosDatasetAttributesListdsi(zosFileManagerMock, zosImageMock);
//        zosDatasetAttributesListdsiSpy = Mockito.spy(zosmfZosDatasetAttributesListdsi);
//    }
//    
//    @Test
//    public void testInitialise() throws ZosDatasetException {
//        PowerMockito.doNothing().when(zosDatasetAttributesListdsiSpy).createExecDataset();
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "zosFileManager", zosFileManagerMock);
//        zosDatasetAttributesListdsiSpy.initialise();
//        Assert.assertTrue("initialise() should set initialised to true", Whitebox.getInternalState(zosDatasetAttributesListdsiSpy, "initialised"));
//    }
//    
//    @Test
//    public void testInitialiseException1() throws ZosDatasetException {
//    	Mockito.when(zosFileManagerMock.getZosUnixCommandManager()).thenReturn(null);
//        String expectMessage = "Unable to create LISTDSI EXEC command";
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//            zosDatasetAttributesListdsiSpy.initialise();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGet() throws ZosDatasetException {
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "initialised", true);
//        JsonObject jsonObject = buildJsonObject(true); 
//        PowerMockito.doReturn(jsonObject).when(zosDatasetAttributesListdsiSpy).execListdsi(Mockito.anyString());
//        JsonObject jsonResult = new JsonObject();
//        Assert.assertEquals("get() should return the expected object", jsonResult, zosDatasetAttributesListdsiSpy.get(DATASET_NAME));
//        
//        jsonObject = buildJsonObject(false);        
//        PowerMockito.doReturn(jsonObject).when(zosDatasetAttributesListdsiSpy).execListdsi(Mockito.anyString());
//        jsonResult = buildJsonResponse();
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
//        String expectMessage = ("Invalid JSON object returend from LISTDSI:\n" + jsonObject);
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetAttributesListdsiSpy.get(DATASET_NAME);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectMessage, expectedException.getMessage());
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
//    public void testExecListdsi() throws ZosDatasetException, ZosUNIXCommandException {
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "unixCommand", zosUNIXCommandMock);
//        PowerMockito.when(zosUNIXCommandMock.issueCommand(Mockito.any())).thenReturn("RC=0");
//        JsonObject jsonObject = buildJsonObject(false);
//        Mockito.when(execDatasetMock.memberRetrieveAsText(Mockito.any())).thenReturn(jsonObject.toString());
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "execDataset", execDatasetMock );
//        Assert.assertEquals("get() should return the expected object", jsonObject, zosDatasetAttributesListdsiSpy.execListdsi(DATASET_NAME));
//    }
//
//    @Test
//    public void testExecListdsiException1() throws ZosUNIXCommandException, ZosDatasetException {
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "unixCommand", zosUNIXCommandMock);
//        PowerMockito.when(zosUNIXCommandMock.issueCommand(Mockito.any())).thenThrow(new ZosUNIXCommandException());
//        String expectMessage = ("Problem issuing zOS UNIX command");
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetAttributesListdsiSpy.execListdsi(DATASET_NAME);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testExecListdsiException2() throws ZosUNIXCommandException, ZosDatasetException {
//        Whitebox.setInternalState(zosDatasetAttributesListdsiSpy, "unixCommand", zosUNIXCommandMock);
//        PowerMockito.when(zosUNIXCommandMock.issueCommand(Mockito.any())).thenReturn("ERROR");
//        String expectMessage = ("Unable to get data set attibutes: ERROR");
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetAttributesListdsiSpy.execListdsi(DATASET_NAME);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectMessage, expectedException.getMessage());
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
//        String expectMessage = "Unable to get LISTDSI EXEC resource from Manager Bundle";
//
//        ZosDatasetException expectedException = Assert.assertThrows("expected exception should be thrown", ZosDatasetException.class, ()->{
//        	zosDatasetAttributesListdsiSpy.getExecResource();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectMessage, expectedException.getMessage());
//    }
//    
//    private JsonObject buildJsonObject(boolean empty) {
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
//    private JsonObject buildJsonResponse() {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("listdsirc",0);
//        jsonObject.addProperty("sysreason",0);
//        jsonObject.addProperty("sysmsglvl1","SYSMSGLVL1");
//        jsonObject.addProperty("sysmsglvl2","SYSMSGLVL2");
//        jsonObject.addProperty("dsname",DATASET_NAME);
//        jsonObject.addProperty("volser","VOLUME");
//        jsonObject.addProperty("unit","UNIT");
//        jsonObject.addProperty("dsorg","DSORG");
//        jsonObject.addProperty("alcunit","UNITS");
//        jsonObject.addProperty("cdate","2020/01/01");
//        jsonObject.addProperty("primary",7);
//        jsonObject.addProperty("secondary",8);
//        jsonObject.addProperty("dirblk",12);
//        jsonObject.addProperty("recfm","RECFM");
//        jsonObject.addProperty("blksize",2);
//        jsonObject.addProperty("lrecl",1);
//        jsonObject.addProperty("storeclass","STOCLASS");
//        jsonObject.addProperty("mgntclass","MGTCLASS");
//        jsonObject.addProperty("dataclass","DATACLASS");
//        jsonObject.addProperty("dsntype","TYPE");
//        jsonObject.addProperty("used",5);
//        jsonObject.addProperty("extx",9);
//        jsonObject.addProperty("edate","2020/01/03");
//        jsonObject.addProperty("rdate","2020/01/02");
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
