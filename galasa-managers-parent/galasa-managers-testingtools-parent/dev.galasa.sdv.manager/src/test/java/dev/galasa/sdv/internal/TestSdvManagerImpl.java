/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;
import dev.galasa.artifact.internal.ArtifactManagerImpl;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.internal.CicstsManagerImpl;
import dev.galasa.cicsts.spi.ICicsRegionProvisioned;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.http.internal.HttpManagerImpl;
import dev.galasa.sdv.ISdvUser;
import dev.galasa.sdv.SdvManagerException;
import dev.galasa.sdv.SdvUser;
import dev.galasa.sdv.internal.properties.SdvHlq;
import dev.galasa.sdv.internal.properties.SdvPort;
import dev.galasa.sdv.internal.properties.SdvRole;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosbatch.IZosBatchJob;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sun.misc.Unsafe;


class TestSdvManagerImpl {

    private MockedStatic<SdvRole> sdvRole;
    private MockedStatic<SdvPort> sdvPort;
    private MockedStatic<SdvHlq> sdvHlq;
    private MockedStatic<SdvUserPool> sdvUserPoolStatic;


    @SuppressWarnings("PMD")
    private static final Log mockLog = Mockito.mock(Log.class);

    private String testCicsTagA = "CICSA";
    private String testCicsTagB = "CICSB";
    private String testCicsTagC = "CICSC";
    private String roleNameTeller = "TELLER";
    private String roleNameAdmin = "ADMIN";
    private String roleNameOperator = "OPERATOR";
    private String testPort = "32000";
    private String sdvManagerImplClassString = "dev.galasa.sdv.internal.SdvManagerImpl";
    private String privateCicsManagerVariableName = "cicsManager";
    private String creds1Tag = "CREDS1";
    private String creds2Tag = "CREDS2";
    private String creds3Tag = "CREDS3";
    private String creds4Tag = "CREDS4";
    private String user1String = "user1";
    private String user2String = "user2";
    private String user3String = "user3";
    private String user4String = "user4";
    private String password1 = "password1";
    private String password2 = "password2";
    private String password3 = "password3";
    private String password4 = "password4";
    private String secOnMsg = "blah\n\nDFHXS1100I: Security initialization has started.\nblah";
    private String testClassString = "testClass";
    private String sdvUsersToRecordListString = "sdvUsersToRecordList";
    private String recordingRegionsString = "recordingRegions";
    private static final String uncheckedString = "unchecked";
    private String frameworkString = "framework";
    private String sdvRecorderVarName = "sdvRecorder";
    private String logString = "LOG";
    private String theUnsafeString = "theUnsafe";
    private String regionaApplid = "APPL1";
    private String runName = "RUN123";
    private String regionbApplid = "APPL2";

    @BeforeAll
    public static void beforeClass() {
        Mockito.when(mockLog.isInfoEnabled()).thenReturn(true);
        Mockito.when(mockLog.isWarnEnabled()).thenReturn(true);
        Mockito.when(mockLog.isErrorEnabled()).thenReturn(true);
    }

    @BeforeEach
    public void setUp() {
        // Registering static mocks before each test
        sdvRole = Mockito.mockStatic(SdvRole.class);
        sdvPort = Mockito.mockStatic(SdvPort.class);
        sdvHlq = Mockito.mockStatic(SdvHlq.class);
        sdvUserPoolStatic = Mockito.mockStatic(SdvUserPool.class);
    }

    @AfterEach
    public void tearDown() {
        // Closing static mocks after each test
        sdvRole.close();
        sdvPort.close();
        sdvHlq.close();
        sdvUserPoolStatic.close();
    }

