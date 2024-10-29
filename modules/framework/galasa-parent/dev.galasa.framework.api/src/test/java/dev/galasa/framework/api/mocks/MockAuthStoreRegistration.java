/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.mocks;

import dev.galasa.framework.spi.IApiServerInitialisation;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreRegistration;
import dev.galasa.framework.spi.auth.AuthStoreException;

public class MockAuthStoreRegistration implements IAuthStoreRegistration {

    private IAuthStore store;

    public MockAuthStoreRegistration(IAuthStore store) {
        this.store = store;
    }

    @Override
    public void initialise(IApiServerInitialisation frameworkInitialisation) throws AuthStoreException {
        frameworkInitialisation.registerAuthStore(store);
    }
}