/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.cps.CpsProperties;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({RseapiPropertiesSingleton.class, CpsProperties.class})
public class TestServerImage {
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    private static final String SERVER_ID = "SERVER";
//    
//    @Test
//    public void testConstructor() {
//        ServerImage serverImage = new ServerImage();
//        Assert.assertNotNull("Object was not created", serverImage);
//    }
//    
//    @Test
//    public void testValid() throws Exception {
//    	PowerMockito.spy(RseapiPropertiesSingleton.class);
//    	PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(RseapiPropertiesSingleton.class, "cps");
//    	PowerMockito.spy(CpsProperties.class);
//    	PowerMockito.doReturn(SERVER_ID).when(CpsProperties.class, "getStringWithDefault", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
//        Assert.assertEquals("Unexpected value returned from ServerImages.get()", SERVER_ID, ServerImage.get(SERVER_ID));
//    }
}
