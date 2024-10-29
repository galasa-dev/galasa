/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreWatcher;
import dev.galasa.framework.spi.IFramework;

public class ResourceManagementRunWatch implements IDynamicStatusStoreWatcher {

    private final Log                        logger         = LogFactory.getLog(this.getClass());

    private final Pattern                    runTestPattern = Pattern.compile("^\\Qrun.\\E(\\w+)\\Q.status\\E$");

    private final IFramework                 framework;
    private final ResourceManagement         resourceManagement;
    private final IDynamicStatusStoreService dssFramework;
    private final UUID                       watchID;

    protected ResourceManagementRunWatch(IFramework framework, ResourceManagement resourceManagement)
            throws FrameworkException {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dssFramework = this.framework.getDynamicStatusStoreService("framework");

        this.watchID = this.dssFramework.watchPrefix(this, "run");
        this.dssFramework.watch(this, "mike");
    }

    @Override
    public void propertyModified(String key, Event event, String oldValue, String newValue) {

        if (event == null || key == null) {
            return;
        }

        Matcher matcher = runTestPattern.matcher(key);
        if (!matcher.find()) {
            return;
        }

        String runName = matcher.group(1);

        if (event == Event.DELETE) {
            logger.debug("Detected deleted run " + runName);
            this.resourceManagement.runFinishedOrDeleted(runName);
            return;
        }

        if ("Finished".equals(newValue)) {
            logger.debug("Detected finished run " + runName);
            this.resourceManagement.runFinishedOrDeleted(runName);
            return;
        }
    }

    public void shutdown() {
        try {
            this.dssFramework.unwatch(watchID);
        } catch (DynamicStatusStoreException e) {
        }
    }

}
