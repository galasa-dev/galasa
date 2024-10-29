/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import dev.galasa.framework.TestRunException;

public enum GherkinKeyword {
    GIVEN,
    WHEN,
    THEN,
    AND,
    DATATABLE;

    public static GherkinKeyword get(String statement) throws TestRunException {
        String[] words = statement.split(" ");
        for(GherkinKeyword keyword : values()) {
            if(words[0].toUpperCase().equals(keyword.name())) {
                return keyword;
            }
        }
        throw new TestRunException("Unrecognised keyword: " + words[0]);
    }
}

