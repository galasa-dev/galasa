/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.metrics;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

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

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IMetricsServer;
import dev.galasa.framework.spi.MetricsServerException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(LogFactory.class)
public class TestZosMetrics {
//
//    private ZosMetrics zosMetrics;
//    
//    private ZosMetrics zosMetricsSpy;
//    
//    @Mock
//    private ScheduledExecutorService scheduledExecutorServiceMock;
//    
//    @Mock
//    private Random randomMock;
//    
//    @Mock
//    private IFramework frameworkMock;
//    
//    @Mock
//    private IMetricsServer metricsServer;
//
//    @Mock
//    private IDynamicStatusStoreService dssMock;
//    
//    @Mock
//    private Log logMock;
//    
//    private static String logMessage;
//    
//    @Before
//    public void setup() throws DynamicStatusStoreException {
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
//        Mockito.doAnswer(answer).when(logMock).error(Mockito.any(), Mockito.any());
//        
//        Mockito.when(frameworkMock.getDynamicStatusStoreService(Mockito.any())).thenReturn(dssMock);
//        Mockito.doReturn(scheduledExecutorServiceMock).when(metricsServer).getScheduledExecutorService();
//        Mockito.when(frameworkMock.getRandom()).thenReturn(randomMock);
//        
//        zosMetrics = new ZosMetrics();
//        zosMetricsSpy = PowerMockito.spy(zosMetrics);        
//    }
//    
//    @Test
//    public void testZosMetrics() throws MetricsServerException, DynamicStatusStoreException {
//        Assert.assertTrue("initialise() should return true", zosMetricsSpy.initialise(frameworkMock, metricsServer));
//        
//        zosMetricsSpy.start();
//        
//        zosMetricsSpy.shutdown();
//        
//        Mockito.when(dssMock.get(Mockito.any())).thenReturn(null);
//        zosMetricsSpy.run();
//        
//        Mockito.when(dssMock.get(Mockito.any())).thenReturn("1");
//        zosMetricsSpy.run();
//        
//        Mockito.when(dssMock.get(Mockito.any())).thenThrow(new RuntimeException());
//        zosMetricsSpy.run();
//        Assert.assertEquals("run() should log specified message", "Problem with zOS poll", logMessage);
//    }
//    
//    @Test
//    public void testInitialiseException() throws DynamicStatusStoreException, MetricsServerException {
//        Mockito.when(frameworkMock.getDynamicStatusStoreService(Mockito.any())).thenThrow(new DynamicStatusStoreException());
//        String expectedMessage = "Unable to initialise zOS Metrics";
//        MetricsServerException expectedException = Assert.assertThrows("expected exception should be thrown", MetricsServerException.class, ()->{
//        	zosMetricsSpy.initialise(frameworkMock, metricsServer);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
}
