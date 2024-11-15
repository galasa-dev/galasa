/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.events.kafka.mocks;

import java.util.Properties;

import dev.galasa.events.kafka.internal.IEventProducerFactory;
import dev.galasa.events.kafka.internal.KafkaException;
import dev.galasa.extensions.common.mocks.MockEnvironment;
import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class MockEventProducerFactory implements IEventProducerFactory {

    private MockEnvironment env;

    public MockEventProducerFactory(MockEnvironment env) {
        this.env = env;
    }

    @Override
    public MockEventProducer createProducer(Properties properties, String topic) throws EventsException {
        MockEventProducer producer = new MockEventProducer(properties, topic);
        return producer;
    }

    @Override
    public Properties createProducerConfig(IConfigurationPropertyStoreService cps, String topic) throws KafkaException {
        Properties properties = new Properties();
        properties.put("topic", topic);
        return properties;

    }
    
}
