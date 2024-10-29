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
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zos.ZosManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosPropertiesSingleton {
    
    private ZosPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Test
    public void testCpsException() throws ZosManagerException {
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
        	ZosPropertiesSingleton.cps();
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsException() throws ZosManagerException {
        String expectedMessage = "Attempt to set manager CPS before instance created";
        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
            ZosPropertiesSingleton.setCps(cpsMock);
        	ZosPropertiesSingleton.cps();
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testZosConsoleZosmfPropertiesSingleton() throws ZosManagerException {
        singletonInstance = new ZosPropertiesSingleton();
        singletonInstance.activate();
        ZosPropertiesSingleton.setCps(null);
        ZosPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosConsoleZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
