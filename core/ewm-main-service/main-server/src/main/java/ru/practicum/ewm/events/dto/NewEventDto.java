package ru.practicum.ewm.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewEventDto {

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotNull
    private Long category;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull
    @Valid
    private Location location;

    private Boolean paid = false;

    @Min(0)
    private Integer participantLimit = 0;

    private Boolean requestModeration = true;

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
}
