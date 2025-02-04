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
public class TestDefaultVersion {
    
    @Mock
    private static IConfigurationPropertyStoreService cps;
    private static ImstmPropertiesSingleton ips = new ImstmPropertiesSingleton();
    
    private static final ProductVersion DEFAULT_VERSION = ProductVersion.v(15).r(5).m(0);
    private static final String INVALID_VERSION_TEXT = "1.2.3.4";
    private static final String CONFIGURED_VERSION_TEXT = "1.2.3";
    private static final ProductVersion CONFIGURED_VERSION = ProductVersion.v(1).r(2).m(3);
 
    @Before
    public void setup() throws ImstmManagerException, ConfigurationPropertyStoreException {
        ips.activate();
        ImstmPropertiesSingleton.setCps(cps);
    }

    @Test
    public void testNull() throws Exception {        
        Mockito.when(cps.getProperty("default", "version")).thenReturn(null);
        Assert.assertEquals("Unexpected value returned from DefaultVersion.get()", DEFAULT_VERSION, DefaultVersion.get());
    }
    
    @Test
    public void testUndefined() throws Exception {        
        Mockito.when(cps.getProperty("default", "version")).thenReturn("");
        Assert.assertEquals("Unexpected value returned from DefaultVersion.get()", DEFAULT_VERSION, DefaultVersion.get());
    }
    
    @Test
    public void testDefined() throws Exception {
        Mockito.when(cps.getProperty("default", "version")).thenReturn(CONFIGURED_VERSION_TEXT);
        Assert.assertEquals("Unexpected value returned from DefaultVersion.get()", CONFIGURED_VERSION, DefaultVersion.get());
    }
    
    @Test
    public void testCpsException() throws Exception {
        Mockito.when(cps.getProperty("default", "version")).thenThrow(new ConfigurationPropertyStoreException());
        Assert.assertEquals("Unexpected value returned from DefaultVersion.get()", DEFAULT_VERSION, DefaultVersion.get());
    }
    
    @Test
    public void testParseException() throws Exception {
        Mockito.when(cps.getProperty("default", "version")).thenReturn(INVALID_VERSION_TEXT);
        Assert.assertEquals("Unexpected value returned from DefaultVersion.get()", DEFAULT_VERSION, DefaultVersion.get());
    }
}
