/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaRunName implements IRasSearchCriteria {
	
	private static final String CRITERIA_NAME = "runName";
	private final String[] runNames;

	public RasSearchCriteriaRunName(@NotNull String... testNameCriteria) {
		this.runNames = testNameCriteria;
	}

	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {

		if(structure == null) {
			return Boolean.FALSE;	
		}

		if(runNames != null) {
			for(String runName : runNames) {
				if(runName.equals(structure.getRunName())) {
					return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;
	}


    public String[] getRunNames() {
        return this.runNames;
    }

	@Override
	public String getCriteriaName() {
		return CRITERIA_NAME;
	}

	@Override
	public String[] getCriteriaContent() {
		return this.runNames;
	}
}
	
