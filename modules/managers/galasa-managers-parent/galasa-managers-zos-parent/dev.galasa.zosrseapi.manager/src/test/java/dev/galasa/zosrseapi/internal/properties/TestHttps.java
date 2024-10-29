/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal.properties;

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
import dev.galasa.zosrseapi.RseapiManagerException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({RseapiPropertiesSingleton.class, CpsProperties.class})
public class TestHttps {
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    private static final String IMAGE_ID = "IMAGE";
//    
//    @Test
//    public void testConstructor() {
//        Https https = new Https();
//        Assert.assertNotNull("Object was not created", https);
//    }
//    
//    @Test
//    public void testNullandEmpty() throws Exception {
//        Assert.assertEquals("Unexpected value returned from Https.get()", true, getProperty(null));
//        Assert.assertEquals("Unexpected value returned from Https.get()", false, getProperty(""));
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        Assert.assertEquals("Unexpected value returned from Https.get()", true, getProperty("true"));
//        Assert.assertEquals("Unexpected value returned from Https.get()", true, getProperty("TRUE"));
//        Assert.assertEquals("Unexpected value returned from Https.get()", true, getProperty("TrUe"));
//        Assert.assertEquals("Unexpected value returned from Https.get()", false, getProperty("fasle"));
//        Assert.assertEquals("Unexpected value returned from Https.get()", false, getProperty("FALSE"));
//        Assert.assertEquals("Unexpected value returned from Https.get()", false, getProperty("FaLsE"));
//    }
//    
//    @Test
//    public void testInvalid() throws Exception {
//        Assert.assertEquals("Unexpected value returned from Https.get()", false, getProperty("XXX"));
//        Assert.assertEquals("Unexpected value returned from Https.get()", false, getProperty("999"));
//    }
//    
//    @Test
//    public void testException() throws Exception {
//        String expectedMessage = "Problem asking the CPS for the RSE API server use https property for zOS image " + IMAGE_ID;
//        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
//        	getProperty("ANY", true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    private boolean getProperty(String value) throws Exception {
//        return getProperty(value, false);
//    }
//    
//    private boolean getProperty(String value, boolean exception) throws Exception {
//        PowerMockito.spy(RseapiPropertiesSingleton.class);
//        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(RseapiPropertiesSingleton.class, "cps");
//        PowerMockito.spy(CpsProperties.class);
//        
//        if (!exception) {
//            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
//        } else {
//            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
//        }
//        
//        return Https.get(IMAGE_ID);
//    }
}
