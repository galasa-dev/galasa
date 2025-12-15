/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaUser implements IRasSearchCriteria {

	private static final String CRITERIA_NAME = "user";
	private final String[] users;

    public RasSearchCriteriaUser(@NotNull String... userCriteria) {
        this.users = userCriteria;

    }

    @Override
    public boolean criteriaMatched(@NotNull TestStructure structure) {
        
        if (structure == null) {
            return Boolean.FALSE;
        }

        if (users != null) {
            for (String user : users) {
                // When searching by user, match on either the test user or requestor.
                if (user.equals(structure.getUser()) || user.equals(structure.getRequestor())) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public String getCriteriaName() {
        return CRITERIA_NAME;
    }

    @Override
    public String[] getCriteriaContent() {
        return this.users;
    }
    
}
