/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.ITestRunManagers;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaMethod;

public class MockTestRunManagers implements ITestRunManagers {

    private boolean ignoreTestClass ;

    public int calledCountEndOfTestRun = 0 ;
    public int calledCountShudown = 0;
    public int calledCountProvisionGenerate = 0;
    public int calledCountProvisionBuild = 0;
    public int calledCountProvisionStart = 0 ;
    public int calledCountProvisionDiscard = 0 ;
    public int calledCountProvisionStop = 0 ;
    public int calledCountStartOfTestClass = 0 ;
    public int calledCountEndOfTestClass = 0 ;
    public int calledCountTestClassResult = 0 ;
    public int calledCountAnyReasonTestMethodShouldBeIgnored = 0 ;

    private Result resultToReturn;
    private Result testMethodResultToReturn;

    public MockTestRunManagers( boolean ignoreTestClass , Result resultToReturn ) {
        this.ignoreTestClass = ignoreTestClass ;
        this.resultToReturn = resultToReturn;
    }

    public void setTestMethodResultToReturn(Result testMethodResultToReturn) {
        this.testMethodResultToReturn = testMethodResultToReturn;
    }

    @Override
    public boolean anyReasonTestClassShouldBeIgnored() throws FrameworkException {
        return ignoreTestClass;
    }

    @Override
    public void endOfTestRun() {
        calledCountEndOfTestRun+=1;
    }

    @Override
    public void shutdown() {
        calledCountShudown +=1;
    }

    @Override
    public void provisionGenerate() throws FrameworkException {
        calledCountProvisionGenerate +=1;
    }

    @Override
    public void provisionBuild() throws FrameworkException {
        calledCountProvisionBuild +=1 ;
    }

    @Override
    public void provisionStart() throws FrameworkException {
        calledCountProvisionStart +=1;
    }

    @Override
    public void provisionDiscard() {
        calledCountProvisionDiscard+=1;
    }

    @Override
    public void provisionStop() {
        calledCountProvisionStop +=1;
    }

    @Override
    public void startOfTestClass() throws FrameworkException {
        calledCountStartOfTestClass +=1;
    }

    @Override
    public Result endOfTestClass(@NotNull Result result, Throwable currentException) throws FrameworkException {
        calledCountEndOfTestClass +=1;
        return resultToReturn;
    }

    @Override
    public void testClassResult(@NotNull Result finalResult, Throwable finalException) {
        calledCountTestClassResult +=1;
    }

    @Override
    public Result anyReasonTestMethodShouldBeIgnored(@NotNull GalasaMethod galasaMethod) throws FrameworkException {
        calledCountAnyReasonTestMethodShouldBeIgnored +=1;
        return this.resultToReturn;
    }

    @Override
    public void fillAnnotatedFields(Object testClassObject) throws FrameworkException {
        // Do nothing...
    }

    @Override
    public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws FrameworkException {
        // Do nothing...
    }

    @Override
    public Result endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull Result currentResult,
            Throwable currentException) throws FrameworkException {
        return this.testMethodResultToReturn;
    }

    // ----------------- un-implemented methods follow -------------------

    @Override
    public List<IManager> getActiveManagers() {
        throw new UnsupportedOperationException("Unimplemented method 'getActiveManagers'");
    }
}
