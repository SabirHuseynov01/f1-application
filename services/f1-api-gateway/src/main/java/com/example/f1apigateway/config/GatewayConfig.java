package com.example.f1apigateway.config;


import com.example.f1apigateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           JwtAuthenticationFilter jwtFilter) {
        return builder.routes()
                // Apply JWT filter to protected routes
                .route("session-protected", r -> r
                        .path("/api/v1/sessions/**", "/api/v1/seasons/**", "/api/v1/circuits/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://session-service"))
                .route("timing-protected", r -> r
                        .path("/api/timing/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://timing-service"))
                .build();
    }
}