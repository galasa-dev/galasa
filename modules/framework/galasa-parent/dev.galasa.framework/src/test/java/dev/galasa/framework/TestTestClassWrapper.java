/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.junit.Test;

import dev.galasa.ContinueOnTestFailure;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockLog;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockTestRunManagers;
import dev.galasa.framework.mocks.MockTestRunnerDataProvider;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
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
        boolean isContinueOnTestFailureFromCPS = true ;
        Framework mockFramework = new MockFramework();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithContinueOnTestFailure.class));

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";
        TestClassWrapper testClassWrapper = new TestClassWrapper(testBundle, MockTestClassWithContinueOnTestFailure.class, testStructure, isContinueOnTestFailureFromCPS, mockFramework);

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isTrue();
    }

    @Test
    public void testClassWithoutContinueOnTestFailureReturnsFalse() throws Exception {
        // Given...
        boolean isContinueOnTestFailureFromCPS = false ;
        Framework mockFramework = new MockFramework();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";
        TestClassWrapper testClassWrapper = new TestClassWrapper(testBundle, MockTestClassWithoutContinueOnTestFailure.class, testStructure, isContinueOnTestFailureFromCPS, mockFramework);

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isFalse();
    }

    @Test
    public void testClassWithCPSContinueOnTestFailureReturnsTrue() throws Exception {
        // Given...
        boolean isContinueOnTestFailureFromCPS = true ;
        Framework mockFramework = new MockFramework();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        cps.setProperty("continue.on.test.failure", "true");

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";
        TestClassWrapper testClassWrapper = new TestClassWrapper(testBundle, MockTestClassWithoutContinueOnTestFailure.class, testStructure, isContinueOnTestFailureFromCPS, mockFramework );

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isTrue();
    }

    @Test
    public void testClassWithCPSContinueOnTestFailureSetToFalseReturnsFalse() throws Exception {
        // Given...
        boolean isContinueOnTestFailureFromCPS = false ;
        Framework mockFramework = new MockFramework();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        cps.setProperty("continue.on.test.failure", "false");

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));


        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";
        TestClassWrapper testClassWrapper = new TestClassWrapper(testBundle, MockTestClassWithoutContinueOnTestFailure.class, testStructure, isContinueOnTestFailureFromCPS, mockFramework);

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isFalse();
    }

    @Test
    public void testClassWithAnnotationAndCPSContinueOnTestFailureSetToFalseReturnsTrue() throws Exception {
        // Given...
        boolean isContinueOnTestFailureFromCPS = true ;
        Framework mockFramework = new MockFramework();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        cps.setProperty("continue.on.test.failure", "false");

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";
        TestClassWrapper testClassWrapper = new TestClassWrapper(testBundle, MockTestClassWithContinueOnTestFailure.class, testStructure, isContinueOnTestFailureFromCPS, mockFramework);

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isTrue();
    }


    // We noticed on code inspection that if the test running fails with an exception, that the 
    // @AfterClass method(s) will never get called.
    // This test simulates a failure and checks that the @AfterCLass methods got called regardless.
    // And that the original test failure exception still gets thrown.
    @Test
    public void testAfterClassTestMethodsGetCalledIfRunTestMethodThrowsException() throws Exception {


        String fakeExceptionMessage = "My Fake exception message";

        // Given...
        class TestClassWrapperWhichThrowsExceptionInRunTestMethod extends TestClassWrapper{

            public boolean isAfterMethodAlreadyCalled = false ;

            public TestClassWrapperWhichThrowsExceptionInRunTestMethod(String testBundle,
                    Class<?> testClass, TestStructure testStructure, boolean isContinueOnTestFailureFromCPS, Framework framework) throws ConfigurationPropertyStoreException {
                super(testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , framework );
            }
            
            // runTestMethods should throw a fake exception so we can check that other methods get called despite the exception.
            protected void runTestMethods(@NotNull ITestRunManagers managers, IDynamicStatusStoreService dss, String runName) throws TestRunException {
                throw new TestRunException(fakeExceptionMessage);
            }

            protected void runBeforeClassMethods( @NotNull ITestRunManagers managers ) throws TestRunException {
                // Do nothing.
            }

            protected void runAfterClassMethods( @NotNull ITestRunManagers managers ) throws TestRunException {
                isAfterMethodAlreadyCalled = true;
            }

        };

        String testBundle = null;

        class FakeTest {
        }

        Class<?> testClass = new FakeTest().getClass();
        TestStructure testStructure = new TestStructure();
        boolean isContinueOnTestFailureFromCPS = true ;
        Framework mockFramework = new MockFramework();

        TestClassWrapperWhichThrowsExceptionInRunTestMethod wrapper = new TestClassWrapperWhichThrowsExceptionInRunTestMethod(
            testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , mockFramework );

        ITestRunManagers managers = new MockTestRunManagers(isContinueOnTestFailureFromCPS, null);

        IDynamicStatusStoreService dss = null;
        String runName = null;

        // When...
        TestRunException exGotBack = catchThrowableOfType( () -> wrapper.runMethods( managers, dss, runName), TestRunException.class );


        // Then...
        assertThat(exGotBack).hasMessage(fakeExceptionMessage);
        assertThat(wrapper.isAfterMethodAlreadyCalled)
            .as("The AfterClass method of the test class was not called. When the methods fail with an exception")
            .isTrue();
        
    }


    // We noticed on code inspection that if the test running fails with an exception, that the 
    // Managers are never told that the test has failed.
    // This test simulates a failure and checks that the managers get told about the failure regardless.
    // And that the original test failure exception still gets thrown.
    @Test
    public void testManagersToldAboutTestMethodFailure() throws Exception {

        String fakeExceptionMessage = "My Fake exception message";

        // Given...
        class TestClassWrapperWhichThrowsExceptionInRunTestMethod extends TestClassWrapper{

            public boolean isAfterMethodAlreadyCalled = false ;

            public TestClassWrapperWhichThrowsExceptionInRunTestMethod(String testBundle,
                    Class<?> testClass, TestStructure testStructure, boolean isContinueOnTestFailureFromCPS, Framework framework, Log logger) throws ConfigurationPropertyStoreException {
                super(testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , framework, logger );
            }
            
            // runTestMethods should throw a fake exception so we can check that other methods get called despite the exception.
            protected void runTestMethods(@NotNull ITestRunManagers managers, IDynamicStatusStoreService dss, String runName) throws TestRunException {
                throw new TestRunException(fakeExceptionMessage);
            }

            protected void runBeforeClassMethods( @NotNull ITestRunManagers managers ) throws TestRunException {
                // Do nothing.
            }

            protected void runAfterClassMethods( @NotNull ITestRunManagers managers ) throws TestRunException {
                // Do nothing.
            }

        };

        String testBundle = null;

        class FakeTestClass {
        }

        Class<?> testClass = new FakeTestClass().getClass();
        TestStructure testStructure = new TestStructure();
        boolean isContinueOnTestFailureFromCPS = true ;
        Framework mockFramework = new MockFramework();


        MockLog logger = new MockLog();
        
        TestClassWrapperWhichThrowsExceptionInRunTestMethod wrapper = new TestClassWrapperWhichThrowsExceptionInRunTestMethod(
            testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , mockFramework , (Log)logger);

        MockTestRunManagers managers = new MockTestRunManagers(isContinueOnTestFailureFromCPS, null);

        IDynamicStatusStoreService dss = null;
        String runName = null;

        // When...
        TestRunException exGotBack = catchThrowableOfType( ()-> wrapper.runMethods( managers, dss, runName), TestRunException.class );


        // Then...
        assertThat(exGotBack).hasMessage(fakeExceptionMessage);
        assertThat(managers.calledCountEndOfTestClass)
            .as("The managers were not told to end the test if a test method throws an exception!")
            .isEqualTo(1);

        assertThat(logger.contains("Finishing Test Class structure:-")).isTrue();
        
    }
}
