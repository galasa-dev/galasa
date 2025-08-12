/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import dev.galasa.framework.ITestMethodResult;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.TestMethodResult;
import dev.galasa.core.manager.ITestResultProvider;

public class CoreManagerImplTest {

    @Test
    public void testCanConstructAnInstance() {
        new CoreManagerImpl();
    }

    @Test
    public void testCanSeeATestFailureViaTheTestResultProvider() {
        // Given...
        CoreManagerImpl coreManager = new CoreManagerImpl();
        ITestResultProvider resultProvider = coreManager.createTestResultProvider(null,null);
        
        // When...
        coreManager.setResultSoFar(Result.failed("Simulating a failure"));

        // Then...
        assertThat( resultProvider.getResult().isFailed()).isTrue();
        assertThat( resultProvider.getResult().isPassed()).isFalse();
    }

    @Test
    public void testCanSeeATestPassViaTheTestResultProvider() {
        // Given...
        CoreManagerImpl coreManager = new CoreManagerImpl();
        ITestResultProvider resultProvider = coreManager.createTestResultProvider(null,null);
        
        // When...
        coreManager.setResultSoFar(Result.passed());

        // Then...
        assertThat( resultProvider.getResult().isPassed()).isTrue();
        assertThat( resultProvider.getResult().isFailed()).isFalse();
    }


    @Test
    public void testCanSeeATestNeitherFailedNorPassedViaTheTestResultProvider() {
        // Given...
        CoreManagerImpl coreManager = new CoreManagerImpl();
        ITestResultProvider resultProvider = coreManager.createTestResultProvider(null,null);
        
        // When...
        // WE DON'T INJECT A RESULT

        // Then...
        assertThat( resultProvider.getResult().isPassed()).isFalse();
        assertThat( resultProvider.getResult().isFailed()).isFalse();
    }

    @Test
    public void testTestMethodResultsViaTheTestResultProviderEmptyIfNoTestMethodsRanYet() {
        // Given...
        CoreManagerImpl coreManager = new CoreManagerImpl();
        ITestResultProvider resultProvider = coreManager.createTestResultProvider(null,null);

        // When...
        // No test methods have ran yet...

        // Then...
        assertThat(resultProvider.getTestMethodResults()).isEmpty();
    }

    @Test
    public void testCanSeeAllTestMethodResultsViaTheTestResultProvider() {
        // Given...
        CoreManagerImpl coreManager = new CoreManagerImpl();
        ITestResultProvider resultProvider = coreManager.createTestResultProvider(null,null);

        ITestMethodResult beforeMethodResult = new TestMethodResult("beforeMethod", true, false, null);
        ITestMethodResult testMethodResult = new TestMethodResult("testMethod", false, true, new Exception());
        ITestMethodResult afterClassMethod = new TestMethodResult("afterClassMethod", true, false, null);

        // When...
        coreManager.addTestMethodResult(beforeMethodResult);
        coreManager.addTestMethodResult(testMethodResult);
        coreManager.addTestMethodResult(afterClassMethod);

        // Then...
        assertThat(resultProvider.getTestMethodResults().size()).isEqualTo(3);

        assertThat(resultProvider.getTestMethodResults().get(0).getMethodName()).isEqualTo("beforeMethod");
        assertThat(resultProvider.getTestMethodResults().get(0).isPassed()).isEqualTo(true);
        assertThat(resultProvider.getTestMethodResults().get(0).isFailed()).isEqualTo(false);
        assertThat(resultProvider.getTestMethodResults().get(0).getThrowable()).isNull();

        assertThat(resultProvider.getTestMethodResults().get(1).getMethodName()).isEqualTo("testMethod");
        assertThat(resultProvider.getTestMethodResults().get(1).isPassed()).isEqualTo(false);
        assertThat(resultProvider.getTestMethodResults().get(1).isFailed()).isEqualTo(true);
        assertThat(resultProvider.getTestMethodResults().get(1).getThrowable()).isInstanceOf(Exception.class);

        assertThat(resultProvider.getTestMethodResults().get(2).getMethodName()).isEqualTo("afterClassMethod");
        assertThat(resultProvider.getTestMethodResults().get(2).isPassed()).isEqualTo(true);
        assertThat(resultProvider.getTestMethodResults().get(2).isFailed()).isEqualTo(false);
        assertThat(resultProvider.getTestMethodResults().get(2).getThrowable()).isNull();
    }

    @Test
    public void testCanGetLastTestMethodResultViaTheTestResultProvider() {
        // Given...
        CoreManagerImpl coreManager = new CoreManagerImpl();
        ITestResultProvider resultProvider = coreManager.createTestResultProvider(null,null);

        ITestMethodResult beforeMethodResult = new TestMethodResult("beforeMethod", true, false, null);
        ITestMethodResult testMethodResult = new TestMethodResult("testMethod", false, true, new Exception());
        ITestMethodResult afterClassMethod = new TestMethodResult("afterClassMethod", true, false, null);

        // When...
        coreManager.addTestMethodResult(beforeMethodResult);
        coreManager.addTestMethodResult(testMethodResult);
        coreManager.addTestMethodResult(afterClassMethod);

        // Then...
        assertThat(resultProvider.getTestMethodResults().get(resultProvider.getTestMethodResults().size() - 1)).isEqualTo(afterClassMethod);
    }

}