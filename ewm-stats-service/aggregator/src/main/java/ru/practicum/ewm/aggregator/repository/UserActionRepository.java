package ru.practicum.ewm.aggregator.repository;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
public class UserActionRepository {

    private final Map<Long, Set<Long>> userEvents = new HashMap<>();

    public void addAction(Long userId, Long eventId) {

        userEvents
                .computeIfAbsent(userId, id -> new HashSet<>())
                .add(eventId);
    }

    public Map<Long, Set<Long>> getUserEvents() {
        return userEvents;
    }
}