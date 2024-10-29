/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.internal.properties;

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
import dev.galasa.zosfile.ZosFileManagerException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ZosFilePropertiesSingleton.class, CpsProperties.class})
public class TestUnixFilePermissions {
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    private static final String IMAGE_ID = "IMAGE";
//    
//    private static final String UNIX_FILE_PERMISSIONS = "rwxrwxr-x";
//    
//    @Test
//    public void testConstructor() {
//        UnixFilePermissions unixFilePermissions = new UnixFilePermissions();
//        Assert.assertNotNull("Object was not created", unixFilePermissions);
//    }
//    
//    @Test
//    public void testNull() throws Exception {
//        Assert.assertEquals("Unexpected value returned from UnixFilePermissions.get()", UNIX_FILE_PERMISSIONS, getProperty(null));
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//        String[] parts = {"---", "--x", "-w-", "-wx", "r--", "r-x", "rw-", "rwx"};
//        for (String part1 : parts) {
//            for (String part2 : parts) {
//                for (String part3 : parts) {
//                    String permission = part1 + part2 + part3;
//                    Assert.assertEquals("Unexpected value returned from UnixFilePermissions.get()", permission, getProperty(permission));
//                }
//            }            
//        }
//    }
//    
//    @Test
//    public void testInvalid() throws Exception {
//        String expectedMessage = "The default UNIX file permissions property must be in the range \"---------\" to \"rwxrwxrwx\" and match the regex expression \"([-r][-w][-x]){3}\"";
//        ZosFileManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosFileManagerException.class, ()->{
//        	getProperty("XXX");
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testException() throws Exception {
//        String expectedMessage = "Problem asking the CPS for the default UNIX file permissions property for zOS image " + IMAGE_ID;
//        ZosFileManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosFileManagerException.class, ()->{
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
//        PowerMockito.spy(ZosFilePropertiesSingleton.class);
//        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosFilePropertiesSingleton.class, "cps");
//        PowerMockito.spy(CpsProperties.class);
//        
//        if (!exception) {
//            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
//        } else {
//            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
//        }
//        
//        return UnixFilePermissions.get(IMAGE_ID);
//    }
}
