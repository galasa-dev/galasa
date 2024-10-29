/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.zos.internal.properties.ZosPropertiesSingleton;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({AbstractManager.class, ZosPropertiesSingleton.class, CpsProperties.class})
public class TestZosDseImageImpl {
//
//    @Mock
//    private ZosManagerImpl zosManagerMock;
//
//    @Mock
//    private IConfigurationPropertyStoreService cpsMock;
//    
//    @Mock
//    private IDynamicStatusStoreService dssMock;
//    
//    @Mock
//    private IFramework frameworkMock;
//    
//    @Mock
//    private ICredentialsService credentialsServiceMock;
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//    
//    private static final String IMAGE_ID = "image";
//
//    private static final String CLUSTER_ID = "cluster";
//
//    private static final String PLEX_ID = "sysplex";
//
//    private static final String DEFAULT_CREDENTIALS_ID = "credentials";
//
//    private static final String IPV4_HOSTNAME = "ipv4.hostname";
//
//    private static final String IPV6_HOSTNAME = "ipv6.hostname";
//    
//    @Test
//    public void testZosDseImageImpl() throws Exception {
//        PowerMockito.when(zosManagerMock.getCPS()).thenReturn(cpsMock);
//        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(PLEX_ID))).thenReturn(PLEX_ID);
//        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(DEFAULT_CREDENTIALS_ID), Mockito.anyString())).thenReturn(DEFAULT_CREDENTIALS_ID);
//        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(IPV4_HOSTNAME))).thenReturn(IPV4_HOSTNAME);
//        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(IPV6_HOSTNAME))).thenReturn(IPV6_HOSTNAME);
//        
//        
//        PowerMockito.when(zosManagerMock.getDSS()).thenReturn(dssMock);
//        PowerMockito.when(zosManagerMock.getFramework()).thenReturn(frameworkMock);
//        PowerMockito.when(frameworkMock.getCredentialsService()).thenReturn(credentialsServiceMock);
//        
//        PowerMockito.mockStatic(AbstractManager.class);
//        PowerMockito.doReturn(PLEX_ID).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(PLEX_ID));
//        PowerMockito.doReturn(DEFAULT_CREDENTIALS_ID).when(AbstractManager.class, "defaultString", ArgumentMatchers.contains(DEFAULT_CREDENTIALS_ID), Mockito.anyString());
//        PowerMockito.doReturn(IPV4_HOSTNAME).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(IPV4_HOSTNAME));
//        PowerMockito.doReturn(IPV6_HOSTNAME).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(IPV6_HOSTNAME));
//
//        PowerMockito.spy(ZosPropertiesSingleton.class);
//        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosPropertiesSingleton.class, "cps");
//        PowerMockito.spy(CpsProperties.class);
//        PowerMockito.doReturn(IMAGE_ID).when(CpsProperties.class, "getStringWithDefault", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//
//        Object zosDseImage = new ZosDseImageImpl(zosManagerMock, IMAGE_ID, CLUSTER_ID);
//        Assert.assertTrue("ZosDseImageImpl should instantiate ZosBaseImageImpl", zosDseImage instanceof ZosDseImageImpl);
//    }
}
