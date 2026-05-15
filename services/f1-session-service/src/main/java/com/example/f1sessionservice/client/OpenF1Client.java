package com.example.f1sessionservice.client;


import com.example.f1sessionservice.dto.openF1.OpenF1DriverDTO;
import com.example.f1sessionservice.dto.openF1.OpenF1SessionDTO;
import com.example.f1sessionservice.exception.OpenF1ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class OpenF1Client {

    private final WebClient webClient;

    public OpenF1Client(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.openf1.org/v1")
                .build();
    }

    public List<OpenF1SessionDTO> fetchSessionsByYear(Integer year) {
        log.info("All sessions from OpenF1 for {} years are being pulled...", year);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sessions")
                        .queryParam("year", year)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> {
                            log.error("OpenF1 4xx error: {}", response.statusCode());
                            return Mono.error(new OpenF1ApiException("OpenF1 client error: " + response.statusCode()));
                })
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> {
                    log.error("OpenF1 5xx error (drivers): {}", response.statusCode());
                    return Mono.error(new OpenF1ApiException("OpenF1 server error: " + response.statusCode()));
                        })
                .bodyToFlux(OpenF1SessionDTO.class)
                .collectList()
                .block();
    }

    public List<OpenF1DriverDTO> fetchDriversBySession(Integer sessionKey) {
        log.info("All drivers from OpenF1 for session {} are being pulled...", sessionKey);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/drivers")
                        .queryParam("session_key", sessionKey)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> {
                    log.error("OpenF1 4xx error (drivers): {}", response.statusCode());
                    return Mono.error(new OpenF1ApiException("OpenF1 client error: " + response.statusCode()));
                        })
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> {
                    log.error("OpenF1 5xx error (drivers): {}", response.statusCode());
                    return Mono.error(new OpenF1ApiException("OpenF1 server error: " + response.statusCode()));
                        })
                .bodyToFlux(OpenF1DriverDTO.class)
                .collectList()
                .block();
    }

}

