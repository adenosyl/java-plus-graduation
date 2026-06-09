package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.dto.EventDto;

@FeignClient(name = "event-service")
public interface EventFeignClient {

    @GetMapping("/internal/events/{eventId}")
    EventDto getEvent(@PathVariable Long eventId);
}