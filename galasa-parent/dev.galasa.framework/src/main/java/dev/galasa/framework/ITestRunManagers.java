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
    public Result endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull Result currentResult, Throwable currentException)
    throws FrameworkException ;
}
