/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.junit.Test;

import dev.galasa.framework.GenericMethodWrapper.Type;
import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockTestRunManagers;
import dev.galasa.framework.mocks.MockTestRunnerDataProvider;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestMethodWrapperTest {

    class MockTestClass {
        public int beforeMethodCallCount = 0;
        public int testMethodCallCount = 0;
        public int afterMethodCallCount = 0;

        public void MockBeforeMethod() {
            beforeMethodCallCount++;
        }

        public void MockTestMethod() {
            testMethodCallCount++;
        }

        public void MockAfterMethod() {
            afterMethodCallCount++;
        }
    }

    class MockTestRunManagersExtended extends MockTestRunManagers {

        private List<GalasaMethod> galasaMethodsReceived = new ArrayList<>();

        public MockTestRunManagersExtended(boolean ignoreTestClass, Result resultToReturn) {
            super(ignoreTestClass, resultToReturn);
        }

        @Override
        public Result anyReasonTestMethodShouldBeIgnored(@NotNull GalasaMethod galasaMethod) throws FrameworkException {
            galasaMethodsReceived.add(galasaMethod);
            return super.anyReasonTestMethodShouldBeIgnored(galasaMethod);
        }

        public List<GalasaMethod> getGalasaMethodsReceived() {
            return this.galasaMethodsReceived;
        }

    }

    private TestClassWrapper creatTestClassWrapper() throws Exception {
        TestRunner testRunner = new TestRunner();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(new MockRun(null, null, null, null, null, null, null, false));

        testRunner.init(mockDataProvider);

        TestStructure testStructure = new TestStructure();

        String testBundle = "my/testbundle";
        TestClassWrapper testClassWrapper = new TestClassWrapper(testRunner, testBundle, MockTestClass.class, testStructure);
        return testClassWrapper;
    }

    @Test
    public void testIgnoredMethodsAreNotInvoked() throws Exception {
        // Given...
        Class<?> mockClass = MockTestClass.class;
        Method beforeMethod = mockClass.getMethod("MockBeforeMethod");
        Method testMethod = mockClass.getMethod("MockTestMethod");
        Method afterMethod = mockClass.getMethod("MockAfterMethod");

        ArrayList<GenericMethodWrapper> beforeMethods = new ArrayList<>();

        GenericMethodWrapper beforeMethodWrapper = new GenericMethodWrapper(beforeMethod, mockClass, Type.Before);
        beforeMethods.add(beforeMethodWrapper);
        
        ArrayList<GenericMethodWrapper> afterMethods = new ArrayList<>();
        GenericMethodWrapper afterMethodWrapper = new GenericMethodWrapper(afterMethod, mockClass, Type.After);
        afterMethods.add(afterMethodWrapper);

        TestMethodWrapper testMethodWrapper = new TestMethodWrapper(testMethod, MockTestClass.class, beforeMethods, afterMethods);
        TestClassWrapper testClassWrapper = creatTestClassWrapper();

        boolean continueOnTestFailure = false;
        boolean ignoreTestClass = false;
        Result resultToReturn = Result.ignore("this method should be ignored");

        MockTestRunManagersExtended mockTestRunManagers = new MockTestRunManagersExtended(ignoreTestClass, resultToReturn);

        MockTestClass mockTestClass = new MockTestClass();

        // When...
        testMethodWrapper.getStructure();
        testMethodWrapper.invoke(mockTestRunManagers, mockTestClass, continueOnTestFailure, testClassWrapper);

        // Then...
        assertThat(beforeMethodWrapper.getResult().getName()).isEqualTo("Ignored");
        assertThat(afterMethodWrapper.getResult().getName()).isEqualTo("Ignored");
        assertThat(testMethodWrapper.getResult().getName()).isEqualTo("Ignored");

        assertThat(mockTestClass.beforeMethodCallCount).isEqualTo(0);
        assertThat(mockTestClass.testMethodCallCount).isEqualTo(0);
        assertThat(mockTestClass.afterMethodCallCount).isEqualTo(0);
    }

    @Test
    public void testMethodsAreInvokedWhenNotIgnored() throws Exception {
        // Given...
        Class<?> mockClass = MockTestClass.class;
        Method beforeMethod = mockClass.getMethod("MockBeforeMethod");
        Method testMethod = mockClass.getMethod("MockTestMethod");
        Method afterMethod = mockClass.getMethod("MockAfterMethod");

        ArrayList<GenericMethodWrapper> beforeMethods = new ArrayList<>();

        GenericMethodWrapper beforeMethodWrapper = new GenericMethodWrapper(beforeMethod, mockClass, Type.Before);
        beforeMethods.add(beforeMethodWrapper);
        
        ArrayList<GenericMethodWrapper> afterMethods = new ArrayList<>();
        GenericMethodWrapper afterMethodWrapper = new GenericMethodWrapper(afterMethod, mockClass, Type.After);
        afterMethods.add(afterMethodWrapper);

        TestMethodWrapper testMethodWrapper = new TestMethodWrapper(testMethod, MockTestClass.class, beforeMethods, afterMethods);
        TestClassWrapper testClassWrapper = creatTestClassWrapper();

        boolean continueOnTestFailure = false;
        boolean ignoreTestClass = false;
        Result ignoredResult = null;
        Result passedResult = Result.passed();

        MockTestRunManagersExtended mockTestRunManagers = new MockTestRunManagersExtended(ignoreTestClass, ignoredResult);
        mockTestRunManagers.setTestMethodResultToReturn(passedResult);

        MockTestClass mockTestClass = new MockTestClass();

        // When...
        testMethodWrapper.getStructure();
        testMethodWrapper.invoke(mockTestRunManagers, mockTestClass, continueOnTestFailure, testClassWrapper);

        // Then...
        String passedResultStr = passedResult.getName();
        assertThat(beforeMethodWrapper.getResult().getName()).isEqualTo(passedResultStr);
        assertThat(afterMethodWrapper.getResult().getName()).isEqualTo(passedResultStr);
        assertThat(testMethodWrapper.getResult().getName()).isEqualTo(passedResultStr);

        assertThat(mockTestClass.beforeMethodCallCount).isEqualTo(1);
        assertThat(mockTestClass.testMethodCallCount).isEqualTo(1);
        assertThat(mockTestClass.afterMethodCallCount).isEqualTo(1);
    }

    @Test
    public void testBeforeAndAfterMethodsAreAssociatedWithTestMethod() throws Exception {
        // Given...
        Class<?> mockClass = MockTestClass.class;
        Method beforeMethod = mockClass.getMethod("MockBeforeMethod");
        Method testMethod = mockClass.getMethod("MockTestMethod");
        Method afterMethod = mockClass.getMethod("MockAfterMethod");

        ArrayList<GenericMethodWrapper> beforeMethods = new ArrayList<>();

        GenericMethodWrapper beforeMethodWrapper = new GenericMethodWrapper(beforeMethod, mockClass, Type.Before);
        beforeMethods.add(beforeMethodWrapper);
        
        ArrayList<GenericMethodWrapper> afterMethods = new ArrayList<>();
        GenericMethodWrapper afterMethodWrapper = new GenericMethodWrapper(afterMethod, mockClass, Type.After);
        afterMethods.add(afterMethodWrapper);

        TestMethodWrapper testMethodWrapper = new TestMethodWrapper(testMethod, MockTestClass.class, beforeMethods, afterMethods);
        TestClassWrapper testClassWrapper = creatTestClassWrapper();

        boolean continueOnTestFailure = false;
        boolean ignoreTestClass = false;
        Result resultToReturn = Result.ignore("this method should be ignored");

        MockTestRunManagersExtended mockTestRunManagers = new MockTestRunManagersExtended(ignoreTestClass, resultToReturn);

        // When...
        testMethodWrapper.getStructure();
        testMethodWrapper.invoke(mockTestRunManagers, new MockTestClass(), continueOnTestFailure, testClassWrapper);

        // Then...
        List<GalasaMethod> galasaMethods = mockTestRunManagers.getGalasaMethodsReceived();
        assertThat(galasaMethods).hasSize(3);

        // The before should have an execution method and a test method associated with it
        assertThat(galasaMethods.get(0).getJavaExecutionMethod()).isEqualTo(beforeMethod);
        assertThat(galasaMethods.get(0).getJavaTestMethod()).isEqualTo(testMethod);

        // The test method should only have an execution method
        assertThat(galasaMethods.get(1).getJavaExecutionMethod()).isEqualTo(testMethod);
        assertThat(galasaMethods.get(1).getJavaTestMethod()).isNull();

        // The before should have an execution method and a test method associated with it
        assertThat(galasaMethods.get(2).getJavaExecutionMethod()).isEqualTo(afterMethod);
        assertThat(galasaMethods.get(2).getJavaTestMethod()).isEqualTo(testMethod);
    }
}
