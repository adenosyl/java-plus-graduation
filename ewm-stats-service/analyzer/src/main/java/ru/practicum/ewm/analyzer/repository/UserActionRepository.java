package ru.practicum.ewm.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.analyzer.model.UserActionEntity;

import java.util.List;

public interface UserActionRepository
        extends JpaRepository<UserActionEntity, Long> {

    long countByEventId(Long eventId);

    List<UserActionEntity> findByUserId(Long userId);

    List<UserActionEntity> findByEventId(Long eventId);
}