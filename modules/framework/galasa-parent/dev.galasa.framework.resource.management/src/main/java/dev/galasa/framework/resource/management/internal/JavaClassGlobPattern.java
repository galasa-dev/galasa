/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import dev.galasa.framework.spi.FrameworkException;

/**
 * A simple representation of a glob pattern that is used to match against Java class names.
 * 
 * Only alphanumeric characters, '.', '?', and '*' characters are supported by these patterns.
 */
public class JavaClassGlobPattern {

    private Pattern pattern;

    public JavaClassGlobPattern(String globPattern) throws FrameworkException {
        validateGlobPattern(globPattern);
        this.pattern = getGlobAsRegexPattern(globPattern);
    }

    public boolean isMatchingString(String strToMatch) {
        Matcher matcher = this.pattern.matcher(strToMatch);
        return matcher.matches();
    }

    private Pattern getGlobAsRegexPattern(String globPattern) throws FrameworkException {
        Pattern pattern = null;
        StringBuilder patternBuilder = new StringBuilder();
        for (char globChar : globPattern.toCharArray()) {

            // Globs use special characters which correspond to different regex patterns:
            // '*' (wildcard) expands to zero or more characters
            // '?' corresponds to exactly one character
            // '.' corresponds to an actual '.' character
            switch (globChar) {
                case '*':
                    patternBuilder.append(".*");
                    break;
                case '?':
                    patternBuilder.append(".");
                    break;
                case '.':
                    patternBuilder.append("\\.");
                    break;
                default:
                    patternBuilder.append(globChar);
            }
        }

        try {
            pattern = Pattern.compile(patternBuilder.toString());
        } catch (PatternSyntaxException e) {
            throw new FrameworkException("Failed to compile glob pattern into a valid regex pattern", e);
        }
        return pattern;
    }

    private void validateGlobPattern(String pattern) throws FrameworkException {
        for (char patternChar : pattern.toCharArray()) {
            if (!Character.isLetterOrDigit(patternChar)
                && patternChar != '*'
                && patternChar != '?'
                && patternChar != '.'
            ) {
                throw new FrameworkException("Unsupported glob pattern character provided. "+
                    "Only alphanumeric (A-Z, a-z, 0-9), '.', '?', and '*' characters are supported.");
            }
        }
    }
}
