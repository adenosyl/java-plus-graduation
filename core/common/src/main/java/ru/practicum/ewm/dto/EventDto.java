package ru.practicum.ewm.dto;

public record EventDto(
    Long id,
    Long initiatorId,
    Integer participantLimit,
    Boolean requestModeration,
    EventStateDto state
) {
}