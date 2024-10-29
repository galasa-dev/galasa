/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourceManagement;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ZosProvisionedImageImpl.class, LogFactory.class})
public class TestSlotResourceMonitor {
//    
//    private SlotResourceMonitor slotResourceMonitor;
//    
//    private SlotResourceMonitor slotResourceMonitorSpy;
//    
//    @Mock
//    private IFramework frameworkMock;
//    
//    @Mock
//    private IFrameworkRuns frameworkRuns;
//
//    @Mock
//    private IResourceManagement resourceManagementMock;
//
//    @Mock
//    private IDynamicStatusStoreService dssMock;
//
//    @Mock
//    private ZosResourceManagement zosResourceManagementMock;
//
//    @Mock
//    private IConfigurationPropertyStoreService cpsMock;
//
//    @Mock
//    private Log logMock;
//    
//    private static String logMessage;
//    
//    @Before
//    public void setup() throws Exception {
//        PowerMockito.mockStatic(LogFactory.class);
//        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
//        Answer<String> answer = new Answer<String>() {
//            @Override
//            public String answer(InvocationOnMock invocation) throws Throwable {
//                logMessage = invocation.getArgument(0);
//                System.err.println("Captured Log Message:\n" + logMessage);
//                if (invocation.getArguments().length > 1 && invocation.getArgument(1) instanceof Throwable) {
//                    ((Throwable) invocation.getArgument(1)).printStackTrace();
//                }
//                return null;
//            }
//        };
//        Mockito.doAnswer(answer).when(logMock).error(Mockito.any());
//        Mockito.doAnswer(answer).when(logMock).error(Mockito.any(), Mockito.any());
//    }
//    
//    @Test
//    public void testRun() throws Exception {
//        Map<String, String> slotRuns = new HashMap<>();
//        Mockito.when(dssMock.getPrefix(Mockito.any())).thenReturn(slotRuns);
//        
//        Set<String> activeRunNames = new HashSet<>();
//        Mockito.when(frameworkRuns.getActiveRunNames()).thenReturn(activeRunNames);        
//        Mockito.when(frameworkMock.getFrameworkRuns()).thenReturn(frameworkRuns);
//        
//        PowerMockito.mockStatic(ZosProvisionedImageImpl.class);
//        PowerMockito.doNothing().when(ZosProvisionedImageImpl.class, "deleteDss", Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
//
//        slotResourceMonitor = new SlotResourceMonitor(frameworkMock, resourceManagementMock, dssMock, zosResourceManagementMock, cpsMock);
//        slotResourceMonitorSpy = PowerMockito.spy(slotResourceMonitor);
//        
//        slotResourceMonitorSpy.run();
//        
//
//        slotRuns.put("slot.run.RUN1.image.SYS1.slot.SLOT_RUN1", "active");
//        slotRuns.put("slot.run.RUN2.image.SYS1.slot.SLOT_RUN2", "active");
//        slotRuns.put("slot.run.RUN3.image.SYS1.slot.SLOT_RUN3", "active");
//        slotRuns.put("XXXX", "active");
//        
//        slotResourceMonitorSpy.run();
//        
//        activeRunNames.add("RUN1");
//        activeRunNames.add("RUN2");        
//        PowerMockito.doThrow(new RuntimeException()).when(ZosProvisionedImageImpl.class, "deleteDss", Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
//        
//        slotResourceMonitorSpy.run();
//        Assert.assertEquals("run() should log specified message", "Failed to discard slot SLOT_RUN3 on image SYS1 as run RUN3", logMessage);
//        
//        Mockito.when(dssMock.getPrefix(Mockito.any())).thenThrow(new RuntimeException());
//        slotResourceMonitorSpy.run();
//        Assert.assertEquals("run() should log specified message", "Failure during slot scan", logMessage);
//    }
//    
//    @Test
//    public void testRunFinishedOrDeleted() throws Exception {
//        Map<String, String> slotRuns = new HashMap<>();
//        Mockito.when(dssMock.getPrefix(Mockito.any())).thenReturn(slotRuns);
//        
//        Set<String> activeRunNames = new HashSet<>();
//        Mockito.when(frameworkRuns.getActiveRunNames()).thenReturn(activeRunNames);        
//        Mockito.when(frameworkMock.getFrameworkRuns()).thenReturn(frameworkRuns);
//        
//        PowerMockito.mockStatic(ZosProvisionedImageImpl.class);
//        PowerMockito.doNothing().when(ZosProvisionedImageImpl.class, "deleteDss", Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
//
//        slotResourceMonitor = new SlotResourceMonitor(frameworkMock, resourceManagementMock, dssMock, zosResourceManagementMock, cpsMock);
//        slotResourceMonitorSpy = PowerMockito.spy(slotResourceMonitor);
//        
//        slotResourceMonitorSpy.runFinishedOrDeleted("RUN2");
//        
//
//        slotRuns.put("slot.run.RUN1.image.SYS1.slot.SLOT_RUN1", "active");
//        slotRuns.put("slot.run.RUN2.image.SYS1.slot.SLOT_RUN2", "active");
//        slotRuns.put("slot.run.RUN3.image.SYS1.slot.SLOT_RUN3", "active");
//        slotRuns.put("XXXX", "active");
//        
//        slotResourceMonitorSpy.runFinishedOrDeleted("RUN2");
//        
//        activeRunNames.add("RUN1");
//        activeRunNames.add("RUN2");        
//        PowerMockito.doThrow(new RuntimeException()).when(ZosProvisionedImageImpl.class, "deleteDss", Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
//        
//        slotResourceMonitorSpy.runFinishedOrDeleted("RUN2");
//        Assert.assertEquals("run() should log specified message", "Failed to discard slot SLOT_RUN2 on image SYS1 as run RUN2", logMessage);
//        
//        Mockito.when(dssMock.getPrefix(Mockito.any())).thenThrow(new RuntimeException());
//        slotResourceMonitorSpy.runFinishedOrDeleted("RUN2");
//        Assert.assertEquals("run() should log specified message", "Failed to delete slots for run RUN2", logMessage);
//    }
}
