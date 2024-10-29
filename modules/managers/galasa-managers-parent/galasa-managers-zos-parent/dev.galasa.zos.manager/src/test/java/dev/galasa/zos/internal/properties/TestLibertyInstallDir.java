/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import org.junit.Assert;
import org.junit.Before;
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
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosProvisionedImageImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ZosPropertiesSingleton.class, CpsProperties.class})
public class TestLibertyInstallDir {
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    @Mock
//    private ZosProvisionedImageImpl zosProvisionedImageMock;
//    
//    private static final String IMAGE_ID = "image";
//    
//    private static final String LIBERTY_INSTALL_DIR = "/liberty/install/";
//    
//    @Before
//    public void setup() {
//        Mockito.doReturn(IMAGE_ID).when(zosProvisionedImageMock).getImageID();
//    }    
//    
//    @Test
//    public void testConstructor() {
//        LibertyInstallDir LibertyInstallDir = new LibertyInstallDir();
//        Assert.assertNotNull("Object was not created", LibertyInstallDir);
//    }
//    
//    @Test
//    public void testNull() throws Exception {
//        Assert.assertNull("Unexpected value returned from LibertyInstallDir.get()", getProperty(zosProvisionedImageMock, null));
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        Assert.assertEquals("Unexpected value returned from LibertyInstallDir.get()", LIBERTY_INSTALL_DIR, getProperty(zosProvisionedImageMock, LIBERTY_INSTALL_DIR));
//    }
//    
//    @Test
//    public void testException() throws Exception {
//        String expectedMessage = "Problem asking the CPS for the Liberty Install Directory for image "  + IMAGE_ID;
//        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
//            getProperty(zosProvisionedImageMock, null, true);
//        });
//        Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    private String getProperty(ZosProvisionedImageImpl arg, String value) throws Exception {
//        return getProperty(arg, value, false);
//    }
//    
//    private String getProperty(ZosProvisionedImageImpl arg, String value, boolean exception) throws Exception {
//        PowerMockito.spy(ZosPropertiesSingleton.class);
//        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosPropertiesSingleton.class, "cps");
//        PowerMockito.spy(CpsProperties.class);
//        
//        if (!exception) {
//            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
//        } else {
//            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
//        }
//        
//        return LibertyInstallDir.get(arg);
//    }
}
