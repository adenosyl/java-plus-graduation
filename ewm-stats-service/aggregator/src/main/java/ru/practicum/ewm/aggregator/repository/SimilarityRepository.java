package ru.practicum.ewm.aggregator.repository;

import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
public class SimilarityRepository {

    private final Set<String> similarities = new HashSet<>();

    public boolean add(Long eventA, Long eventB) {

        return similarities.add(
                eventA + "-" + eventB
        );
    }
}