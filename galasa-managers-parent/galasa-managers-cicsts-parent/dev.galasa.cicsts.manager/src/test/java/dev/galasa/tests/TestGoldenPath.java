/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.ProductVersion;
import dev.galasa.cicsts.internal.CicstsManagerImpl;
import dev.galasa.cicsts.internal.properties.CicstsPropertiesSingleton;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.testharness.TestHarnessFramework;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosfile.spi.IZosFileSpi;

//@RunWith(PowerMockRunner.class)
public class TestGoldenPath {
//
//    private TestHarnessFramework framework;
//
//    private SampleGalasaTst testClass;
//    
//    private static CicstsPropertiesSingleton cicsProperties;
//
//    @Mock
//    private IZosManagerInt zosManager;
//
//    @Mock
//    private IZosBatchManagerInt zosBatchManager;
//
//    @Mock
//    private IZosFileManagerInt zosFileManager;
//    
//    @Mock
//    private IZosImage zosImage;
//
//    @BeforeClass
//    public static void beforeClass() {
//        cicsProperties = new CicstsPropertiesSingleton();
//        cicsProperties.activate();
//    }
//
//    @Before
//    public void before() throws Exception {
//        this.framework = new TestHarnessFramework();
//
//        testClass = new SampleGalasaTst();
//    }
//
//    @Test
//    public void testGoldenPath() throws Exception {
//
//        CicstsManagerImpl cicstsManager = new CicstsManagerImpl();
//
//        ArrayList<IManager> allManagers = new ArrayList<>();
//
//        // Add dependent Managers
//        ArrayList<IManager> activeManagers = new ArrayList<>();
//        allManagers.add(zosManager);
//        allManagers.add(zosBatchManager);
//        allManagers.add(zosFileManager);
//        
//        // Setup calls to zosManager
//        when(zosManager.getImageForTag("PRIMARY")).thenReturn(zosImage);
//
//        // Add our CPS properties
//        framework.cpsStore.properties.put("cicsts.provision.type", "dse");
//        framework.cpsStore.properties.put("cicsts.dse.tag.PRIMARY.applid","REGION1");
//        framework.cpsStore.properties.put("cicsts.dse.tag.PRIMARY.version","5.6.0");
//        framework.cpsStore.properties.put("cicsts.dse.tag.SECONDARY.applid","REGION2");
//        
//        cicstsManager.extraBundles(framework);
//        cicstsManager.initialise(framework, allManagers, activeManagers, new GalasaTest(testClass.getClass()));
//        cicstsManager.youAreRequired(allManagers, activeManagers, null);
//        boolean dependentOnZos = cicstsManager.areYouProvisionalDependentOn(zosManager);
//        assertThat(dependentOnZos).as("CICS TS must be dependent on zOS").isTrue();
//        cicstsManager.provisionGenerate();
//        cicstsManager.provisionBuild();
//        
//        cicstsManager.fillAnnotatedFields(testClass);
//        
//        assertThat(activeManagers).as("Active Managers needs to include cicsts").contains(cicstsManager);
//        assertThat(testClass.region1.getVersion()).as("CICS version needs to be filled in").isEqualTo(ProductVersion.v(5).r(6).m(0));
//    }
//    
//    private interface IZosManagerInt extends IZosManagerSpi, IManager {
//        
//    }
//    
//    private interface IZosBatchManagerInt extends IZosBatchSpi, IManager {
//        
//    }
//    
//    private interface IZosFileManagerInt extends IZosFileSpi, IManager {
//        
//    }
//
}