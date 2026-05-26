package ru.practicum.ewm.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.ewm.events.model.AdminStateAction;

import java.time.LocalDateTime;

@Data
public class UpdateEventAdminRequest {

    @Size(min = 20, max = 2000)
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @Valid
    private Location location;

    private Boolean paid;

    @Min(0)
    private Integer participantLimit;

    private Boolean requestModeration;

    private AdminStateAction stateAction;

    @Size(min = 3, max = 120)
    private String title;
}
