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
import dev.galasa.Summary;
import dev.galasa.Test;
import dev.galasa.core.manager.ITestResultProvider;
import dev.galasa.core.manager.TestResultAnnotation;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.RunName;

@Test
@Summary("A basic test with a forced Failure to test that AfterClass calls the custom cleanup method")
public class TestAfterClass {

    @Logger
    public Log logger;

    @TestResultAnnotation
    public ITestResultProvider testResult;

    @Test
    public void testMethod() throws Exception {
        String result1 = this.testResult.getResult();
        logger.info("This is result1: " + result1);
        String status1 = this.testResult.getTestRun().getStatus();
        logger.info("This is heartbeat1: " + status1);
        // IFrameworkRuns frameworkRuns = this.testResult.getFramework().getFrameworkRuns();
        // List<IRun> runs = frameworkRuns.getAllRuns();
        // for (IRun run : runs) {
        //     logger.info(run.getName());
        // }

        Fail.fail("Forcing this test to fail to ensure the AfterClass method for failures runs");
    }

    @After
    public void after() throws FrameworkException {
        String result2 = this.testResult.getResult();
        logger.info("This is result2: " + result2);
        String status2 = this.testResult.getTestRun().getStatus();
        logger.info("This is heartbeat2: " + status2);
        // IFrameworkRuns frameworkRuns = this.testResult.getFramework().getFrameworkRuns();
        // List<IRun> runs = frameworkRuns.getAllRuns();
        // for (IRun run : runs) {
        //     logger.info(run.getName());
        // }
    }

    @AfterClass
    public void afterClassMethod() throws FrameworkException {
        // if (testResult.isFailed()) {
        //     customCleanUpMethod();
        // }
        String result3 = this.testResult.getResult();
        logger.info("This is result3: " + result3);
        String status3 = this.testResult.getTestRun().getStatus();
        logger.info("This is heartbeat3: " + status3);
        // IFrameworkRuns frameworkRuns = this.testResult.getFramework().getFrameworkRuns();
        // List<IRun> runs = frameworkRuns.getAllRuns();
        // for (IRun run : runs) {
        //     logger.info(run.getName());
        // }
    }

    private void customCleanUpMethod() {
        logger.info("This is the custom clean up method");
    }

}
