/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.properties;

import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.imstm.ImstmManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestImstmPropertiesSingleton {

    ImstmPropertiesSingleton singleton = new ImstmPropertiesSingleton();
    @Mock
    private static IConfigurationPropertyStoreService cps;

    @Before
    public void deactivate() {
        singleton.deactivate();
    }

    @Test
    public void testCpsNotActivated() throws Exception {
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	ImstmPropertiesSingleton.getCps();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsNotActivated() throws Exception {
        String expectedMessage = "Attempt to set manager CPS before instance created";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	ImstmPropertiesSingleton.setCps(cps);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsActivated() throws Exception {
        singleton.activate();
        boolean exception = false;
        try {
            ImstmPropertiesSingleton.setCps(cps);
        } catch (Exception e) {
            exception = true;
        }
        Assert.assertFalse("Exception is not expected", exception);
    }

    @Test
    public void testCpsActivatedNotSet() throws Exception {
        singleton.activate();
        try {
            Assert.assertNull("No CPS expected", ImstmPropertiesSingleton.getCps());
        } catch (Exception e) {
            Assert.assertFalse("Exception is not expected", false);
        }
    }
    
    @Test
    public void testCpsActivatedSet() throws Exception {
        singleton.activate();
        try {
            ImstmPropertiesSingleton.setCps(cps);
            Assert.assertEquals("Unexpected CPS returned", cps, ImstmPropertiesSingleton.getCps());
        } catch (Exception e) {
            Assert.assertFalse("Exception is not expected", false);
        }
    }
    
    @Test
    public void testCpsDeactivated() throws Exception {
        singleton.activate();
        singleton.deactivate();
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	ImstmPropertiesSingleton.getCps();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSetCpsDeactivated() throws Exception {
        singleton.activate();
        singleton.deactivate();
        String expectedMessage = "Attempt to set manager CPS before instance created";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	ImstmPropertiesSingleton.setCps(cps);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
}
