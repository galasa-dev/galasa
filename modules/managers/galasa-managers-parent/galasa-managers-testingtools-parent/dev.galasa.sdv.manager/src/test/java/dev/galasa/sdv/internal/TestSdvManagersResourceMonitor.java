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
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourceManagement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sun.misc.Unsafe;


class TestSdvManagersResourceMonitor {

    private String runningManagersPrefixString = "manager.runningManagers";
    private String sdvManagersResourceMonitorClassString =
            "dev.galasa.sdv.internal.SdvManagersResourceMonitor";
    private String logString = "LOG";
    private String theUnsafeString = "theUnsafe";

    private String runOneString = "RUN1";
    private String runTwoString = "RUN2";
    private String runThreeString = "RUN3";
    private String runningManagersApplid1 = "manager.runningManagers.APPL1";
    private String runningManagersApplid2 = "manager.runningManagers.APPL2";

    @SuppressWarnings("PMD")
    private static final Log mockLog = Mockito.mock(Log.class);

    @BeforeEach
    public void setUp() {
        Mockito.reset(mockLog);
        Mockito.when(mockLog.isInfoEnabled()).thenReturn(true);
        Mockito.when(mockLog.isWarnEnabled()).thenReturn(true);
        Mockito.when(mockLog.isErrorEnabled()).thenReturn(true);
        Mockito.when(mockLog.isTraceEnabled()).thenReturn(true);
        Mockito.when(mockLog.isDebugEnabled()).thenReturn(true);
    }

