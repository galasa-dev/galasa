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
public class TestTruncateJCLRecords {
    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    private static final String IMAGE_ID = "IMAGE";
//    
//    @Test
//    public void testConstructor() {
//        TruncateJCLRecords truncateJCLRecords = new TruncateJCLRecords();
//        Assert.assertNotNull("Object was not created", truncateJCLRecords);
//    }
//    
//    @Test
//    public void testNullandEmpty() throws Exception {
//        Assert.assertEquals("Unexpected value returned from TruncateJCLRecords.get()", true, getProperty(null));
//        Assert.assertEquals("Unexpected value returned from TruncateJCLRecords.get()", true, getProperty(""));
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        Assert.assertEquals("Unexpected value returned from TruncateJCLRecords.get()", true, getProperty("true"));
//        Assert.assertEquals("Unexpected value returned from TruncateJCLRecords.get()", true, getProperty("TRUE"));
//        Assert.assertEquals("Unexpected value returned from TruncateJCLRecords.get()", true, getProperty("TrUe"));
//        Assert.assertEquals("Unexpected value returned from TruncateJCLRecords.get()", false, getProperty("fasle"));
//        Assert.assertEquals("Unexpected value returned from TruncateJCLRecords.get()", false, getProperty("FALSE"));
//        Assert.assertEquals("Unexpected value returned from TruncateJCLRecords.get()", false, getProperty("FaLsE"));
//    }
//    
//    @Test
//    public void testInvalid() throws Exception {
//        Assert.assertEquals("Unexpected value returned from TruncateJCLRecords.get()", false, getProperty("XXX"));
//        Assert.assertEquals("Unexpected value returned from TruncateJCLRecords.get()", false, getProperty("999"));
//    }
//    
//    @Test
//    public void testException() throws Exception {
//        String expectedMessage = "Problem asking the CPS for the truncate JCL records property for zOS image " + IMAGE_ID;
//        ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	getProperty("ANY", true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    private boolean getProperty(String value) throws Exception {
//        return getProperty(value, false);
//    }
//    
//    private boolean getProperty(String value, boolean exception) throws Exception {
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
//        return TruncateJCLRecords.get(IMAGE_ID);
//    }
}
