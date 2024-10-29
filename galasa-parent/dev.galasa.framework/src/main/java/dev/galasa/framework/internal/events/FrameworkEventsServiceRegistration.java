/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.events;

import java.net.URI;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.IEventsServiceRegistration;
import dev.galasa.framework.spi.IFrameworkInitialisation;

@Component(service = { IEventsServiceRegistration.class })
public class FrameworkEventsServiceRegistration implements IEventsServiceRegistration {

    /**
     * This method intialises the service with the framework, managers can then
     * access this service.
     * 
     * @param frameworkInitialisation - the framework setup.
     * @throws EventsException 
     */
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws EventsException {

        try {
            URI cps = frameworkInitialisation.getBootstrapConfigurationPropertyStore();

            // If the CPS is a file, then the test run is local, so we don't want to use the kafka implementation,
            // or generate any events at all. Register this version of the EventsService which just swallows events.
            if (cps.getScheme().equals("file")) {
                frameworkInitialisation.registerEventsService(new NullEventsService());
            }
        } catch (EventsException e) {
            throw e;
        }
    }

}