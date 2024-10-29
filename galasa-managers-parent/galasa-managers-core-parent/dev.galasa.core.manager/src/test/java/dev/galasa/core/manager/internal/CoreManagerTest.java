/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal;

import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.ManagerException;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.RunName;
import dev.galasa.core.manager.StoredArtifactRoot;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

@RunWith(MockitoJUnitRunner.class)
public class CoreManagerTest {
    
    @Mock
    private IFramework framework;
    
    @Mock
    private IResultArchiveStore ras;
    
    private String runName = UUID.randomUUID().toString();
    
    @Before
    public void setup() {
        when(framework.getTestRunName()).thenReturn(runName);
        when(framework.getResultArchiveStore()).thenReturn(ras);
        when(ras.getStoredArtifactsRoot()).thenReturn(Paths.get("a", "root", "dir"));
        new CorePropertiesSingleton().activate();
    }
    
    @Test
    public void testCoreManagerInitialise() throws ManagerException {
        CoreManagerImpl coreManager = new CoreManagerImpl();
        
        ArrayList<IManager> activeManagers = new ArrayList<>();
        coreManager.initialise(framework, null, activeManagers, new GalasaTest(this.getClass()));
        
        Assert.assertTrue("Core Manager missing from active managers", activeManagers.contains(coreManager));
    }

    @Test
    public void testCoreManagerGenerate() throws ManagerException, ResourceUnavailableException {
        CoreManagerImpl coreManager = new CoreManagerImpl();
        TestClass testClass = new TestClass();
        
        
        ArrayList<IManager> activeManagers = new ArrayList<>();
        coreManager.initialise(framework, null, activeManagers, new GalasaTest(testClass.getClass()));

        coreManager.provisionGenerate();
        coreManager.fillAnnotatedFields(testClass);
        
        Assert.assertNotNull("Core Manager field missing", testClass.coreManager);
        Assert.assertNotNull("Logger field missing", testClass.logger);
        Assert.assertNotNull("Run Name field missing", testClass.runName);
        Assert.assertNotNull("Root field missing", testClass.root);
        
        Assert.assertEquals("Core Manager field not valid", coreManager, testClass.coreManager);
        Assert.assertTrue("Logger field not valid", (testClass.logger instanceof Log));
        Assert.assertEquals("Core Manager field not valid", this.runName, testClass.runName);
        Assert.assertEquals("Logger field not valid", Paths.get("a", "root", "dir").toString(), testClass.root.toString());
    }
    
    
    public static class TestClass {
        @dev.galasa.core.manager.CoreManager
        public ICoreManager coreManager;
        
        @Logger
        public Log logger;
        
        @RunName
        public String runName;
        
        @StoredArtifactRoot
        public Path root;
    }
    

}
