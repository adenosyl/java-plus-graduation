package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service")
public interface RequestFeignClient {

    @GetMapping("/internal/events/{eventId}/requests")
    List<ParticipationRequestDto> getEventRequests(
            @PathVariable Long eventId
    );

    @PatchMapping("/internal/events/{eventId}/requests")
    EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request
    );

    @PostMapping("/internal/events/confirmed-counts")
    Map<Long, Long> getConfirmedCounts(
            @RequestBody List<Long> eventIds
    );

    @GetMapping("/internal/events/users/{userId}/requests")
    List<ParticipationRequestDto> getUserRequests(
            @PathVariable Long userId
    );
}