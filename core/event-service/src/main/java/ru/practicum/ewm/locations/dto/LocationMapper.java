package ru.practicum.ewm.locations.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.locations.model.Location;

@UtilityClass
public class LocationMapper {
    public static Location toLocation(NewLocationDto newLocationDto) {
        Location location = new Location();
        location.setName(newLocationDto.getName());
        location.setLat(newLocationDto.getLat());
        location.setLon(newLocationDto.getLon());
        location.setRadiusMeters(newLocationDto.getRadiusMeters());
        return location;
    }

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(
                location.getId(),
                location.getName(),
                location.getLat(),
                location.getLon(),
                location.getRadiusMeters()
        );
    }
}
