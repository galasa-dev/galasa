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
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosrseapi.RseapiManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestRseapiPropertiesSingleton {
    
    private RseapiPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Test
    public void testCpsException() throws RseapiManagerException {
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
        	Assert.assertEquals("Exception", null, RseapiPropertiesSingleton.cps());
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsException() throws RseapiManagerException {
        String expectedMessage = "Attempt to set manager CPS before instance created";
        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
        	RseapiPropertiesSingleton.setCps(cpsMock);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testZosmfPropertiesSingleton() throws RseapiManagerException {
        singletonInstance = new RseapiPropertiesSingleton();
        singletonInstance.activate();
        RseapiPropertiesSingleton.setCps(null);
        RseapiPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, RseapiPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
