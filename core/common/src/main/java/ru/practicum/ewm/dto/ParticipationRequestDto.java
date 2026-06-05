package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ParticipationRequestDto(
    Long id,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime created,
    Long event,
    Long requester,
    String status
) {
}