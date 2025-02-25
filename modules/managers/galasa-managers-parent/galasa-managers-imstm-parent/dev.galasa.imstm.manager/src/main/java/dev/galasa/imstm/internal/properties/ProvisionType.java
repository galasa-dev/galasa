/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.properties;

import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class ProvisionType extends CpsProperties {

    public static String get() throws ImstmManagerException {
        return getStringWithDefault(ImstmPropertiesSingleton.getCps(), "provisioned", "provision", "type").toUpperCase();
    }
}
