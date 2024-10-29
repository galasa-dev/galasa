/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.ResourceManagerException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


class TestSdvResourceManagement {

    private String sdvResourceManagementClassString =
            "dev.galasa.sdv.internal.SdvResourceManagement";

    @Test
    void testStart() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, FrameworkException, ResourceManagerException {
        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);

        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getDynamicStatusStoreService("sdv")).thenReturn(dssService);
        Random randomNum = Mockito.mock(Random.class);
        Mockito.when(randomNum.nextInt()).thenReturn(16);
        Mockito.when(framework.getRandom()).thenReturn(randomNum);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);
        ScheduledExecutorService exeService = Mockito.mock(ScheduledExecutorService.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(exeService).scheduleWithFixedDelay(Mockito.any(SdvUserResourceMonitor.class),
                Mockito.any(int.class), Mockito.eq(20), Mockito.eq(TimeUnit.SECONDS));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(exeService).scheduleWithFixedDelay(Mockito.any(SdvManagersResourceMonitor.class),
                Mockito.any(int.class), Mockito.eq(20), Mockito.eq(TimeUnit.SECONDS));
        Mockito.when(resMan.getScheduledExecutorService()).thenReturn(exeService);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvResourceManagementClass = Class.forName(sdvResourceManagementClassString);
        SdvResourceManagement sdvResourceManagement =
                (SdvResourceManagement) sdvResourceManagementClass.getDeclaredConstructor()
                        .newInstance();

        Boolean initialised = sdvResourceManagement.initialise(framework, resMan);
        Assertions.assertTrue(initialised);

        // Make call to funtion under test
        sdvResourceManagement.start();

        Mockito.verify(exeService, Mockito.times(1)).scheduleWithFixedDelay(
                Mockito.any(SdvUserResourceMonitor.class), Mockito.any(long.class),
                Mockito.any(long.class), Mockito.eq(TimeUnit.SECONDS));

        Mockito.verify(exeService, Mockito.times(1)).scheduleWithFixedDelay(
                Mockito.any(SdvManagersResourceMonitor.class), Mockito.any(long.class),
                Mockito.any(long.class), Mockito.eq(TimeUnit.SECONDS));
    }

    @Test
    void testRunFinishedOrDeleted() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, ResourceManagerException,
            DynamicStatusStoreException, NoSuchFieldException {
        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);

        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);
        Mockito.when(framework.getDynamicStatusStoreService("sdv")).thenReturn(dssService);
        Random randomNum = Mockito.mock(Random.class);
        Mockito.when(randomNum.nextInt()).thenReturn(16);
        Mockito.when(framework.getRandom()).thenReturn(randomNum);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);
        ScheduledExecutorService exeService = Mockito.mock(ScheduledExecutorService.class);
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(exeService).scheduleWithFixedDelay(Mockito.any(SdvUserResourceMonitor.class),
                Mockito.any(int.class), Mockito.eq(20), Mockito.eq(TimeUnit.SECONDS));
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(exeService).scheduleWithFixedDelay(Mockito.any(SdvManagersResourceMonitor.class),
                Mockito.any(int.class), Mockito.eq(20), Mockito.eq(TimeUnit.SECONDS));
        Mockito.when(resMan.getScheduledExecutorService()).thenReturn(exeService);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvResourceManagementClass = Class.forName(sdvResourceManagementClassString);
        SdvResourceManagement sdvResourceManagement =
                (SdvResourceManagement) sdvResourceManagementClass.getDeclaredConstructor()
                        .newInstance();

        Boolean initialised = sdvResourceManagement.initialise(framework, resMan);
        Assertions.assertTrue(initialised);

        // Replace the monitors
        SdvUserResourceMonitor userResMon = Mockito.mock(SdvUserResourceMonitor.class);
        SdvManagersResourceMonitor manResMon = Mockito.mock(SdvManagersResourceMonitor.class);

        Field sdvUserResourceMonitorField =
                sdvResourceManagementClass.getDeclaredField("sdvUserResourceMonitor");
        sdvUserResourceMonitorField.setAccessible(true);
        sdvUserResourceMonitorField.set(sdvResourceManagement, userResMon);

        Field sdvManagersResourceMonitorField =
                sdvResourceManagementClass.getDeclaredField("sdvManagersResourceMonitor");
        sdvManagersResourceMonitorField.setAccessible(true);
        sdvManagersResourceMonitorField.set(sdvResourceManagement, manResMon);

        // Make call to funtion under test
        sdvResourceManagement.runFinishedOrDeleted("RUN123");

        Mockito.verify(userResMon, Mockito.times(1)).runFinishedOrDeleted("RUN123");
        Mockito.verify(manResMon, Mockito.times(1)).runFinishedOrDeleted("RUN123");
    }
}
