/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import dev.galasa.framework.spi.FrameworkException;

public class MonitorFilter {
    
    private List<Pattern> includes;
    private List<Pattern> excludes;

    public MonitorFilter(List<String> includesGlobList, List<String> excludesGlobList) throws FrameworkException {
        this.includes = convertGlobListToPatternList(includesGlobList);
        this.excludes = convertGlobListToPatternList(excludesGlobList);
    }

    public boolean isMonitorClassAllowed(String monitorClassName) {
        boolean isAllowed = false;

        for (Pattern includePattern : includes) {
            Matcher includeMatcher = includePattern.matcher(monitorClassName);
            if (includeMatcher.matches()) {
                isAllowed = true;
                break;
            }
        }

        for (Pattern excludePattern : excludes) {
            Matcher excludeMatcher = excludePattern.matcher(monitorClassName);
            if (excludeMatcher.matches()) {
                isAllowed = false;
                break;
            }
        }

        return isAllowed;
    }

    private List<Pattern> convertGlobListToPatternList(List<String> globList) throws FrameworkException {
        List<Pattern> patternList = new ArrayList<>();
        for (String globPattern : globList) {            
            try {
                String convertedPattern = getGlobAsRegexString(globPattern);
                Pattern patternToAdd = Pattern.compile(convertedPattern);
                patternList.add(patternToAdd);
            } catch (PatternSyntaxException e) {
                throw new FrameworkException("Failed to compile glob pattern into a valid regex pattern", e);
            }
        }
        return patternList;
    }

    private String getGlobAsRegexString(String globPattern) {
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
        return patternBuilder.toString();
    }
}
