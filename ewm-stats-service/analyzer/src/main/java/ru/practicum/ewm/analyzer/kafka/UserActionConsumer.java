package ru.practicum.ewm.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.analyzer.model.UserActionEntity;
import ru.practicum.ewm.analyzer.repository.UserActionRepository;

@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final UserActionRepository repository;

    @KafkaListener(
            topics = "stats.user-actions.v1",
            groupId = "analyzer",
            containerFactory =
                    "userActionKafkaListenerContainerFactory"
    )

    public void consume(UserActionAvro action) {

        UserActionEntity entity =
                UserActionEntity.builder()
                        .userId(action.getUserId())
                        .eventId(action.getEventId())
                        .actionType(action.getActionType().toString())
                        .timestamp(action.getTimestamp())
                        .build();

        repository.save(entity);

        System.out.println(
                "Saved action: " + entity
        );
    }
}