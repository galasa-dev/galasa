/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.internal.properties.JobnamePrefix;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({JobnamePrefix.class})
public class TestZosBatchJobnameImpl {
//    
//    private static final String FIXED_JOBNAME = "GAL45678";
//
//	private static final String FIXED_PREFIX = "PFX4567";
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    @Test
//    public void testGetJobnameImage() throws ZosBatchManagerException {
//    	Mockito.when(zosImageMock.getImageID()).thenReturn("image");
//    	PowerMockito.mockStatic(JobnamePrefix.class);
//    	Mockito.when(JobnamePrefix.get(Mockito.any())).thenReturn(FIXED_PREFIX);
//        ZosBatchJobnameImpl zosJobname = new ZosBatchJobnameImpl(zosImageMock);
//        Assert.assertTrue("getName() should return the supplied value", zosJobname.toString().startsWith(FIXED_PREFIX));
//        Assert.assertTrue("toString() should return the job name", zosJobname.toString().startsWith(FIXED_PREFIX));
//        
//        Mockito.when(JobnamePrefix.get(Mockito.any())).thenThrow(new ZosBatchManagerException());
//        String expectedMessage = "Problem getting batch jobname prefix";
//        ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	new ZosBatchJobnameImpl(zosImageMock);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetJobnameString() {        
//        ZosBatchJobnameImpl zosJobname = new ZosBatchJobnameImpl(FIXED_JOBNAME);
//        Assert.assertEquals("getName() should return the supplied value", FIXED_JOBNAME, zosJobname.getName());
//        Assert.assertEquals("toString() should return the job name", FIXED_JOBNAME, zosJobname.toString());
//    }
}
