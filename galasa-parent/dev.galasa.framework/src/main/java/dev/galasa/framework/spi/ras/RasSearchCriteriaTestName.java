/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaTestName implements IRasSearchCriteria {
	
	private final String[] testNames;
	
	public RasSearchCriteriaTestName(@NotNull String... testNameCriteria) {
		this.testNames = testNameCriteria;
	}
	
	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {
		
		if(structure == null) {
			return Boolean.FALSE;	
		}
		
		if(testNames != null) {
			for(String testName : testNames) {
				if(testName.equals(structure.getTestName())) {
					return Boolean.TRUE;
				}
			}
		}
		
		return Boolean.FALSE;
	}
	

    public String[] getTestNames() {
        return this.testNames;
    }
}
