/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.scheduling;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.tags.ITagsService;
import dev.galasa.framework.spi.tags.Tag;
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

    public static final double DEFAULT_TEST_RUN_PRIORITY_POINTS_GROWTH_RATE_PER_MIN = 1;

    private static final String RUNS_PRIORITY_GROWTH_RATE_CPS_PROPERTY_PREFIX = "runs.priority";
    private static final String RUNS_PRIORITY_GROWTH_RATE_CPS_PROPERTY_SUFFIX = "growth.rate.per.min";
    private static final String RUNS_PRIORITY_GROWTH_RATE_CPS_PROPERTY_KEY =
        "framework." + RUNS_PRIORITY_GROWTH_RATE_CPS_PROPERTY_PREFIX + "." + RUNS_PRIORITY_GROWTH_RATE_CPS_PROPERTY_SUFFIX;

    private final Log logger = LogFactory.getLog(getClass());

    private IFrameworkRuns frameworkRuns;
    private IConfigurationPropertyStoreService cps;
    private ITimeService timeService;
    private RBACService rbacService;
    private ITagsService tagsService;
    
    public PrioritySchedulingService(
        IFrameworkRuns frameworkRuns,
        IConfigurationPropertyStoreService cps,
        RBACService rbacService,
        ITimeService timeService,
        ITagsService tagsService
    ) {
        this.frameworkRuns = frameworkRuns;
        this.rbacService = rbacService;
        this.cps = cps;
        this.timeService = timeService;
        this.tagsService = tagsService;
    }

    @Override
    public List<IRun> getPrioritisedTestRunsToSchedule() throws FrameworkException {
        List<IRun> queuedRuns = getQueuedRemoteRuns();
        Map<String, Tag> queuedRunTags = getAllQueuedRunTagsFromCps(queuedRuns);
        queuedRuns.sort(getPriorityComparator(queuedRunTags));
        return queuedRuns;
    }

    private Map<String, Tag> getAllQueuedRunTagsFromCps(List<IRun> queuedRuns) {
        Set<String> tagNamesSet = new HashSet<>();
        for (IRun run : queuedRuns) {
            tagNamesSet.addAll(run.getTags());
        }

        Map<String, Tag> tagsFromCps = new HashMap<>();
        for (String tagName : tagNamesSet) {
            try {
                Tag tag = tagsService.getTagByName(tagName);
                if (tag != null) {
                    tagsFromCps.put(tagName, tag);
                }
            } catch (Exception e) {
                logger.warn("Could not get tag " + tagName + ", ignoring tag priority");
            }
        }
        return tagsFromCps;
    }

    private Comparator<IRun> getPriorityComparator(Map<String, Tag> queuedRunTags) {
        return (a, b) -> Double.compare(getQueuedRunTotalPriorityPoints(b, queuedRunTags), getQueuedRunTotalPriorityPoints(a, queuedRunTags));
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

    double getQueuedRunTotalPriorityPoints(IRun run, Map<String, Tag> queuedRunTags) {
        double totalPriorityPoints = getPriorityPointsFromQueuedTime(run.getQueued());
        totalPriorityPoints += getRequestorPriorityPoints(run.getRequestor());
        totalPriorityPoints += getPriorityPointsFromTags(run.getTags(), queuedRunTags);

        return totalPriorityPoints;
    }

    private double getPriorityPointsFromQueuedTime(Instant queuedTime) {
        double priorityGrowthRatePerMin = getPriorityGrowthRatePerMin();
        Instant currentTime = timeService.now();

        double minutesElapsedSinceQueued = getMinutesBetween(queuedTime, currentTime);
        return minutesElapsedSinceQueued * priorityGrowthRatePerMin;
    }

    private double getPriorityPointsFromTags(Set<String> runTags, Map<String, Tag> collectedQueuedRunTags) {
        double priorityPointsFromTags = 0;

        for (String tagName : runTags) {
            Tag tag = collectedQueuedRunTags.get(tagName);
            if (tag != null) {
                priorityPointsFromTags += tag.getPriority();
            }
        }

        return priorityPointsFromTags;
    }

    private double getMinutesBetween(Instant startTime, Instant endTime) {
        return Duration.between(startTime, endTime).toMillis() / (60 * 1000.0);
    }

    private int getRequestorPriorityPoints(String requestor) {
        int requestorPriority = 0;
        try {
            requestorPriority = rbacService.getUserPriority(requestor);
        } catch (RBACException e) {
            logger.warn("Could not get user priority details, ignoring user priority");
        }

        return requestorPriority;
    }

    private double getPriorityGrowthRatePerMin() {
        double priorityGrowthRatePerMin = DEFAULT_TEST_RUN_PRIORITY_POINTS_GROWTH_RATE_PER_MIN;

        try {
            String priorityGrowthRateStr = cps.getProperty(RUNS_PRIORITY_GROWTH_RATE_CPS_PROPERTY_PREFIX, RUNS_PRIORITY_GROWTH_RATE_CPS_PROPERTY_SUFFIX);
            if (priorityGrowthRateStr != null && !priorityGrowthRateStr.isBlank()) {
                priorityGrowthRatePerMin = Double.parseDouble(priorityGrowthRateStr);
            } else {
                logger.trace(RUNS_PRIORITY_GROWTH_RATE_CPS_PROPERTY_KEY + " CPS property is not set or is empty, using default: " + DEFAULT_TEST_RUN_PRIORITY_POINTS_GROWTH_RATE_PER_MIN);
            }
        } catch (Exception e) {
            logger.trace("Could not get CPS property " + RUNS_PRIORITY_GROWTH_RATE_CPS_PROPERTY_KEY + ", using default: " + DEFAULT_TEST_RUN_PRIORITY_POINTS_GROWTH_RATE_PER_MIN);
        }
        return priorityGrowthRatePerMin;
    }
}
