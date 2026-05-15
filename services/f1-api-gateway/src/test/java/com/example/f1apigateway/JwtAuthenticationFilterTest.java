package com.example.f1apigateway;

import com.example.f1apigateway.filter.JwtAuthenticationFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private static final String SECRET = "gateway-secret-key-that-is-very-long-for-hs512-2026-f1";
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(SECRET);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    private String generateToken(String username, String role, long expirationOffset) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationOffset))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    @Test
    @DisplayName("Public path - allows without token")
    void publicPath_noToken_allows() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                .filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Protected path - valid token adds headers")
    void protectedPath_validToken_addsHeaders() {
        String token = generateToken("verstappen", "USER", 3600000);
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/sessions/year/2024")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                .filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(argThat(ex -> {
            ServerHttpRequest req = ex.getRequest();
            return "verstappen".equals(req.getHeaders().getFirst("X-User-Username"))
                    && "USER".equals(req.getHeaders().getFirst("X-User-Role"));
        }));
    }

    @Test
    @DisplayName("Protected path - missing token returns 401")
    void protectedPath_missingToken_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/timing/laps/9158")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                .filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Protected path - expired token returns 401")
    void protectedPath_expiredToken_returns401() {
        String expiredToken = generateToken("hamilton", "USER", -1000);
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/sessions/key/9523")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                .filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Protected path - invalid token format returns 401")
    void protectedPath_invalidToken_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/telemetry/snapshot/9158")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                .filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Protected path - malformed Authorization header returns 401")
    void protectedPath_malformedHeader_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/sessions/year/2024")
                .header(HttpHeaders.AUTHORIZATION, "Basic invalid")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                .filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Health endpoint - allows without auth")
    void healthEndpoint_allowsWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                .filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Register endpoint - allows without auth")
    void registerEndpoint_allowsWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/v1/auth/register")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                .filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Fallback endpoint - allows without auth")
    void fallbackEndpoint_allowsWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/fallback/session")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                .filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }
}
