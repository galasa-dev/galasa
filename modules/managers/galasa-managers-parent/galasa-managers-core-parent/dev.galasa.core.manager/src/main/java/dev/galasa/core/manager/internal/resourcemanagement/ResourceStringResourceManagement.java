/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal.resourcemanagement;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.core.manager.internal.ResourceStringGenerator;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;

/**
 * Core Resource String Resource Management
 * 
 *  
 *
 */
public class ResourceStringResourceManagement implements Runnable {

    private final IFramework                 framework;
    private final IResourceManagement        resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log                        logger        = LogFactory.getLog(this.getClass());

    // The pattern used to find ONLY the run indexed resource string keys for the core manager
    private final Pattern                    runStringPattern      = Pattern.compile("^run\\.(\\w+)\\.resource.string\\.(\\w+)$");

    public ResourceStringResourceManagement(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss) {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("Core Resource String resource management initialised");
    }

    @Override
    public void run() {
    	// This method is called every 20 seconds to do the check
        logger.info("Starting Core Resource String search search");
        try {
            // *** Find all CORE run keys
            Map<String, String> linuxRuns = dss.getPrefix("run.");

            // Ask the framework for all the active runs
            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            // Search through all the Core run keys (more than just the resource strings
            for (String key : linuxRuns.keySet()) {
            	//  Check that this is a resource string run key
                Matcher matcher = runStringPattern.matcher(key);
                if (matcher.find()) {
                	// Yes it is, extract the runname and the resource string
                    String runName = matcher.group(1);
                    String resourceString = matcher.group(2);

                    // Is the run still active
                    if (!activeRunNames.contains(runName)) {
                    	// Nope, discard the resource string for that run
                        logger.info("Discarding Resource String " + resourceString + " as run " + runName + " has gone");

                        try {
                            ResourceStringGenerator.discardResourceString(this.dss, runName, resourceString);
                        } catch (DynamicStatusStoreMatchException e) {
                        	// It is possible to have 2 resource managers running,  so they may attempt to clean the same thing
                            logger.info("Resource String changed whilst cleaning, will try again later");
                        } catch (Exception e) {
                            logger.error("Failed to discard Resource String " + resourceString + " for run " + runName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failure during Resource String scan", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Core Resource String search");
    }

    public void runFinishedOrDeleted(String runName) {
    	// This method is called whenever the Resource Management routine detects that a 
    	// run is finished or has been deleted
    	// Drive the same resource search as above.
    	// We do this so we can clean resources instantly after a run finished
    	// so another run can use it without having to wait for 20 seconds.
    	// 20 seconds doesn't sound alot,  but running 1000 tests in parallel it can add up
        try {
        	// Search for all core run keys for this specific run
            Map<String, String> serverRuns = dss.getPrefix("run." + runName + ".");
            for (String key : serverRuns.keySet()) {
            	// we are only interested in the resource string key
                Matcher matcher = runStringPattern.matcher(key);
                if (matcher.find()) {
                	// already know the run name so only extract the resource string
                    String resourceString = matcher.group(2);

                    logger.info("Discarding Resource String " + resourceString + " as run " + runName + " has gone");

                    try {
                    	// discard it
                    	ResourceStringGenerator.discardResourceString(this.dss, runName, resourceString);
                    } catch (DynamicStatusStoreMatchException e) {
                    	// highly likely if 2 resource management servers are running
                        logger.info("Resource String changed whilst cleaning, will try again later");
                    } catch (Exception e) {
                        logger.error("Failed to discard Resource String " + resourceString + " for run " + runName);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete Resource String for run " + runName);
        }
    }

}
