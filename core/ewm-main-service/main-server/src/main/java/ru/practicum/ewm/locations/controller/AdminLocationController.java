package ru.practicum.ewm.locations.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.ewm.locations.dto.LocationDto;
import ru.practicum.ewm.locations.dto.NewLocationDto;
import ru.practicum.ewm.locations.service.LocationService;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/locations")
@Validated
public class AdminLocationController {
    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationDto> create(@RequestBody @Valid NewLocationDto newLocationDto) {
        log.info("POST /admin/locations: {}", newLocationDto);

        LocationDto body = locationService.create(newLocationDto);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(body.getId())
                .toUri();

        return ResponseEntity.created(uri).body(body);
    }

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAll() {
        log.info("GET /admin/locations");
        return ResponseEntity.ok(locationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> get(@PathVariable @Positive Long id) {
        log.info("GET /admin/locations/{}", id);
        return ResponseEntity.ok(locationService.get(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        log.info("DELETE /admin/locations/{}", id);
        locationService.delete(id);
    }
}
