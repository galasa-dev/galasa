/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import com.google.gson.JsonObject;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.cicsts.CedaException;
import dev.galasa.cicsts.CemtException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICeda;
import dev.galasa.cicsts.ICemt;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.spi.ICicsRegionProvisioned;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.sdv.ISdvUser;
import dev.galasa.sdv.SdvManagerException;
import dev.galasa.sdv.internal.properties.SdvHlq;
import dev.galasa.sdv.internal.properties.SdvPort;
import dev.galasa.sdv.internal.properties.SdvSdcActivation;
import dev.galasa.sdv.internal.properties.SdvSrrLogstreamRemoval;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutput;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.internal.ZosBatchJobOutputSpoolFileImpl;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sun.misc.Unsafe;


class TestSdvHttpRecorderImpl {

    private MockedStatic<SdvSdcActivation> sdvSdcActivation;
    private MockedStatic<SdvPort> sdvPort;
    private MockedStatic<SdvSrrLogstreamRemoval> sdvSrrLogstreamRemoval;
    private MockedStatic<SdvHlq> sdvHlq;
    private MockedStatic<Files> files;

    private ICicsRegionProvisioned mockCicsaRegion;
    private IZosImage regionaImage;
    private ICicsRegionProvisioned mockCicsbRegion;
    private IZosImage regionbImage;

    private String regionaTag = "CICSA";
    private String regionaApplid = "APPL1";
    private String passwordString = "password123";
    private String regionbTag = "CICSB";
    private String regionbApplid = "APPL2";
    private String tellerRoleString = "TELLER";
    private String testRunName = "RUN123";
    private String createLogstreamJcl = "jcl_for_defining_logstreams";
    private String deleteLogstreamJcl = "jcl_for_defining_logstreams";
    private String logVariableString = "LOG";
    private String regionAportString = "32000";
    private String regionBportString = "32001";
    private String owner1String = "owner1";
    private String owner2String = "owner2";
    private String user1String = "user1";
    private String user2String = "user2";
    private String user3String = "user3";
    private String creds1String = "CREDS1";
    private String creds2String = "CREDS2";
    private String creds3String = "CREDS3";
    private String adminRoleString = "ADMIN";
    private String sdvHttpRecorderImplClassString =
            "dev.galasa.sdv.internal.SdvHttpRecorderImpl";
    private String jclCreateLogstreamPathString = "/jcl/definelogstream.jcl";
    private String jclDeleteLogstreamPathString = "/jcl/deletelogstreams.jcl";
    private String jclGetYamlPathString = "/jcl/getYaml.jcl";
    private String structureString = "A_STRUCTURE";
    private String theUnsafeString = "theUnsafe";
    private String managerPrefixString = "manager.";
    private String runningManagersString = "runningManagers.";
    private String falseString = "false";
    private String sdcLiveString = ".sdcLive";
    private String cicsServerStringA = "cicsServerA";
    private String sdcUrl = "/DFHSDC";
    private String httpString = "http://";
    private String srrIdString = "srr_id";
    private String srrId1 = "_654654";
    private String srrId2 = "_7676575";
    private String srrId3 = "_4543634";
    private static final String uncheckedString = "unchecked";
    private String user1UnableToStartErrorString
        = "Was unable to start recording for user 'user1', on CICS Region APPL1";
    private String messagePropString = "message";
    private String errorMessageString = "bad stuff happening";
    private String submitString = "submit";
    private String nullString = "null";
    private String errorOutputBadStuffString = "\nbad stuff happening";
    private String errorOutputPayloadStrng =
            "\n{\"srr_id\":\"null\",\"message\":\"somethings gone badly wrong\"}";
    private String serverErrorMessage = "somethings gone badly wrong";
    private String utfString = "utf8";
    private String getYamlString = "/getYaml.jcl";
    private String onCicsRegionMsg = "', on CICS Region ";


    @SuppressWarnings("PMD")
    private static final Log mockLog = Mockito.mock(Log.class);

    @BeforeEach
    public void setUp() throws CicstsManagerException {
        // Registering static mocks before each test
        sdvSdcActivation = Mockito.mockStatic(SdvSdcActivation.class);
        sdvPort = Mockito.mockStatic(SdvPort.class);
        sdvSrrLogstreamRemoval = Mockito.mockStatic(SdvSrrLogstreamRemoval.class);
        sdvHlq = Mockito.mockStatic(SdvHlq.class);
        files = Mockito.mockStatic(Files.class);

        sdvHlq.when(() -> SdvHlq.get(regionaTag)).thenReturn("CICS.INSTALL");
        sdvHlq.when(() -> SdvHlq.get(regionbTag)).thenReturn("CICS.INSTALL");

        // LOG
        Mockito.reset(mockLog);
        Mockito.when(mockLog.isInfoEnabled()).thenReturn(true);
        Mockito.when(mockLog.isWarnEnabled()).thenReturn(true);
        Mockito.when(mockLog.isErrorEnabled()).thenReturn(true);
        Mockito.when(mockLog.isTraceEnabled()).thenReturn(true);
        Mockito.when(mockLog.isDebugEnabled()).thenReturn(true);

        // Region A SdvPort
        sdvPort.when(() -> SdvPort.get(regionaTag)).thenReturn(regionAportString);

        sdvSrrLogstreamRemoval.when(() -> SdvSrrLogstreamRemoval.get(regionaTag)).thenReturn(true);

        // Mock RegionA
        IIpHost ipHostA = Mockito.mock(IIpHost.class);
        Mockito.when(ipHostA.getHostname()).thenReturn(cicsServerStringA);
        regionaImage = Mockito.mock(IZosImage.class);
        Mockito.when(regionaImage.getIpHost()).thenReturn(ipHostA);
        IZosBatchJob mockIzOsBatchJoba = Mockito.mock(IZosBatchJob.class);
        Mockito.when(mockIzOsBatchJoba.getOwner()).thenReturn(owner1String);
        mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getTag()).thenReturn(regionaTag);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn(regionaApplid);
        Mockito.when(mockCicsaRegion.getRegionJob()).thenReturn(mockIzOsBatchJoba);
        Mockito.when(mockCicsaRegion.getZosImage()).thenReturn(regionaImage);

        // Region B SdvPort
        sdvPort.when(() -> SdvPort.get(regionbTag)).thenReturn(regionBportString);

        sdvSrrLogstreamRemoval.when(() -> SdvSrrLogstreamRemoval.get(regionbTag)).thenReturn(true);

