package ru.practicum.ewm.requests.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.requests.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class RequestInternalController {

    private final RequestService requestService;

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(
            @PathVariable Long eventId) {

        return requestService.getEventRequests(eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request) {

        return requestService.changeRequestStatus(eventId, request);
    }

    @PostMapping("/confirmed-counts")
    public Map<Long, Long> getConfirmedCounts(
            @RequestBody List<Long> eventIds) {

        return requestService.getConfirmedCounts(eventIds);
    }

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(
            @PathVariable Long userId) {

        return requestService.getUserRequests(userId);
    }
}