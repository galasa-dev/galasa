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
public class TestServerPort {
//    
//    private ZosmfPropertiesSingleton properties;
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    private static final String SERVERID = "MFSYSA";
//    
//    private static final String TEST_PORT = "5000";
//    
//    @Test
//    public void testConstructor() {
//        ServerPort serverPort = new ServerPort();
//        Assert.assertNotNull("Object was not created", serverPort);
//    }
//    
//    @Before
//    public void setup() throws ConfigurationPropertyStoreException, ZosmfManagerException {
//        Mockito.when(configurationPropertyStoreServiceMock.getProperty("server", "port", SERVERID)).thenReturn(TEST_PORT);
//        properties = new ZosmfPropertiesSingleton();
//        properties.activate();
//        ZosmfPropertiesSingleton.setCps(configurationPropertyStoreServiceMock);       
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        int port = ServerPort.get(SERVERID);
//        
//        Assert.assertEquals("Unexpected value returned from ServerPort.get()", Integer.parseInt(TEST_PORT), port);
//    }
//    
//    @Test
//    public void testDefault() throws Exception {
//        Mockito.when(configurationPropertyStoreServiceMock.getProperty("server", "port", SERVERID)).thenReturn(null);
//
//        int port = ServerPort.get(SERVERID);
//        
//        Assert.assertEquals("Unexpected value returned from ServerPort.get()", 443, port);
//    }
//    
//    @Test
//    public void testInvalidString() throws Exception {
//    	testInvalid("BOB");
//    }
//    
//    @Test
//    public void testInvalidTooSmall() throws Exception {
//    	testInvalid("-1");
//    }
//    
//    @Test
//    public void testInvalidTooBig() throws Exception {
//        testInvalid("70000");
//    }
//    
//    private void testInvalid(String invalidPort) throws Exception {
//        Mockito.when(configurationPropertyStoreServiceMock.getProperty("server", "port", SERVERID)).thenReturn(invalidPort);
//
//        String expectedMessage = "Invalid value '" + invalidPort + "' for zOSMF server port property for zOS server "  + SERVERID + ". Range  0-65535";
//        
//        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
//        	ServerPort.get(SERVERID);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
}
