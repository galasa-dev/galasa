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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.ProductVersion;
import dev.galasa.imstm.internal.ImstmManagerImpl;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.testharness.TestHarnessFramework;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;

@RunWith(MockitoJUnitRunner.class)
public class TestGoldenPath {

    private TestHarnessFramework framework;

    private SampleGalasaTst testClass;
    
    @Mock
    private IZosManagerInt zosManager;
    @Mock
    private ITextScanManagerInt textScanManager;

    @Mock
    private IZosImage zosImage;

    @Before
    public void before() throws Exception {
        this.framework = new TestHarnessFramework();
        testClass = new SampleGalasaTst();
    }

    @Test
    public void testGoldenPath() throws Exception {

        ImstmManagerImpl imstmManager = new ImstmManagerImpl();

        ArrayList<IManager> allManagers = new ArrayList<>();

        // Add dependent Managers
        ArrayList<IManager> activeManagers = new ArrayList<>();
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        
        // Setup calls to zosManager
        when(zosManager.getImageForTag("PRIMARY")).thenReturn(zosImage);

        // Add our CPS properties
        framework.cpsStore.properties.put("imstm.provision.type", "dse");
        framework.cpsStore.properties.put("imstm.dse.tag.PRIMARY.applid","SYSTEM1");
        framework.cpsStore.properties.put("imstm.dse.tag.PRIMARY.version","15.4.0");
        framework.cpsStore.properties.put("imstm.dse.tag.SECONDARY.applid","SYSTEM2");
        
        imstmManager.extraBundles(framework);
        imstmManager.initialise(framework, allManagers, activeManagers, new GalasaTest(testClass.getClass()));
        imstmManager.youAreRequired(allManagers, activeManagers, null);
        boolean dependentOnZos = imstmManager.areYouProvisionalDependentOn(zosManager);
        assertThat(dependentOnZos).as("IMS TM must be dependent on zOS").isTrue();
        imstmManager.provisionGenerate();
        imstmManager.provisionBuild();
        
        imstmManager.fillAnnotatedFields(testClass);
        
        assertThat(activeManagers).as("Active Managers needs to include imstm").contains(imstmManager);
        assertThat(testClass.system1.getVersion()).as("IMS version needs to be filled in").isEqualTo(ProductVersion.v(15).r(4).m(0));
    }
    
    private interface IZosManagerInt extends IZosManagerSpi, IManager {}

    private interface ITextScanManagerInt extends ITextScannerManagerSpi, IManager {}
    
}