/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ivts.core;

import java.time.Instant;

import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;

import org.assertj.core.api.Fail;

import dev.galasa.After;
import dev.galasa.AfterClass;
import dev.galasa.ContinueOnTestFailure;
import dev.galasa.Summary;
import dev.galasa.Test;
import dev.galasa.core.manager.ITestResultProvider;
import dev.galasa.core.manager.TestResultProvider;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.core.manager.Logger;

@Test
@ContinueOnTestFailure
@Summary("A basic test with a forced Failure to test that AfterClass calls the custom cleanup method")
public class TestTestResultProvider {

    @Logger
    public Log logger;

    @TestResultProvider
    public ITestResultProvider testResult;

    @Test
    public void testMethod() throws Exception {
        Fail.fail("Forcing this test to Fail to test if the AfterClass method calls the custom cleanup method that is only called for Failures");
    }

    @AfterClass
    public void afterClassMethod() throws FrameworkException {
        if (testResult.getResult().isFailed()) {
            customCleanUpMethod();
        }
        
    }

    private void customCleanUpMethod() {
        logger.info("This is the custom clean up method that we expected to be called - YAY!");
    }

}
