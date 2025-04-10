/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaMethod;

public interface ITestRunManagers {
    public boolean anyReasonTestClassShouldBeIgnored() throws FrameworkException ;
    public List<IManager> getActiveManagers();
    public void provisionGenerate() throws FrameworkException ;
    public void provisionBuild() throws FrameworkException ;

    /**
     * Tells the managers about the final end-result of the test. 
     * All other methods on the test have been called that are going to be called.
     * This method is called only once, after the @AfterClass methods.
     */
    public void testClassResult(@NotNull Result finalResult, Throwable finalException);

    public Result endOfTestClass(@NotNull Result result, Throwable currentException) throws FrameworkException ;

    public void endOfTestRun();
    public void provisionStart() throws FrameworkException;
    public void shutdown();
    public void startOfTestClass() throws FrameworkException;
    public void provisionDiscard();
    public void provisionStop();
    public Result anyReasonTestMethodShouldBeIgnored(@NotNull GalasaMethod galasaMethod) throws FrameworkException;
    public void fillAnnotatedFields(Object testClassObject) throws FrameworkException;
    public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws FrameworkException;
    public Result endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull Result currentResult, Throwable currentException) throws FrameworkException ;

    /**
     * The result has changed, it could change again. It could be set the the same thing multiple times.
     * This call is used to propogate the very latest overall test result state down to the managers, so they know the test state.
     * This can be used by the @TestResultProvider annotation for example, to maintain a 'current test result' which tests themselves, 
     * and @AfterClass methods can use.
     * 
     * @since 0.41.0
     */
    default void setResultSoFar(IResult newResult) {}
}