/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScenarioTokenMatcher implements TokenMatcher {
    private final Pattern scenarioPattern = Pattern.compile("Scenario:(.*)");
    @Override
    public ParseToken matches(String line) {
        ParseToken token = null ;
        Matcher scenarioMatch = scenarioPattern.matcher(line);
        if (scenarioMatch.matches()) {
            String methodName = scenarioMatch.group(1).trim();
            token = new ParseToken(TokenType.SCENARIO_START,methodName);
        }
        return token;
    }
}