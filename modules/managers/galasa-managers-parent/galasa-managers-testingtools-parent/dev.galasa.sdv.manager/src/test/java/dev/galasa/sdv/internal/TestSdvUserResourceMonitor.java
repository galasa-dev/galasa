/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sun.misc.Unsafe;


class TestSdvUserResourceMonitor {

    private MockedStatic<SdvUserPool> sdvUserPoolStatic;

    private String sdvUserString = "sdvuser";
    private String sdvUserResourceMonitorClassString =
            "dev.galasa.sdv.internal.SdvUserResourceMonitor";
    private String logString = "LOG";
    private String theUnsafeString = "theUnsafe";

    private String dssEntryUser1RegionA = "sdvuser.APPL1.USER1";
    private String dssEntryUser2RegionA = "sdvuser.APPL1.USER2";
    private String dssEntryUser1RegionB = "sdvuser.APPL2.USER1";
    private String runThreeString = "RUN3";
    private String runFourString = "RUN4";
    private String user1String = "USER1";
    private String user2String = "USER2";

    @SuppressWarnings("PMD")
    private static final Log mockLog = Mockito.mock(Log.class);

    @BeforeEach
    public void setUp() {
        // Registering static mocks before each test
        sdvUserPoolStatic = Mockito.mockStatic(SdvUserPool.class);

        Mockito.reset(mockLog);
        Mockito.when(mockLog.isInfoEnabled()).thenReturn(true);
        Mockito.when(mockLog.isWarnEnabled()).thenReturn(true);
        Mockito.when(mockLog.isErrorEnabled()).thenReturn(true);
        Mockito.when(mockLog.isTraceEnabled()).thenReturn(true);
        Mockito.when(mockLog.isDebugEnabled()).thenReturn(true);
    }

