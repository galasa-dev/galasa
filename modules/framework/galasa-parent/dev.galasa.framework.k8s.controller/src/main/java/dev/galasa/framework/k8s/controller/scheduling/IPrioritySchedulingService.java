/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.scheduling;

import java.util.List;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IRun;

/**
 * A service that determines priority ordering for test runs that still need to be scheduled.
 */
public interface IPrioritySchedulingService {

    /**
     * Get a priority ordered list of test runs that have not been scheduled yet.
     * 
     * @return                    a list of test runs, ordered by priority
     * @throws FrameworkException if there was an issue accessing the framework
     */
    List<IRun> getPrioritisedTestRunsToSchedule() throws FrameworkException;
}
