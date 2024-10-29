/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.events.kafka.internal;

import java.net.URI;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IEventsServiceRegistration;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.SystemEnvironment;

@Component(service = { IEventsServiceRegistration.class })
public class KafkaEventsServiceRegistration implements IEventsServiceRegistration {

    private final String NAMESPACE = "kafka";

    private final String TOKEN_NAME = "GALASA_EVENT_STREAMS_TOKEN";
    
    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
            throws EventsException {

        try {

            URI cps = frameworkInitialisation.getBootstrapConfigurationPropertyStore();

            // If the CPS is ETCD, then register this version of the EventsService
            if (cps.getScheme().equals("etcd")) {
                IFramework framework = frameworkInitialisation.getFramework();
                String runName = framework.getTestRunName();

                SystemEnvironment env = new SystemEnvironment();
                String authToken = env.getenv(TOKEN_NAME);
                KafkaEventProducerFactory producerFactory = new KafkaEventProducerFactory(authToken, runName);
                IConfigurationPropertyStoreService cpsService = framework.getConfigurationPropertyService(NAMESPACE);

                frameworkInitialisation.registerEventsService(new KafkaEventsService(cpsService, producerFactory));
            }

        } catch (Exception e) {
            throw new KafkaException("Unable to register the Kafka Events Service", e);
        }

    }

}
