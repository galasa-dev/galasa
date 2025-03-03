/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal;

import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.ProductVersion;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.imstm.ImstmManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestImstmProperties {

    ImstmProperties properties;
    @Mock private IConfigurationPropertyStoreService cps;
    @Mock private IFramework framework;

    private static String NAMESPACE = "imstm";
    private static final String TAG = "tag";
    private static final String TAG_APPLID_LOWER = "tag_applid";
    private static final String TAG_APPLID = "TAG_APPLID";
    private static final String TAG_VERSION_TEXT = "1.2.3";
    private static final ProductVersion TAG_VERSION = ProductVersion.v(1).r(2).m(3);
    private static final String INVALID_VERSION_TEXT = "1.2.3.4";
    private static final String DEFAULT_TYPE = "DSE";
    private static final String TYPE_LOWER = "test_type";
    private static final String TYPE = "TEST_TYPE";

    @Before
    public void setup() throws Exception {
        Mockito.when(framework.getConfigurationPropertyService(NAMESPACE)).thenReturn(cps);
        properties = new ImstmProperties(framework);
    }

    @Test
    public void testGetDseApplidNull() throws Exception {        
        Mockito.when(cps.getProperty("dse.tag." + TAG, "applid")).thenReturn(null);
        Assert.assertNull("Unexpected value returned from ImstmProperties.getDseApplid()", properties.getDseApplid(TAG));

        // A second request should use a cached copy
        Mockito.verify(cps).getProperty("dse.tag." + TAG, "applid");
        Assert.assertNull("Unexpected value returned from ImstmProperties.getDseApplid()", properties.getDseApplid(TAG));
        Mockito.verifyNoMoreInteractions(cps);
    }
    
    @Test
    public void testGetDseApplidBlank() throws Exception {        
        Mockito.when(cps.getProperty("dse.tag." + TAG, "applid")).thenReturn("");
        Assert.assertNull("Unexpected value returned from ImstmProperties.getDseApplid()", properties.getDseApplid(TAG));

        // A second request should use a cached copy
        Mockito.verify(cps).getProperty("dse.tag." + TAG, "applid");
        Assert.assertNull("Unexpected value returned from ImstmProperties.getDseApplid()", properties.getDseApplid(TAG));
        Mockito.verifyNoMoreInteractions(cps);
    }
    
    @Test
    public void testGetDseApplidDefined() throws Exception {
        Mockito.when(cps.getProperty("dse.tag." + TAG, "applid")).thenReturn(TAG_APPLID_LOWER);
        Assert.assertEquals("Unexpected value returned from ImstmProperties.getDseApplid()", TAG_APPLID, properties.getDseApplid(TAG));

        // A second request should use a cached copy
        Mockito.verify(cps).getProperty("dse.tag." + TAG, "applid");
        Assert.assertEquals("Unexpected value returned from ImstmProperties.getDseApplid()", TAG_APPLID, properties.getDseApplid(TAG));
        Mockito.verifyNoMoreInteractions(cps);
    }
    
    @Test
    public void testGetDseApplidCpsException() throws Exception {
        Mockito.when(cps.getProperty("dse.tag." + TAG, "applid")).thenThrow(new ConfigurationPropertyStoreException());
        String expectedMessage = "Problem asking CPS for the DSE applid for tag " + TAG;
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	properties.getDseApplid(TAG);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testGetDseVersionNull() throws Exception {        
        Mockito.when(cps.getProperty("dse.tag", "version", TAG)).thenReturn(null);
        Assert.assertNull("Unexpected value returned from ImstmProperties.getDseVersion()", properties.getDseVersion(TAG));

        // A second request should use a cached copy
        Mockito.verify(cps).getProperty("dse.tag", "version", TAG);
        Assert.assertNull("Unexpected value returned from ImstmProperties.getDseVersion()", properties.getDseVersion(TAG));
        Mockito.verifyNoMoreInteractions(cps);
    }
    
    @Test
    public void testGetDseVersionBlank() throws Exception {        
        Mockito.when(cps.getProperty("dse.tag", "version", TAG)).thenReturn("");
        Assert.assertNull("Unexpected value returned from ImstmProperties.getDseVersion()", properties.getDseVersion(TAG));

        // A second request should use a cached copy
        Mockito.verify(cps).getProperty("dse.tag", "version", TAG);
        Assert.assertNull("Unexpected value returned from ImstmProperties.getDseVersion()", properties.getDseVersion(TAG));
        Mockito.verifyNoMoreInteractions(cps);
    }

    
    @Test
    public void testGetDseVersionDefined() throws Exception {
        Mockito.when(cps.getProperty("dse.tag", "version", TAG)).thenReturn(TAG_VERSION_TEXT);
        Assert.assertEquals("Unexpected value returned from ImstmProperties.getDseVersion()", TAG_VERSION, properties.getDseVersion(TAG));

        // A second request should use a cached copy
        Mockito.verify(cps).getProperty("dse.tag", "version", TAG);
        Assert.assertEquals("Unexpected value returned from ImstmProperties.getDseVersion()", TAG_VERSION, properties.getDseVersion(TAG));
        Mockito.verifyNoMoreInteractions(cps);
    }
    
    @Test
    public void testGetDseVersionCpsException() throws Exception {
        Mockito.when(cps.getProperty("dse.tag", "version", TAG)).thenThrow(new ConfigurationPropertyStoreException());
        String expectedMessage = "Problem accessing the CPS for the IMS version, for tag " + TAG;
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	properties.getDseVersion(TAG);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testGetDseVersionParseException() throws Exception {
        Mockito.when(cps.getProperty("dse.tag", "version", TAG)).thenReturn(INVALID_VERSION_TEXT);
        String expectedMessage = "Failed to parse the IMS version '" + INVALID_VERSION_TEXT + "' for tag '" + TAG + "', should be a valid V.R.M version format, for example 15.5.0";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	properties.getDseVersion(TAG);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetProvisionTypeNull() throws Exception {        
        Mockito.when(cps.getProperty("provision", "type")).thenReturn(null);
        Assert.assertEquals("Unexpected value returned from ImstmProperties.getProvisionType()", DEFAULT_TYPE, properties.getProvisionType());

        // A second request should use a cached copy
        Mockito.verify(cps).getProperty("provision", "type");
        Assert.assertEquals("Unexpected value returned from ImstmProperties.getProvisionType()", DEFAULT_TYPE, properties.getProvisionType());
        Mockito.verifyNoMoreInteractions(cps);
    }
    
    @Test
    public void testGetProvisionTypeUndefined() throws Exception {        
        Mockito.when(cps.getProperty("provision", "type")).thenReturn("");
        Assert.assertEquals("Unexpected value returned from ImstmProperties.getProvisionType()", DEFAULT_TYPE, properties.getProvisionType());

        // A second request should use a cached copy
        Mockito.verify(cps).getProperty("provision", "type");
        Assert.assertEquals("Unexpected value returned from ImstmProperties.getProvisionType()", DEFAULT_TYPE, properties.getProvisionType());
        Mockito.verifyNoMoreInteractions(cps);
    }
    
    @Test
    public void testGetProvisionTypeDefined() throws Exception {
        Mockito.when(cps.getProperty("provision", "type")).thenReturn(TYPE_LOWER);
        Assert.assertEquals("Unexpected value returned from ImstmProperties.getProvisionType()", TYPE, properties.getProvisionType());

        // A second request should use a cached copy
        Mockito.verify(cps).getProperty("provision", "type");
        Assert.assertEquals("Unexpected value returned from ImstmProperties.getProvisionType()", TYPE, properties.getProvisionType());
        Mockito.verifyNoMoreInteractions(cps);
    }
}
