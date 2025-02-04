/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.properties;

import java.util.List;

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
public class TestExtraBundles {
    
    @Mock
    private static IConfigurationPropertyStoreService cps;
    private static ImstmPropertiesSingleton ips = new ImstmPropertiesSingleton();
    
    private static final String EXTRA_BUNDLE1 = "EXTRA BUNDLE1";
    private static final String EXTRA_BUNDLE2 = "EXTRA BUNDLE2";
 
    @Before
    public void setup() throws ImstmManagerException, ConfigurationPropertyStoreException {
        ips.activate();
        ImstmPropertiesSingleton.setCps(cps);
    }

    @Test
    public void testNull() throws Exception {        
        Mockito.when(cps.getProperty("extra", "bundles")).thenReturn(null);
        Assert.assertTrue("Expected empty list from ExtraBundles.get()", ExtraBundles.get().isEmpty());
    }
    
    @Test
    public void testBlank() throws Exception {        
        Mockito.when(cps.getProperty("extra", "bundles")).thenReturn("");
        Assert.assertTrue("Expected empty list from ExtraBundles.get()", ExtraBundles.get().isEmpty());
    }
    
    @Test
    public void testNone() throws Exception {        
        Mockito.when(cps.getProperty("extra", "bundles")).thenReturn("none");
        Assert.assertTrue("Expected empty list from ExtraBundles.get()", ExtraBundles.get().isEmpty());
    }
    
    @Test
    public void testValid() throws Exception {
        Mockito.when(cps.getProperty("extra", "bundles")).thenReturn(EXTRA_BUNDLE1+","+EXTRA_BUNDLE2);
        List<String> extras = ExtraBundles.get();
        Assert.assertEquals("Expected 2 items from ExtraBundles.get()", 2, extras.size());
        Assert.assertEquals("Unexpected value for 1st item from ExtraBundles.get()", EXTRA_BUNDLE1, extras.get(0));
        Assert.assertEquals("Unexpected value for 2nd item from ExtraBundles.get()", EXTRA_BUNDLE2, extras.get(1));
    }
    
    @Test
    public void testException() throws Exception {
        Mockito.when(cps.getProperty("extra", "bundles")).thenThrow(new ConfigurationPropertyStoreException());
        String expectedMessage = "Problem asking CPS for the IMS TM extra bundles";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	ExtraBundles.get();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
}
