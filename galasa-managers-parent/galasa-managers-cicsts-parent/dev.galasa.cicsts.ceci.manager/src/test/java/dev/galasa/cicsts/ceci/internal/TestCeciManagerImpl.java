/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceci.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.CeciManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ceci.internal.properties.CeciPropertiesSingleton;
import dev.galasa.cicsts.internal.CicstsManagerImpl;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.internal.ZosManagerImpl;

//@RunWith(PowerMockRunner.class)
public class TestCeciManagerImpl {
//    
//    private CeciManagerImpl ceciManager;
//    
//    private CeciManagerImpl ceciManagerSpy;
//    
//    private List<IManager> allManagers;
//    
//    private List<IManager> activeManagers;
//    
//    @Mock
//    private IFramework frameworkMock;
//    
//    @Mock
//    public IManager managerMock;
//    
//    @Mock
//    private ZosManagerImpl zosManagerMock;
//    
//    @Mock
//    private CicstsManagerImpl cicsManagerMock;
//    
//    @Mock
//    private ICicsRegion cicsRegionMock;
//    
//    @Before
//    public void setup() throws Exception {
//        CeciPropertiesSingleton ceciPropertiesSingleton = new CeciPropertiesSingleton();
//        ceciPropertiesSingleton.activate();
//        
//        ceciManager = new CeciManagerImpl();
//        ceciManagerSpy = Mockito.spy(ceciManager);
//        Mockito.when(ceciManagerSpy.getFramework()).thenReturn(frameworkMock);
//        
//        allManagers = new ArrayList<>();
//        activeManagers = new ArrayList<>();
//    }
//
//    @Test
//    public void testInitialise() throws ManagerException, ConfigurationPropertyStoreException {        
//        allManagers.add(managerMock);
//        allManagers.add(cicsManagerMock);
//        ceciManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestCeciManagerImpl.class));
//        Assert.assertEquals("Error in initialise() method", ceciManagerSpy.getFramework(), frameworkMock);
//        
//        ceciManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        Assert.assertEquals("Error in initialise() method", ceciManagerSpy.getFramework(), frameworkMock);
//        
//        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
//        String expectedMessage = "Unable to request framework services";
//        CeciManagerException expectedException = Assert.assertThrows("expected exception should be thrown", CeciManagerException.class, ()->{
//        	ceciManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testYouAreRequired() throws Exception {
//    	PowerMockito.doReturn(null).when(ceciManagerSpy, "addDependentManager", Mockito.any(),  Mockito.any(), Mockito.any(), Mockito.any());
//    	ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
//        	ceciManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//        Assert.assertEquals("exception should contain expected cause", "CICS Manager is not available", expectedException.getMessage());
//    }
//    
//    @Test
//    public void testProvisionGenerate() throws Exception {
//        PowerMockito.doNothing().when(ceciManagerSpy, "generateAnnotatedFields", Mockito.any());
//        ceciManagerSpy.provisionGenerate();
//        PowerMockito.verifyPrivate(ceciManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
//    }
//
//    @Test
//    public void testGetCeci() {
//    	Assert.assertEquals("Error in getCeci() method", cicsRegionMock, ((CeciImpl) ceciManager.getCeci(cicsRegionMock)).getCicsRegion());
//    	Assert.assertEquals("Error in getCeci() method", cicsRegionMock, ((CeciImpl) ceciManager.getCeci(cicsRegionMock)).getCicsRegion());
//    }
//
//	class DummyTestClass {
//	}
}
