package ru.practicum.ewm.requests.service;

import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface RequestService {

    ParticipationRequestDto addParticipationRequest(long userId, long eventId);

    List<ParticipationRequestDto> getUserRequests(long userId);

    List<ParticipationRequestDto> getEventRequests(Long eventId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);

    EventRequestStatusUpdateResult changeRequestStatus(
            Long eventId,
            EventRequestStatusUpdateRequest request);

    Map<Long, Long> getConfirmedCounts(List<Long> eventIds);
}
