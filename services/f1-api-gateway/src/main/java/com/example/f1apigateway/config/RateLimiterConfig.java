package com.example.f1apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
            if (username != null) {
                return Mono.just(username);
            }
            return Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
        };
    }
}
