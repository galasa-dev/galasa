/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosconsole.ZosConsoleManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosConsoleZosmfPropertiesSingleton {
    
    private ZosConsolePropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Test
    public void testCpsException() throws ZosConsoleManagerException {
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
        	ZosConsolePropertiesSingleton.cps();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsException() throws ZosConsoleManagerException {
        String expectedMessage = "Attempt to set manager CPS before instance created";
        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
        	ZosConsolePropertiesSingleton.setCps(cpsMock);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testZosConsoleZosmfPropertiesSingleton() throws ZosConsoleManagerException {
        singletonInstance = new ZosConsolePropertiesSingleton();
        singletonInstance.activate();
        ZosConsolePropertiesSingleton.setCps(null);
        ZosConsolePropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosConsolePropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosConsolePropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
