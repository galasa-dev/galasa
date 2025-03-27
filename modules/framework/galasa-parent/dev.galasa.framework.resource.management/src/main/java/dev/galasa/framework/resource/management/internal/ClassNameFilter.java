/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.FrameworkException;

public class ClassNameFilter {

    private List<JavaClassGlobPattern> includes;
    private List<JavaClassGlobPattern> excludes;

    public ClassNameFilter(List<String> includesGlobList, List<String> excludesGlobList) throws FrameworkException {
        this.includes = convertGlobListToPatternList(includesGlobList);
        this.excludes = convertGlobListToPatternList(excludesGlobList);
    }

    public boolean isClassAcceptedByFilter(String className) {
        boolean isAllowed = (
            isMatchingAnyPatternInList(className, includes) && !isMatchingAnyPatternInList(className, excludes)
        );

        return isAllowed;
    }

    private boolean isMatchingAnyPatternInList(String strToCheck, List<JavaClassGlobPattern> patterns) {
        boolean isMatching = false;

        for (JavaClassGlobPattern pattern : patterns) {
            if (pattern.isMatchingString(strToCheck)) {
                isMatching = true;
                break;
            }
        }
        return isMatching;
    }

    private List<JavaClassGlobPattern> convertGlobListToPatternList(List<String> globList) throws FrameworkException {
        List<JavaClassGlobPattern> patternList = new ArrayList<>();
        for (String globPattern : globList) {
            patternList.add(new JavaClassGlobPattern(globPattern));
        }
        return patternList;
    }
}
