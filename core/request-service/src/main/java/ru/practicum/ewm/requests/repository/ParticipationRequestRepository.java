package ru.practicum.ewm.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.requests.model.ParticipationRequest;
import ru.practicum.ewm.requests.model.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByEventIdOrderByIdAsc(Long eventId);

    @Query("""
    select r.eventId as eventId,
           count(r.id) as cnt
    from ParticipationRequest r
    where r.eventId in :eventIds
      and r.status = :status
    group by r.eventId
""")

    List<EventConfirmedCount> countByEventIdsAndStatus(Collection<Long> eventIds, RequestStatus status);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByIdIn(Collection<Long> ids);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<ParticipationRequest> findAllByRequesterIdOrderByIdAsc(Long requesterId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long id, Long requesterId);
}