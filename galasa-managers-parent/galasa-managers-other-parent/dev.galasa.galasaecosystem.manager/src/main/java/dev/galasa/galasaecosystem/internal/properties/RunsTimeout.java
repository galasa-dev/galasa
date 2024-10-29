/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;

/**
 * Timeout for Galasa Ecosystem manager nested test runs
 *
 * In minutes, how long the Galasa Ecosystem manager should wait for nested runs to complete before
 * timing out. The default timeout value is 3 minutes.
 *
 * The property is:-
 * <code>galasaecosystem.runs.timeout</code>
 *
 */
public class RunsTimeout extends CpsProperties {

    private static final int DEFAULT_TIMEOUT_MINUTES = 3;

    public static int get() throws GalasaEcosystemManagerException {
        return getIntWithDefault(GalasaEcosystemPropertiesSingleton.cps(), DEFAULT_TIMEOUT_MINUTES, "runs", "timeout");
    }

}
