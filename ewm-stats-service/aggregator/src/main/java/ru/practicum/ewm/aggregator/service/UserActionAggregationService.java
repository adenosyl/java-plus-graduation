package ru.practicum.ewm.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.aggregator.kafka.EventSimilarityProducer;
import ru.practicum.ewm.aggregator.repository.SimilarityRepository;
import ru.practicum.ewm.aggregator.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor
public class UserActionAggregationService {

    private final UserActionRepository repository;
    private final SimilarityRepository similarityRepository;
    private final EventSimilarityProducer producer;

    public void process(UserActionAvro action) {

        System.out.println("Processing: " + action);

        Long userId = action.getUserId();
        Long eventId = action.getEventId();

        repository.addAction(userId, eventId);

        System.out.println(repository.getUserEvents());

        printPairs();
    }

    private void printPairs() {

        repository.getUserEvents()
                .values()
                .forEach(events -> {

                    for (Long first : events) {

                        for (Long second : events) {

                            if (!first.equals(second)) {
                                createSimilarity(first, second);
                            }
                        }
                    }
                });
    }

    private void createSimilarity(Long first, Long second) {

        if (!similarityRepository.add(first, second)) {
            return;
        }

        EventSimilarityAvro similarity =
                EventSimilarityAvro.newBuilder()
                        .setEventA(first)
                        .setEventB(second)
                        .setScore(1.0)
                        .setTimestamp(System.currentTimeMillis())
                        .build();

        System.out.println(
                "Created similarity: " + similarity
        );

        producer.send(similarity.toString());
    }
}