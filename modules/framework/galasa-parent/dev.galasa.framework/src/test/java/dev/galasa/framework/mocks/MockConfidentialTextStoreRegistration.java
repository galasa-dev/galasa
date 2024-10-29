/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfidentialTextException;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfidentialTextServiceRegistration;
import dev.galasa.framework.spi.IFrameworkInitialisation;

public class MockConfidentialTextStoreRegistration implements IConfidentialTextServiceRegistration {

    private IConfidentialTextService service ;

    public MockConfidentialTextStoreRegistration(IConfidentialTextService service) {
        this.service = service ;
    }

    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws ConfidentialTextException {
        frameworkInitialisation.registerConfidentialTextService(service);
    }

}
