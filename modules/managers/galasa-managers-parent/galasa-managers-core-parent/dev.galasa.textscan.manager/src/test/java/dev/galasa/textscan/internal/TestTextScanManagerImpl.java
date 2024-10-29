/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.testharness.TestHarnessFramework;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.LogScanner;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.TextScanner;

//@RunWith(PowerMockRunner.class)
public class TestTextScanManagerImpl {
//
//    private TestHarnessFramework framework;
//    
//    private DummyTestClass testClass; 
//    
//    private TextScanManagerImpl textScanManager;
//
//    @Before
//    public void before() throws Exception {
//        this.framework = new TestHarnessFramework();
//        this.textScanManager = new TextScanManagerImpl();
//        this.testClass = new DummyTestClass();
//    }
//
//    @Test
//    public void testGoldenPath() throws Exception {
//        ArrayList<IManager> allManagers = new ArrayList<>();
//        ArrayList<IManager> activeManagers = new ArrayList<>();
//        allManagers.add(this.textScanManager);
//        
//    	textScanManager.initialise(framework, allManagers, activeManagers, new GalasaTest(testClass.getClass()));
//    	textScanManager.youAreRequired(allManagers, activeManagers, null);
//        textScanManager.provisionGenerate();
//        textScanManager.provisionBuild();
//        
//        textScanManager.fillAnnotatedFields(testClass);
//        
//        assertThat(activeManagers).as("Active Managers must include TextScanManagerImpl").contains(textScanManager);
//        assertNotNull("textScanner field should not be null", testClass.textScanner);
//        assertTrue("textScanner field should be an instanceof TextScannerImpl", testClass.textScanner instanceof TextScannerImpl);
//        assertNotNull("logScanner field should not be null", testClass.logScanner);
//        assertTrue("textScanner field should be an instanceof LogScannerImpl", testClass.logScanner instanceof LogScannerImpl);
//    }
//
//    @Test
//    public void testInitialiseNoAnnotations() throws Exception {
//    	DummyTestClassEmpty testClassEmpty = new DummyTestClassEmpty();
//        ArrayList<IManager> allManagers = new ArrayList<>();
//        ArrayList<IManager> activeManagers = new ArrayList<>();
//        allManagers.add(this.textScanManager);
//        
//    	textScanManager.initialise(framework, allManagers, activeManagers, new GalasaTest(testClassEmpty.getClass()));
//        textScanManager.provisionGenerate();
//        textScanManager.provisionBuild();
//        
//        textScanManager.fillAnnotatedFields(testClassEmpty);
//        
//        assertNull("textScanner field should be null", testClass.textScanner);
//        assertNull("logScanner field should be null", testClass.logScanner);
//    }
//    
//    @Test
//    public void testGetters() throws TextScanManagerException {
//    	assertTrue("getTextScanner should return an instanceof TextScannerImpl", textScanManager.getTextScanner() instanceof TextScannerImpl);
//    	assertTrue("getLogScanner should return an instanceof LogScannerImpl", textScanManager.getLogScanner() instanceof LogScannerImpl);
//    	
//    }
//    
//    public class DummyTestClass {
//    	@TextScanner
//    	public ITextScanner textScanner;
//    	
//    	@LogScanner
//    	public ILogScanner logScanner;
//    }
//    
//    public class DummyTestClassEmpty {
//    	public ITextScanner textScanner;
//    	
//    	public ILogScanner logScanner;
//    }
}