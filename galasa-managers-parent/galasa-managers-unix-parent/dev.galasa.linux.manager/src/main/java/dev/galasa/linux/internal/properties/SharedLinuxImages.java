/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.linux.LinuxManagerException;

/**
 * Shared Linux images
 * <p>
 * A comma separated list of what servers are available to allocate to tests.
 * </p>
 * <p>
 * Example:-<br>
 * <br>
 * linux.shared.servers=GALLNX01,GALLNX02<br>
 * </p>
 * <p>
 * There are no defaults
 * </p>
 * 
 *  
 *
 */
public class SharedLinuxImages extends CpsProperties {

    public static @NotNull List<String> get() throws ConfigurationPropertyStoreException, LinuxManagerException {
        return getStringList(LinuxPropertiesSingleton.cps(), "shared", "images");
    }

}
