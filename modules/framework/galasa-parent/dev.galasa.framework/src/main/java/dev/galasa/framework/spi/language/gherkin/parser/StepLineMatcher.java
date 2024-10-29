/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

public class StepLineMatcher implements TokenMatcher {
    @Override
    public ParseToken matches(String line) {
        ParseToken token = new ParseToken(TokenType.STEP,line);
        return token;
    }
}