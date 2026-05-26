package ru.practicum.ewm.events.mapper;

import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.GeoLocation;

public final class EventMapper {

    private EventMapper() {
    }

    public static Location toDto(GeoLocation loc) {
        Location dto = new Location();
        dto.setLat(loc.getLat());
        dto.setLon(loc.getLon());
        return dto;
    }

    public static GeoLocation toEmb(Location dto) {
        GeoLocation emb = new GeoLocation();
        emb.setLat(dto.getLat());
        emb.setLon(dto.getLon());
        return emb;
    }

    public static EventShortDto toShortDto(Event e) {
        EventShortDto dto = new EventShortDto();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setAnnotation(e.getAnnotation());
        dto.setEventDate(e.getEventDate());
        dto.setPaid(e.getPaid());
        return dto;
    }

    public static EventFullDto toFullDto(Event e) {
        EventFullDto dto = new EventFullDto();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setAnnotation(e.getAnnotation());
        dto.setDescription(e.getDescription());
        dto.setEventDate(e.getEventDate());
        dto.setCreatedOn(e.getCreatedOn());
        dto.setPublishedOn(e.getPublishedOn());
        dto.setPaid(e.getPaid());
        dto.setParticipantLimit(e.getParticipantLimit());
        dto.setRequestModeration(e.getRequestModeration());
        dto.setState(e.getState());
        dto.setLocation(toDto(e.getLocation()));
        return dto;
    }
}
