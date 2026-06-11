package ru.practicum.ewm.aggregator.repository;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class EventStatisticsRepository {

    private final Map<Long, Long> eventViews =
            new HashMap<>();

    public void incrementEvent(Long eventId) {

        eventViews.merge(
                eventId,
                1L,
                Long::sum
        );
    }

    public long getViews(Long eventId) {

        return eventViews.getOrDefault(
                eventId,
                0L
        );
    }

    public Map<Long, Long> getEventViews() {
        return eventViews;
    }
}