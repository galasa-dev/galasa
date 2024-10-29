/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal.properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosmf.ZosmfManagerException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ZosmfPropertiesSingleton.class, CpsProperties.class})
public class TestRequestRetry {
//    
//    private ZosmfPropertiesSingleton properties;
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    private static final String SERVERID = "MFSYSA";
//    
//    private static final String DEFAULT_REQUEST_RETRY = "3";
//    
//    @Test
//    public void testConstructor() {
//        RequestRetry requestRetry = new RequestRetry();
//        Assert.assertNotNull("Object was not created", requestRetry);
//    }
//    
//    @Before
//    public void setup() throws ConfigurationPropertyStoreException, ZosmfManagerException {
//        Mockito.when(configurationPropertyStoreServiceMock.getProperty("command", "request.retry", SERVERID)).thenReturn(DEFAULT_REQUEST_RETRY);
//        properties = new ZosmfPropertiesSingleton();
//        properties.activate();
//        ZosmfPropertiesSingleton.setCps(configurationPropertyStoreServiceMock);       
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        int retry = RequestRetry.get(SERVERID);
//        
//        Assert.assertEquals("Unexpected value returned from RequestRetry.get()", Integer.parseInt(DEFAULT_REQUEST_RETRY), retry);
//    }
//    
//    @Test
//    public void testInvalid() throws Exception {
//        String invalidValue = "BOB";
//        String expectedMessage = "Invalid value given for zosmf.*.request.retry '" + invalidValue + "'";
//        
//        Mockito.when(configurationPropertyStoreServiceMock.getProperty("command", "request.retry", SERVERID)).thenReturn(invalidValue);
//
//        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
//        	RequestRetry.get(SERVERID);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
}
