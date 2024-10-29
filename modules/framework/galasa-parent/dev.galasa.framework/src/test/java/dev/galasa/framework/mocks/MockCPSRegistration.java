/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import javax.validation.constraints.NotNull;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStore;
import dev.galasa.framework.spi.IConfigurationPropertyStoreRegistration;
import dev.galasa.framework.spi.IFrameworkInitialisation;

public class MockCPSRegistration implements IConfigurationPropertyStoreRegistration {

    private IConfigurationPropertyStore store ;

    public MockCPSRegistration(IConfigurationPropertyStore store) {
        this.store = store ;
    }

    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
            throws ConfigurationPropertyStoreException {

        frameworkInitialisation.registerConfigurationPropertyStore(store);
    }

}