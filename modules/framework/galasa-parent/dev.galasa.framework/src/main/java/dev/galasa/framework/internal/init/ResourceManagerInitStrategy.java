/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.init;

import dev.galasa.framework.Framework;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class ResourceManagerInitStrategy extends DefaultInitStrategy {

    @Override
    public void setTestRunName(Framework framework, IConfigurationPropertyStoreService cps) throws FrameworkException {
        framework.setTestRunName("resourceManagement");
    }

}
