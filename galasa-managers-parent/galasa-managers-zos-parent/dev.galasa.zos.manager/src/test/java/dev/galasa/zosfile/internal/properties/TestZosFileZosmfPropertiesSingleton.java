/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosfile.ZosFileManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosFileZosmfPropertiesSingleton {
    
    private ZosFilePropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Test
    public void testCpsException() throws ZosFileManagerException {
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ZosFileManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosFileManagerException.class, ()->{
        	ZosFilePropertiesSingleton.cps();
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsException() throws ZosFileManagerException {
        String expectedMessage = "Attempt to set manager CPS before instance created";
        ZosFileManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosFileManagerException.class, ()->{
            ZosFilePropertiesSingleton.setCps(cpsMock);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testZosFileZosmfPropertiesSingleton() throws ZosFileManagerException {
        singletonInstance = new ZosFilePropertiesSingleton();
        singletonInstance.activate();
        ZosFilePropertiesSingleton.setCps(null);
        ZosFilePropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosFileZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosFilePropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
