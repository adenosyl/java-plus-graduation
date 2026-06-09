package ru.practicum.ewm.collector.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserActionProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String payload) {
        kafkaTemplate.send(
                "stats.user-actions.v1",
                payload
        );
    }
}