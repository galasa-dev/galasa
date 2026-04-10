/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaRequestor implements IRasSearchCriteria {
	
	private static final String CRITERIA_NAME = "requestor";
	private final String[] requestors;
	
	public RasSearchCriteriaRequestor(@NotNull String... requestorCriteria) {
		this.requestors = requestorCriteria;
	}
	
	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {
		
		if(structure == null) {
			return false;	
		}
		
		if(requestors != null) {
			for(String requestor : requestors) {
				if(requestor.equalsIgnoreCase(structure.getRequestor())){
					return true;
				}
			}
		}
		
		return false;
	}

    public String[] getRequestors() {
        return requestors;
    }

	@Override
	public String getCriteriaName() {
		return CRITERIA_NAME;
	}

	@Override
	public String[] getCriteriaContent() {
		return this.requestors;
	}
}
