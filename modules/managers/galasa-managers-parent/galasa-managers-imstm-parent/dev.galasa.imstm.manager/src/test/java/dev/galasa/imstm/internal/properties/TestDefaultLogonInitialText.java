/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.imstm.ImstmManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultLogonInitialText {
    
    @Mock
    private static IConfigurationPropertyStoreService cps;
    private static ImstmPropertiesSingleton ips = new ImstmPropertiesSingleton();
    
    private static final String INITIAL_TEXT = "TEST INITIAL TEXT";
 
    @Before
    public void setup() throws ImstmManagerException, ConfigurationPropertyStoreException {
        ips.activate();
        ImstmPropertiesSingleton.setCps(cps);
    }

    @Test
    public void testUndefined() throws Exception {        
        Assert.assertNull("Unexpected value returned from DefaultLogonInitialText.get()", DefaultLogonInitialText.get());
    }
    
    @Test
    public void testValid() throws Exception {
        Mockito.when(cps.getProperty("default.logon", "initial.text")).thenReturn(INITIAL_TEXT);
        Assert.assertEquals("Unexpected value returned from DefaultLogonInitialText.get()", INITIAL_TEXT, DefaultLogonInitialText.get());
    }
    
    @Test
    public void testException() throws Exception {
        Mockito.when(cps.getProperty("default.logon", "initial.text")).thenThrow(new ConfigurationPropertyStoreException());
        String expectedMessage = "Problem asking CPS for the default logon initial text";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	DefaultLogonInitialText.get();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
}
