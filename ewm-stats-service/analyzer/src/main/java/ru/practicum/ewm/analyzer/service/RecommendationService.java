package ru.practicum.ewm.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.analyzer.model.EventSimilarityEntity;
import ru.practicum.ewm.analyzer.model.UserActionEntity;
import ru.practicum.ewm.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.analyzer.repository.UserActionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final EventSimilarityRepository repository;
    private final UserActionRepository userActionRepository;
    private double getWeight(String actionType) {

        return switch (actionType) {

            case "ACTION_VIEW" -> 1.0;

            case "ACTION_REGISTER" -> 2.0;

            case "ACTION_LIKE" -> 3.0;

            default -> 0.0;
        };
    }

    public List<EventSimilarityEntity> getSimilarEvents(
            Long eventId,
            Long userId
    ) {

        Set<Long> viewedEvents =
                userActionRepository.findByUserId(userId)
                        .stream()
                        .map(UserActionEntity::getEventId)
                        .collect(Collectors.toSet());

        return repository.findByEventAOrderByScoreDesc(
                        eventId
                )
                .stream()
                .filter(similarity ->
                        !viewedEvents.contains(
                                similarity.getEventB()
                        )
                )
                .toList();
    }

    public double getInteractionsCount(Long eventId) {

        Map<Long, Double> userWeights =
                new HashMap<>();

        for (UserActionEntity action :
                userActionRepository.findByEventId(eventId)) {

            double weight =
                    getWeight(action.getActionType());

            userWeights.merge(
                    action.getUserId(),
                    weight,
                    Math::max
            );
        }

        return userWeights.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public List<EventSimilarityEntity> getRecommendationsForUser(
            Long userId
    ) {

        Set<Long> viewedEvents =
                userActionRepository.findByUserId(userId)
                        .stream()
                        .map(UserActionEntity::getEventId)
                        .collect(Collectors.toSet());

        List<EventSimilarityEntity> recommendations =
                new ArrayList<>();

        for (Long eventId : viewedEvents) {

            repository.findByEventAOrderByScoreDesc(eventId)
                    .stream()
                    .filter(similarity ->
                            !viewedEvents.contains(
                                    similarity.getEventB()
                            )
                    )
                    .forEach(recommendations::add);
        }

        recommendations.sort(
                Comparator.comparing(
                        EventSimilarityEntity::getScore
                ).reversed()
        );

        Map<Long, EventSimilarityEntity> unique =
                new LinkedHashMap<>();

        for (EventSimilarityEntity recommendation : recommendations) {
            unique.putIfAbsent(
                    recommendation.getEventB(),
                    recommendation
            );
        }

        return new ArrayList<>(unique.values());
    }
}