    @Test
    void testRunWithNoActiveRunsOrDssEntries()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, FrameworkException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);
        IFrameworkRuns frameworkRuns = Mockito.mock(IFrameworkRuns.class);

        Set<String> allActiveRuns = new HashSet<String>();
        Mockito.when(frameworkRuns.getActiveRunNames()).thenReturn(allActiveRuns);

        Mockito.when(framework.getFrameworkRuns()).thenReturn(frameworkRuns);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> runningManagersInDss = new HashMap<>();
        Mockito.when(dssService.getPrefix(runningManagersPrefixString))
                .thenReturn(runningManagersInDss);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvManagersResourceMonitorClass =
                Class.forName(sdvManagersResourceMonitorClassString);
        SdvManagersResourceMonitor sdvManagersResourceMonitor =
                (SdvManagersResourceMonitor) sdvManagersResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagersResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManagersResourceMonitor.run();

        Mockito.verify(dssService, Mockito.times(0)).delete(Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(0)).put(Mockito.any(String.class),
                Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(0)).performActions(Mockito.any());
    }

    @Test
    void testRunWithActiveRunsMatchingDssEntries()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, NoSuchFieldException, FrameworkException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);
        IFrameworkRuns frameworkRuns = Mockito.mock(IFrameworkRuns.class);

        Set<String> allActiveRuns = new HashSet<String>();
        allActiveRuns.add(runOneString);
        allActiveRuns.add(runTwoString);
        allActiveRuns.add(runThreeString);
        Mockito.when(frameworkRuns.getActiveRunNames()).thenReturn(allActiveRuns);

        Mockito.when(framework.getFrameworkRuns()).thenReturn(frameworkRuns);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> runningManagersInDss = new HashMap<>();
        runningManagersInDss.put(runningManagersApplid1, runOneString + "," + runTwoString);
        runningManagersInDss.put(runningManagersApplid2, runThreeString);
        Mockito.when(dssService.getPrefix(runningManagersPrefixString))
                .thenReturn(runningManagersInDss);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvManagersResourceMonitorClass =
                Class.forName(sdvManagersResourceMonitorClassString);
        SdvManagersResourceMonitor sdvManagersResourceMonitor =
                (SdvManagersResourceMonitor) sdvManagersResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagersResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManagersResourceMonitor.run();

        Mockito.verify(dssService, Mockito.times(0)).delete(Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(0)).put(Mockito.any(String.class),
                Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(0)).performActions(Mockito.any());
    }

    @Test
    void testRunException()
            throws DynamicStatusStoreException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.getPrefix(runningManagersPrefixString))
                .thenThrow(new DynamicStatusStoreException("cannot read store"));

        // Get SdvUserResourceMonitor instance
        Class<?> sdvManagersResourceMonitorClass =
                Class.forName(sdvManagersResourceMonitorClassString);
        SdvManagersResourceMonitor sdvManagersResourceMonitor =
                (SdvManagersResourceMonitor) sdvManagersResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagersResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManagersResourceMonitor.run();

        Mockito.verify(dssService, Mockito.times(0)).delete(Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(0)).put(Mockito.any(String.class),
                Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(0)).performActions(Mockito.any());

        // Verify that although an exception occurred, the program continues, and simply logs
        // an error to the log.
        Mockito.verify(mockLog, Mockito.times(1))
                .error("Failure during scanning DSS for SDV Managers");
    }

    @Test
    void testRunWithDssEntryNotInActiveRunsButNotLastManagerOnRegion()
            throws FrameworkException, ClassNotFoundException, NoSuchFieldException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);
        IFrameworkRuns frameworkRuns = Mockito.mock(IFrameworkRuns.class);

        Set<String> allActiveRuns = new HashSet<String>();
        allActiveRuns.add(runTwoString);
        allActiveRuns.add(runThreeString);
        Mockito.when(frameworkRuns.getActiveRunNames()).thenReturn(allActiveRuns);

        Mockito.when(framework.getFrameworkRuns()).thenReturn(frameworkRuns);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> runningManagersInDss = new HashMap<>();
        runningManagersInDss.put(runningManagersApplid1, runOneString + "," + runTwoString);
        runningManagersInDss.put(runningManagersApplid2, runThreeString);
        Mockito.when(dssService.getPrefix(runningManagersPrefixString))
                .thenReturn(runningManagersInDss);
        Mockito.when(dssService.get(runningManagersApplid1))
                .thenReturn(runOneString + "," + runTwoString);
        Mockito.when(dssService.get(runningManagersApplid2)).thenReturn(runThreeString);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvManagersResourceMonitorClass =
                Class.forName(sdvManagersResourceMonitorClassString);
        SdvManagersResourceMonitor sdvManagersResourceMonitor =
                (SdvManagersResourceMonitor) sdvManagersResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagersResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManagersResourceMonitor.run();

        Mockito.verify(dssService, Mockito.times(0)).delete(Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(1)).put(runningManagersApplid1, runTwoString);
        Mockito.verify(dssService, Mockito.times(0)).performActions(Mockito.any());
    }

    @Test
    void testRunWithDssEntryNotInActiveRunsButIsLastManagerOnRegion()
            throws FrameworkException, ClassNotFoundException, NoSuchFieldException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);
        IFrameworkRuns frameworkRuns = Mockito.mock(IFrameworkRuns.class);

        Set<String> allActiveRuns = new HashSet<String>();
        allActiveRuns.add(runOneString);
        allActiveRuns.add(runTwoString);
        Mockito.when(frameworkRuns.getActiveRunNames()).thenReturn(allActiveRuns);

        Mockito.when(framework.getFrameworkRuns()).thenReturn(frameworkRuns);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> runningManagersInDss = new HashMap<>();
        runningManagersInDss.put(runningManagersApplid1, runOneString + "," + runTwoString);
        runningManagersInDss.put(runningManagersApplid2, runThreeString);
        Mockito.when(dssService.getPrefix(runningManagersPrefixString))
                .thenReturn(runningManagersInDss);
        Mockito.when(dssService.get(runningManagersApplid2)).thenReturn(runThreeString);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvManagersResourceMonitorClass =
                Class.forName(sdvManagersResourceMonitorClassString);
        SdvManagersResourceMonitor sdvManagersResourceMonitor =
                (SdvManagersResourceMonitor) sdvManagersResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagersResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManagersResourceMonitor.run();

        Mockito.verify(dssService, Mockito.times(0)).delete(Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(0)).put(Mockito.any(String.class),
                Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(1)).performActions(Mockito.any(), Mockito.any());
    }

    @Test
    void testrunFinishedOrDeletedIsLastManagerOnRegion()
            throws FrameworkException, ClassNotFoundException, NoSuchFieldException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> runningManagersInDss = new HashMap<>();
        runningManagersInDss.put(runningManagersApplid1, runOneString + "," + runTwoString);
        runningManagersInDss.put(runningManagersApplid2, runThreeString);
        Mockito.when(dssService.getPrefix(runningManagersPrefixString))
                .thenReturn(runningManagersInDss);
        Mockito.when(dssService.get(runningManagersApplid1))
                .thenReturn(runOneString + "," + runTwoString);
        Mockito.when(dssService.get(runningManagersApplid2)).thenReturn(runThreeString);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvManagersResourceMonitorClass =
                Class.forName(sdvManagersResourceMonitorClassString);
        SdvManagersResourceMonitor sdvManagersResourceMonitor =
                (SdvManagersResourceMonitor) sdvManagersResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagersResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManagersResourceMonitor.runFinishedOrDeleted(runOneString);

        Mockito.verify(dssService, Mockito.times(0)).delete(Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(1)).put(runningManagersApplid1, runTwoString);
        Mockito.verify(dssService, Mockito.times(0)).performActions(Mockito.any());
    }

    @Test
    void testrunFinishedOrDeletedNotLastManagerOnRegion()
            throws FrameworkException, ClassNotFoundException, NoSuchFieldException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> runningManagersInDss = new HashMap<>();
        runningManagersInDss.put(runningManagersApplid1, runOneString + "," + runTwoString);
        runningManagersInDss.put(runningManagersApplid2, runThreeString);
        Mockito.when(dssService.getPrefix(runningManagersPrefixString))
                .thenReturn(runningManagersInDss);
        Mockito.when(dssService.get(runningManagersApplid2)).thenReturn(runThreeString);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvManagersResourceMonitorClass =
                Class.forName(sdvManagersResourceMonitorClassString);
        SdvManagersResourceMonitor sdvManagersResourceMonitor =
                (SdvManagersResourceMonitor) sdvManagersResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagersResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManagersResourceMonitor.runFinishedOrDeleted(runThreeString);

        Mockito.verify(dssService, Mockito.times(0)).delete(Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(0)).put(Mockito.any(String.class),
                Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(1)).performActions(Mockito.any(), Mockito.any());
    }

    @Test
    void testrunFinishedOrDeletedException()
            throws DynamicStatusStoreException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Mockito.when(dssService.getPrefix(runningManagersPrefixString))
                .thenThrow(new DynamicStatusStoreException("cannot read store"));

        // Get SdvUserResourceMonitor instance
        Class<?> sdvManagersResourceMonitorClass =
                Class.forName(sdvManagersResourceMonitorClassString);
        SdvManagersResourceMonitor sdvManagersResourceMonitor =
                (SdvManagersResourceMonitor) sdvManagersResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvManagersResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvManagersResourceMonitor.runFinishedOrDeleted(runOneString);

        Mockito.verify(dssService, Mockito.times(0)).delete(Mockito.any(String.class));
        Mockito.verify(dssService, Mockito.times(0)).put(runningManagersApplid1, runTwoString);
        Mockito.verify(dssService, Mockito.times(0)).performActions(Mockito.any());

        // Verify that although an exception occurred, the program continues, and simply logs
        // an error to the log.
        Mockito.verify(mockLog, Mockito.times(1))
                .error("Failure cleaning up SDV Managers for finished run RUN1");
    }
}
