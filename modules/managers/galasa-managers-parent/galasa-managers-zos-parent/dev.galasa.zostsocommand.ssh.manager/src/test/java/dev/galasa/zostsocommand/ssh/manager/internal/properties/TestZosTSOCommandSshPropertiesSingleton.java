/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand.ssh.manager.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zostsocommand.ZosTSOCommandManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosTSOCommandSshPropertiesSingleton {
    
    private ZosTSOCommandSshPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Test
    public void testCpsException() throws ZosTSOCommandManagerException {
    	String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ZosTSOCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandManagerException.class, ()->{
        	ZosTSOCommandSshPropertiesSingleton.cps();
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsException() throws ZosTSOCommandManagerException {
        String expectedMessage = "Attempt to set manager CPS before instance created";
        ZosTSOCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandManagerException.class, ()->{
        	ZosTSOCommandSshPropertiesSingleton.setCps(cpsMock);;
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void ZosTSOCommandSshPropertiesSingleton() throws ZosTSOCommandManagerException {
        singletonInstance = new ZosTSOCommandSshPropertiesSingleton();
        singletonInstance.activate();
        ZosTSOCommandSshPropertiesSingleton.setCps(null);
        ZosTSOCommandSshPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosTSOCommandSshPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosTSOCommandSshPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
