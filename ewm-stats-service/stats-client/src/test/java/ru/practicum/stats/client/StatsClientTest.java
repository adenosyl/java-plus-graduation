package ru.practicum.stats.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.EndpointHitDto;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DiscoveryClient discoveryClient;

    @Mock
    private ServiceInstance serviceInstance;

    private StatsClient statsClient;

    @BeforeEach
    void setUp() throws Exception {

        RestTemplateBuilder builder = new RestTemplateBuilder();

        when(discoveryClient.getInstances("STATS-SERVER"))
                .thenReturn(List.of(serviceInstance));

        when(serviceInstance.getHost())
                .thenReturn("localhost");

        when(serviceInstance.getPort())
                .thenReturn(9090);

        statsClient = new StatsClient(discoveryClient, builder);

        Field restField = BaseClient.class.getDeclaredField("rest");
        restField.setAccessible(true);
        restField.set(statsClient, restTemplate);
    }

    @Test
    void saveHit_shouldWork() {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app("test")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp("2024-01-15 10:00:00")
                .build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = statsClient.saveHit(hitDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getStats_shouldCallCorrectEndpoint() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(ResponseEntity.ok(List.of()));

        ResponseEntity<Object> response = statsClient.getStats(start, end, null, null);

        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void getStats_withUris_shouldWork() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);
        List<String> uris = List.of("/events", "/events/1");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(ResponseEntity.ok(List.of()));

        ResponseEntity<Object> response = statsClient.getStats(start, end, uris, true);

        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void getStats_shouldHandleServerError() {

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenThrow(new org.springframework.web.client.HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request"
        ));

        ResponseEntity<Object> response = statsClient.getStats(start, end, null, null);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}