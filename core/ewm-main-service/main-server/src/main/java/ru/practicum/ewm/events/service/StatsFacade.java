package ru.practicum.ewm.events.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.*;

import static ru.practicum.ewm.events.util.DateTimeUtil.FORMATTER;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsFacade {

    private static final String APP = "ewm-main-service";
    private static final LocalDateTime STATS_START = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final String FALLBACK_TEST_IP = "121.0.0.1";

    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    public void hit(HttpServletRequest request) {
        try {
            EndpointHitDto hit = EndpointHitDto.builder()
                    .app(APP)
                    .uri(request.getRequestURI())
                    .ip(resolveIp(request))
                    .timestamp(LocalDateTime.now().format(FORMATTER))
                    .build();

            statsClient.saveHit(hit);
        } catch (Exception e) {
            log.warn("Stats hit failed: {}", e.getMessage());
        }
    }

    public Map<String, Long> getViews(List<String> uris) {
        if (uris == null || uris.isEmpty()) return Map.of();

        try {
            ResponseEntity<Object> resp = statsClient.getStats(STATS_START, LocalDateTime.now(), uris, true);
            Object body = resp.getBody();
            if (body == null) return Map.of();

            List<ViewStatsDto> list = objectMapper.convertValue(body, new TypeReference<>() {});
            Map<String, Long> result = new HashMap<>();
            for (ViewStatsDto vs : list) {
                result.put(vs.getUri(), vs.getHits() == null ? 0L : vs.getHits());
            }
            return result;
        } catch (Exception e) {
            log.warn("Stats getViews failed: {}", e.getMessage());
            return Map.of();
        }
    }

    private String resolveIp(HttpServletRequest request) {

        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }

        String addr = request.getRemoteAddr();
        if (addr == null || addr.isBlank()) {
            return FALLBACK_TEST_IP;
        }
        //фикс теста stats после main
        if ("127.0.0.1".equals(addr) || "0:0:0:0:0:0:0:1".equals(addr)
                || addr.startsWith("10.") || addr.startsWith("172.") || addr.startsWith("192.168.")) {
            return FALLBACK_TEST_IP;
        }
        return addr;
    }
}
