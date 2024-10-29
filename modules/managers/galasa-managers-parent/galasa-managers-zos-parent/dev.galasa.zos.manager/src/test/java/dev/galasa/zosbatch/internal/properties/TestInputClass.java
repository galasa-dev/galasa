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
import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.ZosBatchManagerException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ZosBatchPropertiesSingleton.class, CpsProperties.class})
public class TestInputClass {
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    private static final String IMAGE_ID = "IMAGE";
//    
//    private static final String DEFAULT_CLASS = "A";
//    
//    @Test
//    public void testConstructor() {
//        InputClass inputClass = new InputClass();
//        Assert.assertNotNull("Object was not created", inputClass);
//    }
//    
//    @Test
//    public void testNull() throws Exception {
//        Assert.assertEquals("Unexpected value returned from InputClass.get()", DEFAULT_CLASS, getProperty(null));
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        Assert.assertEquals("Unexpected value returned from InputClass.get()", "Z", getProperty("z"));
//        Assert.assertEquals("Unexpected value returned from InputClass.get()", "ZZ1", getProperty("ZZ1"));
//        Assert.assertEquals("Unexpected value returned from InputClass.get()", "ZZZZZZZZ", getProperty("ZZZZZZZZ"));
//    }
//    
//    @Test
//    public void testExceptions() throws Exception {
//        String expectedMessage = "Input class value must be between 1 and 8 characters in the range [A-Z0-9]";
//        ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	getProperty("");
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//
//        expectedMessage = "Input class value must be between 1 and 8 characters in the range [A-Z0-9]";
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	getProperty("XXXXXXXXX");
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    	
//        expectedMessage = "Input class value must be between 1 and 8 characters in the range [A-Z0-9]";
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	getProperty("?XX");
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testException4() throws Exception {
//        String expectedMessage = "Problem asking the CPS for the zOSMF default input class for zOS image " + IMAGE_ID;
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
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE_ID);
//        
//        if (!exception) {
//            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());            
//        } else {
//            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
//        }
//        
//        return InputClass.get(zosImageMock);
//    }
}
