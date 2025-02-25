/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.properties;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Extra bundles required to implement the IMS TM Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name imstm.extra.bundles
 * 
 * @galasa.description The symbolic names of any bundles that need to be loaded
 *                     with the IMS TM Manager
 * 
 * @galasa.required No
 * 
 * @galasa.default none
 * 
 * @galasa.valid_values bundle symbolic names comma separated
 * 
 * @galasa.examples <code>imstm.extra.bundles=org.example.imstm.provisioning</code><br>
 *
 */
public class ExtraBundles extends CpsProperties {

    public static List<String> get() throws ImstmManagerException {
        try {
            List<String> bundles = getStringList(ImstmPropertiesSingleton.getCps(), "extra", "bundles");

            if (bundles.size() == 1) {
                if (bundles.get(0).equalsIgnoreCase("none")) {
                    return new ArrayList<>(0);
                }
            }
            
            return bundles;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ImstmManagerException("Problem asking CPS for the IMS TM extra bundles", e); 
        }
    }
}
