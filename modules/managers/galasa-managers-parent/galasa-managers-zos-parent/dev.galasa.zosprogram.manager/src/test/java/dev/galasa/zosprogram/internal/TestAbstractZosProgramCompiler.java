/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramException;
import dev.galasa.zosprogram.ZosProgramManagerException;

//@RunWith(PowerMockRunner.class)
public class TestAbstractZosProgramCompiler {
//    
//    private AbstractZosProgramCompiler abstractZosProgramCompiler;
//    
//    private AbstractZosProgramCompiler abstractZosProgramCompilerSpy;
//    
//    @Mock
//    private ZosProgramManagerImpl zosProgramManagerMock;
//
//	@Mock
//    private IBundleResources bundleResourcesMock;
//    
//    @Mock
//    private IZosBatchSpi zosBatchSpiMock;
//    
//    @Mock
//    private IZosBatch zosBatchMock;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    @Mock
//    private IZosManagerSpi zosManagerMock;
//
//    @Mock
//    private IZosBatchJob zosBatchJobMock;
//
//    @Mock
//    private ZosProgramImpl zosProgramMock;
//
//    @Mock
//    private IZosDataset loadlibMock;
//
//	private static final String NAME = "NAME";
//
//    private static final String SKEL = "SKEL";
//
//    private static final String JCL = "JCL";
//
//    private static final String EXCEPTION = "EXCEPTION";
//
//    private static final String JOB_RETCODE = "CC 0000";
//
//    private static final String JOBNAME = "JOBNAME";
//
//    private static final String JOBID = "JOBID";
//
//    private static final Language LANGUAGE = Language.COBOL;
//
//    private static final String LOG_FOR_FIELD = " -++- ";
//
//	private static final String JOBNAME_JOBID = JOBNAME + "(" + JOBID + ")";
//
//    @Before
//    public void setup() throws Exception {
//        Mockito.when(zosProgramMock.getLoadlib()).thenReturn(loadlibMock);
//        Mockito.when(zosProgramMock.getZosProgramManager()).thenReturn(zosProgramManagerMock);
//        Mockito.when(zosProgramManagerMock.getZosBatch()).thenReturn(zosBatchSpiMock);
//        Mockito.when(zosProgramManagerMock.getZosBatchForImage(Mockito.any())).thenReturn(zosBatchMock);
//        abstractZosProgramCompiler = new AbstractZosProgramCompiler(zosProgramMock);
//        abstractZosProgramCompilerSpy = Mockito.spy(abstractZosProgramCompiler);
//    }
//    
//    @Test
//    public void testConstructor() throws ZosProgramManagerException {
//        Mockito.when(zosProgramMock.getLoadlib()).thenReturn(null);
//        Mockito.when(zosProgramManagerMock.getRunLoadlib(Mockito.any())).thenReturn(loadlibMock);
//        new AbstractZosProgramCompiler(zosProgramMock);
//        
//        Mockito.when(zosProgramManagerMock.getRunLoadlib(Mockito.any())).thenThrow(new ZosProgramManagerException(EXCEPTION));
//        ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
//        	new AbstractZosProgramCompiler(zosProgramMock);
//        });
//    	Assert.assertEquals("exception should contain expected cause", EXCEPTION, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testCompile() throws ZosProgramException {
//        Mockito.doReturn("DUMMY").when(abstractZosProgramCompilerSpy).buildCompileJcl();
//        Mockito.doNothing().when(abstractZosProgramCompilerSpy).submitCompileJob(Mockito.any());
//        abstractZosProgramCompilerSpy.compile();
//
//        Mockito.doThrow(new ZosProgramException(EXCEPTION)).when(abstractZosProgramCompilerSpy).submitCompileJob(Mockito.any());
//        ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
//        	abstractZosProgramCompilerSpy.compile();
//        });
//    	Assert.assertEquals("exception should contain expected cause", EXCEPTION, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testBuildCompileJcl() throws ZosProgramException, IOException, TestBundleResourceException {
//    	Mockito.when(zosProgramManagerMock.getManagerBundleResources()).thenReturn(bundleResourcesMock);
//        InputStream inputStreamMock = Mockito.mock(InputStream.class);
//        Mockito.when(bundleResourcesMock.retrieveSkeletonFile(Mockito.any(), Mockito.any())).thenReturn(inputStreamMock);
//        Mockito.when(bundleResourcesMock.streamAsString(Mockito.any())).thenReturn(SKEL);
//        
//        Assert.assertEquals("Error in buildCompileJcl() method", SKEL, abstractZosProgramCompilerSpy.buildCompileJcl());
//
//        Mockito.when(bundleResourcesMock.streamAsString(Mockito.any())).thenThrow(new IOException());
//        String expectedMessage = "Problem loading JCL skeleton";
//        ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
//        	abstractZosProgramCompilerSpy.buildCompileJcl();
//        });
//    	Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSubmitCompileJob() throws ZosBatchException, ZosProgramException {
//        setupSubmitCompileJob(false);
//        Mockito.doCallRealMethod().when(zosProgramMock).setCompileJob(Mockito.any());
//        Mockito.doCallRealMethod().when(zosProgramMock).getCompileJob();
//        abstractZosProgramCompilerSpy.submitCompileJob(JCL);
//        Assert.assertEquals("Error in submitCompileJob() method", zosBatchJobMock, abstractZosProgramCompilerSpy.zosProgram.getCompileJob());
//    }
//    
//    @Test
//    public void testSubmitCompileJobException1() throws ZosBatchException, ZosProgramException {
//        setupSubmitCompileJob(true);
//        String expectedMessage = "Problem submitting compile job for " + LANGUAGE + " program " + NAME + LOG_FOR_FIELD;
//        ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
//        	abstractZosProgramCompilerSpy.submitCompileJob(JCL);
//        });
//    	Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSubmitCompileJobException2() throws ZosProgramException, ZosBatchException {
//        setupSubmitCompileJob(false);
//        Mockito.doThrow(new ZosBatchException()).when(zosBatchJobMock).waitForJob();
//        String expectedMessage = "Problem waiting for compile job for " + LANGUAGE + " program " + NAME + LOG_FOR_FIELD + ". " + JOBNAME_JOBID;
//        ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
//        	abstractZosProgramCompilerSpy.submitCompileJob(JCL);
//        });
//    	Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSubmitCompileJobException3() throws ZosProgramException, ZosBatchException {
//        setupSubmitCompileJob(false);
//        Mockito.doThrow(new ZosBatchException()).when(zosBatchJobMock).purge();
//        String expectedMessage = "Problem saving compile job output for " + LANGUAGE + " program " + NAME + LOG_FOR_FIELD + ". " + JOBNAME_JOBID;
//        ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
//        	abstractZosProgramCompilerSpy.submitCompileJob(JCL);
//        });
//    	Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSubmitCompileJobException4() throws ZosProgramException, ZosBatchException {
//        setupSubmitCompileJob(false);
//        Mockito.when(zosBatchJobMock.waitForJob()).thenReturn(9);
//        String expectedMessage = "Compile job for " + LANGUAGE + " program " + NAME + LOG_FOR_FIELD + " failed: " + JOB_RETCODE + ". " + JOBNAME_JOBID;
//        ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
//        	abstractZosProgramCompilerSpy.submitCompileJob(JCL);
//        });
//    	Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSubmitCompileJobException5() throws ZosProgramException, ZosBatchException {
//        setupSubmitCompileJob(false);
//        Mockito.when(zosBatchJobMock.waitForJob()).thenReturn(-1);
//        String expectedMessage = "Compile job for " + LANGUAGE + " program " + NAME + LOG_FOR_FIELD + " failed: " + JOB_RETCODE + ". " + JOBNAME_JOBID;
//        ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
//        	abstractZosProgramCompilerSpy.submitCompileJob(JCL);
//        });
//    	Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//
//    private void setupSubmitCompileJob(boolean exception) throws ZosBatchException {
//        Mockito.when(zosBatchJobMock.toString()).thenReturn(JOBNAME_JOBID);
//        Mockito.when(zosProgramMock.getImage()).thenReturn(zosImageMock);
//        IZosBatch localZosBatch = Mockito.mock(IZosBatch.class);
//        Mockito.when(localZosBatch.toString()).thenReturn("localZosBatch");
//        Mockito.when(zosBatchSpiMock.getZosBatch(Mockito.any())).thenReturn(localZosBatch);
//        Mockito.when(zosProgramManagerMock.getZosBatchForImage(Mockito.any())).thenReturn(localZosBatch);
//        if (exception) {
//            Mockito.when(localZosBatch.submitJob(Mockito.any(), Mockito.any())).thenThrow(new ZosBatchException());
//        } else {
//            Mockito.when(localZosBatch.submitJob(Mockito.any(), Mockito.any())).thenReturn(zosBatchJobMock);
//        }
//        Mockito.when(zosBatchJobMock.waitForJob()).thenReturn(0);
//        Mockito.when(zosBatchJobMock.getRetcode()).thenReturn(JOB_RETCODE);
//        Mockito.when(zosProgramManagerMock.getTestBundleResources()).thenReturn(bundleResourcesMock);
//    	Mockito.when(zosProgramManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        Mockito.when(zosManagerMock.buildUniquePathName(Mockito.any(), Mockito.any())).thenReturn("path/name");
//        Path archivePathMock = Mockito.mock(Path.class);
//        Mockito.when(zosProgramManagerMock.getArchivePath()).thenReturn(archivePathMock);
//        Mockito.when(archivePathMock.resolve(Mockito.anyString())).thenReturn(archivePathMock);
//        IZosBatchJobname zosJobnameMock = Mockito.mock(IZosBatchJobname.class);
//        Mockito.when(zosJobnameMock.getName()).thenReturn(JOBNAME);
//        Mockito.when(zosBatchJobMock.getJobname()).thenReturn(zosJobnameMock);
//        Mockito.when(zosBatchJobMock.getJobId()).thenReturn(JOBID);
//        Mockito.when(zosProgramMock.getLanguage()).thenReturn(Language.COBOL);
//        Mockito.when(zosProgramMock.getName()).thenReturn(NAME);
//        Mockito.when(zosProgramMock.logForField()).thenReturn(LOG_FOR_FIELD);
//    }
//    
//    @Test
//    public void testFormatDatasetConcatenation() {
//        List<String> datasetList = new LinkedList<>();
//        String concatenation = "DUMMY";
//        Assert.assertEquals("Error in formatDatasetConcatenation() method", concatenation , abstractZosProgramCompilerSpy.formatDatasetConcatenation(datasetList));
//        
//        String disp = "DISP=SHR,DSN=";
//        datasetList.add("DSN1");
//        concatenation = disp + "DSN1";
//        Assert.assertEquals("Error in formatDatasetConcatenation() method", concatenation , abstractZosProgramCompilerSpy.formatDatasetConcatenation(datasetList));
//        
//        datasetList.add("DSN2");
//        concatenation = disp + "DSN1\n//         DD " + disp + "DSN2";
//        Assert.assertEquals("Error in formatDatasetConcatenation() method", concatenation , abstractZosProgramCompilerSpy.formatDatasetConcatenation(datasetList));
//        
//        datasetList.clear();
//        datasetList.add("ASIS-DSN");
//        concatenation = "DSN";
//        Assert.assertEquals("Error in formatDatasetConcatenation() method", concatenation , abstractZosProgramCompilerSpy.formatDatasetConcatenation(datasetList));
//    }
}
