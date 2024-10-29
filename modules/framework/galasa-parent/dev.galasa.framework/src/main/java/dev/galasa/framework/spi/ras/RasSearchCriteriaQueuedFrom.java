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
	
	private final Instant from;
	
	public RasSearchCriteriaQueuedFrom(@NotNull Instant fromCriteria) {
		this.from = fromCriteria;
	}
	
	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {
		
		if(structure == null) {
			return Boolean.FALSE;	
		}
		
		if(structure.getStartTime() == null) {
			return Boolean.FALSE;
		}
		
		if(from == null) {
			return Boolean.FALSE;
		}
		
		if(from.equals(structure.getStartTime()) || from.isBefore(structure.getStartTime())) {
			return Boolean.TRUE;
		}
		
		return Boolean.FALSE;
	}

    public Instant getFrom() {
        return this.from;
    }
	
}

