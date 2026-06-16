package ru.practicum.ewm.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.analyzer.model.EventSimilarityEntity;
import ru.practicum.ewm.analyzer.repository.EventSimilarityRepository;

@Component
@RequiredArgsConstructor
public class EventSimilarityConsumer {

    private final EventSimilarityRepository repository;

    @KafkaListener(
            topics = "stats.events-similarity.v1",
            groupId = "storage",
            containerFactory =
                    "eventSimilarityKafkaListenerContainerFactory"
    )

    public void consume(EventSimilarityAvro similarity) {

        EventSimilarityEntity entity =
                EventSimilarityEntity.builder()
                        .eventA(similarity.getEventA())
                        .eventB(similarity.getEventB())
                        .score(similarity.getScore())
                        .timestamp(similarity.getTimestamp())
                        .build();

        repository.save(entity);

        System.out.println(
                "Saved similarity: " + entity
        );
    }
}