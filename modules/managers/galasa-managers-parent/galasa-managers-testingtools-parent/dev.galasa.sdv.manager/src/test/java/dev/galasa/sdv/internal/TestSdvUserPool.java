/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.cicsts.spi.ICicsRegionProvisioned;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.sdv.SdvManagerException;
import dev.galasa.sdv.internal.properties.SdvPoolUsers;
import dev.galasa.zos.IZosImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sun.misc.Unsafe;


class TestSdvUserPool {

    private MockedStatic<SdvPoolUsers> sdvPoolUsers;

    private String roleName = "TELLER";
    private String zosImageId = "IMG1";
    private String applidString = "APPL1";
    private String sdvUserPoolClassString = "dev.galasa.sdv.internal.SdvUserPool";
    private String dssVariableString = "dss";

    @SuppressWarnings("PMD")
    private static final Log mockLog = Mockito.mock(Log.class);

    @BeforeEach
    public void setUp() {
        // Registering static mocks before each test
        sdvPoolUsers = Mockito.mockStatic(SdvPoolUsers.class);

        Mockito.when(mockLog.isTraceEnabled()).thenReturn(true);
    }

    @AfterEach
    public void tearDown() {
        // Closing static mocks after each test
        sdvPoolUsers.close();
    }

    @Test
    void testAllocateUser() throws SdvManagerException, ResourceUnavailableException,
            InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException,
            InsufficientResourcesAvailableException, NoSuchFieldException, ClassNotFoundException,
            DynamicStatusStoreMatchException, DynamicStatusStoreException {
        // Mock IZosImage
        IZosImage mockZosImage = Mockito.mock(IZosImage.class);
        Mockito.when(mockZosImage.getImageID()).thenReturn(zosImageId);

        // Mock CICS region
        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getZosImage()).thenReturn(mockZosImage);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn(applidString);

        // Mocks for statics
        List<String> userCredList = new ArrayList<>();
        userCredList.add("CREDS11");
        userCredList.add("CREDS22");
        userCredList.add("CREDS33");
        sdvPoolUsers.when(() -> SdvPoolUsers.get(zosImageId, roleName)).thenReturn(userCredList);

