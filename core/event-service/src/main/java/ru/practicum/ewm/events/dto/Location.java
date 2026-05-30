package ru.practicum.ewm.events.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Location {
    @NotNull
    private Float lat;

    @NotNull
    private Float lon;
}
