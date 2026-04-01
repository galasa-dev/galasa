/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.rascleanup;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasSearchCriteriaBundle;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTags;
import dev.galasa.framework.spi.ras.RasSearchCriteriaUser;
import dev.galasa.framework.spi.teststructure.TestStructure;

public enum ExcludeCriteria {
    TAGS("tags"),
    USER("user"),
    BUNDLE("bundle");

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
            case USER:
                return new RasSearchCriteriaUser(values);
            case BUNDLE:
                return new RasSearchCriteriaBundle(values);
            default:
                throw new FrameworkException("Unknown criteria type: " + this);
        }
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
