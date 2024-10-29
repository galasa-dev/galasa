/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.elasticlog.manager.ivt;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;

@Test
public class ElasticLogManagerIVT {

    @Logger
    public Log logger;

    @Test
    public void test() {
    }
}