        // Mock RegionB
        IIpHost ipHostB = Mockito.mock(IIpHost.class);
        Mockito.when(ipHostB.getHostname()).thenReturn("cicsServerB");
        regionbImage = Mockito.mock(IZosImage.class);
        Mockito.when(regionbImage.getIpHost()).thenReturn(ipHostB);
        IZosBatchJob mockIzOsBatchJobb = Mockito.mock(IZosBatchJob.class);
        Mockito.when(mockIzOsBatchJobb.getOwner()).thenReturn(owner2String);
        mockCicsbRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsbRegion.getTag()).thenReturn(regionbTag);
        Mockito.when(mockCicsbRegion.getApplid()).thenReturn(regionbApplid);
        Mockito.when(mockCicsbRegion.getRegionJob()).thenReturn(mockIzOsBatchJobb);
        Mockito.when(mockCicsbRegion.getZosImage()).thenReturn(regionbImage);
    }

    @AfterEach
    public void tearDown() {
        // Closing static mocks after each test
        sdvSdcActivation.close();
        sdvPort.close();
        sdvSrrLogstreamRemoval.close();
        sdvHlq.close();
        files.close();
    }

    @Test
    void testPrepareEnvironmentsAsFirstManagerOnRegionWithSdcActivation()
            throws CredentialsException, SdvManagerException, ClassNotFoundException,
            NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, InstantiationException, InvocationTargetException,
            NoSuchMethodException, ResourceUnavailableException, DynamicStatusStoreException,
            CicstsManagerException, TestBundleResourceException, IOException, ZosBatchException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).createResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).installGroup(Mockito.any(), Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());
        Mockito.when(
                ceda.resourceExists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        Mockito.when(mockCicsbRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsbRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrB = new RecordingRegion(regionbTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user3Creds =
                new CredentialsUsernamePassword(null, user3String, passwordString);
        ISdvUser user3 = new SdvUserImpl(creds3String, user3Creds, regionbTag, tellerRoleString);
        rrB.addUserToRecord(user3);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);
        recordingRegions.put(mockCicsbRegion, rrB);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenReturn(true);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + regionaApplid + sdcLiveString, falseString);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionbApplid,
                null, testRunName)).thenReturn(true);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + regionbApplid + sdcLiveString, falseString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclCreateLogstreamPathString), Mockito.any()))
                .thenReturn(createLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(0);
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(createLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);
        Mockito.when(batchManager.getZosBatch(regionbImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);
        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionbTag)).thenReturn(true);

        // Make call to funtion under test
        sdvHttpRecorder.prepareEnvironments(structureString);

        // Ensure that we call ceda & cemt to create resources
        Mockito.verify(cemt, Mockito.times(4)).setResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(6)).createResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(4)).installGroup(Mockito.any(), Mockito.any());
    }

    @Test
    void testPrepareEnvironmentsAsSecondManagerOnRegion()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            DynamicStatusStoreException, TestBundleResourceException, IOException,
            ZosBatchException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).createResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).installGroup(Mockito.any(), Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());
        Mockito.when(
                ceda.resourceExists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        Mockito.when(mockCicsbRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsbRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrB = new RecordingRegion(regionbTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user3Creds =
                new CredentialsUsernamePassword(null, user3String, passwordString);
        ISdvUser user3 = new SdvUserImpl(creds3String, user3Creds, regionbTag, tellerRoleString);
        rrB.addUserToRecord(user3);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);
        recordingRegions.put(mockCicsbRegion, rrB);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenReturn(false);
        Mockito.when(dssService.get(managerPrefixString + runningManagersString + regionaApplid))
                .thenReturn("L1,L2");
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + runningManagersString + regionaApplid,
                "L1,L2,RUN123");
        Mockito.when(dssService.get(managerPrefixString + regionaApplid + sdcLiveString))
                .thenReturn(falseString, "true");
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionbApplid,
                null, testRunName)).thenReturn(false);
        Mockito.when(dssService.get(managerPrefixString + runningManagersString + regionbApplid))
                .thenReturn("L1,L2");
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + runningManagersString + regionbApplid,
                "L5,L6,RUN123");
        Mockito.when(dssService.get(managerPrefixString + regionbApplid + sdcLiveString))
                .thenReturn(falseString, "true");

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclCreateLogstreamPathString), Mockito.any()))
                .thenReturn(createLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(0);
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(createLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        sdvHttpRecorder.prepareEnvironments(structureString);

        // Ensure that we DO NOT call ceda & cemt to create resources.
        // Another manager will had created the resource, so this manager didn't have to.
        Mockito.verify(cemt, Mockito.times(0)).setResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(0)).createResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(0)).installGroup(Mockito.any(), Mockito.any());
    }

    @Test
    void testPrepareEnvironmentsLogstreamJobFailsDueToExisting()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            DynamicStatusStoreException, TestBundleResourceException, IOException,
            ZosBatchException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).createResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).installGroup(Mockito.any(), Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());
        Mockito.when(
                ceda.resourceExists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenReturn(true);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + regionaApplid + sdcLiveString, falseString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclCreateLogstreamPathString), Mockito.any()))
                .thenReturn(createLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(12); // EXISTING RETURN CODE
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(createLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        sdvHttpRecorder.prepareEnvironments(structureString);

        // Ensure that we call ceda & cemt to create resources
        Mockito.verify(cemt, Mockito.times(2)).setResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(3)).createResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(2)).installGroup(Mockito.any(), Mockito.any());

        // Check there is a warning in the log indicating Region A won't record
        Mockito.verify(mockLog).info(
            "Logstream not created, using existing, on CICS Region "
            + regionaApplid
            + "."
        );
    }

    @Test
    void testPrepareEnvironmentsLogstreamJobFailsDueToError()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            DynamicStatusStoreException, TestBundleResourceException, IOException,
            ZosBatchException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).createResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).installGroup(Mockito.any(), Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());
        Mockito.when(
                ceda.resourceExists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenReturn(true);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + regionaApplid + sdcLiveString, falseString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclCreateLogstreamPathString), Mockito.any()))
                .thenReturn(createLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(16); // EXISTING RETURN CODE
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(createLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        sdvHttpRecorder.prepareEnvironments(structureString);

        // Ensure that we call ceda & cemt to create resources
        Mockito.verify(cemt, Mockito.times(2)).setResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(3)).createResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(2)).installGroup(Mockito.any(), Mockito.any());

        // Check there is a warning in the log indicating Region A won't record
        Mockito.verify(mockLog).warn(
            "JCL to define logstreams fail for CICS Region "
            + regionaApplid
            + ", check artifacts for more details"
        );
    }

    @Test
    void testPrepareEnvironmentsCedaCreateException()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            DynamicStatusStoreException, TestBundleResourceException, IOException,
            ZosBatchException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doThrow(new CedaException("Could not create resource")).when(ceda).createResource(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenReturn(true);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + regionaApplid + sdcLiveString, falseString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclCreateLogstreamPathString), Mockito.any()))
                .thenReturn(createLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(0);
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(createLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.prepareEnvironments(structureString);
        });

        Assertions.assertEquals(
            "Could not create SRR JOURNALMODEL definition on CICS Region "
            + regionaApplid
            + ".",
            exception.getMessage()
        );

    }

    @Test
    void testPrepareEnvironmentsCedaInstallSdvGrpException()
            throws IllegalArgumentException, IllegalAccessException, InstantiationException,
            InvocationTargetException, NoSuchMethodException, SecurityException,
            NoSuchFieldException, ClassNotFoundException, ZosBatchException,
            TestBundleResourceException, IOException, DynamicStatusStoreException,
            CicstsManagerException, CredentialsException, SdvManagerException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).createResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.doThrow(new CedaException("could not install")).when(ceda)
                .installGroup(Mockito.any(), Mockito.any());

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenReturn(true);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + regionaApplid + sdcLiveString, falseString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclCreateLogstreamPathString), Mockito.any()))
                .thenReturn(createLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(0);
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(createLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.prepareEnvironments(structureString);
        });

        Assertions.assertEquals(
            "Could not install SDV resource group on CICS region "
            + regionaApplid,
            exception.getMessage()
        );

    }

    @Test
    void testPrepareEnvironmentsCedaInstallDfhxsdException()
            throws IllegalArgumentException, IllegalAccessException, InstantiationException,
            InvocationTargetException, NoSuchMethodException, SecurityException,
            NoSuchFieldException, ClassNotFoundException, ZosBatchException,
            TestBundleResourceException, IOException, DynamicStatusStoreException,
            CicstsManagerException, CredentialsException, SdvManagerException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).createResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).installGroup(Mockito.any(), Mockito.eq("SDVGRP"));
        Mockito.doThrow(new CedaException("could not install")).when(ceda)
                .installGroup(Mockito.any(), Mockito.eq("DFHXSD"));

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenReturn(true);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + regionaApplid + sdcLiveString, falseString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclCreateLogstreamPathString), Mockito.any()))
                .thenReturn(createLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(0);
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(createLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        sdvHttpRecorder.prepareEnvironments(structureString);

        // Ensure that we call ceda & cemt to create resources
        Mockito.verify(cemt, Mockito.times(0)).setResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(3)).createResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(2)).installGroup(Mockito.any(), Mockito.any());

        // Check there is a warning in the log indicating Region A won't record
        Mockito.verify(mockLog, Mockito.times(1)).info(
            "Couldn't install DFHXSD, already installed on CICS Region "
            + regionaApplid
        );

    }

    @Test
    void testPrepareEnvironmentsBatchJobThrowsException() throws CredentialsException,
            SdvManagerException, DynamicStatusStoreException, TestBundleResourceException,
            IOException, ZosBatchException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException, CicstsManagerException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenReturn(true);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + regionaApplid + sdcLiveString, falseString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclCreateLogstreamPathString), Mockito.any()))
                .thenReturn(createLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenThrow(new ZosBatchException("bad job run"));
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(createLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.prepareEnvironments(structureString);
        });

        Assertions.assertEquals(
            "Unable to run JCL to define logstreams for CICS Region "
            + regionaApplid,
            exception.getMessage()
        );
    }

    @Test
    void testPrepareEnvironmentsDssThrowsException()
            throws DynamicStatusStoreException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException, InstantiationException,
            InvocationTargetException, NoSuchMethodException, CicstsManagerException,
            CredentialsException, SdvManagerException, ClassNotFoundException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenThrow(new DynamicStatusStoreException("dss issues"));

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, null, null, null, dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.prepareEnvironments(structureString);
        });

        Assertions.assertEquals("Unable interact with DSS for SDV.", exception.getMessage());
    }

    @Test
    void testPrepareEnvironmentsCemtThrowsException() throws DynamicStatusStoreException,
            TestBundleResourceException, IOException, CicstsManagerException, CredentialsException,
            SdvManagerException, ZosBatchException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doThrow(new CemtException("bad news")).when(cemt).setResource(Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).createResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).installGroup(Mockito.any(), Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());
        Mockito.when(
                ceda.resourceExists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenReturn(true);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + regionaApplid + sdcLiveString, falseString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclCreateLogstreamPathString), Mockito.any()))
                .thenReturn(createLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(0);
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(createLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        sdvHttpRecorder.prepareEnvironments(structureString);

        // Ensure that we call ceda & cemt to create resources
        Mockito.verify(cemt, Mockito.times(1)).setResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(3)).createResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(2)).installGroup(Mockito.any(), Mockito.any());

        // Check there is a warning in the log indicating Region A won't record
        Mockito.verify(mockLog).debug(
            "CICS resource disabling expectedly failed on "
            + regionaApplid
        );
    }

    @Test
    void testPrepareEnvironmentsPortNotFound() throws SdvManagerException, IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException,
            InvocationTargetException, NoSuchMethodException, ZosBatchException,
            TestBundleResourceException, IOException, DynamicStatusStoreException,
            CredentialsException, CicstsManagerException, ClassNotFoundException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).createResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).installGroup(Mockito.any(), Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());
        Mockito.when(
                ceda.resourceExists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);

        // Mock SDVPort
        sdvPort.when(() -> SdvPort.get(regionaTag)).thenReturn(null);

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenReturn(true);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + regionaApplid + sdcLiveString, falseString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclCreateLogstreamPathString), Mockito.any()))
                .thenReturn(createLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(16); // EXISTING RETURN CODE
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(createLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.prepareEnvironments(structureString);
        });

        Assertions.assertEquals(
            "Could not find SDC port in CPS properties for CICS tag: "
            + regionaTag,
            exception.getMessage()
        );

    }

    @Test
    void testPrepareEnvironmentsHttpResourcesDoNotAlreadyExist()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            DynamicStatusStoreException, TestBundleResourceException, IOException,
            ZosBatchException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).createResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).installGroup(Mockito.any(), Mockito.any());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());
        Mockito.when(
                ceda.resourceExists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(false);

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.putSwap(managerPrefixString + runningManagersString + regionaApplid,
                null, testRunName)).thenReturn(true);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + regionaApplid + sdcLiveString, falseString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclCreateLogstreamPathString), Mockito.any()))
                .thenReturn(createLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(16); // EXISTING RETURN CODE
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(createLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        sdvHttpRecorder.prepareEnvironments(structureString);

        // Ensure that we call ceda & cemt was not used to disable resources
        Mockito.verify(cemt, Mockito.times(0)).setResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(3)).createResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(2)).installGroup(Mockito.any(), Mockito.any());
    }

    @Test
    void testCleanUpEnvironmentsAsLastManagerOnRegionWithSdcActivation()
            throws CredentialsException, SdvManagerException, ClassNotFoundException,
            NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, InstantiationException, InvocationTargetException,
            NoSuchMethodException, ResourceUnavailableException, DynamicStatusStoreException,
            CicstsManagerException, TestBundleResourceException, IOException, ZosBatchException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        Mockito.when(mockCicsbRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsbRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrB = new RecordingRegion(regionbTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user3Creds =
                new CredentialsUsernamePassword(null, user3String, passwordString);
        ISdvUser user3 = new SdvUserImpl(creds3String, user3Creds, regionbTag, tellerRoleString);
        rrB.addUserToRecord(user3);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);
        recordingRegions.put(mockCicsbRegion, rrB);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.get(managerPrefixString + runningManagersString + regionaApplid))
                .thenReturn(testRunName);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).delete(managerPrefixString + runningManagersString + regionaApplid);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).delete(managerPrefixString + regionaApplid + sdcLiveString);

        Mockito.when(dssService.get(managerPrefixString + runningManagersString + regionbApplid))
                .thenReturn(testRunName);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).delete(managerPrefixString + runningManagersString + regionbApplid);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).delete(managerPrefixString + regionbApplid + sdcLiveString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclDeleteLogstreamPathString), Mockito.any()))
                .thenReturn(deleteLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(0);
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(deleteLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);
        Mockito.when(batchManager.getZosBatch(regionbImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);
        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionbTag)).thenReturn(true);

        // Make call to funtion under test
        sdvHttpRecorder.cleanUpEnvironments();

        // Ensure that we call ceda & cemt to delete resources
        Mockito.verify(cemt, Mockito.times(4)).setResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(2)).deleteGroup(Mockito.any(), Mockito.any());
    }

    @Test
    void testCleanUpEnvironmentsNotAsLastManagerOnRegionWithSdcActivation()
            throws CredentialsException, SdvManagerException, ClassNotFoundException,
            NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, InstantiationException, InvocationTargetException,
            NoSuchMethodException, ResourceUnavailableException, DynamicStatusStoreException,
            CicstsManagerException, TestBundleResourceException, IOException, ZosBatchException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        Mockito.when(mockCicsbRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsbRegion.cemt()).thenReturn(cemt);
        // Mock RegionA Terminal
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrB = new RecordingRegion(regionbTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user3Creds =
                new CredentialsUsernamePassword(null, user3String, passwordString);
        ISdvUser user3 = new SdvUserImpl(creds3String, user3Creds, regionbTag, tellerRoleString);
        rrB.addUserToRecord(user3);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);
        recordingRegions.put(mockCicsbRegion, rrB);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.get(managerPrefixString + runningManagersString + regionaApplid))
                .thenReturn(testRunName + ",L564,L234");
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + runningManagersString + regionaApplid,
                "L564,L234");

        Mockito.when(dssService.get(managerPrefixString + runningManagersString + regionbApplid))
                .thenReturn(testRunName + ",L789,L101");
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).put(managerPrefixString + runningManagersString + regionbApplid,
                "L789,L101");

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, null, null, null, dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);
        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionbTag)).thenReturn(true);

        // Make call to funtion under test
        sdvHttpRecorder.cleanUpEnvironments();

        // Ensure that we do not call ceda & cemt to delete resources
        Mockito.verify(cemt, Mockito.times(0)).setResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(0)).deleteGroup(Mockito.any(), Mockito.any());
    }

    @Test
    void testCleanUpEnvironmentsDssException() throws CredentialsException, SdvManagerException,
            DynamicStatusStoreException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.get(managerPrefixString + runningManagersString + regionaApplid))
                .thenThrow(new DynamicStatusStoreException("Unable interact with DSS for SDV."));

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, null, null, null, dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.cleanUpEnvironments();
        });

        Assertions.assertEquals("Unable interact with DSS for SDV.", exception.getMessage());
    }

    @Test
    void testCleanUpEnvironmentsCedaDeleteGroupThrowsException()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            DynamicStatusStoreException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException,
            TestBundleResourceException, IOException, ZosBatchException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doThrow(new CedaException("cannot delete group")).when(ceda)
                .deleteGroup(Mockito.any(), Mockito.any());

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.get(managerPrefixString + runningManagersString + regionaApplid))
                .thenReturn(testRunName);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclDeleteLogstreamPathString), Mockito.any()))
                .thenReturn(deleteLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(0);
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(deleteLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);
        Mockito.when(batchManager.getZosBatch(regionbImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        sdvHttpRecorder.cleanUpEnvironments();

        Mockito.verify(cemt, Mockito.times(2)).setResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.verify(ceda, Mockito.times(1)).deleteGroup(Mockito.any(), Mockito.any());

        // Check there is a warning in the log indicating Region A won't record
        Mockito.verify(mockLog).error(Mockito.eq(
            "Could not delete the SDVGRP on CICS Region "
            + regionaApplid
        ), Mockito.any());
    }

    @Test
    void testCleanUpEnvironmentsDeleteSrrLogstreamJobError()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            DynamicStatusStoreException, TestBundleResourceException, IOException,
            ZosBatchException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.get(managerPrefixString + runningManagersString + regionaApplid))
                .thenReturn(testRunName);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclDeleteLogstreamPathString), Mockito.any()))
                .thenReturn(deleteLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(8);
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(deleteLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);
        Mockito.when(batchManager.getZosBatch(regionbImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.cleanUpEnvironments();
        });

        Assertions.assertEquals(
            "JCL to delete logstreams fail on CICS Region "
            + regionaApplid
            + ", check artifacts for more details.",
            exception.getMessage()
        );
    }

    @Test
    void testCleanUpEnvironmentsArtifactManagerException() throws CicstsManagerException,
            CredentialsException, SdvManagerException, DynamicStatusStoreException,
            ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, TestBundleResourceException, IOException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cemt).setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.get(managerPrefixString + runningManagersString + regionaApplid))
                .thenReturn(testRunName);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclDeleteLogstreamPathString), Mockito.any()))
                .thenThrow(new TestBundleResourceException("not found"));
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, null, null, dssService,
                        null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.cleanUpEnvironments();
        });

        Assertions.assertEquals(
            "Unable to run JCL to delete logstreams for CICS Region "
            + regionaApplid,
            exception.getMessage()
        );
    }

    @Test
    void testCleanUpEnvironmentsCemtSetResourceException()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            DynamicStatusStoreException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, ClassNotFoundException,
            TestBundleResourceException, IOException, ZosBatchException {
        // Mock Cemt
        ICemt cemt = Mockito.mock(ICemt.class);
        Mockito.doThrow(new CemtException("couldn't set resource")).when(cemt)
                .setResource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        // Mock Ceda
        ICeda ceda = Mockito.mock(ICeda.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(ceda).deleteGroup(Mockito.any(), Mockito.any());

        Mockito.when(mockCicsaRegion.ceda()).thenReturn(ceda);
        Mockito.when(mockCicsaRegion.cemt()).thenReturn(cemt);

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(testRunName);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.get(managerPrefixString + runningManagersString + regionaApplid))
                .thenReturn(testRunName);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(
                Mockito.eq(jclDeleteLogstreamPathString), Mockito.any()))
                .thenReturn(deleteLogstreamJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJob.waitForJob()).thenReturn(0);
        IZosBatch zosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatch.submitJob(deleteLogstreamJcl, null)).thenReturn(zosJob);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatch);
        Mockito.when(batchManager.getZosBatch(regionbImage)).thenReturn(zosBatch);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(framework, recordingRegions, artifactManager, batchManager, null,
                        dssService, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionaTag)).thenReturn(true);
        sdvSdcActivation.when(() -> SdvSdcActivation.get(regionbTag)).thenReturn(true);

        // Make call to funtion under test
        sdvHttpRecorder.cleanUpEnvironments();

        // Ensure that we call ceda & cemt to create resources
        Mockito.verify(ceda, Mockito.times(1)).deleteGroup(Mockito.any(), Mockito.any());
        Mockito.verify(cemt, Mockito.times(2)).setResource(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());

        // Check there is a warning in the log indicating Region A won't record
        Mockito.verify(mockLog, Mockito.times(1))
                .error("Could not create URIMAP on CICS Region " + regionaApplid);
        Mockito.verify(mockLog, Mockito.times(1))
                .error("Could not create TCPIPSERVICE on CICS Region " + regionaApplid);
    }

    @SuppressWarnings(uncheckedString)
    @Test
    void testStartRecordingNoExistingSdcForMultipleRegionsAndUsers()
            throws SdvManagerException, CicstsManagerException, CredentialsException,
            ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, URISyntaxException, HttpClientException {

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock RegionB Terminal
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionB
        RecordingRegion rrB = new RecordingRegion(regionbTerminal);
        // Mock user 3
        ICredentialsUsernamePassword user3Creds =
                new CredentialsUsernamePassword(null, user3String, passwordString);
        ISdvUser user3 = new SdvUserImpl(creds3String, user3Creds, regionbTag, tellerRoleString);
        rrB.addUserToRecord(user3);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);
        recordingRegions.put(mockCicsbRegion, rrB);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse1.getStatusCode()).thenReturn(404);
        Mockito.when(httpClient1.getJson(sdcUrl)).thenReturn(mockGetResponse1);
        // Mock POST JSON return from SDC - to start recording
        HttpClientResponse<JsonObject> mockPostResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockPostResponse1.getStatusCode()).thenReturn(201);
        JsonObject postResponseContent1 = new JsonObject();
        postResponseContent1.addProperty(srrIdString, srrId1);
        Mockito.when(mockPostResponse1.getContent()).thenReturn(postResponseContent1);
        JsonObject postBody = new JsonObject();
        Mockito.when(httpClient1.postJson(sdcUrl, postBody)).thenReturn(mockPostResponse1);

        // Mock HTTP Client - 2nd time
        IHttpClient httpClient2 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient2)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient2).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse2 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse2.getStatusCode()).thenReturn(404);
        Mockito.when(httpClient2.getJson(sdcUrl)).thenReturn(mockGetResponse2);
        // Mock POST JSON return from SDC - to start recording
        HttpClientResponse<JsonObject> mockPostResponse2 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockPostResponse2.getStatusCode()).thenReturn(201);
        JsonObject postResponseContent2 = new JsonObject();
        postResponseContent2.addProperty(srrIdString, srrId2);
        Mockito.when(mockPostResponse2.getContent()).thenReturn(postResponseContent2);
        Mockito.when(httpClient2.postJson(sdcUrl, postBody)).thenReturn(mockPostResponse2);

        // Mock HTTP Client - 3rd time
        IHttpClient httpClient3 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient3)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient3).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse3 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse3.getStatusCode()).thenReturn(404);
        Mockito.when(httpClient3.getJson(sdcUrl)).thenReturn(mockGetResponse3);
        // Mock POST JSON return from SDC - to start recording
        HttpClientResponse<JsonObject> mockPostResponse3 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockPostResponse3.getStatusCode()).thenReturn(201);
        JsonObject postResponseContent3 = new JsonObject();
        postResponseContent3.addProperty(srrIdString, srrId3);
        Mockito.when(mockPostResponse3.getContent()).thenReturn(postResponseContent3);
        Mockito.when(httpClient3.postJson(sdcUrl, postBody)).thenReturn(mockPostResponse3);

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1, httpClient2, httpClient3);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvHttpRecorder.startRecording();

        // Assert HTTP endpoints called
        Mockito.verify(httpClient1, Mockito.times(1)).getJson(Mockito.any());
        Mockito.verify(httpClient1, Mockito.times(1)).postJson(Mockito.any(), Mockito.any());
        Mockito.verify(httpClient2, Mockito.times(1)).getJson(Mockito.any());
        Mockito.verify(httpClient2, Mockito.times(1)).postJson(Mockito.any(), Mockito.any());
        Mockito.verify(httpClient3, Mockito.times(1)).getJson(Mockito.any());
        Mockito.verify(httpClient3, Mockito.times(1)).postJson(Mockito.any(), Mockito.any());

        // Assert that all users are set to recording with SRR IDs
        List<String> expectedSrrIdsList = new ArrayList<>();
        expectedSrrIdsList.add(srrId1);
        expectedSrrIdsList.add(srrId2);
        expectedSrrIdsList.add(srrId3);

        Assertions.assertEquals(true, user1.isRecording());
        Assertions.assertTrue(expectedSrrIdsList.contains(user1.getSrrId()));
        Assertions.assertEquals(true, user1.isRecording());
        expectedSrrIdsList.remove(user1.getSrrId());

        Assertions.assertEquals(true, user2.isRecording());
        Assertions.assertTrue(expectedSrrIdsList.contains(user2.getSrrId()));
        Assertions.assertEquals(true, user2.isRecording());
        expectedSrrIdsList.remove(user2.getSrrId());

        Assertions.assertEquals(true, user3.isRecording());
        Assertions.assertTrue(expectedSrrIdsList.contains(user3.getSrrId()));
        Assertions.assertEquals(true, user3.isRecording());
        expectedSrrIdsList.remove(user3.getSrrId());
    }

    @SuppressWarnings(uncheckedString)
    @Test
    void testStartRecordingExistingSdcForMultipleRegionsAndUsers()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            URISyntaxException, HttpClientException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        rrA.addUserToRecord(user2);

        // Mock RegionB Terminal
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionB
        RecordingRegion rrB = new RecordingRegion(regionbTerminal);
        // Mock user 3
        ICredentialsUsernamePassword user3Creds =
                new CredentialsUsernamePassword(null, user3String, passwordString);
        ISdvUser user3 = new SdvUserImpl(creds3String, user3Creds, regionbTag, tellerRoleString);
        rrB.addUserToRecord(user3);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);
        recordingRegions.put(mockCicsbRegion, rrB);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse1.getStatusCode()).thenReturn(200);
        Mockito.when(httpClient1.getJson(sdcUrl)).thenReturn(mockGetResponse1);
        // Mock DELETE JSON return from SDC - to stop existing SDC
        HttpClientResponse<JsonObject> mockDeleteResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockDeleteResponse1.getStatusCode()).thenReturn(200);
        JsonObject deleteBody = new JsonObject();
        deleteBody.addProperty(submitString, false);
        Mockito.when(httpClient1.deleteJson(sdcUrl, deleteBody)).thenReturn(mockDeleteResponse1);
        // Mock POST JSON return from SDC - to start recording
        HttpClientResponse<JsonObject> mockPostResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockPostResponse1.getStatusCode()).thenReturn(201);
        JsonObject postResponseContent1 = new JsonObject();
        postResponseContent1.addProperty(srrIdString, srrId1);
        Mockito.when(mockPostResponse1.getContent()).thenReturn(postResponseContent1);
        JsonObject postBody = new JsonObject();
        Mockito.when(httpClient1.postJson(sdcUrl, postBody)).thenReturn(mockPostResponse1);

        // Mock HTTP Client - 2nd time
        IHttpClient httpClient2 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient2)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient2).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse2 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse2.getStatusCode()).thenReturn(200);
        Mockito.when(httpClient2.getJson(sdcUrl)).thenReturn(mockGetResponse2);
        // Mock DELETE JSON return from SDC - to stop existing SDC
        HttpClientResponse<JsonObject> mockDeleteResponse2 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockDeleteResponse2.getStatusCode()).thenReturn(200);
        Mockito.when(httpClient2.deleteJson(sdcUrl, deleteBody)).thenReturn(mockDeleteResponse2);
        // Mock POST JSON return from SDC - to start recording
        HttpClientResponse<JsonObject> mockPostResponse2 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockPostResponse2.getStatusCode()).thenReturn(201);
        JsonObject postResponseContent2 = new JsonObject();
        postResponseContent2.addProperty(srrIdString, srrId2);
        Mockito.when(mockPostResponse2.getContent()).thenReturn(postResponseContent2);
        Mockito.when(httpClient2.postJson(sdcUrl, postBody)).thenReturn(mockPostResponse2);

        // Mock HTTP Client - 3rd time
        IHttpClient httpClient3 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient3)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient3).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse3 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse3.getStatusCode()).thenReturn(200);
        Mockito.when(httpClient3.getJson(sdcUrl)).thenReturn(mockGetResponse3);
        // Mock DELETE JSON return from SDC - to stop existing SDC
        HttpClientResponse<JsonObject> mockDeleteResponse3 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockDeleteResponse3.getStatusCode()).thenReturn(200);
        Mockito.when(httpClient3.deleteJson(sdcUrl, deleteBody)).thenReturn(mockDeleteResponse3);
        // Mock POST JSON return from SDC - to start recording
        HttpClientResponse<JsonObject> mockPostResponse3 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockPostResponse3.getStatusCode()).thenReturn(201);
        JsonObject postResponseContent3 = new JsonObject();
        postResponseContent3.addProperty(srrIdString, srrId3);
        Mockito.when(mockPostResponse3.getContent()).thenReturn(postResponseContent3);
        Mockito.when(httpClient3.postJson(sdcUrl, postBody)).thenReturn(mockPostResponse3);

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1, httpClient2, httpClient3);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvHttpRecorder.startRecording();

        // Assert HTTP endpoints called
        Mockito.verify(httpClient1, Mockito.times(1)).getJson(Mockito.any());
        Mockito.verify(httpClient1, Mockito.times(1)).deleteJson(Mockito.any(), Mockito.any());
        Mockito.verify(httpClient1, Mockito.times(1)).postJson(Mockito.any(), Mockito.any());
        Mockito.verify(httpClient2, Mockito.times(1)).getJson(Mockito.any());
        Mockito.verify(httpClient2, Mockito.times(1)).deleteJson(Mockito.any(), Mockito.any());
        Mockito.verify(httpClient2, Mockito.times(1)).postJson(Mockito.any(), Mockito.any());
        Mockito.verify(httpClient3, Mockito.times(1)).getJson(Mockito.any());
        Mockito.verify(httpClient3, Mockito.times(1)).deleteJson(Mockito.any(), Mockito.any());
        Mockito.verify(httpClient3, Mockito.times(1)).postJson(Mockito.any(), Mockito.any());

        // Assert that all users are set to recording with SRR IDs
        List<String> expectedSrrIdsList = new ArrayList<>();
        expectedSrrIdsList.add(srrId1);
        expectedSrrIdsList.add(srrId2);
        expectedSrrIdsList.add(srrId3);

        Assertions.assertEquals(true, user1.isRecording());
        Assertions.assertTrue(expectedSrrIdsList.contains(user1.getSrrId()));
        Assertions.assertEquals(true, user1.isRecording());
        expectedSrrIdsList.remove(user1.getSrrId());

        Assertions.assertEquals(true, user2.isRecording());
        Assertions.assertTrue(expectedSrrIdsList.contains(user2.getSrrId()));
        Assertions.assertEquals(true, user2.isRecording());
        expectedSrrIdsList.remove(user2.getSrrId());

        Assertions.assertEquals(true, user3.isRecording());
        Assertions.assertTrue(expectedSrrIdsList.contains(user3.getSrrId()));
        Assertions.assertEquals(true, user3.isRecording());
        expectedSrrIdsList.remove(user3.getSrrId());

    }

    @SuppressWarnings(uncheckedString)
    @Test
    void testStartRecordingHttpGetSdcServerError() throws CredentialsException, SdvManagerException,
            URISyntaxException, HttpClientException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException, CicstsManagerException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse1.getStatusCode()).thenReturn(500);
        Mockito.when(mockGetResponse1.getStatusMessage()).thenReturn(errorMessageString);
        JsonObject getResponseContent1 = new JsonObject();
        getResponseContent1.addProperty(srrIdString, nullString);
        getResponseContent1.addProperty(messagePropString, serverErrorMessage);
        Mockito.when(mockGetResponse1.getContent()).thenReturn(getResponseContent1);
        Mockito.when(httpClient1.getJson(sdcUrl)).thenReturn(mockGetResponse1);

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.startRecording();
        });

        Assertions.assertEquals(user1UnableToStartErrorString, exception.getMessage());
        Assertions.assertEquals(
            "Error whilst obtaining current SDC status for user 'user1', "
            + "on CICS Region APPL1. Status code: 500"
            + errorOutputBadStuffString + errorOutputPayloadStrng,
            exception.getCause().getMessage()
        );
    }

    @Test
    void testStartRecordingGetHttpClientException()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            URISyntaxException, HttpClientException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        Mockito.when(httpClient1.getJson(sdcUrl))
                .thenThrow(new HttpClientException("Could not get response from SDC"));

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.startRecording();
        });

        Assertions.assertEquals(user1UnableToStartErrorString, exception.getMessage());
        Assertions.assertEquals(
            "Could not check status SDC recording status for user '"
            + user1String
            + onCicsRegionMsg
            + regionaApplid
            + ". Is SDC activated?",
            exception.getCause().getMessage()
        );
    }

    @SuppressWarnings(uncheckedString)
    @Test
    void testStartRecordingHttpDeleteSdcServerError()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            URISyntaxException, HttpClientException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, ClassNotFoundException, NoSuchFieldException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse1.getStatusCode()).thenReturn(200);
        Mockito.when(httpClient1.getJson(sdcUrl)).thenReturn(mockGetResponse1);
        // Mock DELETE JSON return from SDC - to stop existing SDC
        HttpClientResponse<JsonObject> mockDeleteResponse1 = Mockito.mock(HttpClientResponse.class);
        JsonObject deleteResponseContent1 = new JsonObject();
        deleteResponseContent1.addProperty(srrIdString, nullString);
        deleteResponseContent1.addProperty(messagePropString, serverErrorMessage);
        Mockito.when(mockDeleteResponse1.getStatusCode()).thenReturn(500);
        Mockito.when(mockDeleteResponse1.getStatusMessage()).thenReturn(errorMessageString);
        Mockito.when(mockDeleteResponse1.getContent()).thenReturn(deleteResponseContent1);
        JsonObject deleteBody = new JsonObject();
        deleteBody.addProperty(submitString, false);
        Mockito.when(httpClient1.deleteJson(sdcUrl, deleteBody)).thenReturn(mockDeleteResponse1);

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.startRecording();
        });

        Assertions.assertEquals(user1UnableToStartErrorString, exception.getMessage());
        Assertions.assertEquals(
            "Could not stop SDC recording for user '"
            + user1String
            + onCicsRegionMsg
            + regionaApplid
            + ". Status code: 500"
            + errorOutputBadStuffString
            + errorOutputPayloadStrng,
            exception.getCause().getMessage()
        );
    }

    @SuppressWarnings(uncheckedString)
    @Test
    void testStartRecordingDeleteHttpClientException()
            throws HttpClientException, CicstsManagerException, CredentialsException,
            SdvManagerException, URISyntaxException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse1.getStatusCode()).thenReturn(200);
        Mockito.when(httpClient1.getJson(sdcUrl)).thenReturn(mockGetResponse1);
        // Mock DELETE JSON return from SDC - to stop existing SDC
        JsonObject deleteBody = new JsonObject();
        deleteBody.addProperty(submitString, false);
        Mockito.when(httpClient1.deleteJson(sdcUrl, deleteBody))
                .thenThrow(new HttpClientException("No response"));

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.startRecording();
        });

        Assertions.assertEquals(user1UnableToStartErrorString, exception.getMessage());
        Assertions.assertEquals(
            "Could not stop existing SDC recording for user '"
            + user1String
            + onCicsRegionMsg
            + regionaApplid
            + ".",
            exception.getCause().getMessage()
        );
    }

    @SuppressWarnings(uncheckedString)
    @Test
    void testStartRecordingHttpPostSdcServerError()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
            SecurityException, InstantiationException, InvocationTargetException,
            NoSuchMethodException, HttpClientException, URISyntaxException, ClassNotFoundException,
            SdvManagerException, CredentialsException, CicstsManagerException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse1.getStatusCode()).thenReturn(404);
        Mockito.when(httpClient1.getJson(sdcUrl)).thenReturn(mockGetResponse1);
        // Mock POST return from SDC
        HttpClientResponse<JsonObject> mockPostResponse1 = Mockito.mock(HttpClientResponse.class);
        JsonObject postResponseContent1 = new JsonObject();
        postResponseContent1.addProperty(srrIdString, nullString);
        postResponseContent1.addProperty(messagePropString, serverErrorMessage);
        Mockito.when(mockPostResponse1.getStatusCode()).thenReturn(500);
        Mockito.when(mockPostResponse1.getStatusMessage()).thenReturn(errorMessageString);
        Mockito.when(mockPostResponse1.getContent()).thenReturn(postResponseContent1);
        JsonObject postBody = new JsonObject();
        Mockito.when(httpClient1.postJson(sdcUrl, postBody)).thenReturn(mockPostResponse1);

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.startRecording();
        });

        Assertions.assertEquals(user1UnableToStartErrorString, exception.getMessage());
        Assertions.assertEquals(
                "Could not start SDC recording for user '"
                + user1String
                + onCicsRegionMsg
                + regionaApplid
                + ". Status code: 500"
                + errorOutputBadStuffString
                + errorOutputPayloadStrng,
                exception.getCause().getMessage()
        );
    }

    @SuppressWarnings(uncheckedString)
    @Test
    void testStartRecordingPostHttpClientException()
            throws CicstsManagerException, CredentialsException, SdvManagerException,
            URISyntaxException, HttpClientException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse1.getStatusCode()).thenReturn(404);
        Mockito.when(httpClient1.getJson(sdcUrl)).thenReturn(mockGetResponse1);
        // Mock POST return from SDC
        JsonObject postBody = new JsonObject();
        Mockito.when(httpClient1.postJson(sdcUrl, postBody))
                .thenThrow(new HttpClientException("no response"));

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.startRecording();
        });

        Assertions.assertEquals(user1UnableToStartErrorString, exception.getMessage());
        Assertions.assertEquals(
            "Could not start SDC recording for user '"
            + user1String
            + onCicsRegionMsg
            + regionaApplid,
            exception.getCause().getMessage()
        );
    }

    @SuppressWarnings(uncheckedString)
    @Test
    void testStartRecordingNoSrrIdInResponse()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
            SecurityException, InstantiationException, InvocationTargetException,
            NoSuchMethodException, ClassNotFoundException, HttpClientException,
            CicstsManagerException, CredentialsException, SdvManagerException, URISyntaxException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock GET JSON return from SDC - to find current status
        HttpClientResponse<JsonObject> mockGetResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockGetResponse1.getStatusCode()).thenReturn(404);
        Mockito.when(httpClient1.getJson(sdcUrl)).thenReturn(mockGetResponse1);
        // Mock POST return from SDC
        HttpClientResponse<JsonObject> mockPostResponse1 = Mockito.mock(HttpClientResponse.class);
        JsonObject postResponseContent1 = new JsonObject();
        postResponseContent1.addProperty(srrIdString, "");
        postResponseContent1.addProperty(messagePropString, "edge case");
        Mockito.when(mockPostResponse1.getStatusCode()).thenReturn(201);
        Mockito.when(mockPostResponse1.getStatusMessage()).thenReturn(errorMessageString);
        Mockito.when(mockPostResponse1.getContent()).thenReturn(postResponseContent1);
        JsonObject postBody = new JsonObject();
        Mockito.when(httpClient1.postJson(sdcUrl, postBody)).thenReturn(mockPostResponse1);

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.startRecording();
        });

        Assertions.assertEquals(user1UnableToStartErrorString, exception.getMessage());
        Assertions.assertEquals(
            "SDC recording did not return an SRR ID for user '"
            + user1String
            + onCicsRegionMsg
            + regionaApplid,
            exception.getCause().getMessage()
        );
    }

    @Test
    void testStartRecordingBadUri() throws IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException, InstantiationException,
            InvocationTargetException, NoSuchMethodException, ClassNotFoundException,
            SdvManagerException, CredentialsException, CicstsManagerException {
        // Mock SDVPort
        sdvPort.when(() -> SdvPort.get(regionaTag)).thenReturn("////gfg\\");

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setURI(Mockito.any());

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.startRecording();
        });

        Assertions.assertEquals(user1UnableToStartErrorString, exception.getMessage());
        Assertions.assertEquals(
            "Badly formed URI for SDC service for CICS Region "
            + regionaApplid,
            exception.getCause().getMessage()
        );
    }

    @SuppressWarnings(uncheckedString)
    @Test
    void testEndRecordingForMultipleRegionsAndUsers()
            throws SdvManagerException, CicstsManagerException, CredentialsException,
            ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, URISyntaxException, HttpClientException {

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        user1.setSrrId(srrId1);
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        user2.setSrrId(srrId2);
        rrA.addUserToRecord(user2);

        // Mock RegionB Terminal
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionB
        RecordingRegion rrB = new RecordingRegion(regionbTerminal);
        // Mock user 3
        ICredentialsUsernamePassword user3Creds =
                new CredentialsUsernamePassword(null, user3String, passwordString);
        ISdvUser user3 = new SdvUserImpl(creds3String, user3Creds, regionbTag, tellerRoleString);
        user3.setSrrId(srrId3);
        rrB.addUserToRecord(user3);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);
        recordingRegions.put(mockCicsbRegion, rrB);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock DELETE JSON return from SDC
        HttpClientResponse<JsonObject> mockDeleteResponse1 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockDeleteResponse1.getStatusCode()).thenReturn(200);
        JsonObject deleteBody = new JsonObject();
        deleteBody.addProperty(submitString, false);
        Mockito.when(httpClient1.deleteJson(sdcUrl, deleteBody)).thenReturn(mockDeleteResponse1);

        // Mock HTTP Client - 2nd time
        IHttpClient httpClient2 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient2)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient2).setAuthorisation(user1String, passwordString);
        // Mock DELETE JSON return from SDC
        HttpClientResponse<JsonObject> mockDeleteResponse2 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockDeleteResponse2.getStatusCode()).thenReturn(200);
        Mockito.when(httpClient2.deleteJson(sdcUrl, deleteBody)).thenReturn(mockDeleteResponse2);

        // Mock HTTP Client - 3rd time
        IHttpClient httpClient3 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient3)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient3).setAuthorisation(user1String, passwordString);
        // Mock DELETE JSON return from SDC
        HttpClientResponse<JsonObject> mockDeleteResponse3 = Mockito.mock(HttpClientResponse.class);
        Mockito.when(mockDeleteResponse3.getStatusCode()).thenReturn(200);
        Mockito.when(httpClient3.deleteJson(sdcUrl, deleteBody)).thenReturn(mockDeleteResponse3);

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1, httpClient2, httpClient3);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvHttpRecorder.endRecording();

        // Assert HTTP endpoints called
        Mockito.verify(httpClient1, Mockito.times(1)).deleteJson(Mockito.any(), Mockito.any());
        Mockito.verify(httpClient2, Mockito.times(1)).deleteJson(Mockito.any(), Mockito.any());
        Mockito.verify(httpClient3, Mockito.times(1)).deleteJson(Mockito.any(), Mockito.any());

        // Assert that all users are no longer recording
        Assertions.assertEquals(false, user1.isRecording());
        Assertions.assertEquals(false, user2.isRecording());
        Assertions.assertEquals(false, user3.isRecording());

    }

    @SuppressWarnings(uncheckedString)
    @Test
    void testEndRecordingDeleteServerError()
            throws SdvManagerException, CicstsManagerException, CredentialsException,
            ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, URISyntaxException, HttpClientException {

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        user1.setSrrId(srrId1);
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock DELETE JSON return from SDC
        HttpClientResponse<JsonObject> mockDeleteResponse1 = Mockito.mock(HttpClientResponse.class);
        JsonObject deleteResponseContent1 = new JsonObject();
        deleteResponseContent1.addProperty(srrIdString, nullString);
        deleteResponseContent1.addProperty(messagePropString, serverErrorMessage);
        Mockito.when(mockDeleteResponse1.getStatusCode()).thenReturn(500);
        Mockito.when(mockDeleteResponse1.getStatusMessage()).thenReturn(errorMessageString);
        Mockito.when(mockDeleteResponse1.getContent()).thenReturn(deleteResponseContent1);
        JsonObject deleteBody = new JsonObject();
        deleteBody.addProperty(submitString, false);
        Mockito.when(httpClient1.deleteJson(sdcUrl, deleteBody)).thenReturn(mockDeleteResponse1);

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.endRecording();
        });

        Assertions.assertEquals(
            "Unable to stop SRR recording "
            + srrId1
            + ", for user '"
            + user1String
            + onCicsRegionMsg
            + regionaApplid,
            exception.getMessage()
        );
        Assertions.assertEquals(
            "Could not stop SDC recording for user '"
            + user1String
            + onCicsRegionMsg
            + regionaApplid
            + ". Status code: 500"
            + errorOutputBadStuffString
            + errorOutputPayloadStrng,
            exception.getCause().getMessage()
        );
    }

    @SuppressWarnings(uncheckedString)
    @Test
    void testEndRecordingDeleteHttpClientException()
            throws SdvManagerException, CicstsManagerException, CredentialsException,
            ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, URISyntaxException, HttpClientException {

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        user1.setSrrId(srrId1);
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1)
                .setURI(new URI(httpString + cicsServerStringA + ":" + regionAportString));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setAuthorisation(user1String, passwordString);
        // Mock DELETE JSON return from SDC
        JsonObject deleteBody = new JsonObject();
        deleteBody.addProperty(submitString, false);
        Mockito.when(httpClient1.deleteJson(sdcUrl, deleteBody))
                .thenThrow(new HttpClientException("Nothing back"));

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.endRecording();
        });

        Assertions.assertEquals(
            "Unable to stop SRR recording "
            + srrId1
            + ", for user '"
            + user1String
            + onCicsRegionMsg
            + regionaApplid,
            exception.getMessage()
        );
        Assertions.assertEquals(
            "Could not stop existing SDC recording for user '"
            + user1String
            + onCicsRegionMsg
            + regionaApplid,
            exception.getCause().getMessage()
        );
    }

    @Test
    void testEndRecordingBadUri() throws CredentialsException, SdvManagerException,
            ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException {
        // Mock SDVPort
        sdvPort.when(() -> SdvPort.get(regionaTag)).thenReturn("////gfg\\");

        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        user1.setSrrId(srrId1);
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock HTTP Client - 1st time
        IHttpClient httpClient1 = Mockito.mock(IHttpClient.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(httpClient1).setURI(Mockito.any());

        // Mock HTTP Manager
        IHttpManagerSpi httpManager = Mockito.mock(IHttpManagerSpi.class);
        Mockito.when(httpManager.newHttpClient()).thenReturn(httpClient1);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, null, null, null, null, httpManager);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.endRecording();
        });

        Assertions.assertEquals(
            "Unable to stop SRR recording "
            + srrId1
            + ", for user '"
            + user1String
            + onCicsRegionMsg
            + regionaApplid,
            exception.getMessage()
        );
        Assertions.assertEquals(
            "Badly formed URI for SDC service for CICS Region "
            + regionaApplid,
            exception.getCause().getMessage()
        );
    }

    @Test
    void testExportRecordingsForMulitpleRegions()
            throws CredentialsException, SdvManagerException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException,
            NoSuchFieldException, TestBundleResourceException, IOException, ZosBatchException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        user1.setSrrId(srrId1);
        user1.setNotRecording();
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        user2.setSrrId(srrId2);
        user2.setNotRecording();
        rrA.addUserToRecord(user2);

        // Mock RegionA Terminal
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrB = new RecordingRegion(regionbTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user3Creds =
                new CredentialsUsernamePassword(null, user3String, passwordString);
        ISdvUser user3 = new SdvUserImpl(creds3String, user3Creds, regionbTag, tellerRoleString);
        user3.setSrrId(srrId3);
        user3.setNotRecording();
        rrB.addUserToRecord(user3);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);
        recordingRegions.put(mockCicsbRegion, rrB);

        String getYamlJcl =
                IOUtils.toString(this.getClass().getResourceAsStream(getYamlString), utfString);

        String getYamlAppendedRegionaJcl = IOUtils
                .toString(this.getClass().getResourceAsStream("/getYamlRegionA.jcl"), utfString);

        String getYamlAppendedRegionbJcl = IOUtils
                .toString(this.getClass().getResourceAsStream("/getYamlRegionB.jcl"), utfString);

        String regionAyaml = IOUtils
                .toString(this.getClass().getResourceAsStream("/yamlRegionA.yaml"), utfString);

        String regionByaml = IOUtils
                .toString(this.getClass().getResourceAsStream("/yamlRegionB.yaml"), utfString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(Mockito.eq(jclGetYamlPathString),
                Mockito.any())).thenReturn(getYamlJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJobA = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJobA.waitForJob()).thenReturn(0);
        IZosBatchJob zosJobB = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJobB.waitForJob()).thenReturn(0);
        IZosBatchJobOutput batchJobOutputRegionA = Mockito.mock(IZosBatchJobOutput.class);
        IZosBatchJobOutput batchJobOutputRegionB = Mockito.mock(IZosBatchJobOutput.class);
        List<IZosBatchJobOutputSpoolFile> spoolFilesRegionA = new ArrayList<>();
        IZosBatchJobOutputSpoolFile spoolYamlFileRegionA = new ZosBatchJobOutputSpoolFileImpl(
                zosJobA, "JobA", "ID123", "STEPA", "PROCA", "YAML", "SECA", regionAyaml);
        spoolFilesRegionA.add(spoolYamlFileRegionA);
        List<IZosBatchJobOutputSpoolFile> spoolFilesRegionB = new ArrayList<>();
        IZosBatchJobOutputSpoolFile spoolYamlFileRegionB = new ZosBatchJobOutputSpoolFileImpl(
                zosJobB, "JobB", "ID234", "STEPB", "PROCB", "YAML", "SECB", regionByaml);
        spoolFilesRegionB.add(spoolYamlFileRegionB);
        Mockito.when(batchJobOutputRegionA.getSpoolFiles()).thenReturn(spoolFilesRegionA);
        Mockito.when(batchJobOutputRegionB.getSpoolFiles()).thenReturn(spoolFilesRegionB);
        Mockito.when(zosJobA.retrieveOutput()).thenReturn(batchJobOutputRegionA);
        Mockito.when(zosJobB.retrieveOutput()).thenReturn(batchJobOutputRegionB);
        IZosBatch zosBatchRegionA = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatchRegionA.submitJob(getYamlAppendedRegionaJcl, null))
                .thenReturn(zosJobA);
        IZosBatch zosBatchRegionB = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatchRegionB.submitJob(getYamlAppendedRegionbJcl, null))
                .thenReturn(zosJobB);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatchRegionA);
        Mockito.when(batchManager.getZosBatch(regionbImage)).thenReturn(zosBatchRegionB);

        Path storedArtifactRoot = Mockito.mock(Path.class);
        Path finalYamlPath = Mockito.mock(Path.class);
        Mockito.when(storedArtifactRoot.resolve("bundleB/TestClassB.CICSA.cics-security.yaml"))
                .thenReturn(finalYamlPath);
        Mockito.when(storedArtifactRoot.resolve("bundleB/TestClassB.CICSB.cics-security.yaml"))
                .thenReturn(finalYamlPath);
        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, artifactManager, batchManager,
                        storedArtifactRoot, null, null);

        files.when(() -> Files.write(Mockito.eq(finalYamlPath),
                Mockito.eq(regionAyaml.getBytes(utfString)), Mockito.any())).thenReturn(null);

        files.when(() -> Files.write(Mockito.eq(finalYamlPath),
                Mockito.eq(regionByaml.getBytes(utfString)), Mockito.any())).thenReturn(null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        sdvHttpRecorder.exportRecordings("bundleB", "TestClassB");

        // If these write calls are made, we can say all mocks were used with
        // their expected attributes and all works
        files.verify(() -> Files.write(finalYamlPath, regionAyaml.getBytes(utfString),
                StandardOpenOption.CREATE), Mockito.times(1));

        files.verify(() -> Files.write(finalYamlPath, regionByaml.getBytes(utfString),
                StandardOpenOption.CREATE), Mockito.times(1));

        Mockito.verify(mockLog, Mockito.times(1))
                .info("Storing YAML as test artifact for " + regionaApplid);

        Mockito.verify(mockLog, Mockito.times(1))
                .info("Storing YAML as test artifact for " + regionbApplid);
    }

    @Test
    void testExportRecordingsWithBadSrrIdForRecording()
            throws CredentialsException, SdvManagerException, IOException,
            TestBundleResourceException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        user1.setSrrId(null);
        user1.setNotRecording();
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        String getYamlJcl =
                IOUtils.toString(this.getClass().getResourceAsStream(getYamlString), utfString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(Mockito.eq(jclGetYamlPathString),
                Mockito.any())).thenReturn(getYamlJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, artifactManager, null, null, null, null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        files.when(() -> Files.write(Mockito.any(Path.class), Mockito.any(byte[].class),
                Mockito.any(OpenOption.class))).thenReturn(null);

        sdvHttpRecorder.exportRecordings("bundleC", "TestClassC");

        files.verify(() -> Files.write(Mockito.any(Path.class), Mockito.any(byte[].class),
                Mockito.any(OpenOption.class)), Mockito.times(0));

        // Check there is a warning in the log indicating Region A won't record
        Mockito.verify(mockLog).warn("No SDC registered for user " + user1String + " on region "
                + regionaApplid + ", skipping YAML generation.");

    }

    @Test
    void testExportRecordingsYamlJobErrorCode()
            throws CredentialsException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException, SdvManagerException,
            ZosBatchException, IOException, TestBundleResourceException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        user1.setSrrId(srrId1);
        user1.setNotRecording();
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        String getYamlJcl =
                IOUtils.toString(this.getClass().getResourceAsStream(getYamlString), utfString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(Mockito.eq(jclGetYamlPathString),
                Mockito.any())).thenReturn(getYamlJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJobA = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJobA.waitForJob()).thenReturn(12);
        IZosBatchJobOutput batchJobOutputRegionA = Mockito.mock(IZosBatchJobOutput.class);
        List<IZosBatchJobOutputSpoolFile> spoolFilesRegionA = new ArrayList<>();
        Mockito.when(batchJobOutputRegionA.getSpoolFiles()).thenReturn(spoolFilesRegionA);
        Mockito.when(zosJobA.retrieveOutput()).thenReturn(batchJobOutputRegionA);
        IZosBatch zosBatchRegionA = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatchRegionA.submitJob(Mockito.any(), Mockito.eq(null)))
                .thenReturn(zosJobA);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatchRegionA);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, artifactManager, batchManager, null, null,
                        null);

        files.when(() -> Files.write(Mockito.any(Path.class), Mockito.any(byte[].class),
                Mockito.any(OpenOption.class))).thenReturn(null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.exportRecordings("bundleD", "TestClassD");
        });

        Assertions.assertEquals(
            "Security metadata job did not return any YAML for CICS Region "
            + regionaApplid
            + ", containing SRR IDs: "
            + srrId1,
            exception.getMessage()
        );

        files.verify(() -> Files.write(Mockito.any(Path.class), Mockito.any(byte[].class),
                Mockito.any(OpenOption.class)), Mockito.times(0));

        Mockito.verify(mockLog, Mockito.times(1)).error(
            "JCL to get Security metadata fail on CICS Region "
            + regionaApplid
            + ", check artifacts for more details"
        );
    }

    @Test
    void testExportRecordingsArtifactFindException() throws CredentialsException,
            SdvManagerException, IOException, TestBundleResourceException, ZosBatchException,
            ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        user1.setSrrId(srrId1);
        user1.setNotRecording();
        rrA.addUserToRecord(user1);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(Mockito.eq(jclGetYamlPathString),
                Mockito.any())).thenThrow(new TestBundleResourceException("cant find files"));
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, artifactManager, null, null, null, null);

        files.when(() -> Files.write(Mockito.any(Path.class), Mockito.any(byte[].class),
                Mockito.any(OpenOption.class))).thenReturn(null);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.exportRecordings("bundleE", "TestClassE");
        });

        Assertions.assertEquals(
            "Unable to run JCL to get Security metadata on CICS Region "
            + regionaApplid,
            exception.getMessage()
        );

        files.verify(() -> Files.write(Mockito.any(Path.class), Mockito.any(byte[].class),
                Mockito.any(OpenOption.class)), Mockito.times(0));
    }

    @Test
    void testExportRecordingsUnableToSaveYamlFile() throws CredentialsException,
            SdvManagerException, IOException, TestBundleResourceException, ZosBatchException,
            ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException {
        // Mock RegionA Terminal
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        // Mock RecordingRegionA
        RecordingRegion rrA = new RecordingRegion(regionaTerminal);
        // Mock user 1
        ICredentialsUsernamePassword user1Creds =
                new CredentialsUsernamePassword(null, user1String, passwordString);
        ISdvUser user1 = new SdvUserImpl(creds1String, user1Creds, regionaTag, tellerRoleString);
        user1.setSrrId(srrId1);
        user1.setNotRecording();
        rrA.addUserToRecord(user1);
        // Mock user 2
        ICredentialsUsernamePassword user2Creds =
                new CredentialsUsernamePassword(null, user2String, passwordString);
        ISdvUser user2 = new SdvUserImpl(creds2String, user2Creds, regionaTag, adminRoleString);
        user2.setSrrId(srrId2);
        user2.setNotRecording();
        rrA.addUserToRecord(user2);

        // Mock recordingRegions
        Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();
        recordingRegions.put(mockCicsaRegion, rrA);

        String getYamlJcl =
                IOUtils.toString(this.getClass().getResourceAsStream(getYamlString), utfString);

        String getYamlAppendedRegionaJcl = IOUtils
                .toString(this.getClass().getResourceAsStream("/getYamlRegionA.jcl"), utfString);

        String regionAyaml = IOUtils
                .toString(this.getClass().getResourceAsStream("/yamlRegionA.yaml"), utfString);

        // Mock artifactManager
        IBundleResources bundleResources = Mockito.mock(IBundleResources.class);
        Mockito.when(bundleResources.retrieveSkeletonFileAsString(Mockito.eq(jclGetYamlPathString),
                Mockito.any())).thenReturn(getYamlJcl);
        IArtifactManager artifactManager = Mockito.mock(IArtifactManager.class);
        Mockito.when(artifactManager.getBundleResources(SdvHttpRecorderImpl.class))
                .thenReturn(bundleResources);

        // Mock batchManager
        IZosBatchJob zosJobA = Mockito.mock(IZosBatchJob.class);
        Mockito.when(zosJobA.waitForJob()).thenReturn(0);
        IZosBatchJobOutput batchJobOutputRegionA = Mockito.mock(IZosBatchJobOutput.class);
        List<IZosBatchJobOutputSpoolFile> spoolFilesRegionA = new ArrayList<>();
        IZosBatchJobOutputSpoolFile spoolYamlFileRegionA = new ZosBatchJobOutputSpoolFileImpl(
                zosJobA, "JobA", "ID123", "STEPA", "PROCA", "YAML", "SECA", regionAyaml);
        spoolFilesRegionA.add(spoolYamlFileRegionA);
        Mockito.when(batchJobOutputRegionA.getSpoolFiles()).thenReturn(spoolFilesRegionA);
        Mockito.when(zosJobA.retrieveOutput()).thenReturn(batchJobOutputRegionA);
        IZosBatch zosBatchRegionA = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatchRegionA.submitJob(getYamlAppendedRegionaJcl, null))
                .thenReturn(zosJobA);
        IZosBatchSpi batchManager = Mockito.mock(IZosBatchSpi.class);
        Mockito.when(batchManager.getZosBatch(regionaImage)).thenReturn(zosBatchRegionA);

        Path storedArtifactRoot = Mockito.mock(Path.class);
        Path finalYamlPath = Paths.get("/c/dir/" + "bundleF/TestClassF.CICSA.cics-security.yaml");
        Mockito.when(storedArtifactRoot.resolve("bundleF/TestClassF.CICSA.cics-security.yaml"))
                .thenReturn(finalYamlPath);
        Mockito.when(storedArtifactRoot.resolve("bundleF/TestClassF.CICSB.cics-security.yaml"))
                .thenReturn(finalYamlPath);
        // Mock SdvRecorderImpl
        Class<?> sdvHttpRecorderImplClass = Class.forName(sdvHttpRecorderImplClassString);
        SdvHttpRecorderImpl sdvHttpRecorder = (SdvHttpRecorderImpl) sdvHttpRecorderImplClass
                .getDeclaredConstructor(IFramework.class, Map.class, IArtifactManager.class,
                        IZosBatchSpi.class, Path.class, IDynamicStatusStoreService.class,
                        IHttpManagerSpi.class)
                .newInstance(null, recordingRegions, artifactManager, batchManager,
                        storedArtifactRoot, null, null);

        files.when(() -> Files.write(Mockito.eq(finalYamlPath),
                Mockito.eq(regionAyaml.getBytes(utfString)), Mockito.any()))
                .thenThrow(new IOException("path not there"));

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvHttpRecorderImplClass.getDeclaredField(logVariableString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        final Field superLoggerField = sdvHttpRecorderImplClass.getSuperclass()
                .getDeclaredField(logVariableString);
        final Object staticSuperLoggerFieldBase = unsafe.staticFieldBase(superLoggerField);
        final long staticSuperLoggerFieldOffset = unsafe.staticFieldOffset(superLoggerField);
        unsafe.putObject(staticSuperLoggerFieldBase, staticSuperLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvHttpRecorder.exportRecordings("bundleF", "TestClassF");
        });

        Assertions.assertEquals(
            "Unable to add YAML to Galasa run for CICS Region APPL1. "
            + "Attempting to save to path: /c/dir/bundleF/TestClassF.CICSA.cics-security.yaml",
            exception.getMessage()
        );

    }

}
