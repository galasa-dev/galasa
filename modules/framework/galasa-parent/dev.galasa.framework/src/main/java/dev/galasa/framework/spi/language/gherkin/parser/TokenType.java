/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

public enum TokenType {

    // Terminal tokens
    FEATURE_START("Feature:"), // text contains feature description.
    SCENARIO_START("Scenario:"), // text contains scenario name.
    SCENARIO_OUTLINE_START("Scenario Outline:"), // text contains scenario outline name.
    EXAMPLES_START("Examples:"), 
    STEP("step"), // text contains the step text
    DATA_LINE("data line"), // text contains the table text, including | characters.
    END_OF_FILE("end of file"),

    // Non-terminal tokens
    FEATURE("<feature>"),
    SCENARIO_OUTLINE("<scenarioOutline>"),
    SCENARIO("<scenario>"),
    SCENARIO_PART_LIST("<scenarioPartList>"),
    SCENARIO_PART("<scenarioPart>"),
    DATA_TABLE("<dataTable>"),
    DATA_TABLE_HEADER("<dataTableHeader>"),
    DATA_TABLE_LINE_LIST("<dataTableLineList>"),
    STEP_LIST("<stepList>")
    ;

    private String readableName ;

    private TokenType(String readableName) {
        this.readableName = readableName;
    }

    public String getReadableName() {
        return this.readableName;
    }
}