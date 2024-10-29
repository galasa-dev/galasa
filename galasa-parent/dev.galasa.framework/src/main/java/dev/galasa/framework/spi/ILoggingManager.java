/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.util.List;

/**
 * <p>
 * This is the interface used by the Logging Managers to get test information
 * from the active managers.
 * </p>
 *
 *  
 *
 */
public interface ILoggingManager {
    /**
     * Overrides Galasa test tooling in logging.
     * Return null if not used
     * 
     * @return test tooling name
     */
    public String getTestTooling();

    /**
     * Overrides Galasa test type in logging
     * Return null if not used
     * 
     * @return test type name
     */
    public String getTestType();

    /**
     * Set Testing Environment string for logging
     * Return null if not used
     * 
     * @return testing environment name
     */
    public String getTestingEnvironment();

    /**
     * Set Product Release string for logging
     * Return null if not used
     * 
     * @return product release name
     */
    public String getProductRelease();

    /**
     * Set Build Level string for logging
     * Return null if not used
     * 
     * @return build level name
     */
    public String getBuildLevel();

    /**
     * Set Custom Build string for logging
     * Return null if not used
     * 
     * @return custom build name
     */
    public String getCustomBuild();

    /**
     * Add list of Testing Areas to test for logging
     * Return null if not used
     * 
     * @return list of testing areas
     */
    public List<String> getTestingAreas();

    /**
     * Add list of tags to test for logging
     * Return null if not used
     * 
     * @return list of testing tags
     */
    public List<String> getTags();
}