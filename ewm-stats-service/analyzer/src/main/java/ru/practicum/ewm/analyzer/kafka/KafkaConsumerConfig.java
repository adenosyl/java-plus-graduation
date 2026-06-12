package ru.practicum.ewm.analyzer.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.serialization.AvroDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, UserActionAvro>
    userActionConsumerFactory() {

        Map<String, Object> props =
                new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "analyzer"
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new AvroDeserializer<>(
                        UserActionAvro.class
                )
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String,
            UserActionAvro>
    userActionKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<
                String,
                UserActionAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                userActionConsumerFactory()
        );

        return factory;
    }

    @Bean
    public ConsumerFactory<String, EventSimilarityAvro>
    eventSimilarityConsumerFactory() {

        Map<String, Object> props =
                new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "storage"
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new AvroDeserializer<>(
                        EventSimilarityAvro.class
                )
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String,
            EventSimilarityAvro>
    eventSimilarityKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<
                String,
                EventSimilarityAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                eventSimilarityConsumerFactory()
        );

        return factory;
    }
}