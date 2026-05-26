package ru.practicum.ewm.locations.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class NewLocationDto {
    @NotBlank(message = "name is required")
    private String name;
    @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
    @DecimalMin(value = "-90", message = "Latitude must be between -09 and 90")
    @NotNull
    private Double lat;
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
    @NotNull
    private Double lon;
    @NotNull
    @Positive
    @Min(value = 1, message = "Radius must be at least 1 meter")
    private Integer radiusMeters;
}
