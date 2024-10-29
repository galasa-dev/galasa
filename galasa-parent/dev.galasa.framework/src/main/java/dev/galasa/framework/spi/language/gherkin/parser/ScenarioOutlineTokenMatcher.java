/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScenarioOutlineTokenMatcher implements TokenMatcher {
    private final Pattern scenarioOutlinePattern = Pattern.compile("Scenario Outline:(.*)");
    @Override
    public ParseToken matches(String line) {
        ParseToken token = null ;
        Matcher scenarioOutlineMatch = scenarioOutlinePattern.matcher(line);
        if (scenarioOutlineMatch.matches()) {
            String scenarioName = scenarioOutlineMatch.group(1).trim();
            token = new ParseToken(TokenType.SCENARIO_OUTLINE_START,scenarioName);
        }
        return token;
    }
}