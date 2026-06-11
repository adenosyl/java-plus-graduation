package ru.practicum.ewm.aggregator.repository;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class PairStatisticsRepository {

    private final Map<String, Long> pairViews =
            new HashMap<>();

    public void increment(Long first, Long second) {

        pairViews.merge(
                key(first, second),
                1L,
                Long::sum
        );
    }

    public long getCount(Long first, Long second) {

        return pairViews.getOrDefault(
                key(first, second),
                0L
        );
    }

    public Map<String, Long> getPairViews() {
        return pairViews;
    }

    private String key(Long first, Long second) {

        return Math.min(first, second)
                + ":"
                + Math.max(first, second);
    }
}