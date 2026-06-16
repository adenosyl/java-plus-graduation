package ru.practicum.ewm.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Service
@RequiredArgsConstructor
public class EventSimilarityProducer {

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    public void send(EventSimilarityAvro similarity) {
        kafkaTemplate.send(
                "stats.events-similarity.v1",
                similarity
        );
    }
}