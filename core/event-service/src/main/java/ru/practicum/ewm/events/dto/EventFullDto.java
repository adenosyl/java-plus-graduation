package ru.practicum.ewm.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.ewm.events.model.EventState;

import java.time.LocalDateTime;

@Data
public class EventFullDto {
    private Long id;

    private String title;
    private String annotation;
    private String description;

    private CategoryDto category;
    private UserShortDto initiator;
    private Location location;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;

    private EventState state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private Long confirmedRequests;
    private Long views;
}
