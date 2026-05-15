package com.example.f1timingservice.client;


import com.example.f1timingservice.dto.openF1.OpenF1LapDTO;
import com.example.f1timingservice.dto.openF1.OpenF1StintDTO;
import com.example.f1timingservice.exception.OpenF1ApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenF1TimingClient {

    private final WebClient webClient;


    @CircuitBreaker(name = "openF1", fallbackMethod = "fetchLapsFallback")
    @Retry(name = "openF1")
    public List<OpenF1LapDTO> fetchLapsBySession(Integer sessionKey) {
        log.info("Laps pulled from the OpenF1 API - session: {}", sessionKey);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/laps")
                        .queryParam("session_key", sessionKey)
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new OpenF1ApiException("Open F1 API error: " + body))
                )
                .bodyToFlux(OpenF1LapDTO.class)
                .collectList()
                .block();
    }

    @CircuitBreaker(name = "openF1", fallbackMethod = "fetchStintsFallback")
    @Retry(name = "openF1")
    public List<OpenF1StintDTO> fetchStintsBySession(Integer sessionKey) {
        log.info("Stints pulled from the OpenF1 API - session: {}", sessionKey);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stints")
                        .queryParam("session_key", sessionKey)
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new OpenF1ApiException("OpenF1 API error: " + body))
                )
                .bodyToFlux(OpenF1StintDTO.class)
                .collectList()
                .block();
    }

    private List<OpenF1LapDTO> fetchLapsFallback(Integer sessionKey, Exception ex) {
        log.error("OpenF1 API fallback - session: {}, error: {}", sessionKey, ex.getMessage());
        throw new OpenF1ApiException("OpenF1 API is temporarily unavailable.", ex);
    }

    private List<OpenF1StintDTO> fetchStintsFallback(Integer sessionKey, Exception ex) {
        log.error("OpenF1 API fallback - session: {}, error: {}", sessionKey, ex.getMessage());
        throw new OpenF1ApiException("OpenF1 API is temporarily unavailable.", ex);
    }
}
