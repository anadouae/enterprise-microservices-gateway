package com.douae.ems.query.config;

import com.douae.ems.common.event.DomainEvent;
import com.douae.ems.common.event.OrderPlacedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, OrderPlacedEvent> orderPlacedConsumerFactory() {
        Map<String, Object> props = baseProps();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderPlacedEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> orderPlacedListenerFactory(
            ConsumerFactory<String, OrderPlacedEvent> orderPlacedConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderPlacedConsumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DomainEvent> domainEventConsumerFactory() {
        Map<String, Object> props = baseProps();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DomainEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> domainEventListenerFactory(
            ConsumerFactory<String, DomainEvent> domainEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, DomainEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(domainEventConsumerFactory);
        return factory;
    }

    private Map<String, Object> baseProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.douae.ems.common.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return props;
    }
}
