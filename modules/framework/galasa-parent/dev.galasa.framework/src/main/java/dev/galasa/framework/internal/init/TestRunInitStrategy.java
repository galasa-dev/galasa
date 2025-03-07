/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.init;

import java.util.Properties;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.Framework;
import dev.galasa.framework.IFrameworkInitialisationStrategy;
import dev.galasa.framework.beans.Property;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.utils.GalasaGson;

public class TestRunInitStrategy implements IFrameworkInitialisationStrategy {

    private final static Log logger = LogFactory.getLog(Framework.class);
    private static final GalasaGson gson = new GalasaGson();

    @Override
    public void startLoggingCapture(Framework framework) {
        // Each test run needs to install the log4j capture routine
        framework.installLogCapture();
    }

    @Override
    public void setTestRunName(Framework framework, IConfigurationPropertyStoreService cps) throws FrameworkException {
        // We need to make sure we have a runname for the RAS. If there isn't one, we need to allocate one
        // Need the DSS for this as the latest run number number is stored in there
        String runName = locateRunName(framework,cps);
        framework.setTestRunName(runName);
    }

    // Find the run name of the test run, if it's not a set property 
    // ("framework.run.name")
    // then create a run name by submitting the run, based on language, properties.
    private String locateRunName(IFramework framework ,IConfigurationPropertyStoreService cps) throws FrameworkException {
        //*** Ensure the shared environment = true is set for Shenv runs
        String runName = AbstractManager.nulled(cps.getProperty("run", "name"));
        if (runName == null) {
            String testName = AbstractManager.nulled(cps.getProperty("run", "testbundleclass"));
            String testLanguage  = "java";
            if (testName == null) {
                testName = AbstractManager.nulled(cps.getProperty("run", "gherkintest"));
                testLanguage = "gherkin";
            }
            logger.info("Submitting test "+testName);
            runName = submitRun(framework, testName, testLanguage);
        }
        logger.info("Run name is "+runName);
        return runName;
    }

        /**
     * Submit the run and return the run name.
     * 
     * @param runBundleClass
     * @param language
     * @return The name of the run created.
     * @throws FrameworkException
     */
    protected String submitRun(IFramework framework, String runBundleClass, String language) throws FrameworkException {
        IRun run = null;
        IFrameworkRuns frameworkRuns = framework.getFrameworkRuns();

        String runId = UUID.randomUUID().toString();
        switch(language) {
            case "java": 
                String split[] = runBundleClass.split("/");
                String bundle = split[0];
                String test = split[1];
                run = frameworkRuns.submitRun("local", null, bundle, test, null, null, null, null, true, false, null, null, null, language, runId);
                break;
            case "gherkin":
                run = frameworkRuns.submitRun("local", null, null, runBundleClass, null, null, null, null, true, false, null, null, null, language, runId);
                break;
            default:
                throw new FrameworkException("Unknown language to create run");
        }

        logger.info("Allocated Run Name " + run.getName() + " to this run");

        return run.getName();
    }

    @Override
    public Properties applyOverrides(IFramework framework, IDynamicStatusStoreService dss, Properties overrideProperties) throws DynamicStatusStoreException {
        // If this is a test run, add the overrides from the run dss properties to these overrides
        overrideProperties = loadOverridePropertiesFromDss(framework, dss, overrideProperties);
        return overrideProperties ;
    }
    
    private Properties loadOverridePropertiesFromDss(IFramework framework, IDynamicStatusStoreService dss, Properties overrideProperties) throws DynamicStatusStoreException {
        // The overrides DSS property contains a JSON array of overrides in the form:
        // dss.framework.run.X.overrides=[{ "key1": "value1" }, { "key2", "value2" }]
        String runOverridesProp = "run." + framework.getTestRunName() + ".overrides";
        String runOverrides = dss.get(runOverridesProp);
        if (runOverrides != null && !runOverrides.isBlank()) {
            Property[] properties = gson.fromJson(runOverrides, Property[].class);
            for (Property override : properties) {
                String key = override.getKey();
                String value = override.getValue();
                if (logger.isTraceEnabled()) {
                    logger.trace("Setting run override " + key + "=" + value);
                }
                overrideProperties.put(key, value);
            }
        }
        return overrideProperties;
    }

}
