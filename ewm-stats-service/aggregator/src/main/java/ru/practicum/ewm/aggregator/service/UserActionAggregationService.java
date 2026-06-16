package ru.practicum.ewm.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.aggregator.kafka.EventSimilarityProducer;
import ru.practicum.ewm.aggregator.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor
public class UserActionAggregationService {

    private final UserActionRepository repository;
    private final EventSimilarityProducer producer;

    public void process(UserActionAvro action) {

        System.out.println("Processing: " + action);

        Long userId = action.getUserId();
        Long eventId = action.getEventId();

        double weight =
                getWeight(
                        action.getActionType().toString()
                );

        repository.addAction(
                userId,
                eventId,
                weight
        );

        System.out.println(repository.getUserEvents());

        printPairs(userId);
    }

    private void printPairs(Long userId) {

        Long[] eventArray =
                repository.getUserEvents(userId)
                        .keySet()
                        .toArray(new Long[0]);

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

        double sa = calculateSum(first);

        double sb = calculateSum(second);

        double sMin = calculateMinSum(
                first,
                second
        );

        if (sa == 0 || sb == 0) {
            return;
        }

        double score =
                sMin /
                        Math.sqrt(
                                sa * sb
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

    private double calculateSum(Long eventId) {

        double sum = 0.0;

        for (var userEvents : repository.getAll().values()) {

            sum += userEvents.getOrDefault(
                    eventId,
                    0.0
            );
        }

        return sum;
    }

    private double calculateMinSum(
            Long first,
            Long second
    ) {

        double sum = 0.0;

        for (var userEvents : repository.getAll().values()) {

            double firstWeight =
                    userEvents.getOrDefault(
                            first,
                            0.0
                    );

            double secondWeight =
                    userEvents.getOrDefault(
                            second,
                            0.0
                    );

            sum += Math.min(
                    firstWeight,
                    secondWeight
            );
        }

        return sum;
    }

    private double getWeight(
            String actionType
    ) {

        return switch (actionType) {

            case "ACTION_VIEW" -> 1.0;

            case "ACTION_REGISTER" -> 2.0;

            case "ACTION_LIKE" -> 3.0;

            default -> 0.0;
        };
    }
}