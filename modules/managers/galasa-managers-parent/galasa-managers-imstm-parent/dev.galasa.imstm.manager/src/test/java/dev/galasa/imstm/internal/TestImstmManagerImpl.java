/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.imstm.ImsSystem;
import dev.galasa.imstm.ImsTerminal;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.internal.dse.DseImsImpl;
import dev.galasa.imstm.internal.dse.DseProvisioningImpl;
import dev.galasa.imstm.internal.properties.DefaultVersion;
import dev.galasa.imstm.internal.properties.ExtraBundles;
import dev.galasa.imstm.internal.properties.ImstmPropertiesSingleton;
import dev.galasa.imstm.internal.properties.ProvisionType;
import dev.galasa.imstm.spi.IImsSystemLogonProvider;
import dev.galasa.imstm.spi.IImsSystemProvisioner;
import dev.galasa.imstm.spi.IImstmManagerSpi;
import dev.galasa.imstm.spi.ImsTerminalImpl;
import dev.galasa.textscan.internal.TextScanManagerImpl;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.imstm.IImsSystem;
import dev.galasa.imstm.IImsTerminal;
import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;

@RunWith(MockitoJUnitRunner.class)
public class TestImstmManagerImpl {
    
    private class SystemOnlyTest {

        @ImsSystem
        public IImsSystem imsSystem;

    }

    private class ValidDefaultTest {

        @ImsTerminal
        public IImsTerminal terminal;

        @ImsSystem
        public IImsSystem imsSystem;

    }

    private class ValidTest {

        @ImsTerminal(imsTag = "SYS01", connectAtStartup = false, loginCredentialsTag = "CRED01")
        public IImsTerminal terminal;

        @ImsSystem(imsTag = "SYS01", imageTag = "MVS1")
        public IImsSystem imsSystem;

    }

    private class BadDummyTest {

        @ImsTerminal(imsTag = "TERM01")
        public IImsTerminal terminal;

        @ImsSystem(imsTag = "SYS01")
        public IImsSystem imsSystem;

    }

    // Extending class to give us access to protected methods whilst still
    // testing the correct class
    private class MockImstmManagerImpl extends ImstmManagerImpl{
        public Object getRegisteredField(Field f) {
            return getAnnotatedField(f);
        }
    }

    @Mock
    private ZosManagerImpl zosManager;
    @Mock
    private TextScanManagerImpl textScanManager;
    @Mock
    private GalasaTest galasaTest;
    @Mock
    private IFramework framework;
    @Mock
    private IConfigurationPropertyStoreService cpss;
    @Mock
    private IImsSystem system;
    
    private MockImstmManagerImpl imsTmManager;
    private List<IManager> activeManagers, allManagers;

    private static String APPLID = "APPLID01";
    private static String BUNDLE = "test.bundle";
    private static String TEST_IMS_TAG = "SYS01";
    private static String TEST_IMAGE_TAG = "MVS1";
    private static String DEFAULT_TAG = "PRIMARY";
    private static String TEST_CREDENTIALS = "CRED01";
    private static String DEFAULT_CREDENTIALS = "";

    @Before
    public void setup() throws Exception{
        imsTmManager = new MockImstmManagerImpl();
        activeManagers = new ArrayList<IManager>();
        allManagers = new ArrayList<IManager>();
        Mockito.when(galasaTest.isJava()).thenReturn(true);
        Mockito.doReturn(ValidTest.class).when(galasaTest).getJavaTestClass();
        Mockito.when(system.getApplid()).thenReturn(APPLID);
    }

    @Test
    public void testInitialiseNotJava() throws Exception{
        Mockito.when(galasaTest.isJava()).thenReturn(false);
        imsTmManager.initialise(null, allManagers, activeManagers, galasaTest);
    	Assert.assertTrue("Unexpected active managers", activeManagers.isEmpty());
    }

    @Test
    public void testInitialiseNoAnnotations() throws Exception{
        Mockito.doReturn(Object.class).when(galasaTest).getJavaTestClass();
        imsTmManager.initialise(null, allManagers, activeManagers, galasaTest);
    	Assert.assertTrue("Unexpected active managers", activeManagers.isEmpty());
    }

