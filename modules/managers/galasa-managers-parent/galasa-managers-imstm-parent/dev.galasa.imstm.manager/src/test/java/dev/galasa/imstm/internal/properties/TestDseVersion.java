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

import dev.galasa.ProductVersion;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.imstm.ImstmManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestDseVersion {
    
    @Mock
    private static IConfigurationPropertyStoreService cps;
    private static ImstmPropertiesSingleton ips = new ImstmPropertiesSingleton();
    
    private static final String TAG = "tag";
    private static final String TAG_VERSION_TEXT = "1.2.3";
    private static final ProductVersion TAG_VERSION = ProductVersion.v(1).r(2).m(3);
    private static final String INVALID_VERSION_TEXT = "1.2.3.4";
 
    @Before
    public void setup() throws ImstmManagerException, ConfigurationPropertyStoreException {
        ips.activate();
        ImstmPropertiesSingleton.setCps(cps);
    }

    @Test
    public void testNull() throws Exception {        
        Mockito.when(cps.getProperty("dse.tag", "version", TAG)).thenReturn(null);
        Assert.assertNull("Unexpected value returned from DseVersion.get()", DseVersion.get(TAG));
    }
    
    @Test
    public void testBlank() throws Exception {        
        Mockito.when(cps.getProperty("dse.tag", "version", TAG)).thenReturn("");
        Assert.assertNull("Unexpected value returned from DseVersion.get()", DseVersion.get(TAG));
    }

    
    @Test
    public void testDefined() throws Exception {
        Mockito.when(cps.getProperty("dse.tag", "version", TAG)).thenReturn(TAG_VERSION_TEXT);
        Assert.assertEquals("Unexpected value returned from DseVersion.get()", TAG_VERSION, DseVersion.get(TAG));
    }
    
    @Test
    public void testCpsException() throws Exception {
        Mockito.when(cps.getProperty("dse.tag", "version", TAG)).thenThrow(new ConfigurationPropertyStoreException());
        String expectedMessage = "Problem accessing the CPS for the IMS version, for tag " + TAG;
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	DseVersion.get(TAG);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testImsException() throws Exception {
        ips.deactivate();
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	DseVersion.get(TAG);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testParseException() throws Exception {
        Mockito.when(cps.getProperty("dse.tag", "version", TAG)).thenReturn(INVALID_VERSION_TEXT);
        String expectedMessage = "Failed to parse the IMS version '" + INVALID_VERSION_TEXT + "' for tag '" + TAG + "', should be a valid V.R.M version format, for example 15.5.0";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	DseVersion.get(TAG);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
}
