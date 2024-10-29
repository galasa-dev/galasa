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
public class TestMsgLevel {
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    private static final String IMAGE_ID = "IMAGE";
//    
//    private static final String DEFAULT_LEVEL = "(1,1)";
//    
//    @Test
//    public void testConstructor() {
//        MsgLevel msgLevel = new MsgLevel();
//        Assert.assertNotNull("Object was not created", msgLevel);
//    }
//    
//    @Test
//    public void testNull() throws Exception {
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", DEFAULT_LEVEL, getProperty(null));
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(0,0)", getProperty("(0,0)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(0,1)", getProperty("(0,1)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(0,2)", getProperty("(0,2)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(1,0)", getProperty("(1,0)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(1,1)", getProperty("(1,1)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(1,2)", getProperty("(1,2)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(2,0)", getProperty("(2,0)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(2,1)", getProperty("(2,1)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(2,2)", getProperty("(2,2)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(3,0)", getProperty("(3,0)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(3,1)", getProperty("(3,1)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "(3,2)", getProperty("(3,2)"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "0", getProperty("0"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "1", getProperty("1"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "2", getProperty("2"));
//        Assert.assertEquals("Unexpected value returned from MsgLevel.get()", "3", getProperty("3"));
//    }
//    
//    @Test
//    public void testExceptions() throws Exception {
//        String expectedMessage = "Problem asking the CPS for the zOSMF default message level for zOS image " + IMAGE_ID;
//        ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	getProperty("ANY", true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//
//        expectedMessage = "Message level value invalid. Valid examples: \"(2,1)\", \"0\", \"(,0)\"";
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	getProperty("(0,3)");
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//
//        expectedMessage = "Message level value invalid. Valid examples: \"(2,1)\", \"0\", \"(,0)\"";
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	getProperty("(4,0)");
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//
//    	expectedMessage = "Message level value invalid. Valid examples: \"(2,1)\", \"0\", \"(,0)\"";
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	getProperty("(,3)");
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//
//        expectedMessage = "Message level value invalid. Valid examples: \"(2,1)\", \"0\", \"(,0)\"";
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	getProperty("4");
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//
//        expectedMessage = "Message level value invalid. Valid examples: \"(2,1)\", \"0\", \"(,0)\"";
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//        	getProperty("X");
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
//        return MsgLevel.get(zosImageMock);
//    }
}
