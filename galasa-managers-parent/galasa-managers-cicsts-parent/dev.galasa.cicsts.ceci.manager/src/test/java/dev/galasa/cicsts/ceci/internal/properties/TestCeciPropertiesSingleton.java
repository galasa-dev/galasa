/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceci.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.cicsts.CeciManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@RunWith(MockitoJUnitRunner.class)
public class TestCeciPropertiesSingleton {
    
    private CeciPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Test
    public void testCpsException() throws CeciManagerException {
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        CeciManagerException expectedException = Assert.assertThrows("expected exception should be thrown", CeciManagerException.class, ()->{
        	CeciPropertiesSingleton.cps();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsException() throws CeciManagerException {
        String expectedMessage = "Attempt to set manager CPS before instance created";
        CeciManagerException expectedException = Assert.assertThrows("expected exception should be thrown", CeciManagerException.class, ()->{
            CeciPropertiesSingleton.setCps(cpsMock);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testCECIPropertiesSingleton() throws CeciManagerException {
        singletonInstance = new CeciPropertiesSingleton();
        singletonInstance.activate();
        CeciPropertiesSingleton.setCps(null);
        CeciPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("CeciPropertiesSingleton.cps() should return the mocked cps", cpsMock, CeciPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
