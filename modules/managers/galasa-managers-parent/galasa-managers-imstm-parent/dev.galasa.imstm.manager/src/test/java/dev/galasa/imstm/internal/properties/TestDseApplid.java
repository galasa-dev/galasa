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
public class TestDseApplid {
    
    @Mock
    private static IConfigurationPropertyStoreService cps;
    private static ImstmPropertiesSingleton ips = new ImstmPropertiesSingleton();
    
    private static final String TAG = "tag";
    private static final String TAG_APPLID_LOWER = "tag_applid";
    private static final String TAG_APPLID = "TAG_APPLID";
 
    @Before
    public void setup() throws ImstmManagerException, ConfigurationPropertyStoreException {
        ips.activate();
        ImstmPropertiesSingleton.setCps(cps);
    }

    @Test
    public void testNull() throws Exception {        
        Mockito.when(cps.getProperty("dse.tag", "applid", TAG)).thenReturn(null);
        Assert.assertNull("Unexpected value returned from DseApplid.get()", DseApplid.get(TAG));
    }
    
    @Test
    public void testBlank() throws Exception {        
        Mockito.when(cps.getProperty("dse.tag", "applid", TAG)).thenReturn("");
        Assert.assertNull("Unexpected value returned from DseApplid.get()", DseApplid.get(TAG));
    }
    
    @Test
    public void testDefined() throws Exception {
        Mockito.when(cps.getProperty("dse.tag", "applid", TAG)).thenReturn(TAG_APPLID_LOWER);
        Assert.assertEquals("Unexpected value returned from DseApplid.get()", TAG_APPLID, DseApplid.get(TAG));
    }
    
    @Test
    public void testCpsException() throws Exception {
        Mockito.when(cps.getProperty("dse.tag", "applid", TAG)).thenThrow(new ConfigurationPropertyStoreException());
        String expectedMessage = "Problem asking CPS for the DSE applid for tag " + TAG;
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	DseApplid.get(TAG);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testImsException() throws Exception {
        ips.deactivate();
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	DseApplid.get(TAG);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
}
