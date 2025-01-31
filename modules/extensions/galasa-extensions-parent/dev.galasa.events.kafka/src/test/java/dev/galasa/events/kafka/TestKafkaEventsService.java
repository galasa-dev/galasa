/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.events.kafka;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import dev.galasa.events.kafka.internal.KafkaEventsService;
import dev.galasa.events.kafka.internal.KafkaException;
import dev.galasa.events.kafka.mocks.MockEventProducer;
import dev.galasa.events.kafka.mocks.MockEventProducerFactory;
import dev.galasa.extensions.common.mocks.MockEnvironment;
import dev.galasa.extensions.common.mocks.cps.MockConfigurationPropertyStoreService;
import dev.galasa.extensions.common.mocks.events.MockEvent;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.events.IEvent;

public class TestKafkaEventsService {

    private KafkaEventsService createKafkaEventsService() {
        Map<String,String> props = new HashMap<String,String>();
        props.put("bootstrap.servers", "broker1,broker2");
        MockConfigurationPropertyStoreService mockCps = new MockConfigurationPropertyStoreService(props);
        MockEnvironment mockEnv = new MockEnvironment();
        MockEventProducerFactory mockFactory = new MockEventProducerFactory(mockEnv);
        KafkaEventsService kafkaEventsService = new KafkaEventsService(mockCps, mockFactory);
        return kafkaEventsService;
    }

    @Test
    public void testCanCreateAKafkaEventsService() throws ConfigurationPropertyStoreException {
        createKafkaEventsService();
    }

    @Test
    public void testCanProduceAnEventWithValidTopicAndValidEvent() throws Exception {
        // Given...
        KafkaEventsService kafkaEventsService = createKafkaEventsService();

        String topic = "Topic.MyTopic";
        MockEvent mockEvent = new MockEvent("2024-06-16T12:49:01.921998Z", "This is a mock event!");

        int numProducersBefore = kafkaEventsService.getProducers().size();
        assertThat(numProducersBefore).isEqualTo(0);

        // When...
        kafkaEventsService.produceEvent(topic, mockEvent);

        // Then...
        int numProducersAfter = kafkaEventsService.getProducers().size();
        assertThat(numProducersAfter).isEqualTo(1);

        MockEventProducer mockProducer = (MockEventProducer) kafkaEventsService.getProducers().get(topic);
        assertThat(mockProducer).isNotNull();

        List<IEvent> events = mockProducer.getEvents();
        assertThat(events).contains(mockEvent);
    }

    @Test
    public void testProduceTwoEventsWithSameTopicUsesCachedProducer() throws EventsException {
        // Given...
        KafkaEventsService kafkaEventsService = createKafkaEventsService();

        String topic = "Topic.MyTopic";
        MockEvent mockEvent1 = new MockEvent("2024-06-16T12:49:01.921998Z", "This is a mock event!");
        MockEvent mockEvent2 = new MockEvent("2024-06-16T12:49:01.921998Z", "This is another mock event!");

        int numProducersBefore = kafkaEventsService.getProducers().size();
        assertThat(numProducersBefore).isEqualTo(0);

        // When...
        kafkaEventsService.produceEvent(topic, mockEvent1);

        // Then...
        int numProducersAfterEvent1 = kafkaEventsService.getProducers().size();
        assertThat(numProducersAfterEvent1).isEqualTo(1);

        // When...
        kafkaEventsService.produceEvent(topic, mockEvent2);

        // Then...
        int numProducersAfterEvent2 = kafkaEventsService.getProducers().size();
        assertThat(numProducersAfterEvent2).isEqualTo(1);
    }

    @Test
    public void testProduceTwoEventsWithDifferentTopicsUseDifferentProducers() throws EventsException {
        // Given...
        KafkaEventsService kafkaEventsService = createKafkaEventsService();

        String topic1 = "Topic.MyTopic1";
        MockEvent mockEvent1 = new MockEvent("2024-06-16T12:49:01.921998Z", "This is a mock event about one topic!");

        String topic2 = "Topic.MyTopic2";
        MockEvent mockEvent2 = new MockEvent("2024-06-16T12:49:01.921998Z", "This is a mock event about a different topic!");

        int numProducersBefore = kafkaEventsService.getProducers().size();
        assertThat(numProducersBefore).isEqualTo(0);

        // When...
        kafkaEventsService.produceEvent(topic1, mockEvent1);

        // Then...
        int numProducersAfterEvent1 = kafkaEventsService.getProducers().size();
        assertThat(numProducersAfterEvent1).isEqualTo(1);

        // When...
        kafkaEventsService.produceEvent(topic2, mockEvent2);

        // Then...
        int numProducersAfterEvent2 = kafkaEventsService.getProducers().size();
        assertThat(numProducersAfterEvent2).isEqualTo(2);
    }

    @Test
    public void testProduceEventWithEmptyTopicReturnsError() throws EventsException {
        // Given...
        KafkaEventsService kafkaEventsService = createKafkaEventsService();

        String topic = "";
        MockEvent mockEvent = new MockEvent("2024-06-16T12:49:01.921998Z", "This is a mock event!");

        int numProducersBefore = kafkaEventsService.getProducers().size();
        assertThat(numProducersBefore).isEqualTo(0);

        // When...
        KafkaException thrown = catchThrowableOfType(() -> kafkaEventsService.produceEvent(topic, mockEvent), KafkaException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Topic is empty");

        int numProducersAfter = kafkaEventsService.getProducers().size();
        assertThat(numProducersAfter).isEqualTo(0);
    }

    @Test
    public void testCanShutdown() throws EventsException {
        // Given...
        KafkaEventsService kafkaEventsService = createKafkaEventsService();

        String topic = "Topic.MyTopic";
        MockEvent mockEvent = new MockEvent("2024-06-16T12:49:01.921998Z", "This is a mock event!");

        int numProducersBefore = kafkaEventsService.getProducers().size();
        assertThat(numProducersBefore).isEqualTo(0);

        // When...
        kafkaEventsService.produceEvent(topic, mockEvent);

        // Then...
        int numProducersAfterEvent = kafkaEventsService.getProducers().size();
        assertThat(numProducersAfterEvent).isEqualTo(1);

        // When...
        kafkaEventsService.shutdown();

        // Then...
        int numProducersAfterShutdown = kafkaEventsService.getProducers().size();
        assertThat(numProducersAfterShutdown).isEqualTo(0);
    }
    
}
