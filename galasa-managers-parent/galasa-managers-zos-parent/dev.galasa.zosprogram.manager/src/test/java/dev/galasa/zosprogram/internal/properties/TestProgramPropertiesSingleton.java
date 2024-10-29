/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosprogram.ZosProgramManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestProgramPropertiesSingleton {
    
    private ZosProgramPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Test
    public void testCpsException() throws ZosProgramManagerException {
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ZosProgramManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramManagerException.class, ()->{
        	Assert.assertEquals("Exception", null, ZosProgramPropertiesSingleton.cps());
        });
    	Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsException() throws ZosProgramManagerException {
        String expectedMessage = "Attempt to set manager CPS before instance created";
        ZosProgramManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramManagerException.class, ()->{
            ZosProgramPropertiesSingleton.setCps(cpsMock);
        });
    	Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testZosProgramPropertiesSingleton() throws ZosProgramManagerException {
        singletonInstance = new ZosProgramPropertiesSingleton();
        singletonInstance.activate();
        ZosProgramPropertiesSingleton.setCps(null);
        ZosProgramPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosProgramPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosProgramPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
