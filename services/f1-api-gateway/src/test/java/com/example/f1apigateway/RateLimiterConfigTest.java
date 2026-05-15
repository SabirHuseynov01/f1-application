package com.example.f1apigateway;

import com.example.f1apigateway.config.RateLimiterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RateLimiterConfig Tests")
class RateLimiterConfigTest {

    private RateLimiterConfig config;

    @BeforeEach
    void setUp() {
        config = new RateLimiterConfig();
    }

    @Test
    @DisplayName("userKeyResolver - uses username from header if present")
    void userKeyResolver_withUsernameHeader_usesUsername() {
        KeyResolver resolver = config.userKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-User-Username", "verstappen")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .assertNext(key -> assertThat(key).isEqualTo("verstappen"))
                .verifyComplete();
    }

    @Test
    @DisplayName("userKeyResolver - falls back to IP when no username")
    void userKeyResolver_withoutUsername_usesIp() {
        KeyResolver resolver = config.userKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .remoteAddress(new java.net.InetSocketAddress("192.168.1.100", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .assertNext(key -> assertThat(key).isEqualTo("192.168.1.100"))
                .verifyComplete();
    }

    @Test
    @DisplayName("userKeyResolver - bean is created")
    void userKeyResolver_beanCreated() {
        assertThat(config.userKeyResolver()).isNotNull();
    }
}
