/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ivts.core;

import org.apache.commons.logging.Log;

import dev.galasa.Summary;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;

@Test
@Summary("A basic test with a sleep so there is sufficient time to test the `galasactl runs reset` command")
public class TestSleep {

    @Logger
    public Log logger;

    @Test
    public void sleep() throws Exception {
        logger.info("Sleeping for 1-minute.");
        Thread.sleep(60000);
    }

}