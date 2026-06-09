package ru.practicum.ewm.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventSimilarityProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String payload) {
        kafkaTemplate.send(
                "stats.events-similarity.v1",
                payload
        );
    }
}