        // Get SdvUserPool instance
        Class<?> sdvUserPoolClass = Class.forName(sdvUserPoolClassString);
        SdvUserPool sdvUserPool =
                (SdvUserPool) sdvUserPoolClass.getDeclaredConstructor(IFramework.class,
                        IDynamicStatusStoreService.class, IResourcePoolingService.class)
                        .newInstance(null, null, null);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).performActions(Mockito.any());
        Field dssField = sdvUserPoolClass.getDeclaredField(dssVariableString);
        dssField.setAccessible(true);
        dssField.set(sdvUserPool, dssService);

        // Mock rps
        List<String> allocatedUserCredList = new ArrayList<>();
        allocatedUserCredList.add("CREDS33");
        IResourcePoolingService resourcePoolingService =
                Mockito.mock(IResourcePoolingService.class);
        Mockito.when(resourcePoolingService.obtainResources(userCredList, null, 1, 1, dssService,
                "sdvuser.APPL1.")).thenReturn(allocatedUserCredList);
        Field rpsField = sdvUserPoolClass.getDeclaredField("rps");
        rpsField.setAccessible(true);
        rpsField.set(sdvUserPool, resourcePoolingService);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn("RUN123");
        Field frameworkField = sdvUserPoolClass.getDeclaredField("framework");
        frameworkField.setAccessible(true);
        frameworkField.set(sdvUserPool, framework);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvUserPoolClass.getDeclaredField("LOG");
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        String userCred = sdvUserPool.allocateUser(roleName, mockCicsaRegion);

        Assertions.assertEquals("CREDS33", userCred);

        // Check there is a warning in the log indicating Region A won't record
        Mockito.verify(mockLog, Mockito.times(1)).trace("Allocated SDV User CREDS33 on image "
                + zosImageId + " for CICS Applid APPL1 from SDV User pool allocation");
    }

    @Test
    void testNoUsersFoundForRoleOnImage()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, SdvManagerException, ResourceUnavailableException {
        // Mock IZosImage
        IZosImage mockZosImage = Mockito.mock(IZosImage.class);
        Mockito.when(mockZosImage.getImageID()).thenReturn(zosImageId);

        // Mock CICS region
        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getZosImage()).thenReturn(mockZosImage);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn(applidString);

        // Mocks for statics
        List<String> userCredList = new ArrayList<>();
        sdvPoolUsers.when(() -> SdvPoolUsers.get(zosImageId, roleName)).thenReturn(userCredList);

        // Get SdvUserPool instance
        Class<?> sdvUserPoolClass = Class.forName(sdvUserPoolClassString);
        SdvUserPool sdvUserPool =
                (SdvUserPool) sdvUserPoolClass.getDeclaredConstructor(IFramework.class,
                        IDynamicStatusStoreService.class, IResourcePoolingService.class)
                        .newInstance(null, null, null);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvUserPool.allocateUser(roleName, mockCicsaRegion);
        });

        Assertions.assertEquals(
                "No user credential tags provided for role '" + roleName + "' on z/OS image '"
                        + zosImageId + "'. Create or update CPS property 'sdv.zosImage."
                        + zosImageId + ".role." + roleName + ".credTags'.",
                exception.getMessage());
    }

    @Test
    void testNoResourcesAvailable()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, DynamicStatusStoreMatchException, DynamicStatusStoreException,
            NoSuchFieldException, InsufficientResourcesAvailableException {
        // Mock IZosImage
        IZosImage mockZosImage = Mockito.mock(IZosImage.class);
        Mockito.when(mockZosImage.getImageID()).thenReturn(zosImageId);

        // Mock CICS region
        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getZosImage()).thenReturn(mockZosImage);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn(applidString);

        // Mocks for statics
        List<String> userCredList = new ArrayList<>();
        userCredList.add("CREDS44");
        userCredList.add("CREDS55");
        userCredList.add("CREDS66");
        sdvPoolUsers.when(() -> SdvPoolUsers.get(zosImageId, roleName)).thenReturn(userCredList);

        // Get SdvUserPool instance
        Class<?> sdvUserPoolClass = Class.forName(sdvUserPoolClassString);
        SdvUserPool sdvUserPool =
                (SdvUserPool) sdvUserPoolClass.getDeclaredConstructor(IFramework.class,
                        IDynamicStatusStoreService.class, IResourcePoolingService.class)
                        .newInstance(null, null, null);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService).performActions(Mockito.any());
        Field dssField = sdvUserPoolClass.getDeclaredField(dssVariableString);
        dssField.setAccessible(true);
        dssField.set(sdvUserPool, dssService);

        // Mock rps
        List<String> allocatedUserCredList = new ArrayList<>();
        allocatedUserCredList.add("CREDS66");
        IResourcePoolingService resourcePoolingService =
                Mockito.mock(IResourcePoolingService.class);
        Mockito.when(resourcePoolingService.obtainResources(userCredList, null, 1, 1, dssService,
                "sdvuser.APPL1."))
                .thenThrow(new InsufficientResourcesAvailableException("No Users"));
        Field rpsField = sdvUserPoolClass.getDeclaredField("rps");
        rpsField.setAccessible(true);
        rpsField.set(sdvUserPool, resourcePoolingService);

        // Make call to funtion under test
        ResourceUnavailableException exception =
                Assertions.assertThrows(ResourceUnavailableException.class, () -> {
                    sdvUserPool.allocateUser(roleName, mockCicsaRegion);
                });

        Assertions.assertEquals("No Users", exception.getMessage());
    }

    @Test
    void testDssError()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, DynamicStatusStoreMatchException, DynamicStatusStoreException,
            NoSuchFieldException, InsufficientResourcesAvailableException {
        // Mock IZosImage
        IZosImage mockZosImage = Mockito.mock(IZosImage.class);
        Mockito.when(mockZosImage.getImageID()).thenReturn(zosImageId);

        // Mock CICS region
        ICicsRegionProvisioned mockCicsaRegion = Mockito.mock(ICicsRegionProvisioned.class);
        Mockito.when(mockCicsaRegion.getZosImage()).thenReturn(mockZosImage);
        Mockito.when(mockCicsaRegion.getApplid()).thenReturn(applidString);

        // Mocks for statics
        List<String> userCredList = new ArrayList<>();
        userCredList.add("CREDS77");
        userCredList.add("CREDS88");
        userCredList.add("CREDS99");
        sdvPoolUsers.when(() -> SdvPoolUsers.get(zosImageId, roleName)).thenReturn(userCredList);

        // Get SdvUserPool instance
        Class<?> sdvUserPoolClass = Class.forName(sdvUserPoolClassString);
        SdvUserPool sdvUserPool =
                (SdvUserPool) sdvUserPoolClass.getDeclaredConstructor(IFramework.class,
                        IDynamicStatusStoreService.class, IResourcePoolingService.class)
                        .newInstance(null, null, null);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.doThrow(new DynamicStatusStoreException("something went wrong")).when(dssService)
                .performActions(Mockito.any(), Mockito.any());
        Field dssField = sdvUserPoolClass.getDeclaredField(dssVariableString);
        dssField.setAccessible(true);
        dssField.set(sdvUserPool, dssService);

        // Mock rps
        List<String> allocatedUserCredList = new ArrayList<>();
        allocatedUserCredList.add("CREDS99");
        IResourcePoolingService resourcePoolingService =
                Mockito.mock(IResourcePoolingService.class);
        Mockito.when(resourcePoolingService.obtainResources(userCredList, null, 1, 1, dssService,
                "sdvuser.APPL1.")).thenReturn(allocatedUserCredList);
        Field rpsField = sdvUserPoolClass.getDeclaredField("rps");
        rpsField.setAccessible(true);
        rpsField.set(sdvUserPool, resourcePoolingService);

        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getTestRunName()).thenReturn("RUN123");
        Field frameworkField = sdvUserPoolClass.getDeclaredField("framework");
        frameworkField.setAccessible(true);
        frameworkField.set(sdvUserPool, framework);

        // Make call to funtion under test
        SdvManagerException exception = Assertions.assertThrows(SdvManagerException.class, () -> {
            sdvUserPool.allocateUser(roleName, mockCicsaRegion);
        });

        Assertions.assertEquals(
                "Could not update the DSS for user allocation of SDV User CREDS99 on image "
                        + zosImageId,
                exception.getMessage());
    }

    @Test
    void testDeleteDss()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, DynamicStatusStoreMatchException, DynamicStatusStoreException,
            NoSuchFieldException, InsufficientResourcesAvailableException {
        // Get SdvUserPool instance
        Class<?> sdvUserPoolClass = Class.forName(sdvUserPoolClassString);
        SdvUserPool sdvUserPool =
                (SdvUserPool) sdvUserPoolClass.getDeclaredConstructor(IFramework.class,
                        IDynamicStatusStoreService.class, IResourcePoolingService.class)
                        .newInstance(null, null, null);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(dssService)
            .performActions(Mockito.any(DssDelete.class), Mockito.any(DssDelete.class));
        Field dssField = sdvUserPoolClass.getDeclaredField(dssVariableString);
        dssField.setAccessible(true);
        dssField.set(sdvUserPool, dssService);

        // Make call to funtion under test
        sdvUserPool.deleteDss("user1", "APPL1", "RUN123", dssService);

        // Ensure perform action is called, with 2x dss entries to delete
        Mockito.verify(dssService, Mockito.times(1))
            .performActions(Mockito.any(DssDelete.class), Mockito.any(DssDelete.class));

    }
}
