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
import dev.galasa.Before;
import dev.galasa.BeforeClass;
import dev.galasa.ContinueOnTestFailure;
import dev.galasa.Summary;
import dev.galasa.Test;
import dev.galasa.core.manager.ITestResultProvider;
import dev.galasa.core.manager.TestResultProvider;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.core.manager.Logger;

// Commenting out this @Test annotation so this test doesn't run as part of the regression test suite.
// Can be uncommented and ran locally to test any changes to the TestResultProvider.
// @Test
@ContinueOnTestFailure
@Summary("A basic test with a forced Failure to test that AfterClass calls the custom cleanup method")
public class TestTestResultProvider {

    @Logger
    public Log logger;

    @TestResultProvider
    public ITestResultProvider testResult;

    @BeforeClass
    public void beforeClassMethod() {
        logger.info("In the beforeClassMethod - is the test currently Passed? " + testResult.getResult().isPassed());
        logger.info("In the beforeClassMethod - is the test currently Failed? " + testResult.getResult().isFailed());
    }

    @Before
    public void beforeMethod() {
        logger.info("In the beforeMethod - is the test currently Passed? " + testResult.getResult().isPassed());
        logger.info("In the beforeMethod - is the test currently Failed? " + testResult.getResult().isFailed());
    }

    @Test
    public void testMethod1() {
        logger.info("In the testMethod1 - is the test currently Passed? " + testResult.getResult().isPassed());
        logger.info("In the testMethod1 - is the test currently Failed? " + testResult.getResult().isFailed());
    }

    @Test
    public void testMethod2() throws Exception {
        logger.info("In the testMethod2 - is the test currently Passed? " + testResult.getResult().isPassed());
        logger.info("In the testMethod2 - is the test currently Failed? " + testResult.getResult().isFailed());
        logger.info("About to force a Failure...");
        Fail.fail("Forcing this test to Fail to test if the AfterClass method calls the custom cleanup method that is only called for Failures");
    }

    @Test
    public void testMethod3() throws Exception {
        logger.info("In the testMethod3 - is the test currently Passed? " + testResult.getResult().isPassed());
        logger.info("In the testMethod3 - is the test currently Failed? " + testResult.getResult().isFailed());
    }

    @After
    public void afterMethod() {
        logger.info("In the afterMethod - is the test currently Passed? " + testResult.getResult().isPassed());
        logger.info("In the afterMethod - is the test currently Failed? " + testResult.getResult().isFailed());
    }

    @AfterClass
    public void afterClassMethod() throws FrameworkException {
        logger.info("In the afterClassMethod - is the test currently Passed? " + testResult.getResult().isPassed());
        logger.info("In the afterClassMethod - is the test currently Failed? " + testResult.getResult().isFailed());
        if (testResult.getResult().isFailed()) {
            customCleanUpMethod();
        }
    }

    private void customCleanUpMethod() {
        logger.info("This is the custom clean up method that we expected to be called - YAY!");
    }

}