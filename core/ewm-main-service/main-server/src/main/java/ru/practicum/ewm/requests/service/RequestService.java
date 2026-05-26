package ru.practicum.ewm.requests.service;

import ru.practicum.ewm.events.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto addParticipationRequest(long userId, long eventId);

    List<ParticipationRequestDto> getUserRequests(long userId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);
}