    @Test
    void testGetSdvUser() throws CredentialsException, SdvManagerException, ClassNotFoundException,
            NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, InstantiationException, InvocationTargetException,
            NoSuchMethodException, ResourceUnavailableException {
        // Create variables for common values used throughout test
        String roleTag = "R1";
        String username = user1String;

        // Mocks for statics
        sdvPort.when(() -> SdvPort.get(testCicsTagA)).thenReturn(testPort);
        sdvHlq.when(() -> SdvHlq.get(testCicsTagA)).thenReturn("CICS.INSTALL");
        sdvRole.when(() -> SdvRole.get(roleTag)).thenReturn(roleNameTeller);

        // Mock ISdvUser annotation
        Field testField = Mockito.mock(Field.class);
        SdvUser sdvUser = Mockito.mock(SdvUser.class);
        Mockito.when(sdvUser.cicsTag()).thenReturn(testCicsTagA);
        Mockito.when(sdvUser.roleTag()).thenReturn(roleTag);
        Mockito.when(testField.getAnnotation(SdvUser.class)).thenReturn(sdvUser);

        // Mock check for CICS region
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);
        Map<String, ICicsRegionProvisioned> mockCicsRegionList = new HashMap<>();

        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getTag()).thenReturn(testCicsTagA);

        mockCicsRegionList.put(testCicsTagA, mockCicsaRegion);
        Mockito.when(cicsManager.getTaggedCicsRegions()).thenReturn(mockCicsRegionList);

        // Replace private cicsManager instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);

        // Mock sdvUserPool
        SdvUserPool sdvUserPool = Mockito.mock(SdvUserPool.class);
        Mockito.when(sdvUserPool.allocateUser(roleNameTeller, mockCicsaRegion))
                .thenReturn(creds1Tag);
        Field sdvUserPoolField = sdvManagerImplClass.getDeclaredField("sdvUserPool");
        sdvUserPoolField.setAccessible(true);
        sdvUserPoolField.set(sdvManager, sdvUserPool);

        // Mock getFramework().getCredentialsService()
        ICredentialsUsernamePassword testCreds =
                new CredentialsUsernamePassword(null, username, password1);
        ICredentialsService credService = Mockito.mock(ICredentialsService.class);
        Mockito.when(credService.getCredentials(creds1Tag)).thenReturn(testCreds);
        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getCredentialsService()).thenReturn(credService);
        Field frameworkField =
                sdvManagerImplClass.getSuperclass().getDeclaredField(frameworkString);
        frameworkField.setAccessible(true);
        frameworkField.set(sdvManager, framework);

        // Mock cts
        IConfidentialTextService cts = Mockito.mock(IConfidentialTextService.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cts).registerText(password1, "Password for credential tag: " + creds1Tag);
        Field ctsField = sdvManagerImplClass.getDeclaredField("cts");
        ctsField.setAccessible(true);
        ctsField.set(sdvManager, cts);

        // Make call to funtion under test
        ISdvUser resultUser = sdvManager.getSdvUser(testField, null);

        Assertions.assertEquals(creds1Tag, resultUser.getCredentialsTag());
        Assertions.assertEquals(testCicsTagA, resultUser.getCicsTag());
        Assertions.assertEquals(password1, resultUser.getPassword());
        Assertions.assertEquals(roleNameTeller, resultUser.getRole());
        Assertions.assertEquals(username, resultUser.getUsername());
        Assertions.assertEquals(false, resultUser.isRecording());
    }

    @Test
    void testGetSdvUserBlankRoleTag() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        // Create variables for common values used throughout test
        String roleTag = "";

        // Mock ISdvUser annotation
        Field testField = Mockito.mock(Field.class);
        SdvUser sdvUser = Mockito.mock(SdvUser.class);
        Mockito.when(sdvUser.cicsTag()).thenReturn(testCicsTagA);
        Mockito.when(sdvUser.roleTag()).thenReturn(roleTag);
        Mockito.when(testField.getAnnotation(SdvUser.class)).thenReturn(sdvUser);
        Mockito.when(testField.getName()).thenReturn("testUser");

        // Get an SdvManager instance
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvManager.getSdvUser(testField, null);
        });

        Assertions.assertEquals("SdvUser testUser cannot have a blank RoleTag.",
                exception.getMessage());
    }

    @Test
    void testGetSdvUserNoMatchingRole() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        // Create variables for common values used throughout test
        String roleTag = "R1";

        // Mocks for statics
        sdvRole.when(() -> SdvRole.get(roleTag)).thenReturn(null);

        // Mock ISdvUser annotation
        Field testField = Mockito.mock(Field.class);
        SdvUser sdvUser = Mockito.mock(SdvUser.class);
        Mockito.when(sdvUser.cicsTag()).thenReturn(testCicsTagA);
        Mockito.when(sdvUser.roleTag()).thenReturn(roleTag);
        Mockito.when(testField.getAnnotation(SdvUser.class)).thenReturn(sdvUser);

        // Get an SdvManager instance
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvManager.getSdvUser(testField, null);
        });

        Assertions
                .assertEquals("Cannot find role. Please create or update CPS Property 'sdv.roleTag."
                        + roleTag + ".role'.", exception.getMessage());
    }

    @Test
    void testGetSdvUserNoMatchingCicsRegion() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Create variables for common values used throughout test
        String roleTag = "R1";

        // Mocks for statics
        sdvRole.when(() -> SdvRole.get(roleTag)).thenReturn(roleNameTeller);

        // Mock ISdvUser annotation
        Field testField = Mockito.mock(Field.class);
        SdvUser sdvUser = Mockito.mock(SdvUser.class);
        Mockito.when(sdvUser.cicsTag()).thenReturn(testCicsTagA);
        Mockito.when(sdvUser.roleTag()).thenReturn(roleTag);
        Mockito.when(testField.getAnnotation(SdvUser.class)).thenReturn(sdvUser);
        Mockito.when(testField.getName()).thenReturn("testUser");

        // Mock check for CICS region
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);
        Map<String, ICicsRegionProvisioned> mockCicsRegionList = new HashMap<>();
        Mockito.when(cicsManager.getTaggedCicsRegions()).thenReturn(mockCicsRegionList);

        // Replace private cicsManager instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvManager.getSdvUser(testField, null);
        });

        Assertions.assertEquals("Unable to setup SDV User 'testUser', for region with tag '"
                + testCicsTagA + "' as a region with a matching 'cicsTag' tag was not found,"
                + " or the region was not provisioned.", exception.getMessage());
    }

    @Test
    void testGetSdvUserNoPort() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Create variables for common values used throughout test
        String roleTag = "R1";

        // Mocks for statics
        sdvPort.when(() -> SdvPort.get(testCicsTagA)).thenReturn(null);
        sdvRole.when(() -> SdvRole.get(roleTag)).thenReturn(roleNameTeller);

        // Mock ISdvUser annotation
        Field testField = Mockito.mock(Field.class);
        SdvUser sdvUser = Mockito.mock(SdvUser.class);
        Mockito.when(sdvUser.cicsTag()).thenReturn(testCicsTagA);
        Mockito.when(sdvUser.roleTag()).thenReturn(roleTag);
        Mockito.when(testField.getAnnotation(SdvUser.class)).thenReturn(sdvUser);
        Mockito.when(testField.getName()).thenReturn("testUser");

        // Mock check for CICS region
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);
        Map<String, ICicsRegionProvisioned> mockCicsRegionList = new HashMap<>();

        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getTag()).thenReturn(testCicsTagA);

        mockCicsRegionList.put(testCicsTagA, mockCicsaRegion);
        Mockito.when(cicsManager.getTaggedCicsRegions()).thenReturn(mockCicsRegionList);

        // Replace private cicsManager instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvManager.getSdvUser(testField, null);
        });

        Assertions.assertEquals(
                "Could not find port. Please create or update CPS property 'sdv.cicsTag."
                        + testCicsTagA + ".port'.",
                exception.getMessage());
    }

    @Test
    void testGetSdvUserNoHlq()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, ResourceUnavailableException {
        // Create variables for common values used throughout test
        String roleTag = "R1";

        // Mocks for statics
        sdvPort.when(() -> SdvPort.get(testCicsTagA)).thenReturn(testPort);
        sdvHlq.when(() -> SdvHlq.get(testCicsTagA)).thenReturn(null);
        sdvRole.when(() -> SdvRole.get(roleTag)).thenReturn(roleNameTeller);

        // Mock ISdvUser annotation
        Field testField = Mockito.mock(Field.class);
        SdvUser sdvUser = Mockito.mock(SdvUser.class);
        Mockito.when(sdvUser.cicsTag()).thenReturn(testCicsTagA);
        Mockito.when(sdvUser.roleTag()).thenReturn(roleTag);
        Mockito.when(testField.getAnnotation(SdvUser.class)).thenReturn(sdvUser);

        // Mock check for CICS region
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);
        Map<String, ICicsRegionProvisioned> mockCicsRegionList = new HashMap<>();

        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getTag()).thenReturn(testCicsTagA);

        mockCicsRegionList.put(testCicsTagA, mockCicsaRegion);
        Mockito.when(cicsManager.getTaggedCicsRegions()).thenReturn(mockCicsRegionList);

        // Replace private cicsManager instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvManager.getSdvUser(testField, null);
        });

        Assertions.assertEquals(
                "Could not find HLQ. Please create or update CPS property 'sdv.cicsTag."
                        + testCicsTagA + ".hlq'.",
                exception.getMessage());
    }

    @Test
    void testGetSdvUserUserAllocationException() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException,
            NoSuchFieldException, SdvManagerException, ResourceUnavailableException {
        // Create variables for common values used throughout test
        String roleTag = "R1";

        // Mocks for statics
        sdvPort.when(() -> SdvPort.get(testCicsTagA)).thenReturn(testPort);
        sdvHlq.when(() -> SdvHlq.get(testCicsTagA)).thenReturn("CICS.INSTALL");
        sdvRole.when(() -> SdvRole.get(roleTag)).thenReturn(roleNameTeller);

        // Mock ISdvUser annotation
        Field testField = Mockito.mock(Field.class);
        SdvUser sdvUser = Mockito.mock(SdvUser.class);
        Mockito.when(sdvUser.cicsTag()).thenReturn(testCicsTagA);
        Mockito.when(sdvUser.roleTag()).thenReturn(roleTag);
        Mockito.when(testField.getAnnotation(SdvUser.class)).thenReturn(sdvUser);

        // Mock check for CICS region
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);
        Map<String, ICicsRegionProvisioned> mockCicsRegionList = new HashMap<>();

        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getTag()).thenReturn(testCicsTagA);

        mockCicsRegionList.put(testCicsTagA, mockCicsaRegion);
        Mockito.when(cicsManager.getTaggedCicsRegions()).thenReturn(mockCicsRegionList);

        // Replace private cicsManager instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);

        // Mock sdvUserPool
        SdvUserPool sdvUserPool = Mockito.mock(SdvUserPool.class);
        Mockito.when(sdvUserPool.allocateUser(roleNameTeller, mockCicsaRegion))
                .thenThrow(new ResourceUnavailableException("No users available"));
        Field sdvUserPoolField = sdvManagerImplClass.getDeclaredField("sdvUserPool");
        sdvUserPoolField.setAccessible(true);
        sdvUserPoolField.set(sdvManager, sdvUserPool);

        // Make call to funtion under test
        ResourceUnavailableException exception =
                Assertions.assertThrows(ResourceUnavailableException.class, () -> {
                    sdvManager.getSdvUser(testField, null);
                });

        Assertions.assertEquals("No users available", exception.getMessage());
    }

    @Test
    void testGetSdvUserCredentialsNotFound() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, SdvManagerException,
            ResourceUnavailableException, CredentialsException, ClassNotFoundException {
        // Create variables for common values used throughout test
        String roleTag = "R1";

        // Mocks for statics
        sdvPort.when(() -> SdvPort.get(testCicsTagA)).thenReturn(testPort);
        sdvHlq.when(() -> SdvHlq.get(testCicsTagA)).thenReturn("CICS.INSTALL");
        sdvRole.when(() -> SdvRole.get(roleTag)).thenReturn(roleNameTeller);

        // Mock ISdvUser annotation
        Field testField = Mockito.mock(Field.class);
        SdvUser sdvUser = Mockito.mock(SdvUser.class);
        Mockito.when(sdvUser.cicsTag()).thenReturn(testCicsTagA);
        Mockito.when(sdvUser.roleTag()).thenReturn(roleTag);
        Mockito.when(testField.getAnnotation(SdvUser.class)).thenReturn(sdvUser);

        // Mock check for CICS region
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);
        Map<String, ICicsRegionProvisioned> mockCicsRegionList = new HashMap<>();

        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getTag()).thenReturn(testCicsTagA);

        mockCicsRegionList.put(testCicsTagA, mockCicsaRegion);
        Mockito.when(cicsManager.getTaggedCicsRegions()).thenReturn(mockCicsRegionList);

        // Replace private cicsManager instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);

        // Mock sdvUserPool
        SdvUserPool sdvUserPool = Mockito.mock(SdvUserPool.class);
        Mockito.when(sdvUserPool.allocateUser(roleNameTeller, mockCicsaRegion))
                .thenReturn(creds1Tag);
        Field sdvUserPoolField = sdvManagerImplClass.getDeclaredField("sdvUserPool");
        sdvUserPoolField.setAccessible(true);
        sdvUserPoolField.set(sdvManager, sdvUserPool);

        // Mock getFramework().getCredentialsService()
        ICredentialsService credService = Mockito.mock(ICredentialsService.class);
        Mockito.when(credService.getCredentials(creds1Tag))
                .thenThrow(new CredentialsException("Did not find user"));
        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getCredentialsService()).thenReturn(credService);
        Field frameworkField =
                sdvManagerImplClass.getSuperclass().getDeclaredField(frameworkString);
        frameworkField.setAccessible(true);
        frameworkField.set(sdvManager, framework);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvManager.getSdvUser(testField, null);
        });

        Assertions.assertEquals("No credentials were found with the tag: " + creds1Tag,
                exception.getMessage());
    }

    @Test
    void testProvisionGenerate() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException,
            ResourceUnavailableException, ManagerException, CredentialsException {

        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);
        Map<String, ICicsRegionProvisioned> mockCicsRegionList = new HashMap<>();

        // Mock for CICS region A
        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getTag()).thenReturn(testCicsTagA);
        // Mock region Product version
        ProductVersion mockProductVersion = Mockito.mock(ProductVersion.class);
        Mockito.when(mockProductVersion.isEarlierThan(ProductVersion.v(750))).thenReturn(false);
        Mockito.when(mockCicsaRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        IZosBatchJob mockIzOsBatchJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(mockIzOsBatchJob.retrieveOutputAsString()).thenReturn(secOnMsg);
        Mockito.when(mockCicsaRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagA, mockCicsaRegion);

        // Mock for CICS region B
        ICicsRegionProvisioned mockCicsbRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsbRegion.getTag()).thenReturn(testCicsTagA);
        // Mock region Product version
        Mockito.when(mockCicsbRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        Mockito.when(mockCicsbRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagB, mockCicsbRegion);

        // Mock for CICS region C
        ICicsRegionProvisioned mockCicscRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicscRegion.getTag()).thenReturn(testCicsTagA);
        // Mock region Product version
        Mockito.when(mockCicscRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        Mockito.when(mockCicscRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagC, mockCicscRegion);

        Mockito.when(cicsManager.getTaggedCicsRegions()).thenReturn(mockCicsRegionList);

        // Mock Terminals
        ICicsTerminal regionaTerminal = Mockito.mock(ICicsTerminal.class);
        Mockito.when(cicsManager.generateCicsTerminal(testCicsTagA)).thenReturn(regionaTerminal);
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        Mockito.when(cicsManager.generateCicsTerminal(testCicsTagB)).thenReturn(regionbTerminal);
        ICicsTerminal regioncTerminal = Mockito.mock(ICicsTerminal.class);
        Mockito.when(cicsManager.generateCicsTerminal(testCicsTagC)).thenReturn(regioncTerminal);

        // Replace private cicsManager instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);
        // Replace testClass to bypass generateAnnotatedFields
        Field testClass = sdvManagerImplClass.getSuperclass().getDeclaredField(testClassString);
        testClass.setAccessible(true);
        testClass.set(sdvManager, null);
        // Replace sdvUsersToRecordList with a mocked list
        List<ISdvUser> listOfUsersForAllRegions = new ArrayList<>();
        // user1
        ICredentialsUsernamePassword testCreds1 =
                new CredentialsUsernamePassword(null, user1String, password1);
        SdvUserImpl newSdvUser1 =
                new SdvUserImpl(creds1Tag, testCreds1, testCicsTagA, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser1);
        // user2
        ICredentialsUsernamePassword testCreds2 =
                new CredentialsUsernamePassword(null, user2String, password2);
        SdvUserImpl newSdvUser2 =
                new SdvUserImpl(creds2Tag, testCreds2, testCicsTagB, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser2);
        // user3
        ICredentialsUsernamePassword testCreds3 =
                new CredentialsUsernamePassword(null, user3String, password3);
        SdvUserImpl newSdvUser3 =
                new SdvUserImpl(creds3Tag, testCreds3, testCicsTagA, roleNameAdmin);
        listOfUsersForAllRegions.add(newSdvUser3);
        // user4
        ICredentialsUsernamePassword testCreds4 =
                new CredentialsUsernamePassword(null, user4String, password4);
        SdvUserImpl newSdvUser4 =
                new SdvUserImpl(creds4Tag, testCreds4, testCicsTagC, roleNameOperator);
        listOfUsersForAllRegions.add(newSdvUser4);

        Field sdvUsersToRecordList =
                sdvManagerImplClass.getDeclaredField(sdvUsersToRecordListString);
        sdvUsersToRecordList.setAccessible(true);
        sdvUsersToRecordList.set(sdvManager, listOfUsersForAllRegions);

        // Make call to funtion under test
        sdvManager.provisionGenerate();

        Field recordingRegionsField = sdvManagerImplClass.getDeclaredField(recordingRegionsString);
        recordingRegionsField.setAccessible(true);

        @SuppressWarnings(uncheckedString)
        Map<ICicsRegion, RecordingRegion> recordingRegions =
                (Map<ICicsRegion, RecordingRegion>) recordingRegionsField.get(sdvManager);

        // Check correct number of users against each region
        Assertions.assertEquals(2,
                recordingRegions.get(mockCicsaRegion).getRecordingUsers().size());
        Assertions.assertEquals(1,
                recordingRegions.get(mockCicsbRegion).getRecordingUsers().size());
        Assertions.assertEquals(1,
                recordingRegions.get(mockCicscRegion).getRecordingUsers().size());

        // Check correct users against each region
        List<String> regionaUsers = new ArrayList<>();
        regionaUsers.add(user1String);
        regionaUsers.add(user3String);
        for (ISdvUser user : recordingRegions.get(mockCicsaRegion).getRecordingUsers()) {
            Assertions.assertTrue(regionaUsers.contains(user.getUsername()));
        }

        List<String> regionbUsers = new ArrayList<>();
        regionbUsers.add(user2String);
        for (ISdvUser user : recordingRegions.get(mockCicsbRegion).getRecordingUsers()) {
            Assertions.assertTrue(regionbUsers.contains(user.getUsername()));
        }

        List<String> regioncUsers = new ArrayList<>();
        regioncUsers.add(user4String);
        for (ISdvUser user : recordingRegions.get(mockCicscRegion).getRecordingUsers()) {
            Assertions.assertTrue(regioncUsers.contains(user.getUsername()));
        }

        // Check each region has correct terminal
        Assertions.assertEquals(regionaTerminal,
                recordingRegions.get(mockCicsaRegion).getMaintenanceTerminal());
        Assertions.assertEquals(regionbTerminal,
                recordingRegions.get(mockCicsbRegion).getMaintenanceTerminal());
        Assertions.assertEquals(regioncTerminal,
                recordingRegions.get(mockCicscRegion).getMaintenanceTerminal());
    }

    @Test
    void testProvisionGenerateOldCicsVersionForRegionA()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, CredentialsException,
            ResourceUnavailableException, ManagerException {
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);
        Map<String, ICicsRegionProvisioned> mockCicsRegionList = new HashMap<>();

        // Mock for CICS region A
        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getTag()).thenReturn(testCicsTagA);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn("OLDREGION");
        // Mock region Product version
        ProductVersion mockProductVersion = Mockito.mock(ProductVersion.class);
        Mockito.when(mockProductVersion.isEarlierThan(ProductVersion.v(750))).thenReturn(true,
                false, false);
        Mockito.when(mockCicsaRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        IZosBatchJob mockIzOsBatchJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(mockIzOsBatchJob.retrieveOutputAsString()).thenReturn(secOnMsg);
        Mockito.when(mockCicsaRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagA, mockCicsaRegion);

        // Mock for CICS region B
        ICicsRegionProvisioned mockCicsbRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsbRegion.getTag()).thenReturn(testCicsTagA);
        // Mock region Product version
        Mockito.when(mockCicsbRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        Mockito.when(mockCicsbRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagB, mockCicsbRegion);

        // Mock for CICS region C
        ICicsRegionProvisioned mockCicscRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicscRegion.getTag()).thenReturn(testCicsTagA);
        // Mock region Product version
        Mockito.when(mockCicscRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        Mockito.when(mockCicscRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagC, mockCicscRegion);

        Mockito.when(cicsManager.getTaggedCicsRegions()).thenReturn(mockCicsRegionList);

        // Mock Terminals
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        Mockito.when(cicsManager.generateCicsTerminal(testCicsTagB)).thenReturn(regionbTerminal);
        ICicsTerminal regioncTerminal = Mockito.mock(ICicsTerminal.class);
        Mockito.when(cicsManager.generateCicsTerminal(testCicsTagC)).thenReturn(regioncTerminal);

        // Replace private cicsManager instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);
        // Replace testClass to bypass generateAnnotatedFields
        Field testClass = sdvManagerImplClass.getSuperclass().getDeclaredField(testClassString);
        testClass.setAccessible(true);
        testClass.set(sdvManager, null);
        // Replace sdvUsersToRecordList with a mocked list
        List<ISdvUser> listOfUsersForAllRegions = new ArrayList<>();
        // user1
        ICredentialsUsernamePassword testCreds1 =
                new CredentialsUsernamePassword(null, user1String, password1);
        SdvUserImpl newSdvUser1 =
                new SdvUserImpl(creds1Tag, testCreds1, testCicsTagA, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser1);
        // user2
        ICredentialsUsernamePassword testCreds2 =
                new CredentialsUsernamePassword(null, user2String, password2);
        SdvUserImpl newSdvUser2 =
                new SdvUserImpl(creds2Tag, testCreds2, testCicsTagB, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser2);
        // user3
        ICredentialsUsernamePassword testCreds3 =
                new CredentialsUsernamePassword(null, user3String, password3);
        SdvUserImpl newSdvUser3 =
                new SdvUserImpl(creds3Tag, testCreds3, testCicsTagA, roleNameAdmin);
        listOfUsersForAllRegions.add(newSdvUser3);
        // user4
        ICredentialsUsernamePassword testCreds4 =
                new CredentialsUsernamePassword(null, user4String, password4);
        SdvUserImpl newSdvUser4 =
                new SdvUserImpl(creds4Tag, testCreds4, testCicsTagC, roleNameOperator);
        listOfUsersForAllRegions.add(newSdvUser4);

        Field sdvUsersToRecordList =
                sdvManagerImplClass.getDeclaredField(sdvUsersToRecordListString);
        sdvUsersToRecordList.setAccessible(true);
        sdvUsersToRecordList.set(sdvManager, listOfUsersForAllRegions);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagerImplClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManager.provisionGenerate();

        Field recordingRegionsField = sdvManagerImplClass.getDeclaredField(recordingRegionsString);
        recordingRegionsField.setAccessible(true);

        @SuppressWarnings(uncheckedString)
        Map<ICicsRegion, RecordingRegion> recordingRegions =
                (Map<ICicsRegion, RecordingRegion>) recordingRegionsField.get(sdvManager);

        // Check correct number of users against each region
        Assertions.assertEquals(null, recordingRegions.get(mockCicsaRegion));
        Assertions.assertEquals(1,
                recordingRegions.get(mockCicsbRegion).getRecordingUsers().size());
        Assertions.assertEquals(1,
                recordingRegions.get(mockCicscRegion).getRecordingUsers().size());

        // Check correct users against each region
        List<String> regionbUsers = new ArrayList<>();
        regionbUsers.add(user2String);
        for (ISdvUser user : recordingRegions.get(mockCicsbRegion).getRecordingUsers()) {
            Assertions.assertTrue(regionbUsers.contains(user.getUsername()));
        }

        List<String> regioncUsers = new ArrayList<>();
        regioncUsers.add(user4String);
        for (ISdvUser user : recordingRegions.get(mockCicscRegion).getRecordingUsers()) {
            Assertions.assertTrue(regioncUsers.contains(user.getUsername()));
        }

        // Check each region has correct terminal
        Assertions.assertEquals(regionbTerminal,
                recordingRegions.get(mockCicsbRegion).getMaintenanceTerminal());
        Assertions.assertEquals(regioncTerminal,
                recordingRegions.get(mockCicscRegion).getMaintenanceTerminal());

        // Check there is a warning in the log indicating Region A won't record
        Mockito.verify(mockLog, Mockito.times(1))
                .warn("SDV recording will not take place on CICS region 'OLDREGION'"
                        + ". Running version earlier than 750.");

    }

    @Test
    void testProvisionGenerateNoSecMsg() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException, CredentialsException,
            ResourceUnavailableException, ManagerException {
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);
        Map<String, ICicsRegionProvisioned> mockCicsRegionList = new HashMap<>();

        // Mock for CICS region A
        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getTag()).thenReturn(testCicsTagA);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn("NOSEC");
        // Mock region Product version
        ProductVersion mockProductVersion = Mockito.mock(ProductVersion.class);
        Mockito.when(mockProductVersion.isEarlierThan(ProductVersion.v(750))).thenReturn(false,
                false, false);
        Mockito.when(mockCicsaRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        IZosBatchJob mockIzOsBatchJobNoSec = Mockito.mock(IZosBatchJob.class);
        Mockito.when(mockIzOsBatchJobNoSec.retrieveOutputAsString())
                .thenReturn("blah\n\nDFHXS1102I: Security is inactive.\nblah");
        Mockito.when(mockCicsaRegion.getRegionJob()).thenReturn(mockIzOsBatchJobNoSec);
        mockCicsRegionList.put(testCicsTagA, mockCicsaRegion);

        // Mock for CICS region B
        ICicsRegionProvisioned mockCicsbRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsbRegion.getTag()).thenReturn(testCicsTagA);
        // Mock region Product version
        Mockito.when(mockCicsbRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        IZosBatchJob mockIzOsBatchJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(mockIzOsBatchJob.retrieveOutputAsString()).thenReturn(secOnMsg);
        Mockito.when(mockCicsbRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagB, mockCicsbRegion);

        // Mock for CICS region C
        ICicsRegionProvisioned mockCicscRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicscRegion.getTag()).thenReturn(testCicsTagA);
        // Mock region Product version
        Mockito.when(mockCicscRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        Mockito.when(mockCicscRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagC, mockCicscRegion);

        Mockito.when(cicsManager.getTaggedCicsRegions()).thenReturn(mockCicsRegionList);

        // Mock Terminals
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        Mockito.when(cicsManager.generateCicsTerminal(testCicsTagB)).thenReturn(regionbTerminal);
        ICicsTerminal regioncTerminal = Mockito.mock(ICicsTerminal.class);
        Mockito.when(cicsManager.generateCicsTerminal(testCicsTagC)).thenReturn(regioncTerminal);

        // Replace private cicsManager instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);
        // Replace testClass to bypass generateAnnotatedFields
        Field testClass = sdvManagerImplClass.getSuperclass().getDeclaredField(testClassString);
        testClass.setAccessible(true);
        testClass.set(sdvManager, null);
        // Replace sdvUsersToRecordList with a mocked list
        List<ISdvUser> listOfUsersForAllRegions = new ArrayList<>();
        // user1
        ICredentialsUsernamePassword testCreds1 =
                new CredentialsUsernamePassword(null, user1String, password1);
        SdvUserImpl newSdvUser1 =
                new SdvUserImpl(creds1Tag, testCreds1, testCicsTagA, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser1);
        // user2
        ICredentialsUsernamePassword testCreds2 =
                new CredentialsUsernamePassword(null, user2String, password2);
        SdvUserImpl newSdvUser2 =
                new SdvUserImpl(creds2Tag, testCreds2, testCicsTagB, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser2);
        // user3
        ICredentialsUsernamePassword testCreds3 =
                new CredentialsUsernamePassword(null, user3String, password3);
        SdvUserImpl newSdvUser3 =
                new SdvUserImpl(creds3Tag, testCreds3, testCicsTagA, roleNameAdmin);
        listOfUsersForAllRegions.add(newSdvUser3);
        // user4
        ICredentialsUsernamePassword testCreds4 =
                new CredentialsUsernamePassword(null, user4String, password4);
        SdvUserImpl newSdvUser4 =
                new SdvUserImpl(creds4Tag, testCreds4, testCicsTagC, roleNameOperator);
        listOfUsersForAllRegions.add(newSdvUser4);

        Field sdvUsersToRecordList =
                sdvManagerImplClass.getDeclaredField(sdvUsersToRecordListString);
        sdvUsersToRecordList.setAccessible(true);
        sdvUsersToRecordList.set(sdvManager, listOfUsersForAllRegions);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagerImplClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManager.provisionGenerate();

        Field recordingRegionsField = sdvManagerImplClass.getDeclaredField(recordingRegionsString);
        recordingRegionsField.setAccessible(true);

        @SuppressWarnings(uncheckedString)
        Map<ICicsRegion, RecordingRegion> recordingRegions =
                (Map<ICicsRegion, RecordingRegion>) recordingRegionsField.get(sdvManager);

        // Check correct number of users against each region
        Assertions.assertEquals(null, recordingRegions.get(mockCicsaRegion));
        Assertions.assertEquals(1,
                recordingRegions.get(mockCicsbRegion).getRecordingUsers().size());
        Assertions.assertEquals(1,
                recordingRegions.get(mockCicscRegion).getRecordingUsers().size());

        // Check correct users against each region
        List<String> regionbUsers = new ArrayList<>();
        regionbUsers.add(user2String);
        for (ISdvUser user : recordingRegions.get(mockCicsbRegion).getRecordingUsers()) {
            Assertions.assertTrue(regionbUsers.contains(user.getUsername()));
        }

        List<String> regioncUsers = new ArrayList<>();
        regioncUsers.add(user4String);
        for (ISdvUser user : recordingRegions.get(mockCicscRegion).getRecordingUsers()) {
            Assertions.assertTrue(regioncUsers.contains(user.getUsername()));
        }

        // Check each region has correct terminal
        Assertions.assertEquals(regionbTerminal,
                recordingRegions.get(mockCicsbRegion).getMaintenanceTerminal());
        Assertions.assertEquals(regioncTerminal,
                recordingRegions.get(mockCicscRegion).getMaintenanceTerminal());

        // Check there is a warning in the log indicating Region A won't record
        Mockito.verify(mockLog, Mockito.times(1))
                .warn("SDV recording will not take place on CICS region 'NOSEC'"
                        + ". Security is not active.");
    }

    @Test
    void testProvisionGenerateNoUsersForRegion()
            throws ClassNotFoundException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException, CredentialsException,
            InstantiationException, InvocationTargetException, NoSuchMethodException,
            ResourceUnavailableException, ManagerException {
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);
        Map<String, ICicsRegionProvisioned> mockCicsRegionList = new HashMap<>();

        // Mock for CICS region A
        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getTag()).thenReturn(testCicsTagA);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn("OLDREGION");
        // Mock region Product version
        ProductVersion mockProductVersion = Mockito.mock(ProductVersion.class);
        Mockito.when(mockProductVersion.isEarlierThan(ProductVersion.v(750))).thenReturn(false,
                false, false);
        Mockito.when(mockCicsaRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        IZosBatchJob mockIzOsBatchJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(mockIzOsBatchJob.retrieveOutputAsString()).thenReturn(secOnMsg);
        Mockito.when(mockCicsaRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagA, mockCicsaRegion);

        // Mock for CICS region B
        ICicsRegionProvisioned mockCicsbRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsbRegion.getTag()).thenReturn(testCicsTagA);
        // Mock region Product version
        Mockito.when(mockCicsbRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        Mockito.when(mockCicsbRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagB, mockCicsbRegion);

        // Mock for CICS region C
        ICicsRegionProvisioned mockCicscRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicscRegion.getTag()).thenReturn(testCicsTagA);
        // Mock region Product version
        Mockito.when(mockCicscRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        Mockito.when(mockCicscRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagC, mockCicscRegion);

        Mockito.when(cicsManager.getTaggedCicsRegions()).thenReturn(mockCicsRegionList);

        // Mock Terminals
        ICicsTerminal regionbTerminal = Mockito.mock(ICicsTerminal.class);
        Mockito.when(cicsManager.generateCicsTerminal(testCicsTagB)).thenReturn(regionbTerminal);
        ICicsTerminal regioncTerminal = Mockito.mock(ICicsTerminal.class);
        Mockito.when(cicsManager.generateCicsTerminal(testCicsTagC)).thenReturn(regioncTerminal);

        // Replace private cicsManager instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);
        // Replace testClass to bypass generateAnnotatedFields
        Field testClass = sdvManagerImplClass.getSuperclass().getDeclaredField(testClassString);
        testClass.setAccessible(true);
        testClass.set(sdvManager, null);
        // Replace sdvUsersToRecordList with a mocked list
        List<ISdvUser> listOfUsersForAllRegions = new ArrayList<>();
        // user1
        ICredentialsUsernamePassword testCreds1 =
                new CredentialsUsernamePassword(null, user1String, password1);
        SdvUserImpl newSdvUser1 =
                new SdvUserImpl(creds1Tag, testCreds1, testCicsTagB, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser1);
        // user2
        ICredentialsUsernamePassword testCreds2 =
                new CredentialsUsernamePassword(null, user2String, password2);
        SdvUserImpl newSdvUser2 =
                new SdvUserImpl(creds2Tag, testCreds2, testCicsTagB, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser2);
        // user3
        ICredentialsUsernamePassword testCreds3 =
                new CredentialsUsernamePassword(null, user3String, password3);
        SdvUserImpl newSdvUser3 =
                new SdvUserImpl(creds3Tag, testCreds3, testCicsTagB, roleNameAdmin);
        listOfUsersForAllRegions.add(newSdvUser3);
        // user4
        ICredentialsUsernamePassword testCreds4 =
                new CredentialsUsernamePassword(null, user4String, password4);
        SdvUserImpl newSdvUser4 =
                new SdvUserImpl(creds4Tag, testCreds4, testCicsTagC, roleNameOperator);
        listOfUsersForAllRegions.add(newSdvUser4);

        Field sdvUsersToRecordList =
                sdvManagerImplClass.getDeclaredField(sdvUsersToRecordListString);
        sdvUsersToRecordList.setAccessible(true);
        sdvUsersToRecordList.set(sdvManager, listOfUsersForAllRegions);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagerImplClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManager.provisionGenerate();

        Field recordingRegionsField = sdvManagerImplClass.getDeclaredField(recordingRegionsString);
        recordingRegionsField.setAccessible(true);

        @SuppressWarnings(uncheckedString)
        Map<ICicsRegion, RecordingRegion> recordingRegions =
                (Map<ICicsRegion, RecordingRegion>) recordingRegionsField.get(sdvManager);

        // Check correct number of users against each region
        Assertions.assertEquals(null, recordingRegions.get(mockCicsaRegion));
        Assertions.assertEquals(3,
                recordingRegions.get(mockCicsbRegion).getRecordingUsers().size());
        Assertions.assertEquals(1,
                recordingRegions.get(mockCicscRegion).getRecordingUsers().size());

        // Check correct users against each region
        List<String> regionbUsers = new ArrayList<>();
        regionbUsers.add(user1String);
        regionbUsers.add(user2String);
        regionbUsers.add(user3String);
        for (ISdvUser user : recordingRegions.get(mockCicsbRegion).getRecordingUsers()) {
            Assertions.assertTrue(regionbUsers.contains(user.getUsername()));
        }

        List<String> regioncUsers = new ArrayList<>();
        regioncUsers.add(user4String);
        for (ISdvUser user : recordingRegions.get(mockCicscRegion).getRecordingUsers()) {
            Assertions.assertTrue(regioncUsers.contains(user.getUsername()));
        }

        // Check each region has correct terminal
        Assertions.assertEquals(regionbTerminal,
                recordingRegions.get(mockCicsbRegion).getMaintenanceTerminal());
        Assertions.assertEquals(regioncTerminal,
                recordingRegions.get(mockCicscRegion).getMaintenanceTerminal());

        // Check there is a warning in the log indicating Region A won't record
        Mockito.verify(mockLog, Mockito.times(1))
                .warn("No users have been listed for recording via the SdvUser "
                        + "annotation for cicsTag 'CICSA'.");

    }

    @Test
    void testProvisionGenerateException() throws CredentialsException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException,
            ResourceUnavailableException, ManagerException, ClassNotFoundException,
            InstantiationException, InvocationTargetException, NoSuchMethodException {

        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);
        Map<String, ICicsRegionProvisioned> mockCicsRegionList = new HashMap<>();

        // Mock for CICS region A
        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getTag()).thenReturn(testCicsTagA);
        // Mock region Product version
        ProductVersion mockProductVersion = Mockito.mock(ProductVersion.class);
        Mockito.when(mockProductVersion.isEarlierThan(ProductVersion.v(750))).thenReturn(false);
        Mockito.when(mockCicsaRegion.getVersion()).thenReturn(mockProductVersion);
        // Mock region SEC=YES log message
        IZosBatchJob mockIzOsBatchJob = Mockito.mock(IZosBatchJob.class);
        Mockito.when(mockIzOsBatchJob.retrieveOutputAsString()).thenReturn(secOnMsg);
        Mockito.when(mockCicsaRegion.getRegionJob()).thenReturn(mockIzOsBatchJob);
        mockCicsRegionList.put(testCicsTagA, mockCicsaRegion);

        Mockito.when(cicsManager.getTaggedCicsRegions()).thenReturn(mockCicsRegionList);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn(regionaApplid);
        Mockito.when(cicsManager.locateCicsRegion(testCicsTagA)).thenReturn(mockCicsaRegion);

        // Mock Terminals
        Mockito.when(cicsManager.generateCicsTerminal(testCicsTagA))
                .thenThrow(new CicstsManagerException("Ooppss"));

        // Replace private cicsManager instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);
        // Replace testClass to bypass generateAnnotatedFields
        Field testClass = sdvManagerImplClass.getSuperclass().getDeclaredField(testClassString);
        testClass.setAccessible(true);
        testClass.set(sdvManager, null);
        // Replace sdvUsersToRecordList with a mocked list
        List<ISdvUser> listOfUsersForAllRegions = new ArrayList<>();
        // user1
        ICredentialsUsernamePassword testCreds1 =
                new CredentialsUsernamePassword(null, user1String, password1);
        SdvUserImpl newSdvUser1 =
                new SdvUserImpl(creds1Tag, testCreds1, testCicsTagA, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser1);
        // user3
        ICredentialsUsernamePassword testCreds3 =
                new CredentialsUsernamePassword(null, user3String, password3);
        SdvUserImpl newSdvUser3 =
                new SdvUserImpl(creds3Tag, testCreds3, testCicsTagA, roleNameAdmin);
        listOfUsersForAllRegions.add(newSdvUser3);

        Field sdvUsersToRecordList =
                sdvManagerImplClass.getDeclaredField(sdvUsersToRecordListString);
        sdvUsersToRecordList.setAccessible(true);
        sdvUsersToRecordList.set(sdvManager, listOfUsersForAllRegions);

        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Field dssField = sdvManagerImplClass.getDeclaredField("dss");
        dssField.setAccessible(true);
        dssField.set(sdvManager, dssService);

        // Mock releaseUsers bits
        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds1Tag, regionaApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds3Tag, regionaApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(runName);
        Field frameworkField =
                sdvManagerImplClass.getSuperclass().getDeclaredField(frameworkString);
        frameworkField.setAccessible(true);
        frameworkField.set(sdvManager, framework);

        // Make call to funtion under test
        ManagerException exception = Assertions.assertThrows(ManagerException.class, () -> {
            sdvManager.provisionGenerate();
        });

        Assertions.assertEquals("Ooppss", exception.getMessage());

        // Verify users released.
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds1Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds3Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verifyNoMoreInteractions();

    }

    @Test
    void testProvisionStart() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, ClassNotFoundException, SdvManagerException, NoSuchFieldException {
        // Replace private sdvRecorder instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        SdvHttpRecorderImpl sdvRecorder = Mockito.mock(SdvHttpRecorderImpl.class);
        Field sdvRecorderField = sdvManagerImplClass.getDeclaredField(sdvRecorderVarName);
        sdvRecorderField.setAccessible(true);
        sdvRecorderField.set(sdvManager, sdvRecorder);

        // Make call to funtion under test
        sdvManager.provisionStart();

        Mockito.verify(sdvRecorder, Mockito.times(1)).prepareEnvironments("LOG_GENERAL_001");
        Mockito.verifyNoMoreInteractions(sdvRecorder);
    }

    @Test
    void testProvisionStop() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, ClassNotFoundException, SdvManagerException, NoSuchFieldException,
            CredentialsException, CicstsManagerException {
        // Replace private sdvRecorder instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        SdvHttpRecorderImpl sdvRecorder = Mockito.mock(SdvHttpRecorderImpl.class);
        Field sdvRecorderField = sdvManagerImplClass.getDeclaredField(sdvRecorderVarName);
        sdvRecorderField.setAccessible(true);
        sdvRecorderField.set(sdvManager, sdvRecorder);

        // Replace sdvUsersToRecordList with a mocked list
        List<ISdvUser> listOfUsersForAllRegions = new ArrayList<>();
        // user1
        ICredentialsUsernamePassword testCreds1 =
                new CredentialsUsernamePassword(null, user1String, password1);
        SdvUserImpl newSdvUser1 =
                new SdvUserImpl(creds1Tag, testCreds1, testCicsTagA, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser1);
        // user2
        ICredentialsUsernamePassword testCreds2 =
                new CredentialsUsernamePassword(null, user2String, password2);
        SdvUserImpl newSdvUser2 =
                new SdvUserImpl(creds2Tag, testCreds2, testCicsTagB, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser2);
        // user3
        ICredentialsUsernamePassword testCreds3 =
                new CredentialsUsernamePassword(null, user3String, password3);
        SdvUserImpl newSdvUser3 =
                new SdvUserImpl(creds3Tag, testCreds3, testCicsTagA, roleNameAdmin);
        listOfUsersForAllRegions.add(newSdvUser3);

        Field sdvUsersToRecordList =
                sdvManagerImplClass.getDeclaredField(sdvUsersToRecordListString);
        sdvUsersToRecordList.setAccessible(true);
        sdvUsersToRecordList.set(sdvManager, listOfUsersForAllRegions);

        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);

        // Mock cicsManager
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);

        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn(regionaApplid);
        Mockito.when(cicsManager.locateCicsRegion(testCicsTagA)).thenReturn(mockCicsaRegion);

        ICicsRegionProvisioned mockCicsbRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsbRegion.getApplid()).thenReturn(regionbApplid);
        Mockito.when(cicsManager.locateCicsRegion(testCicsTagB)).thenReturn(mockCicsbRegion);

        // Replace private cicsManager instance in sdvManager
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds1Tag, regionaApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds3Tag, regionaApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds2Tag, regionbApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(runName);
        Field frameworkField =
                sdvManagerImplClass.getSuperclass().getDeclaredField(frameworkString);
        frameworkField.setAccessible(true);
        frameworkField.set(sdvManager, framework);

        // Make call to funtion under test
        sdvManager.provisionStop();

        Mockito.verify(sdvRecorder, Mockito.times(1)).endRecording();
        Mockito.verify(sdvRecorder, Mockito.times(1)).cleanUpEnvironments();
        Mockito.verifyNoMoreInteractions(sdvRecorder);

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds1Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds3Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds2Tag),
                Mockito.eq(regionbApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verifyNoMoreInteractions();
    }

    @Test
    void testProvisionStopEndRecordingException() throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, ClassNotFoundException, SdvManagerException,
            NoSuchFieldException, CredentialsException, CicstsManagerException {
        // Replace private sdvRecorder instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        SdvHttpRecorderImpl sdvRecorder = Mockito.mock(SdvHttpRecorderImpl.class);
        Mockito.doThrow(new SdvManagerException("test1")).when(sdvRecorder).endRecording();
        Field sdvRecorderField = sdvManagerImplClass.getDeclaredField(sdvRecorderVarName);
        sdvRecorderField.setAccessible(true);
        sdvRecorderField.set(sdvManager, sdvRecorder);

        // Replace sdvUsersToRecordList with a mocked list
        List<ISdvUser> listOfUsersForAllRegions = new ArrayList<>();
        // user1
        ICredentialsUsernamePassword testCreds1 =
                new CredentialsUsernamePassword(null, user1String, password1);
        SdvUserImpl newSdvUser1 =
                new SdvUserImpl(creds1Tag, testCreds1, testCicsTagA, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser1);
        // user2
        ICredentialsUsernamePassword testCreds2 =
                new CredentialsUsernamePassword(null, user2String, password2);
        SdvUserImpl newSdvUser2 =
                new SdvUserImpl(creds2Tag, testCreds2, testCicsTagB, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser2);
        // user3
        ICredentialsUsernamePassword testCreds3 =
                new CredentialsUsernamePassword(null, user3String, password3);
        SdvUserImpl newSdvUser3 =
                new SdvUserImpl(creds3Tag, testCreds3, testCicsTagA, roleNameAdmin);
        listOfUsersForAllRegions.add(newSdvUser3);

        Field sdvUsersToRecordList =
                sdvManagerImplClass.getDeclaredField(sdvUsersToRecordListString);
        sdvUsersToRecordList.setAccessible(true);
        sdvUsersToRecordList.set(sdvManager, listOfUsersForAllRegions);

        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);

        // Mock cicsManager
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);

        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn(regionaApplid);
        Mockito.when(cicsManager.locateCicsRegion(testCicsTagA)).thenReturn(mockCicsaRegion);

        ICicsRegionProvisioned mockCicsbRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsbRegion.getApplid()).thenReturn(regionbApplid);
        Mockito.when(cicsManager.locateCicsRegion(testCicsTagB)).thenReturn(mockCicsbRegion);

        // Replace private cicsManager instance in sdvManager
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds1Tag, regionaApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds3Tag, regionaApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds2Tag, regionbApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(runName);
        Field frameworkField =
                sdvManagerImplClass.getSuperclass().getDeclaredField(frameworkString);
        frameworkField.setAccessible(true);
        frameworkField.set(sdvManager, framework);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagerImplClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManager.provisionStop();

        Mockito.verify(sdvRecorder, Mockito.times(1)).endRecording();
        Mockito.verify(sdvRecorder, Mockito.times(1)).cleanUpEnvironments();
        Mockito.verifyNoMoreInteractions(sdvRecorder);

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds1Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds3Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds2Tag),
                Mockito.eq(regionbApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verifyNoMoreInteractions();

        // endRecording exception should not have stopped everything else from running,
        // but it should had made a log output to make the user aware.
        Mockito.verify(mockLog, Mockito.times(1))
                .error(Mockito.eq("Could not stop known SDC recordings in provisionStop."),
                        Mockito.any(SdvManagerException.class));
    }

    @Test
    void testProvisionStopReleaseUsersException() throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, ClassNotFoundException, SdvManagerException,
            NoSuchFieldException, CredentialsException, CicstsManagerException {
        // Replace private sdvRecorder instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        SdvHttpRecorderImpl sdvRecorder = Mockito.mock(SdvHttpRecorderImpl.class);
        Field sdvRecorderField = sdvManagerImplClass.getDeclaredField(sdvRecorderVarName);
        sdvRecorderField.setAccessible(true);
        sdvRecorderField.set(sdvManager, sdvRecorder);

        // Replace sdvUsersToRecordList with a mocked list
        List<ISdvUser> listOfUsersForAllRegions = new ArrayList<>();
        // user1
        ICredentialsUsernamePassword testCreds1 =
                new CredentialsUsernamePassword(null, user1String, password1);
        SdvUserImpl newSdvUser1 =
                new SdvUserImpl(creds1Tag, testCreds1, testCicsTagA, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser1);
        // user2
        ICredentialsUsernamePassword testCreds2 =
                new CredentialsUsernamePassword(null, user2String, password2);
        SdvUserImpl newSdvUser2 =
                new SdvUserImpl(creds2Tag, testCreds2, testCicsTagB, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser2);
        // user3
        ICredentialsUsernamePassword testCreds3 =
                new CredentialsUsernamePassword(null, user3String, password3);
        SdvUserImpl newSdvUser3 =
                new SdvUserImpl(creds3Tag, testCreds3, testCicsTagA, roleNameAdmin);
        listOfUsersForAllRegions.add(newSdvUser3);

        Field sdvUsersToRecordList =
                sdvManagerImplClass.getDeclaredField(sdvUsersToRecordListString);
        sdvUsersToRecordList.setAccessible(true);
        sdvUsersToRecordList.set(sdvManager, listOfUsersForAllRegions);

        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);

        // Mock cicsManager
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);

        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn(regionaApplid);
        Mockito.when(cicsManager.locateCicsRegion(testCicsTagA))
            .thenThrow(new CicstsManagerException("test 2"));

        ICicsRegionProvisioned mockCicsbRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsbRegion.getApplid()).thenReturn(regionbApplid);
        Mockito.when(cicsManager.locateCicsRegion(testCicsTagB)).thenReturn(mockCicsbRegion);

        // Replace private cicsManager instance in sdvManager
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds1Tag, regionaApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds3Tag, regionaApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds2Tag, regionbApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(runName);
        Field frameworkField =
                sdvManagerImplClass.getSuperclass().getDeclaredField(frameworkString);
        frameworkField.setAccessible(true);
        frameworkField.set(sdvManager, framework);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagerImplClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManager.provisionStop();

        Mockito.verify(sdvRecorder, Mockito.times(1)).endRecording();
        Mockito.verify(sdvRecorder, Mockito.times(1)).cleanUpEnvironments();
        Mockito.verifyNoMoreInteractions(sdvRecorder);

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds1Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(0));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds3Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(0));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds2Tag),
                Mockito.eq(regionbApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(0));
        sdvUserPoolStatic.verifyNoMoreInteractions();

        // endRecording exception should not have stopped everything else from running,
        // but it should had made a log output to make the user aware.
        Mockito.verify(mockLog, Mockito.times(1))
                .error(Mockito.eq("Could not release SDV SdvUsers in provisionStop."),
                        Mockito.any(CicstsManagerException.class));
    }

    @Test
    void testProvisionStopcleanUpEnvironmentsException() throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, ClassNotFoundException, SdvManagerException,
            NoSuchFieldException, CredentialsException, CicstsManagerException {
        // Replace private sdvRecorder instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        SdvHttpRecorderImpl sdvRecorder = Mockito.mock(SdvHttpRecorderImpl.class);
        Mockito.doThrow(new SdvManagerException("test3")).when(sdvRecorder).cleanUpEnvironments();
        Field sdvRecorderField = sdvManagerImplClass.getDeclaredField(sdvRecorderVarName);
        sdvRecorderField.setAccessible(true);
        sdvRecorderField.set(sdvManager, sdvRecorder);

        // Replace sdvUsersToRecordList with a mocked list
        List<ISdvUser> listOfUsersForAllRegions = new ArrayList<>();
        // user1
        ICredentialsUsernamePassword testCreds1 =
                new CredentialsUsernamePassword(null, user1String, password1);
        SdvUserImpl newSdvUser1 =
                new SdvUserImpl(creds1Tag, testCreds1, testCicsTagA, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser1);
        // user2
        ICredentialsUsernamePassword testCreds2 =
                new CredentialsUsernamePassword(null, user2String, password2);
        SdvUserImpl newSdvUser2 =
                new SdvUserImpl(creds2Tag, testCreds2, testCicsTagB, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser2);
        // user3
        ICredentialsUsernamePassword testCreds3 =
                new CredentialsUsernamePassword(null, user3String, password3);
        SdvUserImpl newSdvUser3 =
                new SdvUserImpl(creds3Tag, testCreds3, testCicsTagA, roleNameAdmin);
        listOfUsersForAllRegions.add(newSdvUser3);

        Field sdvUsersToRecordList =
                sdvManagerImplClass.getDeclaredField(sdvUsersToRecordListString);
        sdvUsersToRecordList.setAccessible(true);
        sdvUsersToRecordList.set(sdvManager, listOfUsersForAllRegions);

        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);

        // Mock cicsManager
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);

        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn(regionaApplid);
        Mockito.when(cicsManager.locateCicsRegion(testCicsTagA)).thenReturn(mockCicsaRegion);

        ICicsRegionProvisioned mockCicsbRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsbRegion.getApplid()).thenReturn(regionbApplid);
        Mockito.when(cicsManager.locateCicsRegion(testCicsTagB)).thenReturn(mockCicsbRegion);

        // Replace private cicsManager instance in sdvManager
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds1Tag, regionaApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds3Tag, regionaApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds2Tag, regionbApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(runName);
        Field frameworkField =
                sdvManagerImplClass.getSuperclass().getDeclaredField(frameworkString);
        frameworkField.setAccessible(true);
        frameworkField.set(sdvManager, framework);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagerImplClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManager.provisionStop();

        Mockito.verify(sdvRecorder, Mockito.times(1)).endRecording();
        Mockito.verify(sdvRecorder, Mockito.times(1)).cleanUpEnvironments();
        Mockito.verifyNoMoreInteractions(sdvRecorder);

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds1Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds3Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds2Tag),
                Mockito.eq(regionbApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verifyNoMoreInteractions();

        // endRecording exception should not have stopped everything else from running,
        // but it should had made a log output to make the user aware.
        Mockito.verify(mockLog, Mockito.times(1))
                .error(Mockito.eq("Could not cleanup SDV environments in provisionStop."),
                        Mockito.any(SdvManagerException.class));
    }

    @Test
    void testStartOfTestClass() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, ClassNotFoundException, SdvManagerException, NoSuchFieldException {
        // Replace private sdvRecorder instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        SdvHttpRecorderImpl sdvRecorder = Mockito.mock(SdvHttpRecorderImpl.class);
        Field sdvRecorderField = sdvManagerImplClass.getDeclaredField(sdvRecorderVarName);
        sdvRecorderField.setAccessible(true);
        sdvRecorderField.set(sdvManager, sdvRecorder);

        // Make call to funtion under test
        sdvManager.startOfTestClass();

        Mockito.verify(sdvRecorder, Mockito.times(1)).startRecording();
        Mockito.verifyNoMoreInteractions(sdvRecorder);
    }

    @Test
    void testEndOfTestClassTestPassed() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, ClassNotFoundException, NoSuchFieldException, ManagerException {
        // Replace private sdvRecorder instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        SdvHttpRecorderImpl sdvRecorder = Mockito.mock(SdvHttpRecorderImpl.class);
        Field sdvRecorderField = sdvManagerImplClass.getDeclaredField(sdvRecorderVarName);
        sdvRecorderField.setAccessible(true);
        sdvRecorderField.set(sdvManager, sdvRecorder);

        IFramework framework = Mockito.mock(IFramework.class);
        IRun testRun = Mockito.mock(IRun.class);
        Mockito.when(testRun.getTestBundleName()).thenReturn("bundleA");
        Mockito.when(testRun.getTestClassName()).thenReturn("classA");
        Mockito.when(framework.getTestRun()).thenReturn(testRun);
        Field frameworkField =
                sdvManagerImplClass.getSuperclass().getDeclaredField(frameworkString);
        frameworkField.setAccessible(true);
        frameworkField.set(sdvManager, framework);

        // Make call to funtion under test
        Result res = Mockito.mock(Result.class);
        Mockito.when(res.isPassed()).thenReturn(true);

        sdvManager.endOfTestClass(res, null);

        Mockito.verify(sdvRecorder, Mockito.times(1)).endRecording();
        Mockito.verify(sdvRecorder, Mockito.times(1)).exportRecordings("bundleA", "classA");
        Mockito.verifyNoMoreInteractions(sdvRecorder);
    }

    @Test
    void testEndOfTestClassTestFailed() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, ClassNotFoundException, NoSuchFieldException, ManagerException {
        // Replace private sdvRecorder instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        SdvHttpRecorderImpl sdvRecorder = Mockito.mock(SdvHttpRecorderImpl.class);
        Field sdvRecorderField = sdvManagerImplClass.getDeclaredField(sdvRecorderVarName);
        sdvRecorderField.setAccessible(true);
        sdvRecorderField.set(sdvManager, sdvRecorder);

        IFramework framework = Mockito.mock(IFramework.class);
        IRun testRun = Mockito.mock(IRun.class);
        Mockito.when(testRun.getTestBundleName()).thenReturn("bundleB");
        Mockito.when(testRun.getTestClassName()).thenReturn("classB");
        Mockito.when(framework.getTestRun()).thenReturn(testRun);
        Field frameworkField =
                sdvManagerImplClass.getSuperclass().getDeclaredField(frameworkString);
        frameworkField.setAccessible(true);
        frameworkField.set(sdvManager, framework);

        // Make call to funtion under test
        Result res = Mockito.mock(Result.class);
        Mockito.when(res.isPassed()).thenReturn(false);

        sdvManager.endOfTestClass(res, null);

        Mockito.verify(sdvRecorder, Mockito.times(1)).endRecording();
        Mockito.verify(sdvRecorder, Mockito.times(0)).exportRecordings("bundleB", "classB");
        Mockito.verifyNoMoreInteractions(sdvRecorder);
    }

    @Test
    void testAreYouProvisionalDependentOn() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        // Replace private sdvRecorder instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        Boolean dependent = false;
        // Test ICicstsManagerSpi
        IManager cicsManager = new CicstsManagerImpl();
        dependent = sdvManager.areYouProvisionalDependentOn(cicsManager);
        Assertions.assertTrue(dependent);

        // Test IArtifactManager
        IManager artifactManager = new ArtifactManagerImpl();
        dependent = sdvManager.areYouProvisionalDependentOn(artifactManager);
        Assertions.assertTrue(dependent);

        // Test IHttpManagerSpi
        IManager httpManager = new HttpManagerImpl();
        dependent = sdvManager.areYouProvisionalDependentOn(httpManager);
        Assertions.assertTrue(dependent);

        // Test a random manager
        IManager zosManager = new ZosManagerImpl();
        dependent = sdvManager.areYouProvisionalDependentOn(zosManager);
        Assertions.assertFalse(dependent);

    }

    @Test
    void testReleaseUsersException() throws CredentialsException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException,
            ResourceUnavailableException, ManagerException, ClassNotFoundException,
            InstantiationException, InvocationTargetException, NoSuchMethodException {
        // Replace private sdvRecorder instance in sdvManager
        Class<?> sdvManagerImplClass = Class.forName(sdvManagerImplClassString);
        SdvManagerImpl sdvManager =
                (SdvManagerImpl) sdvManagerImplClass.getDeclaredConstructor().newInstance();

        SdvHttpRecorderImpl sdvRecorder = Mockito.mock(SdvHttpRecorderImpl.class);
        Field sdvRecorderField = sdvManagerImplClass.getDeclaredField(sdvRecorderVarName);
        sdvRecorderField.setAccessible(true);
        sdvRecorderField.set(sdvManager, sdvRecorder);

        // Replace sdvUsersToRecordList with a mocked list
        List<ISdvUser> listOfUsersForAllRegions = new ArrayList<>();
        // user1
        ICredentialsUsernamePassword testCreds1 =
                new CredentialsUsernamePassword(null, user1String, password1);
        SdvUserImpl newSdvUser1 =
                new SdvUserImpl(creds1Tag, testCreds1, testCicsTagA, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser1);
        // user2
        ICredentialsUsernamePassword testCreds2 =
                new CredentialsUsernamePassword(null, user2String, password2);
        SdvUserImpl newSdvUser2 =
                new SdvUserImpl(creds2Tag, testCreds2, testCicsTagB, roleNameTeller);
        listOfUsersForAllRegions.add(newSdvUser2);
        // user3
        ICredentialsUsernamePassword testCreds3 =
                new CredentialsUsernamePassword(null, user3String, password3);
        SdvUserImpl newSdvUser3 =
                new SdvUserImpl(creds3Tag, testCreds3, testCicsTagA, roleNameAdmin);
        listOfUsersForAllRegions.add(newSdvUser3);

        Field sdvUsersToRecordList =
                sdvManagerImplClass.getDeclaredField(sdvUsersToRecordListString);
        sdvUsersToRecordList.setAccessible(true);
        sdvUsersToRecordList.set(sdvManager, listOfUsersForAllRegions);

        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Field dssField = sdvManagerImplClass.getDeclaredField("dss");
        dssField.setAccessible(true);
        dssField.set(sdvManager, dssService);

        // Mock cicsManager
        ICicstsManagerSpi cicsManager = Mockito.mock(ICicstsManagerSpi.class);

        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn(regionaApplid);
        Mockito.when(cicsManager.locateCicsRegion(testCicsTagA)).thenReturn(mockCicsaRegion);

        ICicsRegionProvisioned mockCicsbRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsbRegion.getApplid()).thenReturn(regionbApplid);
        Mockito.when(cicsManager.locateCicsRegion(testCicsTagB)).thenReturn(mockCicsbRegion);

        // Replace private cicsManager instance in sdvManager
        Field cicsManagerField =
                sdvManagerImplClass.getDeclaredField(privateCicsManagerVariableName);
        cicsManagerField.setAccessible(true);
        cicsManagerField.set(sdvManager, cicsManager);

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds1Tag, regionaApplid, runName, dssService))
                .thenThrow(new DynamicStatusStoreException("could not delete"));

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds3Tag, regionaApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        sdvUserPoolStatic
                .when(() -> SdvUserPool.deleteDss(creds2Tag, regionbApplid, runName, dssService))
                .thenAnswer(invocation -> {
                    return null;
                });

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn(runName);
        Field frameworkField =
                sdvManagerImplClass.getSuperclass().getDeclaredField(frameworkString);
        frameworkField.setAccessible(true);
        frameworkField.set(sdvManager, framework);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagerImplClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManager.provisionStop();

        Mockito.verify(sdvRecorder, Mockito.times(1)).endRecording();
        Mockito.verify(sdvRecorder, Mockito.times(1)).cleanUpEnvironments();
        Mockito.verifyNoMoreInteractions(sdvRecorder);

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds1Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds3Tag),
                Mockito.eq(regionaApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(creds2Tag),
                Mockito.eq(regionbApplid), Mockito.eq(runName), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verifyNoMoreInteractions();

        Mockito.verify(mockLog, Mockito.times(1))
                .error(Mockito.eq("Could not release SDV User:  " + creds1Tag + ", on CICS region "
                        + regionaApplid + ", for test run " + runName),
                        Mockito.any(DynamicStatusStoreException.class));

    }
}
