/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosunixcommand.ssh.manager.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosunixcommand.ZosUNIXCommandManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosUNIXCommandSshPropertiesSingleton {
    
    private ZosUNIXCommandSshPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Test
    public void testCpsException() throws ZosUNIXCommandManagerException {
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ZosUNIXCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandManagerException.class, ()->{
        	ZosUNIXCommandSshPropertiesSingleton.cps();
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsException() throws ZosUNIXCommandManagerException {
        String expectedMessage = "Attempt to set manager CPS before instance created";
        ZosUNIXCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandManagerException.class, ()->{
        	ZosUNIXCommandSshPropertiesSingleton.setCps(cpsMock);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testZosUNIXCommandSshPropertiesSingleton() throws ZosUNIXCommandManagerException {
        singletonInstance = new ZosUNIXCommandSshPropertiesSingleton();
        singletonInstance.activate();
        ZosUNIXCommandSshPropertiesSingleton.setCps(null);
        ZosUNIXCommandSshPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosUNIXCommandSshPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosUNIXCommandSshPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
