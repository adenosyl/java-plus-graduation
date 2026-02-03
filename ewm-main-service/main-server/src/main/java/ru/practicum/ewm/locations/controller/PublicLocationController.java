package ru.practicum.ewm.locations.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.locations.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicLocationController {
    private final LocationService locationService;

    @GetMapping("/{locId}/events")
    public ResponseEntity<List<EventShortDto>> getEvents(@PathVariable @Positive Long locId) {
        log.info("GET /locations/{}/events", locId);
        return ResponseEntity.ok(locationService.getEvents(locId));
    }

}
