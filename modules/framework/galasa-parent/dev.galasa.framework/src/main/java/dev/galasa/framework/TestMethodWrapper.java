/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.teststructure.TestMethod;

public class TestMethodWrapper {
    
    private final Log                        logger;

    private final List<GenericMethodWrapper> befores  = new ArrayList<>();
    private GenericMethodWrapper             testMethod;
    private final List<GenericMethodWrapper> afters   = new ArrayList<>();

    private Result                           result;
    private boolean                          fullStop = false;

    private TestMethod                       testMethodStructure;

    protected TestMethodWrapper(
        Method testMethod, 
        Class<?> testClass, 
        ArrayList<GenericMethodWrapper> beforeMethods,
        ArrayList<GenericMethodWrapper> afterMethods
        ) {

        this.logger = LogFactory.getLog(TestMethodWrapper.class);

        this.testMethod = new GenericMethodWrapper(testMethod, testClass, GenericMethodWrapper.Type.Test);

        for (GenericMethodWrapper before : beforeMethods) {
            // TODO, check the before can be run, before adding to list
            this.befores.add(before);
        }

        for (GenericMethodWrapper after : afterMethods) {
            // TODO, check the after can be run, before adding to list
            this.afters.add(after);
        }

        return;
    }

    public void invoke(
        @NotNull ITestRunManagers managers, 
        Object testClassObject, 
        boolean continueOnTestFailure, 
        TestClassWrapper testClassWrapper
        ) throws TestRunException {
        try {
            // Check if the test method should be ignored, and if so, mark it as ignored along
            // with any @Before and @After methods associated with the test method
            Method executionMethod = testMethod.getExecutionMethod();
            Result ignoredResult = managers.anyReasonTestMethodShouldBeIgnored(new GalasaMethod(executionMethod, null));
            if (ignoredResult != null) {
                logger.info(TestClassWrapper.LOG_STARTING + TestClassWrapper.LOG_START_LINE + TestClassWrapper.LOG_ASTERS
                        + TestClassWrapper.LOG_START_LINE + "*** Ignoring test method " + testClassObject.getClass().getName() + "#"
                        + testMethod.getName() + ",type=" + testMethod.getType().toString() + TestClassWrapper.LOG_START_LINE
                        + TestClassWrapper.LOG_ASTERS);
                logger.info("Ignoring " + executionMethod.getName() + " due to " + ignoredResult.getReason());

                markTestAndLinkedMethodsIgnored(ignoredResult, testClassWrapper, managers);
            } else {

                runBeforeMethods(managers, testClassObject, testClassWrapper);

                if (this.result == null) {
                    runTestMethod(managers, testClassObject, testClassWrapper, continueOnTestFailure);
                }

                runAfterMethods(managers, testClassObject, testClassWrapper);

            }
        } catch (FrameworkException ex) {
            throw new TestRunException("Failure occurred when invoking methods in the given test class", ex);
        }
    }

    protected void runBeforeMethods(ITestRunManagers managers, Object testClassObject, TestClassWrapper testClassWrapper) throws TestRunException {
        // run all the @Befores before the test method
        for (GenericMethodWrapper before : this.befores) {
            before.invoke(managers, testClassObject, testMethod, testClassWrapper);
            testClassWrapper.setResult(before.getResult(), managers);
            if (before.getResult().isFullStop()) {
                this.fullStop = true;
                this.result = Result.failed("Before method failed");
                break;
            }
        }
    }

    protected void runAfterMethods(ITestRunManagers managers, Object testClassObject, TestClassWrapper testClassWrapper) throws TestRunException {
        // run all the @Afters after the test method
        Result afterResult = null;
        for (GenericMethodWrapper after : this.afters) {
            after.invoke(managers, testClassObject, testMethod, testClassWrapper);
            testClassWrapper.setResult(after.getResult(), managers);
            if (after.fullStop()) {
                this.fullStop = true;
                if (afterResult == null) {
                    afterResult = Result.failed("After method failed");
                    if (this.result == null || this.result.isPassed()) {
                        this.result = afterResult;
                    }
                }
            }
        }
    }

    protected void runTestMethod(ITestRunManagers managers, Object testClassObject, TestClassWrapper testClassWrapper, boolean continueOnTestFailure) throws TestRunException {
        testMethod.invoke(managers, testClassObject, null, testClassWrapper);
        testClassWrapper.setResult(testMethod.getResult(), managers);
        if (this.testMethod.fullStop()) {
            if (continueOnTestFailure) {
                logger.warn("Test method failed, however, continue on test failure was requested, so carrying on");
            } else {
                this.fullStop = this.testMethod.fullStop();
            }
        }
        
        this.result = this.testMethod.getResult();
    }

    /**
     * This creates a new test structure for this @Test method, priming
     * it with the @Before and @After methods that belong to it. It is
     * then set as a class variable. It can be retrieved with getTestStructureMethod().
     */
    public void initialiseTestMethodStructure() {

        testMethod.initialiseGenericMethodStructure();
        this.testMethodStructure = testMethod.getGenericMethodStructure();

        ArrayList<TestMethod> structureBefores = new ArrayList<>();
        ArrayList<TestMethod> structureAfters = new ArrayList<>();

        for (GenericMethodWrapper before : this.befores) {
            before.initialiseGenericMethodStructure();
            TestMethod beforeTestMethodStructure = before.getGenericMethodStructure();
            structureBefores.add(beforeTestMethodStructure);
        }

        for (GenericMethodWrapper after : this.afters) {
            after.initialiseGenericMethodStructure();
            TestMethod afterTestMethodStructure = after.getGenericMethodStructure();
            structureAfters.add(afterTestMethodStructure);
        }

        this.testMethodStructure.setBefores(structureBefores);
        this.testMethodStructure.setAfters(structureAfters);
    }

    /**
     * This returns the test structure for this @Test method.
     * @return the existing TestMethod structure for this @Test method.
     */
    public TestMethod getTestStructureMethod() {
        return this.testMethodStructure;
    }


    public boolean fullStop() {
        return this.fullStop;
    }

    public Result getResult() {
        return this.result;
    }

    public String getName() {
        return this.testMethod.getName();
    }

    private void markTestAndLinkedMethodsIgnored(Result ignoredResult, TestClassWrapper testClassWrapper, ITestRunManagers managers) {
        // Mark test method as ignored
        testMethod.setResult(ignoredResult);
        testClassWrapper.setResult(testMethod.getResult(), managers);
        this.result = ignoredResult;

        // Mark any before methods associated with the test method as ignored
        for (GenericMethodWrapper before : this.befores) {
            before.setResult(ignoredResult);
            testClassWrapper.setResult(before.getResult(), managers);
        }

        // Mark any after methods associated with the test method as ignored
        for (GenericMethodWrapper after : this.afters) {
            after.setResult(ignoredResult);
            testClassWrapper.setResult(after.getResult(), managers);
        }
    }
}