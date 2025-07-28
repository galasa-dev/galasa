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
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockRASStoreService;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockTestRunManagers;
import dev.galasa.framework.mocks.MockTestRunnerDataProvider;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestMethodWrapperTest {

    private MockRASStoreService ras = new MockRASStoreService(null);

    class MockTestClass {
        public int beforeMethodCallCount = 0;
        public int testMethodCallCount = 0;
        public int afterMethodCallCount = 0;

        String testMethodRunLog = "";

        MockTestClass(List<String> runLogLines) {
            for (String runLogLine : runLogLines) {
                this.testMethodRunLog += runLogLine + "\n";
            }
        }

        public void MockBeforeMethod() throws Exception {
            ras.writeLog("This is the before method\n");
            beforeMethodCallCount++;
        }

        public void MockTestMethod() throws Exception {
            ras.writeLog(testMethodRunLog);
            testMethodCallCount++;
        }

        public void MockAfterMethod() throws Exception {
            ras.writeLog("This is the after method\n");
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

    private TestClassWrapper createTestClassWrapper() throws Exception {
        TestRunner testRunner = new TestRunner();

        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        
        MockTestRunnerDataProvider mockDataProvider = new MockTestRunnerDataProvider();
        mockDataProvider.setCps(cps);
        mockDataProvider.setDss(dss);
        mockDataProvider.setRun(new MockRun(null, null, null, null, null, null, null, false));

        MockFramework mockFramework = new MockFramework();
        mockFramework.setMockRas(ras);

        mockDataProvider.setFramework(mockFramework);

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
        TestClassWrapper testClassWrapper = createTestClassWrapper();

        boolean continueOnTestFailure = false;
        boolean ignoreTestClass = false;
        Result resultToReturn = Result.ignore("this method should be ignored");

        MockTestRunManagersExtended mockTestRunManagers = new MockTestRunManagersExtended(ignoreTestClass, resultToReturn);

        MockTestClass mockTestClass = new MockTestClass(new ArrayList<String>());

        // When...
        testMethodWrapper.initialiseTestMethodStructure();
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
        TestClassWrapper testClassWrapper = createTestClassWrapper();

        boolean continueOnTestFailure = false;
        boolean ignoreTestClass = false;
        Result ignoredResult = null;
        Result passedResult = Result.passed();

        MockTestRunManagersExtended mockTestRunManagers = new MockTestRunManagersExtended(ignoreTestClass, ignoredResult);
        mockTestRunManagers.setTestMethodResultToReturn(passedResult);

        MockTestClass mockTestClass = new MockTestClass(new ArrayList<String>());

        // When...
        testMethodWrapper.initialiseTestMethodStructure();
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
    public void testCheckForReasonToIgnoreTestMethodOnlyRunsOnce() throws Exception {
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
        TestClassWrapper testClassWrapper = createTestClassWrapper();

        boolean continueOnTestFailure = false;
        boolean ignoreTestClass = false;
        Result resultToReturn = Result.ignore("this method should be ignored");

        MockTestRunManagersExtended mockTestRunManagers = new MockTestRunManagersExtended(ignoreTestClass, resultToReturn);

        // When...
        testMethodWrapper.initialiseTestMethodStructure();
        testMethodWrapper.invoke(mockTestRunManagers, new MockTestClass(new ArrayList<String>()), continueOnTestFailure, testClassWrapper);

        // Then...
        List<GalasaMethod> galasaMethods = mockTestRunManagers.getGalasaMethodsReceived();
        assertThat(galasaMethods).hasSize(1);

        // Only the test method should have been passed in when checking whether to ignore it
        assertThat(galasaMethods.get(0).getJavaExecutionMethod()).isEqualTo(testMethod);
        assertThat(galasaMethods.get(0).getJavaTestMethod()).isNull();
    }

    @Test
    public void testInvokeSetsCorrectRunLogStartAndEndLines1TestMethodWithSingleLine() throws Exception {
        // Given...
        Class<?> mockClass = MockTestClass.class;

        List<String> runLogLinesForTestMethod = new ArrayList<>();
        runLogLinesForTestMethod.add("This is the test method");

        MockTestClass mockClassInstance = new MockTestClass(runLogLinesForTestMethod);
        Method testMethod = mockClass.getMethod("MockTestMethod");

        ArrayList<GenericMethodWrapper> beforeMethods = new ArrayList<>();

        ArrayList<GenericMethodWrapper> afterMethods = new ArrayList<>();

        TestMethodWrapper testMethodWrapper = new TestMethodWrapper(testMethod, mockClass, beforeMethods, afterMethods);
        testMethodWrapper.initialiseTestMethodStructure();

        ITestRunManagers mockTestRunManagers = new MockTestRunManagers(false, null);

        TestClassWrapper testClassWrapper = createTestClassWrapper();

        // When...
        testMethodWrapper.invoke(mockTestRunManagers, mockClassInstance, false, testClassWrapper);

        // Then...
        assertThat(testMethodWrapper.getTestStructureMethod().getRunLogStart()).isEqualTo(1);
        assertThat(testMethodWrapper.getTestStructureMethod().getRunLogEnd()).isEqualTo(1);
    }

    @Test
    public void testInvokeSetsCorrectRunLogStartAndEndLines1TestMethodWithMultiLine() throws Exception {
        // Given...
        Class<?> mockClass = MockTestClass.class;

        List<String> runLogLinesForTestMethod = new ArrayList<>();
        runLogLinesForTestMethod.add("This is the test method");
        runLogLinesForTestMethod.add("It's run log has multiple lines");
        runLogLinesForTestMethod.add("They need to be counted");

        MockTestClass mockClassInstance = new MockTestClass(runLogLinesForTestMethod);
        Method testMethod = mockClass.getMethod("MockTestMethod");

        ArrayList<GenericMethodWrapper> beforeMethods = new ArrayList<>();

        ArrayList<GenericMethodWrapper> afterMethods = new ArrayList<>();

        TestMethodWrapper testMethodWrapper = new TestMethodWrapper(testMethod, mockClass, beforeMethods, afterMethods);
        testMethodWrapper.initialiseTestMethodStructure();

        ITestRunManagers mockTestRunManagers = new MockTestRunManagers(false, null);

        TestClassWrapper testClassWrapper = createTestClassWrapper();

        // When...
        testMethodWrapper.invoke(mockTestRunManagers, mockClassInstance, false, testClassWrapper);

        // Then...
        assertThat(testMethodWrapper.getTestStructureMethod().getRunLogStart()).isEqualTo(1);
        assertThat(testMethodWrapper.getTestStructureMethod().getRunLogEnd()).isEqualTo(3);
    }

    @Test
    public void testInvokeSetsCorrectRunLogStartAndEndLines1TestMethodWithNoLines() throws Exception {
        // Given...
        Class<?> mockClass = MockTestClass.class;

        List<String> runLogLinesForTestMethod = new ArrayList<>();

        MockTestClass mockClassInstance = new MockTestClass(runLogLinesForTestMethod);
        Method testMethod = mockClass.getMethod("MockTestMethod");

        ArrayList<GenericMethodWrapper> beforeMethods = new ArrayList<>();

        ArrayList<GenericMethodWrapper> afterMethods = new ArrayList<>();

        TestMethodWrapper testMethodWrapper = new TestMethodWrapper(testMethod, mockClass, beforeMethods, afterMethods);
        testMethodWrapper.initialiseTestMethodStructure();

        ITestRunManagers mockTestRunManagers = new MockTestRunManagers(false, null);

        TestClassWrapper testClassWrapper = createTestClassWrapper();

        // When...
        testMethodWrapper.invoke(mockTestRunManagers, mockClassInstance, false, testClassWrapper);

        // Then...
        assertThat(testMethodWrapper.getTestStructureMethod().getRunLogStart()).isEqualTo(0);
        assertThat(testMethodWrapper.getTestStructureMethod().getRunLogEnd()).isEqualTo(0);
    }

    @Test
    public void testInvokeSetsCorrectRunLogStartAndEndLinesBeforeTestAndAfterMethod() throws Exception {
        // Given...
        Class<?> mockClass = MockTestClass.class;

        List<String> runLogLinesForTestMethod = new ArrayList<>();
        runLogLinesForTestMethod.add("This is a test method");

        MockTestClass mockClassInstance = new MockTestClass(runLogLinesForTestMethod);
        Method beforeMethod = mockClass.getMethod("MockBeforeMethod");
        Method testMethod = mockClass.getMethod("MockTestMethod");
        Method afterMethod = mockClass.getMethod("MockAfterMethod");

        ArrayList<GenericMethodWrapper> beforeMethods = new ArrayList<>();
        GenericMethodWrapper beforeMethodWrapper = new GenericMethodWrapper(beforeMethod, mockClass, Type.Before);
        beforeMethodWrapper.initialiseGenericMethodStructure();
        beforeMethods.add(beforeMethodWrapper);

        ArrayList<GenericMethodWrapper> afterMethods = new ArrayList<>();
        GenericMethodWrapper afterMethodWrapper = new GenericMethodWrapper(afterMethod, mockClass, Type.After);
        afterMethodWrapper.initialiseGenericMethodStructure();
        afterMethods.add(afterMethodWrapper);

        TestMethodWrapper testMethodWrapper = new TestMethodWrapper(testMethod, mockClass, beforeMethods, afterMethods);
        testMethodWrapper.initialiseTestMethodStructure();

        ITestRunManagers mockTestRunManagers = new MockTestRunManagers(false, null);

        TestClassWrapper testClassWrapper = createTestClassWrapper();

        // When...
        testMethodWrapper.invoke(mockTestRunManagers, mockClassInstance, false, testClassWrapper);

        // Then...
        assertThat(beforeMethodWrapper.getGenericMethodStructure().getRunLogStart()).isEqualTo(1);
        assertThat(beforeMethodWrapper.getGenericMethodStructure().getRunLogEnd()).isEqualTo(1);

        assertThat(testMethodWrapper.getTestStructureMethod().getRunLogStart()).isEqualTo(2);
        assertThat(testMethodWrapper.getTestStructureMethod().getRunLogEnd()).isEqualTo(2);

        assertThat(afterMethodWrapper.getGenericMethodStructure().getRunLogStart()).isEqualTo(3);
        assertThat(afterMethodWrapper.getGenericMethodStructure().getRunLogEnd()).isEqualTo(3);
    }

    @Test
    public void testInvokeSetsCorrectRunLogStartAndEndLinesBeforeTestAndAfterMethodTestMethodHasNoRunLog() throws Exception {
        // Given...
        Class<?> mockClass = MockTestClass.class;

        List<String> runLogLinesForTestMethod = new ArrayList<>();

        MockTestClass mockClassInstance = new MockTestClass(runLogLinesForTestMethod);
        Method beforeMethod = mockClass.getMethod("MockBeforeMethod");
        Method testMethod = mockClass.getMethod("MockTestMethod");
        Method afterMethod = mockClass.getMethod("MockAfterMethod");

        ArrayList<GenericMethodWrapper> beforeMethods = new ArrayList<>();
        GenericMethodWrapper beforeMethodWrapper = new GenericMethodWrapper(beforeMethod, mockClass, Type.Before);
        beforeMethodWrapper.initialiseGenericMethodStructure();
        beforeMethods.add(beforeMethodWrapper);

        ArrayList<GenericMethodWrapper> afterMethods = new ArrayList<>();
        GenericMethodWrapper afterMethodWrapper = new GenericMethodWrapper(afterMethod, mockClass, Type.After);
        afterMethodWrapper.initialiseGenericMethodStructure();
        afterMethods.add(afterMethodWrapper);

        TestMethodWrapper testMethodWrapper = new TestMethodWrapper(testMethod, mockClass, beforeMethods, afterMethods);
        testMethodWrapper.initialiseTestMethodStructure();

        ITestRunManagers mockTestRunManagers = new MockTestRunManagers(false, null);

        TestClassWrapper testClassWrapper = createTestClassWrapper();

        // When...
        testMethodWrapper.invoke(mockTestRunManagers, mockClassInstance, false, testClassWrapper);

        // Then...
        assertThat(beforeMethodWrapper.getGenericMethodStructure().getRunLogStart()).isEqualTo(1);
        assertThat(beforeMethodWrapper.getGenericMethodStructure().getRunLogEnd()).isEqualTo(1);

        assertThat(testMethodWrapper.getTestStructureMethod().getRunLogStart()).isEqualTo(0);
        assertThat(testMethodWrapper.getTestStructureMethod().getRunLogEnd()).isEqualTo(0);

        assertThat(afterMethodWrapper.getGenericMethodStructure().getRunLogStart()).isEqualTo(2);
        assertThat(afterMethodWrapper.getGenericMethodStructure().getRunLogEnd()).isEqualTo(2);
    }

}