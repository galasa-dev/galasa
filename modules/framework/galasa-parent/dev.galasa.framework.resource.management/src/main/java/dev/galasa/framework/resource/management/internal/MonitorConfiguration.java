/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.List;

import dev.galasa.framework.spi.FrameworkException;

public class MonitorConfiguration {
    
    private String stream;
    private MonitorFilter filter;

    public MonitorConfiguration(String stream, List<String> includesGlobList, List<String> excludesGlobList) throws FrameworkException {
        this.stream = stream;
        this.filter = new MonitorFilter(includesGlobList, excludesGlobList);
    }

    public String getStream() {
        return stream;
    }

    public MonitorFilter getFilter() {
        return this.filter;
    }
}
