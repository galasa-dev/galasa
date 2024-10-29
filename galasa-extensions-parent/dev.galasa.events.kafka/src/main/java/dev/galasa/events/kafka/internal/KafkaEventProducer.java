/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.events.kafka.internal;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import dev.galasa.framework.spi.IEventProducer;
import dev.galasa.framework.spi.events.IEvent;

public class KafkaEventProducer implements IEventProducer {

    private final KafkaProducer<String, String> producer;
    private final String topic;

    public KafkaEventProducer(Properties properties, String topic) {

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        producer.initTransactions();
        this.producer = producer;

        this.topic = topic;
    }

    public void sendEvent(IEvent event){
        producer.beginTransaction();
        producer.send(new ProducerRecord<>(topic, event.toString()));
        producer.commitTransaction();
    }

    public void close(){
        producer.close();
    }
    
}
