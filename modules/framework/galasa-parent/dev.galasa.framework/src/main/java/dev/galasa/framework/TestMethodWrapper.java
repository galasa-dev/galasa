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
    
    private Log                              logger        = LogFactory.getLog(TestMethodWrapper.class);

    private final List<GenericMethodWrapper> befores  = new ArrayList<>();
    private GenericMethodWrapper             testMethod;
    private final List<GenericMethodWrapper> afters   = new ArrayList<>();

    private Result                           result;
    private boolean                          fullStop = false;

    protected TestMethodWrapper(Method testMethod, Class<?> testClass, ArrayList<GenericMethodWrapper> beforeMethods,
            ArrayList<GenericMethodWrapper> afterMethods) {

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

    public void invoke(@NotNull ITestRunManagers managers, Object testClassObject, boolean continueOnTestFailure, TestClassWrapper testClassWrapper) throws TestRunException {
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
                // run all the @Befores before the test method
                for (GenericMethodWrapper before : this.befores) {
                    before.invoke(managers, testClassObject, testMethod);
                    testClassWrapper.setResult(before.getResult(), managers);
                    if (before.getResult().isFullStop()) {
                        this.fullStop = true;
                        this.result = Result.failed("Before method failed");
                        break;
                    }
                }

                if (this.result == null) {
                    testMethod.invoke(managers, testClassObject, null);
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

                // run all the @Afters after the test method
                Result afterResult = null;
                for (GenericMethodWrapper after : this.afters) {
                    after.invoke(managers, testClassObject, testMethod);
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
        } catch (FrameworkException ex) {
            throw new TestRunException("Failure occurred when invoking methods in the given test class", ex);
        }
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
    
    public TestMethod getStructure() {
        TestMethod methodStructure = testMethod.getStructure();
        ArrayList<TestMethod> structureBefores = new ArrayList<>();
        ArrayList<TestMethod> structureAfters = new ArrayList<>();

        methodStructure.setBefores(structureBefores);
        methodStructure.setAfters(structureAfters);

        for (GenericMethodWrapper before : this.befores) {
            structureBefores.add(before.getStructure());
        }

        for (GenericMethodWrapper after : this.afters) {
            structureAfters.add(after.getStructure());
        }

        return methodStructure;
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