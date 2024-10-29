/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import javax.validation.constraints.NotNull;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IResultArchiveStoreRegistration;
import dev.galasa.framework.spi.IResultArchiveStoreService;
import dev.galasa.framework.spi.ResultArchiveStoreException;

public class MockRASRegistration implements IResultArchiveStoreRegistration {

    private @NotNull IResultArchiveStoreService storeService ;

    public MockRASRegistration(IResultArchiveStoreService storeService) {
        this.storeService = storeService ;
    }

    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
            throws ResultArchiveStoreException {

        frameworkInitialisation.registerResultArchiveStoreService(storeService);
    }

}