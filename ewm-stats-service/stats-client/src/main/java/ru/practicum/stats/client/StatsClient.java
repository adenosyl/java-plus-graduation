package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {

    private static final String STATS_SERVICE_ID = "STATS-SERVER";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DiscoveryClient discoveryClient;

    @Autowired
    public StatsClient(DiscoveryClient discoveryClient,
                       RestTemplateBuilder builder) {
        super(builder.build());
        this.discoveryClient = discoveryClient;
    }

    public ResponseEntity<Object> saveHit(EndpointHitDto hitDto) {
        return post(makeUrl("/hit"), hitDto);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start,
                                           LocalDateTime end,
                                           List<String> uris,
                                           Boolean unique) {

        String path = "/stats?start={start}&end={end}";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start.format(FORMATTER));
        parameters.put("end", end.format(FORMATTER));

        if (uris != null && !uris.isEmpty()) {
            path += "&uris={uris}";
            parameters.put("uris", String.join(",", uris));
        }

        if (unique != null) {
            path += "&unique={unique}";
            parameters.put("unique", unique);
        }

        return get(makeUrl(path), parameters);
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances(STATS_SERVICE_ID)
                    .getFirst();
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Ошибка обнаружения сервиса статистики: " + STATS_SERVICE_ID,
                    exception
            );
        }
    }

    private String makeUrl(String path) {
        ServiceInstance instance = getInstance();

        return "http://"
                + instance.getHost()
                + ":"
                + instance.getPort()
                + path;
    }
}