/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import dev.galasa.framework.spi.Result;
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

}