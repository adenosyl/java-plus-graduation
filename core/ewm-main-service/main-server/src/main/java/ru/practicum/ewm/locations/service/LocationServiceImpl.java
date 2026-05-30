package ru.practicum.ewm.locations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.mapper.EventMapper;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.locations.dto.LocationDto;
import ru.practicum.ewm.locations.dto.LocationMapper;
import ru.practicum.ewm.locations.dto.NewLocationDto;
import ru.practicum.ewm.locations.model.Location;
import ru.practicum.ewm.locations.repository.LocationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;

    @Override
    public LocationDto create(NewLocationDto newLocationDto) {
        Location location = LocationMapper.toLocation(newLocationDto);
        locationRepository.save(location);
        return LocationMapper.toLocationDto(location);
    }

    @Override
    public List<LocationDto> getAll() {
        return locationRepository.findAll().stream().map(LocationMapper::toLocationDto).toList();
    }

    @Override
    public LocationDto get(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location with id:" + id + " not found"));

        return LocationMapper.toLocationDto(location);
    }

    @Override
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new NotFoundException("Location with id:" + id + " not found");
        }
        locationRepository.deleteById(id);
    }

    @Override
    public List<EventShortDto> getEvents(Long locId) {
        Location location = locationRepository.findById(locId)
                .orElseThrow(() -> new NotFoundException("Location with id:" + locId + " not found"));

        List<Event> events = eventRepository
                .findPublishedEventsNear(location.getLat(), location.getLon(), location.getRadiusMeters());

        return events.stream().map(EventMapper::toShortDto).toList();
    }
}
