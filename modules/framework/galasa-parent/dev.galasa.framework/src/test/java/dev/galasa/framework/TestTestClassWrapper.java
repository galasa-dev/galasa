/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import dev.galasa.ContinueOnTestFailure;
import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockTestRunnerDataProvider;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestTestClassWrapper {

    @ContinueOnTestFailure
    class MockTestClassWithContinueOnTestFailure {
    }

    class MockTestClassWithoutContinueOnTestFailure {
    }

    private IRun createMockRun(Class<?> testClass) {
        String TEST_STREAM_REPO_URL = "http://myhost/myRepositoryForMyRun";
        String TEST_BUNDLE_NAME = "myTestBundle";
        String TEST_CLASS_NAME = testClass.getName();
        String TEST_RUN_NAME = "myTestRun";
        String TEST_STREAM = "myStreamForMyRun";
        String TEST_STREAM_OBR = "http://myhost/myObrForMyRun";
        String TEST_REQUESTOR_NAME = "daffyduck";
        boolean TEST_IS_LOCAL_RUN_TRUE = true;

        return new MockRun(
            TEST_BUNDLE_NAME, 
            TEST_CLASS_NAME , 
            TEST_RUN_NAME, 
            TEST_STREAM, 
            TEST_STREAM_OBR , 
            TEST_STREAM_REPO_URL,
            TEST_REQUESTOR_NAME,
            TEST_IS_LOCAL_RUN_TRUE
        );
    }

    @Test
    public void testClassAnnotatedWithContinueOnTestFailureReturnsTrue() throws Exception {
        // Given...
        TestRunner testRunner = new TestRunner();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithContinueOnTestFailure.class));

        testRunner.init(mockDataProvider);

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";
        TestClassWrapper testClassWrapper = new TestClassWrapper(testRunner, testBundle, MockTestClassWithContinueOnTestFailure.class, testStructure);

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isTrue();
    }

    @Test
    public void testClassWithoutContinueOnTestFailureReturnsFalse() throws Exception {
        // Given...
        TestRunner testRunner = new TestRunner();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));

        testRunner.init(mockDataProvider);

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";
        TestClassWrapper testClassWrapper = new TestClassWrapper(testRunner, testBundle, MockTestClassWithoutContinueOnTestFailure.class, testStructure);

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isFalse();
    }

    @Test
    public void testClassWithCPSContinueOnTestFailureReturnsTrue() throws Exception {
        // Given...
        TestRunner testRunner = new TestRunner();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        cps.setProperty("continue.on.test.failure", "true");

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));

        testRunner.init(mockDataProvider);

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";
        TestClassWrapper testClassWrapper = new TestClassWrapper(testRunner, testBundle, MockTestClassWithoutContinueOnTestFailure.class, testStructure);

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isTrue();
    }

    @Test
    public void testClassWithCPSContinueOnTestFailureSetToFalseReturnsFalse() throws Exception {
        // Given...
        TestRunner testRunner = new TestRunner();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        cps.setProperty("continue.on.test.failure", "false");

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));

        testRunner.init(mockDataProvider);

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";
        TestClassWrapper testClassWrapper = new TestClassWrapper(testRunner, testBundle, MockTestClassWithoutContinueOnTestFailure.class, testStructure);

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isFalse();
    }

    @Test
    public void testClassWithAnnotationAndCPSContinueOnTestFailureSetToFalseReturnsTrue() throws Exception {
        // Given...
        TestRunner testRunner = new TestRunner();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        cps.setProperty("continue.on.test.failure", "false");

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));

        testRunner.init(mockDataProvider);

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";
        TestClassWrapper testClassWrapper = new TestClassWrapper(testRunner, testBundle, MockTestClassWithContinueOnTestFailure.class, testStructure);

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isTrue();
    }
}
