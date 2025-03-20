/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

import java.util.List;

public class MonitorConfiguration {
    
    private String stream;
    private List<String> includesRegexList;
    private List<String> excludesRegexList;

    public MonitorConfiguration(String stream, List<String> includesRegexList, List<String> excludesRegexList) {
        this.stream = stream;
        this.includesRegexList = includesRegexList;
        this.excludesRegexList = excludesRegexList;
    }

    public String getStream() {
        return stream;
    }

    public List<String> getIncludesRegexList() {
        return includesRegexList;
    }

    public List<String> getExcludesRegexList() {
        return excludesRegexList;
    }
}
