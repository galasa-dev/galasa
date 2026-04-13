/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.rascleanup;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;
import dev.galasa.framework.spi.utils.ITimeService;

public class RasRunCleanup implements Runnable {

    private final Log logger = LogFactory.getLog(getClass());

    private static final String TEST_RUN_CLEANUP_CPS_PREFIX = "ras.cleanup";
    private static final String TEST_RUN_MAX_DAYS_CPS_PROPERTY = "test.run.age.max.days";
    private static final String TEST_RUN_EXCLUDE_PREFIX = TEST_RUN_CLEANUP_CPS_PREFIX + ".test.run.exclude.";

    private final int initialRunCleanupMaxAgeDays;

    private IResultArchiveStore rasService;
    private IConfigurationPropertyStoreService cpsService;
    private ITimeService timeService;

    public RasRunCleanup(
        IConfigurationPropertyStoreService cpsService,
        IResultArchiveStore rasService,
        ITimeService timeService,
        int initialRunCleanupMaxAgeDays
    ) throws FrameworkException {
        this.rasService = rasService;
        this.cpsService = cpsService;
        this.timeService = timeService;
        this.initialRunCleanupMaxAgeDays = initialRunCleanupMaxAgeDays;
    }

    @Override
    public void run() {
        logger.info("Starting scan for runs to clean up from the RAS");

        List<IRunResult> runs = new ArrayList<>();
        try {
            runs = getRunsToCleanUp();
        } catch (Exception e) {
            logger.error("Error while scanning for runs to clean up", e);
        }

        for (IRunResult run : runs) {
            String runId = run.getRunId();

            try {
                logger.trace("Deleting run " + runId);
                run.discard();
                logger.trace("Deleted run " + runId);
            } catch (Exception e) {
                logger.error("Error while deleting run " + runId, e);
            }
        }

        logger.info("Finished scan for runs to clean up from the RAS");
    }

    private List<IRunResult> getRunsToCleanUp() throws FrameworkException {
        List<IRunResult> runs = new ArrayList<>();
        List<IRasSearchCriteria> searchCriteria = buildSearchCriteria();

        if (searchCriteria != null && !searchCriteria.isEmpty()) {
            for (IResultArchiveStoreDirectoryService directoryService : rasService.getDirectoryServices()) {
                runs.addAll(directoryService.getRuns(searchCriteria.toArray(new IRasSearchCriteria[0])));
            }
            logger.info("Found " + runs.size() + " run(s) matching cleanup criteria");

            // Filter out excluded runs - returns only runs that should still be cleaned up
            runs = filterOutRunsToKeep(runs);
            logger.info(runs.size() + " run(s) will be deleted from the RAS");
        }
        return runs;
    }

    private List<IRunResult> filterOutRunsToKeep(List<IRunResult> runsToCleanUp) throws FrameworkException {
        Set<IRunResult> runsToKeep = new HashSet<>();

        // Get all the properties in the form framework.ras.cleanup.test.run.exclude.<field>=<comma-separated-values-to-exclude>
        Map<String, String> excludeProperties = cpsService.getPrefixedProperties(TEST_RUN_EXCLUDE_PREFIX);

        if (!excludeProperties.isEmpty()) {

            // Build a list of exclude criteria
            List<ParsedExcludeCriteria> criteriaList = buildExcludeCriteria(excludeProperties);

            // Build a list of runs to keep
            for (IRunResult run : runsToCleanUp) {
                for (ParsedExcludeCriteria parsedCriteria : criteriaList) {
                    if (parsedCriteria.getCriteria().shouldRunBeKept(run.getTestStructure(), parsedCriteria.getValues())) {
                        runsToKeep.add(run);
                        logger.trace("Run " + run.getRunId() + " excluded from cleanup by " + parsedCriteria.getRunFieldName() + " criteria");
                        break;
                    }
                }
            }
            logger.trace("Excluded " + runsToKeep.size() + " run(s) from cleanup");

            // Remove the runs to keep from the list of runs that should be cleaned up
            runsToCleanUp.removeAll(runsToKeep);
        }

        return runsToCleanUp;
    }

    private List<ParsedExcludeCriteria> buildExcludeCriteria(Map<String, String> excludeProperties) {
        List<ParsedExcludeCriteria> criteriaList = new ArrayList<>();

        for (Map.Entry<String, String> entry : excludeProperties.entrySet()) {
            String key = entry.getKey();
            String fieldName = key.substring(key.lastIndexOf(".") + 1).toLowerCase();

            ExcludeCriteria criteria = ExcludeCriteria.getFromString(fieldName);
            if (criteria != null) {

                // Values may be provided as comma-separated lists,
                // so split by the commas and trim each value
                String[] valuesToKeep = entry.getValue().split(",");
                for (int i = 0; i < valuesToKeep.length; i++) {
                    valuesToKeep[i] = valuesToKeep[i].trim();
                }
                criteriaList.add(new ParsedExcludeCriteria(criteria, valuesToKeep, fieldName));
            }
        }
        return criteriaList;
    }

    private class ParsedExcludeCriteria {
        final ExcludeCriteria criteria;
        final String[] values;
        final String fieldName;

        ParsedExcludeCriteria(ExcludeCriteria criteria, String[] values, String fieldName) {
            this.criteria = criteria;
            this.values = values;
            this.fieldName = fieldName;
        }

        public ExcludeCriteria getCriteria() {
            return criteria;
        }

        public String[] getValues() {
            return values;
        }

        public String getRunFieldName() {
            return fieldName;
        }
    }

    private List<IRasSearchCriteria> buildSearchCriteria() throws ConfigurationPropertyStoreException {
        List<IRasSearchCriteria> criteria = new ArrayList<>();

        int runMaxAgeDays = initialRunCleanupMaxAgeDays;
        try {
            String runMaxAgeStr = cpsService.getProperty(TEST_RUN_CLEANUP_CPS_PREFIX, TEST_RUN_MAX_DAYS_CPS_PROPERTY);
            if (runMaxAgeStr != null) {
                runMaxAgeDays = Integer.parseInt(runMaxAgeStr);
            }

        } catch (NumberFormatException e) {
            logger.warn("Invalid CPS property value provided. A numeric value is expected for '" + TEST_RUN_MAX_DAYS_CPS_PROPERTY + "'.");
        }

        // If a maximum age is set, add queued time criteria
        if (runMaxAgeDays > 0) {
            Instant searchToTime = timeService.now().minus(runMaxAgeDays, ChronoUnit.DAYS);
            criteria.add(new RasSearchCriteriaQueuedTo(searchToTime));
        }
        return criteria;
    }
}
