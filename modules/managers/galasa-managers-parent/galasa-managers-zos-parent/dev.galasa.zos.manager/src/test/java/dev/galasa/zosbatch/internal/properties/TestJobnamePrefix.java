/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosbatch.ZosBatchManagerException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ZosBatchPropertiesSingleton.class, CpsProperties.class})
public class TestJobnamePrefix {
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    private static final String IMAGE_ID = "IMAGE";
//    
//    private static final String DEFAULT_JOBNAME_PREFIX = "GAL";
//    
//    @Test
//    public void testConstructor() {
//        JobnamePrefix jobnamePrefix = new JobnamePrefix();
//        Assert.assertNotNull("Object was not created", jobnamePrefix);
//    }
//    
//    @Test
//    public void testNull() throws Exception {
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty(null));
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", "ZZZZ", getProperty("zzzz"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", "$ZZ1$", getProperty("$ZZ1$"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", "#ZZ2#", getProperty("#ZZ2#"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", "@ZZ3@", getProperty("@ZZ3@"));
//    }
//    
//    @Test
//    public void testInvalidLength() throws Exception {
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("XXXXXXXXXXXX"));
//    }
//    
//    @Test
//    public void testInvalidValues() throws Exception {
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("9XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("?XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("!XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("\"XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("£XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("%XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("^XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("&XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("*XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("(XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty(")XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("?XX"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("X!X"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("X\"X"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("X£X"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("X%X"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("X^X"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("X&X"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("X*X"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("X(X"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("X)X"));
//        Assert.assertEquals("Unexpected value returned from JobnamePrefix.get()", DEFAULT_JOBNAME_PREFIX, getProperty("X?X"));
//    }
//    
//    @Test
//    public void testException() throws Exception {
//        String expectedMessage = "Problem asking the CPS for the zOSMF jobname prefix for zOS image " + IMAGE_ID;
//        ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	getProperty("ANY", true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    private String getProperty(String value) throws Exception {
//        return getProperty(value, false);
//    }
//    
//    private String getProperty(String value, boolean exception) throws Exception {
//        PowerMockito.spy(ZosBatchPropertiesSingleton.class);
//        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosBatchPropertiesSingleton.class, "cps");
//        PowerMockito.spy(CpsProperties.class);
//        
//        if (!exception) {
//            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
//        } else {
//            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
//        }
//        
//        return JobnamePrefix.get(IMAGE_ID);
//    }
}
