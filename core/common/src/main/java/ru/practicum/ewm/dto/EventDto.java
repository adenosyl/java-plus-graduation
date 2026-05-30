package ru.practicum.ewm.dto;

import lombok.Data;

@Data
public class EventDto {

    private Long id;
    private Long initiatorId;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String state;
}