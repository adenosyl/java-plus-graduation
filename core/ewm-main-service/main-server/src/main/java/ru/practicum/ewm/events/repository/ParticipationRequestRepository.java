package ru.practicum.ewm.events.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.events.model.ParticipationRequest;
import ru.practicum.ewm.events.model.RequestStatus;

import java.util.Collection;
import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByEvent_IdOrderByIdAsc(Long eventId);

    @Query("""
        select r.event.id as eventId, count(r.id) as cnt
        from ParticipationRequest r
        where r.event.id in :eventIds and r.status = :status
        group by r.event.id
    """)
    List<Object[]> countByEventIdsAndStatus(Collection<Long> eventIds, RequestStatus status);

    long countByEvent_IdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByIdIn(Collection<Long> ids);

    List<ParticipationRequest> findAllByEvent_IdAndStatus(Long eventId, RequestStatus status);

    boolean existsByEvent_IdAndRequester_Id(Long eventId, Long requesterId);

    List<ParticipationRequest> findAllByRequester_IdOrderByIdAsc(Long requesterId);

    java.util.Optional<ParticipationRequest> findByIdAndRequester_Id(Long id, Long requesterId);

}
