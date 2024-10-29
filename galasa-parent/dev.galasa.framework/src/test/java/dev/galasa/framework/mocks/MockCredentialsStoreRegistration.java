/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import javax.validation.constraints.NotNull;


import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStore;
import dev.galasa.framework.spi.creds.ICredentialsStoreRegistration;

public class MockCredentialsStoreRegistration implements ICredentialsStoreRegistration {

    private ICredentialsStore store ;

    public MockCredentialsStoreRegistration(ICredentialsStore store ){
        this.store = store ;
    }

    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws CredentialsException {
        frameworkInitialisation.registerCredentialsStore(store);
    }

}
