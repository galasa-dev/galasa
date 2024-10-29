/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.cps.CpsProperties;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ZosPropertiesSingleton.class, CpsProperties.class})
public class TestImageSysname {
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    private static final String SYSNAME = "sysname";
//    
//    @Test
//    public void testConstructor() {
//    	ImageSysname imageSysname = new ImageSysname();
//        Assert.assertNotNull("Object was not created", imageSysname);
//    }
//    
//    @Test
//    public void testNull() throws Exception {        
//        Assert.assertEquals("Unexpected value returned from ImageIdForTag.get()", SYSNAME, getProperty(null));
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        Assert.assertEquals("Unexpected value returned from ImageIdForTag.get()", SYSNAME, getProperty(SYSNAME));
//    }
//    
//    private String getProperty(String value) throws Exception {
//        PowerMockito.spy(ZosPropertiesSingleton.class);
//        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosPropertiesSingleton.class, "cps");
//        PowerMockito.spy(CpsProperties.class);
//        if (value == null) {
//            PowerMockito.doReturn(SYSNAME).when(CpsProperties.class, "getStringWithDefault", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        } else {
//        	PowerMockito.doReturn(value).when(CpsProperties.class, "getStringWithDefault", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        }
//        
//        return ImageSysname.get(value);
//    }
}
