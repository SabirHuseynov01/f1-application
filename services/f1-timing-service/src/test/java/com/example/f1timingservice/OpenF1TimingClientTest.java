package com.example.f1timingservice;

import com.example.f1timingservice.client.OpenF1TimingClient;
import com.example.f1timingservice.dto.openF1.OpenF1LapDTO;
import com.example.f1timingservice.dto.openF1.OpenF1StintDTO;
import com.example.f1timingservice.exception.OpenF1ApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OpenF1TimingClient Tests")
class OpenF1TimingClientTest {

    private MockWebServer mockWebServer;
    private OpenF1TimingClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        client = new OpenF1TimingClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    // ─── fetchLapsBySession ───────────────────────────────────────────────────

    @Test
    @DisplayName("fetchLapsBySession - uğurlu cavab - lap siyahısı qayıdır")
    void fetchLapsBySession_success_returnsLapList() {
        String json = """
                [
                  {
                    "session_key": 9158,
                    "driver_number": 44,
                    "lap_number": 1,
                    "lap_duration": 95.123,
                    "duration_sector_1": 28.1,
                    "duration_sector_2": 34.5,
                    "duration_sector_3": 32.4,
                    "is_pit_out_lap": false,
                    "segments_sector_1": [2048, 2049],
                    "segments_sector_2": [2049, 2052],
                    "segments_sector_3": [2049, 2051]
                  },
                  {
                    "session_key": 9158,
                    "driver_number": 44,
                    "lap_number": 2,
                    "lap_duration": 92.456,
                    "duration_sector_1": 27.3,
                    "duration_sector_2": 33.8,
                    "duration_sector_3": 31.3,
                    "is_pit_out_lap": false,
                    "segments_sector_1": [2048],
                    "segments_sector_2": [2049],
                    "segments_sector_3": [2051]
                  }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        List<OpenF1LapDTO> result = client.fetchLapsBySession(9158);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSessionKey()).isEqualTo(9158);
        assertThat(result.get(0).getDriverNumber()).isEqualTo(44);
        assertThat(result.get(0).getLapNumber()).isEqualTo(1);
        assertThat(result.get(0).getLapDuration()).isEqualTo(95.123);
        assertThat(result.get(0).getIsPitOutLap()).isFalse();
        assertThat(result.get(1).getLapNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("fetchLapsBySession ")
    void fetchLapsBySession_emptyResponse_returnsEmptyList() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        List<OpenF1LapDTO> result = client.fetchLapsBySession(9999);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("fetchLapsBySession - 4xx xəta - OpenF1ApiException atılır")
    void fetchLapsBySession_4xxError_throwsOpenF1ApiException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.fetchLapsBySession(9158))
                .isInstanceOf(OpenF1ApiException.class)
                .hasMessageContaining("Open F1 API error");
    }

    @Test
    @DisplayName("fetchLapsBySession - 5xx xəta - OpenF1ApiException atılır")
    void fetchLapsBySession_5xxError_throwsOpenF1ApiException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setBody("Service Unavailable")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.fetchLapsBySession(9158))
                .isInstanceOf(OpenF1ApiException.class)
                .hasMessageContaining("Open F1 API error");
    }

    @Test
    @DisplayName("fetchLapsBySession - isPitOutLap true olduqda düzgün map olunur")
    void fetchLapsBySession_pitOutLap_mappedCorrectly() {
        String json = """
                [
                  {
                    "session_key": 9158,
                    "driver_number": 11,
                    "lap_number": 5,
                    "lap_duration": 110.0,
                    "is_pit_out_lap": true,
                    "segments_sector_1": [],
                    "segments_sector_2": [],
                    "segments_sector_3": []
                  }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        List<OpenF1LapDTO> result = client.fetchLapsBySession(9158);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsPitOutLap()).isTrue();
        assertThat(result.get(0).getDriverNumber()).isEqualTo(11);
    }

    // ─── fetchStintsBySession ─────────────────────────────────────────────────

    @Test
    @DisplayName("fetchStintsBySession - uğurlu cavab - stint siyahısı qayıdır")
    void fetchStintsBySession_success_returnsStintList() {
        String json = """
                [
                  {
                    "session_key": 9158,
                    "driver_number": 1,
                    "stint_number": 1,
                    "lap_start": 1,
                    "lap_end": 22,
                    "compound": "MEDIUM",
                    "tyre_age_at_start": 0
                  },
                  {
                    "session_key": 9158,
                    "driver_number": 1,
                    "stint_number": 2,
                    "lap_start": 23,
                    "lap_end": 50,
                    "compound": "HARD",
                    "tyre_age_at_start": 3
                  }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        List<OpenF1StintDTO> result = client.fetchStintsBySession(9158);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCompound()).isEqualTo("MEDIUM");
        assertThat(result.get(0).getLapStart()).isEqualTo(1);
        assertThat(result.get(0).getLapEnd()).isEqualTo(22);
        assertThat(result.get(1).getCompound()).isEqualTo("HARD");
        assertThat(result.get(1).getTyreAgeAtStart()).isEqualTo(3);
    }

    @Test
    @DisplayName("fetchStintsBySession - boş siyahı qayıdır")
    void fetchStintsBySession_emptyResponse_returnsEmptyList() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        List<OpenF1StintDTO> result = client.fetchStintsBySession(9999);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("fetchStintsBySession - 5xx xəta - OpenF1ApiException atılır")
    void fetchStintsBySession_5xxError_throwsOpenF1ApiException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.fetchStintsBySession(9158))
                .isInstanceOf(OpenF1ApiException.class)
                .hasMessageContaining("OpenF1 API error");
    }

    @Test
    @DisplayName("fetchStintsBySession - bütün compound növləri düzgün map olunur")
    void fetchStintsBySession_allCompounds_mappedCorrectly() {
        String json = """
                [
                  {"session_key": 9158, "driver_number": 16, "stint_number": 1,
                   "lap_start": 1, "lap_end": 10, "compound": "SOFT", "tyre_age_at_start": 0},
                  {"session_key": 9158, "driver_number": 16, "stint_number": 2,
                   "lap_start": 11, "lap_end": 20, "compound": "INTER", "tyre_age_at_start": 0},
                  {"session_key": 9158, "driver_number": 16, "stint_number": 3,
                   "lap_start": 21, "lap_end": 30, "compound": "WET", "tyre_age_at_start": 0}
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        List<OpenF1StintDTO> result = client.fetchStintsBySession(9158);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(OpenF1StintDTO::getCompound)
                .containsExactly("SOFT", "INTER", "WET");
    }
}
