/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal.properties;

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
import dev.galasa.zosmf.ZosmfManagerException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ZosmfPropertiesSingleton.class, CpsProperties.class})
public class TestServerCreds {
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    private static final String SERVER_ID = "server";
//    
//    private static final String CREDS = "creds"; commented out unit test //pragma: allowlist secret
//    
//    @Test
//    public void testConstructor() {
//        ServerCreds serverCreds = new ServerCreds();
//        Assert.assertNotNull("Object was not created", serverCreds);
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        Assert.assertEquals("Unexpected value returned from ServerCreds.get()", CREDS, getProperty(CREDS));
//        Assert.assertEquals("Unexpected value returned from ServerCreds.get()", "", getProperty(""));
//        Assert.assertNull("Unexpected value returned from ServerCreds.get()", getProperty(null));
//    }
//    
//    @Test
//    public void testException() throws Exception {
//        String expectedMessage = "Problem accessing the CPS when retrieving zOS/MF credentials for server " + SERVER_ID;
//        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
//        	getProperty("ANY", true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    private String getProperty(String value) throws Exception {
//        return getProperty(value, false);
//    }
//    
//    private String getProperty(String value, boolean exception) throws Exception {
//        PowerMockito.spy(ZosmfPropertiesSingleton.class);
//        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosmfPropertiesSingleton.class, "cps");
//        PowerMockito.spy(CpsProperties.class);
//        
//        if (!exception) {
//            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
//        } else {
//            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
//        }
//        
//        return ServerCreds.get(SERVER_ID);
//    }
}
