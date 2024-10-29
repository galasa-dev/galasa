/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureTokenMatcher implements TokenMatcher {
    private final Pattern featurePattern = Pattern.compile("Feature:(.*)");
    @Override
    public ParseToken matches(String line) {
        ParseToken token = null ;
        Matcher featureMatch = featurePattern.matcher(line);
        if (featureMatch.matches()) {
            String testName = featureMatch.group(1).trim();
            token = new ParseToken(TokenType.FEATURE_START,testName);
        }
        return token;
    }
}