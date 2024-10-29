/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.events.kafka.internal;

import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IEventProducer;
import dev.galasa.framework.spi.IEventsService;
import dev.galasa.framework.spi.events.IEvent;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KafkaEventsService implements IEventsService {

    private final Log logger = LogFactory.getLog(getClass());

    private IConfigurationPropertyStoreService cps;

    private IEventProducerFactory producerFactory;

    // The EventProducers are cached so they can be reused for performance
    // Keyed on the name of the topic as one EventProducer is made for each topic
    // Note: Private but getter method is so unit tests can access this.
    private Map<String, IEventProducer> producers = new HashMap<String, IEventProducer>();

    public KafkaEventsService(IConfigurationPropertyStoreService cps, IEventProducerFactory producerFactory) {
        this.cps = cps;
        this.producerFactory = producerFactory;
    }

    @Override
    public void produceEvent(String topic, IEvent event) throws EventsException {

        if (topic == null || topic.isEmpty()) {
            throw new KafkaException("Topic is empty");
        }

        IEventProducer producer = producers.get(topic);
    
        if (producer == null) {

            synchronized (producers) {

                producer = producers.get(topic);
        
                if (producer == null) {
                    logger.info("Creating a new producer as one does not exist for the topic " + topic);
     
                    Properties properties = this.producerFactory.createProducerConfig(cps, topic);
                    
                    producer = this.producerFactory.createProducer(properties, topic);
                    producers.put(topic, producer);
                } else {
                    logger.info("Using the cached producer for the topic " + topic);
                }
            }

        }

        producer.sendEvent(event);
    }

    @Override
    public void shutdown() {
        logger.info("Shutting down all cached producers");
        for (Map.Entry<String, IEventProducer> entry : producers.entrySet()) {
            entry.getValue().close();
        }
        producers.clear();
    }

    public Map<String, IEventProducer> getProducers() {
        return producers;
    }
    
}
