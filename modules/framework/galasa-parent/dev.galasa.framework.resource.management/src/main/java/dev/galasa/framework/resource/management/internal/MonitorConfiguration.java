/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import dev.galasa.framework.spi.FrameworkException;

public class MonitorConfiguration {
    
    private String stream;
    private List<Pattern> includesRegexList;
    private List<Pattern> excludesRegexList;

    public MonitorConfiguration(String stream, List<String> includesRegexList, List<String> excludesRegexList) throws FrameworkException {
        this.stream = stream;
        this.includesRegexList = convertListToPatternList(includesRegexList);
        this.excludesRegexList = convertListToPatternList(excludesRegexList);
    }

    public String getStream() {
        return stream;
    }

    public List<Pattern> getIncludesRegexList() {
        return includesRegexList;
    }

    public List<Pattern> getExcludesRegexList() {
        return excludesRegexList;
    }

    private List<Pattern> convertListToPatternList(List<String> regexList) throws FrameworkException {
        List<Pattern> patternList = new ArrayList<>();
        for (String pattern : regexList) {
            String patternToAdd = pattern;

            // Patterns like '*' and '*mysuffix' are not valid regex patterns alone, so convert them into valid
            // patterns like '.*' and '.*mysuffix' to accept any character (.) zero or more times (*) in prefixes.
            if (pattern.startsWith("*")) {
                patternToAdd = "." + pattern;
            }

            try {
                patternList.add(Pattern.compile(patternToAdd));
            } catch (PatternSyntaxException e) {
                throw new FrameworkException("Invalid regex pattern provided", e);
            }
        }
        return patternList;
    }
}
