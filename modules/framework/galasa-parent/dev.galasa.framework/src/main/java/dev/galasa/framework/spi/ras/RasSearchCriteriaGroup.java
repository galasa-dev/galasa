/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaGroup implements IRasSearchCriteria {
	
	private static final String CRITERIA_NAME = "group";
	private final String[] groups;

	public RasSearchCriteriaGroup(@NotNull String... groups) {
		this.groups = groups;
	}

	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {

		if(structure == null) {
			return Boolean.FALSE;	
		}

		if(groups != null) {
			for(String groupIn : groups) {
				if(groupIn.equals(structure.getGroup())) {
					return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;
	}


    public String[] getGroups() {
        return this.groups;
    }

	@Override
	public String getCriteriaName() {
		return CRITERIA_NAME;
	}

	@Override
	public String[] getCriteriaContent() {
		return this.groups;
	}
}
	
