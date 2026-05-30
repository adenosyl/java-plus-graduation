package ru.practicum.ewm.requests.mapper;

import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.requests.model.ParticipationRequest;


public final class RequestMapper {

    private RequestMapper() {
    }

    public static ParticipationRequestDto toDto(ParticipationRequest r) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(r.getId());
        dto.setCreated(r.getCreated());
        dto.setEvent(r.getEventId());
        dto.setRequester(r.getRequesterId());
        dto.setStatus(r.getStatus().name());
        return dto;
    }
}
