package ru.practicum.ewm.events.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EventDto;
import ru.practicum.ewm.events.service.EventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class InternalEventController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventDto getEvent(@PathVariable Long eventId) {
        return eventService.getInternalEvent(eventId);
    }
}