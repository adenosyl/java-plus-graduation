package ru.practicum.ewm.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.aggregator.service.UserActionAggregationService;

@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final UserActionAggregationService service;

    @KafkaListener(
            topics = "stats.user-actions.v1",
            groupId = "aggregator"
    )
    public void consume(String message) {
        service.process(message);
    }
}