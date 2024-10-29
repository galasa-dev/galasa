/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

public class DataLineMatcher implements TokenMatcher {
    @Override
    public ParseToken matches(String line) {
        ParseToken token = null ;
        if (line.startsWith("|")) {
            token = new ParseToken(TokenType.DATA_LINE,line);
        }
        return token;
    }
}