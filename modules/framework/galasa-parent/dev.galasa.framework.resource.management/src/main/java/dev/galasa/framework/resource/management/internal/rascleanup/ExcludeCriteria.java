/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.rascleanup;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasSearchCriteriaGroup;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRequestor;
import dev.galasa.framework.spi.ras.RasSearchCriteriaResult;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRunName;
import dev.galasa.framework.spi.ras.RasSearchCriteriaStatus;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTags;
import dev.galasa.framework.spi.ras.RasSearchCriteriaUser;
import dev.galasa.framework.spi.teststructure.TestStructure;

public enum ExcludeCriteria {
    GROUP("group"),
    REQUESTOR("requestor"),
    RESULT("result"),
    RUN_NAME("runName"),
    STATUS("status"),
    TAGS("tags"),
    USER("user");

    private String criteriaName;

    private ExcludeCriteria(String criteriaName) {
        this.criteriaName = criteriaName;
    }

    /**
     * Creates an IRasSearchCriteria instance with the provided values.
     *
     * @param values The criteria values to match against
     * @return A new IRasSearchCriteria instance configured with the values
     * @throws FrameworkException if an unknown criteria type is provided
     */
    public IRasSearchCriteria createCriteria(String[] values) throws FrameworkException {
        switch (this) {
            case TAGS:
                return new RasSearchCriteriaTags(values);
            case RESULT:
                return new RasSearchCriteriaResult(values);
            case RUN_NAME:
                return new RasSearchCriteriaRunName(values);
            case GROUP:
                return new RasSearchCriteriaGroup(values);
            case USER:
                return new RasSearchCriteriaUser(values);
            case REQUESTOR:
                return new RasSearchCriteriaRequestor(values);
            case STATUS:
                List<TestRunLifecycleStatus> statuses = getTestRunStatusesFromValues(values);
                return new RasSearchCriteriaStatus(statuses);
            default:
                throw new FrameworkException("Unknown criteria type: " + this);
        }
    }

    private List<TestRunLifecycleStatus> getTestRunStatusesFromValues(String[] values) {
        List<TestRunLifecycleStatus> statuses = new ArrayList<>();

        for (String value : values) {
            TestRunLifecycleStatus status = TestRunLifecycleStatus.getFromString(value);
            if (status != null) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    /**
     * Checks if a test run should be kept based on the criteria values.
     *
     * @param testStructure The test structure to check
     * @param excludeValues The values to match against for exclusion from cleanup
     * @return true if the run matches the criteria, false otherwise
     * @throws FrameworkException if an unknown criteria type is provided
     */
    public boolean shouldRunBeKept(TestStructure testStructure, String[] excludeValues) throws FrameworkException {
        boolean shouldKeep = false;
        if (excludeValues != null && excludeValues.length > 0) {
            IRasSearchCriteria criteria = createCriteria(excludeValues);
            shouldKeep = criteria.criteriaMatched(testStructure);
        }
        return shouldKeep;
    }

    public static ExcludeCriteria getFromString(String criteria) {
        ExcludeCriteria match = null;
        for (ExcludeCriteria resource : values()) {
            if (resource.toString().equalsIgnoreCase(criteria.trim())) {
                match = resource;
                break;
            }
        }
        return match;
    }

    @Override
    public String toString() {
        return criteriaName;
    }
}
