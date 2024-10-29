/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExamplesTokenMatcher implements TokenMatcher {
    private final Pattern examplesPattern = Pattern.compile("Examples:");
    @Override
    public ParseToken matches(String line) {
        ParseToken token = null ;
        Matcher examplesMatch = examplesPattern.matcher(line);
        if (examplesMatch.matches()) {
            token = new ParseToken(TokenType.EXAMPLES_START,"");
        }
        return token;
    }
}