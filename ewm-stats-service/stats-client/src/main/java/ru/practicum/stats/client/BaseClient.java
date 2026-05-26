package ru.practicum.stats.client;

import jakarta.annotation.Nullable;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class BaseClient {

    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String url) {
        return makeAndSendRequest(HttpMethod.GET, url, null, null);
    }

    protected ResponseEntity<Object> get(String url, @Nullable Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, url, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(String url, @Nullable T body) {
        return makeAndSendRequest(HttpMethod.POST, url, null, body);
    }

    protected <T> ResponseEntity<Object> post(String url,
                                              @Nullable Map<String, Object> parameters,
                                              @Nullable T body) {
        return makeAndSendRequest(HttpMethod.POST, url, parameters, body);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method,
                                                          String url,
                                                          @Nullable Map<String, Object> parameters,
                                                          @Nullable T body) {

        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        try {
            if (parameters != null && !parameters.isEmpty()) {
                return rest.exchange(url, method, requestEntity, Object.class, parameters);
            }

            return rest.exchange(url, method, requestEntity, Object.class);

        } catch (HttpStatusCodeException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsByteArray());
        }
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}