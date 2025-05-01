/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ivts.core;

import org.apache.commons.logging.Log;

import dev.galasa.Summary;
import dev.galasa.Tags;
import dev.galasa.Test;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.RunName;

@Test
@Summary("Ensure the basic functions are working in the Core Manager")
@Tags({"core","ivt"})
public class CoreManagerIVT {
    
    @Logger
    public Log logger;
    
    @RunName
    public String runName;
    
    @CoreManager
    public ICoreManager coreManager;
    
    @Test
    public void checkLogger() throws Exception {
        if (logger == null) {
            throw new Exception("Logger field is null, should have been filled by the Core Manager");
        }
        logger.info("The Logger field has been correctly initialised");
    }

    @Test
    public void checkRunName() throws Exception {
        if (runName == null || runName.trim().isEmpty()) {
            throw new Exception("Run Name field is null, should have been filled by the Core Manager");
        }
        logger.info("The Run Name  field has been correctly initialised = '" + runName + "'");
    }

    @Test
    public void checkCoreManager() throws Exception {
        if (coreManager == null) {
            throw new Exception("Core Manager field is null, should have been filled by the Core Manager");
        }
        logger.info("The Core Manager field has been correctly initialised");
    }

}
