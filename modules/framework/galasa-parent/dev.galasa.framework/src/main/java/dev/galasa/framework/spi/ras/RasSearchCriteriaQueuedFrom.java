/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;
import java.time.Instant;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaQueuedFrom implements IRasSearchCriteria {
	
	private static final String CRITERIA_NAME = "queued";
	private final Instant from;
	
	public RasSearchCriteriaQueuedFrom(@NotNull Instant fromCriteria) {
		this.from = fromCriteria;
	}
	
	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {
		
		if(structure == null) {
			return true;	
		}
		
		if(structure.getQueued() == null) {
			return false;
		}
		
		if(from == null) {
			return false;
		}
		
		if(from.equals(structure.getQueued()) || from.isBefore(structure.getQueued())) {
			return true;
		}
		
		return false;
	}

    public Instant getFrom() {
        return this.from;
    }

	@Override
	public String getCriteriaName() {
		return CRITERIA_NAME;
	}

	@Override
	public String[] getCriteriaContent() {
		return new String[]{ this.from.toString() };
	}
	
}

