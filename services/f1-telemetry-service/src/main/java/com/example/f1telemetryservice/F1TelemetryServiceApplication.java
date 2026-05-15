package com.example.f1telemetryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableCaching
@EnableKafka
public class F1TelemetryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(F1TelemetryServiceApplication.class, args);
    }

}
