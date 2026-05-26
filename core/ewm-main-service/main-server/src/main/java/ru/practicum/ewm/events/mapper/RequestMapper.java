package ru.practicum.ewm.events.mapper;

import ru.practicum.ewm.events.dto.ParticipationRequestDto;
import ru.practicum.ewm.events.model.ParticipationRequest;

public final class RequestMapper {

    private RequestMapper() {
    }

    public static ParticipationRequestDto toDto(ParticipationRequest r) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(r.getId());
        dto.setCreated(r.getCreated());
        dto.setEvent(r.getEvent().getId());
        dto.setRequester(r.getRequester().getId());
        dto.setStatus(r.getStatus());
        return dto;
    }
}
