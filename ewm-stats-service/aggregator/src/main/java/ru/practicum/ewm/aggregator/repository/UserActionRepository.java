package ru.practicum.ewm.aggregator.repository;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserActionRepository {

    private final Map<Long, Map<Long, Double>> userEvents =
            new HashMap<>();

    public void addAction(
            Long userId,
            Long eventId,
            double weight
    ) {

        userEvents
                .computeIfAbsent(
                        userId,
                        id -> new HashMap<>()
                )
                .merge(
                        eventId,
                        weight,
                        Math::max
                );
    }

    public Map<Long, Map<Long, Double>> getUserEvents() {
        return userEvents;
    }

    public Map<Long, Double> getUserEvents(
            Long userId
    ) {

        return userEvents.getOrDefault(
                userId,
                new HashMap<>()
        );
    }

    public Map<Long, Map<Long, Double>> getAll() {
        return userEvents;
    }
}