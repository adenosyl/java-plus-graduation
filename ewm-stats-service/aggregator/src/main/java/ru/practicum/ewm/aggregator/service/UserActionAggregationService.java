package ru.practicum.ewm.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.aggregator.kafka.EventSimilarityProducer;

@Service
@RequiredArgsConstructor
public class UserActionAggregationService {

    private final EventSimilarityProducer producer;

    public void process(String message) {

        System.out.println("Processing: " + message);

        producer.send(
                "TEST_SIMILARITY_MESSAGE"
        );
    }
}