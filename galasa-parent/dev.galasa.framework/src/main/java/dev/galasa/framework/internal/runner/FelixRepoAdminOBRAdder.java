/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class FelixRepoAdminOBRAdder {

    private Log logger = LogFactory.getLog(FelixRepoAdminOBRAdder.class);
    private IConfigurationPropertyStoreService cps;
    private RepositoryAdmin repoAdmin;

    public FelixRepoAdminOBRAdder(RepositoryAdmin repoAdmin, IConfigurationPropertyStoreService cps) {
        this.repoAdmin = repoAdmin ;
        this.cps = cps ;
    }

    public void addOBRsToRepoAdmin(  String streamName, String runOBRLIst ) throws TestRunException {
        String testOBR = getTestOBRFromStream(streamName);
        testOBR = getOverriddenValue(testOBR, runOBRLIst);
        addOBRsToRepoAdmin(testOBR, repoAdmin);
    }

    private String getTestOBRFromStream(String streamName) throws TestRunException {
        String testOBR = null ;
        if (streamName != null) {
            logger.debug("Loading test stream " + streamName);
            try {
                testOBR = this.cps.getProperty("test.stream", "obr", streamName);
            } catch (Exception e) {
                throw new TestRunException("Unable to load stream " + streamName + " settings", e);
            }
        }
        return testOBR ;
    }

    private void addOBRsToRepoAdmin(String testOBR, RepositoryAdmin repoAdmin) throws TestRunException {
        if (testOBR != null) {
            logger.debug("Loading test obr repository " + testOBR);
            try {
                String[] testOBRs = testOBR.split("\\,");
                for(String obr : testOBRs) {
                    obr = obr.trim();
                    if (!obr.isEmpty()) {
                        repoAdmin.addRepository(obr);
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to load specified OBR " + testOBR, e);
                throw new TestRunException("Unable to load specified OBR " + testOBR, e);
            }
        }
    }
    private String getOverriddenValue(String existingValue, String possibleOverrideValue) {
        String result = existingValue ;
        String possibleNulledValue = AbstractManager.nulled(possibleOverrideValue);
        if (possibleNulledValue != null) {
            result = possibleNulledValue;
        }
        return result ;
    }
}