    @AfterEach
    public void tearDown() {
        // Closing static mocks after each test
        sdvUserPoolStatic.close();
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
        Map<String, String> sdvUsersInDss = new HashMap<>();
        Mockito.when(dssService.getPrefix(sdvUserString)).thenReturn(sdvUsersInDss);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvUserResourceMonitorClass = Class.forName(sdvUserResourceMonitorClassString);
        SdvUserResourceMonitor sdvUserResourceMonitor =
                (SdvUserResourceMonitor) sdvUserResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvUserResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvUserResourceMonitor.run();

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any()), Mockito.times(0));
        sdvUserPoolStatic.verifyNoMoreInteractions();

        Assertions.assertTrue(true);
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
        allActiveRuns.add("RUN1");
        allActiveRuns.add("RUN2");
        Mockito.when(frameworkRuns.getActiveRunNames()).thenReturn(allActiveRuns);

        Mockito.when(framework.getFrameworkRuns()).thenReturn(frameworkRuns);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> sdvUsersInDss = new HashMap<>();
        sdvUsersInDss.put(dssEntryUser1RegionA, "RUN1");
        sdvUsersInDss.put(dssEntryUser2RegionA, "RUN1");
        sdvUsersInDss.put(dssEntryUser1RegionB, "RUN2");
        Mockito.when(dssService.getPrefix(sdvUserString)).thenReturn(sdvUsersInDss);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvUserResourceMonitorClass = Class.forName(sdvUserResourceMonitorClassString);
        SdvUserResourceMonitor sdvUserResourceMonitor =
                (SdvUserResourceMonitor) sdvUserResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvUserResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvUserResourceMonitor.run();

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any()), Mockito.times(0));
        sdvUserPoolStatic.verifyNoMoreInteractions();

        Assertions.assertTrue(true);
    }

    @Test
    void testRunWithDssEntriesNotInActiveRuns() throws FrameworkException, ClassNotFoundException,
            NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);
        IFrameworkRuns frameworkRuns = Mockito.mock(IFrameworkRuns.class);

        Set<String> allActiveRuns = new HashSet<String>();
        allActiveRuns.add(runFourString);
        Mockito.when(frameworkRuns.getActiveRunNames()).thenReturn(allActiveRuns);

        Mockito.when(framework.getFrameworkRuns()).thenReturn(frameworkRuns);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> sdvUsersInDss = new HashMap<>();
        sdvUsersInDss.put(dssEntryUser1RegionA, runThreeString);
        sdvUsersInDss.put(dssEntryUser2RegionA, runThreeString);
        sdvUsersInDss.put(dssEntryUser1RegionB, runFourString);
        Mockito.when(dssService.getPrefix(sdvUserString)).thenReturn(sdvUsersInDss);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvUserResourceMonitorClass = Class.forName(sdvUserResourceMonitorClassString);
        SdvUserResourceMonitor sdvUserResourceMonitor =
                (SdvUserResourceMonitor) sdvUserResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvUserResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvUserResourceMonitor.run();

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(user1String), Mockito.any(),
                Mockito.eq(runThreeString), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(user2String), Mockito.any(),
                Mockito.eq(runThreeString), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verifyNoMoreInteractions();

        Assertions.assertTrue(true);
    }

    @Test
    void testRunException() throws FrameworkException, ClassNotFoundException, NoSuchFieldException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);
        IFrameworkRuns frameworkRuns = Mockito.mock(IFrameworkRuns.class);

        Set<String> allActiveRuns = new HashSet<String>();
        allActiveRuns.add(runFourString);
        Mockito.when(frameworkRuns.getActiveRunNames()).thenReturn(allActiveRuns);

        Mockito.when(framework.getFrameworkRuns())
                .thenThrow(new FrameworkException("cannot access framework"));

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> sdvUsersInDss = new HashMap<>();
        sdvUsersInDss.put(dssEntryUser1RegionA, runThreeString);
        sdvUsersInDss.put(dssEntryUser2RegionA, runThreeString);
        sdvUsersInDss.put(dssEntryUser1RegionB, runFourString);
        Mockito.when(dssService.getPrefix(sdvUserString)).thenReturn(sdvUsersInDss);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvUserResourceMonitorClass = Class.forName(sdvUserResourceMonitorClassString);
        SdvUserResourceMonitor sdvUserResourceMonitor =
                (SdvUserResourceMonitor) sdvUserResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvUserResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvUserResourceMonitor.run();

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(user1String), Mockito.any(),
                Mockito.eq(runThreeString), Mockito.any()), Mockito.times(0));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(user2String), Mockito.any(),
                Mockito.eq(runThreeString), Mockito.any()), Mockito.times(0));
        sdvUserPoolStatic.verifyNoMoreInteractions();

        // Verify that although an exception occurred, the program continues, and simply logs
        // an error to the log.
        Mockito.verify(mockLog, Mockito.times(1))
                .error("Failure during scanning DSS for SDV Users");
    }

    @Test
    void testrunFinishedOrDeleted() throws FrameworkException, ClassNotFoundException,
            NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> sdvUsersInDss = new HashMap<>();
        sdvUsersInDss.put(dssEntryUser1RegionA, runThreeString);
        sdvUsersInDss.put(dssEntryUser2RegionA, runThreeString);
        sdvUsersInDss.put(dssEntryUser1RegionB, runFourString);
        Mockito.when(dssService.getPrefix(sdvUserString)).thenReturn(sdvUsersInDss);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvUserResourceMonitorClass = Class.forName(sdvUserResourceMonitorClassString);
        SdvUserResourceMonitor sdvUserResourceMonitor =
                (SdvUserResourceMonitor) sdvUserResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvUserResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvUserResourceMonitor.runFinishedOrDeleted(runThreeString);

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(user1String), Mockito.any(),
                Mockito.eq(runThreeString), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(user2String), Mockito.any(),
                Mockito.eq(runThreeString), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verifyNoMoreInteractions();

        Assertions.assertTrue(true);
    }

    @Test
    void testrunFinishedOrDeletedException() throws FrameworkException, ClassNotFoundException,
            NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> sdvUsersInDss = new HashMap<>();
        sdvUsersInDss.put(dssEntryUser1RegionA, runThreeString);
        sdvUsersInDss.put(dssEntryUser2RegionA, runThreeString);
        sdvUsersInDss.put(dssEntryUser1RegionB, runFourString);
        Mockito.when(dssService.getPrefix(sdvUserString))
                .thenThrow(new DynamicStatusStoreException("cannot read dss"));

        // Get SdvUserResourceMonitor instance
        Class<?> sdvUserResourceMonitorClass = Class.forName(sdvUserResourceMonitorClassString);
        SdvUserResourceMonitor sdvUserResourceMonitor =
                (SdvUserResourceMonitor) sdvUserResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvUserResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        // Make call to funtion under test
        sdvUserResourceMonitor.runFinishedOrDeleted(runThreeString);

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(user1String), Mockito.any(),
                Mockito.eq(runThreeString), Mockito.any()), Mockito.times(0));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(user2String), Mockito.any(),
                Mockito.eq(runThreeString), Mockito.any()), Mockito.times(0));
        sdvUserPoolStatic.verifyNoMoreInteractions();

        // Verify that although an exception occurred, the program continues, and simply logs
        // an error to the log.
        Mockito.verify(mockLog, Mockito.times(1))
                .error("Failure cleaning up SDV Users for finished run " + runThreeString);
    }

    @Test
    void testrunFinishedOrDeletedDssDeleteException()
            throws FrameworkException, ClassNotFoundException, NoSuchFieldException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        // Mock framework
        IFramework framework = Mockito.mock(IFramework.class);

        // Mock resourceManagement
        IResourceManagement resMan = Mockito.mock(IResourceManagement.class);

        // Mock dss
        IDynamicStatusStoreService dssService = Mockito.mock(IDynamicStatusStoreService.class);
        Map<String, String> sdvUsersInDss = new HashMap<>();
        sdvUsersInDss.put(dssEntryUser1RegionA, runThreeString);
        sdvUsersInDss.put(dssEntryUser2RegionA, runThreeString);
        sdvUsersInDss.put(dssEntryUser1RegionB, runFourString);
        Mockito.when(dssService.getPrefix(sdvUserString)).thenReturn(sdvUsersInDss);

        // Get SdvUserResourceMonitor instance
        Class<?> sdvUserResourceMonitorClass = Class.forName(sdvUserResourceMonitorClassString);
        SdvUserResourceMonitor sdvUserResourceMonitor =
                (SdvUserResourceMonitor) sdvUserResourceMonitorClass
                        .getDeclaredConstructor(IFramework.class, IResourceManagement.class,
                                IDynamicStatusStoreService.class)
                        .newInstance(framework, resMan, dssService);

        // Replace LOG
        final Field unsafeField = Unsafe.class.getDeclaredField(theUnsafeString);
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field loggerField = sdvUserResourceMonitorClass.getDeclaredField(logString);
        final Object staticLoggerFieldBase = unsafe.staticFieldBase(loggerField);
        final long staticLoggerFieldOffset = unsafe.staticFieldOffset(loggerField);
        unsafe.putObject(staticLoggerFieldBase, staticLoggerFieldOffset, mockLog);

        sdvUserPoolStatic.when(() -> SdvUserPool.deleteDss(Mockito.eq(user1String), Mockito.any(),
        Mockito.eq(runThreeString), Mockito.any()))
            .thenThrow(new DynamicStatusStoreMatchException("not found"));

        // Make call to funtion under test
        sdvUserResourceMonitor.runFinishedOrDeleted(runThreeString);

        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(user1String), Mockito.any(),
                Mockito.eq(runThreeString), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verify(() -> SdvUserPool.deleteDss(Mockito.eq(user2String), Mockito.any(),
                Mockito.eq(runThreeString), Mockito.any()), Mockito.times(1));
        sdvUserPoolStatic.verifyNoMoreInteractions();

        // Verify that although an exception occurred, the program continues, and simply logs
        // an error to the log.
        Mockito.verify(mockLog, Mockito.times(1))
            .error("Failure in discarding SDV User " + user1String + " on CICS Applid APPL1"
                + " allocated to run " + runThreeString);
    }
}
