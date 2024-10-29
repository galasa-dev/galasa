/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal.properties;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosmf.ZosmfManagerException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ZosmfPropertiesSingleton.class, CpsProperties.class})
public class TestImageServers {
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    @Mock
//    private IZosImage zosImageMock;
//    
//    private static final String IMAGE_ID = "image";
//    
//    @Test
//    public void testConstructor() {
//        ImageServers imageServers = new ImageServers();
//        Assert.assertNotNull("Object was not created", imageServers);
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        List<String> imageServers = Arrays.asList("");
//        Assert.assertEquals("Unexpected value returned from ImageServers.get()", imageServers, getProperty(imageServers));
//        imageServers = Arrays.asList("image1", "image2");
//        Assert.assertEquals("Unexpected value returned from ImageServers.get()", imageServers, getProperty(imageServers));
//    }
//    
//    @Test
//    public void testException() throws Exception {
//        String expectedMessage = "Problem asking the CPS for the zOSMF servers property for zOS image " + IMAGE_ID;
//        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
//        	getProperty(Arrays.asList(""), true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    private List<String> getProperty(List<String> value) throws Exception {
//        return getProperty(value, false);
//    }
//    
//    private List<String> getProperty(List<String> value, boolean exception) throws Exception {
//        PowerMockito.spy(ZosmfPropertiesSingleton.class);
//        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosmfPropertiesSingleton.class, "cps");
//        PowerMockito.spy(CpsProperties.class);
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE_ID);
//        
//        if (!exception) {
//            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringList", Mockito.any(), Mockito.anyString(), Mockito.anyString());
//        } else {
//            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringList", Mockito.any(), Mockito.anyString(), Mockito.anyString());
//        }
//        
//        return ImageServers.get(zosImageMock);
//    }
}
