package ru.practicum.ewm.events.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.model.EventState;
import ru.practicum.ewm.events.model.PublicEventSort;

import java.util.List;

public interface EventService {

    // public
    List<EventShortDto> getPublicEvents(String text,
                                        List<Long> categories,
                                        Boolean paid,
                                        String rangeStart,
                                        String rangeEnd,
                                        Boolean onlyAvailable,
                                        PublicEventSort sort,
                                        int from,
                                        int size,
                                        HttpServletRequest request);

    EventFullDto getPublicEvent(long id, HttpServletRequest request);

    // private
    List<EventShortDto> getUserEvents(long userId, int from, int size);

    EventFullDto addEvent(long userId, NewEventDto dto);

    EventFullDto getUserEvent(long userId, long eventId);

    EventFullDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest dto);

    List<ParticipationRequestDto> getEventParticipants(long userId, long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(long userId, long eventId, EventRequestStatusUpdateRequest dto);

    // admin
    List<EventFullDto> searchAdmin(List<Long> users,
                                   List<EventState> states,
                                   List<Long> categories,
                                   String rangeStart,
                                   String rangeEnd,
                                   int from,
                                   int size);

    EventFullDto updateAdmin(long eventId, UpdateEventAdminRequest dto);
}
