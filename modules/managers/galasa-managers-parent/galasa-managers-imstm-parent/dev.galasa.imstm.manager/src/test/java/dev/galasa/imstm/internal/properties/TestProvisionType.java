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
public class TestProvisionType {
    
    @Mock
    private static IConfigurationPropertyStoreService cps;
    private static ImstmPropertiesSingleton ips = new ImstmPropertiesSingleton();
    
    private static final String DEFAULT_TYPE = "PROVISIONED";
    private static final String DSE_LOWER = "dse";
    private static final String DSE = "DSE";
 
    @Before
    public void setup() throws ImstmManagerException, ConfigurationPropertyStoreException {
        ips.activate();
        ImstmPropertiesSingleton.setCps(cps);
    }

    @Test
    public void testNull() throws Exception {        
        Mockito.when(cps.getProperty("provision", "type")).thenReturn(null);
        Assert.assertEquals("Unexpected value returned from ProvisionType.get()", DEFAULT_TYPE, ProvisionType.get());
    }
    
    @Test
    public void testUndefined() throws Exception {        
        Mockito.when(cps.getProperty("provision", "type")).thenReturn("");
        Assert.assertEquals("Unexpected value returned from ProvisionType.get()", DEFAULT_TYPE, ProvisionType.get());
    }
    
    @Test
    public void testDefined() throws Exception {
        Mockito.when(cps.getProperty("provision", "type")).thenReturn(DSE_LOWER);
        Assert.assertEquals("Unexpected value returned from ProvisionType.get()", DSE, ProvisionType.get());
    }
    
    @Test
    public void testCpsException() throws Exception {
        Mockito.when(cps.getProperty("provision", "type")).thenThrow(new ConfigurationPropertyStoreException());
        Assert.assertEquals("Unexpected value returned from ProvisionType.get()", DEFAULT_TYPE, ProvisionType.get());
    }

    @Test
    public void testImsException() throws Exception {
        ips.deactivate();
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	ProvisionType.get();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
}
