package ru.practicum.ewm.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.aggregator.kafka.EventSimilarityProducer;
import ru.practicum.ewm.aggregator.repository.EventStatisticsRepository;
import ru.practicum.ewm.aggregator.repository.PairStatisticsRepository;
import ru.practicum.ewm.aggregator.repository.SimilarityRepository;
import ru.practicum.ewm.aggregator.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserActionAggregationService {

    private final UserActionRepository repository;
    private final SimilarityRepository similarityRepository;
    private final EventSimilarityProducer producer;
    private final EventStatisticsRepository eventStatisticsRepository;
    private final PairStatisticsRepository pairStatisticsRepository;

    public void process(UserActionAvro action) {

        System.out.println("Processing: " + action);

        Long userId = action.getUserId();
        Long eventId = action.getEventId();

        repository.addAction(userId, eventId);

        eventStatisticsRepository.incrementEvent(
                eventId
        );

        System.out.println(
                eventStatisticsRepository.getEventViews()
        );

        System.out.println(repository.getUserEvents());

        printPairs(userId);
    }

    private void printPairs(Long userId) {

        Set<Long> events =
                repository.getUserEvents(userId);

        Long[] eventArray =
                events.toArray(new Long[0]);

        for (int i = 0; i < eventArray.length; i++) {

            for (int j = i + 1; j < eventArray.length; j++) {

                createSimilarity(
                        eventArray[i],
                        eventArray[j]
                );
            }
        }
    }

    private void createSimilarity(Long first, Long second) {

        pairStatisticsRepository.increment(
                first,
                second
        );

        long pairCount =
                pairStatisticsRepository.getCount(
                        first,
                        second
                );

        long viewsA =
                eventStatisticsRepository.getViews(
                        first
                );

        long viewsB =
                eventStatisticsRepository.getViews(
                        second
                );

        double score =
                (double) pairCount /
                        Math.sqrt(
                                viewsA * viewsB
                        );

        EventSimilarityAvro similarity =
                EventSimilarityAvro.newBuilder()
                        .setEventA(first)
                        .setEventB(second)
                        .setScore(score)
                        .setTimestamp(System.currentTimeMillis())
                        .build();

        System.out.println(
                "score(" + first + "," + second + ") = "
                        + score
        );

        producer.send(similarity);
    }
}