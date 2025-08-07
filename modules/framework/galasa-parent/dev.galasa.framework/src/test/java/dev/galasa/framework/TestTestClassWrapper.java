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

import dev.galasa.After;
import dev.galasa.Before;
import dev.galasa.ContinueOnTestFailure;
import dev.galasa.framework.internal.runner.InterruptedMonitor;
import dev.galasa.framework.internal.runner.InterruptedMonitorImpl;
import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockIResultArchiveStore;
import dev.galasa.framework.mocks.MockLog;
import dev.galasa.framework.mocks.MockRASStoreService;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockTestRunManagers;
import dev.galasa.framework.mocks.MockTestRunnerDataProvider;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DssPropertyKeyRunNameSuffix;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.Result;
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


    public static class TestWrapperWhichDoesNothing extends TestClassWrapper{

        public TestWrapperWhichDoesNothing(String testBundle,
                Class<?> testClass, TestStructure testStructure, boolean isContinueOnTestFailureFromCPS, IResultArchiveStore ras, InterruptedMonitor interruptedMonitor , Log logger ) throws ConfigurationPropertyStoreException {
            super(testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , ras, interruptedMonitor , logger );
        }
        
        // runTestMethods should throw a fake exception so we can check that other methods get called despite the exception.
        protected void runTestMethods(@NotNull ITestRunManagers managers, IDynamicStatusStoreService dss, String runName) throws TestRunException {
            // Do nothing.
        }

        protected void runBeforeClassMethods( @NotNull ITestRunManagers managers ) throws TestRunException {
            // Do nothing.
        }

        protected void runAfterClassMethods( @NotNull ITestRunManagers managers ) throws TestRunException {
            // Do nothing.
        }

    };

    class TestClassWrapperWhichThrowsExceptionInRunTestMethod extends TestWrapperWhichDoesNothing{

        public boolean isAfterMethodAlreadyCalled = false ;
        private String fakeExceptionMessage;

        public TestClassWrapperWhichThrowsExceptionInRunTestMethod(String testBundle,
                Class<?> testClass, TestStructure testStructure, boolean isContinueOnTestFailureFromCPS, IResultArchiveStore ras, InterruptedMonitor interruptedMonitor , Log logger, String fakeExceptionMessage ) throws ConfigurationPropertyStoreException {
            super(testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , ras, interruptedMonitor , logger );
            this.fakeExceptionMessage = fakeExceptionMessage;
        }
        
        // runTestMethods should throw a fake exception so we can check that other methods get called despite the exception.
        protected void runTestMethods(@NotNull ITestRunManagers managers, IDynamicStatusStoreService dss, String runName) throws TestRunException {
            throw new TestRunException(fakeExceptionMessage);
        }

        protected void runAfterClassMethods( @NotNull ITestRunManagers managers ) throws TestRunException {
            isAfterMethodAlreadyCalled = true;
        }
    };

    @Test
    public void testClassAnnotatedWithContinueOnTestFailureReturnsTrue() throws Exception {
        // Given...
        boolean isContinueOnTestFailureFromCPS = true ;

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithContinueOnTestFailure.class));

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";

        String testRunName = "U12346";
        InterruptedMonitorImpl interruptedMonitor = new InterruptedMonitorImpl(dss,testRunName);
        
        TestClassWrapper testClassWrapper = new TestClassWrapper(testBundle, MockTestClassWithContinueOnTestFailure.class, testStructure, 
        isContinueOnTestFailureFromCPS, new MockIResultArchiveStore() , interruptedMonitor);
        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isTrue();
    }

    @Test
    public void testClassWithoutContinueOnTestFailureReturnsFalse() throws Exception {
        // Given...
        boolean isContinueOnTestFailureFromCPS = false ;

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";

        String testRunName = "U12346";
        InterruptedMonitorImpl interruptedMonitor = new InterruptedMonitorImpl(dss,testRunName);

        TestClassWrapper testClassWrapper = new TestClassWrapper(testBundle, MockTestClassWithoutContinueOnTestFailure.class, 
            testStructure, isContinueOnTestFailureFromCPS, new MockIResultArchiveStore(), interruptedMonitor);

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isFalse();
    }

    @Test
    public void testClassWithCPSContinueOnTestFailureReturnsTrue() throws Exception {
        // Given...
        boolean isContinueOnTestFailureFromCPS = true ;

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        cps.setProperty("continue.on.test.failure", "true");

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";

        String testRunName = "U12346";
        InterruptedMonitorImpl interruptedMonitor = new InterruptedMonitorImpl(dss,testRunName);

        TestClassWrapper testClassWrapper = new TestClassWrapper(testBundle, MockTestClassWithoutContinueOnTestFailure.class, testStructure, 
        isContinueOnTestFailureFromCPS, new MockIResultArchiveStore(), interruptedMonitor );

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isTrue();
    }

    @Test
    public void testClassWithCPSContinueOnTestFailureSetToFalseReturnsFalse() throws Exception {
        // Given...
        boolean isContinueOnTestFailureFromCPS = false ;

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        cps.setProperty("continue.on.test.failure", "false");

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));


        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";

        String testRunName = "U12346";
        InterruptedMonitorImpl interruptedMonitor = new InterruptedMonitorImpl(dss,testRunName);

        TestClassWrapper testClassWrapper = new TestClassWrapper(
            testBundle, MockTestClassWithoutContinueOnTestFailure.class, 
            testStructure, isContinueOnTestFailureFromCPS, new MockIResultArchiveStore(), interruptedMonitor);

        // When...
        boolean isContinueOnTestFailure = testClassWrapper.isContinueOnTestFailureSet();

        // Then...
        assertThat(isContinueOnTestFailure).isFalse();
    }

    @Test
    public void testClassWithAnnotationAndCPSContinueOnTestFailureSetToFalseReturnsTrue() throws Exception {
        // Given...
        boolean isContinueOnTestFailureFromCPS = true ;

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        cps.setProperty("continue.on.test.failure", "false");

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(createMockRun(MockTestClassWithoutContinueOnTestFailure.class));

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";

        String testRunName = "U12346";
        InterruptedMonitorImpl interruptedMonitor = new InterruptedMonitorImpl(dss,testRunName);

        TestClassWrapper testClassWrapper = new TestClassWrapper(
            testBundle, MockTestClassWithContinueOnTestFailure.class, testStructure, 
            isContinueOnTestFailureFromCPS, new MockIResultArchiveStore() , interruptedMonitor);

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
 
        String testBundle = null;

        Class<?> testClass = new MockTestClassWithoutContinueOnTestFailure().getClass();
        TestStructure testStructure = new TestStructure();
        boolean isContinueOnTestFailureFromCPS = true ;
        MockLog logger = new MockLog();
        String testRunName = "U12346";
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        InterruptedMonitorImpl interruptedMonitor = new InterruptedMonitorImpl(dss,testRunName);

        TestClassWrapperWhichThrowsExceptionInRunTestMethod wrapper = new TestClassWrapperWhichThrowsExceptionInRunTestMethod(
            testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , new MockIResultArchiveStore() , 
            interruptedMonitor, logger , fakeExceptionMessage);

        ITestRunManagers managers = new MockTestRunManagers(isContinueOnTestFailureFromCPS, null);

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

        // Given...
        String fakeExceptionMessage = "My Fake exception message";

        String testBundle = null;

        Class<?> testClass = new MockTestClassWithoutContinueOnTestFailure().getClass();
        TestStructure testStructure = new TestStructure();
        boolean isContinueOnTestFailureFromCPS = true ;

        MockLog logger = new MockLog();
        String testRunName = "U12346";
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        InterruptedMonitorImpl interruptedMonitor = new InterruptedMonitorImpl(dss,testRunName);
        
        TestClassWrapperWhichThrowsExceptionInRunTestMethod wrapper = new TestClassWrapperWhichThrowsExceptionInRunTestMethod(
            testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , new MockIResultArchiveStore() , 
            interruptedMonitor, (Log)logger, fakeExceptionMessage );

        MockTestRunManagers managers = new MockTestRunManagers(isContinueOnTestFailureFromCPS, null);

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

    public static class FakeTestThatCanBeRun {

        public int allMethodsCalledCount = 0;

        FakeTestThatCanBeRun() {
        }

        @Before
        public void myBeforeMethod() throws Exception {
            if (allMethodsCalledCount == 1) {
                throw new TestRunException();
            }
        }

        @dev.galasa.Test
        public void myTestMethod1() throws Exception {
            // Do nothing.
        }

        @dev.galasa.Test
        public void myTestMethod2() throws Exception {
            // Do nothing.
        }

        @After
        public void myAfterMethod() throws Exception {
            if (allMethodsCalledCount == 1) {
                throw new TestRunException();
            }

            allMethodsCalledCount++; // Next loop we want different behaviour.
        }
    }

    @Test
    public void testFieldsOfAllBeforeAndAfterMethodsAreSetInStructureOK() throws Exception {

        // Given...
        String testBundle = null;
        String runName = "U1";

        Class<?> testClass = FakeTestThatCanBeRun.class;
        TestStructure testStructure = new TestStructure();
        boolean isContinueOnTestFailureFromCPS = true;

        MockRASStoreService ras = new MockRASStoreService(null);
   

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        InterruptedMonitorImpl interruptedMonitor = new InterruptedMonitorImpl(dss,runName);
        MockLog logger = new MockLog();
        TestClassWrapper wrapper = new TestClassWrapper(
            testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , ras , interruptedMonitor, (Log)logger);

        ITestRunManagers managers = new MockTestRunManagers(isContinueOnTestFailureFromCPS, null);

        wrapper.parseTestClass();
        wrapper.instantiateTestClass();

        // When...
        wrapper.runMethods(managers, dss, runName);

        // Then...
        assertThat(testStructure.getMethods().size()).isEqualTo(2);

        // The Befores of the TestMethods should not be pointing to the same object...
        assertThat(testStructure.getMethods().get(0).getBefores().get(0))
        .isNotEqualTo(testStructure.getMethods().get(1).getBefores().get(0));

        // The Afters of the TestMethods should not be pointing to the same object...
        assertThat(testStructure.getMethods().get(0).getAfters().get(0))
        .isNotEqualTo(testStructure.getMethods().get(1).getAfters().get(0));

        // The structure of the TestMethods for myTestMethod1 and myTestMethod2 should be different...
        assertThat(testStructure.getMethods().get(0).getBefores().get(0).getResult()).isEqualTo("Passed");
        assertThat(testStructure.getMethods().get(0).getBefores().get(0).getStatus()).isEqualTo("finished");
        assertThat(testStructure.getMethods().get(0).getResult()).isEqualTo("Passed");
        assertThat(testStructure.getMethods().get(0).getAfters().get(0).getResult()).isEqualTo("Passed");
        assertThat(testStructure.getMethods().get(0).getAfters().get(0).getStatus()).isEqualTo("finished");

        assertThat(testStructure.getMethods().get(1).getBefores().get(0).getResult()).isEqualTo("Failed");
        assertThat(testStructure.getMethods().get(1).getBefores().get(0).getStatus()).isEqualTo("finished");
        assertThat(testStructure.getMethods().get(1).getResult()).isNull();
        assertThat(testStructure.getMethods().get(1).getAfters().get(0).getResult()).isEqualTo("Failed");

    }


    // If the test is interrupted, it should cancel the test before it does anything with managers.
    @Test
    public void testRunEndsInFinishedCancelledStateIfTestRunGetsInterruptedBeforeItCallsAnything() throws Exception {

        // Given...

        String testBundle = null;

        Class<?> testClass = new MockTestClassWithoutContinueOnTestFailure().getClass();
        TestStructure testStructure = new TestStructure();
        boolean isContinueOnTestFailureFromCPS = true ;

        MockLog logger = new MockLog();
        String testRunName = "U12346";

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        String dssKey = "run."+testRunName+"."+DssPropertyKeyRunNameSuffix.INTERRUPT_REASON;
        dss.put(dssKey,"Cancelled");
        InterruptedMonitorImpl interruptedMonitor = new InterruptedMonitorImpl(dss,testRunName);
        // If asked, the interrupted monitor will say "we have been interrupted"
        
        TestWrapperWhichDoesNothing wrapper = new TestWrapperWhichDoesNothing(
            testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , new MockIResultArchiveStore() , interruptedMonitor, (Log)logger );

        MockTestRunManagers managers = new MockTestRunManagers(isContinueOnTestFailureFromCPS, null);

        String runName = null;

        // When...
        wrapper.runMethods( managers, dss, runName);

        // Then...
        Result result = wrapper.getResult();
        assertThat(result.isCancelled()).isTrue();
    }

    @dev.galasa.Test
    public static class TestClassWhichGetsInterrupted {
        String testRunName ;
        MockIDynamicStatusStoreService dss;
        public int thirdMethodCalledCounter  ;
        public int firstMethodCalledCounter ;


        public TestClassWhichGetsInterrupted() {
        }
        
        public void init(MockIDynamicStatusStoreService dss, String testRunName) {
            this.testRunName = testRunName;
            this.dss = dss ;
        }

        @dev.galasa.Test
        public void firstMethodDoesNothing() {
            firstMethodCalledCounter +=1 ;
        }

        @dev.galasa.Test
        public void secondMethodCausesInterrupt() throws Exception {
            String dssKey = "run."+testRunName+"." +DssPropertyKeyRunNameSuffix.INTERRUPT_REASON;
            dss.put(dssKey,"Cancelled");
        }

        @dev.galasa.Test
        public void thirdMethodNeverGetsCalled() {
            thirdMethodCalledCounter +=1 ;
        }
    }


    public static class TestWrapperWhichAllowsTestMethodsToRun extends TestClassWrapper{

        public TestWrapperWhichAllowsTestMethodsToRun(String testBundle,
                Class<?> testClass, TestStructure testStructure, boolean isContinueOnTestFailureFromCPS, IResultArchiveStore ras, InterruptedMonitor interruptedMonitor , Log logger ) throws ConfigurationPropertyStoreException {
            super(testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , ras, interruptedMonitor , logger );
        }
        
        protected void runBeforeClassMethods( @NotNull ITestRunManagers managers ) throws TestRunException {
            // Do nothing.
        }

        protected void runAfterClassMethods( @NotNull ITestRunManagers managers ) throws TestRunException {
            // Do nothing.
        }

    };

    /**
     * In this test, the aim is to set a run up and tell it to run methods,
     * but on running the 2nd method in the class, it simulates a Cancel coming in
     * from the user.
     * At which point the third method should never be called.
     */
    @Test
    public void testRunStopsBetweenMethodsIfItsInterrupted() throws Exception {
        // Given...

        String testBundle = null;

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        String testRunName = "U12346";

        Class<?> testClass = TestClassWhichGetsInterrupted.class;
        TestStructure testStructure = new TestStructure();
        boolean isContinueOnTestFailureFromCPS = true ;

        MockLog logger = new MockLog();

        InterruptedMonitorImpl interruptedMonitor = new InterruptedMonitorImpl(dss,testRunName);
        // If asked, the interrupted monitor will say "we have been interrupted"
        
        TestWrapperWhichAllowsTestMethodsToRun wrapper = new TestWrapperWhichAllowsTestMethodsToRun(
            testBundle, testClass, testStructure, isContinueOnTestFailureFromCPS , new MockIResultArchiveStore() , interruptedMonitor, (Log)logger );

        MockTestRunManagers managers = new MockTestRunManagers(isContinueOnTestFailureFromCPS, null);

        String runName = null;
        
        // Initialize the testClass with our fake date...
        wrapper.parseTestClass();
        wrapper.instantiateTestClass();
        TestClassWhichGetsInterrupted testClassInstance = (TestClassWhichGetsInterrupted)wrapper.testClassObject;
        testClassInstance.init(dss, testRunName); 

        // When...
        wrapper.runMethods( managers, dss, runName);

        // Then...
        assertThat(testClassInstance.firstMethodCalledCounter).as("Expected the first test method to have been called").isEqualTo(1);
        assertThat(testClassInstance.thirdMethodCalledCounter).as("Expected the last method to be skipped as the test was simulated to be cancelled").isEqualTo(0);

        Result result = wrapper.getResult();
        assertThat(result.isCancelled()).isTrue();
    }

}
