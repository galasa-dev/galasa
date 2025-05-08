/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.time.Instant;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaQueuedTo implements IRasSearchCriteria {

	private static final String CRITERIA_NAME = "queued";
	private final Instant to;
	
	public RasSearchCriteriaQueuedTo(@NotNull Instant toCriteria) {
		this.to = toCriteria;
	}
	
	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {
		
		if(structure == null) {
			return Boolean.FALSE;	
		}
		
		if(structure.getEndTime() == null) {
			return Boolean.FALSE;
		}
		
		if(to == null) {
			return Boolean.FALSE;
		}
		
		if(to.equals(structure.getEndTime()) || to.isAfter(structure.getEndTime())) {
			return Boolean.TRUE;
		}
		
		return Boolean.FALSE;
	}

    public Instant getTo() {
        return this.to;
    }

	@Override
	public String getCriteriaName() {
		return CRITERIA_NAME;
	}

	@Override
	public String[] getCriteriaContent() {
		return new String[]{ this.to.toString() };
	}
}
