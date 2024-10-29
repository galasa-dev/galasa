/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.utils;

import java.time.Instant;

/**
 * An interface to allow the mocking of time-related method calls.
 */
public interface ITimeService {
    Instant now();

    void sleepMillis(long millisToSleep) throws InterruptedException ;
}
