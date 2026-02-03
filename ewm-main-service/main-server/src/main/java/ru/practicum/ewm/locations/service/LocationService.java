package ru.practicum.ewm.locations.service;

import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.locations.dto.LocationDto;
import ru.practicum.ewm.locations.dto.NewLocationDto;

import java.util.List;

public interface LocationService {
    LocationDto create(NewLocationDto newLocationDto);

    List<LocationDto> getAll();

    LocationDto get(Long id);

    void delete(Long id);

    List<EventShortDto> getEvents(Long locId);
}
