/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.properties;

import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Developer Supplied Environment - IMS TM System - Version
 * 
 * @galasa.cps.property
 * 
 * @galasa.name imstm.dse.tag.[TAG].version
 * 
 * @galasa.description Provides the version of the IMS TM system to the DSE provisioner.  
 * 
 * @galasa.required Only requires setting if the test requests it.
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid V.R.M version format, eg 15.5.0
 * 
 * @galasa.examples 
 * <code>imstm.dse.tag.PRIMARY.version=15.5.0</code><br>
 *
 */
public class DseVersion extends CpsProperties {

    public static ProductVersion get(String tag) throws ImstmManagerException {
        String version = null;
        try {
            version = getStringNulled(ImstmPropertiesSingleton.getCps(), "dse.tag", "version", tag);
            if (version == null) {
                return null;
            } else {
                return ProductVersion.parse(version);
            }
        } catch (ImstmManagerException e) {
            // Prevent ImstmManagerExceptions from being caught by the more generic ManagerException
            throw e;
        } catch (ManagerException e) {
            throw new ImstmManagerException("Failed to parse the IMS version '" + version + "' for tag '" + tag + "', should be a valid V.R.M version format, for example 15.5.0", e); 
        } catch (ConfigurationPropertyStoreException e) {
            throw new ImstmManagerException("Problem accessing the CPS for the IMS version, for tag " + tag, e);
        }
    }
}
