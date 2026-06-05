package ru.practicum.ewm.requests.mapper;

import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.requests.model.ParticipationRequest;


public final class RequestMapper {

    private RequestMapper() {
    }

    public static ParticipationRequestDto toDto(ParticipationRequest r) {
        return new ParticipationRequestDto(
            r.getId(),
            r.getCreated(),
            r.getEventId(),
            r.getRequesterId(),
            r.getStatus().name()
        );
    }
}
