package ru.practicum.ewm.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.aggregator.service.UserActionAggregationService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final UserActionAggregationService service;

    @KafkaListener(
            topics = "stats.user-actions.v1",
            groupId = "aggregator"
    )
    public void consume(UserActionAvro action) {

        service.process(action);
    }
}