/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ivts.core;

import org.apache.commons.logging.Log;

import org.assertj.core.api.Fail;

import dev.galasa.AfterClass;
import dev.galasa.Summary;
import dev.galasa.Test;
import dev.galasa.TestStatus;
import dev.galasa.TestStatusAnnotation;
import dev.galasa.core.manager.Logger;

@Test
@Summary("A basic test with a forced Failure to test that the custom AfterClass method is ran")
public class TestAfterClass {

    @Logger
    public Log logger;

    @TestStatusAnnotation
    TestStatus testStatus;

    @Test
    public void fail() throws Exception {
        Fail.fail("Forcing this test to fail to ensure the AfterClass method for failures runs");
    }

    @AfterClass
    public void afterClassMethod() {
        if (testStatus.isFailed()) {
            customCleanUpMethod();
        }
    }

    private void customCleanUpMethod() {
        logger.info("This is the custom clean up method");
    }

}
