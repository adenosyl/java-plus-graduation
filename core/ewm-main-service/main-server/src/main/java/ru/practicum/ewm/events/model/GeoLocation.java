package ru.practicum.ewm.events.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class GeoLocation {
    private Float lat;
    private Float lon;
}
