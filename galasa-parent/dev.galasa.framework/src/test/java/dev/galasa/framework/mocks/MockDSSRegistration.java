/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import javax.validation.constraints.NotNull;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IDynamicStatusStoreRegistration;
import dev.galasa.framework.spi.IFrameworkInitialisation;

public class MockDSSRegistration implements IDynamicStatusStoreRegistration {

    private IDynamicStatusStore store ;

    public MockDSSRegistration(IDynamicStatusStore store) {
        this.store = store ;
    }

    @Override
    public void initialise(
        @NotNull IFrameworkInitialisation frameworkInitialisation
    ) throws DynamicStatusStoreException {

        frameworkInitialisation.registerDynamicStatusStore(store);
    }

}