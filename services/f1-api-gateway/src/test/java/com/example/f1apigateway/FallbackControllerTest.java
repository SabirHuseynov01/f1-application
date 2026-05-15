package com.example.f1apigateway;

import com.example.f1apigateway.controller.FallbackController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FallbackController Tests")
class FallbackControllerTest {

    private FallbackController fallbackController;

    @BeforeEach
    void setUp() {
        fallbackController = new FallbackController();
    }

    @Test
    @DisplayName("sessionFallback - returns 503 with message")
    void sessionFallback_returnsServiceUnavailable() {
        ResponseEntity<Map<String, Object>> response = fallbackController.sessionFallback();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsKey("success");
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message").toString()).contains("Session service");
    }

    @Test
    @DisplayName("timingFallback - returns 503 with message")
    void timingFallback_returnsServiceUnavailable() {
        ResponseEntity<Map<String, Object>> response = fallbackController.timingFallback();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message").toString()).contains("Timing service");
    }

    @Test
    @DisplayName("fallback responses contain timestamp")
    void fallbackResponses_containTimestamp() {
        ResponseEntity<Map<String, Object>> response = fallbackController.sessionFallback();
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    @DisplayName("fallback responses contain error field")
    void fallbackResponses_containErrorField() {
        ResponseEntity<Map<String, Object>> response = fallbackController.timingFallback();
        assertThat(response.getBody()).containsKey("error");
    }

    @Test
    @DisplayName("session and timing fallbacks have different messages")
    void fallbacks_haveDifferentMessages() {
        ResponseEntity<Map<String, Object>> sessionResponse = fallbackController.sessionFallback();
        ResponseEntity<Map<String, Object>> timingResponse = fallbackController.timingFallback();

        assertThat(sessionResponse.getBody().get("message"))
                .isNotEqualTo(timingResponse.getBody().get("message"));
    }
}
