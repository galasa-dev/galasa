/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.util.Set;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaTags implements IRasSearchCriteria {

	private static final String CRITERIA_NAME = "tags";
	private final String[] tags;

	public RasSearchCriteriaTags(@NotNull String... tags) {
		this.tags = tags;
	}

	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {

		boolean isMatched = false;
		if (structure != null && tags != null) {
			Set<String> structureTags = structure.getTags();
			for (String tag : tags) {
				if (structureTags.contains(tag)) {
					isMatched = true;
					break;
				}
			}
		}

		return isMatched;
	}

    public String[] getTags() {
        return this.tags;
    }

	@Override
	public String getCriteriaName() {
		return CRITERIA_NAME;
	}

	@Override
	public String[] getCriteriaContent() {
		return this.tags;
	}
}
