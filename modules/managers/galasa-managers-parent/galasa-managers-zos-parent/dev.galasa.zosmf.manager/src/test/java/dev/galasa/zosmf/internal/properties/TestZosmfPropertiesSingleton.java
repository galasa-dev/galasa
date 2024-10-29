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
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosmf.ZosmfManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosmfPropertiesSingleton {
    
    private ZosmfPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Test
    public void testCpsException() throws ZosmfManagerException {
    	String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
            Assert.assertEquals("Exception", null, ZosmfPropertiesSingleton.cps());
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsException() throws ZosmfManagerException {
    	String expectedMessage = "Attempt to set manager CPS before instance created";
        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
            ZosmfPropertiesSingleton.setCps(cpsMock);
            Assert.assertEquals("Exception", null, ZosmfPropertiesSingleton.cps());
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testZosmfPropertiesSingleton() throws ZosmfManagerException {
        singletonInstance = new ZosmfPropertiesSingleton();
        singletonInstance.activate();
        ZosmfPropertiesSingleton.setCps(null);
        ZosmfPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosmfPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
