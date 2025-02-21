/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zos.ZosManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestImageLogonInitialText {
    
    @Mock
    private static IConfigurationPropertyStoreService cps;
    private static ZosPropertiesSingleton ips = new ZosPropertiesSingleton();
    
    private static final String INITIAL_TEXT = "TEST INITIAL TEXT";
    private static final String TAG = "ZOS_IMAGE_TAG";
 
    @Before
    public void setup() throws ZosManagerException, ConfigurationPropertyStoreException {
        ips.activate();
        ZosPropertiesSingleton.setCps(cps);
    }

    @Test
    public void testUndefined() throws Exception {        
        Assert.assertNull("Unexpected value returned from ImageLogonInitialText.get()", ImageLogonInitialText.get(TAG));
    }
    
    @Test
    public void testValid() throws Exception {
        Mockito.when(cps.getProperty("image", "logon.initial.text", TAG)).thenReturn(INITIAL_TEXT);
        Assert.assertEquals("Unexpected value returned from ImageLogonInitialText.get()", INITIAL_TEXT, ImageLogonInitialText.get(TAG));
    }
    
    @Test
    public void testException() throws Exception {
        Mockito.when(cps.getProperty("image", "logon.initial.text", TAG)).thenThrow(new ConfigurationPropertyStoreException());
        String expectedMessage = "Problem asking the CPS for the logon initial text for z/OS image with tag '"  + TAG + "'";
        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
        	ImageLogonInitialText.get(TAG);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
}
