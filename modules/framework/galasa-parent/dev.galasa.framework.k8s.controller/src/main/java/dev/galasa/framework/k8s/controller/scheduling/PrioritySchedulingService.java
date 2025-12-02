/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.scheduling;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.utils.ITimeService;

/**
 * An implementation of a priority scheduling service.
 * 
 * This service associates each queued test run with a number of priority points
 * which is calculated using a set of criteria, including:
 * 
 * - The number of minutes that have passed since the test run was queued
 * 
 * The queued test runs are then ordered so that the test run with the most priority 
 * points comes first and the one with the fewest points comes last.
 */
public class PrioritySchedulingService implements IPrioritySchedulingService {

    private static final long DEFAULT_TEST_RUN_PRIORITY_POINTS_GROWTH_RATE_PER_MIN = 1;
    private final Log logger = LogFactory.getLog(getClass());

    private IFrameworkRuns frameworkRuns;
    private IConfigurationPropertyStoreService cps;
    private ITimeService timeService;
    
    public PrioritySchedulingService(IFrameworkRuns frameworkRuns, IConfigurationPropertyStoreService cps, ITimeService timeService) {
        this.frameworkRuns = frameworkRuns;
        this.cps = cps;
        this.timeService = timeService;
    }

    @Override
    public List<IRun> getPrioritisedTestRunsToSchedule() throws FrameworkException {
        List<IRun> queuedRuns = getQueuedRemoteRuns();
        queuedRuns.sort(getPriorityComparator());
        return queuedRuns;
    }

    private Comparator<IRun> getPriorityComparator() {
        return (a, b) -> Long.compare(getQueuedRunPriority(b), getQueuedRunPriority(a));
    }

    private List<IRun> getQueuedRemoteRuns() throws FrameworkException {
        List<IRun> queuedRuns = this.frameworkRuns.getQueuedRuns();

        Iterator<IRun> queuedRunsIterator = queuedRuns.iterator();
        while (queuedRunsIterator.hasNext()) {
            IRun run = queuedRunsIterator.next();
            if (run.isLocal() || run.getInterruptReason() != null) {
                queuedRunsIterator.remove();
            }
        }
        return queuedRuns;
    }

    private long getQueuedRunPriority(IRun run) {
        Instant queuedTime = run.getQueued();
        long priorityGrowthRatePerMin = getPriorityGrowthRatePerMin();
        Instant currentTime = timeService.now();

        long minutesElapsedSinceQueued = Duration.between(queuedTime, currentTime).toMinutes();
        return minutesElapsedSinceQueued * priorityGrowthRatePerMin;
    }

    private long getPriorityGrowthRatePerMin() {
        long priorityGrowthRatePerMin = DEFAULT_TEST_RUN_PRIORITY_POINTS_GROWTH_RATE_PER_MIN;

        try {
            String priorityGrowthRateStr = cps.getProperty("runs.priority.points", "growth.per.min");
            if (priorityGrowthRateStr != null && !priorityGrowthRateStr.isBlank()) {
                priorityGrowthRatePerMin = Long.parseLong(priorityGrowthRateStr);
            }
        } catch (Exception e) {
            logger.info("Could not get framework.test.runs.priority.growth.rate.per.min CPS property, using default: " + DEFAULT_TEST_RUN_PRIORITY_POINTS_GROWTH_RATE_PER_MIN);
        }
        return priorityGrowthRatePerMin;
    }
}