    @Test
    public void testInitialiseNoZosManager() throws Exception{
        String expectedMessage = "Unable to locate the zOS Manager, required for the IMS TM Manager";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            imsTmManager.initialise(null, allManagers, activeManagers, galasaTest);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testInitialiseNoTextScannerManager() throws Exception{
        allManagers.add(zosManager);
        String expectedMessage = "The Text Scanner Manager is not available";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            imsTmManager.initialise(null, allManagers, activeManagers, galasaTest);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testInitialise() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
        MockedConstruction<DseProvisioningImpl> provisioner = Mockito.mockConstruction(DseProvisioningImpl.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(null, allManagers, activeManagers, galasaTest);
            Assert.assertEquals("Unexpected number of provisioners created", 1, provisioner.constructed().size());
            Assert.assertTrue("Wrong type of provisioner created", provisioner.constructed().get(0) instanceof DseProvisioningImpl);
        }
    	Assert.assertEquals("Unexpected number of active managers", 1, activeManagers.size());
    	Assert.assertTrue("IMS TM Manager not activated", activeManagers.contains(imsTmManager));
        Mockito.verify(zosManager).youAreRequired(allManagers, activeManagers, galasaTest);
        Mockito.verify(textScanManager).youAreRequired(allManagers, activeManagers, galasaTest);
    }

    @Test
    public void testInitialiseAlreadyActive() throws Exception{
        activeManagers.add(imsTmManager);
        imsTmManager.initialise(null, allManagers, activeManagers, galasaTest);
    	Assert.assertEquals("Unexpected number of active managers", 1, activeManagers.size());
    	Assert.assertTrue("IMS TM Manager not activated", activeManagers.contains(imsTmManager));
    }

    @Test
    public void testYouAreRequiredNoZosManager() throws Exception{
        String expectedMessage = "Unable to locate the zOS Manager, required for the IMS TM Manager";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            imsTmManager.youAreRequired(allManagers, activeManagers, galasaTest);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testYouAreRequiredNoTextScannerManager() throws Exception{
        allManagers.add(zosManager);
        String expectedMessage = "The Text Scanner Manager is not available";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            imsTmManager.youAreRequired(allManagers, activeManagers, galasaTest);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testYouAreRequired() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
        MockedConstruction<DseProvisioningImpl> provisioner = Mockito.mockConstruction(DseProvisioningImpl.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.youAreRequired(allManagers, activeManagers, galasaTest);
            Assert.assertEquals("Unexpected number of provisioners created", 1, provisioner.constructed().size());
            Assert.assertTrue("Wrong type of provisioner created", provisioner.constructed().get(0) instanceof DseProvisioningImpl);
        }
    	Assert.assertEquals("Unexpected number of active managers", 1, activeManagers.size());
    	Assert.assertTrue("IMS TM Manager not activated", activeManagers.contains(imsTmManager));
        Mockito.verify(zosManager).youAreRequired(allManagers, activeManagers, galasaTest);
        Mockito.verify(textScanManager).youAreRequired(allManagers, activeManagers, galasaTest);
    }

    @Test
    public void testYouAreRequiredAlreadyActive() throws Exception{
        activeManagers.add(imsTmManager);
        imsTmManager.youAreRequired(allManagers, activeManagers, galasaTest);
    	Assert.assertEquals("Unexpected number of active managers", 1, activeManagers.size());
    	Assert.assertTrue("IMS TM Manager not activated", activeManagers.contains(imsTmManager));
    }

    @Test
    public void testExtraBundlesCpsException() throws Exception{
        Mockito.when(framework.getConfigurationPropertyService("imstm")).thenThrow(new ConfigurationPropertyStoreException());
        String expectedMessage = "Unable to request framework services";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            imsTmManager.extraBundles(framework);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testExtraBundles() throws Exception{
        Mockito.when(framework.getConfigurationPropertyService("imstm")).thenReturn(cpss);
        List<String> bundlesIn = new ArrayList<String>();
        bundlesIn.add(BUNDLE);
        try (MockedStatic<ExtraBundles> eb = Mockito.mockStatic(ExtraBundles.class);
                MockedStatic<ImstmPropertiesSingleton> ips = Mockito.mockStatic(ImstmPropertiesSingleton.class)) {
            eb.when(() -> ExtraBundles.get()).thenReturn(bundlesIn);
            List<String> bundlesOut = imsTmManager.extraBundles(framework);
            Assert.assertEquals("Wrong number of extra bundles returned", 1, bundlesOut.size());
            Assert.assertEquals("Wrong bundle returned", BUNDLE, bundlesOut.get(0));
        }
    }

    @Test
    public void testAreYouProvisionalDependentOnTrue() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(null, allManagers, activeManagers, galasaTest);
        }
        Assert.assertTrue("IMS TM Manager not dependent on zOS Manager", imsTmManager.areYouProvisionalDependentOn(zosManager));
    }

    @Test
    public void testAreYouProvisionalDependentOnFalse() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(null, allManagers, activeManagers, galasaTest);
        }
        Assert.assertFalse("IMS TM Manager not dependent on zOS Manager", imsTmManager.areYouProvisionalDependentOn(textScanManager));
    }

    @Test
    public void testProvisionGenerate() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    List<?> arguments = context.arguments();
                    Assert.assertEquals("Wrong IMS TM Manager passed to terminal constructor", imsTmManager, (IImstmManagerSpi) arguments.get(0));
                    Assert.assertEquals("Wrong Framework passed to terminal constructor", framework, (IFramework) arguments.get(1));
                    Assert.assertEquals("Wrong IMS System passed to terminal constructor", system, (IImsSystem) arguments.get(2));
                    Assert.assertEquals("Wrong 'Connect at startup' value passed to terminal constructor", false, (Boolean) arguments.get(3));
                    Assert.assertEquals("Wrong text scanner passed to terminal constructor", textScanManager, (ITextScannerManagerSpi) arguments.get(4));
                    Assert.assertEquals("Wrong 'Login credentials' value passed to terminal constructor", TEST_CREDENTIALS, (String) arguments.get(5));
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0); 
            Mockito.verify(provisioner).imsProvisionGenerate();
            Mockito.verify(provisioner).provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations);

            // Verify generated system
            Map<String, IImsSystem> systems = imsTmManager.getTaggedImsSystems();
            Assert.assertEquals("Wrong number of IMS systems provisioned", 1, systems.size());
            Assert.assertTrue("Wrong system provisioned", systems.containsValue(system));

            // Verify system is registered
            Object registeredSystem = imsTmManager.getRegisteredField(ValidTest.class.getField("imsSystem"));
            Assert.assertNotNull("IMS System not registered", registeredSystem);
            Assert.assertEquals("Wrong object registered as IMS System for field 'imsSystem'", system, registeredSystem);

            // Verify generated terminal
            Assert.assertEquals("Wrong number of IMS terminals constructed", 1, terminals.constructed().size());
            ImsTerminalImpl terminal = terminals.constructed().get(0);
            List<IImsTerminal> savedTerminals = imsTmManager.getImsTerminals();
            Assert.assertEquals("Wrong number of IMS terminals generated", 1, savedTerminals.size());
            Assert.assertTrue("Wrong terminal generated", savedTerminals.contains(terminal));
        }
    }

    @Test
    public void testProvisionGenerateDefaults() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        Mockito.doReturn(ValidDefaultTest.class).when(galasaTest).getJavaTestClass();
        List<Annotation> annotations = getSystemAnnotations(ValidDefaultTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(DEFAULT_TAG, DEFAULT_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    List<?> arguments = context.arguments();
                    Assert.assertEquals("Wrong IMS TM Manager passed to terminal constructor", imsTmManager, (IImstmManagerSpi) arguments.get(0));
                    Assert.assertEquals("Wrong Framework passed to terminal constructor", framework, (IFramework) arguments.get(1));
                    Assert.assertEquals("Wrong IMS System passed to terminal constructor", system, (IImsSystem) arguments.get(2));
                    Assert.assertEquals("Wrong 'Connect at startup' value passed to terminal constructor", true, (Boolean) arguments.get(3));
                    Assert.assertEquals("Wrong text scanner passed to terminal constructor", textScanManager, (ITextScannerManagerSpi) arguments.get(4));
                    Assert.assertEquals("Wrong 'Login credentials' value passed to terminal constructor", DEFAULT_CREDENTIALS, (String) arguments.get(5));
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0); 
            Mockito.verify(provisioner).imsProvisionGenerate();
            Mockito.verify(provisioner).provision(DEFAULT_TAG, DEFAULT_TAG, annotations);

            // Verify generated system
            Map<String, IImsSystem> systems = imsTmManager.getTaggedImsSystems();
            Assert.assertEquals("Wrong number of IMS systems provisioned", 1, systems.size());
            Assert.assertTrue("Wrong system provisioned", systems.containsValue(system));

            // Verify system is registered
            Object registeredSystem = imsTmManager.getRegisteredField(ValidDefaultTest.class.getField("imsSystem"));
            Assert.assertNotNull("IMS System not registered", registeredSystem);
            Assert.assertEquals("Wrong object registered as IMS System for field 'imsSystem'", system, registeredSystem);

            // Verify generated terminal
            Assert.assertEquals("Wrong number of IMS terminals constructed", 1, terminals.constructed().size());
            ImsTerminalImpl terminal = terminals.constructed().get(0);
            List<IImsTerminal> savedTerminals = imsTmManager.getImsTerminals();
            Assert.assertEquals("Wrong number of IMS terminals generated", 1, savedTerminals.size());
            Assert.assertTrue("Wrong terminal generated", savedTerminals.contains(terminal));
        }
    }

    @Test
    public void testProvisionGenerateNoTerminal() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        Mockito.doReturn(SystemOnlyTest.class).when(galasaTest).getJavaTestClass();
        List<Annotation> annotations = getSystemAnnotations(SystemOnlyTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(DEFAULT_TAG, DEFAULT_TAG, annotations)).thenReturn(system);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0); 
            Mockito.verify(provisioner).imsProvisionGenerate();
            Mockito.verify(provisioner).provision(DEFAULT_TAG, DEFAULT_TAG, annotations);

            // Verify generated system
            Map<String, IImsSystem> systems = imsTmManager.getTaggedImsSystems();
            Assert.assertEquals("Wrong number of IMS systems provisioned", 1, systems.size());
            Assert.assertTrue("Wrong system provisioned", systems.containsValue(system));

            // Verify system is registered
            Object registeredSystem = imsTmManager.getRegisteredField(SystemOnlyTest.class.getField("imsSystem"));
            Assert.assertNotNull("IMS System not registered", registeredSystem);
            Assert.assertEquals("Wrong object registered as IMS System for field 'imsSystem'", system, registeredSystem);

            // Verify no terminal
            Assert.assertEquals("Wrong number of IMS terminals generated", 0, imsTmManager.getImsTerminals().size());
        }
    }

    @Test
    public void testProvisionGenerateNoSystemForTerminal() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        Mockito.doReturn(BadDummyTest.class).when(galasaTest).getJavaTestClass();
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, DEFAULT_TAG, getSystemAnnotations(BadDummyTest.class))).thenReturn(system);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            String expectedMessage1 = "Problem generating Test Class fields";
            String expectedMessage2 = "Unable to setup IMS Terminal for field 'terminal', for system with tag 'TERM01' as a system with a matching 'imsTag' tag was not found, or the system was not provisioned.";
            ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
                imsTmManager.provisionGenerate();
            });
        	Assert.assertEquals("Exception should contain expected message", expectedMessage1, expectedException.getMessage());
            Throwable expectedException2 = expectedException.getCause();
            Assert.assertTrue("Exception should have expected cause", expectedException2 instanceof InvocationTargetException);
            Throwable expectedException3 = expectedException2.getCause();
            Assert.assertTrue("Exception should have expected root cause", expectedException3 instanceof ImstmManagerException);
        	Assert.assertEquals("Root cause should contain expected message", expectedMessage2, expectedException3.getMessage());
        }
    }

    @Test
    public void testProvisionGenerateAlreadyProvisioned() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        Field f = ValidTest.class.getField("imsSystem");
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    List<?> arguments = context.arguments();
                    Assert.assertEquals("Wrong IMS TM Manager passed to terminal constructor", imsTmManager, (IImstmManagerSpi) arguments.get(0));
                    Assert.assertEquals("Wrong Framework passed to terminal constructor", framework, (IFramework) arguments.get(1));
                    Assert.assertEquals("Wrong IMS System passed to terminal constructor", system, (IImsSystem) arguments.get(2));
                    Assert.assertEquals("Wrong 'Connect at startup' value passed to terminal constructor", false, (Boolean) arguments.get(3));
                    Assert.assertEquals("Wrong text scanner passed to terminal constructor", textScanManager, (ITextScannerManagerSpi) arguments.get(4));
                    Assert.assertEquals("Wrong 'Login credentials' value passed to terminal constructor", TEST_CREDENTIALS, (String) arguments.get(5));
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.generateImsSystem(f, annotations);
            DseProvisioningImpl provisioner = provisioners.constructed().get(0);
            Mockito.verify(provisioner).provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations);
            imsTmManager.provisionGenerate();
            Mockito.verify(provisioner).imsProvisionGenerate();
            Mockito.verifyNoMoreInteractions(provisioner); 

            // Verify generated system
            Map<String, IImsSystem> systems = imsTmManager.getTaggedImsSystems();
            Assert.assertEquals("Wrong number of IMS systems provisioned", 1, systems.size());
            Assert.assertTrue("Wrong system provisioned", systems.containsValue(system));

            // Verify system is registered
            Object registeredSystem = imsTmManager.getRegisteredField(f);
            Assert.assertNotNull("IMS System not registered", registeredSystem);
            Assert.assertEquals("Wrong object registered as IMS System for field 'imsSystem'", system, registeredSystem);

            // Verify generated terminal
            Assert.assertEquals("Wrong number of IMS terminals constructed", 1, terminals.constructed().size());
            ImsTerminalImpl terminal = terminals.constructed().get(0);
            List<IImsTerminal> savedTerminals = imsTmManager.getImsTerminals();
            Assert.assertEquals("Wrong number of IMS terminals generated", 1, savedTerminals.size());
            Assert.assertTrue("Wrong terminal generated", savedTerminals.contains(terminal));
        }
    }

    @Test
    public void testProvisionGenerateProvisionFails() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);

            // Mocked provisioner will return a null system on provision
            String expectedMessage = "Unable to provision IMS System tagged " + TEST_IMS_TAG;
            ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
                imsTmManager.provisionGenerate();
            });
        	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
        }
    }

    // One provisionGenerate() test that cannot currently be done is when instantiation of the
    // terminal throws a TerminalInterruptedException as there is no way to mock this using
    // just Mockito.
    
    @Test
    public void testGenerateImsSystem() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.generateImsSystem(ValidTest.class.getField("imsSystem"), annotations);
            DseProvisioningImpl provisioner = provisioners.constructed().get(0); 
            Mockito.verify(provisioner).provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations);

            // Verify generated system
            Map<String, IImsSystem> systems = imsTmManager.getTaggedImsSystems();
            Assert.assertEquals("Wrong number of IMS systems provisioned", 1, systems.size());
            Assert.assertTrue("Wrong system provisioned", systems.containsValue(system));
        }
    }

    @Test
    public void testGenerateImsSystemDefaults() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        Mockito.doReturn(ValidDefaultTest.class).when(galasaTest).getJavaTestClass();
        List<Annotation> annotations = getSystemAnnotations(ValidDefaultTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(DEFAULT_TAG, DEFAULT_TAG, annotations)).thenReturn(system);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.generateImsSystem(ValidDefaultTest.class.getField("imsSystem"), annotations);
            DseProvisioningImpl provisioner = provisioners.constructed().get(0); 
            Mockito.verify(provisioner).provision(DEFAULT_TAG, DEFAULT_TAG, annotations);

            // Verify generated system
            Map<String, IImsSystem> systems = imsTmManager.getTaggedImsSystems();
            Assert.assertEquals("Wrong number of IMS systems provisioned", 1, systems.size());
            Assert.assertTrue("Wrong system provisioned", systems.containsValue(system));
        }
    }

    @Test
    public void testGenerateImsSystemAlreadyProvisioned() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        Field f = ValidTest.class.getField("imsSystem");
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.generateImsSystem(f, annotations);
            DseProvisioningImpl provisioner = provisioners.constructed().get(0);
            Mockito.verify(provisioner).provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations);
            imsTmManager.generateImsSystem(ValidTest.class.getField("imsSystem"), annotations);
            Mockito.verifyNoMoreInteractions(provisioner); 

            // Verify generated system
            Map<String, IImsSystem> systems = imsTmManager.getTaggedImsSystems();
            Assert.assertEquals("Wrong number of IMS systems provisioned", 1, systems.size());
            Assert.assertTrue("Wrong system provisioned", systems.containsValue(system));
        }
    }

    @Test
    public void testGenerateImsSystemProvisionFails() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);

            // Mocked provisioner will return a null system on provision
            String expectedMessage = "Unable to provision IMS System tagged " + TEST_IMS_TAG;
            ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
                imsTmManager.generateImsSystem(ValidTest.class.getField("imsSystem"), getSystemAnnotations(ValidTest.class));
            });
        	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
        }
    }

    @Test
    public void testGenerateImsTerminalFromField() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    List<?> arguments = context.arguments();
                    Assert.assertEquals("Wrong IMS TM Manager passed to terminal constructor", imsTmManager, (IImstmManagerSpi) arguments.get(0));
                    Assert.assertEquals("Wrong Framework passed to terminal constructor", framework, (IFramework) arguments.get(1));
                    Assert.assertEquals("Wrong IMS System passed to terminal constructor", system, (IImsSystem) arguments.get(2));
                    Assert.assertEquals("Wrong 'Connect at startup' value passed to terminal constructor", false, (Boolean) arguments.get(3));
                    Assert.assertEquals("Wrong text scanner passed to terminal constructor", textScanManager, (ITextScannerManagerSpi) arguments.get(4));
                    Assert.assertEquals("Wrong 'Login credentials' value passed to terminal constructor", TEST_CREDENTIALS, (String) arguments.get(5));
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.generateImsSystem(ValidTest.class.getField("imsSystem"), annotations);
            imsTmManager.generateImsTerminal(ValidTest.class.getField("terminal"), getTerminalAnnotations(ValidTest.class));

            // Verify generated terminal
            Assert.assertEquals("Wrong number of IMS terminals constructed", 1, terminals.constructed().size());
            ImsTerminalImpl terminal = terminals.constructed().get(0);
            List<IImsTerminal> savedTerminals = imsTmManager.getImsTerminals();
            Assert.assertEquals("Wrong number of IMS terminals generated", 1, savedTerminals.size());
            Assert.assertTrue("Wrong terminal generated", savedTerminals.contains(terminal));
        }
    }

    @Test
    public void testGenerateImsTerminalFromFieldDefaults() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        Mockito.doReturn(ValidDefaultTest.class).when(galasaTest).getJavaTestClass();
        List<Annotation> annotations = getSystemAnnotations(ValidDefaultTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(DEFAULT_TAG, DEFAULT_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    List<?> arguments = context.arguments();
                    Assert.assertEquals("Wrong IMS TM Manager passed to terminal constructor", imsTmManager, (IImstmManagerSpi) arguments.get(0));
                    Assert.assertEquals("Wrong Framework passed to terminal constructor", framework, (IFramework) arguments.get(1));
                    Assert.assertEquals("Wrong IMS System passed to terminal constructor", system, (IImsSystem) arguments.get(2));
                    Assert.assertEquals("Wrong 'Connect at startup' value passed to terminal constructor", true, (Boolean) arguments.get(3));
                    Assert.assertEquals("Wrong text scanner passed to terminal constructor", textScanManager, (ITextScannerManagerSpi) arguments.get(4));
                    Assert.assertEquals("Wrong 'Login credentials' value passed to terminal constructor", DEFAULT_CREDENTIALS, (String) arguments.get(5));
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.generateImsSystem(ValidDefaultTest.class.getField("imsSystem"), annotations);
            imsTmManager.generateImsTerminal(ValidDefaultTest.class.getField("terminal"), getTerminalAnnotations(ValidDefaultTest.class));

            // Verify generated terminal
            Assert.assertEquals("Wrong number of IMS terminals constructed", 1, terminals.constructed().size());
            ImsTerminalImpl terminal = terminals.constructed().get(0);
            List<IImsTerminal> savedTerminals = imsTmManager.getImsTerminals();
            Assert.assertEquals("Wrong number of IMS terminals generated", 1, savedTerminals.size());
            Assert.assertTrue("Wrong terminal generated", savedTerminals.contains(terminal));
        }
    }

    @Test
    public void testGenerateImsTerminalFromFieldNoSystemForTerminal() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        Mockito.doReturn(BadDummyTest.class).when(galasaTest).getJavaTestClass();
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            String expectedMessage = "Unable to setup IMS Terminal for field 'terminal', for system with tag 'TERM01' as a system with a matching 'imsTag' tag was not found, or the system was not provisioned.";
            ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
                imsTmManager.generateImsTerminal(BadDummyTest.class.getField("terminal"), getTerminalAnnotations(BadDummyTest.class));
            });
        	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
        }
    }

    // One generateImsTerminal(Field, List<Annotation>) test that cannot currently be done is when instantiation of the
    // terminal throws a TerminalInterruptedException as there is no way to mock this using
    // just Mockito.
    
    @Test
    public void testGenerateImsTerminalFromTag() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    List<?> arguments = context.arguments();
                    Assert.assertEquals("Wrong number of arguments", 5, arguments.size());
                    Assert.assertEquals("Wrong IMS TM Manager passed to terminal constructor", imsTmManager, (IImstmManagerSpi) arguments.get(0));
                    Assert.assertEquals("Wrong Framework passed to terminal constructor", framework, (IFramework) arguments.get(1));
                    Assert.assertEquals("Wrong IMS System passed to terminal constructor", system, (IImsSystem) arguments.get(2));
                    Assert.assertEquals("Wrong 'Connect at startup' value passed to terminal constructor", true, (Boolean) arguments.get(3));
                    Assert.assertEquals("Wrong text scanner passed to terminal constructor", textScanManager, (ITextScannerManagerSpi) arguments.get(4));
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.generateImsSystem(ValidTest.class.getField("imsSystem"), annotations);
            imsTmManager.generateImsTerminal("SYS01");

            // Verify generated terminal
            Assert.assertEquals("Wrong number of IMS terminals constructed", 1, terminals.constructed().size());
            ImsTerminalImpl terminal = terminals.constructed().get(0);
            List<IImsTerminal> savedTerminals = imsTmManager.getImsTerminals();
            Assert.assertEquals("Wrong number of IMS terminals generated", 1, savedTerminals.size());
            Assert.assertTrue("Wrong terminal generated", savedTerminals.contains(terminal));
        }
    }

    @Test
    public void testGenerateImsTerminalFromTagNoSystemForTerminal() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        Mockito.doReturn(BadDummyTest.class).when(galasaTest).getJavaTestClass();
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            String expectedMessage = "Unable to setup IMS Terminal for tag TERM01, no system was provisioned";
            ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
                imsTmManager.generateImsTerminal("TERM01");
            });
        	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
        }
    }

    // One generateImsTerminal(String) test that cannot currently be done is when instantiation of the
    // terminal throws a TerminalInterruptedException as there is no way to mock this using
    // just Mockito.
    
    @Test
    public void testLocateImsSystem() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.generateImsSystem(ValidTest.class.getField("imsSystem"), annotations);
            IImsSystem located = imsTmManager.locateImsSystem("SYS01");

            // Verify located system
            Assert.assertTrue("Wrong system located", located == system);
        }
    }

    @Test
    public void testLocateImsSystemNotFound() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class)) {
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            String expectedMessage = "Unable to locate IMS System for tag SYS01";
            ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
                imsTmManager.locateImsSystem("SYS01");
            });
            Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
        }
    }

    @Test
    public void testProvisionBuild() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionBuild();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0);
            Mockito.verify(provisioner).imsProvisionBuild();
        }
    }

    @Test
    public void testProvisionStartNoTerminals() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class);
                MockedConstruction<ImstmDefaultLogonProvider> lp = Mockito.mockConstruction(ImstmDefaultLogonProvider.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionStart();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0);
            Mockito.verify(provisioner).imsProvisionStart();

            // Verify logon provider
            List<ImstmDefaultLogonProvider> mockedProviders = lp.constructed();
            Assert.assertEquals("Wrong number of default logon providers created", 1, mockedProviders.size());
            List<IImsSystemLogonProvider> providers = imsTmManager.getLogonProviders();
            Assert.assertEquals("Wrong number of logon providers registered", 1, providers.size());
            Assert.assertTrue("Wrong logon provider registered", mockedProviders.get(0) == providers.get(0));
        }
    }

    @Test
    public void testProvisionStartConnectedAutoconnectStarted() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImstmDefaultLogonProvider> lp = Mockito.mockConstruction(ImstmDefaultLogonProvider.class);
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    Mockito.when(mock.isConnected()).thenReturn(true);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0); 
            Mockito.verify(provisioner).imsProvisionGenerate();
            Mockito.verify(provisioner).provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations);
            imsTmManager.provisionStart();
            IImsTerminal terminal = imsTmManager.getImsTerminals().get(0);
            Mockito.verify(terminal).isConnected();
            Mockito.verifyNoMoreInteractions(terminal);

            // Verify logon provider
            List<ImstmDefaultLogonProvider> mockedProviders = lp.constructed();
            Assert.assertEquals("Wrong number of default logon providers created", 1, mockedProviders.size());
            List<IImsSystemLogonProvider> providers = imsTmManager.getLogonProviders();
            Assert.assertEquals("Wrong number of logon providers registered", 1, providers.size());
            Assert.assertTrue("Wrong logon provider registered", mockedProviders.get(0) == providers.get(0));
        }
    }

    @Test
    public void testProvisionStartNotConnectedNoAutoconnectStarted() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImstmDefaultLogonProvider> lp = Mockito.mockConstruction(ImstmDefaultLogonProvider.class);
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    Mockito.when(mock.isConnected()).thenReturn(false);
                    Mockito.when(mock.isConnectAtStartup()).thenReturn(false);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0); 
            Mockito.verify(provisioner).imsProvisionGenerate();
            Mockito.verify(provisioner).provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations);
            imsTmManager.provisionStart();
            ImsTerminalImpl terminal = (ImsTerminalImpl) imsTmManager.getImsTerminals().get(0);
            Mockito.verify(terminal).isConnected();
            Mockito.verify(terminal).isConnectAtStartup();
            Mockito.verifyNoMoreInteractions(terminal);

            // Verify logon provider
            List<ImstmDefaultLogonProvider> mockedProviders = lp.constructed();
            Assert.assertEquals("Wrong number of default logon providers created", 1, mockedProviders.size());
            List<IImsSystemLogonProvider> providers = imsTmManager.getLogonProviders();
            Assert.assertEquals("Wrong number of logon providers registered", 1, providers.size());
            Assert.assertTrue("Wrong logon provider registered", mockedProviders.get(0) == providers.get(0));
        }
    }

    @Test
    public void testProvisionStartNotConnectedAutoconnectNotStarted() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImstmDefaultLogonProvider> lp = Mockito.mockConstruction(ImstmDefaultLogonProvider.class);
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    Mockito.when(mock.isConnected()).thenReturn(false);
                    Mockito.when(mock.isConnectAtStartup()).thenReturn(true);
                    Mockito.when(mock.getImsSystem()).thenReturn(system);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            Mockito.when(system.isProvisionStart()).thenReturn(false);
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0); 
            Mockito.verify(provisioner).imsProvisionGenerate();
            Mockito.verify(provisioner).provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations);
            imsTmManager.provisionStart();
            ImsTerminalImpl terminal = (ImsTerminalImpl) imsTmManager.getImsTerminals().get(0);
            Mockito.verify(terminal).isConnected();
            Mockito.verify(terminal).isConnectAtStartup();
            Mockito.verify(terminal).getImsSystem();
            Mockito.verifyNoMoreInteractions(terminal);

            // Verify logon provider
            List<ImstmDefaultLogonProvider> mockedProviders = lp.constructed();
            Assert.assertEquals("Wrong number of default logon providers created", 1, mockedProviders.size());
            List<IImsSystemLogonProvider> providers = imsTmManager.getLogonProviders();
            Assert.assertEquals("Wrong number of logon providers registered", 1, providers.size());
            Assert.assertTrue("Wrong logon provider registered", mockedProviders.get(0) == providers.get(0));
        }
    }

    @Test
    public void testProvisionStartNotConnectedAutoconnectStarted() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImstmDefaultLogonProvider> lp = Mockito.mockConstruction(ImstmDefaultLogonProvider.class);
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    Mockito.when(mock.isConnected()).thenReturn(false);
                    Mockito.when(mock.isConnectAtStartup()).thenReturn(true);
                    Mockito.when(mock.getImsSystem()).thenReturn(system);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            Mockito.when(system.isProvisionStart()).thenReturn(true);
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0); 
            Mockito.verify(provisioner).imsProvisionGenerate();
            Mockito.verify(provisioner).provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations);
            imsTmManager.provisionStart();
            ImsTerminalImpl terminal = (ImsTerminalImpl) imsTmManager.getImsTerminals().get(0);
            Mockito.verify(terminal).isConnected();
            Mockito.verify(terminal).isConnectAtStartup();
            Mockito.verify(terminal).getImsSystem();
            Mockito.verify(terminal).connectToImsSystem();
            Mockito.verifyNoMoreInteractions(terminal);

            // Verify logon provider
            List<ImstmDefaultLogonProvider> mockedProviders = lp.constructed();
            Assert.assertEquals("Wrong number of default logon providers created", 1, mockedProviders.size());
            List<IImsSystemLogonProvider> providers = imsTmManager.getLogonProviders();
            Assert.assertEquals("Wrong number of logon providers registered", 1, providers.size());
            Assert.assertTrue("Wrong logon provider registered", mockedProviders.get(0) == providers.get(0));
        }
    }

    @Test
    public void testProvisionStartConnectFail() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImstmDefaultLogonProvider> lp = Mockito.mockConstruction(ImstmDefaultLogonProvider.class);
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    Mockito.when(mock.isConnected()).thenReturn(false);
                    Mockito.when(mock.isConnectAtStartup()).thenReturn(true);
                    Mockito.when(mock.getImsSystem()).thenReturn(system);
                    Mockito.doThrow(new ImstmManagerException()).when(mock).connectToImsSystem();
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            Mockito.when(system.isProvisionStart()).thenReturn(true);
            Mockito.when(system.toString()).thenReturn("TEST SYSTEM");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0); 
            Mockito.verify(provisioner).imsProvisionGenerate();
            Mockito.verify(provisioner).provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations);
            String expectedMessage = "Failed to connect to the TEST SYSTEM";
            ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
                imsTmManager.provisionStart();
            });
            Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());

            // Verify logon provider
            List<ImstmDefaultLogonProvider> mockedProviders = lp.constructed();
            Assert.assertEquals("Wrong number of default logon providers created", 1, mockedProviders.size());
            List<IImsSystemLogonProvider> providers = imsTmManager.getLogonProviders();
            Assert.assertEquals("Wrong number of logon providers registered", 1, providers.size());
            Assert.assertTrue("Wrong logon provider registered", mockedProviders.get(0) == providers.get(0));
        }
    }

    @Test
    public void testProvisionStopNoTerminals() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionStop();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0);
            Mockito.verify(provisioner).imsProvisionStop();
        }
    }

    @Test
    public void testProvisionStop() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            imsTmManager.provisionStop();
            ImsTerminalImpl terminal = (ImsTerminalImpl) imsTmManager.getImsTerminals().get(0);
            Mockito.verify(terminal).writeRasOutput();
            Mockito.verify(terminal).flushTerminalCache();;
            Mockito.verify(terminal).disconnect();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0);
            Mockito.verify(provisioner).imsProvisionStop();
        }
    }

    @Test
    public void testProvisionStopTerminalInterrupted() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    Mockito.when(mock.isConnected()).thenReturn(false);
                    Mockito.when(mock.isConnectAtStartup()).thenReturn(true);
                    Mockito.when(mock.getImsSystem()).thenReturn(system);
                    Mockito.doThrow(new TerminalInterruptedException()).when(mock).disconnect();
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            imsTmManager.provisionStop();
            ImsTerminalImpl terminal = (ImsTerminalImpl) imsTmManager.getImsTerminals().get(0);
            Mockito.verify(terminal).writeRasOutput();
            Mockito.verify(terminal).flushTerminalCache();;
            Mockito.verify(terminal).disconnect();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0);
            Mockito.verify(provisioner).imsProvisionStop();
        }
    }

    @Test
    public void testProvisionDiscard() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionDiscard();
            DseProvisioningImpl provisioner = provisioners.constructed().get(0);
            Mockito.verify(provisioner).imsProvisionDiscard();
        }
    }

    @Test
    public void testRegisterProvisioner() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            IImsSystemProvisioner provisioner = Mockito.mock(DseProvisioningImpl.class);
            imsTmManager.registerProvisioner(provisioner);
            imsTmManager.provisionDiscard();
            Mockito.verify(provisioner).imsProvisionDiscard();
        }
    }

    @Test
    public void testGetZosManager() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            Assert.assertTrue("Wrong Zos Manager returned", imsTmManager.getZosManager() == zosManager);
        }
    }

    @Test
    public void testGetProvisionType() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class)) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("ABC");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            Assert.assertEquals("Wrong provision type returned", "ABC", imsTmManager.getProvisionType());
        }
    }

    @Test
    public void testGetDefaultVersion() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        try (MockedStatic<DefaultVersion> defaultVersion = Mockito.mockStatic(DefaultVersion.class)) {
            ProductVersion vrm = ProductVersion.v(1).r(2).m(3);
            defaultVersion.when(() -> DefaultVersion.get()).thenReturn(vrm);
            Assert.assertEquals("Wrong default version returned", vrm, imsTmManager.getDefaultVersion());
        }
    }

    @Test
    public void testImstmSystemStartedNoAutoconnect() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    Mockito.when(mock.getImsSystem()).thenReturn(system);
                    Mockito.when(mock.isConnectAtStartup()).thenReturn(false);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            imsTmManager.imstmSystemStarted(system);
            ImsTerminalImpl terminal = (ImsTerminalImpl) imsTmManager.getImsTerminals().get(0);
            Mockito.verify(terminal).getImsSystem();
            Mockito.verify(terminal).isConnectAtStartup();
            Mockito.verifyNoMoreInteractions(terminal);
        }
    }

    @Test
    public void testImstmSystemStartedAutoconnect() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImstmDefaultLogonProvider> lp = Mockito.mockConstruction(ImstmDefaultLogonProvider.class);
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    Mockito.when(mock.isConnectAtStartup()).thenReturn(true);
                    Mockito.when(mock.getImsSystem()).thenReturn(system);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            imsTmManager.imstmSystemStarted(system);
            ImsTerminalImpl terminal = (ImsTerminalImpl) imsTmManager.getImsTerminals().get(0);
            Mockito.verify(terminal).isConnectAtStartup();
            Mockito.verify(terminal).getImsSystem();
            Mockito.verify(terminal).connectToImsSystem();
            Mockito.verifyNoMoreInteractions(terminal);
        }
    }

    @Test
    public void testImstmSystemNoMatch() throws Exception{
        allManagers.add(zosManager);
        allManagers.add(textScanManager);
        List<Annotation> annotations = getSystemAnnotations(ValidTest.class);
        try (MockedStatic<ProvisionType> provisionType = Mockito.mockStatic(ProvisionType.class);
                MockedConstruction<DseProvisioningImpl> provisioners = Mockito.mockConstruction(DseProvisioningImpl.class, (mock, context) -> {
                    Mockito.when(mock.provision(TEST_IMS_TAG, TEST_IMAGE_TAG, annotations)).thenReturn(system);
                });
                MockedConstruction<ImsTerminalImpl> terminals = Mockito.mockConstruction(ImsTerminalImpl.class, (mock, context) -> {
                    Mockito.when(mock.getImsSystem()).thenReturn(system);
                    Mockito.when(mock.isConnectAtStartup()).thenReturn(false);
                })) {
            provisionType.when(() -> ProvisionType.get()).thenReturn("DSE");
            imsTmManager.initialise(framework, allManagers, activeManagers, galasaTest);
            imsTmManager.provisionGenerate();
            imsTmManager.imstmSystemStarted(Mockito.mock(DseImsImpl.class));
            ImsTerminalImpl terminal = (ImsTerminalImpl) imsTmManager.getImsTerminals().get(0);
            Mockito.verify(terminal).getImsSystem();
            Mockito.verifyNoMoreInteractions(terminal);
        }
    }

    @Test
    public void testGetNextTerminalId() {
        Assert.assertEquals("Wrong terminal id returned by getNextTerminalId()", APPLID + "_1", imsTmManager.getNextTerminalId(system));
        Assert.assertEquals("Wrong terminal id returned by getNextTerminalId()", APPLID + "_2", imsTmManager.getNextTerminalId(system));
    }

    private List<Annotation> getSystemAnnotations(Class<?> testClass) throws NoSuchFieldException, SecurityException {
        List<Annotation> systemAnnotations = new ArrayList<Annotation>();
        Field system = testClass.getField("imsSystem");
        systemAnnotations.add(system.getAnnotation(ImsSystem.class));
        return systemAnnotations;
    }

    private List<Annotation> getTerminalAnnotations(Class<?> testClass) throws NoSuchFieldException, SecurityException {
        List<Annotation> terminalAnnotations = new ArrayList<Annotation>();
        Field system = testClass.getField("terminal");
        terminalAnnotations.add(system.getAnnotation(ImsTerminal.class));
        return terminalAnnotations;
    }
}
