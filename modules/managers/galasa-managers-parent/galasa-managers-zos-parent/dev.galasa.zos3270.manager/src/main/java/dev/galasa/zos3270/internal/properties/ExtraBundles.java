/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.properties;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos3270.Zos3270ManagerException;

/**
 * Extra bundles required to implement the z/OS 3270 Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos3270.extra.bundles
 * 
 * @galasa.description The symbolic names of any bundles that need to be loaded
 *                     with the z/OS 3270 Manager
 * 
 * @galasa.required No
 * 
 * @galasa.default dev.galasa.textscan.manager
 * 
 * @galasa.valid_values bundle symbolic names comma separated
 * 
 * @galasa.examples <code>zos3270.extra.bundles=dev.galasa.textscan.manager</code><br>
 *
 */
public class ExtraBundles extends CpsProperties {

    public static List<String> get() throws Zos3270ManagerException {
        try {
            List<String> list = getStringList(Zos3270PropertiesSingleton.cps(), "extra", "bundles");

            if (list.isEmpty()) {
                list = new ArrayList<>(3);
                list.add("dev.galasa.textscan.manager");
            } else if (list.size() == 1) {
                if (list.get(0).equalsIgnoreCase("none")) {
                    return new ArrayList<>(0);
                }
            }
            
            return list;
        } catch (ConfigurationPropertyStoreException e) {
            throw new Zos3270ManagerException("Problem asking CPS for the z/OS 3270 extra bundles", e); 
        }
    }
}
