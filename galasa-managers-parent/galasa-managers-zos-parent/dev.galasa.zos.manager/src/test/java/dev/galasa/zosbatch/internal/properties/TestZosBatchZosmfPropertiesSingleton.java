/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosbatch.ZosBatchManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosBatchZosmfPropertiesSingleton {
    
    private ZosBatchPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Test
    public void testCpsException() throws ZosBatchManagerException {
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
        	ZosBatchPropertiesSingleton.cps();
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsException() throws ZosBatchManagerException {
        String expectedMessage = "Attempt to set manager CPS before instance created";
        ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
            ZosBatchPropertiesSingleton.setCps(cpsMock);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testZosBatchZosmfPropertiesSingleton() throws ZosBatchManagerException {
        singletonInstance = new ZosBatchPropertiesSingleton();
        singletonInstance.activate();
        ZosBatchPropertiesSingleton.setCps(null);
        ZosBatchPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosBatchZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosBatchPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
