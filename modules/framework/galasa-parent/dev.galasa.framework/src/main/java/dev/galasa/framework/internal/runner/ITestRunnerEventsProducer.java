/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.TestRunLifecycleStatus;

/**
 * Something which publishes information about important events in the system.
 */
public interface ITestRunnerEventsProducer {

    boolean isEnabled();

    void produceTestHeartbeatStoppedEvent(String testRunName) throws TestRunException ;

    void produceTestRunLifecycleStatusChangedEvent(String testRunName, TestRunLifecycleStatus status) throws TestRunException ;
}
