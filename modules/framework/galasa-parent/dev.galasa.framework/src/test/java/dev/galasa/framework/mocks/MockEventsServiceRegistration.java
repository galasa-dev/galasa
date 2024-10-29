/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.IEventsService;
import dev.galasa.framework.spi.IEventsServiceRegistration;
import dev.galasa.framework.spi.IFrameworkInitialisation;

public class MockEventsServiceRegistration implements IEventsServiceRegistration {

    private IEventsService service;

    public MockEventsServiceRegistration(IEventsService service) {
        this.service = service;
    }

    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws EventsException {
        frameworkInitialisation.registerEventsService(service);
    }
    
}
